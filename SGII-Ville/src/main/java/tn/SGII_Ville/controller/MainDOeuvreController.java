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
import java.util.Map;
import java.util.HashMap;

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

    // Classe pour gérer les requêtes de changement d'état
    public static class ChangerEtatTacheRequest {
        @com.fasterxml.jackson.annotation.JsonProperty("nouvelEtat")
        private String nouvelEtat;
        
        @com.fasterxml.jackson.annotation.JsonProperty("commentaire")
        private String commentaire;
        
        @com.fasterxml.jackson.annotation.JsonProperty("tempsPasseMinutes")
        private Integer tempsPasseMinutes;

        // Constructors
        public ChangerEtatTacheRequest() {}
        
        public ChangerEtatTacheRequest(String nouvelEtat, String commentaire, Integer tempsPasseMinutes) {
            this.nouvelEtat = nouvelEtat;
            this.commentaire = commentaire;
            this.tempsPasseMinutes = tempsPasseMinutes;
        }

        // Getters et Setters
        public String getNouvelEtat() { return nouvelEtat; }
        public void setNouvelEtat(String nouvelEtat) { this.nouvelEtat = nouvelEtat; }
        
        public String getCommentaire() { return commentaire; }
        public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
        
        public Integer getTempsPasseMinutes() { return tempsPasseMinutes; }
        public void setTempsPasseMinutes(Integer tempsPasseMinutes) { this.tempsPasseMinutes = tempsPasseMinutes; }
    }

    // Classe pour les requêtes de commentaire
    public static class CommentaireRequest {
        private String commentaire;

        public String getCommentaire() { return commentaire; }
        public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    }

    /**
     * GET /api/main-doeuvre/profil
     * Récupère le profil de l'agent connecté
     */
    @GetMapping("/profil")
public ResponseEntity<?> getProfil() {
    try {
        System.out.println("=== GET /api/main-doeuvre/profil appelé ===");
        
        // Récupérer l'email de l'utilisateur connecté
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Email utilisateur connecté: " + email);
        
        AgentMainDOeuvre agent = getCurrentAgent();
        System.out.println("Agent trouvé: " + (agent != null));
        
        if (agent == null) {
            System.out.println("Agent non trouvé ou non de type AgentMainDOeuvre");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        System.out.println("MainDOeuvreId de l'agent: " + agent.getMainDOeuvreId());
        
        // Récupérer la fiche complète de main-d'œuvre
        MainDOeuvre fiche = mainDOeuvreService.findById(agent.getMainDOeuvreId());
        System.out.println("Fiche MainDOeuvre trouvée: " + (fiche != null));
        
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
                    .filter(i -> i.getOuvrierIds() != null && 
                               i.getOuvrierIds().contains(mainDOeuvreId))
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
            // S'assurer que ouvrierIds est initialisé
            if (intervention.getOuvrierIds() == null) {
                intervention.setOuvrierIds(new ArrayList<>());
            }
            if (!intervention.getOuvrierIds().contains(mainDOeuvreId)) {
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
                    .filter(i -> i.getOuvrierIds() != null && 
                               i.getOuvrierIds().contains(mainDOeuvreId))
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

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalInterventions", total);
            stats.put("interventionsTerminees", terminees);
            stats.put("tauxReussite", tauxReussite);
            stats.put("tempsTotalMinutes", tempsTotal);

            return ResponseEntity.ok(stats);

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
        System.out.println("Email utilisateur connecté: " + email);
        
        Optional<Utilisateur> userOpt = userXmlService.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            System.out.println("Utilisateur non trouvé dans la base avec email: " + email);
            return null;
        }
        
        Utilisateur user = userOpt.get();
        System.out.println("Type d'utilisateur: " + user.getClass().getSimpleName());
        
        if (user instanceof AgentMainDOeuvre) {
            AgentMainDOeuvre agent = (AgentMainDOeuvre) user;
            System.out.println("MainDOeuvreId de l'agent: " + agent.getMainDOeuvreId());
            return agent;
        } else {
            System.out.println("L'utilisateur n'est pas un AgentMainDOeuvre");
            return null;
        }
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
public ResponseEntity<List<Tache>> getMyTaches(@RequestParam(required = false) String etat) {
    try {
        System.out.println("=== GET /api/main-doeuvre/taches appelé ===");
        
        AgentMainDOeuvre agent = getCurrentAgent();
        if (agent == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(agent.getMainDOeuvreId());
        if (mainDOeuvre == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<Tache> taches = tacheService.findByMainDOeuvreId(mainDOeuvre.getId());
        
        // Filtrer par état si fourni
        if (etat != null && !etat.isEmpty()) {
            taches = taches.stream()
                    .filter(t -> etat.equals(t.getEtat()))
                    .collect(Collectors.toList());
        }
        
        System.out.println("Nombre de tâches trouvées: " + taches.size());
        
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
                intervention.getOuvrierIds() == null || 
                !intervention.getOuvrierIds().contains(mainDOeuvre.getId())) {
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
     * GET /api/main-doeuvre/taches/{tacheId}
     * Récupère les détails d'une tâche spécifique
     */
    @GetMapping("/taches/{tacheId}")
    public ResponseEntity<Tache> getTacheById(@PathVariable int tacheId) {
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

            return ResponseEntity.ok(tache);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/main-doeuvre/taches/{tacheId}/etat
     * Change l'état d'une tâche (méthode générique)
     */
    @PutMapping("/taches/{tacheId}/etat")
    public ResponseEntity<?> changerEtatTache(
            @PathVariable int tacheId,
            @RequestBody ChangerEtatTacheRequest request) {
        try {
            // Validation du nouvelEtat
            if (request == null || request.getNouvelEtat() == null || request.getNouvelEtat().trim().isEmpty()) {
                System.out.println("❌ REQUEST NULL OR NEWETAT EMPTY: request=" + request);
                Map<String, String> error = new HashMap<>();
                error.put("error", "nouvelEtat est obligatoire");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            System.out.println("✅ CHANGEMENT ETAT - TacheId: " + tacheId + ", NouvelEtat: " + request.getNouvelEtat());

            AgentMainDOeuvre agent = getCurrentAgent();
            if (agent == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(agent.getMainDOeuvreId());
            if (mainDOeuvre == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Fiche main-d'œuvre non trouvée");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Tache tache = tacheService.findById(tacheId);
            if (tache == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Tâche non trouvée");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Vérifier que la tâche est bien assignée à cet agent
            if (tache.getMainDOeuvreId() == null || tache.getMainDOeuvreId() != mainDOeuvre.getId()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cette tâche n'est pas assignée à cet agent");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Validation des transitions d'état
            if (tache.getEtat().equals(request.getNouvelEtat())) {
                // Idempotent transition - task is already in this state
                System.out.println("⚠️ IDEMPOTENT TRANSITION: Task already in state " + tache.getEtat());
                return ResponseEntity.ok(tache); // Return success without doing anything
            }
            
            if (!isTransitionEtatValide(tache.getEtat(), request.getNouvelEtat())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", String.format("Transition invalide: %s → %s", tache.getEtat(), request.getNouvelEtat()));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Mettre à jour l'état et les dates
            tache.setEtat(request.getNouvelEtat());
            
            // Gérer les dates selon le nouvel état
            if ("EN_COURS".equals(request.getNouvelEtat())) {
                tache.setDateDebut(LocalDateTime.now());
            } else if ("TERMINEE".equals(request.getNouvelEtat())) {
                tache.setDateFin(LocalDateTime.now());
                if (request.getTempsPasseMinutes() != null) {
                    tache.setTempsPasseMinutes(request.getTempsPasseMinutes());
                }
            } else if ("VERIFIEE".equals(request.getNouvelEtat())) {
                tache.setDateVerification(LocalDateTime.now());
            }

            // Mettre à jour le commentaire si fourni
            if (request.getCommentaire() != null && !request.getCommentaire().isEmpty()) {
                tache.setCommentaireMainDOeuvre(request.getCommentaire());
            }

            Tache saved = tacheService.save(tache);

            // Notifications
            notifierChangementEtat(tache, mainDOeuvre, request.getNouvelEtat());

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors du changement d'état: " + e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /api/main-doeuvre/taches/{tacheId}/commentaire
     * Ajouter un commentaire à une tâche sans changer l'état
     */
    @PostMapping("/taches/{tacheId}/commentaire")
    public ResponseEntity<Tache> ajouterCommentaire(
            @PathVariable int tacheId,
            @RequestBody CommentaireRequest request) {
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

            tache.setCommentaireMainDOeuvre(request.getCommentaire());
            Tache saved = tacheService.save(tache);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/main-doeuvre/taches/{tacheId}/commencer
     * Commencer une tâche (méthode spécifique)
     */
    @PostMapping("/taches/{tacheId}/commencer")
    public ResponseEntity<?> commencerTache(@PathVariable int tacheId) {
        ChangerEtatTacheRequest request = new ChangerEtatTacheRequest();
        request.setNouvelEtat("EN_COURS");
        request.setCommentaire("Tâche commencée");
        return changerEtatTache(tacheId, request);
    }

    /**
     * POST /api/main-doeuvre/taches/{tacheId}/terminer
     * Terminer une tâche (méthode spécifique)
     */
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

        // Validation des transitions d'état
        if (!isTransitionEtatValide(tache.getEtat(), "TERMINEE")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null); // État non autorisé
        }

        // Sauvegarder l'ancien état pour la notification (non utilisé maintenant)
        // String ancienEtat = tache.getEtat();
        
        // Mettre à jour la tâche
        tache.setEtat("TERMINEE");
        tache.setDateFin(LocalDateTime.now());
        if (request.getCommentaire() != null && !request.getCommentaire().isEmpty()) {
            tache.setCommentaireMainDOeuvre(request.getCommentaire());
        }
        if (request.getTempsPasseMinutes() != null) {
            tache.setTempsPasseMinutes(request.getTempsPasseMinutes());
        }

        Tache saved = tacheService.save(tache);
        
        // 1. Notifier le technicien que CETTE tâche est terminée
        // DÉSACTIVÉ: Ne pas envoyer de notifications individuelles
        // notifierTerminaisonTache(tache, mainDOeuvre, ancienEtat);
        
        // 2. VÉRIFIER SI TOUTES LES TÂCHES DE L'INTERVENTION SONT TERMINÉES
        //    Si oui, envoyer une notification spéciale (seule notification à envoyer)
        verifierEtNotifierSiToutesTachesTerminees(tache.getInterventionId());

        return ResponseEntity.ok(saved);

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}


/**
 * Notifier le technicien qu'une tâche spécifique est terminée
 * (DÉSACTIVÉE - ne plus envoyer les notifications individuelles)
 * Laissée comme référence au cas où on souhaiterait la réactiver
 
private void notifierTerminaisonTache(Tache tache, MainDOeuvre mainDOeuvre, String ancienEtat) {
    try {
        Intervention intervention = interventionService.findById(tache.getInterventionId());
        if (intervention != null) {
            String message = String.format(
                "✅ TÂCHE TERMINÉE\n" +
                "La main-d'œuvre %s %s a terminé la tâche:\n" +
                "• Tâche: %s (ID: #%d)\n" +
                "• Intervention: #%d\n" +
                "• Commentaire: %s\n" +
                "• Temps passé: %d minutes",
                mainDOeuvre.getNom(), mainDOeuvre.getPrenom(),
                tache.getLibelle(), tache.getId(),
                intervention.getId(),
                tache.getCommentaireMainDOeuvre() != null ? tache.getCommentaireMainDOeuvre() : "Aucun commentaire",
                tache.getTempsPasseMinutes() != null ? tache.getTempsPasseMinutes() : 0
            );

            // Notifier le technicien
            notificationService.notifierTechnicien(intervention.getTechnicienId(), message);
            
            // Notifier le chef de service si présent
            if (intervention.getChefServiceId() != null) {
                notificationService.notifierChefService(intervention.getChefServiceId(), message);
            }
        }
    } catch (Exception e) {
        System.err.println("❌ Erreur notification terminaison tâche: " + e.getMessage());
        e.printStackTrace();
    }
}
*/
    /**
     * GET /api/main-doeuvre/taches/{tacheId}/historique
     * Récupère l'historique des états d'une tâche
     * IMPLÉMENTATION COMPLÈTE - Plus de TODO
     */
    
    @GetMapping("/taches/{tacheId}/historique")
    public ResponseEntity<List<HistoriqueEtatTache>> getHistoriqueTache(@PathVariable int tacheId) {
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

            // Générer l'historique à partir des données de la tâche
            List<HistoriqueEtatTache> historique = genererHistoriqueFromTache(tache);
            return ResponseEntity.ok(historique);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Valide les transitions d'état autorisées
     */
    private boolean isTransitionEtatValide(String etatActuel, String nouvelEtat) {
        // Allow idempotent transitions (same state)
        if (etatActuel.equals(nouvelEtat)) {
            return true;
        }
        
        // Logique de validation des transitions
        switch (etatActuel) {
            case "A_FAIRE":
                return "EN_COURS".equals(nouvelEtat) || "SUSPENDUE".equals(nouvelEtat) || "REPORTEE".equals(nouvelEtat);
            case "EN_COURS":
                return "TERMINEE".equals(nouvelEtat) || "SUSPENDUE".equals(nouvelEtat) || "A_FAIRE".equals(nouvelEtat);
            case "TERMINEE":
                return "VERIFIEE".equals(nouvelEtat) || "EN_COURS".equals(nouvelEtat);
            case "SUSPENDUE":
                return "EN_COURS".equals(nouvelEtat) || "A_FAIRE".equals(nouvelEtat);
            case "REPORTEE":
                return "A_FAIRE".equals(nouvelEtat) || "EN_COURS".equals(nouvelEtat);
            case "VERIFIEE":
                return "TERMINEE".equals(nouvelEtat); // Réouverture pour correction
            default:
                return false;
        }
    }

    /**
     * Notification des changements d'état
     */
    /**
 * Notification des changements d'état (générique)
 * Ne pas notifier pour TERMINEE, car on le fait spécifiquement dans notifierTerminaisonTache
 */
private void notifierChangementEtat(Tache tache, MainDOeuvre mainDOeuvre, String nouvelEtat) {
    try {
        // Ne pas notifier pour TERMINEE, car on le fait dans notifierTerminaisonTache
        if ("TERMINEE".equals(nouvelEtat)) {
            return;
        }
        
        Intervention intervention = interventionService.findById(tache.getInterventionId());
        if (intervention != null) {
            String message = String.format(
                "🔄 CHANGEMENT D'ÉTAT\n" +
                "La main-d'œuvre %s %s a changé l'état de la tâche:\n" +
                "• Tâche: %s\n" +
                "• Nouvel état: %s\n" +
                "• Intervention: #%d",
                mainDOeuvre.getNom(), mainDOeuvre.getPrenom(),
                tache.getLibelle(), nouvelEtat,
                intervention.getId()
            );

            // Notifier le technicien
            notificationService.notifierTechnicien(intervention.getTechnicienId(), message);
            
            // Notifier le chef de service si présent
            if (intervention.getChefServiceId() != null) {
                notificationService.notifierChefService(intervention.getChefServiceId(), message);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    /**
     * Génère un historique à partir des dates et états de la tâche
     * IMPLÉMENTATION COMPLÈTE
     */
    private List<HistoriqueEtatTache> genererHistoriqueFromTache(Tache tache) {
    List<HistoriqueEtatTache> historique = new ArrayList<>();
    int histId = 1;
    
    // État initial : création
    if (tache.getDateCreation() != null) {
        HistoriqueEtatTache histCreation = new HistoriqueEtatTache();
        histCreation.setId(histId++);
        histCreation.setTacheId(tache.getId());
        histCreation.setEtat("A_FAIRE");
        histCreation.setDateChangement(tache.getDateCreation());
        histCreation.setCommentaire("Tâche créée et assignée");
        historique.add(histCreation);
    }
    
    // Début de la tâche
    if (tache.getDateDebut() != null) {
        HistoriqueEtatTache histDebut = new HistoriqueEtatTache();
        histDebut.setId(histId++);
        histDebut.setTacheId(tache.getId());
        histDebut.setEtat("EN_COURS");
        histDebut.setDateChangement(tache.getDateDebut());
        histDebut.setCommentaire("Tâche commencée par la main d'œuvre");
        historique.add(histDebut);
    }
    
    // Fin de la tâche
    if (tache.getDateFin() != null) {
        HistoriqueEtatTache histFin = new HistoriqueEtatTache();
        histFin.setId(histId++);
        histFin.setTacheId(tache.getId());
        histFin.setEtat("TERMINEE");
        histFin.setDateChangement(tache.getDateFin());
        histFin.setCommentaire(tache.getCommentaireMainDOeuvre() != null ? 
            tache.getCommentaireMainDOeuvre() : "Tâche terminée");
        histFin.setTempsPasseMinutes(tache.getTempsPasseMinutes());
        historique.add(histFin);
    }
    
    // Vérification
    if (tache.getDateVerification() != null) {
        HistoriqueEtatTache histVerif = new HistoriqueEtatTache();
        histVerif.setId(histId++);
        histVerif.setTacheId(tache.getId());
        histVerif.setEtat("VERIFIEE");
        histVerif.setDateChangement(tache.getDateVerification());
        histVerif.setCommentaire(tache.getCommentaireTechnicien() != null ? 
            tache.getCommentaireTechnicien() : "Tâche vérifiée et validée");
        historique.add(histVerif);
    }
    
    // État actuel (si différent des états historiques)
    if (!historique.isEmpty()) {
        String dernierEtat = historique.get(historique.size() - 1).getEtat();
        if (!dernierEtat.equals(tache.getEtat())) {
            HistoriqueEtatTache histActuel = new HistoriqueEtatTache();
            histActuel.setId(histId);
            histActuel.setTacheId(tache.getId());
            histActuel.setEtat(tache.getEtat());
            histActuel.setDateChangement(LocalDateTime.now());
            histActuel.setCommentaire("Dernier changement d'état");
            historique.add(histActuel);
        }
    }
    
    return historique;
}
    // ==================== CLASSES INTERNES POUR LES REQUÊTES ====================

    public static class TerminerTacheRequest {
        private String commentaire;
        private Integer tempsPasseMinutes;

        public String getCommentaire() { return commentaire; }
        public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

        public Integer getTempsPasseMinutes() { return tempsPasseMinutes; }
        public void setTempsPasseMinutes(Integer tempsPasseMinutes) { this.tempsPasseMinutes = tempsPasseMinutes; }
    }

    // Ajouter cette classe interne à la fin de MainDOeuvreController
public static class HistoriqueEtatTache {
    private int id;
    private int tacheId;
    private String etat;
    private LocalDateTime dateChangement;
    private String commentaire;
    private Integer utilisateurId;
    private String utilisateurNom;
    private Integer tempsPasseMinutes;

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getTacheId() { return tacheId; }
    public void setTacheId(int tacheId) { this.tacheId = tacheId; }
    
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
    
    public LocalDateTime getDateChangement() { return dateChangement; }
    public void setDateChangement(LocalDateTime dateChangement) { this.dateChangement = dateChangement; }
    
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    
    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }
    
    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }
    
    public Integer getTempsPasseMinutes() { return tempsPasseMinutes; }
    public void setTempsPasseMinutes(Integer tempsPasseMinutes) { this.tempsPasseMinutes = tempsPasseMinutes; }
}
/**
 * GET /api/main-doeuvre/test
 * Endpoint de test pour vérifier que le contrôleur fonctionne
 */
@GetMapping("/test")
public ResponseEntity<String> test() {
    return ResponseEntity.ok("API MainDOeuvre fonctionnelle - " + LocalDateTime.now());
}

/**
 * GET /api/main-doeuvre/debug
 * Endpoint de débogage pour vérifier les données
 */

/**
 * GET /api/main-doeuvre/test
 * Endpoint de test pour vérifier que le contrôleur fonctionne
 */


/**
 * GET /api/main-doeuvre/debug
 * Endpoint de débogage pour vérifier les données
 */
@GetMapping("/debug")
public ResponseEntity<?> debug() {
    try {
        Map<String, Object> debugInfo = new HashMap<>();
        
        // Info utilisateur
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        debugInfo.put("emailUtilisateur", email);
        
        // Info agent
        AgentMainDOeuvre agent = getCurrentAgent();
        debugInfo.put("agentExiste", agent != null);
        if (agent != null) {
            debugInfo.put("agentId", agent.getId());
            debugInfo.put("mainDOeuvreId", agent.getMainDOeuvreId());
            
            // Info main-d'œuvre
            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(agent.getMainDOeuvreId());
            debugInfo.put("mainDOeuvreExiste", mainDOeuvre != null);
            
            // Info tâches
            if (mainDOeuvre != null) {
                List<Tache> taches = tacheService.findByMainDOeuvreId(mainDOeuvre.getId());
                debugInfo.put("nombreTaches", taches != null ? taches.size() : 0);
            }
        }
        
        // Nombre total de main-d'œuvre
        List<MainDOeuvre> allMainDOeuvre = mainDOeuvreService.findAll();
        debugInfo.put("totalMainDOeuvre", allMainDOeuvre.size());
        
        // Nombre total de tâches
        List<Tache> allTaches = tacheService.findAll();
        debugInfo.put("totalTaches", allTaches.size());
        
        return ResponseEntity.ok(debugInfo);
        
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur: " + e.getMessage());
    }
}

/**
 * Vérifie si TOUTES les tâches d'une intervention sont terminées
 * et notifie le technicien uniquement dans ce cas
 */
/**
 * Vérifie si toutes les tâches d'une intervention sont terminées
 */

/**
 * GET /api/main-doeuvre/interventions/{id}/verifier-taches
 * Vérifie manuellement si toutes les tâches sont terminées
 */
@GetMapping("/interventions/{id}/verifier-taches")
public ResponseEntity<?> verifierToutesTachesTerminees(@PathVariable int id) {
    try {
        AgentMainDOeuvre agent = getCurrentAgent();
        if (agent == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Intervention intervention = interventionService.findById(id);
        if (intervention == null) {
            return ResponseEntity.notFound().build();
        }

        // Récupérer toutes les tâches
        List<Tache> taches = tacheService.findByInterventionId(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("interventionId", id);
        result.put("totalTaches", taches.size());
        result.put("tachesAFaire", taches.stream().filter(t -> "A_FAIRE".equals(t.getEtat())).count());
        result.put("tachesEnCours", taches.stream().filter(t -> "EN_COURS".equals(t.getEtat())).count());
        result.put("tachesTerminees", taches.stream().filter(t -> "TERMINEE".equals(t.getEtat())).count());
        result.put("tachesVerifiees", taches.stream().filter(t -> "VERIFIEE".equals(t.getEtat())).count());
        result.put("toutesTerminees", taches.stream()
                .allMatch(t -> "TERMINEE".equals(t.getEtat()) || "VERIFIEE".equals(t.getEtat())));
        
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
/**
 * Vérifie si toutes les tâches d'une intervention sont terminées
 */
/**
 * Vérifie si toutes les tâches d'une intervention sont terminées
 */
private void verifierEtNotifierSiToutesTachesTerminees(int interventionId) {
    try {
        System.out.println("🔍 === VÉRIFICATION TÂCHES TERMINÉES POUR INTERVENTION #" + interventionId + " ===");
        
        Intervention intervention = interventionService.findById(interventionId);
        if (intervention == null) {
            System.out.println("❌ Intervention non trouvée ID: " + interventionId);
            return;
        }
        
        System.out.println("📋 Intervention trouvée - ID: " + intervention.getId());
        System.out.println("👷 ID Technicien: " + intervention.getTechnicienId());
        System.out.println("🏷️ État intervention: " + intervention.getEtat());
        
        // Si l'intervention est déjà terminée, ne rien faire
        if (intervention.getEtat() == EtatInterventionType.TERMINEE) {
            System.out.println("ℹ️ Intervention déjà terminée, notification ignorée");
            return;
        }
        
        // Récupérer toutes les tâches de l'intervention
        List<Tache> taches = tacheService.findByInterventionId(interventionId);
        System.out.println("📊 Nombre total de tâches trouvées: " + taches.size());
        
        if (taches.isEmpty()) {
            System.out.println("ℹ️ Aucune tâche pour cette intervention");
            return;
        }
        
        // Afficher le détail de chaque tâche
        for (Tache t : taches) {
            System.out.println("   📝 Tâche ID: " + t.getId() + 
                             " | État: " + t.getEtat() + 
                             " | Libellé: " + t.getLibelle());
        }
        
        // Vérifier si TOUTES les tâches sont terminées (état TERMINEE ou VERIFIEE)
        boolean toutesTerminees = taches.stream()
                .allMatch(t -> "TERMINEE".equals(t.getEtat()) || "VERIFIEE".equals(t.getEtat()));
        
        // Compter les tâches par état
        long nbTerminees = taches.stream().filter(t -> "TERMINEE".equals(t.getEtat())).count();
        long nbVerifiees = taches.stream().filter(t -> "VERIFIEE".equals(t.getEtat())).count();
        long nbEnCours = taches.stream().filter(t -> "EN_COURS".equals(t.getEtat())).count();
        long nbAFaire = taches.stream().filter(t -> "A_FAIRE".equals(t.getEtat())).count();
        
        System.out.println("📈 Statistiques tâches:");
        System.out.println("  • À faire: " + nbAFaire);
        System.out.println("  • En cours: " + nbEnCours);
        System.out.println("  • Terminées: " + nbTerminees);
        System.out.println("  • Vérifiées: " + nbVerifiees);
        System.out.println("  • Toutes terminées? " + toutesTerminees);
        
        if (toutesTerminees) {
            System.out.println("🎉 🎉 🎉 TOUTES LES TÂCHES SONT TERMINÉES !");
            System.out.println("📢 Envoi notification au technicien ID: " + intervention.getTechnicienId());
            
            // Créer le message de notification
            String message = String.format(
                "🏁 **INTERVENTION #%d TERMINÉE**\n\n" +
                "Toutes les tâches ont été complétées par la main-d'œuvre.\n\n" +
                "📋 Détails:\n" +
                "• Intervention: #%d\n" +
                "• Type: %s\n" +
                "• Description: %s\n" +
                "• Date planifiée: %s\n" +
                "• Total tâches: %d\n" +
                "• Tâches terminées: %d\n" +
                "• Tâches vérifiées: %d\n\n" +
                "✅ Veuillez maintenant vérifier et clôturer l'intervention.",
                intervention.getId(),
                intervention.getId(),
                intervention.getTypeIntervention() != null ? intervention.getTypeIntervention() : "Non spécifié",
                intervention.getDescription() != null ? intervention.getDescription().substring(0, Math.min(50, intervention.getDescription().length())) : "Pas de description",
                intervention.getDatePlanifiee() != null ? intervention.getDatePlanifiee().toString() : "Non planifiée",
                taches.size(),
                nbTerminees,
                nbVerifiees
            );
            
            // Notifier le technicien
            System.out.println("📤 Appel notificationService.notifierTechnicien avec ID: " + intervention.getTechnicienId());
            notificationService.notifierTechnicien(intervention.getTechnicienId(), message);
            System.out.println("✅ Notification envoyée au technicien !");
            
            // Notifier aussi le chef de service si présent
            if (intervention.getChefServiceId() != null) {
                String messageChef = String.format(
                    "📊 **Intervention #%d terminée**\n" +
                    "Toutes les tâches ont été complétées.\n" +
                    "Le technicien doit maintenant vérifier et clôturer.",
                    intervention.getId()
                );
                notificationService.notifierChefService(intervention.getChefServiceId(), messageChef);
                System.out.println("✅ Notification envoyée au chef de service !");
            }
            
        } else {
            System.out.println("ℹ️ Pas toutes les tâches sont terminées:");
            System.out.println("   - Manquent: " + (taches.size() - (nbTerminees + nbVerifiees)) + " tâches");
        }
        
    } catch (Exception e) {
        System.err.println("❌ ERREUR dans verifierEtNotifierSiToutesTachesTerminees: " + e.getMessage());
        e.printStackTrace();
    }
}
/**
 * GET /api/main-doeuvre/test-verification/{interventionId}
 * Test manuel de vérification des tâches terminées
 */
@GetMapping("/test-verification/{interventionId}")
public ResponseEntity<?> testVerification(@PathVariable int interventionId) {
    try {
        System.out.println("🧪 TEST VERIFICATION pour intervention #" + interventionId);
        verifierEtNotifierSiToutesTachesTerminees(interventionId);
        return ResponseEntity.ok("Test exécuté - Vérifiez les logs");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
}