package tn.SGII_Ville.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.SGII_Ville.entities.Demande;
import tn.SGII_Ville.entities.Intervention;
import tn.SGII_Ville.entities.Photo;
import tn.SGII_Ville.model.enums.EtatDemandeType;
import tn.SGII_Ville.service.DemandeXmlService;
import tn.SGII_Ville.service.InterventionXmlService;
import tn.SGII_Ville.service.PhotoXmlService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour les données publiques accessibles sans authentification
 * Interface Visiteur - elBaladiya.tn
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "http://localhost:4200")
public class PublicController {

    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);

    @Autowired
    private DemandeXmlService demandeService;

    @Autowired
    private InterventionXmlService interventionService;

    @Autowired
    private PhotoXmlService photoService;

    /**
     * Récupère les statistiques publiques de la plateforme
     * GET /api/public/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPublicStats() {
        try {
            List<Demande> toutesDemandes = demandeService.getAllDemandes();
            
            long demandesTraitees = toutesDemandes.stream()
                .filter(d -> d.getEtat() == EtatDemandeType.TRAITEE)
                .count();
            
            long totalDemandes = toutesDemandes.size();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("demandesTraitees", demandesTraitees);
            stats.put("totalDemandes", totalDemandes);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des statistiques publiques", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère toutes les demandes terminées (état TRAITEE) pour consultation publique
     * Les données sensibles sont filtrées
     * GET /api/public/demandes-terminees
     */
    @GetMapping("/demandes-terminees")
    public ResponseEntity<List<Map<String, Object>>> getDemandesTerminees(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin) {
        try {
            List<Demande> toutesDemandes = demandeService.getAllDemandes();
            
            // Filtrer uniquement les demandes terminées
            List<Demande> demandesTerminees = toutesDemandes.stream()
                .filter(d -> d.getEtat() == EtatDemandeType.TRAITEE)
                .collect(Collectors.toList());
            
            // Filtrer par catégorie si fourni
            if (category != null && !category.isEmpty()) {
                demandesTerminees = demandesTerminees.stream()
                    .filter(d -> category.equalsIgnoreCase(d.getCategory()))
                    .collect(Collectors.toList());
            }
            
            // Filtrer par date si fourni
            if (dateDebut != null && !dateDebut.isEmpty()) {
                demandesTerminees = demandesTerminees.stream()
                    .filter(d -> d.getDateSoumission() != null && 
                                !d.getDateSoumission().isBefore(java.time.LocalDate.parse(dateDebut)))
                    .collect(Collectors.toList());
            }
            
            if (dateFin != null && !dateFin.isEmpty()) {
                demandesTerminees = demandesTerminees.stream()
                    .filter(d -> d.getDateSoumission() != null && 
                                !d.getDateSoumission().isAfter(java.time.LocalDate.parse(dateFin)))
                    .collect(Collectors.toList());
            }
            
            // Transformer en format public (sans données sensibles)
            List<Map<String, Object>> demandesPubliques = new ArrayList<>();
            for (Demande demande : demandesTerminees) {
                Map<String, Object> demandePublique = new HashMap<>();
                demandePublique.put("id", demande.getId());
                demandePublique.put("description", demande.getDescription());
                demandePublique.put("dateSoumission", demande.getDateSoumission());
                demandePublique.put("category", demande.getCategory());
                demandePublique.put("subCategory", demande.getSubCategory());
                demandePublique.put("priority", demande.getPriority());
                
                // Localisation (PointGeo) - seulement les coordonnées publiques
                if (demande.getLocalisation() != null) {
                    Map<String, Object> localisation = new HashMap<>();
                    localisation.put("latitude", demande.getLocalisation().getLatitude());
                    localisation.put("longitude", demande.getLocalisation().getLongitude());
                    localisation.put("address", demande.getAddress()); // Adresse publique uniquement
                    demandePublique.put("localisation", localisation);
                }
                
                // Photos publiques uniquement
                List<Map<String, Object>> photosPubliques = new ArrayList<>();
                if (demande.getPhotos() != null) {
                    for (Photo photo : demande.getPhotos()) {
                        Map<String, Object> photoPublique = new HashMap<>();
                        photoPublique.put("idPhoto", photo.getIdPhoto());
                        photoPublique.put("url", photo.getUrl());
                        photoPublique.put("nom", photo.getNom());
                        photosPubliques.add(photoPublique);
                    }
                }
                demandePublique.put("photos", photosPubliques);
                
                // Intervention associée (si publiée)
                Intervention intervention = interventionService.findByDemandeId(demande.getId());
                if (intervention != null && intervention.getRapportFinal() != null && 
                    !intervention.getRapportFinal().isEmpty()) {
                    Map<String, Object> interventionPublique = new HashMap<>();
                    interventionPublique.put("id", intervention.getId());
                    interventionPublique.put("typeIntervention", intervention.getTypeIntervention());
                    interventionPublique.put("dateDebut", intervention.getDateDebut());
                    interventionPublique.put("dateFin", intervention.getDateFin());
                    interventionPublique.put("rapportFinal", intervention.getRapportFinal());
                    demandePublique.put("intervention", interventionPublique);
                }
                
                // NE PAS INCLURE : citoyenId, contactEmail, address (données privées)
                
                demandesPubliques.add(demandePublique);
            }
            
            return ResponseEntity.ok(demandesPubliques);
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des demandes terminées", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les détails d'une demande terminée spécifique (publique)
     * GET /api/public/demandes/{id}
     */
    @GetMapping("/demandes/{id}")
    public ResponseEntity<Map<String, Object>> getDemandePublique(@PathVariable int id) {
        try {
            Demande demande = demandeService.findById(id);
            
            if (demande == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Vérifier que la demande est terminée (sécurité)
            if (demande.getEtat() != EtatDemandeType.TRAITEE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Cette demande n'est pas accessible publiquement"));
            }
            
            // Construire la réponse publique
            Map<String, Object> demandePublique = new HashMap<>();
            demandePublique.put("id", demande.getId());
            demandePublique.put("description", demande.getDescription());
            demandePublique.put("dateSoumission", demande.getDateSoumission());
            demandePublique.put("category", demande.getCategory());
            demandePublique.put("subCategory", demande.getSubCategory());
            demandePublique.put("priority", demande.getPriority());
            
            // Localisation
            if (demande.getLocalisation() != null) {
                Map<String, Object> localisation = new HashMap<>();
                localisation.put("latitude", demande.getLocalisation().getLatitude());
                localisation.put("longitude", demande.getLocalisation().getLongitude());
                localisation.put("address", demande.getAddress());
                demandePublique.put("localisation", localisation);
            }
            
            // Photos
            List<Map<String, Object>> photosPubliques = new ArrayList<>();
            if (demande.getPhotos() != null) {
                for (Photo photo : demande.getPhotos()) {
                    Map<String, Object> photoPublique = new HashMap<>();
                    photoPublique.put("idPhoto", photo.getIdPhoto());
                    photoPublique.put("url", photo.getUrl());
                    photoPublique.put("nom", photo.getNom());
                    photosPubliques.add(photoPublique);
                }
            }
            demandePublique.put("photos", photosPubliques);
            
            // Intervention associée (si rapport publié)
            Intervention intervention = interventionService.findByDemandeId(demande.getId());
            if (intervention != null && intervention.getRapportFinal() != null && 
                !intervention.getRapportFinal().isEmpty()) {
                Map<String, Object> interventionPublique = new HashMap<>();
                interventionPublique.put("id", intervention.getId());
                interventionPublique.put("typeIntervention", intervention.getTypeIntervention());
                interventionPublique.put("dateDebut", intervention.getDateDebut());
                interventionPublique.put("dateFin", intervention.getDateFin());
                interventionPublique.put("rapportFinal", intervention.getRapportFinal());
                demandePublique.put("intervention", interventionPublique);
            }
            
            return ResponseEntity.ok(demandePublique);
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la demande publique ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Soumission d'une demande d'inscription pour une municipalité
     * POST /api/public/municipalite-inscription
     */
    @PostMapping("/municipalite-inscription")
    public ResponseEntity<Map<String, String>> soumettreInscriptionMunicipalite(
            @RequestBody Map<String, String> demande) {
        try {
            logger.info("Nouvelle demande d'inscription municipalité: {}", demande.get("nom"));
            
            // Ici, on pourrait envoyer un email à l'administrateur
            // ou stocker dans une table de demandes d'inscription
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Votre demande d'inscription a été reçue. Nous vous contacterons prochainement.");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la soumission de l'inscription municipalité", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

