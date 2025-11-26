package tn.SGII_Ville.model;

import java.util.List;

public class Intervention {
    private Integer id;
    private String priorite;
    private String etat;
    private String datePlanifiee;
    private Double budget;
    private Integer demandeId;
    private List<Integer> techniciens;
    private List<Integer> equipements;
    private List<Integer> ressources;

    // Constructeurs
    public Intervention() {}

    public Intervention(Integer id, String priorite, String etat, String datePlanifiee, Double budget, Integer demandeId) {
        this.id = id;
        this.priorite = priorite;
        this.etat = etat;
        this.datePlanifiee = datePlanifiee;
        this.budget = budget;
        this.demandeId = demandeId;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getDatePlanifiee() { return datePlanifiee; }
    public void setDatePlanifiee(String datePlanifiee) { this.datePlanifiee = datePlanifiee; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    public Integer getDemandeId() { return demandeId; }
    public void setDemandeId(Integer demandeId) { this.demandeId = demandeId; }

    public List<Integer> getTechniciens() { return techniciens; }
    public void setTechniciens(List<Integer> techniciens) { this.techniciens = techniciens; }

    public List<Integer> getEquipements() { return equipements; }
    public void setEquipements(List<Integer> equipements) { this.equipements = equipements; }

    public List<Integer> getRessources() { return ressources; }
    public void setRessources(List<Integer> ressources) { this.ressources = ressources; }
}