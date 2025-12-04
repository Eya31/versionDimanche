package tn.SGII_Ville.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tn.SGII_Ville.model.enums.RoleType;

public class RegisterRequest {

    // Champs généraux
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String prenom; // MAIN_DOEUVRE

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String motDePasse;

    private RoleType role;

    // Champs CITOYEN
    private String adresse;
    private String telephone;

    // Champ CHEF_SERVICE
    private String departement;

    // Champs MAIN_DOEUVRE
    private String matricule;
    private String cin;
    private String competence;

    public RegisterRequest() {
    }

    public RegisterRequest(String nom, String prenom, String email, String motDePasse, RoleType role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // Getters et Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public RoleType getRole() { return role; }
    public void setRole(RoleType role) { this.role = role; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getCompetence() { return competence; }
    public void setCompetence(String competence) { this.competence = competence; }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
