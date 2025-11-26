package tn.SGII_Ville.dto;

import java.util.List;

public class UpdateProfilTechnicienRequest {
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private List<String> competences;
    private List<String> habilitations;
    private String ancienMotDePasse;
    private String nouveauMotDePasse;

    public UpdateProfilTechnicienRequest() {}

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

    public List<String> getCompetences() {
        return competences;
    }

    public void setCompetences(List<String> competences) {
        this.competences = competences;
    }

    public List<String> getHabilitations() {
        return habilitations;
    }

    public void setHabilitations(List<String> habilitations) {
        this.habilitations = habilitations;
    }

    public String getAncienMotDePasse() {
        return ancienMotDePasse;
    }

    public void setAncienMotDePasse(String ancienMotDePasse) {
        this.ancienMotDePasse = ancienMotDePasse;
    }

    public String getNouveauMotDePasse() {
        return nouveauMotDePasse;
    }

    public void setNouveauMotDePasse(String nouveauMotDePasse) {
        this.nouveauMotDePasse = nouveauMotDePasse;
    }
}

