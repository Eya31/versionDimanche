package tn.SGII_Ville.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import tn.SGII_Ville.entities.RessourceMaterielle;

import org.springframework.beans.factory.annotation.Autowired;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class RessourceMaterielleService {

    private static final String XML_PATH = "src/main/resources/data/ressources.xml";
    private static final String NAMESPACE_URI = "http://example.com/gestion-interventions";
    private Document document;

    @Autowired
    public void init() {
        try {
            loadXML();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadXML() throws Exception {
        File xmlFile = new File(XML_PATH);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        if (!xmlFile.exists()) {
            document = builder.newDocument();
            Element root = document.createElementNS(NAMESPACE_URI, "RessourcesMaterielles");
            document.appendChild(root);
            saveXML();
        } else {
            document = builder.parse(xmlFile);
        }
    }

    private void saveXML() {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(XML_PATH));
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new RuntimeException("Erreur écriture XML", e);
        }
    }

    public List<RessourceMaterielle> getAll() {
        List<RessourceMaterielle> list = new ArrayList<>();
        NodeList nodes = document.getElementsByTagNameNS(NAMESPACE_URI, "RessourceMaterielle");
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                list.add(parseRessource((Element) n));
            }
        }
        return list;
    }

    public RessourceMaterielle getById(int id) {
        NodeList nodes = document.getElementsByTagNameNS(NAMESPACE_URI, "RessourceMaterielle");
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            if (Integer.parseInt(getElementTextContent(e, "id")) == id) {
                return parseRessource(e);
            }
        }
        return null;
    }

    private RessourceMaterielle parseRessource(Element e) {
        RessourceMaterielle r = new RessourceMaterielle();

        r.setId(Integer.parseInt(getElementTextContent(e, "id")));
        r.setDesignation(getElementTextContent(e, "designation"));
        r.setQuantiteEnStock(Integer.parseInt(getElementTextContent(e, "quantiteEnStock")));
        
        // Valeur d'achat
        try {
            r.setValeurAchat(Double.parseDouble(getElementTextContent(e, "valeurAchat")));
        } catch (Exception ex) {
            r.setValeurAchat(0.0);
        }
        
        // Fournisseur
        try {
            r.setFournisseurId(Integer.parseInt(getElementTextContent(e, "fournisseurId")));
        } catch (Exception ex) {
            r.setFournisseurId(null);
        }
        
        // Unité
        r.setUnite(getElementTextContent(e, "unite"));

        return r;
    }

    public RessourceMaterielle create(RessourceMaterielle r) {
        Element root = document.getDocumentElement();

        Element item = document.createElementNS(NAMESPACE_URI, "RessourceMaterielle");

        appendElement(item, "id", String.valueOf(generateNewId()));
        appendElement(item, "designation", r.getDesignation());
        appendElement(item, "quantiteEnStock", String.valueOf(r.getQuantiteEnStock()));
        appendElement(item, "valeurAchat", String.valueOf(r.getValeurAchat()));
        
        if (r.getFournisseurId() != null) {
            appendElement(item, "fournisseurId", String.valueOf(r.getFournisseurId()));
        }
        
        if (r.getUnite() != null) {
            appendElement(item, "unite", r.getUnite());
        }

        root.appendChild(item);
        saveXML();
        return r;
    }

    public RessourceMaterielle update(int id, RessourceMaterielle updated) {
        NodeList nodes = document.getElementsByTagNameNS(NAMESPACE_URI, "RessourceMaterielle");
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            int currentId = Integer.parseInt(getElementTextContent(e, "id"));

            if (currentId == id) {
                updateElementTextContent(e, "designation", updated.getDesignation());
                updateElementTextContent(e, "quantiteEnStock", String.valueOf(updated.getQuantiteEnStock()));
                updateElementTextContent(e, "valeurAchat", String.valueOf(updated.getValeurAchat()));
                
                if (updated.getFournisseurId() != null) {
                    updateElementTextContent(e, "fournisseurId", String.valueOf(updated.getFournisseurId()));
                }
                
                if (updated.getUnite() != null) {
                    updateElementTextContent(e, "unite", updated.getUnite());
                }

                saveXML();
                return updated;
            }
        }
        return null;
    }

    public void delete(int id) {
        NodeList nodes = document.getElementsByTagNameNS(NAMESPACE_URI, "RessourceMaterielle");
        Element root = document.getDocumentElement();
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            int currentId = Integer.parseInt(getElementTextContent(e, "id"));

            if (currentId == id) {
                root.removeChild(e);
                saveXML();
                return;
            }
        }
    }

    private void appendElement(Element parent, String tag, String value) {
        Element el = document.createElementNS(NAMESPACE_URI, tag);
        el.setTextContent(value);
        parent.appendChild(el);
    }

    private String getElementTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }

    private void updateElementTextContent(Element parent, String tagName, String textContent) {
        NodeList nodes = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
        if (nodes.getLength() > 0) {
            nodes.item(0).setTextContent(textContent);
        } else {
            // Créer l'élément s'il n'existe pas
            appendElement(parent, tagName, textContent);
        }
    }

    private int generateNewId() {
        NodeList nodes = document.getElementsByTagNameNS(NAMESPACE_URI, "RessourceMaterielle");
        int maxId = 0;
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            int id = Integer.parseInt(getElementTextContent(e, "id"));
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }
}