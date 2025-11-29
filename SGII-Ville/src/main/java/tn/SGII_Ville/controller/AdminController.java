package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.Utilisateur;
import tn.SGII_Ville.entities.Technicien;
import tn.SGII_Ville.entities.ChefDeService;
import tn.SGII_Ville.entities.MainDOeuvre;
import tn.SGII_Ville.entities.AgentMainDOeuvre;
import tn.SGII_Ville.service.UserXmlService;
import tn.SGII_Ville.service.MainDOeuvreXmlService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contrôleur pour les opérations admin
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserXmlService userXmlService;

    @Autowired
    private MainDOeuvreXmlService mainDOeuvreService;

    /**
     * GET /api/admin/users
     * Récupère tous les utilisateurs
     */
    @GetMapping("/users")
    public ResponseEntity<List<Utilisateur>> getAllUsers() {
        try {
            List<Utilisateur> users = userXmlService.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/techniciens
     * Récupère tous les techniciens
     */
    @GetMapping("/techniciens")
    public ResponseEntity<List<Technicien>> getAllTechniciens() {
        try {
            List<Technicien> techniciens = userXmlService.findAllTechniciens();
            return ResponseEntity.ok(techniciens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/admin/technicien
     * Crée un nouveau technicien
     */
    @PostMapping("/technicien")
    public ResponseEntity<?> createTechnicien(@RequestBody CreateTechnicienRequest request) {
        try {
            System.out.println("🔍 DEBUG - Request reçue: " + request.getNom() + " - " + request.getEmail());
            System.out.println("🔍 DEBUG - Compétences reçues: " + request.getCompetences());
            System.out.println("🔍 DEBUG - Nombre de compétences: " + (request.getCompetences() != null ? request.getCompetences().size() : 0));
            
            // Vérifier si l'email existe déjà
            if (userXmlService.findByEmail(request.getEmail()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cet email est déjà utilisé");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            // Créer le technicien avec le constructeur complet (comme dans AuthService)
            Technicien technicien = new Technicien(
                    0, // L'ID sera généré automatiquement
                    request.getNom(),
                    request.getEmail(),
                    userXmlService.encodePassword(request.getMotDePasse()),
                    request.getCompetences() != null ? request.getCompetences() : new java.util.ArrayList<>(),
                    true // Disponible par défaut
            );

            System.out.println("🔍 DEBUG - Technicien créé avec compétences: " + technicien.getCompetences());
            
            Utilisateur savedUser = userXmlService.save(technicien);
            
            System.out.println("🔍 DEBUG - Technicien sauvegardé: " + savedUser);
            if (savedUser instanceof Technicien) {
                System.out.println("🔍 DEBUG - Compétences après save: " + ((Technicien) savedUser).getCompetences());
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la création du technicien");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /api/admin/chef-service
     * Crée un nouveau chef de service
     */
    @PostMapping("/chef-service")
    public ResponseEntity<?> createChefService(@RequestBody CreateChefServiceRequest request) {
        try {
            // Vérifier si l'email existe déjà
            if (userXmlService.findByEmail(request.getEmail()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cet email est déjà utilisé");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            // Créer le chef de service avec le constructeur complet (comme dans AuthService)
            ChefDeService chef = new ChefDeService(
                    0, // L'ID sera généré automatiquement
                    request.getNom(),
                    request.getEmail(),
                    userXmlService.encodePassword(request.getMotDePasse()),
                    request.getDepartement()
            );

            Utilisateur savedUser = userXmlService.save(chef);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la création du chef de service");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * DELETE /api/admin/users/{id}
     * Supprime un utilisateur
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        try {
            boolean deleted = userXmlService.deleteById(id);
            if (deleted) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== MAIN D'ŒUVRE ====================

    /**
     * GET /api/admin/main-doeuvre
     * Récupère toutes les fiches de main-d'œuvre (pour admin)
     */
    @GetMapping("/main-doeuvre")
    public ResponseEntity<?> getAllMainDOeuvre(
            @RequestParam(required = false) String competence,
            @RequestParam(required = false) String disponibilite) {
        try {
            List<MainDOeuvre> list = new ArrayList<>();
            
            try {
                if (competence != null && !competence.isEmpty()) {
                    list = mainDOeuvreService.findByCompetence(competence);
                } else if (disponibilite != null && !disponibilite.isEmpty()) {
                    list = mainDOeuvreService.findByDisponibilite(disponibilite);
                } else {
                    list = mainDOeuvreService.findActive();
                }
            } catch (Exception e) {
                // Si le fichier n'existe pas encore ou erreur de parsing, retourner une liste vide
                logger.warn("Erreur lors du chargement de la main-d'œuvre (fichier peut-être vide): {}", e.getMessage());
                // Retourner une liste vide au lieu de lever une exception
                list = new ArrayList<>();
            }

            return ResponseEntity.ok(list);

        } catch (Exception e) {
            logger.error("Erreur critique lors du chargement de la main-d'œuvre", e);
            // En cas d'erreur critique, retourner quand même une liste vide
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * GET /api/admin/main-doeuvre/{id}
     * Récupère une fiche de main-d'œuvre (pour admin)
     */
    @GetMapping("/main-doeuvre/{id}")
    public ResponseEntity<?> getMainDOeuvre(@PathVariable int id) {
        try {
            MainDOeuvre mainDOeuvre = mainDOeuvreService.findById(id);
            if (mainDOeuvre == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(mainDOeuvre);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne: " + e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /api/admin/main-doeuvre
     * Crée une nouvelle fiche de main-d'œuvre et un compte utilisateur associé (pour admin)
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
            mainDOeuvre.setDisponibilite("DISPONIBLE");
            mainDOeuvre.setActive(true);
            MainDOeuvre saved = mainDOeuvreService.save(mainDOeuvre);

            // Créer le compte utilisateur AgentMainDOeuvre
            AgentMainDOeuvre agent = new AgentMainDOeuvre();
            agent.setNom(mainDOeuvre.getNom());
            agent.setPrenom(mainDOeuvre.getPrenom());
            agent.setEmail(mainDOeuvre.getEmail());
            agent.setMatricule(mainDOeuvre.getMatricule());
            agent.setCin(mainDOeuvre.getCin());
            agent.setTelephone(mainDOeuvre.getTelephone());
            agent.setMetier(mainDOeuvre.getMetier());
            agent.setCompetences(mainDOeuvre.getCompetences());
            agent.setMainDOeuvreId(saved.getId());

            // Générer un mot de passe par défaut (matricule ou CIN)
            String defaultPassword = mainDOeuvre.getMatricule() != null && !mainDOeuvre.getMatricule().isEmpty()
                ? mainDOeuvre.getMatricule()
                : mainDOeuvre.getCin();
            agent.setMotDePasse(defaultPassword); // Sera encodé automatiquement par UserXmlService

            // Sauvegarder le compte utilisateur
            Utilisateur savedUser = userXmlService.save(agent);

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
     * PUT /api/admin/main-doeuvre/{id}
     * Met à jour une fiche de main-d'œuvre (pour admin)
     */
    @PutMapping("/main-doeuvre/{id}")
    public ResponseEntity<?> updateMainDOeuvre(
            @PathVariable int id,
            @RequestBody MainDOeuvre mainDOeuvre) {
        try {
            MainDOeuvre existing = mainDOeuvreService.findById(id);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            // Mettre à jour les champs
            existing.setNom(mainDOeuvre.getNom());
            existing.setPrenom(mainDOeuvre.getPrenom());
            existing.setMatricule(mainDOeuvre.getMatricule());
            existing.setCin(mainDOeuvre.getCin());
            existing.setTelephone(mainDOeuvre.getTelephone());
            existing.setEmail(mainDOeuvre.getEmail());
            existing.setMetier(mainDOeuvre.getMetier());
            existing.setCompetences(mainDOeuvre.getCompetences());
            existing.setHabilitations(mainDOeuvre.getHabilitations());
            existing.setDisponibilite(mainDOeuvre.getDisponibilite());
            existing.setActive(mainDOeuvre.isActive());

            MainDOeuvre updated = mainDOeuvreService.save(existing);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne: " + e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * DELETE /api/admin/main-doeuvre/{id}
     * Archive une fiche de main-d'œuvre (pour admin)
     */
    @DeleteMapping("/main-doeuvre/{id}")
    public ResponseEntity<?> deleteMainDOeuvre(@PathVariable int id) {
        try {
            MainDOeuvre existing = mainDOeuvreService.findById(id);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            // Archiver au lieu de supprimer
            existing.setDisponibilite("ARCHIVE");
            existing.setActive(false);
            mainDOeuvreService.save(existing);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne: " + e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Classe interne pour la création de technicien
     */
    public static class CreateTechnicienRequest {
        private String nom;
        private String email;
        private String motDePasse;
        private List<String> competences;

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMotDePasse() { return motDePasse; }
        public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
        public List<String> getCompetences() { return competences; }
        public void setCompetences(List<String> competences) { this.competences = competences; }
    }

    /**
     * Classe interne pour la création de chef de service
     */
    public static class CreateChefServiceRequest {
        private String nom;
        private String email;
        private String motDePasse;
        private String departement;

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMotDePasse() { return motDePasse; }
        public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
        public String getDepartement() { return departement; }
        public void setDepartement(String departement) { this.departement = departement; }
    }
}
