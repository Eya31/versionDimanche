package tn.SGII_Ville.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO pour créer une nouvelle fiche de main-d'œuvre
 */
public class CreateMainDOeuvreRequest {
    private String nom;
    private String prenom;
    private String matricule;
    private String cin;
    private String telephone;
    private String email;
    private String metier;
    private List<String> competences;
    private List<HabilitationDTO> habilitations; // Avec dates d'expiration
    private Map<String, String> horairesTravail; // "LUNDI" -> "08:00-17:00"
    private String photoPath;

    public CreateMainDOeuvreRequest() {}

    // Getters et Setters
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMetier() {
        return metier;
    }

    public void setMetier(String metier) {
        this.metier = metier;
    }

    public List<String> getCompetences() {
        return competences;
    }

    public void setCompetences(List<String> competences) {
        this.competences = competences;
    }

    public List<HabilitationDTO> getHabilitations() {
        return habilitations;
    }

    public void setHabilitations(List<HabilitationDTO> habilitations) {
        this.habilitations = habilitations;
    }

    public Map<String, String> getHorairesTravail() {
        return horairesTravail;
    }

    public void setHorairesTravail(Map<String, String> horairesTravail) {
        this.horairesTravail = horairesTravail;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}

