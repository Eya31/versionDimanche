package tn.SGII_Ville.dto;

import java.time.LocalDate;

/**
 * DTO pour représenter une habilitation avec sa date d'expiration
 */
public class HabilitationDTO {
    private String nom; // Ex: "ELECTRIQUE", "CACES", "TRAVAIL_HAUTEUR"
    private LocalDate dateObtention;
    private LocalDate dateExpiration;
    private String numeroCertificat; // Optionnel
    private boolean valide;

    public HabilitationDTO() {}

    public HabilitationDTO(String nom, LocalDate dateObtention, LocalDate dateExpiration) {
        this.nom = nom;
        this.dateObtention = dateObtention;
        this.dateExpiration = dateExpiration;
        this.valide = dateExpiration == null || dateExpiration.isAfter(LocalDate.now());
    }

    // Getters et Setters
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDate getDateObtention() {
        return dateObtention;
    }

    public void setDateObtention(LocalDate dateObtention) {
        this.dateObtention = dateObtention;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
        this.valide = dateExpiration == null || dateExpiration.isAfter(LocalDate.now());
    }

    public String getNumeroCertificat() {
        return numeroCertificat;
    }

    public void setNumeroCertificat(String numeroCertificat) {
        this.numeroCertificat = numeroCertificat;
    }

    public boolean isValide() {
        return valide;
    }

    public void setValide(boolean valide) {
        this.valide = valide;
    }
}

