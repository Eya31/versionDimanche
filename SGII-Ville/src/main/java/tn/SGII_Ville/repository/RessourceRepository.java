package tn.SGII_Ville.repository;

import org.springframework.stereotype.Repository;
import tn.SGII_Ville.model.RessourceMaterielle;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

@Repository
public class RessourceRepository {
    private static final String FILE_PATH = "src/main/resources/data/ressources.xml";

    public List<RessourceMaterielle> findAll() {
        List<RessourceMaterielle> ressources = new ArrayList<>();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return ressources;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList ressourceNodes = doc.getElementsByTagName("RessourceMaterielle");
            for (int i = 0; i < ressourceNodes.getLength(); i++) {
                Element ressourceElement = (Element) ressourceNodes.item(i);
                RessourceMaterielle ressource = parseRessource(ressourceElement);
                ressources.add(ressource);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ressources;
    }

    public Optional<RessourceMaterielle> findById(Integer id) {
        return findAll().stream()
            .filter(r -> r.getId().equals(id))
            .findFirst();
    }

    public RessourceMaterielle save(RessourceMaterielle ressource) {
        try {
            Document doc;
            Element root;
            File file = new File(FILE_PATH);
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            if (file.exists()) {
                doc = builder.parse(file);
                root = doc.getDocumentElement();
                
                // Vérifier si la ressource existe déjà
                NodeList ressourceNodes = root.getElementsByTagName("RessourceMaterielle");
                for (int i = 0; i < ressourceNodes.getLength(); i++) {
                    Element ressourceElement = (Element) ressourceNodes.item(i);
                    Integer currentId = Integer.parseInt(getElementText(ressourceElement, "id"));
                    
                    if (currentId.equals(ressource.getId())) {
                        // Mettre à jour la ressource existante
                        updateRessourceElement(ressourceElement, ressource);
                        saveDocument(doc);
                        return ressource;
                    }
                }
            } else {
                doc = builder.newDocument();
                root = doc.createElement("Ressources");
                doc.appendChild(root);
            }
            
            // Ajouter une nouvelle ressource
            Element newRessource = createRessourceElement(doc, ressource);
            root.appendChild(newRessource);
            
            saveDocument(doc);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ressource;
    }

    public void deleteById(Integer id) {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            Element root = doc.getDocumentElement();
            NodeList ressourceNodes = root.getElementsByTagName("RessourceMaterielle");
            
            for (int i = 0; i < ressourceNodes.getLength(); i++) {
                Element ressourceElement = (Element) ressourceNodes.item(i);
                Integer currentId = Integer.parseInt(getElementText(ressourceElement, "id"));
                
                if (currentId.equals(id)) {
                    root.removeChild(ressourceElement);
                    break;
                }
            }
            
            saveDocument(doc);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthodes privées utilitaires
    private RessourceMaterielle parseRessource(Element element) {
        Integer id = Integer.parseInt(getElementText(element, "id"));
        String designation = getElementText(element, "designation");
        Integer quantiteEnStock = Integer.parseInt(getElementText(element, "quantiteEnStock"));
        Double prixUnitaire = Double.parseDouble(getElementText(element, "prixUnitaire"));
        
        return new RessourceMaterielle(id, designation, quantiteEnStock, prixUnitaire);
    }

    private Element createRessourceElement(Document doc, RessourceMaterielle ressource) {
        Element ressourceElement = doc.createElement("RessourceMaterielle");
        
        appendElement(doc, ressourceElement, "id", ressource.getId().toString());
        appendElement(doc, ressourceElement, "designation", ressource.getDesignation());
        appendElement(doc, ressourceElement, "quantiteEnStock", ressource.getQuantiteEnStock().toString());
        appendElement(doc, ressourceElement, "prixUnitaire", ressource.getPrixUnitaire().toString());
        
        return ressourceElement;
    }

    private void updateRessourceElement(Element ressourceElement, RessourceMaterielle ressource) {
        NodeList designationNodes = ressourceElement.getElementsByTagName("designation");
        if (designationNodes.getLength() > 0) {
            designationNodes.item(0).setTextContent(ressource.getDesignation());
        }
        
        NodeList quantiteNodes = ressourceElement.getElementsByTagName("quantiteEnStock");
        if (quantiteNodes.getLength() > 0) {
            quantiteNodes.item(0).setTextContent(ressource.getQuantiteEnStock().toString());
        }
        
        NodeList prixNodes = ressourceElement.getElementsByTagName("prixUnitaire");
        if (prixNodes.getLength() > 0) {
            prixNodes.item(0).setTextContent(ressource.getPrixUnitaire().toString());
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