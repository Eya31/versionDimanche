package tn.SGII_Ville.dto;

/**
 * DTO pour créer une nouvelle tâche
 */
public class CreateTacheRequest {
    private String libelle;
    private String description;
    private Integer mainDOeuvreId; // Optionnel : peut être assignée plus tard
    private Integer ordre;

    public CreateTacheRequest() {}

    // Getters et Setters
    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMainDOeuvreId() {
        return mainDOeuvreId;
    }

    public void setMainDOeuvreId(Integer mainDOeuvreId) {
        this.mainDOeuvreId = mainDOeuvreId;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }
}

