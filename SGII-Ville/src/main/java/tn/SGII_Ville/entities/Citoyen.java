package tn.SGII_Ville.entities;

import tn.SGII_Ville.model.enums.RoleType;

public class Citoyen extends Utilisateur {
    private String adresse;
    private String telephone;
    private String cin;
    private String prenom; // Ajouter ce champ

    // Constructeurs
    public Citoyen() {
        super();
    }

    public Citoyen(int id, String nom, String prenom, String email, String motDePasse, String adresse, String telephone, String cin) {
        super(id, nom, email, motDePasse, RoleType.CITOYEN);
        this.prenom = prenom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.cin = cin;
    }

    // Getters et Setters
    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    @Override
    public String toString() {
        return "Citoyen{" +
                "id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + getEmail() + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", cin='" + cin + '\'' +
                '}';
    }
}