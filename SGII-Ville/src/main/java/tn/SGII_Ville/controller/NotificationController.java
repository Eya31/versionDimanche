package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.Notification;
import tn.SGII_Ville.service.NotificationService;
import tn.SGII_Ville.service.NotifService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotifService notifService;

    // ==================== ENDPOINTS NOTIFICATIONSERVICE ====================

    /**
     * Récupère les notifications d'un utilisateur (NotificationService)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable int userId) {
        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Compte les notifications non lues d'un utilisateur (NotificationService)
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable int userId) {
        long count = notificationService.compterNonLues(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Marque une notification comme lue (NotificationService)
     */
    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<Void> markAsRead(@PathVariable int notificationId) {
        notificationService.marquerCommeLue(notificationId);
        return ResponseEntity.ok().build();
    }

    // ==================== ENDPOINTS NOTIFSERVICE ====================

    /**
     * Crée une notification pour une demande
     */
    @PostMapping("/create-for-demand")
    public ResponseEntity<?> createNotificationForDemand(@RequestBody Map<String, Object> request) {
        try {
            int userId = (Integer) request.get("userId");
            String message = (String) request.get("message");
            
            boolean success = notifService.creerNotificationPourDemande(userId, message);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "Notification créée avec succès",
                    "userId", userId
                ));
            } else {
                return ResponseEntity.status(500)
                    .body(Map.of("error", "Échec création notification"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère les notifications d'un utilisateur (NotifService)
     */
    @GetMapping("/notif/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUserNotif(@PathVariable int userId) {
        try {
            List<Notification> notifications = notifService.getNotificationsByUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte les notifications non lues d'un utilisateur (NotifService)
     */
    @GetMapping("/notif/user/{userId}/unread-count")
    public ResponseEntity<Integer> getUnreadCountNotif(@PathVariable int userId) {
        try {
            int count = notifService.getUnreadCountByUser(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marque une notification comme lue (NotifService)
     */
    @PutMapping("/notif/{notificationId}/read")
    public ResponseEntity<?> markAsReadNotif(@PathVariable int notificationId) {
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

    // ==================== ENDPOINTS COMBINÉS / UTILITAIRES ====================

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
                "unreadCountNotifService", unreadCountV1,
                "unreadCountNotificationService", unreadCountV2,
                "totalNotificationsNotifService", notificationsV1.size(),
                "totalNotificationsNotificationService", notificationsV2.size(),
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
                    "message", "Notification traitée (NotificationService seulement)",
                    "notificationId", notificationId
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors du marquage: " + e.getMessage()));
        }
    }

    /**
     * Test de création de notification
     */
    @PostMapping("/test")
    public ResponseEntity<?> testNotification(@RequestBody Map<String, Object> request) {
        try {
            int userId = (Integer) request.get("userId");
            String message = (String) request.get("message");
            
            // Message de test formaté
            String testMessage = "🧪 TEST: " + message + " - " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            boolean success = notifService.creerNotificationPourDemande(userId, testMessage);
            
            return ResponseEntity.ok(Map.of(
                "success", success,
                "userId", userId,
                "message", testMessage
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
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
