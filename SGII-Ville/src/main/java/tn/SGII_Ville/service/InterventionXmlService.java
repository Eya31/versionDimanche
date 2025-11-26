package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tn.SGII_Ville.entities.Demande;
import tn.SGII_Ville.entities.Intervention;
import tn.SGII_Ville.model.enums.EtatDemandeType;
import tn.SGII_Ville.model.enums.EtatInterventionType;
import tn.SGII_Ville.model.enums.PrioriteType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InterventionXmlService {

    private static final Logger logger = LoggerFactory.getLogger(InterventionXmlService.class);

    @Autowired private XmlService xmlService;
    @Autowired private DemandeXmlService demandeXmlService;

    public List<Intervention> getAllInterventions() {
        List<Intervention> interventions = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("Interventions");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Intervention");

            for (int i = 0; i < nodes.getLength(); i++) {
                interventions.add(parseIntervention((Element) nodes.item(i)));
            }

        } catch (Exception e) {
            logger.error("Erreur lors du chargement des interventions", e);
        }
        return interventions;
    }

    public Intervention findById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Interventions");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Intervention");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    return parseIntervention(el);
                }
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de l'intervention {}", id, e);
        }
        return null;
    }

    public Intervention planifierDemande(int demandeId) {
        try {
            logger.info("=== DÉBUT PLANIFICATION DEMANDE #{} ===", demandeId);

            Demande demande = demandeXmlService.findById(demandeId);
            if (demande == null) {
                throw new RuntimeException("Demande non trouvée ID: " + demandeId);
            }
            
            logger.info("Demande trouvée: {} - État: {}", demande.getId(), demande.getEtat());

            if (demande.getEtat() == EtatDemandeType.TRAITEE) {
                throw new RuntimeException("Demande déjà planifiée");
            }

            // Générer nouvel ID intervention
            int newId = xmlService.getNextInterventionId();
            logger.info("Nouvel ID intervention généré: {}", newId);

            // Créer l'intervention
            Intervention intervention = new Intervention();
            intervention.setId(newId);
            intervention.setPriorite(PrioriteType.PLANIFIEE);
            intervention.setEtat(EtatInterventionType.EN_ATTENTE);
            intervention.setDatePlanifiee(LocalDate.now().plusDays(7));
            intervention.setBudget(new BigDecimal("800.00"));
            intervention.setTechnicienId(0);
            intervention.setDemandeId(demandeId);

            logger.info("Intervention créée: {}", intervention);

            // Sauvegarde dans le fichier principal
            saveToMainFile(intervention);

            // Mise à jour de la demande
            boolean ok = demandeXmlService.updateEtat(demandeId, EtatDemandeType.TRAITEE);
            if (!ok) {
                throw new RuntimeException("Échec mise à jour état demande");
            }

            logger.info("=== SUCCÈS → Intervention #{} créée ===", newId);
            return intervention;

        } catch (Exception e) {
            logger.error("=== ERREUR PLANIFICATION demande {} ===", demandeId, e);
            throw new RuntimeException("Échec planification: " + e.getMessage(), e);
        }
    }

    private void saveToMainFile(Intervention intervention) {
        try {
            logger.info("Sauvegarde intervention ID: {}", intervention.getId());
            
            Document doc = xmlService.loadXmlDocument("Interventions");
            Element root = doc.getDocumentElement();
            
            logger.info("Racine du document: {}", root.getNodeName());

            // Créer un nouvel élément Intervention
            Element newIntervention = doc.createElementNS(xmlService.getNamespaceUri(), "Intervention");
            
            // Ajouter tous les éléments enfants
            xmlService.addTextElement(doc, newIntervention, "id", String.valueOf(intervention.getId()));
            xmlService.addTextElement(doc, newIntervention, "priorite", intervention.getPriorite().name());
            xmlService.addTextElement(doc, newIntervention, "etat", intervention.getEtat().name());
            xmlService.addTextElement(doc, newIntervention, "datePlanifiee", intervention.getDatePlanifiee().toString());
            xmlService.addTextElement(doc, newIntervention, "budget", intervention.getBudget().toString());
            xmlService.addTextElement(doc, newIntervention, "technicienId", String.valueOf(intervention.getTechnicienId()));
            
            // demandeId (obligatoire)
            xmlService.addTextElement(doc, newIntervention, "demandeId", String.valueOf(intervention.getDemandeId()));

            // Ajouter à la racine
            root.appendChild(newIntervention);
            logger.info("Nouvel élément Intervention créé et ajouté");

            // Sauvegarder le document
            xmlService.saveXmlDocument(doc, "Interventions");
            logger.info("Document interventions sauvegardé avec succès");

        } catch (Exception e) {
            logger.error("ERREUR sauvegarde intervention:", e);
            throw new RuntimeException("Erreur sauvegarde intervention: " + e.getMessage(), e);
        }
    }

    private Intervention parseIntervention(Element el) {
        try {
            Intervention i = new Intervention();

            i.setId(Integer.parseInt(xmlService.getElementTextContent(el, "id")));
            
            String prioriteStr = xmlService.getElementTextContent(el, "priorite");
            if (prioriteStr != null) {
                i.setPriorite(PrioriteType.valueOf(prioriteStr));
            }
            
            String etatStr = xmlService.getElementTextContent(el, "etat");
            if (etatStr != null) {
                i.setEtat(EtatInterventionType.valueOf(etatStr));
            }
            
            String datePlanifieeStr = xmlService.getElementTextContent(el, "datePlanifiee");
            if (datePlanifieeStr != null) {
                i.setDatePlanifiee(LocalDate.parse(datePlanifieeStr));
            }
            
            String budgetStr = xmlService.getElementTextContent(el, "budget");
            if (budgetStr != null) {
                i.setBudget(new BigDecimal(budgetStr));
            }

            String tech = xmlService.getElementTextContent(el, "technicienId");
            i.setTechnicienId(tech != null && !tech.isEmpty() ? Integer.parseInt(tech) : 0);

            String demandeId = xmlService.getElementTextContent(el, "demandeId");
            i.setDemandeId(demandeId != null && !demandeId.isEmpty() ? Integer.parseInt(demandeId) : null);
            
            String chefServiceId = xmlService.getElementTextContent(el, "chefServiceId");
            i.setChefServiceId(chefServiceId != null && !chefServiceId.isEmpty() ? Integer.parseInt(chefServiceId) : null);
            
            String description = xmlService.getElementTextContent(el, "description");
            i.setDescription(description);
            
            String typeIntervention = xmlService.getElementTextContent(el, "typeIntervention");
            i.setTypeIntervention(typeIntervention);
            
            String commentaire = xmlService.getElementTextContent(el, "commentaire");
            i.setCommentaire(commentaire);
            
            String rapportFinal = xmlService.getElementTextContent(el, "rapportFinal");
            i.setRapportFinal(rapportFinal);
            
            String tempsPasseStr = xmlService.getElementTextContent(el, "tempsPasseMinutes");
            if (tempsPasseStr != null && !tempsPasseStr.isEmpty()) {
                i.setTempsPasseMinutes(Integer.parseInt(tempsPasseStr));
            }
            
            String dateDebutStr = xmlService.getElementTextContent(el, "dateDebut");
            if (dateDebutStr != null && !dateDebutStr.isEmpty()) {
                i.setDateDebut(java.time.LocalDate.parse(dateDebutStr));
            }
            
            String dateFinStr = xmlService.getElementTextContent(el, "dateFin");
            if (dateFinStr != null && !dateFinStr.isEmpty()) {
                i.setDateFin(java.time.LocalDate.parse(dateFinStr));
            }

            // Charger mainDOeuvreIds
            List<Integer> mainDOeuvreIds = new ArrayList<>();
            NodeList mainDOeuvreNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "mainDOeuvreIds");
            if (mainDOeuvreNodes.getLength() > 0) {
                Element mainDOeuvreEl = (Element) mainDOeuvreNodes.item(0);
                NodeList idNodes = mainDOeuvreEl.getElementsByTagNameNS(xmlService.getNamespaceUri(), "id");
                for (int j = 0; j < idNodes.getLength(); j++) {
                    String idStr = idNodes.item(j).getTextContent();
                    if (idStr != null && !idStr.isEmpty()) {
                        mainDOeuvreIds.add(Integer.parseInt(idStr));
                    }
                }
            }
            i.setOuvrierIds(mainDOeuvreIds);
            logger.debug("Intervention {} - mainDOeuvreIds chargés: {}", i.getId(), mainDOeuvreIds);

            // Charger photoIds
            List<Integer> photoIds = new ArrayList<>();
            NodeList photoNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "photoIds");
            if (photoNodes.getLength() > 0) {
                Element photoEl = (Element) photoNodes.item(0);
                NodeList idNodes = photoEl.getElementsByTagNameNS(xmlService.getNamespaceUri(), "id");
                for (int j = 0; j < idNodes.getLength(); j++) {
                    String idStr = idNodes.item(j).getTextContent();
                    if (idStr != null && !idStr.isEmpty()) {
                        photoIds.add(Integer.parseInt(idStr));
                    }
                }
            }

            // Charger equipementIds
            List<Integer> equipementIds = new ArrayList<>();
            NodeList equipementNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "equipementIds");
            if (equipementNodes.getLength() > 0) {
                Element equipementEl = (Element) equipementNodes.item(0);
                NodeList idNodes = equipementEl.getElementsByTagNameNS(xmlService.getNamespaceUri(), "id");
                for (int j = 0; j < idNodes.getLength(); j++) {
                    String idStr = idNodes.item(j).getTextContent();
                    if (idStr != null && !idStr.isEmpty()) {
                        equipementIds.add(Integer.parseInt(idStr));
                    }
                }
            }
            i.setEquipementIds(equipementIds);

            // Charger ressourceIds
            List<Integer> ressourceIds = new ArrayList<>();
            NodeList ressourceNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "ressourceIds");
            if (ressourceNodes.getLength() > 0) {
                Element ressourceEl = (Element) ressourceNodes.item(0);
                NodeList idNodes = ressourceEl.getElementsByTagNameNS(xmlService.getNamespaceUri(), "id");
                for (int j = 0; j < idNodes.getLength(); j++) {
                    String idStr = idNodes.item(j).getTextContent();
                    if (idStr != null && !idStr.isEmpty()) {
                        ressourceIds.add(Integer.parseInt(idStr));
                    }
                }
            }
            i.setRessourceIds(ressourceIds);

            return i;

        } catch (Exception e) {
            logger.error("Erreur parsing intervention", e);
            throw new RuntimeException("Erreur parsing intervention", e);
        }
    }

    public boolean updateEtat(int id, EtatInterventionType nouvelEtat) {
        try {
            Document doc = xmlService.loadXmlDocument("Interventions");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Intervention");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    NodeList etatNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "etat");
                    if (etatNodes.getLength() > 0) {
                        etatNodes.item(0).setTextContent(nouvelEtat.name());
                    }
                    xmlService.saveXmlDocument(doc, "Interventions");
                    return true;
                }
            }

        } catch (Exception e) {
            logger.error("Erreur mise à jour état intervention {}", id, e);
        }
        return false;
    }

    public Intervention updateIntervention(Intervention intervention) {
        try {
            Document doc = xmlService.loadXmlDocument("Interventions");
            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Intervention");

            // Trouver et supprimer l'ancien élément s'il existe
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == intervention.getId()) {
                    root.removeChild(el);
                    break;
                }
            }

            // Créer le nouvel élément avec toutes les données
            Element newIntervention = doc.createElementNS(xmlService.getNamespaceUri(), "Intervention");
            xmlService.addTextElement(doc, newIntervention, "id", String.valueOf(intervention.getId()));
            
            if (intervention.getDescription() != null) {
                xmlService.addTextElement(doc, newIntervention, "description", intervention.getDescription());
            }
            if (intervention.getTypeIntervention() != null) {
                xmlService.addTextElement(doc, newIntervention, "typeIntervention", intervention.getTypeIntervention());
            }
            if (intervention.getPriorite() != null) {
                xmlService.addTextElement(doc, newIntervention, "priorite", intervention.getPriorite().name());
            }
            if (intervention.getEtat() != null) {
                xmlService.addTextElement(doc, newIntervention, "etat", intervention.getEtat().name());
            }
            if (intervention.getDatePlanifiee() != null) {
                xmlService.addTextElement(doc, newIntervention, "datePlanifiee", intervention.getDatePlanifiee().toString());
            }
            if (intervention.getDateDebut() != null) {
                xmlService.addTextElement(doc, newIntervention, "dateDebut", intervention.getDateDebut().toString());
            }
            if (intervention.getDateFin() != null) {
                xmlService.addTextElement(doc, newIntervention, "dateFin", intervention.getDateFin().toString());
            }
            if (intervention.getBudget() != null) {
                xmlService.addTextElement(doc, newIntervention, "budget", intervention.getBudget().toString());
            }
            xmlService.addTextElement(doc, newIntervention, "technicienId", String.valueOf(intervention.getTechnicienId()));
            
            // demandeId (obligatoire)
            xmlService.addTextElement(doc, newIntervention, "demandeId", String.valueOf(intervention.getDemandeId()));
            
            if (intervention.getChefServiceId() != null) {
                xmlService.addTextElement(doc, newIntervention, "chefServiceId", String.valueOf(intervention.getChefServiceId()));
            }
            if (intervention.getCommentaire() != null) {
                xmlService.addTextElement(doc, newIntervention, "commentaire", intervention.getCommentaire());
            }
            if (intervention.getRapportFinal() != null) {
                xmlService.addTextElement(doc, newIntervention, "rapportFinal", intervention.getRapportFinal());
            }
            if (intervention.getTempsPasseMinutes() != null) {
                xmlService.addTextElement(doc, newIntervention, "tempsPasseMinutes", String.valueOf(intervention.getTempsPasseMinutes()));
            }
            if (intervention.getSignatureElectronique() != null) {
                xmlService.addTextElement(doc, newIntervention, "signatureElectronique", intervention.getSignatureElectronique());
            }

            // Main-d'œuvre
            if (intervention.getOuvrierIds() != null && !intervention.getOuvrierIds().isEmpty()) {
                Element ouvrierIdsEl = doc.createElementNS(xmlService.getNamespaceUri(), "ouvrierIds");
                for (Integer id : intervention.getOuvrierIds()) {
                    Element idEl = doc.createElementNS(xmlService.getNamespaceUri(), "ouvrierId");
                    idEl.setTextContent(String.valueOf(id));
                    ouvrierIdsEl.appendChild(idEl);
                }
                newIntervention.appendChild(ouvrierIdsEl);
            }

            // Équipements
            if (intervention.getEquipementIds() != null && !intervention.getEquipementIds().isEmpty()) {
                Element equipementsEl = doc.createElementNS(xmlService.getNamespaceUri(), "equipementIds");
                for (Integer id : intervention.getEquipementIds()) {
                    Element idEl = doc.createElementNS(xmlService.getNamespaceUri(), "id");
                    idEl.setTextContent(String.valueOf(id));
                    equipementsEl.appendChild(idEl);
                }
                newIntervention.appendChild(equipementsEl);
            }

            // Ressources
            if (intervention.getRessourceIds() != null && !intervention.getRessourceIds().isEmpty()) {
                Element ressourcesEl = doc.createElementNS(xmlService.getNamespaceUri(), "ressourceIds");
                for (Integer id : intervention.getRessourceIds()) {
                    Element idEl = doc.createElementNS(xmlService.getNamespaceUri(), "id");
                    idEl.setTextContent(String.valueOf(id));
                    ressourcesEl.appendChild(idEl);
                }
                newIntervention.appendChild(ressourcesEl);
            }

            root.appendChild(newIntervention);
            xmlService.saveXmlDocument(doc, "Interventions");

            return intervention;

        } catch (Exception e) {
            logger.error("Erreur mise à jour intervention {}", intervention.getId(), e);
            throw new RuntimeException("Erreur mise à jour intervention", e);
        }
    }

    /**
     * Planifie une intervention complète avec tous les détails (technicien, ressources, etc.)
     */
    public Intervention planifierInterventionComplete(tn.SGII_Ville.dto.PlanificationCompleteRequest request) {
        try {
            logger.info("=== DÉBUT PLANIFICATION COMPLÈTE DEMANDE #{} ===", request.getDemandeId());

            Demande demande = demandeXmlService.findById(request.getDemandeId());
            if (demande == null) {
                throw new RuntimeException("Demande non trouvée ID: " + request.getDemandeId());
            }

            if (demande.getEtat() == EtatDemandeType.TRAITEE) {
                throw new RuntimeException("Demande déjà planifiée");
            }

            // Générer nouvel ID intervention
            int newId = xmlService.getNextInterventionId();
            logger.info("Nouvel ID intervention généré: {}", newId);

            // Créer l'intervention avec tous les détails
            Intervention intervention = new Intervention();
            intervention.setId(newId);
            intervention.setDemandeId(request.getDemandeId());
            intervention.setTechnicienId(request.getTechnicienId() != null ? request.getTechnicienId() : 0);
            intervention.setDescription(request.getDescription() != null ? request.getDescription() : demande.getDescription());
            intervention.setTypeIntervention(request.getTypeIntervention());
            intervention.setPriorite(request.getPriorite() != null ? request.getPriorite() : PrioriteType.PLANIFIEE);
            intervention.setEtat(EtatInterventionType.EN_ATTENTE);
            intervention.setDatePlanifiee(request.getDatePlanifiee() != null ? request.getDatePlanifiee() : LocalDate.now().plusDays(1));
            
            if (request.getHeureDebut() != null) {
                intervention.setDateDebut(request.getDatePlanifiee());
            }
            
            intervention.setBudget(request.getBudget() != null ? request.getBudget() : new BigDecimal("500.00"));
            intervention.setCommentaire(request.getRemarques());
            
            // Assigner les ressources
            if (request.getEquipementIds() != null) {
                intervention.setEquipementIds(request.getEquipementIds());
            }
            if (request.getRessourceIds() != null) {
                intervention.setRessourceIds(request.getRessourceIds());
            }
            if (request.getOuvrierIds() != null) {
                intervention.setOuvrierIds(request.getOuvrierIds());
            }

            // Récupérer le chef de service depuis la demande ou l'authentification
            // Pour l'instant, on peut utiliser 0 ou récupérer depuis le contexte
            intervention.setChefServiceId(0); // À améliorer avec l'authentification

            logger.info("Intervention créée avec détails: {}", intervention);

            // Sauvegarder l'intervention avec tous les détails en utilisant updateIntervention
            // qui gère tous les champs (description, typeIntervention, ressources, etc.)
            updateIntervention(intervention);

            // Mise à jour de la demande (état devient TRAITEE)
            boolean ok = demandeXmlService.updateEtat(request.getDemandeId(), EtatDemandeType.TRAITEE);
            if (!ok) {
                throw new RuntimeException("Échec mise à jour état demande");
            }

            logger.info("=== SUCCÈS → Intervention #{} planifiée avec succès ===", newId);
            return intervention;

        } catch (Exception e) {
            logger.error("=== ERREUR PLANIFICATION COMPLÈTE demande {} ===", request.getDemandeId(), e);
            throw new RuntimeException("Échec planification: " + e.getMessage(), e);
        }
    }

    public boolean affecterTechnicien(int interventionId, int technicienId) {
        try {
            Document doc = xmlService.loadXmlDocument("Interventions");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Intervention");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == interventionId) {
                    // Mettre à jour technicienId
                    NodeList techNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "technicienId");
                    if (techNodes.getLength() > 0) {
                        techNodes.item(0).setTextContent(String.valueOf(technicienId));
                    }
                    
                    // Si intervention en attente → la mettre en cours
                    String currentEtat = xmlService.getElementTextContent(el, "etat");
                    if ("EN_ATTENTE".equals(currentEtat)) {
                        NodeList etatNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "etat");
                        if (etatNodes.getLength() > 0) {
                            etatNodes.item(0).setTextContent(EtatInterventionType.EN_COURS.name());
                        }
                    }

                    xmlService.saveXmlDocument(doc, "Interventions");
                    return true;
                }
            }

        } catch (Exception e) {
            logger.error("Erreur affectation technicien {} à intervention {}", technicienId, interventionId, e);
        }
        return false;
    }
}