package tn.SGII_Ville.controller.ve2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.Notification;
import tn.SGII_Ville.service.NotifService;
import tn.SGII_Ville.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notif")  // ← CHANGER LE BASE PATH POUR ÉVITER LES CONFLITS
@CrossOrigin(origins = "http://localhost:4200")
public class NotifController {

    @Autowired
    private NotifService notifService;

    @Autowired
    private NotificationService notificationService;

    // === MÉTHODES SERVICE NOTIFSERVICE ===
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable int userId) {
        try {
            List<Notification> notifications = notifService.getNotificationsByUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Integer> getUnreadCountByUser(@PathVariable int userId) {
        try {
            int count = notifService.getUnreadCountByUser(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable int notificationId) {
        try {
            boolean success = notifService.markAsRead(notificationId);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Notification marquée comme lue"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // === MÉTHODES SERVICE NOTIFICATIONSERVICE ===
    
    /**
     * Récupère les notifications d'un utilisateur (NotificationService)
     */
    @GetMapping("/ns/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUserNS(@PathVariable int userId) {
        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Compte les notifications non lues d'un utilisateur (NotificationService)
     */
    @GetMapping("/ns/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCountNS(@PathVariable int userId) {
        long count = notificationService.compterNonLues(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Marque une notification comme lue (NotificationService)
     */
    @PutMapping("/ns/{notificationId}/mark-read")
    public ResponseEntity<Void> markAsReadNS(@PathVariable int notificationId) {
        notificationService.marquerCommeLue(notificationId);
        return ResponseEntity.ok().build();
    }

    // === MÉTHODES COMBINÉES ===
    
    /**
     * Endpoint unifié pour les statistiques de notifications
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserNotificationStats(@PathVariable int userId) {
        try {
            int unreadCountV1 = notifService.getUnreadCountByUser(userId);
            long unreadCountV2 = notificationService.compterNonLues(userId);
            List<Notification> notificationsV1 = notifService.getNotificationsByUser(userId);
            List<Notification> notificationsV2 = notificationService.getNotificationsByUser(userId);
            
            Map<String, Object> stats = Map.of(
                "unreadCountV1", unreadCountV1,
                "unreadCountV2", unreadCountV2,
                "totalNotificationsV1", notificationsV1.size(),
                "totalNotificationsV2", notificationsV2.size(),
                "userId", userId
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marquer comme lu avec les deux services
     */
    @PutMapping("/{notificationId}/read-all")
    public ResponseEntity<?> markAsReadAllServices(@PathVariable int notificationId) {
        try {
            boolean successV1 = notifService.markAsRead(notificationId);
            notificationService.marquerCommeLue(notificationId);
            
            if (successV1) {
                return ResponseEntity.ok(Map.of(
                    "message", "Notification marquée comme lue avec les deux services",
                    "notificationId", notificationId
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "message", "Notification traitée (service V2 seulement)",
                    "notificationId", notificationId
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors du marquage: " + e.getMessage()));
        }
    }

    // === MÉTHODES DE TEST ===
    
    /**
     * Test de création de notification
     */
    @PostMapping("/test-create")
    public ResponseEntity<?> testCreateNotification(@RequestBody Map<String, Object> request) {
        try {
            int userId = (Integer) request.get("userId");
            String message = (String) request.get("message");
            
            boolean success = notifService.creerNotificationPourDemande(userId, message);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "Notification test créée",
                    "userId", userId
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "Échec création notification"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère toutes les notifications (pour débogage)
     */
    @GetMapping("/debug/all")
    public ResponseEntity<List<Notification>> getAllNotificationsDebug() {
        try {
            List<Notification> allNotifications = notifService.getAllNotifications();
            return ResponseEntity.ok(allNotifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}