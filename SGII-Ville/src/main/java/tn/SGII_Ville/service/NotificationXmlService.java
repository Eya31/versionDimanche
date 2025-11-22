package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import tn.SGII_Ville.entities.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationXmlService {

    @Autowired
    private XmlService xmlService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Récupère toutes les notifications
     */
    public List<Notification> getAllNotifications() {
        List<Notification> notifications = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("Notifications");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Notification");
            for (int i = 0; i < nodes.getLength(); i++) {
                notifications.add(parseNotification((Element) nodes.item(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notifications;
    }

    /**
     * Trouve une notification par ID
     */
    public Notification findById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Notifications");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Notification");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "idNotification")) == id) {
                    return parseNotification(el);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère les notifications d'un utilisateur
     */
    public List<Notification> getNotificationsByUserId(int userId) {
        return getAllNotifications().stream()
            .filter(n -> n.getUserId() == userId)
            .collect(Collectors.toList());
    }

    /**
     * Parse un élément Notification XML
     */
    private Notification parseNotification(Element element) {
        Notification notification = new Notification();
        
        notification.setIdNotification(Integer.parseInt(xmlService.getElementTextContent(element, "idNotification")));
        notification.setMessage(xmlService.getElementTextContent(element, "message"));
        notification.setUserId(Integer.parseInt(xmlService.getElementTextContent(element, "userId")));
        
        String createdAtStr = xmlService.getElementTextContent(element, "createdAt");
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            notification.setCreatedAt(LocalDateTime.parse(createdAtStr, FORMATTER));
        }
        
        String readableStr = xmlService.getElementTextContent(element, "readable");
        notification.setReadable("true".equalsIgnoreCase(readableStr));
        
        return notification;
    }

    /**
     * Sauvegarde une nouvelle notification
     */
    public Notification save(Notification notification) {
        try {
            Document doc = xmlService.loadXmlDocument("Notifications");
            Element root = doc.getDocumentElement();

            // Générer un nouvel ID
            int newId = generateNewId(doc);
            notification.setIdNotification(newId);

            // Créer l'élément Notification
            Element notifElement = doc.createElementNS(xmlService.getNamespaceUri(), "Notification");

            xmlService.addTextElement(doc, notifElement, "idNotification", String.valueOf(notification.getIdNotification()));
            xmlService.addTextElement(doc, notifElement, "message", notification.getMessage());
            xmlService.addTextElement(doc, notifElement, "createdAt", 
                notification.getCreatedAt() != null ? notification.getCreatedAt().format(FORMATTER) : LocalDateTime.now().format(FORMATTER));
            xmlService.addTextElement(doc, notifElement, "userId", String.valueOf(notification.getUserId()));
            xmlService.addTextElement(doc, notifElement, "readable", String.valueOf(notification.isReadable()));

            root.appendChild(notifElement);

            xmlService.saveXmlDocument(doc, "Notifications");
            return notification;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la sauvegarde de la notification", e);
        }
    }

    /**
     * Met à jour une notification
     */
    public Notification update(Notification notification) {
        try {
            Document doc = xmlService.loadXmlDocument("Notifications");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Notification");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "idNotification")) == notification.getIdNotification()) {
                    // Mise à jour des éléments
                    NodeList messageNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "message");
                    if (messageNodes.getLength() > 0) messageNodes.item(0).setTextContent(notification.getMessage());
                    
                    NodeList readableNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "readable");
                    if (readableNodes.getLength() > 0) readableNodes.item(0).setTextContent(String.valueOf(notification.isReadable()));
                    
                    NodeList createdAtNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "createdAt");
                    if (createdAtNodes.getLength() > 0) createdAtNodes.item(0).setTextContent(notification.getCreatedAt().format(FORMATTER));
                    
                    xmlService.saveXmlDocument(doc, "Notifications");
                    return notification;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Génère un nouvel ID unique
     */
    private int generateNewId(Document doc) {
        int maxId = 0;
        NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Notification");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            int id = Integer.parseInt(xmlService.getElementTextContent(el, "idNotification"));
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }

    /**
     * Supprime une notification
     */
    public void delete(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Notifications");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Notification");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "idNotification")) == id) {
                    el.getParentNode().removeChild(el);
                    xmlService.saveXmlDocument(doc, "Notifications");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
