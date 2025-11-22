package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SGII_Ville.entities.Notification;
import tn.SGII_Ville.entities.Utilisateur;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationXmlService notificationXmlService;
    
    @Autowired
    private UserXmlService utilisateurService;

    /**
     * Crée une notification pour une nouvelle demande (destinée au chef de service)
     */
    public void notifierNouvelleDemande(int demandeId, String description) {
        // Trouver tous les chefs de service
        List<Utilisateur> chefs = utilisateurService.findAll().stream()
            .filter(u -> "CHEF_SERVICE".equals(u.getRole()))
            .collect(Collectors.toList());
        
        for (Utilisateur chef : chefs) {
            Notification notification = new Notification();
            notification.setMessage("🆕 Nouvelle demande #" + demandeId + " : " + description);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUserId(chef.getId());
            notification.setReadable(false);
            
            notificationXmlService.save(notification);
        }
    }

    /**
     * Crée une notification pour une nouvelle intervention (destinée à l'admin)
     */
    public void notifierNouvelleIntervention(int interventionId, int demandeId) {
        System.out.println("🔔 notifierNouvelleIntervention appelée - Intervention #" + interventionId + " Demande #" + demandeId);
        
        // Trouver tous les admins (ADMIN ou ADMINISTRATEUR)
        List<Utilisateur> admins = utilisateurService.findAll().stream()
            .filter(u -> "ADMIN".equals(u.getRole()) || "ADMINISTRATEUR".equals(u.getRole()))
            .collect(Collectors.toList());
        
        System.out.println("📋 Nombre d'admins trouvés: " + admins.size());
        
        for (Utilisateur admin : admins) {
            System.out.println("👤 Création notification pour admin ID: " + admin.getId() + " (" + admin.getNom() + ")");
            Notification notification = new Notification();
            notification.setMessage("🔧 Nouvelle intervention #" + interventionId + " planifiée pour la demande #" + demandeId);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUserId(admin.getId());
            notification.setReadable(false);
            
            Notification saved = notificationXmlService.save(notification);
            System.out.println("✅ Notification sauvegardée avec ID: " + saved.getIdNotification());
        }
    }

    /**
     * Crée une notification pour le citoyen quand son intervention est lancée
     */
    public void notifierCitoyenInterventionLancee(int citoyenId, int demandeId, int interventionId) {
        System.out.println("🔔 notifierCitoyenInterventionLancee appelée - Citoyen #" + citoyenId + " Demande #" + demandeId + " Intervention #" + interventionId);
        
        Notification notification = new Notification();
        notification.setMessage("✅ Votre demande #" + demandeId + " a été acceptée ! Intervention #" + interventionId + " en cours.");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUserId(citoyenId);
        notification.setReadable(false);
        
        Notification saved = notificationXmlService.save(notification);
        System.out.println("✅ Notification citoyen sauvegardée avec ID: " + saved.getIdNotification());
    }

    /**
     * Récupère toutes les notifications d'un utilisateur
     */
    public List<Notification> getNotificationsByUser(int userId) {
        return notificationXmlService.getNotificationsByUserId(userId);
    }

    /**
     * Marque une notification comme lue
     */
    public void marquerCommeLue(int notificationId) {
        Notification notification = notificationXmlService.findById(notificationId);
        if (notification != null) {
            notification.setReadable(true);
            notificationXmlService.update(notification);
        }
    }

    /**
     * Compte les notifications non lues d'un utilisateur
     */
    public long compterNonLues(int userId) {
        return notificationXmlService.getNotificationsByUserId(userId).stream()
            .filter(n -> !n.isReadable())
            .count();
    }
}
