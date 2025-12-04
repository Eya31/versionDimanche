package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

 import java.io.File; // <--- CETTE LIGNE MANQUE


import java.math.BigDecimal;

@Service
public class StockRessourceService {

    @Autowired
    private XmlService xmlService;

    /**
     * Augmente le stock si la ressource existe, sinon crée une nouvelle ressource
     ;

    /**
     * Augmente le stock si la ressource existe, sinon crée une nouvelle ressource
     */
    public boolean augmenterStock(String designation, int quantiteAjoutee, double budget) {
        try {
            System.out.println("🔧 [DEBUT] Gestion stock pour: '" + designation + "'");
            System.out.println("📦 Quantité à ajouter: " + quantiteAjoutee);
            System.out.println("💰 Budget total: " + budget);
            System.out.println("=== DEBUG augmenterStock ===");
        System.out.println("Designation: " + designation);
        System.out.println("Quantite: " + quantiteAjoutee);
        System.out.println("Budget: " + budget);
        
        // Vérifier d'abord si le fichier existe
        String filePath = "src/main/resources/data/ressources.xml";
        File file = new File(filePath);
        System.out.println("Fichier existe: " + file.exists());
        System.out.println("Chemin absolu: " + file.getAbsolutePath());
            
            // Vérifier d'abord si le fichier existe
            if (!xmlService.checkFileExists("RessourcesMaterielles")) {
                System.out.println("⚠️ Fichier ressources.xml n'existe pas, création...");
                // Créer un document vide
                Document newDoc = xmlService.createNewDocument("RessourcesMaterielles");
                xmlService.saveXmlDocument(newDoc, "RessourcesMaterielles");
            }
            
            // Charger le document
            Document doc = xmlService.loadXmlDocument("RessourcesMaterielles");
            Element root = doc.getDocumentElement();
            
            if (root == null) {
                System.err.println("❌ ERREUR: Racine du document est null!");
                return false;
            }
            
            NodeList ressources = root.getElementsByTagNameNS(
                xmlService.getNamespaceUri(), "RessourceMaterielle"
            );
            
            System.out.println("📊 Nombre total de ressources trouvées: " + ressources.getLength());
            
            boolean ressourceTrouvee = false;
            
            // Afficher toutes les ressources existantes
            for (int i = 0; i < ressources.getLength(); i++) {
                Element ressource = (Element) ressources.item(i);
                String nomRessource = getElementText(ressource, "designation");
                System.out.println("   - Ressource #" + i + ": '" + nomRessource + "'");
            }
            
            // 1. Chercher la ressource existante
            for (int i = 0; i < ressources.getLength(); i++) {
                Element ressource = (Element) ressources.item(i);
                String nomRessource = getElementText(ressource, "designation");
                
                System.out.println("🔍 Comparaison:");
                System.out.println("   - Recherche: '" + designation + "'");
                System.out.println("   - Existant: '" + nomRessource + "'");
                System.out.println("   - Égaux (ignore case)? " + 
                    (nomRessource != null && nomRessource.equalsIgnoreCase(designation)));
                
                if (nomRessource != null && nomRessource.equalsIgnoreCase(designation)) {
                    ressourceTrouvee = true;
                    System.out.println("✅ Ressource existante trouvée: " + nomRessource);
                    
                    // Récupérer la quantité actuelle
                    String quantiteStr = getElementText(ressource, "quantiteEnStock");
                    int quantiteActuelle = 0;
                    if (quantiteStr != null && !quantiteStr.trim().isEmpty()) {
                        quantiteActuelle = Integer.parseInt(quantiteStr.trim());
                    }
                    
                    // Calculer la nouvelle quantité
                    int nouvelleQuantite = quantiteActuelle + quantiteAjoutee;
                    
                    // Mettre à jour la quantité
                    Element quantiteElement = (Element) ressource.getElementsByTagNameNS(
                        xmlService.getNamespaceUri(), "quantiteEnStock").item(0);
                    if (quantiteElement != null) {
                        quantiteElement.setTextContent(String.valueOf(nouvelleQuantite));
                    }
                    
                    // Calculer la nouvelle valeur d'achat (moyenne pondérée)
                    String valeurStr = getElementText(ressource, "valeurAchat");
                    double ancienneValeur = 0.0;
                    if (valeurStr != null && !valeurStr.trim().isEmpty()) {
                        ancienneValeur = Double.parseDouble(valeurStr.trim());
                    }
                    
                    double nouvelleValeur = 0.0;
                    if (quantiteActuelle + quantiteAjoutee > 0) {
                        // Calcul de la moyenne pondérée
                        double totalActuel = ancienneValeur * quantiteActuelle;
                        double totalAjoute = (budget / quantiteAjoutee) * quantiteAjoutee;
                        nouvelleValeur = (totalActuel + totalAjoute) / (quantiteActuelle + quantiteAjoutee);
                    }
                    
                    // Mettre à jour la valeur d'achat
                    Element valeurElement = (Element) ressource.getElementsByTagNameNS(
                        xmlService.getNamespaceUri(), "valeurAchat").item(0);
                    if (valeurElement != null) {
                        valeurElement.setTextContent(String.format("%.2f", nouvelleValeur));
                    }
                    
                    System.out.println("📊 Mise à jour ressource existante:");
                    System.out.println("   - Ancienne quantité: " + quantiteActuelle);
                    System.out.println("   - Nouvelle quantité: " + nouvelleQuantite);
                    System.out.println("   - Nouvelle valeur unitaire: " + nouvelleValeur);
                    
                    break;
                }
            }
            
            // 2. Si la ressource n'existe pas, la créer
            if (!ressourceTrouvee) {
                System.out.println("🆕 Création nouvelle ressource: '" + designation + "'");
                
                // Calculer la valeur unitaire
                double valeurUnitaire = 0.0;
                if (quantiteAjoutee > 0) {
                    valeurUnitaire = budget / quantiteAjoutee;
                }
                
                System.out.println("   - Valeur unitaire calculée: " + valeurUnitaire);
                
                // Créer l'élément RessourceMaterielle
                Element nouvelleRessource = doc.createElementNS(
                    xmlService.getNamespaceUri(), "RessourceMaterielle"
                );
                
                // Générer un nouvel ID
                int nouvelId = genererNouvelId(doc);
                
                // Ajouter les éléments
                ajouterElementTexte(doc, nouvelleRessource, "id", String.valueOf(nouvelId));
                ajouterElementTexte(doc, nouvelleRessource, "designation", designation);
                ajouterElementTexte(doc, nouvelleRessource, "quantiteEnStock", String.valueOf(quantiteAjoutee));
                ajouterElementTexte(doc, nouvelleRessource, "valeurAchat", String.format("%.2f", valeurUnitaire));
                ajouterElementTexte(doc, nouvelleRessource, "unite", "unité");
                
                // Ajouter au document
                root.appendChild(nouvelleRessource);
                
                System.out.println("✅ Nouvelle ressource créée avec ID: " + nouvelId);
            }
            
            // Sauvegarder le document
            xmlService.saveXmlDocument(doc, "RessourcesMaterielles");
            System.out.println("💾 Fichier sauvegardé.");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur critique gestion stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Le reste des méthodes reste identique...


    /**
     * Méthode pour récupérer le texte d'un élément (gère le namespace)
     */
    private String getElementText(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagNameNS(xmlService.getNamespaceUri(), tagName);
        if (nodes.getLength() > 0) {
            String text = nodes.item(0).getTextContent();
            return text != null ? text.trim() : null;
        }
        return null;
    }
    
    /**
     * Ajoute un élément texte avec namespace
     */
    private void ajouterElementTexte(Document doc, Element parent, String tagName, String texte) {
        Element element = doc.createElementNS(xmlService.getNamespaceUri(), tagName);
        if (texte != null) {
            element.setTextContent(texte);
        }
        parent.appendChild(element);
    }
    
    /**
     * Génère un nouvel ID pour une ressource
     */
    private int genererNouvelId(Document doc) {
        int maxId = 0;
        NodeList ressources = doc.getElementsByTagNameNS(
            xmlService.getNamespaceUri(), "RessourceMaterielle"
        );
        
        for (int i = 0; i < ressources.getLength(); i++) {
            Element ressource = (Element) ressources.item(i);
            String idStr = getElementText(ressource, "id");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    if (id > maxId) maxId = id;
                } catch (NumberFormatException e) {
                    // Ignorer
                }
            }
        }
        
        return maxId + 1;
    }

    public int getQuantiteStock(String designation) {
        try {
            Document doc = xmlService.loadXmlDocument("RessourcesMaterielles");
            Element root = doc.getDocumentElement();
             if (root == null) {
                System.out.println("⚠️ Aucune ressource dans le fichier");
                return 0;
            }
            NodeList ressources = root.getElementsByTagNameNS(
                xmlService.getNamespaceUri(), "RessourceMaterielle"
            );
            
            for (int i = 0; i < ressources.getLength(); i++) {
                Element ressource = (Element) ressources.item(i);
                String nomRessource = getElementText(ressource, "designation");
                
                if (nomRessource != null && nomRessource.equalsIgnoreCase(designation)) {
                    String quantiteStr = getElementText(ressource, "quantiteEnStock");
                    if (quantiteStr != null && !quantiteStr.trim().isEmpty()) {
                        return Integer.parseInt(quantiteStr.trim());
                    }
                }
            }
            
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
  // Méthode pour afficher tout le contenu du fichier (débogage)
    public void debugAfficherToutesRessources() {
        try {
            System.out.println("=== DEBUG: Contenu de ressources.xml ===");
            
            if (!xmlService.checkFileExists("RessourcesMaterielles")) {
                System.out.println("❌ Fichier n'existe pas");
                return;
            }
            
            Document doc = xmlService.loadXmlDocument("RessourcesMaterielles");
            Element root = doc.getDocumentElement();
            
            NodeList ressources = root.getElementsByTagNameNS(
                xmlService.getNamespaceUri(), "RessourceMaterielle"
            );
            
            System.out.println("Nombre de ressources: " + ressources.getLength());
            
            for (int i = 0; i < ressources.getLength(); i++) {
                Element ressource = (Element) ressources.item(i);
                System.out.println("\n--- Ressource #" + (i+1) + " ---");
                System.out.println("ID: " + getElementText(ressource, "id"));
                System.out.println("Désignation: " + getElementText(ressource, "designation"));
                System.out.println("Quantité: " + getElementText(ressource, "quantiteEnStock"));
                System.out.println("Valeur: " + getElementText(ressource, "valeurAchat"));
                System.out.println("Unité: " + getElementText(ressource, "unite"));
            }
            
            System.out.println("=== FIN DEBUG ===");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur debug: " + e.getMessage());
        }
    }
}  