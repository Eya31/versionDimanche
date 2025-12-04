package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SGII_Ville.dto.VerificationAffectationDTO;
import tn.SGII_Ville.entities.Intervention;
import tn.SGII_Ville.entities.MainDOeuvre;
import tn.SGII_Ville.entities.Tache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour vérifier la validité d'une affectation de main-d'œuvre
 */
@Service
public class MainDOeuvreVerificationService {

    @Autowired
    private MainDOeuvreXmlService mainDOeuvreService;

    @Autowired
    private InterventionXmlService interventionService;

    @Autowired
    private TacheXmlService tacheService;

    /**
     * Vérifie si un agent peut être affecté à une intervention
     */
    public VerificationAffectationDTO verifierAffectation(
            MainDOeuvre mainDOeuvre, 
            Intervention intervention,
            List<Intervention> toutesInterventions) {
        
        VerificationAffectationDTO result = new VerificationAffectationDTO();
        List<String> erreurs = new ArrayList<>();
        List<String> avertissements = new ArrayList<>();

        // 1. Vérifier disponibilité
        if (!"LIBRE".equals(mainDOeuvre.getDisponibilite())) {
            erreurs.add("L'agent n'est pas disponible (statut: " + mainDOeuvre.getDisponibilite() + ")");
            result.setDisponible(false);
        } else {
            result.setDisponible(true);
        }

        // 2. Vérifier compétence (obligatoire selon le schéma XSD)
        if (mainDOeuvre.getCompetence() == null || mainDOeuvre.getCompetence().isEmpty()) {
            avertissements.add("L'agent n'a aucune compétence enregistrée");
            result.setCompetencesOk(false);
        } else {
            result.setCompetencesOk(true);
        }

        // 3. Vérifier habilitations et dates d'expiration
        // Les habilitations ne sont plus dans le nouveau schéma XSD
        // On considère toujours les habilitations comme valides
        result.setHabilitationsOk(true);

        // 4. Vérifier conflits d'horaires
        boolean pasDeConflit = verifierConflitsHoraires(mainDOeuvre, intervention, toutesInterventions, erreurs);
        result.setPasDeConflit(pasDeConflit);

        // 5. Vérifier si déjà affecté ailleurs
        boolean pasDejaAffecte = verifierPasDejaAffecte(mainDOeuvre, intervention, toutesInterventions, erreurs);
        result.setPasDejaAffecte(pasDejaAffecte);

        // 6. Vérifier horaires de travail
        boolean horairesOk = verifierHorairesTravail(mainDOeuvre, intervention, erreurs);
        result.setHorairesOk(horairesOk);

        result.setErreurs(erreurs);
        result.setAvertissements(avertissements);

        return result;
    }

    private boolean verifierConflitsHoraires(
            MainDOeuvre mainDOeuvre, 
            Intervention intervention,
            List<Intervention> toutesInterventions,
            List<String> erreurs) {
        
        if (intervention.getDatePlanifiee() == null) {
            return true; // Pas de date planifiée = pas de conflit possible
        }

        LocalDateTime dateIntervention = intervention.getDatePlanifiee().atStartOfDay();
        if (dateIntervention == null) return true;

        for (Intervention autreIntervention : toutesInterventions) {
            if (autreIntervention.getId() == intervention.getId()) continue;
            if (autreIntervention.getOuvrierIds() == null || 
                !autreIntervention.getOuvrierIds().contains(mainDOeuvre.getId())) continue;
            if (autreIntervention.getDatePlanifiee() == null) continue;

            LocalDateTime dateAutre = autreIntervention.getDatePlanifiee().atStartOfDay();
            if (dateAutre == null) continue;

            // Vérifier chevauchement (même jour)
            if (dateIntervention.toLocalDate().equals(dateAutre.toLocalDate())) {
                erreurs.add("Conflit avec l'intervention #" + autreIntervention.getId() + " le " + dateAutre.toLocalDate());
                return false;
            }
        }

        return true;
    }

    private boolean verifierPasDejaAffecte(
            MainDOeuvre mainDOeuvre,
            Intervention intervention,
            List<Intervention> toutesInterventions,
            List<String> erreurs) {
        
        // Vérifier si déjà dans cette intervention
        if (intervention.getOuvrierIds() != null && 
            intervention.getOuvrierIds().contains(mainDOeuvre.getId())) {
            
            // Si la main-d'œuvre est déjà affectée, vérifier si toutes ses tâches sont vérifiées
            // Si toutes les tâches sont vérifiées, on permet la réaffectation à une nouvelle tâche
            List<Tache> tachesDeLaMainDOeuvre = tacheService.findByInterventionId(intervention.getId())
                .stream()
                .filter(t -> t.getMainDOeuvreId() != null && t.getMainDOeuvreId() == mainDOeuvre.getId())
                .collect(java.util.stream.Collectors.toList());
            
            // Vérifier si toutes les tâches sont vérifiées
            boolean toutesTachesVerifiees = tachesDeLaMainDOeuvre.isEmpty() || 
                tachesDeLaMainDOeuvre.stream().allMatch(t -> 
                    "VERIFIEE".equals(t.getEtat()) || t.isVerifiee()
                );
            
            if (!toutesTachesVerifiees) {
                // Il y a au moins une tâche non vérifiée, on refuse l'affectation
                long tachesNonVerifiees = tachesDeLaMainDOeuvre.stream()
                    .filter(t -> !"VERIFIEE".equals(t.getEtat()) && !t.isVerifiee())
                    .count();
                erreurs.add("L'agent est déjà affecté à cette intervention avec " + tachesNonVerifiees + " tâche(s) non vérifiée(s)");
                return false;
            }
            
            // Toutes les tâches sont vérifiées, on permet la réaffectation
            // Ne pas retourner d'erreur, permettre la création d'une nouvelle tâche
        }

        return true;
    }

    private boolean verifierHorairesTravail(
            MainDOeuvre mainDOeuvre,
            Intervention intervention,
            List<String> erreurs) {
        
        // Les horaires de travail ne sont plus dans le nouveau schéma XSD
        // Cette vérification est désactivée mais la méthode est conservée pour compatibilité
        // On accepte toujours les affectations
        return true;
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            // Essayer différents formats
            if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr);
            } else {
                LocalDate date = LocalDate.parse(dateStr);
                return date.atStartOfDay();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Vérifie et met à jour le statut d'un agent selon ses habilitations
     * Note: Les habilitations ne sont plus dans le schéma XSD, mais on garde pour compatibilité
     */
    public void verifierEtMettreAJourStatut(MainDOeuvre mainDOeuvre) {
        // Les habilitations ne sont plus dans le nouveau schéma XSD
        // Cette méthode est conservée pour compatibilité mais ne fait rien
    }
}

