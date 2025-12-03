package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service d'envoi d'emails
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Envoie un email de bienvenue avec les informations de connexion
     */
    public void sendWelcomeEmail(String toEmail, String userName, String password, String role) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎉 Bienvenue sur SGII-Ville - Vos identifiants de connexion");
            
            String htmlContent = buildWelcomeEmailTemplate(userName, toEmail, password, role);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email de bienvenue: " + e.getMessage());
            // Fallback vers email simple
            sendSimpleWelcomeEmail(toEmail, userName);
        }
    }

    /**
     * Email de bienvenue simple en cas d'erreur
     */
    private void sendSimpleWelcomeEmail(String toEmail, String userName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Bienvenue sur SGII-Ville");
        message.setText(String.format(
            "Bonjour %s,\n\n" +
            "Bienvenue sur la plateforme SGII-Ville.\n\n" +
            "Votre compte a été créé avec succès.\n\n" +
            "Cordialement,\n" +
            "L'équipe SGII-Ville",
            userName
        ));
        mailSender.send(message);
    }

    /**
     * Construit le template HTML pour l'email de bienvenue
     */
    private String buildWelcomeEmailTemplate(String userName, String email, String password, String role) {
        String motivationalMessage = getMotivationalMessage(role);
        String roleDisplay = getRoleDisplay(role);
        
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Bienvenue sur SGII-Ville</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 20px;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.3);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px; text-align: center;">
                                        <h1 style="color: white; margin: 0; font-size: 32px; font-weight: bold;">
                                            🎉 Bienvenue sur SGII-Ville
                                        </h1>
                                        <p style="color: rgba(255,255,255,0.9); margin: 10px 0 0 0; font-size: 16px;">
                                            Système de Gestion Intelligente des Interventions
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px;">
                                        <h2 style="color: #333; margin: 0 0 20px 0; font-size: 24px;">
                                            Bonjour %s ! 👋
                                        </h2>
                                        
                                        <p style="color: #666; line-height: 1.6; font-size: 16px; margin: 0 0 25px 0;">
                                            Votre compte a été créé avec succès en tant que <strong style="color: #667eea;">%s</strong>. 
                                            Nous sommes ravis de vous accueillir dans notre équipe !
                                        </p>
                                        
                                        <!-- Credentials Box -->
                                        <div style="background: linear-gradient(135deg, #f8f9ff 0%%, #e8ebff 100%%); border-left: 4px solid #667eea; padding: 25px; border-radius: 10px; margin: 25px 0;">
                                            <h3 style="color: #333; margin: 0 0 20px 0; font-size: 18px;">
                                                🔐 Vos identifiants de connexion
                                            </h3>
                                            
                                            <table width="100%%" style="margin: 0;">
                                                <tr>
                                                    <td style="padding: 12px 0; color: #666; font-weight: 600;">
                                                        📧 Email :
                                                    </td>
                                                    <td style="padding: 12px 0; color: #333; font-family: monospace; font-size: 15px;">
                                                        %s
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 0; color: #666; font-weight: 600;">
                                                        🔑 Mot de passe :
                                                    </td>
                                                    <td style="padding: 12px 0; color: #333; font-family: monospace; font-size: 15px; background: #fff; padding-left: 10px; border-radius: 5px;">
                                                        %s
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <p style="color: #e74c3c; margin: 15px 0 0 0; font-size: 13px; font-style: italic;">
                                                ⚠️ Conservez ces informations en lieu sûr et changez votre mot de passe après votre première connexion.
                                            </p>
                                        </div>
                                        
                                        <!-- Motivational Message -->
                                        <div style="background: linear-gradient(135deg, #ffeaa7 0%%, #fdcb6e 100%%); padding: 20px; border-radius: 10px; margin: 25px 0;">
                                            <p style="color: #2d3436; margin: 0; font-size: 15px; line-height: 1.6; font-style: italic;">
                                                💡 <strong>%s</strong>
                                            </p>
                                        </div>
                                        
                                        <!-- CTA Button -->
                                        <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 30px 0;">
                                            <tr>
                                                <td align="center">
                                                    <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; padding: 15px 40px; border-radius: 30px; font-weight: bold; font-size: 16px; box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);">
                                                        🚀 Accéder à la plateforme
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <!-- Support Info -->
                                        <div style="border-top: 2px solid #f0f0f0; padding-top: 20px; margin-top: 30px;">
                                            <p style="color: #999; font-size: 13px; line-height: 1.6; margin: 0;">
                                                <strong>Besoin d'aide ?</strong><br>
                                                Notre équipe est là pour vous ! Contactez-nous à <a href="mailto:support@sgii-ville.com" style="color: #667eea; text-decoration: none;">support@sgii-ville.com</a>
                                            </p>
                                        </div>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background: #f8f9fa; padding: 25px; text-align: center; border-top: 1px solid #e0e0e0;">
                                        <p style="color: #999; margin: 0; font-size: 13px; line-height: 1.6;">
                                            © 2025 SGII-Ville. Tous droits réservés.<br>
                                            Système de Gestion Intelligente des Interventions Urbaines
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(userName, roleDisplay, email, password, motivationalMessage, baseUrl);
    }

    /**
     * Retourne un message motivant selon le rôle
     */
    private String getMotivationalMessage(String role) {
        return switch (role.toUpperCase()) {
            case "CITOYEN" -> 
                "Ensemble, construisons une ville plus belle et plus fonctionnelle ! Votre voix compte, chaque signalement aide à améliorer notre cadre de vie commun. Merci de votre engagement citoyen ! 🏙️";
            case "TECHNICIEN" -> 
                "Vous êtes les héros du terrain ! Chaque intervention que vous réalisez améliore concrètement la vie de nos concitoyens. Votre expertise et votre dévouement font la différence. Bon courage ! 🔧";
            case "CHEF_SERVICE", "CHEF" -> 
                "L'excellence opérationnelle commence par une bonne coordination. Votre leadership et votre organisation sont essentiels pour garantir la qualité de nos services. Bonne gestion ! 📋";
            case "ADMINISTRATEUR", "ADMIN" -> 
                "Vous avez les clés du système ! Votre rôle est crucial pour maintenir la plateforme performante et sécurisée. Merci de veiller sur notre infrastructure ! ⚙️";
            case "MAIN_DOEUVRE" -> 
                "Votre travail sur le terrain est indispensable ! Chaque tâche accomplie contribue au bon fonctionnement de notre ville. Votre engagement fait la différence ! 💪";
            default -> 
                "Bienvenue dans l'équipe ! Ensemble, nous rendons notre ville meilleure chaque jour. Votre contribution est précieuse ! 🌟";
        };
    }

    /**
     * Retourne l'affichage du rôle
     */
    private String getRoleDisplay(String role) {
        return switch (role.toUpperCase()) {
            case "CITOYEN" -> "Citoyen";
            case "TECHNICIEN" -> "Technicien";
            case "CHEF_SERVICE", "CHEF" -> "Chef de Service";
            case "ADMINISTRATEUR", "ADMIN" -> "Administrateur";
            case "MAIN_DOEUVRE" -> "Agent Main d'Œuvre";
            default -> role;
        };
    }

    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        String resetLink = baseUrl + "/api/auth/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de votre mot de passe - SGII-Ville");
        message.setText(String.format(
            "Bonjour %s,\n\n" +
            "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
            "Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :\n" +
            "%s\n\n" +
            "Ce lien est valide pendant 1 heure.\n\n" +
            "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
            "Cordialement,\n" +
            "L'équipe SGII-Ville",
            userName,
            resetLink
        ));

        mailSender.send(message);
    }

    /**
     * Envoie une notification pour un changement d'état de demande
     */
    public void sendDemandeStatusChangeEmail(String toEmail, String userName, int demandeId, String newStatus) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Mise à jour de votre demande #" + demandeId);
        message.setText(String.format(
            "Bonjour %s,\n\n" +
            "Le statut de votre demande #%d a été mis à jour.\n\n" +
            "Nouveau statut : %s\n\n" +
            "Vous pouvez consulter les détails de votre demande en vous connectant à la plateforme.\n\n" +
            "Cordialement,\n" +
            "L'équipe SGII-Ville",
            userName,
            demandeId,
            newStatus
        ));

        mailSender.send(message);
    }

    /**
     * Envoie une notification au chef de service pour une nouvelle demande
     */
    public void sendNewDemandeNotificationToChef(String toEmail, String chefName, int demandeId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Nouvelle demande citoyenne #" + demandeId);
        message.setText(String.format(
            "Bonjour %s,\n\n" +
            "Une nouvelle demande citoyenne (#%d) a été soumise et nécessite votre attention.\n\n" +
            "Connectez-vous à la plateforme pour consulter les détails et prendre les mesures nécessaires.\n\n" +
            "Cordialement,\n" +
            "SGII-Ville",
            chefName,
            demandeId
        ));

        mailSender.send(message);
    }

    /**
     * Envoie une notification d'affectation d'intervention à un technicien
     */
    public void sendInterventionAssignmentEmail(String toEmail, String technicianName, int interventionId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Nouvelle intervention assignée #" + interventionId);
        message.setText(String.format(
            "Bonjour %s,\n\n" +
            "Une nouvelle intervention (#%d) vous a été assignée.\n\n" +
            "Connectez-vous à la plateforme pour consulter les détails et planifier votre travail.\n\n" +
            "Cordialement,\n" +
            "SGII-Ville",
            technicianName,
            interventionId
        ));

        mailSender.send(message);
    }

    /**
     * Envoie un email générique
     */
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
