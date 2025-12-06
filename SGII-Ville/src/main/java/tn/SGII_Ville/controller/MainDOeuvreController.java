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
        private String nouvelEtat;
        private String commentaire;
        private Integer tempsPasseMinutes;

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
    public ResponseEntity<Tache> changerEtatTache(
            @PathVariable int tacheId,
            @RequestBody ChangerEtatTacheRequest request) {
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
            if (!isTransitionEtatValide(tache.getEtat(), request.getNouvelEtat())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
    public ResponseEntity<Tache> commencerTache(@PathVariable int tacheId) {
        ChangerEtatTacheRequest request = new ChangerEtatTacheRequest();
        request.setNouvelEtat("EN_COURS");
        request.setCommentaire("Tâche commencée");
        return changerEtatTache(tacheId, request);
    }

    /**
     * POST /api/main-doeuvre/taches/{tacheId}/terminer
     * Terminer une tâche (méthode spécifique)
     */
    @PostMapping("/taches/{tacheId}/terminer")
    public ResponseEntity<Tache> terminerTache(
            @PathVariable int tacheId,
            @RequestBody TerminerTacheRequest request) {
        ChangerEtatTacheRequest changerEtatRequest = new ChangerEtatTacheRequest();
        changerEtatRequest.setNouvelEtat("TERMINEE");
        changerEtatRequest.setCommentaire(request.getCommentaire());
        changerEtatRequest.setTempsPasseMinutes(request.getTempsPasseMinutes());
        return changerEtatTache(tacheId, changerEtatRequest);
    }

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
    private void notifierChangementEtat(Tache tache, MainDOeuvre mainDOeuvre, String nouvelEtat) {
        try {
            Intervention intervention = interventionService.findById(tache.getInterventionId());
            if (intervention != null) {
                String message = String.format(
                    "La main-d'œuvre %s %s a changé l'état de la tâche \"%s\" à %s (Intervention #%d)",
                    mainDOeuvre.getNom(), mainDOeuvre.getPrenom(), tache.getLibelle(), nouvelEtat, intervention.getId()
                );

                // Notifier le technicien
                notificationService.notifierTechnicien(intervention.getTechnicienId(), message);

                // Notifier le chef de service si présent
                if (intervention.getChefServiceId() != null) {
                    notificationService.notifierChefService(intervention.getChefServiceId(), message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log l'erreur mais ne pas faire échouer la requête principale
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
}