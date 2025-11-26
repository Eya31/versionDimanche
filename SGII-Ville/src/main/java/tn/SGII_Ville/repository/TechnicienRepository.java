package tn.SGII_Ville.repository;

import org.springframework.stereotype.Repository;
import tn.SGII_Ville.model.Technicien;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

@Repository
public class TechnicienRepository {
    private static final String FILE_PATH = "src/main/resources/data/techniciens.xml";
    private static final String NAMESPACE_URI = "http://example.com/gestion-interventions";

    public List<Technicien> findAll() {
        List<Technicien> techniciens = new ArrayList<>();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return techniciens;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // IMPORTANT: Activer la prise en charge des namespaces
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            // Utiliser getElementsByTagNameNS pour les namespaces
            NodeList technicienNodes = doc.getElementsByTagNameNS(NAMESPACE_URI, "Technicien");
            for (int i = 0; i < technicienNodes.getLength(); i++) {
                Element technicienElement = (Element) technicienNodes.item(i);
                Technicien technicien = parseTechnicien(technicienElement);
                techniciens.add(technicien);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return techniciens;
    }

    public Optional<Technicien> findById(Integer id) {
        return findAll().stream()
            .filter(t -> t.getId().equals(id))
            .findFirst();
    }

    public Technicien save(Technicien technicien) {
        try {
            Document doc;
            Element root;
            File file = new File(FILE_PATH);
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            if (file.exists()) {
                doc = builder.parse(file);
                root = doc.getDocumentElement();
                
                // Vérifier si le technicien existe déjà
                NodeList technicienNodes = doc.getElementsByTagNameNS(NAMESPACE_URI, "Technicien");
                for (int i = 0; i < technicienNodes.getLength(); i++) {
                    Element technicienElement = (Element) technicienNodes.item(i);
                    Integer currentId = Integer.parseInt(getElementTextNS(technicienElement, "id"));
                    
                    if (currentId.equals(technicien.getId())) {
                        // Mettre à jour le technicien existant
                        updateTechnicienElement(technicienElement, technicien);
                        saveDocument(doc);
                        return technicien;
                    }
                }
            } else {
                doc = builder.newDocument();
                root = doc.createElementNS(NAMESPACE_URI, "Techniciens");
                doc.appendChild(root);
            }
            
            // Ajouter un nouveau technicien
            Element newTechnicien = createTechnicienElement(doc, technicien);
            root.appendChild(newTechnicien);
            
            saveDocument(doc);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return technicien;
    }

    // Méthodes privées utilitaires
    private Technicien parseTechnicien(Element element) {
        Integer id = Integer.parseInt(getElementTextNS(element, "id"));
        String nom = getElementTextNS(element, "nom");
        String email = getElementTextNS(element, "email");
        String motDePasse = getElementTextNS(element, "motDePasse");
        String role = getElementTextNS(element, "role");
        Boolean disponibilite = Boolean.parseBoolean(getElementTextNS(element, "disponibilite"));
        
        // Parser les compétences
        List<String> competences = new ArrayList<>();
        NodeList competenceNodes = element.getElementsByTagNameNS(NAMESPACE_URI, "competences");
        for (int i = 0; i < competenceNodes.getLength(); i++) {
            competences.add(competenceNodes.item(i).getTextContent());
        }
        
        // Parser les matériels
        List<Integer> materiels = new ArrayList<>();
        Element materielsElement = getChildElementNS(element, "materiels");
        if (materielsElement != null) {
            NodeList materielNodes = materielsElement.getElementsByTagNameNS(NAMESPACE_URI, "materielId");
            for (int i = 0; i < materielNodes.getLength(); i++) {
                materiels.add(Integer.parseInt(materielNodes.item(i).getTextContent()));
            }
        }
        
        // Parser les équipements
        List<Integer> equipements = new ArrayList<>();
        Element equipementsElement = getChildElementNS(element, "equipements");
        if (equipementsElement != null) {
            NodeList equipementNodes = equipementsElement.getElementsByTagNameNS(NAMESPACE_URI, "equipementId");
            for (int i = 0; i < equipementNodes.getLength(); i++) {
                equipements.add(Integer.parseInt(equipementNodes.item(i).getTextContent()));
            }
        }
        
        Technicien technicien = new Technicien();
        technicien.setId(id);
        technicien.setNom(nom);
        technicien.setEmail(email);
        technicien.setMotDePasse(motDePasse);
        technicien.setRole(role);
        technicien.setCompetences(competences);
        technicien.setDisponibilite(disponibilite);
        technicien.setMateriels(materiels);
        technicien.setEquipements(equipements);
        
        return technicien;
    }

    private Element createTechnicienElement(Document doc, Technicien technicien) {
        Element technicienElement = doc.createElementNS(NAMESPACE_URI, "Technicien");
        
        appendElementNS(doc, technicienElement, "id", technicien.getId().toString());
        appendElementNS(doc, technicienElement, "nom", technicien.getNom());
        appendElementNS(doc, technicienElement, "email", technicien.getEmail());
        appendElementNS(doc, technicienElement, "motDePasse", technicien.getMotDePasse());
        appendElementNS(doc, technicienElement, "role", technicien.getRole());
        
        // Ajouter les compétences
        if (technicien.getCompetences() != null) {
            for (String competence : technicien.getCompetences()) {
                appendElementNS(doc, technicienElement, "competences", competence);
            }
        }
        
        appendElementNS(doc, technicienElement, "disponibilite", technicien.getDisponibilite().toString());
        
        // Ajouter les matériels
        if (technicien.getMateriels() != null && !technicien.getMateriels().isEmpty()) {
            Element materielsElement = doc.createElementNS(NAMESPACE_URI, "materiels");
            for (Integer materielId : technicien.getMateriels()) {
                appendElementNS(doc, materielsElement, "materielId", materielId.toString());
            }
            technicienElement.appendChild(materielsElement);
        } else {
            appendElementNS(doc, technicienElement, "materiels", "");
        }
        
        // Ajouter les équipements
        if (technicien.getEquipements() != null && !technicien.getEquipements().isEmpty()) {
            Element equipementsElement = doc.createElementNS(NAMESPACE_URI, "equipements");
            for (Integer equipementId : technicien.getEquipements()) {
                appendElementNS(doc, equipementsElement, "equipementId", equipementId.toString());
            }
            technicienElement.appendChild(equipementsElement);
        } else {
            appendElementNS(doc, technicienElement, "equipements", "");
        }
        
        return technicienElement;
    }

    private void updateTechnicienElement(Element technicienElement, Technicien technicien) {
        // Mettre à jour les éléments simples
        updateElementTextNS(technicienElement, "nom", technicien.getNom());
        updateElementTextNS(technicienElement, "email", technicien.getEmail());
        updateElementTextNS(technicienElement, "motDePasse", technicien.getMotDePasse());
        updateElementTextNS(technicienElement, "role", technicien.getRole());
        updateElementTextNS(technicienElement, "disponibilite", technicien.getDisponibilite().toString());
        
        // Mettre à jour les compétences (supprimer et recréer)
        removeElementsNS(technicienElement, "competences");
        if (technicien.getCompetences() != null) {
            for (String competence : technicien.getCompetences()) {
                appendElementNS(technicienElement.getOwnerDocument(), technicienElement, "competences", competence);
            }
        }
        
        // Mettre à jour les matériels
        updateListElementsNS(technicienElement, "materiels", "materielId", technicien.getMateriels());
        
        // Mettre à jour les équipements
        updateListElementsNS(technicienElement, "equipements", "equipementId", technicien.getEquipements());
    }

    // Méthodes utilitaires avec support namespace
    private void appendElementNS(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElementNS(NAMESPACE_URI, tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    private String getElementTextNS(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }

    private Element getChildElementNS(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
        if (nodes.getLength() > 0) {
            return (Element) nodes.item(0);
        }
        return null;
    }

    private void updateElementTextNS(Element parent, String tagName, String textContent) {
        NodeList nodes = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
        if (nodes.getLength() > 0) {
            nodes.item(0).setTextContent(textContent);
        }
    }

    private void removeElementsNS(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            parent.removeChild(nodes.item(i));
        }
    }

    private void updateListElementsNS(Element parent, String containerTag, String itemTag, List<Integer> items) {
        // Supprimer l'ancien conteneur
        removeElementsNS(parent, containerTag);
        
        if (items != null && !items.isEmpty()) {
            Document doc = parent.getOwnerDocument();
            Element containerElement = doc.createElementNS(NAMESPACE_URI, containerTag);
            for (Integer itemId : items) {
                appendElementNS(doc, containerElement, itemTag, itemId.toString());
            }
            parent.appendChild(containerElement);
        }
    }

    private void saveDocument(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(FILE_PATH));
        transformer.transform(source, result);
    }
}