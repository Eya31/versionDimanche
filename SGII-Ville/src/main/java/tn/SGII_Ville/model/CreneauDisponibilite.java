package tn.SGII_Ville.model;

import java.util.List;

public class CreneauDisponibilite {
    private String date;
    private List<Creneau> creneaux;

    // Constructeurs
    public CreneauDisponibilite() {}
    
    public CreneauDisponibilite(String date, List<Creneau> creneaux) {
        this.date = date;
        this.creneaux = creneaux;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<Creneau> getCreneaux() { return creneaux; }
    public void setCreneaux(List<Creneau> creneaux) { this.creneaux = creneaux; }
}