package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import tn.SGII_Ville.entities.Administrateur;
import tn.SGII_Ville.entities.AgentMainDOeuvre;
import tn.SGII_Ville.entities.ChefDeService;
import tn.SGII_Ville.entities.Citoyen;
import tn.SGII_Ville.entities.Technicien;
import tn.SGII_Ville.entities.Utilisateur;
import tn.SGII_Ville.model.enums.RoleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des utilisateurs dans le XML
 */
@Service
public class UserXmlService {

    @Autowired
    private XmlService xmlService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Génère un nouvel ID pour les utilisateurs
     */
    public int generateNewId() {
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            return xmlService.generateNewId(doc, "Utilisateurs");
        } catch (Exception e) {
            e.printStackTrace();
            return 1; // ID par défaut en cas d'erreur
        }
    }

    /**
     * Trouve un utilisateur par email
     */
    public Optional<Utilisateur> findByEmail(String email) {
    try {
        System.out.println("🔍 Recherche utilisateur par email: " + email);
        Document doc = xmlService.loadXmlDocument("Utilisateurs");
        Element utilisateursSection = doc.getDocumentElement();
        
        if (utilisateursSection == null) {
            System.out.println("❌ Section Utilisateurs introuvable");
            return Optional.empty();
        }

        NodeList children = utilisateursSection.getChildNodes();
        System.out.println("📊 Nombre d'éléments enfants: " + children.getLength());
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element userElement = (Element) child;
                String userEmail = xmlService.getElementTextContent(userElement, "email");
                String tagName = userElement.getLocalName();
                
                System.out.println("🔍 Élément: " + tagName + " - Email: " + userEmail);
                
                if (email.equals(userEmail)) {
                    System.out.println("✅ Utilisateur trouvé: " + email + " (Type: " + tagName + ")");
                    return Optional.of(parseUtilisateur(userElement));
                }
            }
        }
        
        System.out.println("❌ Aucun utilisateur trouvé avec l'email: " + email);
    } catch (Exception e) {
        System.err.println("❌ Erreur lors de la recherche par email: " + e.getMessage());
        e.printStackTrace();
    }
    return Optional.empty();
}
    /**
     * Trouve un utilisateur par ID
     */
    public Optional<Utilisateur> findById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            Element utilisateursSection = doc.getDocumentElement();
            
            if (utilisateursSection == null) return Optional.empty();

            NodeList children = utilisateursSection.getChildNodes();
            
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) child;
                    String userId = xmlService.getElementTextContent(userElement, "id");
                    
                    if (userId != null && Integer.parseInt(userId) == id) {
                        return Optional.of(parseUtilisateur(userElement));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Récupère tous les utilisateurs
     */
    public List<Utilisateur> findAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            Element utilisateursSection = doc.getDocumentElement();
            
            if (utilisateursSection == null) return utilisateurs;

            NodeList children = utilisateursSection.getChildNodes();
            
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) child;
                    utilisateurs.add(parseUtilisateur(userElement));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return utilisateurs;
    }

    /**
     * Sauvegarde un nouvel utilisateur
     */
    public Utilisateur save(Utilisateur user) {
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            Element utilisateursSection = doc.getDocumentElement();
            
            if (utilisateursSection == null) {
                throw new RuntimeException("Section Utilisateurs introuvable");
            }

            // Générer un nouvel ID si nécessaire
            if (user.getId() == 0) {
                int newId = generateNewId();
                user.setId(newId);
            }

            // Encoder le mot de passe si ce n'est pas déjà fait
            if (!user.getMotDePasse().startsWith("$2a$")) {
                user.setMotDePasse(passwordEncoder.encode(user.getMotDePasse()));
            }

            // Créer l'élément XML selon le type d'utilisateur
            Element userElement = createUserElement(doc, user);
            utilisateursSection.appendChild(userElement);

            // Sauvegarder le document
            xmlService.saveXmlDocument(doc, "Utilisateurs");

            return user;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la sauvegarde de l'utilisateur", e);
        }
    }

    /**
     * Met à jour un utilisateur existant
     */
    public Utilisateur update(Utilisateur utilisateur) {
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            
            // Supprimer l'ancien
            xmlService.deleteElementById(doc, "Utilisateurs", utilisateur.getId());
            
            // Recréer avec les nouvelles données
            Element utilisateursSection = doc.getDocumentElement();
            Element userElement = createUserElement(doc, utilisateur);
            utilisateursSection.appendChild(userElement);

            xmlService.saveXmlDocument(doc, "Utilisateurs");
            return utilisateur;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur", e);
        }
    }

    /**
     * Supprime un utilisateur par ID
     */
    public boolean deleteById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            boolean deleted = xmlService.deleteElementById(doc, "Utilisateurs", id);
            if (deleted) {
                xmlService.saveXmlDocument(doc, "Utilisateurs");
            }
            return deleted;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    /**
     * Parse un élément XML en objet Utilisateur
     */
    private Utilisateur parseUtilisateur(Element userElement) {
    String tagName = userElement.getLocalName();
    
    // CORRECTION: Normaliser les noms des éléments
    String normalizedTagName = normalizeTagName(tagName);
    
    int id = Integer.parseInt(xmlService.getElementTextContent(userElement, "id"));
    String nom = xmlService.getElementTextContent(userElement, "nom");
    String email = xmlService.getElementTextContent(userElement, "email");
    String motDePasse = xmlService.getElementTextContent(userElement, "motDePasse");
    String roleStr = xmlService.getElementTextContent(userElement, "role");
    RoleType role = RoleType.valueOf(roleStr);

    System.out.println("🔍 Parsing utilisateur - Tag: " + tagName + " -> Normalisé: " + normalizedTagName);
    
    return switch (normalizedTagName) {
        case "Citoyen" -> {
            String adresse = xmlService.getElementTextContent(userElement, "adresse");
            String telephone = xmlService.getElementTextContent(userElement, "telephone");
            Citoyen citoyen = new Citoyen(id, nom, email, motDePasse, adresse, telephone);
            citoyen.setRole(role);
            yield citoyen;
        }
        case "Technicien" -> {
            List<String> competences = parseCompetences(userElement);
            String disponibiliteStr = xmlService.getElementTextContent(userElement, "disponibilite");
            boolean disponibilite = disponibiliteStr != null ? Boolean.parseBoolean(disponibiliteStr) : true;
            Technicien technicien = new Technicien(id, nom, email, motDePasse, competences, disponibilite);
            technicien.setRole(role);
            yield technicien;
        }
        case "ChefDeService", "chefService" -> { // Gérer les deux cas
            String departement = xmlService.getElementTextContent(userElement, "departement");
            ChefDeService chef = new ChefDeService(id, nom, email, motDePasse, departement);
            chef.setRole(role);
            yield chef;
        }
        case "Administrateur", "administrateur" -> { // Gérer les deux cas
            Administrateur admin = new Administrateur(id, nom, email, motDePasse);
            admin.setRole(role);
            yield admin;
        }
        case "AgentMainDOeuvre", "agentMainDOeuvre", "MainDOeuvre" -> { // Gérer tous les cas
            String prenom = xmlService.getElementTextContent(userElement, "prenom");
            String matricule = xmlService.getElementTextContent(userElement, "matricule");
            String cin = xmlService.getElementTextContent(userElement, "cin");
            String telephone = xmlService.getElementTextContent(userElement, "telephone");
            String metier = xmlService.getElementTextContent(userElement, "metier");
            List<String> competences = parseCompetences(userElement);
            
            int mainDOeuvreId = 0;
            try {
                String mainDOeuvreIdStr = xmlService.getElementTextContent(userElement, "mainDOeuvreId");
                if (mainDOeuvreIdStr != null && !mainDOeuvreIdStr.isEmpty()) {
                    mainDOeuvreId = Integer.parseInt(mainDOeuvreIdStr);
                }
            } catch (Exception e) {
                // Ignorer si non présent
            }
            
            AgentMainDOeuvre agent = new AgentMainDOeuvre(id, nom, email, motDePasse, prenom, matricule, cin, telephone);
            agent.setMetier(metier);
            agent.setCompetences(competences);
            agent.setMainDOeuvreId(mainDOeuvreId);
            agent.setRole(role);
            yield agent;
        }
        default -> {
            System.err.println("❌ Type d'utilisateur inconnu: " + tagName);
            throw new IllegalArgumentException("Type d'utilisateur inconnu: " + tagName);
        }
    };
}

// Méthode pour normaliser les noms des tags
private String normalizeTagName(String tagName) {
    if (tagName == null) return tagName;
    
    return switch (tagName.toLowerCase()) {
        case "chefservice" -> "ChefDeService";
        case "administrateur" -> "Administrateur";
        case "agentmaindoeuvre" -> "AgentMainDOeuvre";
        case "maindoeuvre" -> "AgentMainDOeuvre"; // Traiter MainDOeuvre comme AgentMainDOeuvre
        default -> tagName;
    };
}
    /**
     * Crée un élément XML à partir d'un objet Utilisateur
     */
    private Element createUserElement(Document doc, Utilisateur user) {
        Element userElement = null;
        
        switch (user.getRole()) {
            case CITOYEN:
                userElement = doc.createElement("citoyen");
                break;
            case TECHNICIEN:
                userElement = doc.createElement("technicien");
                break;
            case CHEF_SERVICE:
                userElement = doc.createElement("chefService");
                break;
            case ADMINISTRATEUR:
                userElement = doc.createElement("administrateur");
                break;
            case MAIN_DOEUVRE:
                userElement = doc.createElement("agentMainDOeuvre");
                break;
            default:
                throw new IllegalArgumentException("Type d'utilisateur non supporté: " + user.getRole());
        }
        
        // Ajouter les éléments de base
        addBaseUserFields(doc, userElement, user);
        
        // Attributs spécifiques
        if (user instanceof Citoyen) {
            Citoyen citoyen = (Citoyen) user;
            xmlService.addTextElement(doc, userElement, "adresse", citoyen.getAdresse() != null ? citoyen.getAdresse() : "");
            xmlService.addTextElement(doc, userElement, "telephone", citoyen.getTelephone() != null ? citoyen.getTelephone() : "");
        } else if (user instanceof Technicien) {
            Technicien technicien = (Technicien) user;
            // Note: Technicien n'a pas de département, on ne l'ajoute pas
            
            // Gérer les compétences
            if (technicien.getCompetences() != null && !technicien.getCompetences().isEmpty()) {
                Element competencesElement = doc.createElement("competences");
                for (String competence : technicien.getCompetences()) {
                    Element competenceElement = doc.createElement("competence");
                    competenceElement.setTextContent(competence);
                    competencesElement.appendChild(competenceElement);
                }
                userElement.appendChild(competencesElement);
            }
            
            xmlService.addTextElement(doc, userElement, "disponibilite", String.valueOf(technicien.isDisponibilite()));
        } else if (user instanceof ChefDeService) {
            ChefDeService chef = (ChefDeService) user;
            xmlService.addTextElement(doc, userElement, "departement", chef.getDepartement() != null ? chef.getDepartement() : "");
        } else if (user instanceof AgentMainDOeuvre) {
            AgentMainDOeuvre agent = (AgentMainDOeuvre) user;
            xmlService.addTextElement(doc, userElement, "prenom", agent.getPrenom() != null ? agent.getPrenom() : "");
            xmlService.addTextElement(doc, userElement, "matricule", agent.getMatricule() != null ? agent.getMatricule() : "");
            xmlService.addTextElement(doc, userElement, "cin", agent.getCin() != null ? agent.getCin() : "");
            xmlService.addTextElement(doc, userElement, "telephone", agent.getTelephone() != null ? agent.getTelephone() : "");
            xmlService.addTextElement(doc, userElement, "metier", agent.getMetier() != null ? agent.getMetier() : "");
            xmlService.addTextElement(doc, userElement, "mainDOeuvreId", String.valueOf(agent.getMainDOeuvreId()));
            
            // Ajouter les compétences comme éléments enfants
            if (agent.getCompetences() != null && !agent.getCompetences().isEmpty()) {
                Element competencesElement = doc.createElement("competences");
                for (String competence : agent.getCompetences()) {
                    Element competenceElement = doc.createElement("competence");
                    competenceElement.setTextContent(competence);
                    competencesElement.appendChild(competenceElement);
                }
                userElement.appendChild(competencesElement);
            }
        }
        
        return userElement;
    }

    /**
     * Ajoute les champs de base d'un utilisateur
     */
    private void addBaseUserFields(Document doc, Element userElement, Utilisateur utilisateur) {
        xmlService.addTextElement(doc, userElement, "id", String.valueOf(utilisateur.getId()));
        xmlService.addTextElement(doc, userElement, "nom", utilisateur.getNom());
        xmlService.addTextElement(doc, userElement, "email", utilisateur.getEmail());
        xmlService.addTextElement(doc, userElement, "motDePasse", utilisateur.getMotDePasse());
        xmlService.addTextElement(doc, userElement, "role", utilisateur.getRole().name());
    }

    /**
     * Parse les compétences d'un technicien depuis utilisateurs.xml
     */
    private List<String> parseCompetences(Element userElement) {
        List<String> competences = new ArrayList<>();
        try {
            NodeList competencesNodes = userElement.getElementsByTagNameNS(
                xmlService.getNamespaceUri(), "competences"
            );
            
            // Cas 1: Plusieurs éléments <competences> directement (Technicien)
            if (competencesNodes.getLength() > 0) {
                for (int i = 0; i < competencesNodes.getLength(); i++) {
                    Element compElement = (Element) competencesNodes.item(i);
                    
                    // Vérifier si c'est un conteneur avec des enfants <competence>
                    NodeList children = compElement.getElementsByTagNameNS(
                        xmlService.getNamespaceUri(), "competence"
                    );
                    
                    if (children.getLength() > 0) {
                        // Cas 2: Conteneur <competences> avec des enfants <competence> (AgentMainDOeuvre)
                        for (int j = 0; j < children.getLength(); j++) {
                            String competence = children.item(j).getTextContent().trim();
                            if (!competence.isEmpty()) {
                                competences.add(competence);
                            }
                        }
                    } else {
                        // Cas 1: Élément <competences> simple avec texte direct (Technicien)
                        String competence = compElement.getTextContent().trim();
                        if (!competence.isEmpty()) {
                            competences.add(competence);
                        }
                    }
                }
            }
            
            System.out.println("🔍 DEBUG parseCompetences - Compétences parsées: " + competences);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return competences;
    }

    /**
     * Récupère tous les techniciens
     */
    public List<Technicien> findAllTechniciens() {
        List<Technicien> techniciens = new ArrayList<>();
        try {
            List<Utilisateur> allUsers = findAll();
            for (Utilisateur user : allUsers) {
                if (user instanceof Technicien) {
                    techniciens.add((Technicien) user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return techniciens;
    }

    /**
     * Vérifie le mot de passe
     */
   /**
 * Vérifie le mot de passe avec débogage
 */
public boolean checkPassword(String rawPassword, String encodedPassword) {
    System.out.println("🔐 Vérification mot de passe:");
    System.out.println("   Mot de passe fourni: " + rawPassword);
    System.out.println("   Hash en BD: " + encodedPassword);
    
    boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
    System.out.println("   Résultat: " + matches);
    
    return matches;
}

    /**
     * Encode un mot de passe
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Récupère TOUS les techniciens depuis utilisateurs.xml (dynamique)
     */
    public List<Technicien> findAllTechniciensFromXml() {
        List<Technicien> techniciens = new ArrayList<>();
        try {
            // Lire depuis utilisateurs.xml au lieu de techniciens.xml
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            Element root = doc.getDocumentElement();
            
            if (root == null) return techniciens;

            // Parcourir tous les éléments utilisateurs
            NodeList children = root.getChildNodes();
            
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) child;
                    
                    // Vérifier si c'est un Technicien
                    if ("Technicien".equals(userElement.getLocalName())) {
                        Utilisateur user = parseUtilisateur(userElement);
                        if (user instanceof Technicien) {
                            techniciens.add((Technicien) user);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return techniciens;
    }

    /**
     * Marque un technicien comme indisponible pour une date donnée
     */
    public void marquerTechnicienIndisponible(Long technicienId, String dateIntervention) {
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            Element root = doc.getDocumentElement();
            
            if (root == null) {
                throw new RuntimeException("Document Utilisateurs vide");
            }

            NodeList children = root.getChildNodes();
            boolean found = false;
            
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) child;
                    
                    if ("Technicien".equals(userElement.getLocalName())) {
                        String idStr = xmlService.getElementTextContent(userElement, "id");
                        if (idStr != null && Long.parseLong(idStr) == technicienId) {
                            // Mettre disponibilite à false
                            Element dispElement = (Element) userElement.getElementsByTagNameNS(
                                xmlService.getNamespaceUri(), "disponibilite").item(0);
                            
                            if (dispElement != null) {
                                dispElement.setTextContent("false");
                                found = true;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (!found) {
                throw new RuntimeException("Technicien non trouvé: " + technicienId);
            }
            
            xmlService.saveXmlDocument(doc, "Utilisateurs");
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du marquage du technicien comme indisponible", e);
        }
    }
    // Méthode de test temporaire
public void testUserParsing() {
    try {
        String testEmail = "admin@ville.com";
        System.out.println("🧪 TEST: Recherche de " + testEmail);
        
        Optional<Utilisateur> user = findByEmail(testEmail);
        if (user.isPresent()) {
            System.out.println("✅ TEST RÉUSSI: Utilisateur trouvé - " + user.get().getNom());
            System.out.println("   Role: " + user.get().getRole());
            System.out.println("   Mot de passe présent: " + (user.get().getMotDePasse() != null));
        } else {
            System.out.println("❌ TEST ÉCHOUÉ: Utilisateur non trouvé");
        }
    } catch (Exception e) {
        System.err.println("❌ ERREUR TEST: " + e.getMessage());
        e.printStackTrace();
    }
}
    
}