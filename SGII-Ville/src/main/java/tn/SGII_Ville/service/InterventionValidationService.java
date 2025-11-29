package tn.SGII_Ville.service;

import org.springframework.stereotype.Service;
import tn.SGII_Ville.dto.*;
import tn.SGII_Ville.model.Equipement;
import tn.SGII_Ville.entities.RessourceMaterielle;
import tn.SGII_Ville.entities.Technicien;
import tn.SGII_Ville.repository.EquipementRepository;
import tn.SGII_Ville.repository.RessourceMaterielleRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InterventionValidationService {

    private final UserXmlService userXmlService;
    private final EquipementRepository equipementRepository;
    private final RessourceMaterielleRepository ressourceMaterielleRepository;

    public InterventionValidationService(
            UserXmlService userXmlService,
            EquipementRepository equipementRepository,
            RessourceMaterielleRepository ressourceMaterielleRepository) {
        this.userXmlService = userXmlService;
        this.equipementRepository = equipementRepository;
        this.ressourceMaterielleRepository = ressourceMaterielleRepository;
    }

    /**
     * Valide les disponibilités pour une liste de dates
     */
    public List<DateValidationResult> validateDates(DateValidationRequest request) {
        List<DateValidationResult> results = new ArrayList<>();
        
        LocalDate currentDate = request.getDateDebut();
        while (!currentDate.isAfter(request.getDateFin())) {
            DateValidationResult result = validateDate(
                currentDate,
                request.getCompetencesRequises(),
                request.getMaterielsRequis(),
                request.getEquipementsRequis()
            );
            results.add(result);
            currentDate = currentDate.plusDays(1);
        }
        
        return results;
    }

    /**
     * Valide la disponibilité pour une date spécifique
     */
    public DateValidationResult validateDate(
            LocalDate date,
            CompetenceRequise[] competencesRequises,
            MaterielRequis[] materielsRequis,
            EquipementRequis[] equipementsRequis) {
        
        DateValidationResult result = new DateValidationResult();
        result.setDate(date);
        
        // Vérifier les techniciens
        boolean techniciensOk = verifyTechniciens(competencesRequises, date);
        result.setTechniciensDisponibles(techniciensOk);
        
        // Vérifier les équipements
        boolean equipementsOk = verifyEquipements(equipementsRequis, date);
        result.setEquipementsDisponibles(equipementsOk);
        
        // Vérifier les matériels
        boolean materielsOk = verifyMateriels(materielsRequis);
        result.setMaterielsDisponibles(materielsOk);
        
        // Déterminer le statut global
        if (techniciensOk && equipementsOk && materielsOk) {
            result.setStatus("VERT");
            result.setMessage("Toutes les ressources sont disponibles");
        } else if (techniciensOk && equipementsOk && !materielsOk) {
            result.setStatus("JAUNE");
            result.setMessage("Techniciens et équipements disponibles, mais matériel insuffisant");
        } else {
            result.setStatus("ROUGE");
            List<String> manquants = new ArrayList<>();
            if (!techniciensOk) manquants.add("techniciens");
            if (!equipementsOk) manquants.add("équipements");
            if (!materielsOk) manquants.add("matériels");
            result.setMessage("Ressources manquantes : " + String.join(", ", manquants));
        }
        
        return result;
    }

    /**
     * Vérifie la disponibilité des techniciens avec les compétences requises
     */
    private boolean verifyTechniciens(CompetenceRequise[] competencesRequises, LocalDate date) {
        if (competencesRequises == null || competencesRequises.length == 0) {
            return true;
        }
        
        List<Technicien> allTechniciens = userXmlService.findAllTechniciens();
        System.out.println("🔍 VALIDATION - Nombre total de techniciens: " + allTechniciens.size());
        
        for (CompetenceRequise comp : competencesRequises) {
            int techniciensTrouves = 0;
            System.out.println("🔍 VALIDATION - Recherche compétence: '" + comp.getCompetence() + "' (nombre requis: " + comp.getNombreTechniciens() + ")");
            
            for (Technicien tech : allTechniciens) {
                System.out.println("  👤 Technicien " + tech.getNom() + " - Disponible: " + tech.isDisponibilite() + ", Compétences: " + tech.getCompetences());
                
                // Vérifier si le technicien a la compétence et est disponible
                if (tech.isDisponibilite() && tech.getCompetences() != null) {
                    // Comparaison insensible à la casse et aux espaces
                    String compRequise = comp.getCompetence().trim().toLowerCase();
                    for (String compTech : tech.getCompetences()) {
                        if (compTech != null && compTech.trim().toLowerCase().equals(compRequise)) {
                            techniciensTrouves++;
                            System.out.println("    ✅ Match trouvé pour " + tech.getNom());
                            break;
                        }
                    }
                }
            }
            
            System.out.println("  📊 Techniciens trouvés: " + techniciensTrouves + " / " + comp.getNombreTechniciens() + " requis");
            
            if (techniciensTrouves < comp.getNombreTechniciens()) {
                System.out.println("  ❌ Pas assez de techniciens avec la compétence '" + comp.getCompetence() + "'");
                return false;
            }
        }
        
        System.out.println("✅ Tous les techniciens requis sont disponibles");
        return true;
    }

    /**
     * Vérifie la disponibilité des équipements
     */
    private boolean verifyEquipements(EquipementRequis[] equipementsRequis, LocalDate date) {
        if (equipementsRequis == null || equipementsRequis.length == 0) {
            return true;
        }
        
        List<Equipement> allEquipements = equipementRepository.findAll();
        
        for (EquipementRequis eq : equipementsRequis) {
            int equipementsTrouves = 0;
            
            for (Equipement equipement : allEquipements) {
                // Vérifier type et état fonctionnel
                if (equipement.getType() != null && 
                    equipement.getType().equalsIgnoreCase(eq.getType()) &&
                    "fonctionnel".equalsIgnoreCase(equipement.getEtat())) {
                    
                    // Pour le model.Equipement, on ne vérifie que le type et l'état
                    equipementsTrouves++;
                }
            }
            
            if (equipementsTrouves < eq.getQuantiteRequise()) {
                return false;
            }
        }
        
        return true;
    }



    /**
     * Vérifie la disponibilité des matériels en stock
     */
    private boolean verifyMateriels(MaterielRequis[] materielsRequis) {
        if (materielsRequis == null || materielsRequis.length == 0) {
            return true;
        }
        
        List<RessourceMaterielle> allMateriels = ressourceMaterielleRepository.findAll();
        
        for (MaterielRequis mat : materielsRequis) {
            boolean trouve = false;
            
            for (RessourceMaterielle ressource : allMateriels) {
                if (ressource.getDesignation() != null && 
                    ressource.getDesignation().equalsIgnoreCase(mat.getDesignation()) &&
                    ressource.getQuantiteEnStock() != null &&
                    ressource.getQuantiteEnStock() >= mat.getQuantiteRequise()) {
                    trouve = true;
                    break;
                }
            }
            
            if (!trouve) {
                return false;
            }
        }
        
        return true;
    }
}
