package tn.SGII_Ville.dto;

/**
 * DTO pour terminer une tâche (côté main-d'œuvre)
 */
public class TerminerTacheRequest {
    private String commentaire;
    private Integer tempsPasseMinutes;

    public TerminerTacheRequest() {}

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Integer getTempsPasseMinutes() {
        return tempsPasseMinutes;
    }

    public void setTempsPasseMinutes(Integer tempsPasseMinutes) {
        this.tempsPasseMinutes = tempsPasseMinutes;
    }
}

