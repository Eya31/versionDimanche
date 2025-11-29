package tn.SGII_Ville.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import tn.SGII_Ville.model.enums.EtatInterventionType;
import tn.SGII_Ville.model.enums.PrioriteType;
import tn.SGII_Ville.common.PointGeo;
import tn.SGII_Ville.dto.CompetenceRequise;
import tn.SGII_Ville.dto.MaterielRequis;
import tn.SGII_Ville.dto.EquipementRequis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Intervention conforme au schéma XSD interventions.xsd
 * Relations:
 * - 1 Demande -> 0..1 Intervention (demandeId obligatoire)
 * - 1 Technicien -> * Interventions (technicienId optionnel)
 * - 1 ChefService -> * Interventions (chefServiceId optionnel)
 * - * Intervention <-> * Equipements (many-to-many)
 * - * Intervention <-> * RessourcesMaterielles (many-to-many)
 * - * Intervention <-> * MainDOeuvre (many-to-many)
 */
public class Intervention {
    private int id;
    private int demandeId; // Clé étrangère obligatoire vers Demande
    private Integer technicienId; // Clé étrangère optionnelle vers Technicien
    private Integer chefServiceId; // Clé étrangère optionnelle vers ChefService
    private String description;
    private String typeIntervention; // "ECLAIRAGE", "EAU", "ROUTE", etc.
    private PrioriteType priorite;
    private EtatInterventionType etat;
    private LocalDate datePlanifiee;
    private LocalDate dateDebut; // Changé de LocalDateTime à LocalDate pour conformité XSD
    private LocalDate dateFin; // Changé de LocalDateTime à LocalDate pour conformité XSD
    private BigDecimal budget;
    private PointGeo localisation; // Optionnel
    
    // Exigences définies lors de la planification
    private List<CompetenceRequise> competencesRequises = new ArrayList<>();
    private List<MaterielRequis> materielsRequis = new ArrayList<>();
    private List<EquipementRequis> equipementsRequis = new ArrayList<>();
    
    // Relations many-to-many via collections d'IDs
    private List<Integer> equipementIds = new ArrayList<>(); // * Intervention <-> * Equipements
    private List<Integer> ressourceIds = new ArrayList<>(); // * Intervention <-> * RessourcesMaterielles
    private List<Integer> ouvrierIds = new ArrayList<>(); // * Intervention <-> * MainDOeuvre
    
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

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public Integer getTechnicienId() { return technicienId; }
    public void setTechnicienId(Integer technicienId) { this.technicienId = technicienId; }

    public int getDemandeId() { return demandeId; }
    public void setDemandeId(int demandeId) { this.demandeId = demandeId; }

    public Integer getChefServiceId() { return chefServiceId; }
    public void setChefServiceId(Integer chefServiceId) { this.chefServiceId = chefServiceId; }

    public PointGeo getLocalisation() { return localisation; }
    public void setLocalisation(PointGeo localisation) { this.localisation = localisation; }

    @JsonProperty("mainDOeuvreIds")
    public List<Integer> getOuvrierIds() { return ouvrierIds; }
    
    @JsonProperty("mainDOeuvreIds")
    public void setOuvrierIds(List<Integer> ouvrierIds) { 
        this.ouvrierIds = ouvrierIds != null ? ouvrierIds : new ArrayList<>(); 
    }

    public List<Integer> getEquipementIds() { return equipementIds; }
    public void setEquipementIds(List<Integer> equipementIds) { 
        this.equipementIds = equipementIds != null ? equipementIds : new ArrayList<>(); 
    }

    public List<Integer> getRessourceIds() { return ressourceIds; }
    public void setRessourceIds(List<Integer> ressourceIds) { 
        this.ressourceIds = ressourceIds != null ? ressourceIds : new ArrayList<>(); 
    }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public String getRapportFinal() { return rapportFinal; }
    public void setRapportFinal(String rapportFinal) { this.rapportFinal = rapportFinal; }

    public Integer getTempsPasseMinutes() { return tempsPasseMinutes; }
    public void setTempsPasseMinutes(Integer tempsPasseMinutes) { this.tempsPasseMinutes = tempsPasseMinutes; }

    public String getSignatureElectronique() { return signatureElectronique; }
    public void setSignatureElectronique(String signatureElectronique) { this.signatureElectronique = signatureElectronique; }

    public List<CompetenceRequise> getCompetencesRequises() { return competencesRequises; }
    public void setCompetencesRequises(List<CompetenceRequise> competencesRequises) { 
        this.competencesRequises = competencesRequises != null ? competencesRequises : new ArrayList<>(); 
    }

    public List<MaterielRequis> getMaterielsRequis() { return materielsRequis; }
    public void setMaterielsRequis(List<MaterielRequis> materielsRequis) { 
        this.materielsRequis = materielsRequis != null ? materielsRequis : new ArrayList<>(); 
    }

    public List<EquipementRequis> getEquipementsRequis() { return equipementsRequis; }
    public void setEquipementsRequis(List<EquipementRequis> equipementsRequis) { 
        this.equipementsRequis = equipementsRequis != null ? equipementsRequis : new ArrayList<>(); 
    }

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
                ", ouvrierIds=" + ouvrierIds +
                ", equipementIds=" + equipementIds +
                ", ressourceIds=" + ressourceIds +
                ", tempsPasseMinutes=" + tempsPasseMinutes +
                '}';
    }
}