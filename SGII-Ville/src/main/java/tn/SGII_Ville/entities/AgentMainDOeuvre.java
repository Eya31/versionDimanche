package tn.SGII_Ville.entities;

import tn.SGII_Ville.model.enums.RoleType;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un agent de main-d'œuvre avec compte utilisateur
 * Hérite de Utilisateur pour permettre la connexion
 */
public class AgentMainDOeuvre extends Utilisateur {
    private String prenom;
    private String matricule;
    private String cin;
    private String telephone;
    private String metier;
    private List<String> competences = new ArrayList<>();
    private int mainDOeuvreId; // ID de la fiche MainDOeuvre correspondante

    // Constructeurs
    public AgentMainDOeuvre() {
        super();
        this.setRole(RoleType.MAIN_DOEUVRE);
    }

    public AgentMainDOeuvre(int id, String nom, String email, String motDePasse, 
                           String prenom, String matricule, String cin, String telephone) {
        super(id, nom, email, motDePasse, RoleType.MAIN_DOEUVRE);
        this.prenom = prenom;
        this.matricule = matricule;
        this.cin = cin;
        this.telephone = telephone;
        this.competences = new ArrayList<>();
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
        this.competences = competences != null ? competences : new ArrayList<>();
    }

    public int getMainDOeuvreId() {
        return mainDOeuvreId;
    }

    public void setMainDOeuvreId(int mainDOeuvreId) {
        this.mainDOeuvreId = mainDOeuvreId;
    }
}

