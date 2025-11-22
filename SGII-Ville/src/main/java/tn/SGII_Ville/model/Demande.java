package tn.SGII_Ville.model;

public class Demande {
    private Integer id;
    private String type;
    private String description;
    private String etat;
    private String dateCreation;
    private String localisation;

    // Constructeurs
    public Demande() {}

    public Demande(Integer id, String type, String description, String etat, String dateCreation, String localisation) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.etat = etat;
        this.dateCreation = dateCreation;
        this.localisation = localisation;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }
}