package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SGII_Ville.entities.DemandeAjout;
import tn.SGII_Ville.entities.Utilisateur;
import tn.SGII_Ville.model.enums.RoleType;
import tn.SGII_Ville.model.enums.TypeDemandeAjout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DemandeAjoutNotificationService {

    @Autowired
    private NotifService notifService; // ✅ Utilise NotifService
    
    @Autowired
    private UserXmlService userXmlService;

    /**
     * Notifie tous les administrateurs d'une nouvelle demande d'ajout
     */
    public void notifierNouvelleDemandeAjout(DemandeAjout demande) {
        System.out.println("🔔 [NOTIFICATION] Nouvelle demande #" + demande.getId() + " de chef #" + demande.getChefId());
        
        try {
            // Trouver tous les administrateurs
            List<Utilisateur> admins = trouverAdministrateurs();
            
            if (admins.isEmpty()) {
                System.err.println("❌ AUCUN ADMINISTRATEUR TROUVÉ !");
                return;
            }
            
            String message = construireMessageNouvelleDemande(demande);
            
            for (Utilisateur admin : admins) {
                System.out.println("📨 Envoi notification à admin #" + admin.getId());
                boolean success = notifService.creerNotificationPourDemande(admin.getId(), message);
                
                if (success) {
                    System.out.println("✅ Notification envoyée à l'admin #" + admin.getId());
                } else {
                    System.err.println("❌ Échec envoi notification admin #" + admin.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur notification nouvelle demande: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Notifie le chef de service de la réponse à sa demande
     */
    public void notifierReponseDemandeAjout(DemandeAjout demande, boolean acceptee, String motifRefus) {
    System.out.println("🔔 [NOTIFICATION] Réponse demande #" + demande.getId() + 
                     " pour chef #" + demande.getChefId() + 
                     " - Acceptée: " + acceptee);
    
    try {
        // Vérifier que le chefId est valide
        if (demande.getChefId() <= 0) {
            System.err.println("❌ ChefId invalide: " + demande.getChefId());
            return;
        }
        
        String message = construireMessageReponse(demande, acceptee, motifRefus);
        
        System.out.println("📨 Envoi notification au chef #" + demande.getChefId() + " - Message: " + message);
        
        // CORRECTION : Utiliser demande.getChefId() au lieu d'un ID fixe
        boolean success = notifService.creerNotificationPourDemande(demande.getChefId(), message);
        
        if (success) {
            System.out.println("✅ Notification réponse envoyée au chef #" + demande.getChefId());
        } else {
            System.err.println("❌ Échec envoi notification au chef #" + demande.getChefId());
        }
        
    } catch (Exception e) {
        System.err.println("❌ Erreur notification réponse: " + e.getMessage());
        e.printStackTrace();
    }
}
    /**
     * Trouve tous les administrateurs dans le système
     */
    private List<Utilisateur> trouverAdministrateurs() {
        try {
            List<Utilisateur> allUsers = userXmlService.findAll();
            System.out.println("👥 Utilisateurs totaux: " + allUsers.size());
            
            List<Utilisateur> admins = allUsers.stream()
                .filter(this::estAdministrateur)
                .collect(Collectors.toList());
            
            System.out.println("👥 Administrateurs trouvés: " + admins.size());
            admins.forEach(admin -> 
                System.out.println("   - Admin #" + admin.getId() + ": " + admin.getNom() + " (" + admin.getRole() + ")")
            );
            return admins;
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche administrateurs: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Vérifie si l'utilisateur est un administrateur
     */
    private boolean estAdministrateur(Utilisateur utilisateur) {
        if (utilisateur == null) return false;
        
        RoleType role = utilisateur.getRole();
        boolean isAdmin = role == RoleType.ADMINISTRATEUR;
        
        System.out.println("🔍 Vérification admin - User: " + utilisateur.getId() + 
                         ", Nom: " + utilisateur.getNom() + 
                         ", Role: " + role + 
                         ", EstAdmin: " + isAdmin);
        return isAdmin;
    }

    /**
     * Construit le message pour une nouvelle demande
     */
    private String construireMessageNouvelleDemande(DemandeAjout demande) {
        String typeDemande = demande.getTypeDemande() == TypeDemandeAjout.EQUIPEMENT ? 
            "d'équipement" : "de matériel";
            
        return String.format(
            "📦 Nouvelle demande %s #%d%n" +
            "Désignation: %s%n" +
            "Quantité: %d | Budget: %.2f DT%n" +
            "Justification: %s%n" +
            "Chef de service: #%d",
            typeDemande,
            demande.getId(),
            demande.getDesignation(),
            demande.getQuantite(),
            demande.getBudget(),
            demande.getJustification(),
            demande.getChefId()
        );
    }

    /**
     * Construit le message de réponse à une demande
     */
    private String construireMessageReponse(DemandeAjout demande, boolean acceptee, String motifRefus) {
    String typeDemande = demande.getTypeDemande() == TypeDemandeAjout.EQUIPEMENT ? 
        "d'équipement" : "de matériel";
        
    if (acceptee) {
        return String.format(
            "✅ Votre demande %s a été ACCEPTÉE !%n" +
            "📋 Détails:%n" +
            "   • Référence: #%d%n" +
            "   • Désignation: %s%n" +
            "   • Quantité: %d unités%n" +
            "   • Budget: %.2f DT%n" +
            "   • Traitée le: %s%n" +
            "🎉 Votre demande a été approuvée par l'administration.",
            typeDemande,
            demande.getId(),           // ID de la demande réelle
            demande.getDesignation(),  // Désignation réelle
            demande.getQuantite(),     // Quantité réelle
            demande.getBudget(),       // Budget réel
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
        );
    } else {
        return String.format(
            "❌ Votre demande %s a été REFUSÉE%n" +
            "📋 Détails:%n" +
            "   • Référence: #%d%n" +
            "   • Désignation: %s%n" +
            "   • Motif: %s%n" +
            "   • Traitée le: %s%n" +
            "💡 Vous pouvez soumettre une nouvelle demande avec les corrections nécessaires.",
            typeDemande,
            demande.getId(),
            demande.getDesignation(),
            motifRefus != null ? motifRefus : "Non spécifié par l'administration",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
        );
    }
}
    /**
     * Méthode de test pour vérifier les notifications
     */
    public void testerNotification(int userId, String testMessage) {
        System.out.println("🧪 TEST Notification pour user #" + userId);
        
        String message = "🧪 TEST: " + testMessage + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
        boolean success = notifService.creerNotificationPourDemande(userId, message);
        
        if (success) {
            System.out.println("✅ Notification test envoyée à l'user #" + userId);
        } else {
            System.err.println("❌ Échec notification test pour user #" + userId);
        }
    }
}