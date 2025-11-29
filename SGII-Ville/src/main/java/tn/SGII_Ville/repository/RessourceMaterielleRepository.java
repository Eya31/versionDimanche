package tn.SGII_Ville.repository;

import org.springframework.stereotype.Repository;
import tn.SGII_Ville.entities.RessourceMaterielle;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

@Repository
public class RessourceMaterielleRepository {
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

    public List<RessourceMaterielle> findByDesignation(String designation) {
        return findAll().stream()
            .filter(r -> r.getDesignation().equalsIgnoreCase(designation))
            .toList();
    }

    private RessourceMaterielle parseRessource(Element element) {
        Integer id = getIntElement(element, "id");
        String designation = getElementText(element, "designation");
        Integer quantiteEnStock = getIntElement(element, "quantiteEnStock");
        Double valeurAchat = getDoubleElement(element, "valeurAchat");
        Integer fournisseurId = getIntElement(element, "fournisseurId");
        String unite = getElementText(element, "unite");
        
        RessourceMaterielle ressource = new RessourceMaterielle();
        ressource.setId(id);
        ressource.setDesignation(designation);
        ressource.setQuantiteEnStock(quantiteEnStock);
        ressource.setValeurAchat(valeurAchat);
        ressource.setFournisseurId(fournisseurId);
        ressource.setUnite(unite);
        
        return ressource;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }

    private Integer getIntElement(Element parent, String tagName) {
        String text = getElementText(parent, tagName);
        try {
            return text.isEmpty() ? 0 : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Double getDoubleElement(Element parent, String tagName) {
        String text = getElementText(parent, tagName);
        try {
            return text.isEmpty() ? 0.0 : Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
