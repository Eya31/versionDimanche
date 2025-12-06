package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SGII_Ville.entities.Notification;
import tn.SGII_Ville.entities.Utilisateur;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import tn.SGII_Ville.entities.AgentMainDOeuvre;

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

    /**
     * Notifie un chef de service spécifique
     */
    public void notifierChefService(Integer chefServiceId, String message) {
        if (chefServiceId == null) {
            return;
        }
        
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUserId(chefServiceId);
        notification.setReadable(false);
        
        notificationXmlService.save(notification);
    }

    /**
     * Notifie un technicien qu'une intervention lui a été assignée
     */
    public void notifierTechnicienIntervention(int technicienId, int interventionId, int demandeId) {
        Notification notification = new Notification();
        notification.setMessage("🔧 Nouvelle intervention #" + interventionId + " assignée pour la demande #" + demandeId + ". Veuillez consulter vos interventions.");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUserId(technicienId);
        notification.setReadable(false);
        
        notificationXmlService.save(notification);
    }

    /**
     * Notifie un technicien (méthode générique)
     */
    public void notifierTechnicien(int technicienId, String message) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUserId(technicienId);
        notification.setReadable(false);
        
        notificationXmlService.save(notification);
    }

    /**
     * Notifie une main-d'œuvre
     */
    public void notifierMainDOeuvre(Integer mainDOeuvreId, String message) {
        if (mainDOeuvreId == null) {
            System.out.println("⚠️ [NOTIFICATION] mainDOeuvreId est null - notification annulée");
            return;
        }
        
        System.out.println("📢 [NOTIFICATION] Notifier main-d'œuvre ID: " + mainDOeuvreId);
        System.out.println("📝 [NOTIFICATION] Message: " + message);
        
        // Trouver l'utilisateur AgentMainDOeuvre correspondant
        Optional<Utilisateur> userOpt = utilisateurService.findAll().stream()
            .filter(u -> u instanceof AgentMainDOeuvre)
            .filter(u -> {
                AgentMainDOeuvre agent = (AgentMainDOeuvre) u;
                return agent.getMainDOeuvreId() == mainDOeuvreId;
            })
            .findFirst();
        
        if (userOpt.isPresent()) {
            Utilisateur user = userOpt.get();
            System.out.println("✅ [NOTIFICATION] Utilisateur AgentMainDOeuvre trouvé - ID: " + user.getId() + ", Email: " + user.getEmail());
            
            Notification notification = new Notification();
            notification.setMessage(message);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUserId(user.getId());
            notification.setReadable(false);
            
            try {
                Notification saved = notificationXmlService.save(notification);
                System.out.println("✅ [NOTIFICATION] Notification créée avec succès - ID: " + saved.getIdNotification() + " pour userId: " + saved.getUserId());
            } catch (Exception e) {
                System.err.println("❌ [NOTIFICATION] Erreur lors de la sauvegarde: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("❌ [NOTIFICATION] Aucun utilisateur AgentMainDOeuvre trouvé pour mainDOeuvreId: " + mainDOeuvreId);
        }
    }

    /**
     * Notifie un citoyen
     */
    public void notifierCitoyen(Integer citoyenId, String message) {
        if (citoyenId == null) {
            return;
        }
        
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUserId(citoyenId);
        notification.setReadable(false);
        
        notificationXmlService.save(notification);
    }
    //************************************************************************************** */
    /**
 * Notifie le technicien quand la main-d'œuvre commence une tâche
 */
public void notifierTechnicienDebutTache(int technicienId, int tacheId, String libelleTache, String mainDOeuvreNom) {
    Notification notification = new Notification();
    notification.setMessage("🛠️ La main-d'œuvre " + mainDOeuvreNom + " a COMMENCÉ la tâche #" + tacheId + " : " + libelleTache);
    notification.setCreatedAt(LocalDateTime.now());
    notification.setUserId(technicienId);
    notification.setReadable(false);
    
    notificationXmlService.save(notification);
    System.out.println("📢 Notification envoyée au technicien #" + technicienId + " : début tâche #" + tacheId);
}

/**
 * Notifie le technicien quand la main-d'œuvre termine une tâche
 */
public void notifierTechnicienTacheTerminee(int technicienId, int tacheId, String libelleTache, String mainDOeuvreNom, String commentaire) {
    String message = "✅ La main-d'œuvre " + mainDOeuvreNom + " a TERMINÉ la tâche #" + tacheId + " : " + libelleTache;
    if (commentaire != null && !commentaire.trim().isEmpty()) {
        message += " - Commentaire : " + commentaire;
    }
    
    Notification notification = new Notification();
    notification.setMessage(message);
    notification.setCreatedAt(LocalDateTime.now());
    notification.setUserId(technicienId);
    notification.setReadable(false);
    
    notificationXmlService.save(notification);
    System.out.println("📢 Notification envoyée au technicien #" + technicienId + " : tâche terminée #" + tacheId);
}

/**
 * Notifie le technicien quand la main-d'œuvre suspend une tâche
 */
public void notifierTechnicienTacheSuspendue(int technicienId, int tacheId, String libelleTache, String mainDOeuvreNom, String raison) {
    String message = "⏸️ La main-d'œuvre " + mainDOeuvreNom + " a SUSPENDU la tâche #" + tacheId + " : " + libelleTache;
    if (raison != null && !raison.trim().isEmpty()) {
        message += " - Raison : " + raison;
    }
    
    Notification notification = new Notification();
    notification.setMessage(message);
    notification.setCreatedAt(LocalDateTime.now());
    notification.setUserId(technicienId);
    notification.setReadable(false);
    
    notificationXmlService.save(notification);
    System.out.println("📢 Notification envoyée au technicien #" + technicienId + " : tâche suspendue #" + tacheId);
}

/**
 * Notifie le technicien quand la main-d'œuvre reprend une tâche suspendue
 */
public void notifierTechnicienTacheReprise(int technicienId, int tacheId, String libelleTache, String mainDOeuvreNom) {
    Notification notification = new Notification();
    notification.setMessage("🔁 La main-d'œuvre " + mainDOeuvreNom + " a REPRIS la tâche #" + tacheId + " : " + libelleTache);
    notification.setCreatedAt(LocalDateTime.now());
    notification.setUserId(technicienId);
    notification.setReadable(false);
    
    notificationXmlService.save(notification);
    System.out.println("📢 Notification envoyée au technicien #" + technicienId + " : tâche reprise #" + tacheId);
}

/**
 * Notifie le technicien quand la main-d'œuvre ajoute un commentaire à une tâche
 */
public void notifierTechnicienCommentaireTache(int technicienId, int tacheId, String libelleTache, String mainDOeuvreNom, String commentaire) {
    Notification notification = new Notification();
    notification.setMessage("💬 La main-d'œuvre " + mainDOeuvreNom + " a ajouté un commentaire sur la tâche #" + tacheId + " : " + libelleTache + " - \"" + commentaire + "\"");
    notification.setCreatedAt(LocalDateTime.now());
    notification.setUserId(technicienId);
    notification.setReadable(false);
    
    notificationXmlService.save(notification);
    System.out.println("📢 Notification envoyée au technicien #" + technicienId + " : commentaire sur tâche #" + tacheId);
}

/**
 * Notifie le technicien pour tout changement d'état d'une tâche (méthode générique)
 */
public void notifierTechnicienChangementEtatTache(int technicienId, int tacheId, String libelleTache, String mainDOeuvreNom, String ancienEtat, String nouvelEtat, String details) {
    String emoji = "";
    switch (nouvelEtat) {
        case "EN_COURS": emoji = "🛠️"; break;
        case "TERMINEE": emoji = "✅"; break;
        case "SUSPENDUE": emoji = "⏸️"; break;
        case "REPORTEE": emoji = "📅"; break;
        default: emoji = "📝";
    }
    
    String message = emoji + " La main-d'œuvre " + mainDOeuvreNom + " a changé l'état de la tâche #" + tacheId + " : " + libelleTache;
    message += "\nÉtat : " + ancienEtat + " → " + nouvelEtat;
    if (details != null && !details.trim().isEmpty()) {
        message += "\n" + details;
    }
    
    Notification notification = new Notification();
    notification.setMessage(message);
    notification.setCreatedAt(LocalDateTime.now());
    notification.setUserId(technicienId);
    notification.setReadable(false);
    
    notificationXmlService.save(notification);
    System.out.println("📢 Notification envoyée au technicien #" + technicienId + " : changement état tâche #" + tacheId + " (" + ancienEtat + " → " + nouvelEtat + ")");
}
}
