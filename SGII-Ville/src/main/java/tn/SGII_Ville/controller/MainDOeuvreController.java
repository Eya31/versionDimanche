package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.*;
import tn.SGII_Ville.model.enums.EtatInterventionType;
import tn.SGII_Ville.service.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import tn.SGII_Ville.dto.TerminerTacheRequest;

/**
 * Contrôleur dédié aux agents de main-d'œuvre
 * Endpoints accessibles uniquement aux utilisateurs authentifiés avec rôle MAIN_DOEUVRE
 */
@RestController
@RequestMapping("/api/main-doeuvre")
@CrossOrigin(origins = "http://localhost:4200")
public class MainDOeuvreController {

    @Autowired
    private UserXmlService userXmlService;

    @Autowired
    private MainDOeuvreXmlService mainDOeuvreService;

    @Autowired
    private InterventionXmlService interventionService;

    @Autowired
    private TacheXmlService tacheService;

    @Autowired
    private NotificationService notificationService;

    /**
     * GET /api/main-doeuvre/profil
     * Récupère le profil de l'agent connecté
     */
    @GetMapping("/profil")
    public ResponseEntity<?> getProfil() {
        try {
            AgentMainDOeuvre agent = getCurrentAgent();
            if (agent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Récupérer la fiche complète de main-d'œuvre
            MainDOeuvre fiche = mainDOeuvreService.findById(agent.getMainDOeuvreId());
            if (fiche == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(fiche);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/main-doeuvre/interventions
     * Récupère toutes les interventions auxquelles l'agent est affecté
     */
    @GetMapping("/interventions")
    public ResponseEntity<List<Intervention>> getMyInterventions(
            @RequestParam(required = false) String etat) {
        try {
            AgentMainDOeuvre agent = getCurrentAgent();
            if (agent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            int mainDOeuvreId = agent.getMainDOeuvreId();
            List<Intervention> allInterventions = interventionService.getAllInterventions();
            
            // Filtrer les interventions où l'agent est affecté
            List<Intervention> myInterventions = allInterventions.stream()
                    .filter(i -> i.getMainDOeuvreIds() != null && 
                               i.getMainDOeuvreIds().contains(mainDOeuvreId))
                    .collect(Collectors.toList());

            // Filtrer par état si fourni
            if (etat != null && !etat.isEmpty()) {
                myInterventions = myInterventions.stream()
                        .filter(i -> i.getEtat() != null && 
                                   i.getEtat().name().equals(etat))
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(myInterventions);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * GET /api/main-doeuvre/interventions/{id}
     * Récupère les détails d'une intervention
     */
    @GetMapping("/interventions/{id}")
    public ResponseEntity<Intervention> getInterventionDetails(@PathVariable int id) {
        try {
            AgentMainDOeuvre agent = getCurrentAgent();
            if (agent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Intervention intervention = interventionService.findById(id);
            if (intervention == null) {
                return ResponseEntity.notFound().build();
            }

            // Vérifier que l'agent est affecté à cette intervention
            int mainDOeuvreId = agent.getMainDOeuvreId();
            if (intervention.getMainDOeuvreIds() == null || 
                !intervention.getMainDOeuvreIds().contains(mainDOeuvreId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(intervention);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/main-doeuvre/statistiques
     * Récupère les statistiques de l'agent
     */
    @GetMapping("/statistiques")
    public ResponseEntity<?> getStatistiques() {
        try {
            AgentMainDOeuvre agent = getCurrentAgent();
            if (agent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            int mainDOeuvreId = agent.getMainDOeuvreId();
            List<Intervention> allInterventions = interventionService.getAllInterventions();
            
            List<Intervention> myInterventions = allInterventions.stream()
                    .filter(i -> i.getMainDOeuvreIds() != null && 
                               i.getMainDOeuvreIds().contains(mainDOeuvreId))
                    .collect(Collectors.toList());

            long total = myInterventions.size();
            long terminees = myInterventions.stream()
                    .filter(i -> i.getEtat() == EtatInterventionType.TERMINEE)
                    .count();
            
            double tauxReussite = total > 0 ? (terminees * 100.0 / total) : 0;
            
            int tempsTotal = myInterventions.stream()
                    .filter(i -> i.getTempsPasseMinutes() != null)
                    .mapToInt(Intervention::getTempsPasseMinutes)
                    .sum();

            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                put("totalInterventions", total);
                put("interventionsTerminees", terminees);
                put("tauxReussite", tauxReussite);
                put("tempsTotalMinutes", tempsTotal);
            }});

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Méthode utilitaire pour récupérer l'agent connecté
     */
    private AgentMainDOeuvre getCurrentAgent() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<Utilisateur> userOpt = userXmlService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return null;
            }
            
            Utilisateur user = userOpt.get();
            if (user instanceof AgentMainDOeuvre) {
                return (AgentMainDOeuvre) user;
            }
            
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== GESTION DES TÂCHES ====================

    /**
     * GET /api/main-doeuvre/taches
     * Récupère toutes les tâches assignées à l'agent connecté
     */
    @GetMapping("/taches")
    public ResponseEntity<List<Tache>> getMyTaches() {
        try {
            AgentMainDOeuvre agent = getCurrentAgent();
            if (agent == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(agent.getMainDOeuvreId());
            if (mainDOeuvre == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            List<Tache> taches = tacheService.findByMainDOeuvreId(mainDOeuvre.getId());
            return ResponseEntity.ok(taches);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/main-doeuvre/interventions/{interventionId}/taches
     * Récupère toutes les tâches d'une intervention assignées à l'agent
     */
    @GetMapping("/interventions/{interventionId}/taches")
    public ResponseEntity<List<Tache>> getTachesByIntervention(@PathVariable int interventionId) {
        try {
            AgentMainDOeuvre agent = getCurrentAgent();
            if (agent == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(agent.getMainDOeuvreId());
            if (mainDOeuvre == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Vérifier que l'intervention est bien assignée à cet agent
            Intervention intervention = interventionService.findById(interventionId);
            if (intervention == null || 
                intervention.getMainDOeuvreIds() == null || 
                !intervention.getMainDOeuvreIds().contains(mainDOeuvre.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Tache> allTaches = tacheService.findByInterventionId(interventionId);
            List<Tache> myTaches = allTaches.stream()
                    .filter(t -> t.getMainDOeuvreId() != null && t.getMainDOeuvreId() == mainDOeuvre.getId())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(myTaches);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/main-doeuvre/taches/{tacheId}/commencer
     * Commencer une tâche
     */
    @PostMapping("/taches/{tacheId}/commencer")
    public ResponseEntity<Tache> commencerTache(@PathVariable int tacheId) {
        try {
            AgentMainDOeuvre agent = getCurrentAgent();
            if (agent == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(agent.getMainDOeuvreId());
            if (mainDOeuvre == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Tache tache = tacheService.findById(tacheId);
            if (tache == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Vérifier que la tâche est bien assignée à cet agent
            if (tache.getMainDOeuvreId() == null || tache.getMainDOeuvreId() != mainDOeuvre.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!"A_FAIRE".equals(tache.getEtat()) && !"VERIFIEE".equals(tache.getEtat())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            tache.setEtat("EN_COURS");
            tache.setDateDebut(LocalDateTime.now());
            Tache saved = tacheService.save(tache);

            // Notification au technicien
            Intervention intervention = interventionService.findById(tache.getInterventionId());
            if (intervention != null) {
                notificationService.notifierTechnicien(intervention.getTechnicienId(),
                    "La main-d'œuvre " + mainDOeuvre.getNom() + " " + mainDOeuvre.getPrenom() + 
                    " a commencé la tâche \"" + tache.getLibelle() + "\" (Intervention #" + intervention.getId() + ")");
            }

            // Notification au chef de service
            if (intervention != null && intervention.getChefServiceId() != null) {
                notificationService.notifierChefService(intervention.getChefServiceId(),
                    "Tâche \"" + tache.getLibelle() + "\" commencée par " + mainDOeuvre.getNom() + " " + mainDOeuvre.getPrenom());
            }

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/main-doeuvre/taches/{tacheId}/terminer
     * Terminer une tâche
     */
    @PostMapping("/taches/{tacheId}/terminer")
    public ResponseEntity<Tache> terminerTache(
            @PathVariable int tacheId,
            @RequestBody TerminerTacheRequest request) {
        try {
            AgentMainDOeuvre agent = getCurrentAgent();
            if (agent == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(agent.getMainDOeuvreId());
            if (mainDOeuvre == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Tache tache = tacheService.findById(tacheId);
            if (tache == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Vérifier que la tâche est bien assignée à cet agent
            if (tache.getMainDOeuvreId() == null || tache.getMainDOeuvreId() != mainDOeuvre.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!"EN_COURS".equals(tache.getEtat())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            tache.setEtat("TERMINEE");
            tache.setDateFin(LocalDateTime.now());
            tache.setCommentaireMainDOeuvre(request.getCommentaire());
            tache.setTempsPasseMinutes(request.getTempsPasseMinutes());
            Tache saved = tacheService.save(tache);

            // Notification au technicien
            Intervention intervention = interventionService.findById(tache.getInterventionId());
            if (intervention != null) {
                notificationService.notifierTechnicien(intervention.getTechnicienId(),
                    "La main-d'œuvre " + mainDOeuvre.getNom() + " " + mainDOeuvre.getPrenom() + 
                    " a terminé la tâche \"" + tache.getLibelle() + "\" (Intervention #" + intervention.getId() + "). Veuillez vérifier.");
            }

            // Notification au chef de service
            if (intervention != null && intervention.getChefServiceId() != null) {
                notificationService.notifierChefService(intervention.getChefServiceId(),
                    "Tâche \"" + tache.getLibelle() + "\" terminée par " + mainDOeuvre.getNom() + " " + mainDOeuvre.getPrenom() + 
                    " (Intervention #" + intervention.getId() + ")");
            }

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

