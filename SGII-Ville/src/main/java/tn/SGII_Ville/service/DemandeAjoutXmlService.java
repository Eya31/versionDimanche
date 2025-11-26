package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import tn.SGII_Ville.entities.DemandeAjout;
import tn.SGII_Ville.model.enums.EtatDemandeAjoutType;
import tn.SGII_Ville.model.enums.TypeDemandeAjout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DemandeAjoutXmlService {

    @Autowired
    private XmlService xmlService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public List<DemandeAjout> getAllDemandesAjout() {
        List<DemandeAjout> demandes = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("DemandesAjout");
            
            // Méthode plus robuste pour trouver les éléments
            NodeList nodes = doc.getDocumentElement().getChildNodes();
            
            System.out.println("📝 Chargement des demandes - " + nodes.getLength() + " nœuds trouvés");
            
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE && 
                    "DemandeAjout".equals(node.getNodeName())) {
                    demandes.add(parseDemandeAjout((Element) node));
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des demandes: " + e.getMessage());
            e.printStackTrace();
        }
        return demandes;
    }


    public Optional<DemandeAjout> findById(int id) {
        try {
            List<DemandeAjout> allDemandes = getAllDemandesAjout();
            return allDemandes.stream()
                .filter(d -> d.getId() == id)
                .findFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public List<DemandeAjout> getDemandesByChef(int chefId) {
        return getAllDemandesAjout().stream()
            .filter(d -> d.getChefId() == chefId)
            .toList();
    }

    public List<DemandeAjout> getDemandesEnAttente() {
        return getAllDemandesAjout().stream()
            .filter(d -> d.getEtat() == EtatDemandeAjoutType.EN_ATTENTE_ADMIN)
            .toList();
    }

    private DemandeAjout parseDemandeAjout(Element element) {
        DemandeAjout demande = new DemandeAjout();
        
        try {
            demande.setId(Integer.parseInt(getElementText(element, "id")));
            demande.setTypeDemande(TypeDemandeAjout.valueOf(getElementText(element, "typeDemande")));
            demande.setDesignation(getElementText(element, "designation"));
            demande.setQuantite(Integer.parseInt(getElementText(element, "quantite")));
            demande.setBudget(Double.parseDouble(getElementText(element, "budget")));
            demande.setJustification(getElementText(element, "justification"));
            
            String etatStr = getElementText(element, "etat");
            if (etatStr != null && !etatStr.isEmpty()) {
                demande.setEtat(EtatDemandeAjoutType.valueOf(etatStr));
            }
            
            demande.setChefId(Integer.parseInt(getElementText(element, "chefId")));

            // Champs optionnels
            String adminId = getElementText(element, "adminId");
            if (adminId != null && !adminId.isEmpty()) {
                demande.setAdminId(Integer.parseInt(adminId));
            }

            String dateTraitement = getElementText(element, "dateTraitement");
            if (dateTraitement != null && !dateTraitement.isEmpty()) {
                demande.setDateTraitement(LocalDateTime.parse(dateTraitement, FORMATTER));
            }

            String motifRefus = getElementText(element, "motifRefus");
            if (motifRefus != null) {
                demande.setMotifRefus(motifRefus);
            }

            String dateDemande = getElementText(element, "dateDemande");
            if (dateDemande != null && !dateDemande.isEmpty()) {
                demande.setDateDemande(LocalDateTime.parse(dateDemande, FORMATTER));
            } else {
                demande.setDateDemande(LocalDateTime.now());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing demande: " + e.getMessage());
            e.printStackTrace();
        }
        
        return demande;
    }

private String getElementText(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            String text = nodes.item(0).getTextContent();
            return text != null ? text.trim() : null;
        }
        return null;
    }
   public DemandeAjout save(DemandeAjout demande) {
    try {
        Document doc = xmlService.loadXmlDocument("DemandesAjout");
        Element root = doc.getDocumentElement();

        // Générer nouvel ID si nécessaire
        if (demande.getId() == 0) {
            int newId = generateNewId(doc);
            demande.setId(newId);
        }

        Element demandeElement = doc.createElement("DemandeAjout");

        // Créer les éléments sans namespace
        addTextElementSimple(doc, demandeElement, "id", String.valueOf(demande.getId()));
        addTextElementSimple(doc, demandeElement, "typeDemande", demande.getTypeDemande().name());
        addTextElementSimple(doc, demandeElement, "designation", demande.getDesignation());
        addTextElementSimple(doc, demandeElement, "quantite", String.valueOf(demande.getQuantite()));
        addTextElementSimple(doc, demandeElement, "budget", String.valueOf(demande.getBudget()));
        addTextElementSimple(doc, demandeElement, "justification", demande.getJustification());
        addTextElementSimple(doc, demandeElement, "etat", demande.getEtat().name());
        addTextElementSimple(doc, demandeElement, "dateDemande", 
            demande.getDateDemande() != null ? demande.getDateDemande().format(FORMATTER) : LocalDateTime.now().format(FORMATTER));
        addTextElementSimple(doc, demandeElement, "chefId", String.valueOf(demande.getChefId()));

        // Champs optionnels
        if (demande.getAdminId() != null) {
            addTextElementSimple(doc, demandeElement, "adminId", String.valueOf(demande.getAdminId()));
        }
        if (demande.getDateTraitement() != null) {
            addTextElementSimple(doc, demandeElement, "dateTraitement", demande.getDateTraitement().format(FORMATTER));
        }
        if (demande.getMotifRefus() != null) {
            addTextElementSimple(doc, demandeElement, "motifRefus", demande.getMotifRefus());
        }

        root.appendChild(demandeElement);
        xmlService.saveXmlDocument(doc, "DemandesAjout");
        
        System.out.println("✅ Demande sauvegardée avec ID: " + demande.getId());
        return demande;
    } catch (Exception e) {
        System.err.println("❌ Erreur lors de la sauvegarde de la demande: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Erreur lors de la sauvegarde de la demande", e);
    }
}
private void addTextElementSimple(Document doc, Element parent, String tagName, String textContent) {
    Element element = doc.createElement(tagName);
    element.setTextContent(textContent);
    parent.appendChild(element);
}

    public DemandeAjout update(DemandeAjout demande) {
        try {
            Document doc = xmlService.loadXmlDocument("DemandesAjout");
            Element root = doc.getDocumentElement();
            
            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE && 
                    "DemandeAjout".equals(node.getNodeName())) {
                    
                    Element el = (Element) node;
                    if (Integer.parseInt(getElementText(el, "id")) == demande.getId()) {
                        // Mettre à jour l'élément existant
                        updateElementText(el, "typeDemande", demande.getTypeDemande().name());
                        updateElementText(el, "designation", demande.getDesignation());
                        updateElementText(el, "quantite", String.valueOf(demande.getQuantite()));
                        updateElementText(el, "budget", String.valueOf(demande.getBudget()));
                        updateElementText(el, "justification", demande.getJustification());
                        updateElementText(el, "etat", demande.getEtat().name());
                        updateElementText(el, "chefId", String.valueOf(demande.getChefId()));
                        
                        if (demande.getAdminId() != null) {
                            updateElementText(el, "adminId", String.valueOf(demande.getAdminId()));
                        } else {
                            removeElement(el, "adminId");
                        }
                        
                        if (demande.getDateTraitement() != null) {
                            updateElementText(el, "dateTraitement", demande.getDateTraitement().format(FORMATTER));
                        } else {
                            removeElement(el, "dateTraitement");
                        }
                        
                        if (demande.getMotifRefus() != null) {
                            updateElementText(el, "motifRefus", demande.getMotifRefus());
                        } else {
                            removeElement(el, "motifRefus");
                        }

                        xmlService.saveXmlDocument(doc, "DemandesAjout");
                        System.out.println("✅ Demande mise à jour: " + demande.getId());
                        return demande;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour demande: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


      private void updateElementText(Element parent, String tagName, String textContent) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            nodes.item(0).setTextContent(textContent);
        } else {
            // Créer l'élément s'il n'existe pas
            Element newElement = parent.getOwnerDocument().createElement(tagName);
            newElement.setTextContent(textContent);
            parent.appendChild(newElement);
        }
    }

    private int generateNewId(Document doc) {
        int maxId = 0;
        NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "DemandeAjout");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            int id = Integer.parseInt(xmlService.getElementTextContent(el, "id"));
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }
    private void removeElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            parent.removeChild(nodes.item(0));
        }
    }
}