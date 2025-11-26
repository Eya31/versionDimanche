package tn.SGII_Ville.entities;

import java.util.ArrayList;
import java.util.List;

public class Equipement {
    private Integer id;
    private String nom;
    private String type;
    private String etat;
    private Integer fournisseurId;
    private Double valeurAchat;
    private Localisation localisation;
    private String dateAchat;
    private Boolean disponible;
    private List<PeriodeIndisponibilite> indisponibilites;

    // Constructeurs
    public Equipement() {
        this.disponible = true; // Par défaut disponible
        this.indisponibilites = new ArrayList<>();
    }

    public Equipement(Integer id, String nom, String type, String etat, Integer fournisseurId, 
                     Double valeurAchat, Localisation localisation, String dateAchat) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.etat = etat;
        this.fournisseurId = fournisseurId;
        this.valeurAchat = valeurAchat;
        this.localisation = localisation;
        this.dateAchat = dateAchat;
        this.disponible = true;
        this.indisponibilites = new ArrayList<>();
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public Integer getFournisseurId() { return fournisseurId; }
    public void setFournisseurId(Integer fournisseurId) { this.fournisseurId = fournisseurId; }

    public Double getValeurAchat() { return valeurAchat; }
    public void setValeurAchat(Double valeurAchat) { this.valeurAchat = valeurAchat; }

    public Localisation getLocalisation() { return localisation; }
    public void setLocalisation(Localisation localisation) { this.localisation = localisation; }

    public String getDateAchat() { return dateAchat; }
    public void setDateAchat(String dateAchat) { this.dateAchat = dateAchat; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public List<PeriodeIndisponibilite> getIndisponibilites() { return indisponibilites; }
    public void setIndisponibilites(List<PeriodeIndisponibilite> indisponibilites) { this.indisponibilites = indisponibilites; }
}