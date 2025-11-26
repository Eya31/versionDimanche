package tn.SGII_Ville.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.RessourceMaterielle;
import tn.SGII_Ville.service.RessourceMaterielleService;

import java.util.List;

@RestController
@RequestMapping("/api/ressources")
@CrossOrigin(origins = "http://localhost:4200")
public class RessourceMaterielleController {

    private final RessourceMaterielleService service;

    public RessourceMaterielleController(RessourceMaterielleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RessourceMaterielle>> getAll() {
        try {
            List<RessourceMaterielle> ressources = service.getAll();
            return ResponseEntity.ok(ressources);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RessourceMaterielle> getOne(@PathVariable int id) {
        try {
            RessourceMaterielle ressource = service.getById(id);
            if (ressource != null) {
                return ResponseEntity.ok(ressource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<RessourceMaterielle> create(@RequestBody RessourceMaterielle r) {
        try {
            RessourceMaterielle nouvelleRessource = service.create(r);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouvelleRessource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
public ResponseEntity<RessourceMaterielle> update(@PathVariable int id, @RequestBody RessourceMaterielle r) {
    try {
        RessourceMaterielle updated = service.update(id, r);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}