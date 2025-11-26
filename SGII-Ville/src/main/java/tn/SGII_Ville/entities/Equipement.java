package tn.SGII_Ville.entities;


import tn.SGII_Ville.common.PointGeo;
import tn.SGII_Ville.model.enums.EtatEquipementType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entité Equipement conforme au schéma XSD equipements.xsd
 * Relation: 1 Fournisseur -> * Equipements
 */
public class Equipement {
    private int id;
    private String type;
    private EtatEquipementType etat;
    private Integer fournisseurId; // Référence au fournisseur (clé étrangère)
    private BigDecimal valeurAchat;
    private PointGeo localisation;
    private LocalDate dateAchat; // Optionnel

    // Constructeurs
    public Equipement() {}

    public Equipement(int id, String type, EtatEquipementType etat, Integer fournisseurId, 
                     BigDecimal valeurAchat, PointGeo localisation) {
        this.id = id;
        this.type = type;
        this.etat = etat;
        this.fournisseurId = fournisseurId;
        this.valeurAchat = valeurAchat;
        this.localisation = localisation;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EtatEquipementType getEtat() {
        return etat;
    }

    public void setEtat(EtatEquipementType etat) {
        this.etat = etat;
    }

    public Integer getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(Integer fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    public BigDecimal getValeurAchat() {
        return valeurAchat;
    }

    public void setValeurAchat(BigDecimal valeurAchat) {
        this.valeurAchat = valeurAchat;
    }

    public PointGeo getLocalisation() {
        return localisation;
    }

    public void setLocalisation(PointGeo localisation) {
        this.localisation = localisation;
    }

    public LocalDate getDateAchat() {
        return dateAchat;
    }

    public void setDateAchat(LocalDate dateAchat) {
        this.dateAchat = dateAchat;
    }

    @Override
    public String toString() {
        return "Equipement{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", etat=" + etat +
                ", fournisseurId=" + fournisseurId +
                ", valeurAchat=" + valeurAchat +
                ", localisation=" + localisation +
                ", dateAchat=" + dateAchat +
                '}';
    }
}
