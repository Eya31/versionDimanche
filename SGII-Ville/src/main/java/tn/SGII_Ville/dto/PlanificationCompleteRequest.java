package tn.SGII_Ville.dto;

import tn.SGII_Ville.model.enums.PrioriteType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PlanificationCompleteRequest {
    private Integer demandeId;
    private Integer technicienId; // Technicien principal assigné
    private LocalDate datePlanifiee;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Integer dureeMinutes;
    private PrioriteType priorite;
    private BigDecimal budget;
    private String description;
    private String typeIntervention;
    private List<Integer> equipementIds = new ArrayList<>();
    private List<Integer> ressourceIds = new ArrayList<>();
    private List<Integer> ouvrierIds = new ArrayList<>();
    private String remarques;

    public PlanificationCompleteRequest() {}

    public Integer getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Integer demandeId) {
        this.demandeId = demandeId;
    }

    public Integer getTechnicienId() {
        return technicienId;
    }

    public void setTechnicienId(Integer technicienId) {
        this.technicienId = technicienId;
    }

    public LocalDate getDatePlanifiee() {
        return datePlanifiee;
    }

    public void setDatePlanifiee(LocalDate datePlanifiee) {
        this.datePlanifiee = datePlanifiee;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public Integer getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(Integer dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public PrioriteType getPriorite() {
        return priorite;
    }

    public void setPriorite(PrioriteType priorite) {
        this.priorite = priorite;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeIntervention() {
        return typeIntervention;
    }

    public void setTypeIntervention(String typeIntervention) {
        this.typeIntervention = typeIntervention;
    }

    public List<Integer> getEquipementIds() {
        return equipementIds;
    }

    public void setEquipementIds(List<Integer> equipementIds) {
        this.equipementIds = equipementIds != null ? equipementIds : new ArrayList<>();
    }

    public List<Integer> getRessourceIds() {
        return ressourceIds;
    }

    public void setRessourceIds(List<Integer> ressourceIds) {
        this.ressourceIds = ressourceIds != null ? ressourceIds : new ArrayList<>();
    }

    public List<Integer> getOuvrierIds() {
        return ouvrierIds;
    }

    public void setOuvrierIds(List<Integer> ouvrierIds) {
        this.ouvrierIds = ouvrierIds != null ? ouvrierIds : new ArrayList<>();
    }

    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }
}

