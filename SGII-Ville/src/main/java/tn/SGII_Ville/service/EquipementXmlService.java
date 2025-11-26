package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import tn.SGII_Ville.entities.Equipement;
import tn.SGII_Ville.entities.Localisation;
import tn.SGII_Ville.entities.PeriodeIndisponibilite;

import java.util.ArrayList;
import java.util.List;

@Service
public class EquipementXmlService {

    @Autowired
    private XmlService xmlService;

    public List<Equipement> getAllEquipements() {
        List<Equipement> equipements = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("Equipements");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Equipement");
            for (int i = 0; i < nodes.getLength(); i++) {
                equipements.add(parseEquipement((Element) nodes.item(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return equipements;
    }

    public Equipement findById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Equipements");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Equipement");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    return parseEquipement(el);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Equipement parseEquipement(Element equipementElement) {
        try {
            Equipement equipement = new Equipement();
            
            // Données de base
            equipement.setId(Integer.parseInt(xmlService.getElementTextContent(equipementElement, "id")));
            equipement.setNom(xmlService.getElementTextContent(equipementElement, "nom"));
            equipement.setType(xmlService.getElementTextContent(equipementElement, "type"));
            equipement.setEtat(xmlService.getElementTextContent(equipementElement, "etat"));
            
            // Fournisseur
            try {
                equipement.setFournisseurId(Integer.parseInt(xmlService.getElementTextContent(equipementElement, "fournisseurId")));
            } catch (Exception e) {
                equipement.setFournisseurId(null);
            }
            
            // Valeur d'achat
            try {
                equipement.setValeurAchat(Double.parseDouble(xmlService.getElementTextContent(equipementElement, "valeurAchat")));
            } catch (Exception e) {
                equipement.setValeurAchat(0.0);
            }
            
            // Localisation
            NodeList localisationNodes = equipementElement.getElementsByTagNameNS(xmlService.getNamespaceUri(), "localisation");
            if (localisationNodes.getLength() > 0) {
                Element locElement = (Element) localisationNodes.item(0);
                Localisation localisation = new Localisation();
                try {
                    localisation.setLatitude(Double.parseDouble(xmlService.getElementTextContent(locElement, "latitude")));
                    localisation.setLongitude(Double.parseDouble(xmlService.getElementTextContent(locElement, "longitude")));
                } catch (Exception e) {
                    // Ignorer si erreur de parsing
                }
                equipement.setLocalisation(localisation);
            }
            
            // Date d'achat
            equipement.setDateAchat(xmlService.getElementTextContent(equipementElement, "dateAchat"));
            
            // Disponibilité
            try {
                equipement.setDisponible(Boolean.parseBoolean(xmlService.getElementTextContent(equipementElement, "disponible")));
            } catch (Exception e) {
                equipement.setDisponible(true);
            }
            
            // Périodes d'indisponibilité
            NodeList indisponibilitesNodes = equipementElement.getElementsByTagNameNS(xmlService.getNamespaceUri(), "indisponibilites");
            if (indisponibilitesNodes.getLength() > 0) {
                Element indispoElement = (Element) indisponibilitesNodes.item(0);
                NodeList periodeNodes = indispoElement.getElementsByTagNameNS(xmlService.getNamespaceUri(), "periode");
                List<PeriodeIndisponibilite> periodes = new ArrayList<>();
                
                for (int i = 0; i < periodeNodes.getLength(); i++) {
                    Element periodeElement = (Element) periodeNodes.item(i);
                    PeriodeIndisponibilite periode = new PeriodeIndisponibilite();
                    periode.setDebut(xmlService.getElementTextContent(periodeElement, "debut"));
                    periode.setFin(xmlService.getElementTextContent(periodeElement, "fin"));
                    periodes.add(periode);
                }
                equipement.setIndisponibilites(periodes);
            }

            return equipement;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Equipement save(Equipement equipement) {
        try {
            Document doc = xmlService.loadXmlDocument("Equipements");
            Element root = doc.getDocumentElement();

            int newId = generateNewId(doc);
            equipement.setId(newId);

            Element equipementEl = doc.createElementNS(xmlService.getNamespaceUri(), "Equipement");

            // Données de base
            xmlService.addTextElement(doc, equipementEl, "id", String.valueOf(newId));
            xmlService.addTextElement(doc, equipementEl, "nom", equipement.getNom());
            xmlService.addTextElement(doc, equipementEl, "type", equipement.getType());
            xmlService.addTextElement(doc, equipementEl, "etat", equipement.getEtat());
            
            // Fournisseur
            if (equipement.getFournisseurId() != null) {
                xmlService.addTextElement(doc, equipementEl, "fournisseurId", String.valueOf(equipement.getFournisseurId()));
            }
            
            // Valeur d'achat
            if (equipement.getValeurAchat() != null) {
                xmlService.addTextElement(doc, equipementEl, "valeurAchat", String.valueOf(equipement.getValeurAchat()));
            }
            
            // Localisation
            if (equipement.getLocalisation() != null) {
                Element localisationEl = doc.createElementNS(xmlService.getNamespaceUri(), "localisation");
                xmlService.addTextElement(doc, localisationEl, "latitude", String.valueOf(equipement.getLocalisation().getLatitude()));
                xmlService.addTextElement(doc, localisationEl, "longitude", String.valueOf(equipement.getLocalisation().getLongitude()));
                equipementEl.appendChild(localisationEl);
            }
            
            // Date d'achat
            if (equipement.getDateAchat() != null) {
                xmlService.addTextElement(doc, equipementEl, "dateAchat", equipement.getDateAchat());
            }
            
            // Disponibilité (toujours true à la création)
            xmlService.addTextElement(doc, equipementEl, "disponible", "true");
            
            // Périodes d'indisponibilité (vide à la création)
            Element indisponibilitesEl = doc.createElementNS(xmlService.getNamespaceUri(), "indisponibilites");
            equipementEl.appendChild(indisponibilitesEl);

            root.appendChild(equipementEl);
            xmlService.saveXmlDocument(doc, "Equipements");

            return equipement;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur création équipement", e);
        }
    }

    public Equipement update(int id, Equipement equipement) {
        try {
            Document doc = xmlService.loadXmlDocument("Equipements");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Equipement");
            
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    
                    // Mettre à jour les champs de base
                    updateElementTextContent(el, "nom", equipement.getNom());
                    updateElementTextContent(el, "type", equipement.getType());
                    updateElementTextContent(el, "etat", equipement.getEtat());
                    
                    // Fournisseur
                    if (equipement.getFournisseurId() != null) {
                        updateElementTextContent(el, "fournisseurId", String.valueOf(equipement.getFournisseurId()));
                    }
                    
                    // Valeur d'achat
                    if (equipement.getValeurAchat() != null) {
                        updateElementTextContent(el, "valeurAchat", String.valueOf(equipement.getValeurAchat()));
                    }
                    
                    // Date d'achat
                    if (equipement.getDateAchat() != null) {
                        updateElementTextContent(el, "dateAchat", equipement.getDateAchat());
                    }
                    
                    // Disponibilité
                    if (equipement.getDisponible() != null) {
                        updateElementTextContent(el, "disponible", String.valueOf(equipement.getDisponible()));
                    }

                    xmlService.saveXmlDocument(doc, "Equipements");
                    return equipement;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean delete(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Equipements");
            boolean deleted = xmlService.deleteElementById(doc, "Equipements", id);
            if (deleted) {
                xmlService.saveXmlDocument(doc, "Equipements");
            }
            return deleted;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateElementTextContent(Element parent, String tagName, String textContent) {
        NodeList nodes = parent.getElementsByTagNameNS(xmlService.getNamespaceUri(), tagName);
        if (nodes.getLength() > 0) {
            nodes.item(0).setTextContent(textContent);
        } else {
            // Si l'élément n'existe pas, le créer
            Element newElement = parent.getOwnerDocument().createElementNS(xmlService.getNamespaceUri(), tagName);
            newElement.setTextContent(textContent);
            parent.appendChild(newElement);
        }
    }

    private int generateNewId(Document doc) {
        return xmlService.generateNewId(doc, "Equipements");
    }
}