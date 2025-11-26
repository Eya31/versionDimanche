package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.model.PlanificationRequest;
import tn.SGII_Ville.model.Intervention;
import tn.SGII_Ville.model.DisponibiliteTechnicien;
import tn.SGII_Ville.model.RessourceDisponibilite;
import tn.SGII_Ville.service.PlanificationService;
import tn.SGII_Ville.exception.RessourceIndisponibleException;

import java.util.List;

@RestController
@RequestMapping("/api/planification")
@CrossOrigin(origins = "http://localhost:4200")
public class PlanificationController {
    
    @Autowired
    private PlanificationService planificationService;
    
    /**
     * Récupère tous les techniciens disponibles pour une date donnée
     */
    @GetMapping("/techniciens/disponibles")
    public ResponseEntity<List<DisponibiliteTechnicien>> getTechniciensDisponibles(
            @RequestParam String date,
            @RequestParam(required = false) String competences) {
        
        List<DisponibiliteTechnicien> techniciens = planificationService.getTechniciensDisponibles(date, competences);
        return ResponseEntity.ok(techniciens);
    }
    
    /**
     * Planifie une intervention avec vérification des disponibilités
     */
    @PostMapping("/intervention")
    public ResponseEntity<?> planifierIntervention(@RequestBody PlanificationRequest request) {
        try {
            Intervention intervention = planificationService.planifierIntervention(request);
            return ResponseEntity.ok(intervention);
        } catch (RessourceIndisponibleException e) {
            return ResponseEntity.badRequest().body(
                java.util.Map.of("error", "Ressource indisponible", "details", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                java.util.Map.of("error", "Erreur lors de la planification", "details", e.getMessage())
            );
        }
    }
    
    /**
     * Vérifie la disponibilité des ressources pour une date
     */
    @GetMapping("/ressources/disponibles")
    public ResponseEntity<RessourceDisponibilite> checkRessourcesDisponibles(
            @RequestParam String date,
            @RequestParam(required = false) List<Integer> equipementIds,
            @RequestParam(required = false) List<Integer> ressourceIds) {
        
        RessourceDisponibilite disponibilite = planificationService.checkRessourcesDisponibles(date, equipementIds, ressourceIds);
        return ResponseEntity.ok(disponibilite);
    }
    
    /**
     * Récupère le calendrier des disponibilités pour un mois
     */
    @GetMapping("/calendrier/{technicienId}")
    public ResponseEntity<List<DisponibiliteTechnicien>> getCalendrierTechnicien(
            @PathVariable Integer technicienId,
            @RequestParam String mois) {
        
        // Implémentation pour récupérer le calendrier mensuel
        java.time.LocalDate startDate = java.time.LocalDate.parse(mois + "-01");
        java.time.LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        List<DisponibiliteTechnicien> calendrier = planificationService.getTechniciensDisponiblesForPeriod(technicienId, startDate, endDate);
        return ResponseEntity.ok(calendrier);
    }
}