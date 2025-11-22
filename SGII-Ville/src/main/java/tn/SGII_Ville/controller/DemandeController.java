package tn.SGII_Ville.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import tn.SGII_Ville.entities.Demande;
import tn.SGII_Ville.entities.Intervention;
import tn.SGII_Ville.entities.Photo;
import tn.SGII_Ville.model.enums.EtatDemandeType;
import tn.SGII_Ville.service.DemandeXmlService;
import tn.SGII_Ville.service.FileStorageService;
import tn.SGII_Ville.service.InterventionXmlService;

/**
 * Contrôleur REST pour gérer les demandes citoyennes
 */
@RestController
@RequestMapping("/api/demandes")
@CrossOrigin(origins = "http://localhost:4200")
public class DemandeController {

    private static final Logger logger = LoggerFactory.getLogger(DemandeController.class);

    @Autowired
    private DemandeXmlService demandeService;

    @Autowired
    private InterventionXmlService interventionService;

    @Autowired
    private tn.SGII_Ville.service.NotificationService notificationService;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${nominatim.contact:}")
    private String nominatimContact;

    // ==================== GET ALL DEMANDES ====================
    @GetMapping
    public ResponseEntity<List<Demande>> getAllDemandes() {
        try {
            List<Demande> demandes = demandeService.getAllDemandes();
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de toutes les demandes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== GET DEMANDE BY ID ====================
    @GetMapping("/{id}")
    public ResponseEntity<Demande> getDemandeById(@PathVariable int id) {
        try {
            Demande demande = demandeService.findById(id);
            return (demande != null) ? ResponseEntity.ok(demande) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la demande ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== GET DEMANDES BY CITOYEN ID ====================
    @GetMapping("/citoyen/{citoyenId}")
    public ResponseEntity<List<Demande>> getDemandesByCitoyen(@PathVariable int citoyenId) {
        try {
            List<Demande> toutes = demandeService.getAllDemandes();
            List<Demande> demandesCitoyen = toutes.stream()
                    .filter(d -> d.getCitoyenId() != null && d.getCitoyenId() == citoyenId)
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(demandesCitoyen);
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des demandes du citoyen ID: {}", citoyenId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== CREATE DEMANDE (JSON ONLY) ====================
    @PostMapping
    public ResponseEntity<?> createDemande(@RequestBody Demande demande) {
        try {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("🆕 POST /api/demandes - CRÉATION DE DEMANDE");
            System.out.println("=".repeat(80));
            System.out.println("📋 Description: " + demande.getDescription());
            System.out.println("👤 CitoyenId: " + demande.getCitoyenId());
            System.out.println("🏷️  Catégorie: " + demande.getCategory());
            System.out.println("📍 Localisation: " + demande.getLocalisation());
            
            logger.info("Réception demande: description={}, citoyenId={}, category={}", 
                       demande.getDescription(), demande.getCitoyenId(), demande.getCategory());
            
            if (demande.getDescription() == null || demande.getDescription().trim().isEmpty()) {
                System.out.println("❌ ERREUR: Description vide");
                return ResponseEntity.badRequest().body(Map.of("error", "La description est obligatoire"));
            }
            if (demande.getEtat() == null) demande.setEtat(EtatDemandeType.SOUMISE);
            if (demande.getDateSoumission() == null) demande.setDateSoumission(java.time.LocalDate.now());

            System.out.println("💾 Sauvegarde de la demande...");
            Demande nouvelle = demandeService.save(demande);
            System.out.println("✅ Demande sauvegardée avec ID: " + nouvelle.getId());
            
            logger.info("Demande créée avec succès: ID={}, citoyenId={}", nouvelle.getId(), nouvelle.getCitoyenId());
            
            // Créer notification pour le chef de service
            System.out.println("🔔 Envoi notification au chef de service...");
            notificationService.notifierNouvelleDemande(nouvelle.getId(), 
                nouvelle.getDescription().length() > 50 ? nouvelle.getDescription().substring(0, 50) + "..." : nouvelle.getDescription());
            System.out.println("✅ Notification envoyée avec succès");
            System.out.println("=".repeat(80) + "\n");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(nouvelle);
        } catch (Exception e) {
            logger.error("Erreur création demande JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur serveur", "details", e.getMessage()));
        }
    }

    // ==================== REVERSE GEOCODE PROXY ====================
    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(@RequestParam double lat, @RequestParam double lon) {
        try {
            String contactEmail = nominatimContact != null && !nominatimContact.isBlank()
                    ? nominatimContact : "eya.boussarsar@example.com";

            String encodedEmail = URLEncoder.encode(contactEmail, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s&zoom=18&addressdetails=1&email=%s",
                    lat, lon, encodedEmail);

            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "SGIIVILLE/1.0 (contact: " + contactEmail + ")")
                    .header("Accept", "application/json")
                    .header("Referer", "http://localhost:4200")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body();

            if (status == 200) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
            }
            if (status == 429) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Trop de requêtes Nominatim, réessayez plus tard"));
            }
            if (status == 403) {
                return ResponseEntity.ok(Map.of(
                        "warning", "Nominatim a bloqué la requête (403)",
                        "address", String.format("%s,%s", lat, lon),
                        "source", "coords"
                ));
            }

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Échec géocodage inverse", "status", status));

        } catch (Exception e) {
            logger.error("Erreur reverse-geocode lat={} lon={}", lat, lon, e);
            return ResponseEntity.ok(Map.of(
                    "address", String.format("%s,%s", lat, lon),
                    "source", "coords",
                    "warning", "Erreur géocodage"
            ));
        }
    }

    // ==================== CREATE DEMANDE WITH FILES (MULTIPART) ====================
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createDemandeMultipart(
            @RequestPart(value = "demande", required = false) String demandeJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        try {
            if (demandeJson == null || demandeJson.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le JSON 'demande' est requis"));
            }

            ObjectMapper om = new ObjectMapper();
            om.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            Demande demande = om.readValue(demandeJson, Demande.class);

            if (demande.getDescription() == null || demande.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La description est obligatoire"));
            }
            if (demande.getEtat() == null) demande.setEtat(EtatDemandeType.SOUMISE);

            // Gestion des photos
            if (files != null && files.length > 0) {
                List<Photo> savedPhotos = fileStorageService.storeFiles(files);
                List<Integer> photoIds = savedPhotos.stream().map(Photo::getIdPhoto).toList();
                demande.setPhotoRefs(photoIds);
            }

            Demande nouvelle = demandeService.save(demande);
            
            // Créer notification pour le chef de service
            notificationService.notifierNouvelleDemande(nouvelle.getId(), 
                nouvelle.getDescription().length() > 50 ? nouvelle.getDescription().substring(0, 50) + "..." : nouvelle.getDescription());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(nouvelle);

        } catch (Exception e) {
            logger.error("Erreur création demande multipart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur serveur lors de la création", "details", e.getMessage()));
        }
    }

    // ==================== SERVE UPLOADED FILES ====================
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<byte[]> serveUpload(@PathVariable String filename) {
        try {
            Path p = fileStorageService.getFilePath(filename);
            if (p == null || !Files.exists(p)) return ResponseEntity.notFound().build();

            String contentType = Files.probeContentType(p);
            if (contentType == null) contentType = "application/octet-stream";

            byte[] data = Files.readAllBytes(p);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du fichier: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== PLANIFIER UNE DEMANDE (CORRIGÉ & FINAL) ====================
    
    @PostMapping("/planifier/{id}")
public ResponseEntity<?> planifierDemande(@PathVariable int id) {
    try {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 POST /api/demandes/planifier/" + id + " - PLANIFICATION INTERVENTION");
        System.out.println("=".repeat(80));
        
        logger.info("DÉBUT PLANIFICATION DE LA DEMANDE #{}", id);

        System.out.println("🔍 Recherche de la demande #" + id + "...");
        Demande demande = demandeService.findById(id);
        if (demande == null) {
            System.out.println("❌ ERREUR: Demande non trouvée");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Demande non trouvée : " + id));
        }
        System.out.println("✅ Demande trouvée: ID=" + demande.getId() + ", CitoyenId=" + demande.getCitoyenId());

        if (demande.getEtat() == EtatDemandeType.TRAITEE) {
            System.out.println("⚠️ Demande déjà planifiée");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Cette demande est déjà planifiée."));
        }

        System.out.println("💾 Création de l'intervention...");
        Intervention intervention = interventionService.planifierDemande(id);
        System.out.println("✅ Intervention créée avec ID: " + intervention.getId());

        logger.info("PLANIFICATION RÉUSSIE → Intervention #{} créée", intervention.getId());
        
        // NOTIFICATIONS
        System.out.println("\n📢 ENVOI DES NOTIFICATIONS:");
        System.out.println("-".repeat(80));
        
        // 1. Notifier l'admin de la nouvelle intervention
        System.out.println("1️⃣ Notification ADMIN pour nouvelle intervention...");
        notificationService.notifierNouvelleIntervention(intervention.getId(), id);
        System.out.println("   ✅ Notification admin envoyée");
        
        // 2. Notifier le citoyen que sa demande est acceptée
        if (demande.getCitoyenId() != null) {
            System.out.println("2️⃣ Notification CITOYEN (ID: " + demande.getCitoyenId() + ") pour intervention lancée...");
            notificationService.notifierCitoyenInterventionLancee(demande.getCitoyenId(), id, intervention.getId());
            System.out.println("   ✅ Notification citoyen envoyée");
        } else {
            System.out.println("⚠️ Aucun citoyenId pour la demande #" + id + " - Pas de notification citoyen");
        }
        System.out.println("=".repeat(80) + "\n");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(intervention);

    } catch (Exception e) {
        logger.error("ERREUR PLANIFICATION DEMANDE #{}", id, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la planification", "details", e.getMessage()));
    }
}
}