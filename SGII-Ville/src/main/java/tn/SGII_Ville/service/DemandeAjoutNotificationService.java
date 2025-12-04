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

    @Autowired
    private NotifService notifService;
    
    @Autowired
    private UserXmlService userXmlService;
    @Autowired
    private StockRessourceService stockRessourceService;

    /**
     * Notifie tous les administrateurs d'une nouvelle demande d'ajout
     */
public void notifierReponseDemandeAjout(DemandeAjout demande, boolean acceptee, String motifRefus) {
        try {
            // Notifier le chef de la réponse
            String message = construireMessageReponse(demande, acceptee, motifRefus);
            
            // ✅ Appel correct avec 2 paramètres seulement
            notifService.creerNotificationPourDemande(demande.getChefId(), message);
            
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
        System.out.println("🧪 TEST Notification pour user #" + userId);
        
        String message = "🧪 TEST: " + testMessage + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
        // ✅ Appel correct avec 2 paramètres seulement
        boolean success = notifService.creerNotificationPourDemande(userId, message);
        
        if (success) {
            System.out.println("✅ Notification test envoyée à l'user #" + userId);
        } else {
            System.err.println("❌ Échec notification test pour user #" + userId);
        }
    }
    
    public void notifierNouvelleDemandeAjout(DemandeAjout demande) {
        try {
            // Récupérer le chef qui a fait la demande
            Optional<Utilisateur> chefOpt = userXmlService.findById(demande.getChefId());
            if (chefOpt.isEmpty()) {
                System.err.println("❌ Chef non trouvé pour notification: " + demande.getChefId());
                return;
            }
            
            Utilisateur chef = chefOpt.get();
            String nomChef = chef.getNom();
            String departement = "Département"; // À remplacer par la méthode correcte si elle existe
            
            // Trouver tous les administrateurs
            List<Utilisateur> admins = userXmlService.findAll().stream()
                .filter(this::estAdministrateur)
                .collect(Collectors.toList());
            
            for (Utilisateur admin : admins) {
                String message = String.format(
                    "📋 Nouvelle demande de %s%n" +
                    "👤 Chef: %s%n" +
                    "🏢 Département: %s%n" +
                    "📦 Type: %s%n" +
                    "🛒 Désignation: %s%n" +
                    "📊 Quantité: %d%n" +
                    "💰 Budget: %.2f DT",
                    nomChef,
                    nomChef,
                    departement,
                    demande.getTypeDemande().toString(),
                    demande.getDesignation(),
                    demande.getQuantite(),
                    demande.getBudget()
                );
                
                // ✅ Appel correct avec 2 paramètres seulement
                notifService.creerNotificationPourDemande(admin.getId(), message);
                
                System.out.println("📨 Notification envoyée à l'admin #" + admin.getId() + 
                                 " pour demande #" + demande.getId());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}