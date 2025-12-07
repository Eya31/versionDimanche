package tn.SGII_Ville.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import tn.SGII_Ville.common.PointGeo;
import tn.SGII_Ville.model.enums.EtatDemandeType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Demande {

    private int id;
    private String description;
    private LocalDate dateSoumission;
    private EtatDemandeType etat;
    private PointGeo localisation;
    private List<Photo> photos = new ArrayList<>();

    // ✅ IMPORTANT: Utiliser Integer pour permettre null (anonymat)
    private Integer citoyenId;

    // Nouveaux champs selon XML
    private String category;
    private String subCategory;
    private String priority;
    private String contactEmail;
    private String address; // Adresse lisible

    @JsonProperty("isAnonymous")
    private boolean isAnonymous;

    // Références aux photos via ID (1 Demande -> 1..* Photos selon XSD)
    private List<Integer> photoIds = new ArrayList<>();

    // Constructeurs
    public Demande() {}

    public Demande(int id, String description, LocalDate dateSoumission, EtatDemandeType etat, PointGeo localisation) {
        this.id = id;
        this.description = description;
        this.dateSoumission = dateSoumission;
        this.etat = etat;
        this.localisation = localisation;
    }

    // ✅ Getters & Setters avec gestion de l'anonymat
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(LocalDate dateSoumission) { this.dateSoumission = dateSoumission; }

    public EtatDemandeType getEtat() { return etat; }
    public void setEtat(EtatDemandeType etat) { this.etat = etat; }

    public PointGeo getLocalisation() { return localisation; }
    public void setLocalisation(PointGeo localisation) { this.localisation = localisation; }

    public List<Photo> getPhotos() { return photos; }
    public void setPhotos(List<Photo> photos) { this.photos = photos != null ? photos : new ArrayList<>(); }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { this.isAnonymous = anonymous; }

    // ✅ Getter qui retourne null si la demande est anonyme
    public Integer getCitoyenId() {
    return citoyenId;  // Remove the null check for anonymous
}

// Add a separate method for display purposes
public boolean isAssociatedWithCitoyen(Integer citoyenId) {
    if (isAnonymous) return false;
    return citoyenId != null && citoyenId.equals(this.citoyenId);
}
    
    // ✅ Setter pour le stockage interne (toujours sauvegarder l'ID)
    public void setCitoyenId(Integer citoyenId) {
        this.citoyenId = citoyenId;
    }
    
    // ✅ Méthode pour obtenir l'ID interne (pour le XML/BDD)
    public Integer getInternalCitoyenId() {
        return citoyenId;
    }

    public List<Integer> getPhotoIds() { 
        if (photoIds == null) photoIds = new ArrayList<>();
        return photoIds; 
    }
    public void setPhotoIds(List<Integer> photoIds) { this.photoIds = photoIds; }

    // ✅ Méthode utilitaire pour obtenir le nom affiché
    public String getCitoyenDisplayName() {
        if (isAnonymous) {
            return "Citoyen anonyme";
        }
        return "Citoyen #" + (citoyenId != null ? citoyenId : "Inconnu");
    }
    
    // ✅ Méthode utilitaire pour savoir si le citoyen peut voir cette demande
    public boolean canCitoyenView(Integer citoyenId) {
        if (citoyenId == null) return false;
        if (isAnonymous) return false; // Les demandes anonymes ne sont pas visibles dans le dashboard
        return citoyenId.equals(this.citoyenId);
    }

    // Méthode utilitaire pour obtenir les URLs des photos à partir des références
    public List<String> getPhotoUrls() {
        List<String> urls = new ArrayList<>();
        if (photos != null) {
            for (Photo photo : photos) {
                urls.add(photo.getUrl());
            }
        }
        return urls;
    }

    @Override
    public String toString() {
        return "Demande{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", dateSoumission=" + dateSoumission +
                ", etat=" + etat +
                ", category='" + category + '\'' +
                ", priority='" + priority + '\'' +
                ", address='" + address + '\'' +
                ", isAnonymous=" + isAnonymous +
                ", citoyenId=" + citoyenId +
                ", photosCount=" + (photos != null ? photos.size() : 0) +
                ", photoIdsCount=" + (photoIds != null ? photoIds.size() : 0) +
                '}';
    }
}