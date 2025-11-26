package tn.SGII_Ville.model;

public class RessourceMaterielleDisponibilite {
    private Integer ressourceId;
    private String designation;
    private Integer quantiteDisponible;
    private Integer quantiteDemandee;
    private Boolean disponible;
    private Boolean seuilAlerte;

    // Constructeurs
    public RessourceMaterielleDisponibilite() {}
    
    public RessourceMaterielleDisponibilite(Integer ressourceId, String designation, 
                                           Integer quantiteDisponible, Integer quantiteDemandee, 
                                           Boolean disponible) {
        this.ressourceId = ressourceId;
        this.designation = designation;
        this.quantiteDisponible = quantiteDisponible;
        this.quantiteDemandee = quantiteDemandee;
        this.disponible = disponible;
        this.seuilAlerte = quantiteDisponible < 5; // Seuil d'alerte à 5 unités
    }

    public Integer getRessourceId() { return ressourceId; }
    public void setRessourceId(Integer ressourceId) { this.ressourceId = ressourceId; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Integer getQuantiteDisponible() { return quantiteDisponible; }
    public void setQuantiteDisponible(Integer quantiteDisponible) { 
        this.quantiteDisponible = quantiteDisponible;
        this.seuilAlerte = quantiteDisponible < 5;
    }

    public Integer getQuantiteDemandee() { return quantiteDemandee; }
    public void setQuantiteDemandee(Integer quantiteDemandee) { this.quantiteDemandee = quantiteDemandee; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public Boolean getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(Boolean seuilAlerte) { this.seuilAlerte = seuilAlerte; }
}