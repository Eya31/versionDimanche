package tn.SGII_Ville.model;

import java.util.List;

public class DisponibiliteTechnicien {
    private Integer technicienId;
    private String nom;
    private String email;
    private List<String> competences;
    private List<CreneauDisponibilite> disponibilites;
    private Boolean disponible;
    private Double tauxDisponibilite;

    // Constructeurs
    public DisponibiliteTechnicien() {}

    public DisponibiliteTechnicien(Integer technicienId, String nom, String email, 
                                  List<String> competences, List<CreneauDisponibilite> disponibilites, 
                                  Boolean disponible) {
        this.technicienId = technicienId;
        this.nom = nom;
        this.email = email;
        this.competences = competences;
        this.disponibilites = disponibilites;
        this.disponible = disponible;
        this.tauxDisponibilite = calculerTauxDisponibilite();
    }

    // Getters et Setters
    public Integer getTechnicienId() { return technicienId; }
    public void setTechnicienId(Integer technicienId) { this.technicienId = technicienId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getCompetences() { return competences; }
    public void setCompetences(List<String> competences) { this.competences = competences; }

    public List<CreneauDisponibilite> getDisponibilites() { return disponibilites; }
    public void setDisponibilites(List<CreneauDisponibilite> disponibilites) { 
        this.disponibilites = disponibilites;
        this.tauxDisponibilite = calculerTauxDisponibilite();
    }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public Double getTauxDisponibilite() { return tauxDisponibilite; }
    public void setTauxDisponibilite(Double tauxDisponibilite) { this.tauxDisponibilite = tauxDisponibilite; }

    // Méthode utilitaire
    private Double calculerTauxDisponibilite() {
        if (disponibilites == null || disponibilites.isEmpty()) return 0.0;
        
        long totalCreneaux = disponibilites.stream()
            .flatMap(d -> d.getCreneaux().stream())
            .count();
            
        long creneauxDisponibles = disponibilites.stream()
            .flatMap(d -> d.getCreneaux().stream())
            .filter(Creneau::isDisponible)
            .count();
            
        return totalCreneaux > 0 ? (double) creneauxDisponibles / totalCreneaux * 100 : 0.0;
    }
}