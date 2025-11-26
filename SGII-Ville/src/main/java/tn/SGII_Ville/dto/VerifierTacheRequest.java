package tn.SGII_Ville.dto;

/**
 * DTO pour vérifier une tâche (côté technicien)
 */
public class VerifierTacheRequest {
    private String commentaire;
    private boolean validee; // true si la tâche est validée, false si elle doit être refaite

    public VerifierTacheRequest() {}

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public boolean isValidee() {
        return validee;
    }

    public void setValidee(boolean validee) {
        this.validee = validee;
    }
}

