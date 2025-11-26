package tn.SGII_Ville.entities;

public class PeriodeIndisponibilite {
    private String debut;
    private String fin;

    public PeriodeIndisponibilite() {}

    public PeriodeIndisponibilite(String debut, String fin) {
        this.debut = debut;
        this.fin = fin;
    }

    // Getters et Setters
    public String getDebut() { return debut; }
    public void setDebut(String debut) { this.debut = debut; }

    public String getFin() { return fin; }
    public void setFin(String fin) { this.fin = fin; }
}