// src/main/java/tn/SGII_Ville/controller/InterventionController.java
package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.dto.DateValidationRequest;
import tn.SGII_Ville.dto.DateValidationResult;
import tn.SGII_Ville.entities.Demande;
import tn.SGII_Ville.entities.Intervention;
import tn.SGII_Ville.entities.Tache;
import tn.SGII_Ville.model.enums.EtatDemandeType;
import tn.SGII_Ville.model.enums.EtatInterventionType;
import tn.SGII_Ville.service.DemandeXmlService;
import tn.SGII_Ville.service.InterventionValidationService;
import tn.SGII_Ville.service.InterventionXmlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import tn.SGII_Ville.service.TacheXmlService;

/**
 * Contrôleur REST pour gérer les interventions et la planification des demandes
 */
@RestController
@RequestMapping("/api/interventions")
@CrossOrigin(origins = "http://localhost:4200")
public class InterventionController {

    private static final Logger logger = LoggerFactory.getLogger(InterventionController.class);

    @Autowired
    private InterventionXmlService interventionService;

    @Autowired
    private DemandeXmlService demandeService;

    @Autowired
    private tn.SGII_Ville.service.NotificationService notificationService;

    @Autowired
    private TacheXmlService tacheXmlService;

    @Autowired
    private InterventionValidationService validationService;

    @Autowired
    private tn.SGII_Ville.service.RessourceAssignationService ressourceAssignationService;

    // -------------------------------------------------------
    // 1. PLANIFIER UNE DEMANDE → crée automatiquement intervention
    // -------------------------------------------------------
    @PostMapping("/planifier/{id}")
public ResponseEntity<?> planifierDemande(@PathVariable int id) {
    try {
        logger.info("=== DÉBUT PLANIFICATION DEMANDE #{} ===", id);

        Demande demande = demandeService.findById(id);
        logger.info("Demande trouvée: {}", demande);

        if (demande == null) {
            logger.error("Demande non trouvée ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        logger.info("État actuel de la demande: {}", demande.getEtat());

        if (demande.getEtat() == EtatDemandeType.TRAITEE) {
            logger.warn("Demande déjà planifiée ID: {}", id);
            return ResponseEntity.badRequest()
                    .body("Cette demande est déjà planifiée.");
        }

        // Création intervention
        logger.info("Appel à interventionService.planifierDemande...");
        Intervention intervention = interventionService.planifierDemande(id);
        logger.info("Intervention créée avec succès: {}", intervention);

        // Notifications
        // 1. Notifier l'admin de la nouvelle intervention
        notificationService.notifierNouvelleIntervention(intervention.getId(), id);
        
        // 2. Notifier le citoyen que sa demande est acceptée
        if (demande.getCitoyenId() > 0) {
            notificationService.notifierCitoyenInterventionLancee(demande.getCitoyenId(), id, intervention.getId());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(intervention);

    } catch (Exception e) {
        logger.error("=== ERREUR CRITIQUE planification demande {} ===", id, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur : " + e.getMessage());
    }
}

    // -------------------------------------------------------
    // 2. Récupérer toutes les interventions
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Intervention>> getAllInterventions() {
        try {
            return ResponseEntity.ok(interventionService.getAllInterventions());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -------------------------------------------------------
    // 3. Récupérer une intervention par ID
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Intervention> getInterventionById(@PathVariable int id) {
        try {
            Intervention intervention = interventionService.findById(id);
            return (intervention != null)
                    ? ResponseEntity.ok(intervention)
                    : ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -------------------------------------------------------
    // 4. Modifier le statut d’une intervention
    // -------------------------------------------------------
    @PatchMapping("/{id}")
    public ResponseEntity<Intervention> updateInterventionStatus(
            @PathVariable int id,
            @RequestBody StatutUpdate statutUpdate) {
        try {
            Intervention intervention = interventionService.findById(id);
            if (intervention == null) {
                return ResponseEntity.notFound().build();
            }

            boolean updated = interventionService.updateEtat(id, statutUpdate.getStatut());
            if (!updated) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -------------------------------------------------------
    // 5. Affecter un technicien à une intervention
    // -------------------------------------------------------
    @PutMapping("/{id}/affecter")
    public ResponseEntity<Intervention> affecterTechnicien(
            @PathVariable int id,
            @RequestBody AffectationRequest request) {
        try {
            Intervention intervention = interventionService.findById(id);
            if (intervention == null) {
                return ResponseEntity.notFound().build();
            }

            boolean updated = interventionService.affecterTechnicien(id, request.getTechnicienId());
            if (!updated) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // -------------------------------------------------------
    // 6. Valider les dates disponibles selon les exigences
    // -------------------------------------------------------
    @PostMapping("/valider-dates")
    public ResponseEntity<List<DateValidationResult>> validateDates(
            @RequestBody DateValidationRequest request) {
        try {
            logger.info("Validation des dates du {} au {}", request.getDateDebut(), request.getDateFin());
            List<DateValidationResult> results = validationService.validateDates(request);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Erreur lors de la validation des dates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -------------------------------------------------------
    // 7. Assigner les ressources sélectionnées
    // -------------------------------------------------------
    @PostMapping("/assigner-ressources")
    public ResponseEntity<?> assignerRessources(
            @RequestBody tn.SGII_Ville.dto.AssignerRessourcesRequest request) {
        try {
            logger.info("Assignation des ressources pour la date: {}", request.getDateIntervention());
            ressourceAssignationService.assignerRessources(request);
            
            // Retourner un objet JSON au lieu d'une string
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                put("success", true);
                put("message", "Ressources assignées avec succès");
            }});
        } catch (Exception e) {
            logger.error("Erreur lors de l'assignation des ressources", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new java.util.HashMap<String, Object>() {{
                        put("success", false);
                        put("message", "Erreur: " + e.getMessage());
                    }});
        }
    }

    // -------------------------------------------------------
    // 8. Planifier intervention complète (ressources + intervention)
    // -------------------------------------------------------
    @PostMapping("/planifier-complete")
    public ResponseEntity<?> planifierInterventionComplete(
            @RequestBody tn.SGII_Ville.dto.AssignerRessourcesRequest request) {
        try {
            logger.info("Planification complète pour demande {} - date: {}", request.getDemandeId(), request.getDateIntervention());
            
            if (request.getDemandeId() == null) {
                return ResponseEntity.badRequest()
                    .body(new java.util.HashMap<String, Object>() {{
                        put("success", false);
                        put("message", "DemandeId est requis");
                    }});
            }
            
            // 1. Assigner les ressources (marquer indisponible, réduire stock)
            logger.info("Étape 1: Assignation des ressources");
            ressourceAssignationService.assignerRessources(request);
            
            // 2. Créer l'intervention pour la demande
            logger.info("Étape 2: Création de l'intervention");
            Intervention intervention = interventionService.planifierDemande(request.getDemandeId());
            
            // 3. Affecter le premier technicien sélectionné à l'intervention
            if (request.getTechniciensIds() != null && !request.getTechniciensIds().isEmpty()) {
                Integer technicienId = request.getTechniciensIds().get(0);
                logger.info("Étape 3: Affectation du technicien {} à l'intervention {}", technicienId, intervention.getId());
                interventionService.affecterTechnicien(intervention.getId(), technicienId);
                intervention.setTechnicienId(technicienId);
            }
            
            // 4. Envoyer les notifications
            Demande demande = demandeService.findById(request.getDemandeId());
            if (demande != null) {
                notificationService.notifierNouvelleIntervention(intervention.getId(), request.getDemandeId());
                if (demande.getCitoyenId() > 0) {
                    notificationService.notifierCitoyenInterventionLancee(demande.getCitoyenId(), request.getDemandeId(), intervention.getId());
                }
            }
            
            logger.info("✅ Planification complète terminée: Intervention #{}", intervention.getId());
            
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                put("success", true);
                put("message", "Intervention créée avec succès");
                put("interventionId", intervention.getId());
                put("intervention", intervention);
            }});
            
        } catch (Exception e) {
            logger.error("Erreur lors de la planification complète", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new java.util.HashMap<String, Object>() {{
                        put("success", false);
                        put("message", "Erreur: " + e.getMessage());
                    }});
        }
    }

    // -------------------------------------------------------
    // DTO internes
    // -------------------------------------------------------
    public static class StatutUpdate {
        private EtatInterventionType statut;

        public EtatInterventionType getStatut() {
            return statut;
        }
        public void setStatut(EtatInterventionType statut) {
            this.statut = statut;
        }
    }

    public static class AffectationRequest {
        private int technicienId;

        public int getTechnicienId() {
            return technicienId;
        }
        public void setTechnicienId(int technicienId) {
            this.technicienId = technicienId;
        }
    }
    // InterventionController.java
// Ajouter cet endpoint

@GetMapping("/{interventionId}/taches/statut")
public ResponseEntity<?> verifierToutesTachesTerminees(@PathVariable int interventionId) {
    try {
        // Récupérer toutes les tâches de l'intervention
        List<Tache> taches = tacheXmlService.findByInterventionId(interventionId);
        
        if (taches.isEmpty()) {
            return ResponseEntity.ok(Map.of("toutesTerminees", false, "message", "Aucune tâche trouvée"));
        }
        
        // Vérifier si toutes les tâches sont terminées
        boolean toutesTerminees = taches.stream()
            .allMatch(t -> "TERMINEE".equals(t.getEtat()) || "VERIFIEE".equals(t.getEtat()));
        
        // Calculer les statistiques
        long totalTaches = taches.size();
        long tachesTerminees = taches.stream()
            .filter(t -> "TERMINEE".equals(t.getEtat()) || "VERIFIEE".equals(t.getEtat()))
            .count();
        
        return ResponseEntity.ok(Map.of(
            "toutesTerminees", toutesTerminees,
            "statistiques", Map.of(
                "totalTaches", totalTaches,
                "tachesTerminees", tachesTerminees,
                "tachesEnCours", totalTaches - tachesTerminees
            ),
            "message", toutesTerminees ? 
                "✅ Toutes les tâches sont terminées" : 
                "❌ " + (totalTaches - tachesTerminees) + " tâche(s) restante(s)"
        ));
    } catch (Exception e) {
        return ResponseEntity.status(500)
            .body(Map.of("error", "Erreur vérification tâches: " + e.getMessage()));
    }
}
}
