package tn.SGII_Ville.entities;

/**
 * Entité représentant une photo attachée à une demande
 * Conforme au schéma XSD photos.xsd
 */
public class Photo {
    private int idPhoto;
    private String url;
    private String nom;
    private Integer demandeId; // Référence à la demande (optionnel)

    // Constructeurs
    public Photo() {}

    public Photo(int idPhoto, String url, String nom) {
        this.idPhoto = idPhoto;
        this.url = url;
        this.nom = nom;
    }

    public Photo(int idPhoto, String url, String nom, Integer demandeId) {
        this.idPhoto = idPhoto;
        this.url = url;
        this.nom = nom;
        this.demandeId = demandeId;
    }

    // Getters et Setters
    public int getIdPhoto() {
        return idPhoto;
    }

    public void setIdPhoto(int idPhoto) {
        this.idPhoto = idPhoto;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Integer getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Integer demandeId) {
        this.demandeId = demandeId;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "idPhoto=" + idPhoto +
                ", url='" + url + '\'' +
                ", nom='" + nom + '\'' +
                ", demandeId=" + demandeId +
                '}';
    }
}
