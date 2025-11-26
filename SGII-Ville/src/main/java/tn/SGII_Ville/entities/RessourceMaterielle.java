package tn.SGII_Ville.entities;

public class RessourceMaterielle {
    private Integer id;
    private String designation;
    private Integer quantiteEnStock;
    private Double valeurAchat;
    private Integer fournisseurId;
    private String unite;

    // Constructeurs
    public RessourceMaterielle() {}

    public RessourceMaterielle(Integer id, String designation, Integer quantiteEnStock, 
                              Double valeurAchat, Integer fournisseurId, String unite) {
        this.id = id;
        this.designation = designation;
        this.quantiteEnStock = quantiteEnStock;
        this.valeurAchat = valeurAchat;
        this.fournisseurId = fournisseurId;
        this.unite = unite;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Integer getQuantiteEnStock() { return quantiteEnStock; }
    public void setQuantiteEnStock(Integer quantiteEnStock) { this.quantiteEnStock = quantiteEnStock; }

    public Double getValeurAchat() { return valeurAchat; }
    public void setValeurAchat(Double valeurAchat) { this.valeurAchat = valeurAchat; }

    public Integer getFournisseurId() { return fournisseurId; }
    public void setFournisseurId(Integer fournisseurId) { this.fournisseurId = fournisseurId; }

    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }
}