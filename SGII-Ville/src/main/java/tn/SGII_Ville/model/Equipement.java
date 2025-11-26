package tn.SGII_Ville.model;

public class Equipement {
    private Integer id;
    private String type;
    private String modele;
    private String numeroSerie;
    private String etat;
    private String dateAcquisition;

    // Constructeurs
    public Equipement() {}

    public Equipement(Integer id, String type, String modele, String numeroSerie, String etat, String dateAcquisition) {
        this.id = id;
        this.type = type;
        this.modele = modele;
        this.numeroSerie = numeroSerie;
        this.etat = etat;
        this.dateAcquisition = dateAcquisition;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getDateAcquisition() { return dateAcquisition; }
    public void setDateAcquisition(String dateAcquisition) { this.dateAcquisition = dateAcquisition; }
}