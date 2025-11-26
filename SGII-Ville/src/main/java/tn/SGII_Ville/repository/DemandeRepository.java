package tn.SGII_Ville.repository;

import org.springframework.stereotype.Repository;
import tn.SGII_Ville.model.Demande;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

@Repository
public class DemandeRepository {
    private static final String FILE_PATH = "src/main/resources/data/demandes.xml";

    public List<Demande> findAll() {
        List<Demande> demandes = new ArrayList<>();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return demandes;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList demandeNodes = doc.getElementsByTagName("Demande");
            for (int i = 0; i < demandeNodes.getLength(); i++) {
                Element demandeElement = (Element) demandeNodes.item(i);
                Demande demande = parseDemande(demandeElement);
                demandes.add(demande);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return demandes;
    }

    public Optional<Demande> findById(Integer id) {
        return findAll().stream()
            .filter(d -> d.getId().equals(id))
            .findFirst();
    }

    public List<Demande> findByEtat(String etat) {
        return findAll().stream()
            .filter(d -> d.getEtat().equals(etat))
            .toList();
    }

    public Demande save(Demande demande) {
        try {
            Document doc;
            Element root;
            File file = new File(FILE_PATH);
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            if (file.exists()) {
                doc = builder.parse(file);
                root = doc.getDocumentElement();
                
                // Vérifier si la demande existe déjà
                NodeList demandeNodes = root.getElementsByTagName("Demande");
                for (int i = 0; i < demandeNodes.getLength(); i++) {
                    Element demandeElement = (Element) demandeNodes.item(i);
                    Integer currentId = Integer.parseInt(getElementText(demandeElement, "id"));
                    
                    if (currentId.equals(demande.getId())) {
                        // Mettre à jour la demande existante
                        updateDemandeElement(demandeElement, demande);
                        saveDocument(doc);
                        return demande;
                    }
                }
            } else {
                doc = builder.newDocument();
                root = doc.createElement("Demandes");
                doc.appendChild(root);
            }
            
            // Ajouter une nouvelle demande
            Element newDemande = createDemandeElement(doc, demande);
            root.appendChild(newDemande);
            
            saveDocument(doc);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return demande;
    }

    public void deleteById(Integer id) {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            Element root = doc.getDocumentElement();
            NodeList demandeNodes = root.getElementsByTagName("Demande");
            
            for (int i = 0; i < demandeNodes.getLength(); i++) {
                Element demandeElement = (Element) demandeNodes.item(i);
                Integer currentId = Integer.parseInt(getElementText(demandeElement, "id"));
                
                if (currentId.equals(id)) {
                    root.removeChild(demandeElement);
                    break;
                }
            }
            
            saveDocument(doc);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthodes privées utilitaires
    private Demande parseDemande(Element element) {
        Integer id = Integer.parseInt(getElementText(element, "id"));
        String type = getElementText(element, "type");
        String description = getElementText(element, "description");
        String etat = getElementText(element, "etat");
        String dateCreation = getElementText(element, "dateCreation");
        String localisation = getElementText(element, "localisation");
        
        Demande demande = new Demande();
        demande.setId(id);
        demande.setType(type);
        demande.setDescription(description);
        demande.setEtat(etat);
        demande.setDateCreation(dateCreation);
        demande.setLocalisation(localisation);
        
        return demande;
    }

    private Element createDemandeElement(Document doc, Demande demande) {
        Element demandeElement = doc.createElement("Demande");
        
        appendElement(doc, demandeElement, "id", demande.getId().toString());
        appendElement(doc, demandeElement, "type", demande.getType());
        appendElement(doc, demandeElement, "description", demande.getDescription());
        appendElement(doc, demandeElement, "etat", demande.getEtat());
        appendElement(doc, demandeElement, "dateCreation", demande.getDateCreation());
        appendElement(doc, demandeElement, "localisation", demande.getLocalisation());
        
        return demandeElement;
    }

    private void updateDemandeElement(Element demandeElement, Demande demande) {
        NodeList typeNodes = demandeElement.getElementsByTagName("type");
        if (typeNodes.getLength() > 0) {
            typeNodes.item(0).setTextContent(demande.getType());
        }
        
        NodeList descriptionNodes = demandeElement.getElementsByTagName("description");
        if (descriptionNodes.getLength() > 0) {
            descriptionNodes.item(0).setTextContent(demande.getDescription());
        }
        
        NodeList etatNodes = demandeElement.getElementsByTagName("etat");
        if (etatNodes.getLength() > 0) {
            etatNodes.item(0).setTextContent(demande.getEtat());
        }
        
        NodeList dateCreationNodes = demandeElement.getElementsByTagName("dateCreation");
        if (dateCreationNodes.getLength() > 0) {
            dateCreationNodes.item(0).setTextContent(demande.getDateCreation());
        }
        
        NodeList localisationNodes = demandeElement.getElementsByTagName("localisation");
        if (localisationNodes.getLength() > 0) {
            localisationNodes.item(0).setTextContent(demande.getLocalisation());
        }
    }

    private void appendElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
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