package tn.SGII_Ville.model;

public class Fournisseur {
    private Integer id;
    private String nom;
    private String contact;
    private String email;

    // Constructeurs
    public Fournisseur() {}

    public Fournisseur(Integer id, String nom, String contact, String email) {
        this.id = id;
        this.nom = nom;
        this.contact = contact;
        this.email = email;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}