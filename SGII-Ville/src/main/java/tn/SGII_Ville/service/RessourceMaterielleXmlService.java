package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

@Service
public class RessourceMaterielleXmlService {

    @Autowired
    private XmlService xmlService;

    /**
     * Réduit le stock d'une ressource matérielle
     */
    public void reduireStock(Integer materielId, Integer quantite) {
        try {
            System.out.println("=== DÉBUT reduireStock ===");
            System.out.println("Matériel ID: " + materielId);
            System.out.println("Quantité à réduire: " + quantite);
            
            Document doc = xmlService.loadXmlDocument("RessourcesMaterielles");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "RessourceMaterielle");
            
            System.out.println("Nombre de RessourceMaterielle trouvées: " + nodes.getLength());
            
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                String idStr = xmlService.getElementTextContent(el, "id");
                System.out.println("RessourceMaterielle[" + i + "] id=" + idStr);
                
                if (idStr != null && Integer.parseInt(idStr.trim()) == materielId) {
                    System.out.println("Matériel trouvé!");
                    
                    // Récupérer le stock actuel
                    String stockStr = xmlService.getElementTextContent(el, "quantiteEnStock");
                    System.out.println("Stock actuel (string): " + stockStr);
                    
                    int stockActuel = Integer.parseInt(stockStr.trim());
                    System.out.println("Stock actuel (int): " + stockActuel);
                    
                    if (stockActuel < quantite) {
                        throw new RuntimeException("Stock insuffisant pour le matériel " + materielId + 
                            ". Stock actuel: " + stockActuel + ", demandé: " + quantite);
                    }
                    
                    // Calculer le nouveau stock
                    int nouveauStock = stockActuel - quantite;
                    System.out.println("Nouveau stock: " + nouveauStock);
                    
                    // Mettre à jour l'élément
                    NodeList stockNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "quantiteEnStock");
                    System.out.println("Nombre de nœuds quantiteEnStock: " + stockNodes.getLength());
                    
                    if (stockNodes.getLength() > 0) {
                        stockNodes.item(0).setTextContent(String.valueOf(nouveauStock));
                        System.out.println("Stock mis à jour dans le DOM");
                    }
                    
                    xmlService.saveXmlDocument(doc, "RessourcesMaterielles");
                    System.out.println("Document XML sauvegardé");
                    System.out.println("=== FIN reduireStock (succès) ===");
                    return;
                }
            }
            throw new RuntimeException("Matériel non trouvé: " + materielId);
        } catch (Exception e) {
            System.err.println("=== ERREUR dans reduireStock ===");
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la réduction du stock", e);
        }
    }
}
