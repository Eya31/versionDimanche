package tn.SGII_Ville.repository;

import org.springframework.stereotype.Repository;
import tn.SGII_Ville.model.Creneau;
import tn.SGII_Ville.model.CreneauDisponibilite;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

@Repository
public class PlanningRepository {
    private static final String FILE_PATH = "src/main/resources/data/planning.xml";

    /**
     * Récupère les disponibilités d'un technicien pour une date
     */
    public List<CreneauDisponibilite> getDisponibilitesTechnicien(Integer technicienId, String date) {
        List<CreneauDisponibilite> result = new ArrayList<>();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                // Retourner des créneaux par défaut si le fichier n'existe pas
                return getCreneauxParDefaut(date);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList disponibiliteNodes = doc.getElementsByTagName("DisponibiliteTechnicien");
            for (int i = 0; i < disponibiliteNodes.getLength(); i++) {
                Element disponibiliteElement = (Element) disponibiliteNodes.item(i);
                
                Integer currentTechnicienId = Integer.parseInt(
                    getElementText(disponibiliteElement, "technicienId")
                );
                String currentDate = getElementText(disponibiliteElement, "date");
                
                if (currentTechnicienId.equals(technicienId) && currentDate.equals(date)) {
                    CreneauDisponibilite creneauDispo = parseCreneauDisponibilite(disponibiliteElement);
                    result.add(creneauDispo);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Si aucune disponibilité trouvée, retourner les créneaux par défaut
        if (result.isEmpty()) {
            result.addAll(getCreneauxParDefaut(date));
        }
        
        return result;
    }

    /**
     * Marque un technicien comme indisponible pour une date
     */
    public void marquerIndisponible(Integer technicienId, String date, String raison, Integer interventionId) {
        try {
            Document doc;
            Element root;
            File file = new File(FILE_PATH);
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            if (file.exists()) {
                doc = builder.parse(file);
                root = doc.getDocumentElement();
            } else {
                doc = builder.newDocument();
                root = doc.createElement("Planning");
                doc.appendChild(root);
            }
            
            // Vérifier si une entrée existe déjà pour ce technicien à cette date
            boolean existeDeja = false;
            NodeList disponibiliteNodes = root.getElementsByTagName("DisponibiliteTechnicien");
            for (int i = 0; i < disponibiliteNodes.getLength(); i++) {
                Element disponibiliteElement = (Element) disponibiliteNodes.item(i);
                Integer currentTechnicienId = Integer.parseInt(
                    getElementText(disponibiliteElement, "technicienId")
                );
                String currentDate = getElementText(disponibiliteElement, "date");
                
                if (currentTechnicienId.equals(technicienId) && currentDate.equals(date)) {
                    // Mettre à jour les créneaux existants
                    updateCreneauxIndisponibles(doc, disponibiliteElement, raison, interventionId);
                    existeDeja = true;
                    break;
                }
            }
            
            if (!existeDeja) {
                // Créer une nouvelle entrée
                Element newDisponibilite = createDisponibiliteElement(doc, technicienId, date, raison, interventionId);
                root.appendChild(newDisponibilite);
            }
            
            // Sauvegarder le document
            saveDocument(doc);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère toutes les indisponibilités pour une période
     */
    public List<CreneauDisponibilite> getIndisponibilitesPeriod(String startDate, String endDate) {
        List<CreneauDisponibilite> result = new ArrayList<>();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return result;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList disponibiliteNodes = doc.getElementsByTagName("DisponibiliteTechnicien");
            for (int i = 0; i < disponibiliteNodes.getLength(); i++) {
                Element disponibiliteElement = (Element) disponibiliteNodes.item(i);
                String date = getElementText(disponibiliteElement, "date");
                
                // Vérifier si la date est dans la période
                if (isDateInPeriod(date, startDate, endDate)) {
                    CreneauDisponibilite creneauDispo = parseCreneauDisponibilite(disponibiliteElement);
                    result.add(creneauDispo);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Méthodes privées utilitaires
    private List<CreneauDisponibilite> getCreneauxParDefaut(String date) {
        List<CreneauDisponibilite> result = new ArrayList<>();
        CreneauDisponibilite creneauDispo = new CreneauDisponibilite();
        creneauDispo.setDate(date);
        
        List<Creneau> creneaux = new ArrayList<>();
        
        // Créneau matin
        Creneau matin = new Creneau();
        matin.setDebut("08:00");
        matin.setFin("12:00");
        matin.setDisponible(true);
        creneaux.add(matin);
        
        // Créneau après-midi
        Creneau apresMidi = new Creneau();
        apresMidi.setDebut("14:00");
        apresMidi.setFin("18:00");
        apresMidi.setDisponible(true);
        creneaux.add(apresMidi);
        
        creneauDispo.setCreneaux(creneaux);
        result.add(creneauDispo);
        
        return result;
    }

    private CreneauDisponibilite parseCreneauDisponibilite(Element element) {
        CreneauDisponibilite creneauDispo = new CreneauDisponibilite();
        creneauDispo.setDate(getElementText(element, "date"));
        
        List<Creneau> creneaux = new ArrayList<>();
        NodeList creneauNodes = element.getElementsByTagName("Creneau");
        for (int i = 0; i < creneauNodes.getLength(); i++) {
            Element creneauElement = (Element) creneauNodes.item(i);
            Creneau creneau = new Creneau();
            creneau.setDebut(getElementText(creneauElement, "debut"));
            creneau.setFin(getElementText(creneauElement, "fin"));
            creneau.setDisponible(Boolean.parseBoolean(getElementText(creneauElement, "disponible")));
            
            String interventionId = getElementText(creneauElement, "interventionId");
            if (!interventionId.isEmpty()) {
                creneau.setInterventionId(Integer.parseInt(interventionId));
            }
            
            creneaux.add(creneau);
        }
        
        creneauDispo.setCreneaux(creneaux);
        return creneauDispo;
    }

    private Element createDisponibiliteElement(Document doc, Integer technicienId, String date, 
                                             String raison, Integer interventionId) {
        Element disponibiliteElement = doc.createElement("DisponibiliteTechnicien");
        
        appendElement(doc, disponibiliteElement, "technicienId", technicienId.toString());
        appendElement(doc, disponibiliteElement, "date", date);
        
        Element creneauxElement = doc.createElement("creneaux");
        
        // Marquer tous les créneaux comme indisponibles
        String[][] creneauxHoraires = {
            {"08:00", "12:00"},
            {"14:00", "18:00"}
        };
        
        for (String[] horaire : creneauxHoraires) {
            Element creneauElement = doc.createElement("Creneau");
            appendElement(doc, creneauElement, "debut", horaire[0]);
            appendElement(doc, creneauElement, "fin", horaire[1]);
            appendElement(doc, creneauElement, "disponible", "false");
            appendElement(doc, creneauElement, "interventionId", interventionId != null ? interventionId.toString() : "");
            creneauxElement.appendChild(creneauElement);
        }
        
        disponibiliteElement.appendChild(creneauxElement);
        return disponibiliteElement;
    }

    private void updateCreneauxIndisponibles(Document doc, Element disponibiliteElement, 
                                           String raison, Integer interventionId) {
        NodeList creneauNodes = disponibiliteElement.getElementsByTagName("Creneau");
        for (int i = 0; i < creneauNodes.getLength(); i++) {
            Element creneauElement = (Element) creneauNodes.item(i);
            
            // Mettre à jour le créneau comme indisponible
            NodeList disponibleNodes = creneauElement.getElementsByTagName("disponible");
            if (disponibleNodes.getLength() > 0) {
                disponibleNodes.item(0).setTextContent("false");
            } else {
                appendElement(doc, creneauElement, "disponible", "false");
            }
            
            // Ajouter l'ID de l'intervention
            NodeList interventionIdNodes = creneauElement.getElementsByTagName("interventionId");
            if (interventionIdNodes.getLength() > 0) {
                interventionIdNodes.item(0).setTextContent(interventionId != null ? interventionId.toString() : "");
            } else {
                appendElement(doc, creneauElement, "interventionId", interventionId != null ? interventionId.toString() : "");
            }
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

    private boolean isDateInPeriod(String date, String startDate, String endDate) {
        return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
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