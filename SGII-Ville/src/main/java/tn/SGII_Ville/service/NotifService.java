package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import tn.SGII_Ville.entities.Notification;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotifService {

    private static final String XML_FILE_PATH = "src/main/resources/data/notificationsDemande.xml";
    private static final String NAMESPACE_URI = "http://www.SGII-Ville.tn/notifications";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private XmlService xmlService;

    /**
     * Récupère toutes les notifications
     */
    public List<Notification> getAllNotifications() {
        List<Notification> notifications = new ArrayList<>();
        try {
            Document doc = loadXmlDocument();
            NodeList notificationNodes = doc.getElementsByTagNameNS(NAMESPACE_URI, "Notification");
            
            for (int i = 0; i < notificationNodes.getLength(); i++) {
                Element notificationElement = (Element) notificationNodes.item(i);
                notifications.add(parseNotification(notificationElement));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des notifications: " + e.getMessage());
            e.printStackTrace();
        }
        return notifications;
    }

    /**
     * Récupère les notifications par utilisateur
     */
    public List<Notification> getNotificationsByUser(int userId) {
        return getAllNotifications().stream()
                .filter(notification -> notification.getUserId() == userId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Récupère les notifications non lues par utilisateur
     */
    public List<Notification> getUnreadNotificationsByUser(int userId) {
        return getNotificationsByUser(userId).stream()
                .filter(notification -> !notification.isReadable())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Compte les notifications non lues par utilisateur
     */
    public int getUnreadCountByUser(int userId) {
        return (int) getNotificationsByUser(userId).stream()
                .filter(notification -> !notification.isReadable())
                .count();
    }

    /**
     * Sauvegarde une nouvelle notification
     */
    public Notification save(Notification notification) {
        try {
            Document doc = loadXmlDocument();
            Element root = doc.getDocumentElement();

            // Générer nouvel ID
            int newId = generateNewId(doc);
            notification.setIdNotification(newId);

            // Créer l'élément Notification
            Element notificationElement = doc.createElementNS(NAMESPACE_URI, "Notification");

            addTextElement(doc, notificationElement, "idNotification", String.valueOf(notification.getIdNotification()));
            addTextElement(doc, notificationElement, "message", notification.getMessage());
            addTextElement(doc, notificationElement, "createdAt", 
                notification.getCreatedAt() != null ? notification.getCreatedAt().format(FORMATTER) : LocalDateTime.now().format(FORMATTER));
            addTextElement(doc, notificationElement, "userId", String.valueOf(notification.getUserId()));
            addTextElement(doc, notificationElement, "readable", String.valueOf(notification.isReadable()));

            root.appendChild(notificationElement);
            saveXmlDocument(doc);

            System.out.println("✅ Notification sauvegardée avec ID: " + notification.getIdNotification());
            return notification;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde de la notification: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la sauvegarde de la notification", e);
        }
    }

    /**
     * Marque une notification comme lue
     */
    public boolean markAsRead(int notificationId) {
        try {
            Document doc = loadXmlDocument();
            NodeList notificationNodes = doc.getElementsByTagNameNS(NAMESPACE_URI, "Notification");
            
            for (int i = 0; i < notificationNodes.getLength(); i++) {
                Element notificationElement = (Element) notificationNodes.item(i);
                int id = Integer.parseInt(getElementText(notificationElement, "idNotification"));
                
                if (id == notificationId) {
                    updateElementText(notificationElement, "readable", "true");
                    saveXmlDocument(doc);
                    System.out.println("✅ Notification #" + notificationId + " marquée comme lue");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du marquage de la notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Trouve une notification par son ID
     */
    public Optional<Notification> findById(int notificationId) {
        return getAllNotifications().stream()
                .filter(notification -> notification.getIdNotification() == notificationId)
                .findFirst();
    }

    // === NOUVELLE MÉTHODE POUR LES DEMANDES D'AJOUT ===
    
    /**
     * Crée une notification pour une demande d'ajout (méthode simplifiée)
     */
    public boolean creerNotificationPourDemande(int userId, String message) {
    try {
        System.out.println("📨 [NOTIF SERVICE] Création notification pour user #" + userId);
        System.out.println("📝 Message: " + message);
        
        // Créer une notification simple
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUserId(userId);
        notification.setReadable(false);
        
        System.out.println("💾 Sauvegarde notification...");
        Notification saved = save(notification);
        
        if (saved != null && saved.getIdNotification() > 0) {
            System.out.println("✅ Notification créée avec ID: " + saved.getIdNotification());
            return true;
        } else {
            System.err.println("❌ Échec création notification - saved est null ou ID invalide");
            return false;
        }
        
    } catch (Exception e) {
        System.err.println("❌ Erreur création notification: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

    // === MÉTHODES PRIVÉES ===

    private Document loadXmlDocument() throws Exception {
        File file = new File(XML_FILE_PATH);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        if (!file.exists()) {
            // Créer le fichier s'il n'existe pas
            Document doc = builder.newDocument();
            Element root = doc.createElementNS(NAMESPACE_URI, "Notifications");
            doc.appendChild(root);
            saveXmlDocument(doc);
        }
        
        return builder.parse(file);
    }

    private void saveXmlDocument(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(XML_FILE_PATH));
        transformer.transform(source, result);
    }

    private Notification parseNotification(Element element) {
        Notification notification = new Notification();
        
        try {
            notification.setIdNotification(Integer.parseInt(getElementText(element, "idNotification")));
            notification.setMessage(getElementText(element, "message"));
            
            String createdAt = getElementText(element, "createdAt");
            if (createdAt != null && !createdAt.isEmpty()) {
                notification.setCreatedAt(LocalDateTime.parse(createdAt, FORMATTER));
            } else {
                notification.setCreatedAt(LocalDateTime.now());
            }
            
            notification.setUserId(Integer.parseInt(getElementText(element, "userId")));
            notification.setReadable(Boolean.parseBoolean(getElementText(element, "readable")));
            
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return notification;
    }

    private String getElementText(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagNameNS(NAMESPACE_URI, tagName);
        if (nodes.getLength() > 0) {
            String text = nodes.item(0).getTextContent();
            return text != null ? text.trim() : null;
        }
        return null;
    }

    private void addTextElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElementNS(NAMESPACE_URI, tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    private void updateElementText(Element parent, String tagName, String textContent) {
        NodeList nodes = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
        if (nodes.getLength() > 0) {
            nodes.item(0).setTextContent(textContent);
        }
    }

    private int generateNewId(Document doc) {
        int maxId = 0;
        NodeList nodes = doc.getElementsByTagNameNS(NAMESPACE_URI, "Notification");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            int id = Integer.parseInt(getElementText(el, "idNotification"));
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }

    // === MÉTHODES POUR LES DEMANDES CITOYENNES ===
    
    public void notifierNouvelleDemande(int demandeId, String description) {
        String message = String.format(
            "📋 Nouvelle demande citoyenne #%d%nDescription: %s",
            demandeId, description
        );
        
        // Notifier les administrateurs (userId = 1 par exemple)
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUserId(1); // ID de l'admin
        notification.setReadable(false);
        
        save(notification);
        System.out.println("✅ Notification demande citoyenne envoyée");
    }

    public void notifierNouvelleIntervention(int interventionId, int demandeId) {
        String message = String.format(
            "🔧 Nouvelle intervention #%d planifiée pour la demande #%d",
            interventionId, demandeId
        );
        
        // Notifier les administrateurs
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUserId(1); // ID de l'admin
        notification.setReadable(false);
        
        save(notification);
        System.out.println("✅ Notification intervention envoyée");
    }

    public void notifierCitoyenInterventionLancee(int citoyenId, int demandeId, int interventionId) {
        String message = String.format(
            "🚀 Votre demande #%d a été prise en charge !%n" +
            "Une intervention #%d a été planifiée pour résoudre votre problème.",
            demandeId, interventionId
        );
        
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUserId(citoyenId);
        notification.setReadable(false);
        
        save(notification);
        System.out.println("✅ Notification citoyen envoyée");
    }
}