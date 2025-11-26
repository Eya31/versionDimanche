package tn.SGII_Ville.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainDOeuvre {
    private int id;
    private String nom;
    private String prenom;
    private String matricule;
    private String cin;
    private String telephone;
    private String email; // Nouveau
    private String metier;
    private List<String> competences = new ArrayList<>();
    private List<String> habilitations = new ArrayList<>(); // Ex: "ELECTRIQUE", "CACES", etc.
    private Map<String, LocalDate> habilitationsExpiration = new HashMap<>(); // Nouveau: habilitation -> date expiration
    private String disponibilite; // "DISPONIBLE", "OCCUPE", "CONFLIT", "EN_CONGE", "ABSENT", "HORS_HABILITATION"
    private boolean active = true;
    private String photoPath; // Nouveau: chemin vers la photo
    
    // Disponibilités (jours travaillés)
    private Map<String, String> horairesTravail = new HashMap<>(); // "LUNDI" -> "08:00-17:00"
    private List<LocalDate> conges = new ArrayList<>(); // Dates de congés
    private List<LocalDate> absences = new ArrayList<>(); // Dates d'absences (maladie, permission)
    
    // Historique (optionnel, peut être chargé séparément)
    private List<Integer> historiqueInterventionIds = new ArrayList<>();

    // Constructeurs
    public MainDOeuvre() {
        this.competences = new ArrayList<>();
        this.habilitations = new ArrayList<>();
    }

    public MainDOeuvre(int id, String nom, String prenom, String matricule, String cin, String telephone) {
        this();
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.matricule = matricule;
        this.cin = cin;
        this.telephone = telephone;
        this.disponibilite = "DISPONIBLE";
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMetier() {
        return metier;
    }

    public void setMetier(String metier) {
        this.metier = metier;
    }

    public List<String> getCompetences() {
        return competences;
    }

    public void setCompetences(List<String> competences) {
        this.competences = competences != null ? competences : new ArrayList<>();
    }

    public List<String> getHabilitations() {
        return habilitations;
    }

    public void setHabilitations(List<String> habilitations) {
        this.habilitations = habilitations != null ? habilitations : new ArrayList<>();
    }

    public Map<String, LocalDate> getHabilitationsExpiration() {
        return habilitationsExpiration;
    }

    public void setHabilitationsExpiration(Map<String, LocalDate> habilitationsExpiration) {
        this.habilitationsExpiration = habilitationsExpiration != null ? habilitationsExpiration : new HashMap<>();
    }

    public String getDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(String disponibilite) {
        this.disponibilite = disponibilite;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public Map<String, String> getHorairesTravail() {
        return horairesTravail;
    }

    public void setHorairesTravail(Map<String, String> horairesTravail) {
        this.horairesTravail = horairesTravail != null ? horairesTravail : new HashMap<>();
    }

    public List<LocalDate> getConges() {
        return conges;
    }

    public void setConges(List<LocalDate> conges) {
        this.conges = conges != null ? conges : new ArrayList<>();
    }

    public List<LocalDate> getAbsences() {
        return absences;
    }

    public void setAbsences(List<LocalDate> absences) {
        this.absences = absences != null ? absences : new ArrayList<>();
    }

    public List<Integer> getHistoriqueInterventionIds() {
        return historiqueInterventionIds;
    }

    public void setHistoriqueInterventionIds(List<Integer> historiqueInterventionIds) {
        this.historiqueInterventionIds = historiqueInterventionIds != null ? historiqueInterventionIds : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "MainDOeuvre{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", matricule='" + matricule + '\'' +
                ", cin='" + cin + '\'' +
                ", telephone='" + telephone + '\'' +
                ", metier='" + metier + '\'' +
                ", competences=" + competences +
                ", habilitations=" + habilitations +
                ", disponibilite='" + disponibilite + '\'' +
                ", active=" + active +
                '}';
    }
}