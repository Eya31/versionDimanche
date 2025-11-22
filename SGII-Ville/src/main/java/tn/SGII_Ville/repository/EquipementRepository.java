package tn.SGII_Ville.repository;

import org.springframework.stereotype.Repository;
import tn.SGII_Ville.model.Equipement;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

@Repository
public class EquipementRepository {
    private static final String FILE_PATH = "src/main/resources/data/equipements.xml";

    public List<Equipement> findAll() {
        List<Equipement> equipements = new ArrayList<>();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return equipements;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList equipementNodes = doc.getElementsByTagName("Equipement");
            for (int i = 0; i < equipementNodes.getLength(); i++) {
                Element equipementElement = (Element) equipementNodes.item(i);
                Equipement equipement = parseEquipement(equipementElement);
                equipements.add(equipement);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return equipements;
    }

    public Optional<Equipement> findById(Integer id) {
        return findAll().stream()
            .filter(e -> e.getId().equals(id))
            .findFirst();
    }

    public List<Equipement> findByEtat(String etat) {
        return findAll().stream()
            .filter(e -> e.getEtat().equals(etat))
            .toList();
    }

    public Equipement save(Equipement equipement) {
        try {
            Document doc;
            Element root;
            File file = new File(FILE_PATH);
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            if (file.exists()) {
                doc = builder.parse(file);
                root = doc.getDocumentElement();
                
                // Vérifier si l'équipement existe déjà
                NodeList equipementNodes = root.getElementsByTagName("Equipement");
                for (int i = 0; i < equipementNodes.getLength(); i++) {
                    Element equipementElement = (Element) equipementNodes.item(i);
                    Integer currentId = Integer.parseInt(getElementText(equipementElement, "id"));
                    
                    if (currentId.equals(equipement.getId())) {
                        // Mettre à jour l'équipement existant
                        updateEquipementElement(equipementElement, equipement);
                        saveDocument(doc);
                        return equipement;
                    }
                }
            } else {
                doc = builder.newDocument();
                root = doc.createElement("Equipements");
                doc.appendChild(root);
            }
            
            // Ajouter un nouvel équipement
            Element newEquipement = createEquipementElement(doc, equipement);
            root.appendChild(newEquipement);
            
            saveDocument(doc);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return equipement;
    }

    public void deleteById(Integer id) {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            Element root = doc.getDocumentElement();
            NodeList equipementNodes = root.getElementsByTagName("Equipement");
            
            for (int i = 0; i < equipementNodes.getLength(); i++) {
                Element equipementElement = (Element) equipementNodes.item(i);
                Integer currentId = Integer.parseInt(getElementText(equipementElement, "id"));
                
                if (currentId.equals(id)) {
                    root.removeChild(equipementElement);
                    break;
                }
            }
            
            saveDocument(doc);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthodes privées utilitaires
    private Equipement parseEquipement(Element element) {
        Integer id = Integer.parseInt(getElementText(element, "id"));
        String type = getElementText(element, "type");
        String modele = getElementText(element, "modele");
        String numeroSerie = getElementText(element, "numeroSerie");
        String etat = getElementText(element, "etat");
        String dateAcquisition = getElementText(element, "dateAcquisition");
        
        Equipement equipement = new Equipement();
        equipement.setId(id);
        equipement.setType(type);
        equipement.setModele(modele);
        equipement.setNumeroSerie(numeroSerie);
        equipement.setEtat(etat);
        equipement.setDateAcquisition(dateAcquisition);
        
        return equipement;
    }

    private Element createEquipementElement(Document doc, Equipement equipement) {
        Element equipementElement = doc.createElement("Equipement");
        
        appendElement(doc, equipementElement, "id", equipement.getId().toString());
        appendElement(doc, equipementElement, "type", equipement.getType());
        appendElement(doc, equipementElement, "modele", equipement.getModele());
        appendElement(doc, equipementElement, "numeroSerie", equipement.getNumeroSerie());
        appendElement(doc, equipementElement, "etat", equipement.getEtat());
        appendElement(doc, equipementElement, "dateAcquisition", equipement.getDateAcquisition());
        
        return equipementElement;
    }

    private void updateEquipementElement(Element equipementElement, Equipement equipement) {
        NodeList typeNodes = equipementElement.getElementsByTagName("type");
        if (typeNodes.getLength() > 0) {
            typeNodes.item(0).setTextContent(equipement.getType());
        }
        
        NodeList modeleNodes = equipementElement.getElementsByTagName("modele");
        if (modeleNodes.getLength() > 0) {
            modeleNodes.item(0).setTextContent(equipement.getModele());
        }
        
        NodeList numeroSerieNodes = equipementElement.getElementsByTagName("numeroSerie");
        if (numeroSerieNodes.getLength() > 0) {
            numeroSerieNodes.item(0).setTextContent(equipement.getNumeroSerie());
        }
        
        NodeList etatNodes = equipementElement.getElementsByTagName("etat");
        if (etatNodes.getLength() > 0) {
            etatNodes.item(0).setTextContent(equipement.getEtat());
        }
        
        NodeList dateAcquisitionNodes = equipementElement.getElementsByTagName("dateAcquisition");
        if (dateAcquisitionNodes.getLength() > 0) {
            dateAcquisitionNodes.item(0).setTextContent(equipement.getDateAcquisition());
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