package tn.SGII_Ville.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import tn.SGII_Ville.dto.*;
import tn.SGII_Ville.entities.*;
import tn.SGII_Ville.model.enums.EtatInterventionType;
import tn.SGII_Ville.service.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur dédié aux techniciens
 * Endpoints accessibles uniquement aux utilisateurs authentifiés avec rôle TECHNICIEN
 */
@RestController
@RequestMapping("/api/technicien")
@CrossOrigin(origins = "http://localhost:4200")
public class TechnicienController {

    private static final Logger logger = LoggerFactory.getLogger(TechnicienController.class);

    @Autowired
    private InterventionXmlService interventionService;

    @Autowired
    private UserXmlService userXmlService;

    @Autowired
    private MainDOeuvreXmlService mainDOeuvreService;

    @Autowired
    private DemandeXmlService demandeService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private TacheXmlService tacheService;

    @Autowired
    private PhotoXmlService photoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MainDOeuvreVerificationService verificationService;

    // ==================== T2 - TABLEAU DE BORD ====================

    /**
     * GET /api/technicien/interventions
     * Récupère toutes les interventions du technicien (du jour et futures)
     */
    @GetMapping("/interventions")
    public ResponseEntity<List<Intervention>> getMyInterventions(
            @RequestParam(required = false) String etat,
            @RequestParam(required = false) String priorite,
            @RequestParam(required = false) String date) {
        try {
            int technicienId = getCurrentTechnicienId();
            List<Intervention> allInterventions = interventionService.getAllInterventions();
            List<Intervention> myInterventions = allInterventions.stream()
                    .filter(i -> i.getTechnicienId() == technicienId)
                    .collect(Collectors.toList());

            // Filtres
            if (etat != null && !etat.isEmpty()) {
                myInterventions = myInterventions.stream()
                        .filter(i -> i.getEtat().name().equals(etat))
                        .collect(Collectors.toList());
            }
            if (priorite != null && !priorite.isEmpty()) {
                myInterventions = myInterventions.stream()
                        .filter(i -> i.getPriorite().name().equals(priorite))
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(myInterventions);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * GET /api/technicien/interventions/{id}
     * Récupère les détails complets d'une intervention
     */
    @GetMapping("/interventions/{id}")
    public ResponseEntity<Intervention> getInterventionDetails(@PathVariable int id) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // S'assurer que ouvrierIds est initialisé
            if (intervention.getOuvrierIds() == null) {
                intervention.setOuvrierIds(new ArrayList<>());
            }

            return ResponseEntity.ok(intervention);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/technicien/interventions/{id}/confirmer
     * Confirmer la réception d'une intervention
     */
    @PostMapping("/interventions/{id}/confirmer")
    public ResponseEntity<Intervention> confirmerIntervention(@PathVariable int id) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Mettre à jour l'état à ACCEPTEE si EN_ATTENTE
            if (intervention.getEtat() == EtatInterventionType.EN_ATTENTE) {
                interventionService.updateEtat(id, EtatInterventionType.EN_ATTENTE);
            }

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== T3 - GESTION INTERVENTION ====================

    /**
     * POST /api/technicien/interventions/{id}/commencer
     * Commencer une intervention
     */
    @PostMapping("/interventions/{id}/commencer")
    public ResponseEntity<Intervention> commencerIntervention(@PathVariable int id) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            intervention.setEtat(EtatInterventionType.EN_COURS);
            intervention.setDateDebut(LocalDate.now());
            interventionService.updateIntervention(intervention);

            // Notification au chef
            notificationService.notifierChefService(intervention.getChefServiceId(), 
                "Intervention #" + id + " démarrée par le technicien");

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/technicien/interventions/{id}/pause
     * Mettre en pause une intervention
     */
    @PostMapping("/interventions/{id}/pause")
    public ResponseEntity<Intervention> mettreEnPause(@PathVariable int id) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            intervention.setEtat(EtatInterventionType.SUSPENDUE);
            interventionService.updateIntervention(intervention);

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/technicien/interventions/{id}/reprendre
     * Reprendre une intervention suspendue
     */
    @PostMapping("/interventions/{id}/reprendre")
    public ResponseEntity<Intervention> reprendreIntervention(@PathVariable int id) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            intervention.setEtat(EtatInterventionType.EN_COURS);
            interventionService.updateIntervention(intervention);

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/technicien/interventions/{id}/photos
     * Ajouter des photos à une intervention
     */
    @PostMapping("/interventions/{id}/photos")
    public ResponseEntity<Intervention> ajouterPhotos(
            @PathVariable int id,
            @RequestParam("files") MultipartFile[] files) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Utiliser storeFiles qui prend un tableau et retourne une liste de Photo
            List<Photo> photos = fileStorageService.storeFiles(files);
            
            // Note: Les photos sont liées à la demande, pas à l'intervention

            interventionService.updateIntervention(intervention);

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/technicien/interventions/{id}/commentaire
     * Ajouter un commentaire à une intervention
     */
    @PostMapping("/interventions/{id}/commentaire")
    public ResponseEntity<Intervention> ajouterCommentaire(
            @PathVariable int id,
            @RequestBody String commentaire) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            intervention.setCommentaire(commentaire);
            interventionService.updateIntervention(intervention);

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== T4 - MISE À JOUR D'ÉTAT ====================

    /**
     * PATCH /api/technicien/interventions/{id}/etat
     * Mettre à jour l'état d'une intervention
     */
    @PatchMapping("/interventions/{id}/etat")
    public ResponseEntity<Intervention> updateEtat(
            @PathVariable int id,
            @RequestBody UpdateEtatInterventionRequest request) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            intervention.setEtat(request.getNouvelEtat());
            if (request.getTempsPasseMinutes() != null) {
                intervention.setTempsPasseMinutes(request.getTempsPasseMinutes());
            }
            if (request.getCommentaire() != null) {
                intervention.setCommentaire(request.getCommentaire());
            }

            interventionService.updateIntervention(intervention);

            // Notification au chef
            notificationService.notifierChefService(intervention.getChefServiceId(), 
                "Intervention #" + id + " : État mis à jour à " + request.getNouvelEtat());

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== T7 - RAPPORT FINAL ====================

    /**
     * POST /api/technicien/interventions/{id}/rapport-final
     * Clôturer une intervention avec un rapport final
     */
    @PostMapping("/interventions/{id}/rapport-final")
    public ResponseEntity<Intervention> soumettreRapportFinal(
            @PathVariable int id,
            @RequestBody RapportFinalRequest request) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            intervention.setRapportFinal(request.getResultatObtenu());
            intervention.setTempsPasseMinutes(request.getTempsTotalMinutes());
            intervention.setCommentaire(request.getProblemesRencontres());
            intervention.setSignatureElectronique(request.getSignatureElectronique());
            intervention.setEtat(EtatInterventionType.TERMINEE);
            intervention.setDateFin(LocalDate.now());

            // Note: Les photos sont liées à la demande, pas à l'intervention

            interventionService.updateIntervention(intervention);

            // Notification au chef
            notificationService.notifierChefService(intervention.getChefServiceId(), 
                "Intervention #" + id + " terminée - Rapport final soumis");

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== T6 - GESTION MAIN-D'ŒUVRE ====================

    /**
     * GET /api/technicien/main-doeuvre
     * Récupère toutes les fiches de main-d'œuvre
     */
    @GetMapping("/main-doeuvre")
    public ResponseEntity<List<MainDOeuvre>> getAllMainDOeuvre(
            @RequestParam(required = false) String competence,
            @RequestParam(required = false) String disponibilite) {
        try {
            List<MainDOeuvre> list;
            
            if (competence != null && !competence.isEmpty()) {
                list = mainDOeuvreService.findByCompetence(competence);
            } else if (disponibilite != null && !disponibilite.isEmpty()) {
                list = mainDOeuvreService.findByDisponibilite(disponibilite);
            } else {
                list = mainDOeuvreService.findActive();
            }

            return ResponseEntity.ok(list);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/technicien/main-doeuvre/{id}
     * Récupère une fiche de main-d'œuvre
     */
    @GetMapping("/main-doeuvre/{id}")
    public ResponseEntity<MainDOeuvre> getMainDOeuvre(@PathVariable int id) {
        try {
            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(id);
            if (mainDOeuvre == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(mainDOeuvre);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/technicien/main-doeuvre
     * Créer une nouvelle fiche de main-d'œuvre et un compte utilisateur associé
     */
    @PostMapping("/main-doeuvre")
    public ResponseEntity<?> createMainDOeuvre(@RequestBody MainDOeuvre mainDOeuvre) {
        try {
            // Vérifier que l'email est fourni et n'existe pas déjà
            if (mainDOeuvre.getEmail() == null || mainDOeuvre.getEmail().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "L'email est obligatoire pour créer un compte");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (userXmlService.findByEmail(mainDOeuvre.getEmail()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cet email est déjà utilisé");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            // Sauvegarder la fiche main-d'œuvre
            mainDOeuvre.setDisponibilite("LIBRE");
            MainDOeuvre saved = mainDOeuvreService.save(mainDOeuvre);

            // Créer le compte utilisateur AgentMainDOeuvre
            AgentMainDOeuvre agent = new AgentMainDOeuvre();
            agent.setNom(mainDOeuvre.getNom());
            agent.setPrenom(mainDOeuvre.getPrenom());
            agent.setEmail(mainDOeuvre.getEmail());
            agent.setMatricule(mainDOeuvre.getMatricule());
            agent.setCin(mainDOeuvre.getCin());
            agent.setTelephone(mainDOeuvre.getTelephone());
            // Metier supprimé du schéma XSD
            // agent.setMetier(mainDOeuvre.getMetier());
            // Convertir la compétence unique en liste pour AgentMainDOeuvre
            if (mainDOeuvre.getCompetence() != null) {
                List<String> competences = new ArrayList<>();
                competences.add(mainDOeuvre.getCompetence());
                agent.setCompetences(competences);
            }
            agent.setMainDOeuvreId(saved.getId());

            // Générer un mot de passe par défaut (matricule ou CIN)
            String defaultPassword = mainDOeuvre.getMatricule() != null && !mainDOeuvre.getMatricule().isEmpty()
                ? mainDOeuvre.getMatricule()
                : mainDOeuvre.getCin();
            agent.setMotDePasse(defaultPassword); // Sera encodé automatiquement par UserXmlService

            // Sauvegarder le compte utilisateur
            Utilisateur savedUser = userXmlService.save(agent);

            // Envoyer une notification avec les identifiants
            try {
                notificationService.notifierChefService(
                    getCurrentTechnicienId(),
                    "Nouvelle main-d'œuvre créée : " + mainDOeuvre.getNom() + " " + mainDOeuvre.getPrenom() +
                    " - Email: " + mainDOeuvre.getEmail() + " - Mot de passe par défaut: " + defaultPassword
                );
            } catch (Exception e) {
                // Log mais ne pas bloquer
                System.err.println("Erreur notification: " + e.getMessage());
            }

            // Retourner la fiche avec l'ID du compte utilisateur
            Map<String, Object> response = new HashMap<>();
            response.put("mainDOeuvre", saved);
            response.put("userId", savedUser.getId());
            response.put("defaultPassword", defaultPassword);
            response.put("message", "Fiche et compte utilisateur créés avec succès");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la création: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * PUT /api/technicien/main-doeuvre/{id}
     * Modifier une fiche de main-d'œuvre
     */
    @PutMapping("/main-doeuvre/{id}")
    public ResponseEntity<MainDOeuvre> updateMainDOeuvre(
            @PathVariable int id,
            @RequestBody MainDOeuvre mainDOeuvre) {
        try {
            mainDOeuvre.setId(id);
            MainDOeuvre updated = mainDOeuvreService.save(mainDOeuvre);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/technicien/main-doeuvre/{id}
     * Archiver/désactiver une fiche de main-d'œuvre
     */
    @DeleteMapping("/main-doeuvre/{id}")
    public ResponseEntity<Void> archiverMainDOeuvre(@PathVariable int id) {
        try {
            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(id);
            if (mainDOeuvre == null) {
                return ResponseEntity.notFound().build();
            }

            // Active supprimé du schéma XSD - utiliser disponibilite à la place
            mainDOeuvre.setDisponibilite("ARCHIVE");
            mainDOeuvreService.save(mainDOeuvre);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/technicien/interventions/{id}/verifier-affectation
     * Vérifier si un agent peut être affecté (avant affectation)
     */
    @PostMapping("/interventions/{id}/verifier-affectation")
    public ResponseEntity<VerificationAffectationDTO> verifierAffectation(
            @PathVariable int id,
            @RequestBody Map<String, Integer> request) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Integer mainDOeuvreId = request.get("mainDOeuvreId");
            if (mainDOeuvreId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(mainDOeuvreId);
            if (mainDOeuvre == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Charger toutes les interventions pour vérifier les conflits
            List<Intervention> toutesInterventions = interventionService.getAllInterventions();

            VerificationAffectationDTO verification = verificationService.verifierAffectation(
                mainDOeuvre, intervention, toutesInterventions);

            return ResponseEntity.ok(verification);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/technicien/interventions/{id}/affecter-main-doeuvre
     * Affecter de la main-d'œuvre à une intervention avec vérifications complètes
     */
    @PostMapping("/interventions/{id}/affecter-main-doeuvre")
    public ResponseEntity<?> affecterMainDOeuvre(
            @PathVariable int id,
            @RequestBody AffecterMainDOeuvreRequest request) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Vérifier que la requête contient des IDs
            if (request.getOuvrierIds() == null || request.getOuvrierIds().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("erreurs", List.of("Aucun agent de main-d'œuvre spécifié"));
                response.put("avertissements", new ArrayList<>());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Initialiser ouvrierIds si null
            if (intervention.getOuvrierIds() == null) {
                intervention.setOuvrierIds(new ArrayList<>());
            }

            // Charger toutes les interventions pour vérifier les conflits
            List<Intervention> toutesInterventions = interventionService.getAllInterventions();
            List<String> toutesErreurs = new ArrayList<>();

            // Vérifier chaque agent avant affectation
            for (Integer mainDOeuvreId : request.getOuvrierIds()) {
                MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(mainDOeuvreId);
                if (mainDOeuvre == null) {
                    toutesErreurs.add("Agent #" + mainDOeuvreId + " non trouvé");
                    continue;
                }

                VerificationAffectationDTO verification = verificationService.verifierAffectation(
                    mainDOeuvre, intervention, toutesInterventions);

                if (!verification.isValide()) {
                    toutesErreurs.addAll(verification.getErreurs());
                }
            }

            // Si erreurs, retourner les erreurs
            if (!toutesErreurs.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("erreurs", toutesErreurs);
                response.put("avertissements", new ArrayList<>());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Affecter les agents (éviter les doublons)
            for (Integer mainDOeuvreId : request.getOuvrierIds()) {
                // S'assurer que la liste n'est pas null
                if (intervention.getOuvrierIds() == null) {
                    intervention.setOuvrierIds(new ArrayList<>());
                }
                if (!intervention.getOuvrierIds().contains(mainDOeuvreId)) {
                    intervention.getOuvrierIds().add(mainDOeuvreId);
                }
            }
            
            // Mettre à jour la disponibilité et l'historique
            for (Integer mainDOeuvreId : request.getOuvrierIds()) {
                MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(mainDOeuvreId);
                if (mainDOeuvre != null) {
                    mainDOeuvre.setDisponibilite("OCCUPE");
                    
                    // HistoriqueInterventions supprimé du schéma XSD - plus de gestion d'historique
                    // if (!mainDOeuvre.getHistoriqueInterventionIds().contains(id)) {
                    //     mainDOeuvre.getHistoriqueInterventionIds().add(id);
                    // }
                    
                    mainDOeuvreService.save(mainDOeuvre);
                }
            }

            // Sauvegarder l'intervention mise à jour
            interventionService.updateIntervention(intervention);

            // Notification au chef
            notificationService.notifierChefService(intervention.getChefServiceId(), 
                "Main-d'œuvre affectée à l'intervention #" + id);

            // Recharger l'intervention depuis la base pour s'assurer d'avoir les données à jour
            Intervention updatedIntervention = interventionService.findById(id);
            // S'assurer que ouvrierIds est initialisé
            if (updatedIntervention.getOuvrierIds() == null) {
                updatedIntervention.setOuvrierIds(new ArrayList<>());
            }
            logger.info("Intervention {} mise à jour, ouvrierIds: {}", id, updatedIntervention.getOuvrierIds());
            return ResponseEntity.ok(updatedIntervention);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/technicien/interventions/{id}/main-doeuvre/{mainDOeuvreId}
     * Désaffecter de la main-d'œuvre d'une intervention
     */
    @DeleteMapping("/interventions/{id}/main-doeuvre/{mainDOeuvreId}")
    public ResponseEntity<Intervention> desaffecterMainDOeuvre(
            @PathVariable int id,
            @PathVariable int mainDOeuvreId) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(id);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // S'assurer que ouvrierIds est initialisé
            if (intervention.getOuvrierIds() == null) {
                intervention.setOuvrierIds(new ArrayList<>());
            }

            intervention.getOuvrierIds().removeIf(mid -> mid == mainDOeuvreId);
            interventionService.updateIntervention(intervention);

            // Remettre la disponibilité (vérifier s'il n'est pas affecté ailleurs)
            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(mainDOeuvreId);
            if (mainDOeuvre != null) {
                // Vérifier si l'agent est encore affecté à d'autres interventions
                List<Intervention> toutesInterventions = interventionService.getAllInterventions();
                boolean encoreAffecte = toutesInterventions.stream()
                    .anyMatch(i -> i.getOuvrierIds() != null && 
                                  i.getOuvrierIds().contains(mainDOeuvreId) && 
                                  i.getId() != id);
                
                if (!encoreAffecte) {
                    mainDOeuvre.setDisponibilite("DISPONIBLE");
                    mainDOeuvreService.save(mainDOeuvre);
                }
            }

            // Notification au chef
            notificationService.notifierChefService(intervention.getChefServiceId(), 
                "Main-d'œuvre désaffectée de l'intervention #" + id);

            // Recharger l'intervention mise à jour
            Intervention updatedIntervention = interventionService.findById(id);
            // S'assurer que ouvrierIds est initialisé
            if (updatedIntervention.getOuvrierIds() == null) {
                updatedIntervention.setOuvrierIds(new ArrayList<>());
            }
            logger.info("Intervention {} - main-d'œuvre {} désaffectée, ouvrierIds restants: {}", 
                id, mainDOeuvreId, updatedIntervention.getOuvrierIds());
            return ResponseEntity.ok(updatedIntervention);

        } catch (Exception e) {
            logger.error("Erreur lors de la désaffectation de main-d'œuvre {} de l'intervention {}", mainDOeuvreId, id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/technicien/main-doeuvre/{id}/historique
     * @deprecated HistoriqueInterventions supprimé du schéma XSD
     * Récupère l'historique complet des interventions d'un agent
     */
    @GetMapping("/main-doeuvre/{id}/historique")
    @Deprecated
    public ResponseEntity<List<HistoriqueInterventionDTO>> getHistoriqueMainDOeuvre(@PathVariable int id) {
        // HistoriqueInterventions n'est plus dans le schéma XSD - retourner une liste vide
        return ResponseEntity.ok(new ArrayList<>());
    }

    // ==================== T8 - PROFIL TECHNICIEN ====================

    /**
     * GET /api/technicien/profil
     * Récupère le profil du technicien connecté
     */
    @GetMapping("/profil")
    public ResponseEntity<Technicien> getProfil() {
        try {
            int technicienId = getCurrentTechnicienId();
            Optional<Utilisateur> userOpt = userXmlService.findById(technicienId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            Utilisateur user = userOpt.get();
            if (user instanceof Technicien) {
                return ResponseEntity.ok((Technicien) user);
            }
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/technicien/profil
     * Mettre à jour le profil du technicien
     */
    @PutMapping("/profil")
    public ResponseEntity<Technicien> updateProfil(@RequestBody UpdateProfilTechnicienRequest request) {
        try {
            int technicienId = getCurrentTechnicienId();
            Optional<Utilisateur> userOpt = userXmlService.findById(technicienId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            Utilisateur user = userOpt.get();
            if (!(user instanceof Technicien)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Technicien technicien = (Technicien) user;
            
            if (request.getNom() != null) technicien.setNom(request.getNom());
            if (request.getEmail() != null) technicien.setEmail(request.getEmail());
            if (request.getTelephone() != null) {
                // Mettre à jour le téléphone si l'entité le supporte
            }
            if (request.getCompetences() != null) technicien.setCompetences(request.getCompetences());
            
            // Changer le mot de passe si fourni
            if (request.getNouveauMotDePasse() != null && request.getAncienMotDePasse() != null) {
                if (passwordEncoder.matches(request.getAncienMotDePasse(), technicien.getMotDePasse())) {
                    technicien.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
            }

            userXmlService.update(technicien);

            Optional<Utilisateur> updatedOpt = userXmlService.findById(technicienId);
            if (updatedOpt.isPresent() && updatedOpt.get() instanceof Technicien) {
                return ResponseEntity.ok((Technicien) updatedOpt.get());
            }
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/technicien/statistiques
     * Récupère les statistiques du technicien
     */
    @GetMapping("/statistiques")
    public ResponseEntity<?> getStatistiques() {
        try {
            int technicienId = getCurrentTechnicienId();
            List<Intervention> interventions = interventionService.getAllInterventions().stream()
                    .filter(i -> i.getTechnicienId() == technicienId)
                    .collect(Collectors.toList());

            long total = interventions.size();
            long terminees = interventions.stream()
                    .filter(i -> i.getEtat() == EtatInterventionType.TERMINEE)
                    .count();
            
            double tauxReussite = total > 0 ? (terminees * 100.0 / total) : 0;
            
            int tempsTotal = interventions.stream()
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

    // ==================== MÉTHODES UTILITAIRES ====================

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // ==================== GESTION DES TÂCHES ====================

    /**
     * POST /api/technicien/interventions/{interventionId}/taches
     * Créer une nouvelle tâche pour une intervention
     */
    @PostMapping("/interventions/{interventionId}/taches")
    public ResponseEntity<Tache> createTache(
            @PathVariable int interventionId,
            @RequestBody CreateTacheRequest request) {
        try {
            // Validation
            if (request.getLibelle() == null || request.getLibelle().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(interventionId);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Tache tache = new Tache();
            tache.setInterventionId(interventionId);
            tache.setLibelle(request.getLibelle());
            tache.setDescription(request.getDescription());
            tache.setMainDOeuvreId(request.getMainDOeuvreId());
            tache.setOrdre(request.getOrdre());
            tache.setEtat("A_FAIRE");
            tache.setDateCreation(LocalDateTime.now());
            tache.setVerifiee(false);

            System.out.println("Création tâche pour intervention #" + interventionId + " : " + request.getLibelle());
            Tache saved = tacheService.save(tache);
            System.out.println("Tâche créée avec ID: " + saved.getId());

            // Notification au chef de service
            if (intervention.getChefServiceId() != null) {
                notificationService.notifierChefService(intervention.getChefServiceId(),
                    "Nouvelle tâche créée pour l'intervention #" + interventionId + " : " + request.getLibelle());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            System.err.println("❌ Erreur création tâche: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/technicien/interventions/{interventionId}/taches
     * Récupérer toutes les tâches d'une intervention
     */
    @GetMapping("/interventions/{interventionId}/taches")
    public ResponseEntity<List<Tache>> getTaches(@PathVariable int interventionId) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(interventionId);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Tache> taches = tacheService.findByInterventionId(interventionId);
            return ResponseEntity.ok(taches);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/technicien/taches/{tacheId}/assigner
     * Assigner une tâche à une main-d'œuvre
     */
    @PutMapping("/taches/{tacheId}/assigner")
    public ResponseEntity<Tache> assignerTache(
            @PathVariable int tacheId,
            @RequestBody AssignerTacheRequest request) {
        try {
            Tache tache = tacheService.findById(tacheId);
            if (tache == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Intervention intervention = interventionService.findById(tache.getInterventionId());
            int technicienId = getCurrentTechnicienId();
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(request.getMainDOeuvreId());
            if (mainDOeuvre == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            tache.setMainDOeuvreId(request.getMainDOeuvreId());
            Tache saved = tacheService.save(tache);

            // Notification à la main-d'œuvre
            notificationService.notifierMainDOeuvre(request.getMainDOeuvreId(),
                "Nouvelle tâche assignée : " + tache.getLibelle() + " (Intervention #" + intervention.getId() + ")");

            // Notification au chef de service
            if (intervention.getChefServiceId() != null) {
                notificationService.notifierChefService(intervention.getChefServiceId(),
                    "Tâche \"" + tache.getLibelle() + "\" assignée à " + mainDOeuvre.getNom() + " " + mainDOeuvre.getPrenom());
            }

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/technicien/taches/{tacheId}/verifier
     * Vérifier qu'une tâche est bien terminée (côté technicien)
     */
    @PostMapping("/taches/{tacheId}/verifier")
    public ResponseEntity<Tache> verifierTache(
            @PathVariable int tacheId,
            @RequestBody VerifierTacheRequest request) {
        try {
            Tache tache = tacheService.findById(tacheId);
            if (tache == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Intervention intervention = interventionService.findById(tache.getInterventionId());
            int technicienId = getCurrentTechnicienId();
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!"TERMINEE".equals(tache.getEtat())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            if (request.isValidee()) {
                tache.setVerifiee(true);
                tache.setDateVerification(LocalDateTime.now());
                tache.setCommentaireTechnicien(request.getCommentaire());
                tache.setEtat("VERIFIEE");
            } else {
                // La tâche doit être refaite
                tache.setEtat("A_FAIRE");
                tache.setDateFin(null);
                tache.setCommentaireTechnicien(request.getCommentaire());
            }

            Tache saved = tacheService.save(tache);

            // Si la tâche est validée, remettre immédiatement la main-d'œuvre en DISPONIBLE
            // Même si d'autres tâches de l'intervention ne sont pas terminées
            if (request.isValidee() && tache.getMainDOeuvreId() != null) {
                try {
                    MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(tache.getMainDOeuvreId());
                    if (mainDOeuvre != null && !"DISPONIBLE".equals(mainDOeuvre.getDisponibilite())) {
                        mainDOeuvre.setDisponibilite("DISPONIBLE");
                        mainDOeuvreService.save(mainDOeuvre);
                        logger.info("Main-d'œuvre #{} remise en DISPONIBLE car sa tâche \"{}\" a été vérifiée", 
                                tache.getMainDOeuvreId(), tache.getLibelle());
                    }
                } catch (Exception e) {
                    logger.error("Erreur lors de la mise à jour du statut de la main-d'œuvre", e);
                    // Ne pas bloquer la vérification de la tâche en cas d'erreur
                }
            }

            // Notification à la main-d'œuvre
            if (tache.getMainDOeuvreId() != null) {
                if (request.isValidee()) {
                    notificationService.notifierMainDOeuvre(tache.getMainDOeuvreId(),
                        "Votre tâche \"" + tache.getLibelle() + "\" a été vérifiée et validée par le technicien.");
                } else {
                    notificationService.notifierMainDOeuvre(tache.getMainDOeuvreId(),
                        "Votre tâche \"" + tache.getLibelle() + "\" nécessite des corrections. Veuillez la refaire.");
                }
            }

            // Notification au chef de service
            if (intervention.getChefServiceId() != null) {
                notificationService.notifierChefService(intervention.getChefServiceId(),
                    "Tâche \"" + tache.getLibelle() + "\" " + (request.isValidee() ? "vérifiée" : "à refaire") + " (Intervention #" + intervention.getId() + ")");
            }

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/technicien/interventions/{interventionId}/terminer
     * Terminer une intervention (uniquement si toutes les tâches sont vérifiées)
     */
    @PostMapping("/interventions/{interventionId}/terminer")
    public ResponseEntity<Intervention> terminerIntervention(@PathVariable int interventionId) {
        try {
            int technicienId = getCurrentTechnicienId();
            Intervention intervention = interventionService.findById(interventionId);
            
            if (intervention == null || intervention.getTechnicienId() != technicienId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Vérifier que toutes les tâches sont vérifiées
            List<Tache> taches = tacheService.findByInterventionId(interventionId);
            if (taches.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Pas de tâches
            }

            boolean toutesVerifiees = taches.stream()
                    .allMatch(Tache::isVerifiee);

            if (!toutesVerifiees) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Toutes les tâches ne sont pas vérifiées
            }

            intervention.setEtat(EtatInterventionType.TERMINEE);
            intervention.setDateFin(LocalDate.now());
            interventionService.updateIntervention(intervention);

            // Notification au chef de service
            if (intervention.getChefServiceId() != null) {
                notificationService.notifierChefService(intervention.getChefServiceId(),
                    "Intervention #" + interventionId + " terminée et vérifiée par le technicien");
            }

            // Notification au citoyen
            if (intervention.getDemandeId() > 0) {
                Demande demande = demandeService.findById(intervention.getDemandeId());
                if (demande != null && demande.getCitoyenId() > 0) {
                    notificationService.notifierCitoyen(demande.getCitoyenId(),
                        "Votre demande #" + demande.getId() + " a été traitée avec succès ! Intervention #" + interventionId + " terminée.");
                }
            }

            return ResponseEntity.ok(intervention);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private int getCurrentTechnicienId() {
        String email = getCurrentUserEmail();
        Utilisateur user = userXmlService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        if (!(user instanceof Technicien)) {
            throw new RuntimeException("Utilisateur n'est pas un technicien");
        }
        
        return user.getId();
    }
}