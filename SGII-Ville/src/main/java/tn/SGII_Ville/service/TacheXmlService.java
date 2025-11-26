package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.SGII_Ville.entities.Tache;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TacheXmlService {

    private static final Logger logger = LoggerFactory.getLogger(TacheXmlService.class);

    @Autowired
    private XmlService xmlService;

    /**
     * Récupère toutes les tâches
     */
    public List<Tache> findAll() {
        List<Tache> list = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("Taches");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Tache");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                list.add(parseTache(el));
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des tâches", e);
        }
        return list;
    }

    /**
     * Récupère toutes les tâches d'une intervention
     */
    public List<Tache> findByInterventionId(int interventionId) {
        return findAll().stream()
                .filter(t -> t.getInterventionId() == interventionId)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les tâches d'une main-d'œuvre
     */
    public List<Tache> findByMainDOeuvreId(int mainDOeuvreId) {
        return findAll().stream()
                .filter(t -> t.getMainDOeuvreId() != null && t.getMainDOeuvreId() == mainDOeuvreId)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une tâche par ID
     */
    public Tache findById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Taches");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Tache");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    return parseTache(el);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de la tâche ID: {}", id, e);
        }
        return null;
    }

    /**
     * Sauvegarde une tâche
     */
    public Tache save(Tache tache) throws Exception {
        Document doc;
        try {
            doc = xmlService.loadXmlDocument("Taches");
        } catch (Exception e) {
            doc = xmlService.createNewDocument("Taches");
        }

        Element root = doc.getDocumentElement();
        if (root == null) {
            root = doc.createElementNS(xmlService.getNamespaceUri(), "Taches");
            doc.appendChild(root);
        }

        // Si nouvel ID, générer un ID
        if (tache.getId() == 0) {
            // Générer un nouvel ID en cherchant le max parmi les tâches existantes
            int maxId = 0;
            NodeList existingTaches = root.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Tache");
            for (int i = 0; i < existingTaches.getLength(); i++) {
                Element el = (Element) existingTaches.item(i);
                String idStr = xmlService.getElementTextContent(el, "id");
                if (idStr != null && !idStr.isEmpty()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        if (id > maxId) maxId = id;
                    } catch (NumberFormatException e) {
                        logger.warn("ID invalide dans tâche: {}", idStr);
                    }
                }
            }
            tache.setId(maxId + 1);
            logger.info("Nouvel ID tâche généré: {}", tache.getId());
        } else {
            // Mettre à jour l'existant
            NodeList nodes = root.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Tache");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == tache.getId()) {
                    root.removeChild(el);
                    break;
                }
            }
        }

        // Créer l'élément
        Element tacheEl = xmlService.createElement(doc, "Tache");
        xmlService.addTextElement(doc, tacheEl, "id", String.valueOf(tache.getId()));
        xmlService.addTextElement(doc, tacheEl, "interventionId", String.valueOf(tache.getInterventionId()));
        
        // Libellé est obligatoire
        if (tache.getLibelle() == null || tache.getLibelle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le libellé de la tâche ne peut pas être vide");
        }
        xmlService.addTextElement(doc, tacheEl, "libelle", tache.getLibelle());
        
        if (tache.getDescription() != null && !tache.getDescription().trim().isEmpty()) {
            xmlService.addTextElement(doc, tacheEl, "description", tache.getDescription());
        }
        if (tache.getMainDOeuvreId() != null) {
            xmlService.addTextElement(doc, tacheEl, "mainDOeuvreId", String.valueOf(tache.getMainDOeuvreId()));
        }
        
        // État est obligatoire
        String etat = tache.getEtat() != null ? tache.getEtat() : "A_FAIRE";
        xmlService.addTextElement(doc, tacheEl, "etat", etat);
        if (tache.getDateCreation() != null) {
            xmlService.addTextElement(doc, tacheEl, "dateCreation", tache.getDateCreation().toString());
        }
        if (tache.getDateDebut() != null) {
            xmlService.addTextElement(doc, tacheEl, "dateDebut", tache.getDateDebut().toString());
        }
        if (tache.getDateFin() != null) {
            xmlService.addTextElement(doc, tacheEl, "dateFin", tache.getDateFin().toString());
        }
        if (tache.getDateVerification() != null) {
            xmlService.addTextElement(doc, tacheEl, "dateVerification", tache.getDateVerification().toString());
        }
        if (tache.getTempsPasseMinutes() != null) {
            xmlService.addTextElement(doc, tacheEl, "tempsPasseMinutes", String.valueOf(tache.getTempsPasseMinutes()));
        }
        if (tache.getCommentaireMainDOeuvre() != null) {
            xmlService.addTextElement(doc, tacheEl, "commentaireMainDOeuvre", tache.getCommentaireMainDOeuvre());
        }
        if (tache.getCommentaireTechnicien() != null) {
            xmlService.addTextElement(doc, tacheEl, "commentaireTechnicien", tache.getCommentaireTechnicien());
        }
        if (tache.getOrdre() != null) {
            xmlService.addTextElement(doc, tacheEl, "ordre", String.valueOf(tache.getOrdre()));
        }
        xmlService.addTextElement(doc, tacheEl, "verifiee", String.valueOf(tache.isVerifiee()));

        root.appendChild(tacheEl);
        
        try {
            xmlService.saveXmlDocument(doc, "Taches");
            logger.info("Tâche sauvegardée avec succès, ID: {}", tache.getId());
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde de la tâche", e);
            throw e;
        }

        return tache;
    }

    /**
     * Supprime une tâche
     */
    public boolean delete(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Taches");
            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Tache");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    root.removeChild(el);
                    xmlService.saveXmlDocument(doc, "Taches");
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de la tâche ID: {}", id, e);
        }
        return false;
    }

    /**
     * Parse un élément XML en objet Tache
     */
    private Tache parseTache(Element el) {
        Tache t = new Tache();
        try {
            t.setId(Integer.parseInt(xmlService.getElementTextContent(el, "id")));
            t.setInterventionId(Integer.parseInt(xmlService.getElementTextContent(el, "interventionId")));
            t.setLibelle(xmlService.getElementTextContent(el, "libelle"));
            t.setDescription(xmlService.getElementTextContent(el, "description"));
            
            String mainDOeuvreIdStr = xmlService.getElementTextContent(el, "mainDOeuvreId");
            if (mainDOeuvreIdStr != null && !mainDOeuvreIdStr.isEmpty()) {
                t.setMainDOeuvreId(Integer.parseInt(mainDOeuvreIdStr));
            }
            
            t.setEtat(xmlService.getElementTextContent(el, "etat"));
            
            String dateCreationStr = xmlService.getElementTextContent(el, "dateCreation");
            if (dateCreationStr != null && !dateCreationStr.isEmpty()) {
                t.setDateCreation(LocalDateTime.parse(dateCreationStr));
            }
            
            String dateDebutStr = xmlService.getElementTextContent(el, "dateDebut");
            if (dateDebutStr != null && !dateDebutStr.isEmpty()) {
                t.setDateDebut(LocalDateTime.parse(dateDebutStr));
            }
            
            String dateFinStr = xmlService.getElementTextContent(el, "dateFin");
            if (dateFinStr != null && !dateFinStr.isEmpty()) {
                t.setDateFin(LocalDateTime.parse(dateFinStr));
            }
            
            String dateVerificationStr = xmlService.getElementTextContent(el, "dateVerification");
            if (dateVerificationStr != null && !dateVerificationStr.isEmpty()) {
                t.setDateVerification(LocalDateTime.parse(dateVerificationStr));
            }
            
            String tempsPasseStr = xmlService.getElementTextContent(el, "tempsPasseMinutes");
            if (tempsPasseStr != null && !tempsPasseStr.isEmpty()) {
                t.setTempsPasseMinutes(Integer.parseInt(tempsPasseStr));
            }
            
            t.setCommentaireMainDOeuvre(xmlService.getElementTextContent(el, "commentaireMainDOeuvre"));
            t.setCommentaireTechnicien(xmlService.getElementTextContent(el, "commentaireTechnicien"));
            
            String ordreStr = xmlService.getElementTextContent(el, "ordre");
            if (ordreStr != null && !ordreStr.isEmpty()) {
                t.setOrdre(Integer.parseInt(ordreStr));
            }
            
            String verifieeStr = xmlService.getElementTextContent(el, "verifiee");
            t.setVerifiee(verifieeStr != null && Boolean.parseBoolean(verifieeStr));

        } catch (Exception e) {
            logger.error("Erreur parsing tâche", e);
        }
        return t;
    }
}

