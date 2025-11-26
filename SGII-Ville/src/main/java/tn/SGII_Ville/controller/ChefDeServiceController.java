package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.Technicien;
import tn.SGII_Ville.service.UserXmlService;

import java.util.List;

@RestController
@RequestMapping("/api/chef")
@CrossOrigin(origins = "http://localhost:4200")
public class ChefDeServiceController {

    @Autowired
    private UserXmlService userXmlService;

    /**
     * GET /api/chef/techniciens
     * → Retourne uniquement les techniciens depuis ton service existant
     */
    @GetMapping("/techniciens")
    public ResponseEntity<List<Technicien>> getAllTechniciens() {
        try {
            // Lire dynamiquement depuis utilisateurs.xml (pas statique)
            List<Technicien> techniciens = userXmlService.findAllTechniciensFromXml();
            return ResponseEntity.ok(techniciens);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}