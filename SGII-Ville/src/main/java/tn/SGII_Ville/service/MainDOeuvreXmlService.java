package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.SGII_Ville.entities.MainDOeuvre;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainDOeuvreXmlService {

    private static final Logger logger = LoggerFactory.getLogger(MainDOeuvreXmlService.class);

    @Autowired
    private XmlService xmlService;

    public List<MainDOeuvre> findAll() {
        List<MainDOeuvre> list = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("MainDOeuvre");
            if (doc == null) {
                logger.warn("Document XML MainDOeuvre est null");
                return list;
            }
            
            Element root = doc.getDocumentElement();
            if (root == null) {
                logger.warn("Document XML MainDOeuvre n'a pas d'élément racine");
                return list;
            }
            
            NodeList nodes = root.getElementsByTagNameNS(xmlService.getNamespaceUri(), "MainDOeuvre");
            if (nodes == null) {
                logger.warn("NodeList est null");
                return list;
            }

            for (int i = 0; i < nodes.getLength(); i++) {
                try {
                    Node node = nodes.item(i);
                    if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
                        Element el = (Element) node;
                        MainDOeuvre md = parseMainDOeuvre(el);
                        if (md != null) {
                            list.add(md);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Erreur lors du parsing d'un élément MainDOeuvre à l'index {}", i, e);
                    // Continuer avec les autres éléments
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la main-d'œuvre", e);
            // Retourner une liste vide au lieu de propager l'exception
        }
        return list;
    }

    public List<MainDOeuvre> findActive() {
        try {
            List<MainDOeuvre> all = findAll();
            return all.stream()
                    .filter(m -> m != null && !"ARCHIVE".equals(m.getDisponibilite()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Erreur lors de la recherche de main-d'œuvre active", e);
            return new ArrayList<>();
        }
    }

    public MainDOeuvre findById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("MainDOeuvre");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "MainDOeuvre");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    return parseMainDOeuvre(el);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de la main-d'œuvre ID: {}", id, e);
        }
        return null;
    }

    public MainDOeuvre save(MainDOeuvre mainDOeuvre) throws Exception {
        Document doc;
        try {
            doc = xmlService.loadXmlDocument("MainDOeuvre");
        } catch (Exception e) {
            doc = xmlService.createNewDocument("MainDOeuvre");
        }

        Element root = doc.getDocumentElement();
        if (root == null) {
            root = doc.createElementNS(xmlService.getNamespaceUri(), "MainDOeuvre");
            doc.appendChild(root);
        }

        // Si nouvel ID, générer un ID
        if (mainDOeuvre.getId() == 0) {
            int newId = xmlService.generateNewId(doc, "MainDOeuvre");
            mainDOeuvre.setId(newId);
        } else {
            // Mettre à jour l'existant
            NodeList nodes = root.getElementsByTagNameNS(xmlService.getNamespaceUri(), "MainDOeuvre");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == mainDOeuvre.getId()) {
                    root.removeChild(el);
                    break;
                }
            }
        }

        // Créer l'élément
        Element mainDOeuvreEl = xmlService.createElement(doc, "MainDOeuvre");
        
        // Champs de UtilisateurType (base)
        xmlService.addTextElement(doc, mainDOeuvreEl, "id", String.valueOf(mainDOeuvre.getId()));
        xmlService.addTextElement(doc, mainDOeuvreEl, "nom", mainDOeuvre.getNom());
        xmlService.addTextElement(doc, mainDOeuvreEl, "email", mainDOeuvre.getEmail());
        xmlService.addTextElement(doc, mainDOeuvreEl, "motDePasse", mainDOeuvre.getMotDePasse());
        xmlService.addTextElement(doc, mainDOeuvreEl, "role", mainDOeuvre.getRole().name());
        
        // Champs spécifiques MainDOeuvreType
        xmlService.addTextElement(doc, mainDOeuvreEl, "prenom", mainDOeuvre.getPrenom());
        xmlService.addTextElement(doc, mainDOeuvreEl, "matricule", mainDOeuvre.getMatricule());
        xmlService.addTextElement(doc, mainDOeuvreEl, "cin", mainDOeuvre.getCin());
        xmlService.addTextElement(doc, mainDOeuvreEl, "telephone", mainDOeuvre.getTelephone());
        xmlService.addTextElement(doc, mainDOeuvreEl, "disponibilite", 
            mainDOeuvre.getDisponibilite() != null ? mainDOeuvre.getDisponibilite() : "LIBRE");
        
        // Compétence unique (obligatoire)
        xmlService.addTextElement(doc, mainDOeuvreEl, "competence", 
            mainDOeuvre.getCompetence() != null ? mainDOeuvre.getCompetence() : "");

        root.appendChild(mainDOeuvreEl);
        xmlService.saveXmlDocument(doc, "MainDOeuvre");

        return mainDOeuvre;
    }

    public boolean delete(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("MainDOeuvre");
            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagNameNS(xmlService.getNamespaceUri(), "MainDOeuvre");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    root.removeChild(el);
                    xmlService.saveXmlDocument(doc, "MainDOeuvre");
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de la main-d'œuvre ID: {}", id, e);
        }
        return false;
    }

    public List<MainDOeuvre> findByCompetence(String competence) {
        return findActive().stream()
                .filter(m -> m.getCompetence() != null && m.getCompetence().equals(competence))
                .collect(Collectors.toList());
    }

    public List<MainDOeuvre> findByDisponibilite(String disponibilite) {
        // Utiliser findActive() pour ne retourner que les main-d'œuvre actives
        return findActive().stream()
                .filter(m -> disponibilite != null && disponibilite.equals(m.getDisponibilite()))
                .collect(Collectors.toList());
    }
/**
 * Génère un nouvel ID pour les main d'œuvre
 */
/**
 * Génère un nouvel ID pour les main d'œuvre
 */
public int generateNewId() {
    try {
        Document doc = xmlService.loadXmlDocument("MainDOeuvre");
        return xmlService.generateNewId(doc, "MainDOeuvre");
    } catch (Exception e) {
        e.printStackTrace();
        return 1; // ID par défaut en cas d'erreur
    }
}
    private MainDOeuvre parseMainDOeuvre(Element el) {
        MainDOeuvre m = new MainDOeuvre();
        try {
            // Champs de UtilisateurType (base)
            m.setId(Integer.parseInt(xmlService.getElementTextContent(el, "id")));
            m.setNom(xmlService.getElementTextContent(el, "nom"));
            m.setEmail(xmlService.getElementTextContent(el, "email"));
            m.setMotDePasse(xmlService.getElementTextContent(el, "motDePasse"));
            String roleStr = xmlService.getElementTextContent(el, "role");
            if (roleStr != null) {
                try {
                    m.setRole(tn.SGII_Ville.model.enums.RoleType.valueOf(roleStr));
                } catch (Exception e) {
                    logger.warn("Erreur parsing rôle: {}", roleStr);
                    m.setRole(tn.SGII_Ville.model.enums.RoleType.MAIN_DOEUVRE);
                }
            }
            
            // Champs spécifiques MainDOeuvreType
            m.setPrenom(xmlService.getElementTextContent(el, "prenom"));
            m.setMatricule(xmlService.getElementTextContent(el, "matricule"));
            m.setCin(xmlService.getElementTextContent(el, "cin"));
            m.setTelephone(xmlService.getElementTextContent(el, "telephone"));
            
            String disp = xmlService.getElementTextContent(el, "disponibilite");
            m.setDisponibilite(disp != null ? disp : "LIBRE");
            
            // Compétence unique
            m.setCompetence(xmlService.getElementTextContent(el, "competence"));

        } catch (Exception e) {
            logger.error("Erreur parsing main-d'œuvre", e);
        }
        return m;
    }
}

