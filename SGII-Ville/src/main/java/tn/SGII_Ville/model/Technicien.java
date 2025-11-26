package tn.SGII_Ville.model;

import java.util.List;

public class Technicien {
    private Integer id;
    private String nom;
    private String email;
    private String motDePasse;
    private String role;
    private List<String> competences;
    private Boolean disponibilite;
    private List<Integer> materiels;
    private List<Integer> equipements;

    // Constructeurs
    public Technicien() {}

    public Technicien(Integer id, String nom, String email, String motDePasse, String role, 
                     List<String> competences, Boolean disponibilite) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.competences = competences;
        this.disponibilite = disponibilite;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getCompetences() { return competences; }
    public void setCompetences(List<String> competences) { this.competences = competences; }

    public Boolean getDisponibilite() { return disponibilite; }
    public void setDisponibilite(Boolean disponibilite) { this.disponibilite = disponibilite; }

    public List<Integer> getMateriels() { return materiels; }
    public void setMateriels(List<Integer> materiels) { this.materiels = materiels; }

    public List<Integer> getEquipements() { return equipements; }
    public void setEquipements(List<Integer> equipements) { this.equipements = equipements; }
}