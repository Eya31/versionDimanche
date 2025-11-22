package tn.SGII_Ville.model;

import java.util.List;

public class PlanificationRequest {
    private Integer demandeId;
    private String datePlanifiee;
    private List<Integer> techniciensIds;
    private List<Integer> equipementsIds;
    private List<Integer> ressourcesIds;
    private String priorite;
    private Double budget;
    private String notes;

    // Constructeurs
    public PlanificationRequest() {}

    public PlanificationRequest(Integer demandeId, String datePlanifiee, List<Integer> techniciensIds, 
                               List<Integer> equipementsIds, List<Integer> ressourcesIds, 
                               String priorite, Double budget) {
        this.demandeId = demandeId;
        this.datePlanifiee = datePlanifiee;
        this.techniciensIds = techniciensIds;
        this.equipementsIds = equipementsIds;
        this.ressourcesIds = ressourcesIds;
        this.priorite = priorite;
        this.budget = budget;
    }

    // Getters et Setters
    public Integer getDemandeId() { return demandeId; }
    public void setDemandeId(Integer demandeId) { this.demandeId = demandeId; }

    public String getDatePlanifiee() { return datePlanifiee; }
    public void setDatePlanifiee(String datePlanifiee) { this.datePlanifiee = datePlanifiee; }

    public List<Integer> getTechniciensIds() { return techniciensIds; }
    public void setTechniciensIds(List<Integer> techniciensIds) { this.techniciensIds = techniciensIds; }

    public List<Integer> getEquipementsIds() { return equipementsIds; }
    public void setEquipementsIds(List<Integer> equipementsIds) { this.equipementsIds = equipementsIds; }

    public List<Integer> getRessourcesIds() { return ressourcesIds; }
    public void setRessourcesIds(List<Integer> ressourcesIds) { this.ressourcesIds = ressourcesIds; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}