package tn.SGII_Ville.entities;

import tn.SGII_Ville.model.enums.RoleType;

public class MainDOeuvre extends Utilisateur{
    private String prenom;
    private String matricule;
    private String cin;
    private String telephone;
    private String disponibilite; // "LIBRE", "OCCUPE", "ARCHIVE"
    private String competence; // Compétence unique (obligatoire)

    // Constructeurs
    public MainDOeuvre() {
        super();
        this.setRole(RoleType.MAIN_DOEUVRE);
        this.disponibilite = "LIBRE";
    }

    public MainDOeuvre(int id, String nom, String email, String motDePasse, 
                      String prenom, String matricule, String cin, String telephone, String competence) {
        super(id, nom, email, motDePasse, RoleType.MAIN_DOEUVRE);
        this.prenom = prenom;
        this.matricule = matricule;
        this.cin = cin;
        this.telephone = telephone;
        this.competence = competence;
        this.disponibilite = "LIBRE";
    }

    // Getters et Setters
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

    public String getDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(String disponibilite) {
        this.disponibilite = disponibilite;
    }

    public String getCompetence() {
        return competence;
    }

    public void setCompetence(String competence) {
        this.competence = competence;
    }

    @Override
    public String toString() {
        return "MainDOeuvre{" +
                "id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + prenom + '\'' +
                ", matricule='" + matricule + '\'' +
                ", cin='" + cin + '\'' +
                ", telephone='" + telephone + '\'' +
                ", competence='" + competence + '\'' +
                ", disponibilite='" + disponibilite + '\'' +
                '}';
    }
}