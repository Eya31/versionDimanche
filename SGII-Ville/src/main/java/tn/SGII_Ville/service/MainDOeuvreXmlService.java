package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.SGII_Ville.entities.MainDOeuvre;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    .filter(m -> m != null && m.isActive() && !"ARCHIVE".equals(m.getDisponibilite()) && !"DESACTIVE".equals(m.getDisponibilite()))
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
        xmlService.addTextElement(doc, mainDOeuvreEl, "id", String.valueOf(mainDOeuvre.getId()));
        xmlService.addTextElement(doc, mainDOeuvreEl, "nom", mainDOeuvre.getNom());
        if (mainDOeuvre.getPrenom() != null) {
            xmlService.addTextElement(doc, mainDOeuvreEl, "prenom", mainDOeuvre.getPrenom());
        }
        if (mainDOeuvre.getMatricule() != null) {
            xmlService.addTextElement(doc, mainDOeuvreEl, "matricule", mainDOeuvre.getMatricule());
        }
        xmlService.addTextElement(doc, mainDOeuvreEl, "cin", mainDOeuvre.getCin());
        xmlService.addTextElement(doc, mainDOeuvreEl, "telephone", mainDOeuvre.getTelephone());
        if (mainDOeuvre.getEmail() != null) {
            xmlService.addTextElement(doc, mainDOeuvreEl, "email", mainDOeuvre.getEmail());
        }
        if (mainDOeuvre.getMetier() != null) {
            xmlService.addTextElement(doc, mainDOeuvreEl, "metier", mainDOeuvre.getMetier());
        }
        if (mainDOeuvre.getDisponibilite() != null) {
            xmlService.addTextElement(doc, mainDOeuvreEl, "disponibilite", mainDOeuvre.getDisponibilite());
        }
        xmlService.addTextElement(doc, mainDOeuvreEl, "active", String.valueOf(mainDOeuvre.isActive()));
        if (mainDOeuvre.getPhotoPath() != null) {
            xmlService.addTextElement(doc, mainDOeuvreEl, "photoPath", mainDOeuvre.getPhotoPath());
        }

        // Compétences
        if (mainDOeuvre.getCompetences() != null && !mainDOeuvre.getCompetences().isEmpty()) {
            Element competencesEl = doc.createElementNS(xmlService.getNamespaceUri(), "competences");
            for (String comp : mainDOeuvre.getCompetences()) {
                Element compEl = doc.createElementNS(xmlService.getNamespaceUri(), "competence");
                compEl.setTextContent(comp);
                competencesEl.appendChild(compEl);
            }
            mainDOeuvreEl.appendChild(competencesEl);
        }

        // Habilitations
        if (mainDOeuvre.getHabilitations() != null && !mainDOeuvre.getHabilitations().isEmpty()) {
            Element habilitationsEl = doc.createElementNS(xmlService.getNamespaceUri(), "habilitations");
            for (String hab : mainDOeuvre.getHabilitations()) {
                Element habEl = doc.createElementNS(xmlService.getNamespaceUri(), "habilitation");
                habEl.setTextContent(hab);
                
                // Ajouter date d'expiration si disponible
                if (mainDOeuvre.getHabilitationsExpiration() != null && 
                    mainDOeuvre.getHabilitationsExpiration().containsKey(hab)) {
                    java.time.LocalDate expiration = mainDOeuvre.getHabilitationsExpiration().get(hab);
                    if (expiration != null) {
                        xmlService.addTextElement(doc, habEl, "dateExpiration", expiration.toString());
                    }
                }
                
                habilitationsEl.appendChild(habEl);
            }
            mainDOeuvreEl.appendChild(habilitationsEl);
        }

        // Horaires de travail
        if (mainDOeuvre.getHorairesTravail() != null && !mainDOeuvre.getHorairesTravail().isEmpty()) {
            Element horairesEl = doc.createElementNS(xmlService.getNamespaceUri(), "horairesTravail");
            for (Map.Entry<String, String> entry : mainDOeuvre.getHorairesTravail().entrySet()) {
                Element jourEl = doc.createElementNS(xmlService.getNamespaceUri(), "jour");
                jourEl.setAttribute("nom", entry.getKey());
                jourEl.setTextContent(entry.getValue());
                horairesEl.appendChild(jourEl);
            }
            mainDOeuvreEl.appendChild(horairesEl);
        }

        // Congés
        if (mainDOeuvre.getConges() != null && !mainDOeuvre.getConges().isEmpty()) {
            Element congesEl = doc.createElementNS(xmlService.getNamespaceUri(), "conges");
            for (java.time.LocalDate date : mainDOeuvre.getConges()) {
                Element congeEl = doc.createElementNS(xmlService.getNamespaceUri(), "conge");
                congeEl.setTextContent(date.toString());
                congesEl.appendChild(congeEl);
            }
            mainDOeuvreEl.appendChild(congesEl);
        }

        // Absences
        if (mainDOeuvre.getAbsences() != null && !mainDOeuvre.getAbsences().isEmpty()) {
            Element absencesEl = doc.createElementNS(xmlService.getNamespaceUri(), "absences");
            for (java.time.LocalDate date : mainDOeuvre.getAbsences()) {
                Element absenceEl = doc.createElementNS(xmlService.getNamespaceUri(), "absence");
                absenceEl.setTextContent(date.toString());
                absencesEl.appendChild(absenceEl);
            }
            mainDOeuvreEl.appendChild(absencesEl);
        }

        // Historique interventions
        if (mainDOeuvre.getHistoriqueInterventionIds() != null && !mainDOeuvre.getHistoriqueInterventionIds().isEmpty()) {
            Element historiqueEl = doc.createElementNS(xmlService.getNamespaceUri(), "historiqueInterventions");
            for (Integer interventionId : mainDOeuvre.getHistoriqueInterventionIds()) {
                Element intervEl = doc.createElementNS(xmlService.getNamespaceUri(), "interventionId");
                intervEl.setTextContent(String.valueOf(interventionId));
                historiqueEl.appendChild(intervEl);
            }
            mainDOeuvreEl.appendChild(historiqueEl);
        }

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
                .filter(m -> m.getCompetences().contains(competence))
                .collect(Collectors.toList());
    }

    public List<MainDOeuvre> findByDisponibilite(String disponibilite) {
        return findAll().stream()
                .filter(m -> disponibilite.equals(m.getDisponibilite()))
                .collect(Collectors.toList());
    }

    private MainDOeuvre parseMainDOeuvre(Element el) {
        MainDOeuvre m = new MainDOeuvre();
        try {
            m.setId(Integer.parseInt(xmlService.getElementTextContent(el, "id")));
            m.setNom(xmlService.getElementTextContent(el, "nom"));
            m.setPrenom(xmlService.getElementTextContent(el, "prenom"));
            m.setMatricule(xmlService.getElementTextContent(el, "matricule"));
            m.setCin(xmlService.getElementTextContent(el, "cin"));
            m.setTelephone(xmlService.getElementTextContent(el, "telephone"));
            m.setEmail(xmlService.getElementTextContent(el, "email"));
            m.setMetier(xmlService.getElementTextContent(el, "metier"));
            m.setPhotoPath(xmlService.getElementTextContent(el, "photoPath"));
            
            String disp = xmlService.getElementTextContent(el, "disponibilite");
            m.setDisponibilite(disp != null ? disp : "DISPONIBLE");
            
            String active = xmlService.getElementTextContent(el, "active");
            m.setActive(active == null || Boolean.parseBoolean(active));

            // Compétences
            NodeList competencesNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "competence");
            List<String> competences = new ArrayList<>();
            for (int i = 0; i < competencesNodes.getLength(); i++) {
                competences.add(competencesNodes.item(i).getTextContent());
            }
            m.setCompetences(competences);

            // Habilitations avec dates d'expiration
            NodeList habilitationsNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "habilitation");
            List<String> habilitations = new ArrayList<>();
            Map<String, LocalDate> habilitationsExpiration = new HashMap<>();
            for (int i = 0; i < habilitationsNodes.getLength(); i++) {
                Element habEl = (Element) habilitationsNodes.item(i);
                String habNom = habEl.getTextContent();
                habilitations.add(habNom);
                
                // Lire date d'expiration si présente
                String dateExpStr = xmlService.getElementTextContent(habEl, "dateExpiration");
                if (dateExpStr != null && !dateExpStr.isEmpty()) {
                    try {
                        LocalDate dateExp = LocalDate.parse(dateExpStr);
                        habilitationsExpiration.put(habNom, dateExp);
                    } catch (Exception e) {
                        logger.warn("Erreur parsing date expiration habilitation: {}", dateExpStr);
                    }
                }
            }
            m.setHabilitations(habilitations);
            m.setHabilitationsExpiration(habilitationsExpiration);

            // Horaires de travail
            NodeList horairesNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "jour");
            Map<String, String> horairesTravail = new HashMap<>();
            for (int i = 0; i < horairesNodes.getLength(); i++) {
                Element jourEl = (Element) horairesNodes.item(i);
                String jourNom = jourEl.getAttribute("nom");
                String horaires = jourEl.getTextContent();
                if (jourNom != null && horaires != null) {
                    horairesTravail.put(jourNom, horaires);
                }
            }
            m.setHorairesTravail(horairesTravail);

            // Congés
            NodeList congesNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "conge");
            List<LocalDate> conges = new ArrayList<>();
            for (int i = 0; i < congesNodes.getLength(); i++) {
                String dateStr = congesNodes.item(i).getTextContent();
                try {
                    conges.add(LocalDate.parse(dateStr));
                } catch (Exception e) {
                    logger.warn("Erreur parsing date congé: {}", dateStr);
                }
            }
            m.setConges(conges);

            // Absences
            NodeList absencesNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "absence");
            List<LocalDate> absences = new ArrayList<>();
            for (int i = 0; i < absencesNodes.getLength(); i++) {
                String dateStr = absencesNodes.item(i).getTextContent();
                try {
                    absences.add(LocalDate.parse(dateStr));
                } catch (Exception e) {
                    logger.warn("Erreur parsing date absence: {}", dateStr);
                }
            }
            m.setAbsences(absences);

            // Historique interventions
            NodeList historiqueNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "interventionId");
            List<Integer> historiqueIds = new ArrayList<>();
            for (int i = 0; i < historiqueNodes.getLength(); i++) {
                String idStr = historiqueNodes.item(i).getTextContent();
                try {
                    historiqueIds.add(Integer.parseInt(idStr));
                } catch (Exception e) {
                    logger.warn("Erreur parsing ID intervention historique: {}", idStr);
                }
            }
            m.setHistoriqueInterventionIds(historiqueIds);

        } catch (Exception e) {
            logger.error("Erreur parsing main-d'œuvre", e);
        }
        return m;
    }
}

