package tn.SGII_Ville.model;

public class EquipementDisponibilite {
    private Integer equipementId;
    private String type;
    private String etat;
    private Boolean disponible;
    private String raisonIndisponibilite;
    private String dateRetour;

    // Constructeurs
    public EquipementDisponibilite() {}
    
    public EquipementDisponibilite(Integer equipementId, String type, String etat, Boolean disponible) {
        this.equipementId = equipementId;
        this.type = type;
        this.etat = etat;
        this.disponible = disponible;
    }

    // Getters et Setters
    public Integer getEquipementId() { return equipementId; }
    public void setEquipementId(Integer equipementId) { this.equipementId = equipementId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public String getRaisonIndisponibilite() { return raisonIndisponibilite; }
    public void setRaisonIndisponibilite(String raisonIndisponibilite) { this.raisonIndisponibilite = raisonIndisponibilite; }

    public String getDateRetour() { return dateRetour; }
    public void setDateRetour(String dateRetour) { this.dateRetour = dateRetour; }
}