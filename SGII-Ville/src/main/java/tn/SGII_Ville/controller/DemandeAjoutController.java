package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// Ajoutez ces imports au début du fichier
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import tn.SGII_Ville.service.XmlService;  // Ajoutez cet import
import tn.SGII_Ville.entities.DemandeAjout;
import tn.SGII_Ville.entities.Utilisateur;
import tn.SGII_Ville.model.enums.EtatDemandeAjoutType;
import tn.SGII_Ville.model.enums.RoleType;
import tn.SGII_Ville.model.enums.TypeDemandeAjout;
import tn.SGII_Ville.service.DemandeAjoutNotificationService;
import tn.SGII_Ville.service.DemandeAjoutXmlService;
import tn.SGII_Ville.service.StockRessourceService;
import tn.SGII_Ville.service.UserXmlService;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@RestController
@RequestMapping("/api/demandes-ajout")
@CrossOrigin(origins = "http://localhost:4200")
public class DemandeAjoutController {

    @Autowired
    private DemandeAjoutXmlService demandeAjoutService;

    @Autowired
    private DemandeAjoutNotificationService demandeAjoutNotificationService;

    @Autowired
    private UserXmlService userXmlService;

    // === ENDPOINTS CHEF DE SERVICE ===
    @Autowired  // AJOUTEZ CETTE INJECTION
    private XmlService xmlService;  // AJOUTEZ CETTE LIGNE

    @PostMapping
public ResponseEntity<?> creerDemande(@RequestBody CreateDemandeRequest request) {
    try {
        System.out.println("=== DÉBUT CRÉATION DEMANDE ===");
        System.out.println("Request: " + request);
        
        // Validation
        if (request.getChefId() <= 0) {
            return ResponseEntity.badRequest().body("ID du chef invalide");
        }
        
        System.out.println("✅ Validation OK");

        DemandeAjout demande = new DemandeAjout();
        demande.setTypeDemande(request.getTypeDemande());
        demande.setDesignation(request.getDesignation());
        demande.setQuantite(request.getQuantite());
        demande.setBudget(request.getBudget());
        demande.setJustification(request.getJustification());
        demande.setChefId(request.getChefId());
        
        // État déjà défini dans le constructeur
        System.out.println("📋 Demande créée - État: " + demande.getEtat());
        System.out.println("📋 Date demande: " + demande.getDateDemande());

        System.out.println("💾 Tentative de sauvegarde...");
        DemandeAjout savedDemande = demandeAjoutService.save(demande);
        System.out.println("✅ Demande sauvegardée avec ID: " + savedDemande.getId());

        // Notifier les administrateurs
        notifierNouvelleDemandeAjout(savedDemande);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedDemande);
    } catch (Exception e) {
        System.err.println("=== ERREUR CRITIQUE ===");
        System.err.println("Exception: " + e.getClass().getName());
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "error", "Erreur lors de la création de la demande",
                "details", e.getMessage(),
                "exception", e.getClass().getName()
            ));
    }
}

    @GetMapping("/chef/{chefId}")
    public ResponseEntity<List<DemandeAjout>> getDemandesParChef(@PathVariable int chefId) {
        try {
            List<DemandeAjout> demandes = demandeAjoutService.getDemandesByChef(chefId);
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === ENDPOINTS ADMINISTRATEUR ===

    @GetMapping("/admin/en-attente")
    public ResponseEntity<List<DemandeAjout>> getDemandesEnAttente() {
        try {
            List<DemandeAjout> demandes = demandeAjoutService.getDemandesEnAttente();
            System.out.println("📋 Demandes en attente: " + demandes.size());
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<List<DemandeAjout>> getAllDemandes() {
        try {
            List<DemandeAjout> demandes = demandeAjoutService.getAllDemandesAjout();
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

  @PostMapping("/admin/{demandeId}/accepter")
public ResponseEntity<?> accepterDemande(@PathVariable int demandeId, @RequestBody TraitementDemandeRequest request) {
    try {
        System.out.println("✅ [CONTROLLER] Acceptation demande #" + demandeId + " par admin #" + request.getAdminId());
        
        Optional<DemandeAjout> optionalDemande = demandeAjoutService.findById(demandeId);
        if (optionalDemande.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Demande non trouvée"));
        }

        DemandeAjout demande = optionalDemande.get();
        
        System.out.println("🔍 Détails demande trouvée:");
        System.out.println("   - ID: " + demande.getId());
        System.out.println("   - ChefId: " + demande.getChefId());
        System.out.println("   - Designation: " + demande.getDesignation());
        System.out.println("   - Type: " + demande.getTypeDemande());
        System.out.println("   - État actuel: " + demande.getEtat());

        // 🔥 CORRECTION ICI : Vérifier si c'est une demande RESSOURCE et mettre à jour le stock
        if (demande.getTypeDemande() == TypeDemandeAjout.RESSOURCE) {
            System.out.println("📦 Mise à jour du stock pour: " + demande.getDesignation());
            
            boolean stockMisAJour = stockRessourceService.augmenterStock(
                demande.getDesignation(), 
                demande.getQuantite(),
                demande.getBudget()
            );
            
            if (!stockMisAJour) {
                System.err.println("❌ Échec mise à jour stock pour: " + demande.getDesignation());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour du stock"));
            }
            
            System.out.println("✅ Stock mis à jour avec succès");
        }

        // Mettre à jour la demande
        demande.setEtat(EtatDemandeAjoutType.ACCEPTEE);
        demande.setAdminId(request.getAdminId());
        demande.setDateTraitement(LocalDateTime.now());

        DemandeAjout updatedDemande = demandeAjoutService.update(demande);
        
        // NOTIFIER LE VRAI CHEF
        System.out.println("🔔 Notification en cours pour le VRAI chef #" + updatedDemande.getChefId());
        notifierReponseDemandeAjout(updatedDemande, true, null);

        System.out.println("✅ Demande acceptée et notifiée au chef #" + updatedDemande.getChefId());
        return ResponseEntity.ok(updatedDemande);
    } catch (Exception e) {
        System.err.println("❌ Erreur acceptation: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Erreur lors de l'acceptation: " + e.getMessage()));
    }
}
    @PostMapping("/admin/{demandeId}/refuser")
    public ResponseEntity<?> refuserDemande(@PathVariable int demandeId, @RequestBody RefusDemandeRequest request) {
        try {
            System.out.println("❌ [CONTROLLER] Refus demande #" + demandeId + " par admin #" + request.getAdminId());
            
            Optional<DemandeAjout> optionalDemande = demandeAjoutService.findById(demandeId);
            if (optionalDemande.isEmpty()) {
                System.out.println("❌ Demande non trouvée pour refus: " + demandeId);
                return ResponseEntity.notFound().build();
            }

            DemandeAjout demande = optionalDemande.get();
            System.out.println("📋 Demande trouvée pour refus - ChefId: " + demande.getChefId());
            
            demande.setEtat(EtatDemandeAjoutType.REFUSEE);
            demande.setAdminId(request.getAdminId());
            demande.setDateTraitement(LocalDateTime.now());
            demande.setMotifRefus(request.getMotifRefus());

            DemandeAjout updatedDemande = demandeAjoutService.update(demande);

            // Notifier le chef de service
            notifierReponseDemandeAjout(updatedDemande, false, request.getMotifRefus());

            System.out.println("❌ Demande refusée et notifiée: " + updatedDemande);
            return ResponseEntity.ok(updatedDemande);
        } catch (Exception e) {
            System.err.println("❌ Erreur critique refus: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors du refus de la demande: " + e.getMessage()));
        }
    }

    // === ENDPOINTS DE DEBUG SIMPLIFIÉS ===
    @GetMapping("/debug/info/{userId}")
    public ResponseEntity<?> debugInfo(@PathVariable int userId) {
        try {
            System.out.println("🐛 DEBUG Info pour user #" + userId);
            
            // Vérifier les administrateurs
            List<Utilisateur> admins = userXmlService.findAll().stream()
                .filter(u -> u.getRole() == RoleType.ADMINISTRATEUR)
                .collect(Collectors.toList());
                
            // Vérifier les demandes du chef
            List<DemandeAjout> demandesChef = demandeAjoutService.getDemandesByChef(userId);
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("userId", userId);
            debugInfo.put("adminsCount", admins.size());
            debugInfo.put("admins", admins.stream().map(u -> Map.of("id", u.getId(), "nom", u.getNom(), "role", u.getRole())).collect(Collectors.toList()));
            debugInfo.put("demandesChefCount", demandesChef.size());
            debugInfo.put("demandesChef", demandesChef.stream().map(d -> Map.of(
                "id", d.getId(), 
                "etat", d.getEtat(), 
                "designation", d.getDesignation(),
                "chefId", d.getChefId()
            )).collect(Collectors.toList()));
            
            System.out.println("🐛 DEBUG Info:");
            System.out.println("   - Admins: " + admins.size());
            System.out.println("   - Demandes chef: " + demandesChef.size());
            System.out.println("   - ChefId testé: " + userId);
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur debug: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/debug/test-notification")
    public ResponseEntity<?> testNotification(@RequestBody TestNotificationRequest request) {
        try {
            System.out.println("🧪 TEST Notification manuelle pour user #" + request.getUserId());
            
            demandeAjoutNotificationService.testerNotification(request.getUserId(), request.getMessage());
            
            return ResponseEntity.ok(Map.of(
                "message", "Notification test envoyée",
                "userId", request.getUserId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // === ENDPOINT DE TEST ===
    @GetMapping("/admin/test-notification/{demandeId}")
    public ResponseEntity<?> testNotificationDemande(@PathVariable int demandeId) {
        try {
            System.out.println("🧪 TEST Notification pour demande #" + demandeId);
            
            Optional<DemandeAjout> optionalDemande = demandeAjoutService.findById(demandeId);
            if (optionalDemande.isEmpty()) {
                System.out.println("❌ Demande non trouvée pour test");
                return ResponseEntity.notFound().build();
            }

            DemandeAjout demande = optionalDemande.get();
            System.out.println("🧪 Détails demande test:");
            System.out.println("🧪   - ID: " + demande.getId());
            System.out.println("🧪   - ChefId: " + demande.getChefId());
            System.out.println("🧪   - État: " + demande.getEtat());
            System.out.println("🧪   - Désignation: " + demande.getDesignation());

            // Tester la notification
            System.out.println("🧪 Envoi notification test...");
            demandeAjoutNotificationService.notifierReponseDemandeAjout(demande, true, "Test de notification");

            return ResponseEntity.ok(Map.of(
                "message", "Notification test envoyée",
                "chefId", demande.getChefId(),
                "demandeId", demande.getId(),
                "designation", demande.getDesignation()
            ));
        } catch (Exception e) {
            System.err.println("❌ Erreur test notification: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // === MÉTHODES DE NOTIFICATION ===

    private void notifierNouvelleDemandeAjout(DemandeAjout demande) {
        System.out.println("📨 Notification nouvelle demande #" + demande.getId());
        demandeAjoutNotificationService.notifierNouvelleDemandeAjout(demande);
    }

    private void notifierReponseDemandeAjout(DemandeAjout demande, boolean acceptee, String motifRefus) {
        System.out.println("📨 Notification réponse demande #" + demande.getId() + " - Acceptée: " + acceptee);
        demandeAjoutNotificationService.notifierReponseDemandeAjout(demande, acceptee, motifRefus);
    }

    // === CLASSES REQUEST (DOIVENT ÊTRE STATIC) ===

    public static class CreateDemandeRequest {
        private TypeDemandeAjout typeDemande;
        private String designation;
        private int quantite;
        private double budget;
        private String justification;
        private int chefId;

        // Getters et Setters
        public TypeDemandeAjout getTypeDemande() { return typeDemande; }
        public void setTypeDemande(TypeDemandeAjout typeDemande) { this.typeDemande = typeDemande; }
        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
        public int getQuantite() { return quantite; }
        public void setQuantite(int quantite) { this.quantite = quantite; }
        public double getBudget() { return budget; }
        public void setBudget(double budget) { this.budget = budget; }
        public String getJustification() { return justification; }
        public void setJustification(String justification) { this.justification = justification; }
        public int getChefId() { return chefId; }
        public void setChefId(int chefId) { this.chefId = chefId; }

        @Override
        public String toString() {
            return "CreateDemandeRequest{" +
                    "typeDemande=" + typeDemande +
                    ", designation='" + designation + '\'' +
                    ", quantite=" + quantite +
                    ", budget=" + budget +
                    ", justification='" + justification + '\'' +
                    ", chefId=" + chefId +
                    '}';
        }
    }

    public static class TraitementDemandeRequest {
        private int adminId;

        public int getAdminId() { return adminId; }
        public void setAdminId(int adminId) { this.adminId = adminId; }

        @Override
        public String toString() {
            return "TraitementDemandeRequest{" +
                    "adminId=" + adminId +
                    '}';
        }
    }

    public static class RefusDemandeRequest {
        private int adminId;
        private String motifRefus;

        public int getAdminId() { return adminId; }
        public void setAdminId(int adminId) { this.adminId = adminId; }
        public String getMotifRefus() { return motifRefus; }
        public void setMotifRefus(String motifRefus) { this.motifRefus = motifRefus; }

        @Override
        public String toString() {
            return "RefusDemandeRequest{" +
                    "adminId=" + adminId +
                    ", motifRefus='" + motifRefus + '\'' +
                    '}';
        }
    }
    
    public static class TestNotificationRequest {
        private int userId;
        private String message;
        
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    @PostMapping("/test-notification-chef")
public ResponseEntity<?> testNotificationChef(@RequestBody TestNotificationChefRequest request) {
    try {
        System.out.println("🧪 TEST Notification manuelle pour chef #" + request.getChefId());
        
        // Créer une demande fictive pour tester
        DemandeAjout demandeTest = new DemandeAjout();
        demandeTest.setId(999);
        demandeTest.setChefId(request.getChefId());
        demandeTest.setDesignation("TEST - Équipement de test");
        demandeTest.setQuantite(1);
        demandeTest.setBudget(100.0);
        demandeTest.setTypeDemande(TypeDemandeAjout.EQUIPEMENT);
        
        // Tester l'acceptation
        demandeAjoutNotificationService.notifierReponseDemandeAjout(demandeTest, true, "Test d'acceptation");
        
        return ResponseEntity.ok(Map.of(
            "message", "Notification test envoyée au chef",
            "chefId", request.getChefId(),
            "test", "acceptation"
        ));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}

public static class TestNotificationChefRequest {
    private int chefId;
    
    public int getChefId() { return chefId; }
    public void setChefId(int chefId) { this.chefId = chefId; }
}
@PostMapping("/debug/test-notification-chef")
public ResponseEntity<?> debugTestNotificationChef(@RequestBody Map<String, Integer> request) {
    try {
        int chefId = request.get("chefId");
        System.out.println("🧪 DEBUG TEST Notification pour chef #" + chefId);
        
        // Créer une demande fictive
        DemandeAjout demandeTest = new DemandeAjout();
        demandeTest.setId(999);
        demandeTest.setChefId(chefId);
        demandeTest.setDesignation("TEST - Notification debug");
        demandeTest.setQuantite(1);
        demandeTest.setBudget(100.0);
        demandeTest.setTypeDemande(TypeDemandeAjout.EQUIPEMENT);
        
        // Tester la notification
        demandeAjoutNotificationService.notifierReponseDemandeAjout(demandeTest, true, "Test debug");
        
        return ResponseEntity.ok(Map.of(
            "message", "Notification debug envoyée",
            "chefId", chefId,
            "status", "success"
        ));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}
// Dans DemandeAjoutController.java

@GetMapping("/admin/ressources/en-attente")
public ResponseEntity<List<DemandeAjout>> getDemandesRessourcesEnAttente() {
    try {
        List<DemandeAjout> demandes = demandeAjoutService.getAllDemandesAjout().stream()
            .filter(d -> d.getEtat() == EtatDemandeAjoutType.EN_ATTENTE_ADMIN 
                      && d.getTypeDemande() == TypeDemandeAjout.RESSOURCE)
            .collect(Collectors.toList());
        System.out.println("📦 Demandes ressources en attente: " + demandes.size());
        return ResponseEntity.ok(demandes);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

@GetMapping("/admin/equipements/en-attente")
public ResponseEntity<List<DemandeAjout>> getDemandesEquipementsEnAttente() {
    try {
        List<DemandeAjout> demandes = demandeAjoutService.getAllDemandesAjout().stream()
            .filter(d -> d.getEtat() == EtatDemandeAjoutType.EN_ATTENTE_ADMIN 
                      && d.getTypeDemande() == TypeDemandeAjout.EQUIPEMENT)
            .collect(Collectors.toList());
        System.out.println("🛠️ Demandes équipements en attente: " + demandes.size());
        return ResponseEntity.ok(demandes);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
@GetMapping("/debug/ressources")
public ResponseEntity<?> debugRessources() {
    try {
        // Vérifier si le fichier existe
        String filePath = "src/main/resources/data/ressources.xml";
        File file = new File(filePath);
        System.out.println("📁 Fichier ressources.xml existe: " + file.exists());
        System.out.println("📁 Chemin absolu: " + file.getAbsolutePath());
        
        // Essayer de charger le document
        Document doc = xmlService.loadXmlDocument("RessourcesMaterielles");
        Element root = doc.getDocumentElement();
        System.out.println("📝 Élément racine: " + root.getTagName());
        
        // Compter les ressources
        NodeList ressources = doc.getElementsByTagNameNS(
            xmlService.getNamespaceUri(), "RessourceMaterielle"
        );
        System.out.println("📊 Nombre de ressources: " + ressources.getLength());
        
        // Afficher le contenu XML
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        
        return ResponseEntity.ok(Map.of(
            "fileExists", file.exists(),
            "rootElement", root.getTagName(),
            "resourceCount", ressources.getLength(),
            "xmlContent", writer.toString()
        ));
        
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}
// Endpoint pour accepter et mettre à jour le stock
// Ajouter cette méthode dans DemandeAjoutController.java
@Autowired
private StockRessourceService stockRessourceService;

@PostMapping("/admin/{demandeId}/accepter-et-mettre-a-jour")
public ResponseEntity<?> accepterEtMettreAJour(@PathVariable int demandeId, 
                                               @RequestBody TraitementDemandeRequest request) {
    try {
        System.out.println("🔄 Acceptation et mise à jour stock demande #" + demandeId);
        
        Optional<DemandeAjout> optionalDemande = demandeAjoutService.findById(demandeId);
        if (optionalDemande.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Demande non trouvée"));
        }

        DemandeAjout demande = optionalDemande.get();
        
        // Vérifier si c'est une demande de ressource
        if (demande.getTypeDemande() != TypeDemandeAjout.RESSOURCE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Cette demande n'est pas une demande de ressource"));
        }

        // Mettre à jour la demande
        demande.setEtat(EtatDemandeAjoutType.ACCEPTEE);
        demande.setAdminId(request.getAdminId());
        demande.setDateTraitement(LocalDateTime.now());

        // Mettre à jour le stock de la ressource
        boolean stockMisAJour = stockRessourceService.augmenterStock(
            demande.getDesignation(), 
            demande.getQuantite(),
            demande.getBudget()
        );
        
        if (!stockMisAJour) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la mise à jour du stock"));
        }

        DemandeAjout updatedDemande = demandeAjoutService.update(demande);
        
        // Notifier le chef
        notifierReponseDemandeAjout(updatedDemande, true, "Demande acceptée et stock mis à jour");

        System.out.println("✅ Demande acceptée et stock mis à jour");
        return ResponseEntity.ok(Map.of(
            "message", "Demande acceptée et stock mis à jour avec succès",
            "demande", updatedDemande,
            "stockMisAJour", true
        ));
    } catch (Exception e) {
        System.err.println("❌ Erreur acceptation et mise à jour: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Erreur lors de l'acceptation et mise à jour: " + e.getMessage()));
    }
}
@PostMapping("/test/add-ressource")
public ResponseEntity<?> testAddRessource(@RequestBody Map<String, Object> request) {
    try {
        String designation = (String) request.get("designation");
        int quantite = (int) request.get("quantite");
        double budget = (double) request.get("budget");
        
        System.out.println("🧪 TEST: Ajout de ressource");
        System.out.println("   - Désignation: " + designation);
        System.out.println("   - Quantité: " + quantite);
        System.out.println("   - Budget: " + budget);
        
        // Appeler directement le service
        boolean success = stockRessourceService.augmenterStock(designation, quantite, budget);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "message", "Ressource ajoutée avec succès",
                "designation", designation,
                "quantite", quantite
            ));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Échec de l'ajout"));
        }
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}
}