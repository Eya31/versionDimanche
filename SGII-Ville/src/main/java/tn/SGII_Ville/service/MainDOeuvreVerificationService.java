package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SGII_Ville.dto.VerificationAffectationDTO;
import tn.SGII_Ville.entities.Intervention;
import tn.SGII_Ville.entities.MainDOeuvre;
import tn.SGII_Ville.entities.Tache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        if (!"DISPONIBLE".equals(mainDOeuvre.getDisponibilite())) {
            erreurs.add("L'agent n'est pas disponible (statut: " + mainDOeuvre.getDisponibilite() + ")");
            result.setDisponible(false);
        } else {
            result.setDisponible(true);
        }

        // 2. Vérifier compétences (si l'intervention nécessite des compétences spécifiques)
        // Pour l'instant, on accepte si l'agent a au moins une compétence
        if (mainDOeuvre.getCompetences() == null || mainDOeuvre.getCompetences().isEmpty()) {
            avertissements.add("L'agent n'a aucune compétence enregistrée");
            result.setCompetencesOk(false);
        } else {
            result.setCompetencesOk(true);
        }

        // 3. Vérifier habilitations et dates d'expiration
        boolean habilitationsValides = true;
        if (mainDOeuvre.getHabilitationsExpiration() != null) {
            LocalDate aujourdhui = LocalDate.now();
            for (Map.Entry<String, LocalDate> entry : mainDOeuvre.getHabilitationsExpiration().entrySet()) {
                LocalDate expiration = entry.getValue();
                if (expiration != null && expiration.isBefore(aujourdhui)) {
                    erreurs.add("Habilitation '" + entry.getKey() + "' expirée depuis le " + expiration);
                    habilitationsValides = false;
                } else if (expiration != null && expiration.isBefore(aujourdhui.plusDays(30))) {
                    avertissements.add("Habilitation '" + entry.getKey() + "' expire le " + expiration);
                }
            }
        }
        result.setHabilitationsOk(habilitationsValides);

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
        
        if (intervention.getDatePlanifiee() == null) return true;

        LocalDateTime dateIntervention = intervention.getDatePlanifiee().atStartOfDay();
        if (dateIntervention == null) return true;

        // Vérifier si jour de congé
        LocalDate date = dateIntervention.toLocalDate();
        if (mainDOeuvre.getConges() != null && mainDOeuvre.getConges().contains(date)) {
            erreurs.add("L'agent est en congé le " + date);
            return false;
        }

        if (mainDOeuvre.getAbsences() != null && mainDOeuvre.getAbsences().contains(date)) {
            erreurs.add("L'agent est absent le " + date);
            return false;
        }

        // Vérifier horaires de travail
        String jourSemaine = date.getDayOfWeek().name(); // LUNDI, MARDI, etc.
        String horaires = mainDOeuvre.getHorairesTravail() != null ? 
            mainDOeuvre.getHorairesTravail().get(jourSemaine) : null;
        
        if (horaires == null || horaires.isEmpty()) {
            // Pas d'horaires définis, on accepte mais c'est un avertissement (géré dans la méthode appelante)
            return true;
        }

        // Parser horaires (format: "08:00-17:00")
        String[] parts = horaires.split("-");
        if (parts.length == 2) {
            try {
                LocalTime debut = LocalTime.parse(parts[0].trim());
                LocalTime fin = LocalTime.parse(parts[1].trim());
                LocalTime heureIntervention = dateIntervention.toLocalTime();

                if (heureIntervention.isBefore(debut) || heureIntervention.isAfter(fin)) {
                    erreurs.add("L'intervention est en dehors des horaires de travail (" + horaires + ")");
                    return false;
                }
            } catch (Exception e) {
                // Format invalide, on accepte
            }
        }

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
     */
    public void verifierEtMettreAJourStatut(MainDOeuvre mainDOeuvre) {
        if (mainDOeuvre.getHabilitationsExpiration() != null) {
            LocalDate aujourdhui = LocalDate.now();
            boolean hasExpired = false;

            for (LocalDate expiration : mainDOeuvre.getHabilitationsExpiration().values()) {
                if (expiration != null && expiration.isBefore(aujourdhui)) {
                    hasExpired = true;
                    break;
                }
            }

            if (hasExpired && !"HORS_HABILITATION".equals(mainDOeuvre.getDisponibilite())) {
                mainDOeuvre.setDisponibilite("HORS_HABILITATION");
                try {
                    mainDOeuvreService.save(mainDOeuvre);
                } catch (Exception e) {
                    // Log error
                }
            }
        }
    }
}

