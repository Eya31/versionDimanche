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
import java.util.Optional; // ✅ Ajouter cet import

@Service
public class DemandeAjoutNotificationService {

    private NotificationService notificationService; // Changé de NotifService à NotificationService

    
    @Autowired
    private UserXmlService userXmlService;
    @Autowired
    private StockRessourceService stockRessourceService;

    /**
     * Notifie tous les administrateurs d'une nouvelle demande d'ajout
     */
public void notifierNouvelleDemandeAjout(DemandeAjout demande) {
        try {
            // Trouver tous les administrateurs
            List<Utilisateur> admins = trouverAdministrateurs();
            
            // Message pour l'administrateur
            String messageAdmin = construireMessageNouvelleDemande(demande);
            
            for (Utilisateur admin : admins) {
                // Utiliser NotificationService pour notifier chaque admin
                notificationService.creerNotificationPourDemande(admin.getId(), messageAdmin);
                
                System.out.println("📨 Notification envoyée à l'admin #" + admin.getId() + 
                                 " pour nouvelle demande #" + demande.getId());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    
        /**
     * Notifie le chef de la réponse à sa demande
     */
    public void notifierReponseDemandeAjout(DemandeAjout demande, boolean acceptee, String motifRefus) {
        try {
            // Construire le message pour le chef
            String messageChef = construireMessageReponse(demande, acceptee, motifRefus);
            
            // Utiliser NotificationService pour notifier le chef
            notificationService.creerNotificationPourDemande(demande.getChefId(), messageChef);
            
            System.out.println("📨 Notification envoyée au chef #" + demande.getChefId() + 
                             " pour réponse demande #" + demande.getId());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la notification de réponse: " + e.getMessage());
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
        return utilisateur.getRole() == RoleType.ADMINISTRATEUR;
    }

    /**
     * Construit le message pour une nouvelle demande (pour les admins)
     */
    private String construireMessageNouvelleDemande(DemandeAjout demande) {
        String typeDemande = demande.getTypeDemande() == TypeDemandeAjout.EQUIPEMENT ? 
            "d'équipement" : "de matériel";
            
        return String.format(
            "📦 NOUVELLE DEMANDE %s #%d%n" +
            "👤 Chef: #%d%n" +
            "🛒 Désignation: %s%n" +
            "📊 Quantité: %d unités%n" +
            "💰 Budget: %.2f DT%n" +
            "📝 Justification: %s",
            typeDemande,
            demande.getId(),
            demande.getChefId(),
            demande.getDesignation(),
            demande.getQuantite(),
            demande.getBudget(),
            demande.getJustification()
        );
    }

    /**
     * Construit le message de réponse à une demande (pour le chef)
     */
    private String construireMessageReponse(DemandeAjout demande, boolean acceptee, String motifRefus) {
        String typeDemande = demande.getTypeDemande() == TypeDemandeAjout.EQUIPEMENT ? 
            "d'équipement" : "de ressource matérielle";
        
        if (acceptee) {
            // Pour les ressources, ajouter des infos sur le stock
            String infoStock = "";
            if (demande.getTypeDemande() == TypeDemandeAjout.RESSOURCE) {
                try {
                    int stockActuel = stockRessourceService.getQuantiteStock(demande.getDesignation());
                    infoStock = String.format(
                        "%n📦 Stock après ajout: %d unités",
                        stockActuel
                    );
                } catch (Exception e) {
                    infoStock = "%n📦 Stock: mise à jour effectuée";
                }
            }
            
            return String.format(
                "✅ Votre demande %s a été ACCEPTÉE !%n" +
                "📋 Détails:%n" +
                "   • Référence: #%d%n" +
                "   • Désignation: %s%n" +
                "   • Quantité: %d unités%n" +
                "   • Budget: %.2f DT%n" +
                "   • Traitée le: %s%s%n" +
                "🎉 Les ressources ont été ajoutées au stock avec succès.",
                typeDemande,
                demande.getId(),
                demande.getDesignation(),
                demande.getQuantite(),
                demande.getBudget(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                infoStock
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
        try {
            String message = "🧪 TEST: " + testMessage + " - " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            // Utiliser NotificationService pour le test
            notificationService.creerNotificationPourDemande(userId, message);
            
            System.out.println("✅ Notification test envoyée à l'user #" + userId);
        } catch (Exception e) {
            System.err.println("❌ Échec notification test: " + e.getMessage());
        }
    }
    
}