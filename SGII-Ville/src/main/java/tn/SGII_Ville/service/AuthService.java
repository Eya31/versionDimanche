package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import tn.SGII_Ville.dto.LoginRequest;
import tn.SGII_Ville.dto.LoginResponse;
import tn.SGII_Ville.dto.RegisterRequest;
import tn.SGII_Ville.entities.Administrateur;
import tn.SGII_Ville.entities.AgentMainDOeuvre;
import tn.SGII_Ville.entities.ChefDeService;
import tn.SGII_Ville.entities.Citoyen;
import tn.SGII_Ville.entities.MainDOeuvre;
import tn.SGII_Ville.entities.Technicien;
import tn.SGII_Ville.entities.Utilisateur;
import tn.SGII_Ville.model.enums.RoleType;

import java.util.ArrayList;

/**
 * Service d'authentification
 */
@Service
public class AuthService {

    @Autowired
    private UserXmlService userXmlService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MainDOeuvreXmlService mainDOeuvreService;

    /**
     * Authentifie un utilisateur et retourne un token JWT
     */
    /**
 * Authentifie un utilisateur et retourne un token JWT
 */
public LoginResponse login(LoginRequest request) {
    System.out.println("🔍 Tentative de connexion pour: " + request.getEmail());
    
    // Trouver l'utilisateur par email
    Utilisateur utilisateur = userXmlService.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                System.out.println("❌ Utilisateur non trouvé: " + request.getEmail());
                return new RuntimeException("Email ou mot de passe incorrect");
            });

    System.out.println("✅ Utilisateur trouvé: " + utilisateur.getNom() + 
                      " (ID: " + utilisateur.getId() + 
                      ", Rôle: " + utilisateur.getRole() + ")");
    
    // Vérifier si le mot de passe existe dans l'utilisateur
    if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isEmpty()) {
        System.err.println("❌ Mot de passe manquant dans la base pour l'utilisateur: " + utilisateur.getEmail());
        throw new RuntimeException("Problème de configuration du compte");
    }

    System.out.println("🔑 Hash en BD: " + utilisateur.getMotDePasse());
    System.out.println("🔑 Mot de passe fourni: " + request.getMotDePasse());

    // Vérifier le mot de passe
    boolean passwordMatches = passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse());
    System.out.println("🔐 Résultat vérification: " + passwordMatches);
    
    if (!passwordMatches) {
        System.out.println("❌ Mot de passe incorrect");
        throw new RuntimeException("Email ou mot de passe incorrect");
    }

    System.out.println("✅ Authentification réussie");

    // Générer le token JWT
    String token = jwtService.generateToken(utilisateur);

    // Retourner la réponse
    return new LoginResponse(
            token,
            utilisateur.getId(),
            utilisateur.getNom(),
            utilisateur.getEmail(),
            utilisateur.getRole()
    );
}
    /**
     * Enregistre un nouvel utilisateur
     */
    public LoginResponse register(RegisterRequest request) {
        System.out.println("📝 Tentative d'inscription pour: " + request.getEmail() + " - Rôle: " + request.getRole());

        // Vérifier si l'email existe déjà
        if (userXmlService.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Sauvegarder le mot de passe en clair pour l'email (avant encodage)
        String plainPassword = request.getMotDePasse();

        // Créer l'utilisateur selon son rôle
        Utilisateur utilisateur = createUserFromRequest(request);

        // Sauvegarder l'utilisateur
        utilisateur = userXmlService.save(utilisateur);

        // Pour MAIN_DOEUVRE, créer également une entrée dans MainDOeuvre
        if (request.getRole() == RoleType.MAIN_DOEUVRE) {
            createMainDOeuvreEntry(utilisateur, request);
        }

        // Envoyer un email de bienvenue avec les informations de connexion
        try {
            emailService.sendWelcomeEmail(
                utilisateur.getEmail(), 
                utilisateur.getNom(), 
                plainPassword,  // Mot de passe en clair
                utilisateur.getRole().name()  // Rôle
            );
            System.out.println("✅ Email de bienvenue envoyé à: " + utilisateur.getEmail());
        } catch (Exception e) {
            // Log l'erreur mais ne pas bloquer l'enregistrement
            System.err.println("❌ Erreur lors de l'envoi de l'email de bienvenue: " + e.getMessage());
            e.printStackTrace();
        }

        // Générer le token JWT
        String token = jwtService.generateToken(utilisateur);

        // Retourner la réponse
        return new LoginResponse(
                token,
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getEmail(),
                utilisateur.getRole()
        );
    }

    /**
     * Initie la réinitialisation du mot de passe
     */
    public void forgotPassword(String email) {
        // Trouver l'utilisateur
        Utilisateur utilisateur = userXmlService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur trouvé avec cet email"));

        // Générer un token de réinitialisation
        String resetToken = jwtService.generatePasswordResetToken(email);

        // Envoyer l'email avec le lien de réinitialisation
        try {
            emailService.sendPasswordResetEmail(email, utilisateur.getNom(), resetToken);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation", e);
        }
    }

    /**
     * Réinitialise le mot de passe avec un token
     */
    public void resetPassword(String token, String newPassword) {
        // Valider le token
        if (!jwtService.validatePasswordResetToken(token)) {
            throw new RuntimeException("Token invalide ou expiré");
        }

        // Extraire l'email du token
        String email = jwtService.extractEmail(token);

        // Trouver l'utilisateur
        Utilisateur utilisateur = userXmlService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Mettre à jour le mot de passe
        utilisateur.setMotDePasse(passwordEncoder.encode(newPassword));
        userXmlService.update(utilisateur);
    }

    /**
     * Crée un utilisateur à partir d'une requête d'enregistrement
     */
    private Utilisateur createUserFromRequest(RegisterRequest request) {
        Utilisateur utilisateur;
        
        switch (request.getRole()) {
            case CITOYEN:
                Citoyen citoyen = new Citoyen();
                citoyen.setAdresse(request.getAdresse());
                citoyen.setTelephone(request.getTelephone());
                utilisateur = citoyen;
                break;
                
            case TECHNICIEN:
                Technicien technicien = new Technicien();
                technicien.setCompetences(new ArrayList<>());
                technicien.setDisponibilite(true);
                utilisateur = technicien;
                break;
                
            case CHEF_SERVICE:
                ChefDeService chef = new ChefDeService();
                chef.setDepartement(request.getDepartement());
                utilisateur = chef;
                break;
                
            case ADMINISTRATEUR:
                utilisateur = new Administrateur();
                break;
                
            case MAIN_DOEUVRE:
                AgentMainDOeuvre agent = new AgentMainDOeuvre();
                agent.setPrenom(request.getPrenom());
                agent.setMatricule(request.getMatricule());
                agent.setCin(request.getCin());
                agent.setTelephone(request.getTelephone());
                agent.setMetier(request.getMetier());
                agent.setMainDOeuvreId(0); // Temporaire, sera mis à jour après création MainDOeuvre
                utilisateur = agent;
                break;
                
            default:
                throw new IllegalArgumentException("Rôle non supporté: " + request.getRole());
        }

        // Champs communs
        utilisateur.setId(userXmlService.generateNewId());
        utilisateur.setNom(request.getNom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setRole(request.getRole());

        return utilisateur;
    }

    /**
     * Crée une entrée MainDOeuvre pour un AgentMainDOeuvre
     */
    private void createMainDOeuvreEntry(Utilisateur user, RegisterRequest request) {
    if (user instanceof AgentMainDOeuvre) {
        MainDOeuvre mainDOeuvre = new MainDOeuvre();
        mainDOeuvre.setId(mainDOeuvreService.generateNewId());
        mainDOeuvre.setNom(request.getNom());
        mainDOeuvre.setPrenom(request.getPrenom());
        mainDOeuvre.setEmail(request.getEmail());
        mainDOeuvre.setMatricule(request.getMatricule());
        mainDOeuvre.setCin(request.getCin());
        mainDOeuvre.setTelephone(request.getTelephone());
        mainDOeuvre.setMetier(request.getMetier());
        mainDOeuvre.setDisponibilite("LIBRE");
        mainDOeuvre.setActive(true);
        mainDOeuvre.setCompetences(new ArrayList<>());
        mainDOeuvre.setHabilitations(new ArrayList<>());
        // Note: setHistoriqueInterventions n'existe pas dans MainDOeuvre, on l'ignore

        try {
            MainDOeuvre savedMainDOeuvre = mainDOeuvreService.save(mainDOeuvre);
            
            // Mettre à jour l'AgentMainDOeuvre avec le bon ID
            AgentMainDOeuvre agent = (AgentMainDOeuvre) user;
            agent.setMainDOeuvreId(savedMainDOeuvre.getId());
            userXmlService.update(agent);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de MainDOeuvre: " + e.getMessage());
            // Ne pas propager l'exception pour ne pas bloquer l'inscription
        }
    }
}

    /**
     * Vérifie si un token JWT est valide
     */
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    /**
     * Récupère les informations d'un utilisateur à partir d'un token
     */
    public Utilisateur getUserFromToken(String token) {
        if (!jwtService.validateToken(token)) {
            throw new RuntimeException("Token invalide");
        }

        String email = jwtService.extractEmail(token);
        return userXmlService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }
    @PostConstruct
public void createTestUser() {
    try {
        // Vérifier si un admin existe déjà
        if (userXmlService.findByEmail("admin@ville.com").isEmpty()) {
            System.out.println("👤 Création de l'utilisateur admin de test...");
            
            RegisterRequest testUser = new RegisterRequest();
            testUser.setNom("Administrateur");
            testUser.setEmail("admin@ville.com");
            testUser.setMotDePasse("admin123");
            testUser.setRole(RoleType.ADMINISTRATEUR);
            
            this.register(testUser);
            System.out.println("✅ Utilisateur admin créé avec succès");
        }
    } catch (Exception e) {
        System.err.println("❌ Erreur création utilisateur test: " + e.getMessage());
    }
}
@PostConstruct
public void init() {
    System.out.println("🚀 Initialisation AuthService...");
    userXmlService.testUserParsing(); // Appeler le test
}
}