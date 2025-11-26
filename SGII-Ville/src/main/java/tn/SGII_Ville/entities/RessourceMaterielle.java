package tn.SGII_Ville.entities;


import java.math.BigDecimal;

/**
 * Entité Ressource Matérielle conforme au schéma XSD ressources.xsd
 * Relation: 1 Fournisseur -> * RessourcesMaterielles
 */
public class RessourceMaterielle {
    private int id;
    private String designation;
    private int quantiteEnStock;
    private BigDecimal valeurAchat;
    private Integer fournisseurId; // Référence au fournisseur (clé étrangère)
    private String unite; // Unité de mesure (optionnel)

    // Constructeurs
    public RessourceMaterielle() {}

    public RessourceMaterielle(int id, String designation, int quantiteEnStock, 
                              BigDecimal valeurAchat, Integer fournisseurId) {
        this.id = id;
        this.designation = designation;
        this.quantiteEnStock = quantiteEnStock;
        this.valeurAchat = valeurAchat;
        this.fournisseurId = fournisseurId;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public int getQuantiteEnStock() {
        return quantiteEnStock;
    }

    public void setQuantiteEnStock(int quantiteEnStock) {
        this.quantiteEnStock = quantiteEnStock;
    }

    public BigDecimal getValeurAchat() {
        return valeurAchat;
    }

    public void setValeurAchat(BigDecimal valeurAchat) {
        this.valeurAchat = valeurAchat;
    }

    public Integer getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(Integer fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    @Override
    public String toString() {
        return "RessourceMaterielle{" +
                "id=" + id +
                ", designation='" + designation + '\'' +
                ", quantiteEnStock=" + quantiteEnStock +
                ", valeurAchat=" + valeurAchat +
                ", fournisseurId=" + fournisseurId +
                ", unite='" + unite + '\'' +
                '}';
    }
}
