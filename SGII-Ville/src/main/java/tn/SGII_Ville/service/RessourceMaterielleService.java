package tn.SGII_Ville.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import tn.SGII_Ville.entities.Fournisseur;
import tn.SGII_Ville.entities.RessourceMaterielle;

import jakarta.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class RessourceMaterielleService {

    private static final String XML_PATH = "src/main/resources/data/ressources.xml";
    private Document document;

    @PostConstruct
    public void init() {
        try {
            loadXML();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadXML() throws ParserConfigurationException, IOException, SAXException {
        File xmlFile = new File(XML_PATH);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        if (!xmlFile.exists()) {
            document = builder.newDocument();
            Element root = document.createElementNS("http://example.com/gestion-interventions", "RessourcesMaterielles");
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

    // ============================================================
    // ===============   LECTURE (GET)   ==========================
    // ============================================================

    public List<RessourceMaterielle> getAll() {
        List<RessourceMaterielle> list = new ArrayList<>();
        NodeList nodes = document.getElementsByTagName("id");

        Node root = document.getDocumentElement();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                list.add(parseRessource((Element) n));
            }
        }
        return list;
    }

    public RessourceMaterielle getById(int id) {
        Node root = document.getDocumentElement();
        NodeList items = root.getChildNodes();

        for (int i = 0; i < items.getLength(); i++) {
            Node n = items.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                if (Integer.parseInt(e.getElementsByTagName("id").item(0).getTextContent()) == id) {
                    return parseRessource(e);
                }
            }
        }
        return null;
    }

    private RessourceMaterielle parseRessource(Element e) {
        RessourceMaterielle r = new RessourceMaterielle();

        r.setId(Integer.parseInt(e.getElementsByTagName("id").item(0).getTextContent()));
        r.setDesignation(e.getElementsByTagName("designation").item(0).getTextContent());
        r.setQuantiteEnStock(Integer.parseInt(e.getElementsByTagName("quantiteEnStock").item(0).getTextContent()));
        r.setValeurAchat(new BigDecimal(e.getElementsByTagName("valeurAchat").item(0).getTextContent()));

        // Fournisseur ID (clé étrangère)
        NodeList fournisseurIdNodes = e.getElementsByTagName("fournisseurId");
        if (fournisseurIdNodes.getLength() > 0) {
            r.setFournisseurId(Integer.parseInt(fournisseurIdNodes.item(0).getTextContent()));
        }

        // Unité (optionnel)
        NodeList uniteNodes = e.getElementsByTagName("unite");
        if (uniteNodes.getLength() > 0) {
            r.setUnite(uniteNodes.item(0).getTextContent());
        }

        return r;
    }

    // ============================================================
    // ===============   CRÉATION (POST)   ========================
    // ============================================================

    public RessourceMaterielle create(RessourceMaterielle r) {
        Element root = document.getDocumentElement();

        Element item = document.createElement("RessourceMaterielle");

        appendElement(item, "id", String.valueOf(r.getId()));
        appendElement(item, "designation", r.getDesignation());
        appendElement(item, "quantiteEnStock", String.valueOf(r.getQuantiteEnStock()));
        appendElement(item, "valeurAchat", r.getValeurAchat().toString());

        // Fournisseur ID (clé étrangère)
        if (r.getFournisseurId() != null) {
            appendElement(item, "fournisseurId", String.valueOf(r.getFournisseurId()));
        }

        // Unité (optionnel)
        if (r.getUnite() != null) {
            appendElement(item, "unite", r.getUnite());
        }

        root.appendChild(item);

        saveXML();
        return r;
    }

    // ============================================================
    // ===============   MISE À JOUR (PUT)   =====================
    // ============================================================

    public RessourceMaterielle update(int id, RessourceMaterielle updated) {
        Node root = document.getDocumentElement();
        NodeList nodes = root.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                int currentId = Integer.parseInt(e.getElementsByTagName("id").item(0).getTextContent());

                if (currentId == id) {
                    e.getElementsByTagName("designation").item(0).setTextContent(updated.getDesignation());
                    e.getElementsByTagName("quantiteEnStock").item(0).setTextContent(String.valueOf(updated.getQuantiteEnStock()));
                    e.getElementsByTagName("valeurAchat").item(0).setTextContent(updated.getValeurAchat().toString());

                    // Fournisseur ID
                    if (updated.getFournisseurId() != null) {
                        NodeList fournIdNodes = e.getElementsByTagName("fournisseurId");
                        if (fournIdNodes.getLength() > 0) {
                            fournIdNodes.item(0).setTextContent(String.valueOf(updated.getFournisseurId()));
                        } else {
                            Element fournIdEl = document.createElement("fournisseurId");
                            fournIdEl.setTextContent(String.valueOf(updated.getFournisseurId()));
                            e.appendChild(fournIdEl);
                        }
                    }

                    // Unité
                    if (updated.getUnite() != null) {
                        NodeList uniteNodes = e.getElementsByTagName("unite");
                        if (uniteNodes.getLength() > 0) {
                            uniteNodes.item(0).setTextContent(updated.getUnite());
                        } else {
                            Element uniteEl = document.createElement("unite");
                            uniteEl.setTextContent(updated.getUnite());
                            e.appendChild(uniteEl);
                        }
                    }

                    saveXML();
                    return updated;
                }
            }
        }

        return null;
    }

    // ============================================================
    // ==================   SUPPRESSION (DELETE)   ================
    // ============================================================

    public void delete(int id) {
        Node root = document.getDocumentElement();
        NodeList nodes = root.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;

                int currentId = Integer.parseInt(e.getElementsByTagName("id").item(0).getTextContent());

                if (currentId == id) {
                    root.removeChild(e);
                    saveXML();
                    return;
                }
            }
        }
    }

    private void appendElement(Element parent, String tag, String value) {
        Element el = document.createElement(tag);
        el.setTextContent(value);
        parent.appendChild(el);
    }
}
