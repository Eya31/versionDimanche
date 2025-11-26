package tn.SGII_Ville.dto;

import java.util.List;

/**
 * DTO pour les résultats de vérification avant affectation
 */
public class VerificationAffectationDTO {
    private boolean disponible;
    private boolean competencesOk;
    private boolean habilitationsOk;
    private boolean pasDeConflit;
    private boolean pasDejaAffecte;
    private boolean horairesOk;
    private List<String> erreurs; // Liste des erreurs si vérification échoue
    private List<String> avertissements; // Avertissements (ex: habilitation expire bientôt)

    public VerificationAffectationDTO() {
        this.erreurs = new java.util.ArrayList<>();
        this.avertissements = new java.util.ArrayList<>();
    }

    public boolean isValide() {
        return disponible && competencesOk && habilitationsOk && pasDeConflit && pasDejaAffecte && horairesOk;
    }

    // Getters et Setters
    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public boolean isCompetencesOk() {
        return competencesOk;
    }

    public void setCompetencesOk(boolean competencesOk) {
        this.competencesOk = competencesOk;
    }

    public boolean isHabilitationsOk() {
        return habilitationsOk;
    }

    public void setHabilitationsOk(boolean habilitationsOk) {
        this.habilitationsOk = habilitationsOk;
    }

    public boolean isPasDeConflit() {
        return pasDeConflit;
    }

    public void setPasDeConflit(boolean pasDeConflit) {
        this.pasDeConflit = pasDeConflit;
    }

    public boolean isPasDejaAffecte() {
        return pasDejaAffecte;
    }

    public void setPasDejaAffecte(boolean pasDejaAffecte) {
        this.pasDejaAffecte = pasDejaAffecte;
    }

    public boolean isHorairesOk() {
        return horairesOk;
    }

    public void setHorairesOk(boolean horairesOk) {
        this.horairesOk = horairesOk;
    }

    public List<String> getErreurs() {
        return erreurs;
    }

    public void setErreurs(List<String> erreurs) {
        this.erreurs = erreurs;
    }

    public List<String> getAvertissements() {
        return avertissements;
    }

    public void setAvertissements(List<String> avertissements) {
        this.avertissements = avertissements;
    }
}

