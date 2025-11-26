package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.model.DisponibiliteTechnicien;
import tn.SGII_Ville.model.RessourceDisponibilite;
import tn.SGII_Ville.model.RessourceMaterielleDisponibilite;
import tn.SGII_Ville.service.PlanificationService;
import tn.SGII_Ville.service.RessourceAvailabilityService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilite")
@CrossOrigin(origins = "http://localhost:4200")
public class DisponibiliteController {

    @Autowired
    private PlanificationService planificationService;
    
    @Autowired
    private RessourceAvailabilityService ressourceAvailabilityService;

    /**
     * Récupère le calendrier des disponibilités pour un technicien sur une période
     */
    @GetMapping("/technicien/{technicienId}/periode")
    public ResponseEntity<List<DisponibiliteTechnicien>> getCalendrierTechnicienPeriode(
            @PathVariable Integer technicienId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<DisponibiliteTechnicien> disponibilites = planificationService.getTechniciensDisponiblesForPeriod(technicienId, startDate, endDate);
        return ResponseEntity.ok(disponibilites);
    }

    /**
     * Récupère les indisponibilités pour une période
     */
    @GetMapping("/indisponibilites")
    public ResponseEntity<?> getIndisponibilites(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Implémentation pour récupérer toutes les indisponibilités
        return ResponseEntity.ok().build();
    }
    
    /**
     * Vérifie la disponibilité globale pour une intervention
     */
    @PostMapping("/verifier")
    public ResponseEntity<RessourceDisponibilite> verifierDisponibilite(
            @RequestParam String date,
            @RequestParam(required = false) List<Integer> technicienIds,
            @RequestParam(required = false) List<Integer> equipementIds,
            @RequestParam(required = false) List<Integer> ressourceIds) {
        
        RessourceDisponibilite disponibilite = ressourceAvailabilityService.verifierDisponibiliteGlobale(date, technicienIds, equipementIds, ressourceIds);
        return ResponseEntity.ok(disponibilite);
    }
    
    /**
     * Récupère les ressources en alerte de stock
     */
    @GetMapping("/alertes-stock")
    public ResponseEntity<List<RessourceMaterielleDisponibilite>> getRessourcesAlerteStock() {
        List<RessourceMaterielleDisponibilite> alertes = ressourceAvailabilityService.getRessourcesAlerteStock();
        return ResponseEntity.ok(alertes);
    }
}