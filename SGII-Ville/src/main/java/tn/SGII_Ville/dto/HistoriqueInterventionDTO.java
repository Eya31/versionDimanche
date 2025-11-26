package tn.SGII_Ville.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour l'historique des interventions d'un agent
 */
public class HistoriqueInterventionDTO {
    private int interventionId;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int tempsPasseMinutes;
    private String etat; // TERMINEE, EN_COURS, etc.
    private List<String> competencesUtilisees;
    private String resultat; // Succès, Échec, Partiel

    public HistoriqueInterventionDTO() {}

    // Getters et Setters
    public int getInterventionId() {
        return interventionId;
    }

    public void setInterventionId(int interventionId) {
        this.interventionId = interventionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public int getTempsPasseMinutes() {
        return tempsPasseMinutes;
    }

    public void setTempsPasseMinutes(int tempsPasseMinutes) {
        this.tempsPasseMinutes = tempsPasseMinutes;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public List<String> getCompetencesUtilisees() {
        return competencesUtilisees;
    }

    public void setCompetencesUtilisees(List<String> competencesUtilisees) {
        this.competencesUtilisees = competencesUtilisees;
    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }
}

