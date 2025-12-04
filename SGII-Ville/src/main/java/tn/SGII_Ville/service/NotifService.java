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
            Document doc = xmlService.loadXmlDocument("Notifications");
            Element root = doc.getDocumentElement();

            NodeList notifNodes = root.getElementsByTagNameNS(
                xmlService.getNamespaceUri(), "Notification"
            );

            for (int i = 0; i < notifNodes.getLength(); i++) {
                Element notifElement = (Element) notifNodes.item(i);
                Notification notification = parseNotification(notifElement);
                notifications.add(notification);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération toutes notifications: " + e.getMessage());
        }
        return notifications;
    }

    /**
     * Récupère les notifications par utilisateur
     */
/**
     * Récupère toutes les notifications d'un utilisateur
     */
    public List<Notification> getNotificationsByUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("Notifications");
            Element root = doc.getDocumentElement();

            NodeList notifNodes = root.getElementsByTagNameNS(
                xmlService.getNamespaceUri(), "Notification"
            );

            for (int i = 0; i < notifNodes.getLength(); i++) {
                Element notifElement = (Element) notifNodes.item(i);
                
                // Vérifier si c'est pour cet utilisateur
                String userIdStr = xmlService.getElementTextContent(notifElement, "userId");
                if (userIdStr != null && Integer.parseInt(userIdStr) == userId) {
                    Notification notification = parseNotification(notifElement);
                    notifications.add(notification);
                }
            }

            // Trier par date (plus récentes d'abord)
            notifications.sort((n1, n2) -> 
                n2.getCreatedAt().compareTo(n1.getCreatedAt())
            );

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération notifications: " + e.getMessage());
        }
        return notifications;
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
        try {
            List<Notification> notifications = getNotificationsByUser(userId);
            return (int) notifications.stream()
                .filter(n -> !n.isReadable())
                .count();
        } catch (Exception e) {
            return 0;
        }
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
            Document doc = xmlService.loadXmlDocument("Notifications");
            Element root = doc.getDocumentElement();

            NodeList notifNodes = root.getElementsByTagNameNS(
                xmlService.getNamespaceUri(), "Notification"
            );

            for (int i = 0; i < notifNodes.getLength(); i++) {
                Element notifElement = (Element) notifNodes.item(i);
                String idStr = xmlService.getElementTextContent(notifElement, "idNotification");
                
                if (idStr != null && Integer.parseInt(idStr) == notificationId) {
                    // Mettre à jour le champ readable
                    NodeList readableNodes = notifElement.getElementsByTagNameNS(
                        xmlService.getNamespaceUri(), "readable"
                    );
                    
                    if (readableNodes.getLength() > 0) {
                        Element readableElement = (Element) readableNodes.item(0);
                        readableElement.setTextContent("true");
                    } else {
                        // Créer l'élément s'il n'existe pas
                        xmlService.addTextElement(doc, notifElement, "readable", "true");
                    }

                    xmlService.saveXmlDocument(doc, "Notifications");
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("❌ Erreur marquage notification comme lue: " + e.getMessage());
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
     /**
     * Crée une notification pour une demande d'ajout
     */
    public boolean creerNotificationPourDemande(int userId, String message) {
        try {
            System.out.println("📨 Création notification pour user #" + userId);
            System.out.println("📝 Message: " + message);

            Document doc = xmlService.loadXmlDocument("Notifications");
            Element root = doc.getDocumentElement();

            // Générer nouvel ID
            int newId = generateNewId(doc);

            // Créer l'élément Notification
            Element notifElement = doc.createElementNS(xmlService.getNamespaceUri(), "Notification");

            // Ajouter les champs
            xmlService.addTextElement(doc, notifElement, "idNotification", String.valueOf(newId));
            xmlService.addTextElement(doc, notifElement, "userId", String.valueOf(userId));
            xmlService.addTextElement(doc, notifElement, "message", message);
            xmlService.addTextElement(doc, notifElement, "createdAt", LocalDateTime.now().format(FORMATTER));
            xmlService.addTextElement(doc, notifElement, "readable", "false");

            root.appendChild(notifElement);
            xmlService.saveXmlDocument(doc, "Notifications");

            System.out.println("✅ Notification créée avec ID: " + newId);
            return true;

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
            String idStr = xmlService.getElementTextContent(element, "idNotification");
            if (idStr != null) notification.setIdNotification(Integer.parseInt(idStr));

            String userIdStr = xmlService.getElementTextContent(element, "userId");
            if (userIdStr != null) notification.setUserId(Integer.parseInt(userIdStr));

            notification.setMessage(xmlService.getElementTextContent(element, "message"));

            String createdAtStr = xmlService.getElementTextContent(element, "createdAt");
            if (createdAtStr != null) {
                notification.setCreatedAt(LocalDateTime.parse(createdAtStr, FORMATTER));
            }

            String readableStr = xmlService.getElementTextContent(element, "readable");
            if (readableStr != null) {
                notification.setReadable(Boolean.parseBoolean(readableStr));
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing notification: " + e.getMessage());
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
        NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Notification");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String idStr = xmlService.getElementTextContent(el, "idNotification");
            if (idStr != null) {
                try {
                    maxId = Math.max(maxId, Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
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