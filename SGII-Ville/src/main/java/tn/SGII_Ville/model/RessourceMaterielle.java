package tn.SGII_Ville.model;

public class RessourceMaterielle {
    private Integer id;
    private String designation;
    private Integer quantiteEnStock;
    private Double prixUnitaire;

    // Constructeurs
    public RessourceMaterielle() {}

    public RessourceMaterielle(Integer id, String designation, Integer quantiteEnStock, Double prixUnitaire) {
        this.id = id;
        this.designation = designation;
        this.quantiteEnStock = quantiteEnStock;
        this.prixUnitaire = prixUnitaire;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Integer getQuantiteEnStock() { return quantiteEnStock; }
    public void setQuantiteEnStock(Integer quantiteEnStock) { this.quantiteEnStock = quantiteEnStock; }

    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
}