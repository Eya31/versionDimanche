package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SGII_Ville.dto.LoginRequest;
import tn.SGII_Ville.dto.LoginResponse;
import tn.SGII_Ville.dto.RegisterRequest;
import tn.SGII_Ville.entities.Administrateur;
import tn.SGII_Ville.entities.ChefDeService;
import tn.SGII_Ville.entities.Citoyen;
import tn.SGII_Ville.entities.Technicien;
import tn.SGII_Ville.entities.Utilisateur;

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

        System.out.println("✅ Utilisateur trouvé: " + utilisateur.getNom() + " (ID: " + utilisateur.getId() + ")");
        System.out.println("🔑 Hash en BD: " + utilisateur.getMotDePasse());
        System.out.println("🔑 Mot de passe fourni: " + request.getMotDePasse());

        // Vérifier le mot de passe
        boolean passwordMatches = userXmlService.checkPassword(request.getMotDePasse(), utilisateur.getMotDePasse());
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
        // Vérifier si l'email existe déjà
        if (userXmlService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Créer l'utilisateur selon son rôle
        Utilisateur utilisateur = createUserFromRequest(request);

        // Sauvegarder l'utilisateur
        utilisateur = userXmlService.save(utilisateur);

        // Envoyer un email de bienvenue
        try {
            emailService.sendWelcomeEmail(utilisateur.getEmail(), utilisateur.getNom());
        } catch (Exception e) {
            // Log l'erreur mais ne pas bloquer l'enregistrement
            System.err.println("Erreur lors de l'envoi de l'email de bienvenue: " + e.getMessage());
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
        utilisateur.setMotDePasse(userXmlService.encodePassword(newPassword));
        userXmlService.update(utilisateur);
    }

    /**
     * Crée un utilisateur à partir d'une requête d'enregistrement
     */
    private Utilisateur createUserFromRequest(RegisterRequest request) {
        return switch (request.getRole()) {
            case CITOYEN -> new Citoyen(
                    0, // L'ID sera généré automatiquement
                    request.getNom(),
                    request.getEmail(),
                    request.getMotDePasse(),
                    request.getAdresse(),
                    request.getTelephone()
            );
            case TECHNICIEN -> new Technicien(
                    0,
                    request.getNom(),
                    request.getEmail(),
                    request.getMotDePasse(),
                    new ArrayList<>(), // Compétences vides au début
                    true // Disponible par défaut
            );
            case CHEF_SERVICE -> new ChefDeService(
                    0,
                    request.getNom(),
                    request.getEmail(),
                    request.getMotDePasse(),
                    request.getDepartement()
            );
            case ADMINISTRATEUR -> new Administrateur(
                    0,
                    request.getNom(),
                    request.getEmail(),
                    request.getMotDePasse()
            );
        };
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
}
