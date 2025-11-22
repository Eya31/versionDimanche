package tn.SGII_Ville.model;

public class Photo {
    private Integer id;
    private String url;
    private String description;

    // Constructeurs
    public Photo() {}

    public Photo(Integer id, String url, String description) {
        this.id = id;
        this.url = url;
        this.description = description;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}