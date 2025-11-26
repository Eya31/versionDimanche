package tn.SGII_Ville.model;

public class Creneau {
    private String debut;
    private String fin;
    private Boolean disponible;
    private Integer interventionId;
    private String typeIndisponibilite;

    // Constructeurs
    public Creneau() {}
    
    public Creneau(String debut, String fin, Boolean disponible) {
        this.debut = debut;
        this.fin = fin;
        this.disponible = disponible;
    }

    public String getDebut() { return debut; }
    public void setDebut(String debut) { this.debut = debut; }

    public String getFin() { return fin; }
    public void setFin(String fin) { this.fin = fin; }

    public Boolean isDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public Integer getInterventionId() { return interventionId; }
    public void setInterventionId(Integer interventionId) { this.interventionId = interventionId; }

    public String getTypeIndisponibilite() { return typeIndisponibilite; }
    public void setTypeIndisponibilite(String typeIndisponibilite) { this.typeIndisponibilite = typeIndisponibilite; }
}