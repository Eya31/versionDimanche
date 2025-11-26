package tn.SGII_Ville.dto;

import tn.SGII_Ville.model.enums.EtatInterventionType;

public class UpdateEtatInterventionRequest {
    private EtatInterventionType nouvelEtat;
    private Integer tempsPasseMinutes;
    private String notes;
    private String commentaire;

    public UpdateEtatInterventionRequest() {}

    public EtatInterventionType getNouvelEtat() {
        return nouvelEtat;
    }

    public void setNouvelEtat(EtatInterventionType nouvelEtat) {
        this.nouvelEtat = nouvelEtat;
    }

    public Integer getTempsPasseMinutes() {
        return tempsPasseMinutes;
    }

    public void setTempsPasseMinutes(Integer tempsPasseMinutes) {
        this.tempsPasseMinutes = tempsPasseMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}

