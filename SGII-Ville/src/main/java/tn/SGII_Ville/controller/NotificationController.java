package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.Notification;
import tn.SGII_Ville.service.NotificationService;
import tn.SGII_Ville.service.NotificationXmlService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationXmlService notificationXmlService;

    // ==================== ENDPOINTS NOTIFICATIONSERVICE ====================

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

    /**
     * Crée une notification pour une demande (utilisée par les autres services)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createNotification(@RequestBody Map<String, Object> request) {
        try {
            int userId = (Integer) request.get("userId");
            String message = (String) request.get("message");
            
            boolean success = notificationService.creerNotificationPourDemande(userId, message);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification créée avec succès"
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
     * Test de création de notification (utilisé par le frontend pour tester)
     */
    @PostMapping("/test")
    public ResponseEntity<?> testNotification(@RequestBody Map<String, Object> request) {
        try {
            int userId = (Integer) request.get("userId");
            String message = (String) request.get("message");
            
            // Message de test formaté
            String testMessage = "🧪 TEST: " + message + " - " + LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            boolean success = notificationService.creerNotificationPourDemande(userId, testMessage);
            
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
     * Notifie le technicien d'un changement d'état de tâche par la main-d'œuvre
     */
    @PostMapping("/notifier-technicien-tache")
    public ResponseEntity<?> notifierTechnicienTache(@RequestBody Map<String, Object> request) {
        try {
            int technicienId = (Integer) request.get("technicienId");
            int tacheId = (Integer) request.get("tacheId");
            String libelleTache = (String) request.get("libelleTache");
            String mainDOeuvreNom = (String) request.get("mainDOeuvreNom");
            String ancienEtat = (String) request.get("ancienEtat");
            String nouvelEtat = (String) request.get("nouvelEtat");
            String details = (String) request.get("details");
            
            notificationService.notifierTechnicienChangementEtatTache(
                technicienId, tacheId, libelleTache, mainDOeuvreNom, 
                ancienEtat, nouvelEtat, details
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification envoyée au technicien",
                "technicienId", technicienId,
                "tacheId", tacheId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Erreur envoi notification: " + e.getMessage()));
        }
    }

    /**
     * Test de notification technicien pour tâche
     */
    @PostMapping("/test-notif-technicien-tache")
    public ResponseEntity<?> testNotifTechnicienTache(@RequestBody Map<String, Object> request) {
        try {
            int technicienId = (Integer) request.get("technicienId");
            int tacheId = request.containsKey("tacheId") ? (Integer) request.get("tacheId") : 999;
            String libelleTache = request.containsKey("libelleTache") ? (String) request.get("libelleTache") : "Tâche de test";
            String mainDOeuvreNom = request.containsKey("mainDOeuvreNom") ? (String) request.get("mainDOeuvreNom") : "Test MDO";
            String ancienEtat = request.containsKey("ancienEtat") ? (String) request.get("ancienEtat") : "A_FAIRE";
            String nouvelEtat = request.containsKey("nouvelEtat") ? (String) request.get("nouvelEtat") : "EN_COURS";
            String details = request.containsKey("details") ? (String) request.get("details") : "Ceci est un test de notification";
            
            notificationService.notifierTechnicienChangementEtatTache(
                technicienId, tacheId, libelleTache, mainDOeuvreNom,
                ancienEtat, nouvelEtat, details
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification test envoyée au technicien",
                "technicienId", technicienId,
                "tacheId", tacheId,
                "timestamp", LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/notifier-chef-intervention-terminee")
    public ResponseEntity<?> notifierChefInterventionTerminee(@RequestBody Map<String, Object> request) {
        try {
            int chefId = (Integer) request.get("chefId");
            int interventionId = (Integer) request.get("interventionId");
            String message = (String) request.get("message");
            
            Notification notification = new Notification();
            notification.setMessage("🏁 INTERVENTION TERMINÉE #" + interventionId + "\n" + message);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUserId(chefId);
            notification.setReadable(false);
            
            notificationXmlService.save(notification);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Chef notifié avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Erreur notification chef: " + e.getMessage()));
        }
    }

    @PostMapping("/notifier-technicien-verification")
    public ResponseEntity<?> notifierTechnicienVerification(@RequestBody Map<String, Object> request) {
        try {
            int technicienId = (Integer) request.get("technicienId");
            int interventionId = (Integer) request.get("interventionId");
            String message = (String) request.get("message");
            
            Notification notification = new Notification();
            notification.setMessage("🔍 VÉRIFICATION REQUISE - Intervention #" + interventionId + "\n" + message);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUserId(technicienId);
            notification.setReadable(false);
            
            notificationXmlService.save(notification);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Technicien notifié pour vérification"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Erreur notification technicien: " + e.getMessage()));
        }
    }
}