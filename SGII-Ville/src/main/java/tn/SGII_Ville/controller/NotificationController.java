package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.Notification;
import tn.SGII_Ville.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Récupère les notifications d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable int userId) {
        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Compte les notifications non lues d'un utilisateur
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable int userId) {
        long count = notificationService.compterNonLues(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Marque une notification comme lue
     */
    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<Void> markAsRead(@PathVariable int notificationId) {
        notificationService.marquerCommeLue(notificationId);
        return ResponseEntity.ok().build();
    }
}
