package tn.SGII_Ville.repository;

import org.springframework.stereotype.Repository;
import tn.SGII_Ville.model.Intervention;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

@Repository
public class InterventionRepository {
    private static final String FILE_PATH = "src/main/resources/data/interventions.xml";
    private static final String NAMESPACE_URI = "http://example.com/gestion-interventions";

    public List<Intervention> findAll() {
        List<Intervention> interventions = new ArrayList<>();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return interventions;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList interventionNodes = doc.getElementsByTagNameNS(NAMESPACE_URI, "Intervention");
            for (int i = 0; i < interventionNodes.getLength(); i++) {
                Element interventionElement = (Element) interventionNodes.item(i);
                Intervention intervention = parseIntervention(interventionElement);
                interventions.add(intervention);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return interventions;
    }

    public Optional<Intervention> findById(Integer id) {
        return findAll().stream()
            .filter(i -> i.getId().equals(id))
            .findFirst();
    }

    public List<Intervention> findByDate(String date) {
        return findAll().stream()
            .filter(i -> i.getDatePlanifiee().equals(date))
            .toList();
    }

    public Intervention save(Intervention intervention) {
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
                
                // Vérifier si l'intervention existe déjà
                NodeList interventionNodes = doc.getElementsByTagNameNS(NAMESPACE_URI, "Intervention");
                for (int i = 0; i < interventionNodes.getLength(); i++) {
                    Element interventionElement = (Element) interventionNodes.item(i);
                    Integer currentId = Integer.parseInt(getElementTextNS(interventionElement, "id"));
                    
                    if (currentId.equals(intervention.getId())) {
                        updateInterventionElement(interventionElement, intervention);
                        saveDocument(doc);
                        return intervention;
                    }
                }
            } else {
                doc = builder.newDocument();
                root = doc.createElementNS(NAMESPACE_URI, "Interventions");
                doc.appendChild(root);
            }
            
            Element newIntervention = createInterventionElement(doc, intervention);
            root.appendChild(newIntervention);
            saveDocument(doc);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return intervention;
    }

    // Méthodes privées utilitaires avec namespace
    private Intervention parseIntervention(Element element) {
        Integer id = Integer.parseInt(getElementTextNS(element, "id"));
        String priorite = getElementTextNS(element, "priorite");
        String etat = getElementTextNS(element, "etat");
        String datePlanifiee = getElementTextNS(element, "datePlanifiee");
        Double budget = Double.parseDouble(getElementTextNS(element, "budget"));
        Integer demandeId = Integer.parseInt(getElementTextNS(element, "demandeId"));
        
        // Parser les techniciens
        List<Integer> techniciens = new ArrayList<>();
        Element techniciensElement = getChildElementNS(element, "techniciens");
        if (techniciensElement != null) {
            NodeList technicienNodes = techniciensElement.getElementsByTagNameNS(NAMESPACE_URI, "technicienId");
            for (int i = 0; i < technicienNodes.getLength(); i++) {
                techniciens.add(Integer.parseInt(technicienNodes.item(i).getTextContent()));
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
        
        // Parser les ressources
        List<Integer> ressources = new ArrayList<>();
        Element ressourcesElement = getChildElementNS(element, "ressources");
        if (ressourcesElement != null) {
            NodeList ressourceNodes = ressourcesElement.getElementsByTagNameNS(NAMESPACE_URI, "ressourceId");
            for (int i = 0; i < ressourceNodes.getLength(); i++) {
                ressources.add(Integer.parseInt(ressourceNodes.item(i).getTextContent()));
            }
        }
        
        Intervention intervention = new Intervention();
        intervention.setId(id);
        intervention.setPriorite(priorite);
        intervention.setEtat(etat);
        intervention.setDatePlanifiee(datePlanifiee);
        intervention.setBudget(budget);
        intervention.setDemandeId(demandeId);
        intervention.setTechniciens(techniciens);
        intervention.setEquipements(equipements);
        intervention.setRessources(ressources);
        
        return intervention;
    }

    private Element createInterventionElement(Document doc, Intervention intervention) {
        Element interventionElement = doc.createElementNS(NAMESPACE_URI, "Intervention");
        
        appendElementNS(doc, interventionElement, "id", intervention.getId().toString());
        appendElementNS(doc, interventionElement, "priorite", intervention.getPriorite());
        appendElementNS(doc, interventionElement, "etat", intervention.getEtat());
        appendElementNS(doc, interventionElement, "datePlanifiee", intervention.getDatePlanifiee());
        appendElementNS(doc, interventionElement, "budget", intervention.getBudget().toString());
        appendElementNS(doc, interventionElement, "demandeId", intervention.getDemandeId().toString());
        
        // Ajouter les techniciens
        if (intervention.getTechniciens() != null && !intervention.getTechniciens().isEmpty()) {
            Element techniciensElement = doc.createElementNS(NAMESPACE_URI, "techniciens");
            for (Integer technicienId : intervention.getTechniciens()) {
                appendElementNS(doc, techniciensElement, "technicienId", technicienId.toString());
            }
            interventionElement.appendChild(techniciensElement);
        }
        
        // Ajouter les équipements
        if (intervention.getEquipements() != null && !intervention.getEquipements().isEmpty()) {
            Element equipementsElement = doc.createElementNS(NAMESPACE_URI, "equipements");
            for (Integer equipementId : intervention.getEquipements()) {
                appendElementNS(doc, equipementsElement, "equipementId", equipementId.toString());
            }
            interventionElement.appendChild(equipementsElement);
        }
        
        // Ajouter les ressources
        if (intervention.getRessources() != null && !intervention.getRessources().isEmpty()) {
            Element ressourcesElement = doc.createElementNS(NAMESPACE_URI, "ressources");
            for (Integer ressourceId : intervention.getRessources()) {
                appendElementNS(doc, ressourcesElement, "ressourceId", ressourceId.toString());
            }
            interventionElement.appendChild(ressourcesElement);
        }
        
        return interventionElement;
    }

    private void updateInterventionElement(Element interventionElement, Intervention intervention) {
        updateElementTextNS(interventionElement, "priorite", intervention.getPriorite());
        updateElementTextNS(interventionElement, "etat", intervention.getEtat());
        updateElementTextNS(interventionElement, "datePlanifiee", intervention.getDatePlanifiee());
        updateElementTextNS(interventionElement, "budget", intervention.getBudget().toString());
        updateElementTextNS(interventionElement, "demandeId", intervention.getDemandeId().toString());
        
        updateListElementsNS(interventionElement, "techniciens", "technicienId", intervention.getTechniciens());
        updateListElementsNS(interventionElement, "equipements", "equipementId", intervention.getEquipements());
        updateListElementsNS(interventionElement, "ressources", "ressourceId", intervention.getRessources());
    }

    // Méthodes utilitaires namespace (identique à TechnicienRepository)
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