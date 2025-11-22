package tn.SGII_Ville.model;

import java.util.List;

public class TechnicienDisponibilite {
    private Technicien technicien;
    private List<Disponibilite> disponibilites;

    // Constructeurs
    public TechnicienDisponibilite() {}

    public TechnicienDisponibilite(Technicien technicien, List<Disponibilite> disponibilites) {
        this.technicien = technicien;
        this.disponibilites = disponibilites;
    }

    // Getters et Setters
    public Technicien getTechnicien() { return technicien; }
    public void setTechnicien(Technicien technicien) { this.technicien = technicien; }

    public List<Disponibilite> getDisponibilites() { return disponibilites; }
    public void setDisponibilites(List<Disponibilite> disponibilites) { this.disponibilites = disponibilites; }

    // Classe interne pour les disponibilités
    public static class Disponibilite {
        private String date;
        private List<Creneau> creneaux;

        public Disponibilite() {}
        public Disponibilite(String date, List<Creneau> creneaux) {
            this.date = date;
            this.creneaux = creneaux;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public List<Creneau> getCreneaux() { return creneaux; }
        public void setCreneaux(List<Creneau> creneaux) { this.creneaux = creneaux; }
    }

    // Classe interne pour les créneaux
    public static class Creneau {
        private String debut;
        private String fin;
        private Boolean disponible;
        private Integer interventionId;

        public Creneau() {}
        public Creneau(String debut, String fin, Boolean disponible, Integer interventionId) {
            this.debut = debut;
            this.fin = fin;
            this.disponible = disponible;
            this.interventionId = interventionId;
        }

        public String getDebut() { return debut; }
        public void setDebut(String debut) { this.debut = debut; }

        public String getFin() { return fin; }
        public void setFin(String fin) { this.fin = fin; }

        public Boolean getDisponible() { return disponible; }
        public void setDisponible(Boolean disponible) { this.disponible = disponible; }

        public Integer getInterventionId() { return interventionId; }
        public void setInterventionId(Integer interventionId) { this.interventionId = interventionId; }
    }
}