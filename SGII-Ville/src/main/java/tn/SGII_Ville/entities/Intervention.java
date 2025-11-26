package tn.SGII_Ville.entities;

import tn.SGII_Ville.model.enums.EtatInterventionType;
import tn.SGII_Ville.model.enums.PrioriteType;
import tn.SGII_Ville.common.PointGeo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Intervention {
    private int id;
    private String description;
    private String typeIntervention; // "ECLAIRAGE", "EAU", "ROUTE", etc.
    private PrioriteType priorite;
    private EtatInterventionType etat;
    private LocalDate datePlanifiee;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private BigDecimal budget;
    private int technicienId;
    private Integer demandeId;
    private Integer chefServiceId;
    private PointGeo localisation;
    private List<Integer> mainDOeuvreIds = new ArrayList<>(); // IDs de la main-d'œuvre affectée
    private List<Integer> equipementIds = new ArrayList<>(); // IDs des équipements utilisés
    private List<Integer> ressourceIds = new ArrayList<>(); // IDs des ressources utilisées
    private List<Integer> photoIds = new ArrayList<>(); // IDs des photos
    private String commentaire;
    private String rapportFinal;
    private Integer tempsPasseMinutes; // Temps passé en minutes
    private String signatureElectronique; // Pour le rapport final

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTypeIntervention() { return typeIntervention; }
    public void setTypeIntervention(String typeIntervention) { this.typeIntervention = typeIntervention; }

    public PrioriteType getPriorite() { return priorite; }
    public void setPriorite(PrioriteType priorite) { this.priorite = priorite; }

    public EtatInterventionType getEtat() { return etat; }
    public void setEtat(EtatInterventionType etat) { this.etat = etat; }

    public LocalDate getDatePlanifiee() { return datePlanifiee; }
    public void setDatePlanifiee(LocalDate datePlanifiee) { this.datePlanifiee = datePlanifiee; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public int getTechnicienId() { return technicienId; }
    public void setTechnicienId(int technicienId) { this.technicienId = technicienId; }

    public Integer getDemandeId() { return demandeId; }
    public void setDemandeId(Integer demandeId) { this.demandeId = demandeId; }

    public Integer getChefServiceId() { return chefServiceId; }
    public void setChefServiceId(Integer chefServiceId) { this.chefServiceId = chefServiceId; }

    public PointGeo getLocalisation() { return localisation; }
    public void setLocalisation(PointGeo localisation) { this.localisation = localisation; }

    public List<Integer> getMainDOeuvreIds() { return mainDOeuvreIds; }
    public void setMainDOeuvreIds(List<Integer> mainDOeuvreIds) { 
        this.mainDOeuvreIds = mainDOeuvreIds != null ? mainDOeuvreIds : new ArrayList<>(); 
    }

    public List<Integer> getEquipementIds() { return equipementIds; }
    public void setEquipementIds(List<Integer> equipementIds) { 
        this.equipementIds = equipementIds != null ? equipementIds : new ArrayList<>(); 
    }

    public List<Integer> getRessourceIds() { return ressourceIds; }
    public void setRessourceIds(List<Integer> ressourceIds) { 
        this.ressourceIds = ressourceIds != null ? ressourceIds : new ArrayList<>(); 
    }

    public List<Integer> getPhotoIds() { return photoIds; }
    public void setPhotoIds(List<Integer> photoIds) { 
        this.photoIds = photoIds != null ? photoIds : new ArrayList<>(); 
    }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public String getRapportFinal() { return rapportFinal; }
    public void setRapportFinal(String rapportFinal) { this.rapportFinal = rapportFinal; }

    public Integer getTempsPasseMinutes() { return tempsPasseMinutes; }
    public void setTempsPasseMinutes(Integer tempsPasseMinutes) { this.tempsPasseMinutes = tempsPasseMinutes; }

    public String getSignatureElectronique() { return signatureElectronique; }
    public void setSignatureElectronique(String signatureElectronique) { this.signatureElectronique = signatureElectronique; }

    @Override
    public String toString() {
        return "Intervention{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", typeIntervention='" + typeIntervention + '\'' +
                ", priorite=" + priorite +
                ", etat=" + etat +
                ", datePlanifiee=" + datePlanifiee +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", budget=" + budget +
                ", technicienId=" + technicienId +
                ", demandeId=" + demandeId +
                ", chefServiceId=" + chefServiceId +
                ", localisation=" + localisation +
                ", mainDOeuvreIds=" + mainDOeuvreIds +
                ", equipementIds=" + equipementIds +
                ", ressourceIds=" + ressourceIds +
                ", photoIds=" + photoIds +
                ", tempsPasseMinutes=" + tempsPasseMinutes +
                '}';
    }
}