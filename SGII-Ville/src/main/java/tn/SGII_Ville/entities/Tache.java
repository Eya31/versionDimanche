package tn.SGII_Ville.entities;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entité représentant une tâche d'une intervention
 * Chaque intervention peut être divisée en plusieurs tâches
 * Chaque tâche est assignée à une main-d'œuvre spécifique
 */
public class Tache {
    private int id;
    private int interventionId; // ID de l'intervention parente
    private String libelle; // Nom/description de la tâche
    private String description; // Description détaillée
    private Integer mainDOeuvreId; // ID de la main-d'œuvre assignée (null si non assignée)
    private String etat; // "A_FAIRE", "EN_COURS", "TERMINEE", "VERIFIEE"
    private LocalDateTime dateCreation;
    private LocalDateTime dateDebut; // Quand la main-d'œuvre a commencé
    private LocalDateTime dateFin; // Quand la main-d'œuvre a terminé
    private LocalDateTime dateVerification; // Quand le technicien a vérifié
    private Integer tempsPasseMinutes; // Temps passé en minutes
    private String commentaireMainDOeuvre; // Commentaire de la main-d'œuvre
    private String commentaireTechnicien; // Commentaire du technicien lors de la vérification
    private Integer ordre; // Ordre d'exécution (1, 2, 3...)
    private boolean verifiee = false; // Si le technicien a vérifié que la tâche est bien terminée
    private Map<String, Object> interventionInfo;
    public Map<String, Object> getInterventionInfo() {
        return interventionInfo;
    }
    
    public void setInterventionInfo(Map<String, Object> interventionInfo) {
        this.interventionInfo = interventionInfo;
    }
    // Constructeurs
    public Tache() {
        this.etat = "A_FAIRE";
        this.dateCreation = LocalDateTime.now();
    }

    public Tache(int id, int interventionId, String libelle, String description) {
        this();
        this.id = id;
        this.interventionId = interventionId;
        this.libelle = libelle;
        this.description = description;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInterventionId() {
        return interventionId;
    }

    public void setInterventionId(int interventionId) {
        this.interventionId = interventionId;
    }

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

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public LocalDateTime getDateVerification() {
        return dateVerification;
    }

    public void setDateVerification(LocalDateTime dateVerification) {
        this.dateVerification = dateVerification;
    }

    public Integer getTempsPasseMinutes() {
        return tempsPasseMinutes;
    }

    public void setTempsPasseMinutes(Integer tempsPasseMinutes) {
        this.tempsPasseMinutes = tempsPasseMinutes;
    }

    public String getCommentaireMainDOeuvre() {
        return commentaireMainDOeuvre;
    }

    public void setCommentaireMainDOeuvre(String commentaireMainDOeuvre) {
        this.commentaireMainDOeuvre = commentaireMainDOeuvre;
    }

    public String getCommentaireTechnicien() {
        return commentaireTechnicien;
    }

    public void setCommentaireTechnicien(String commentaireTechnicien) {
        this.commentaireTechnicien = commentaireTechnicien;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public boolean isVerifiee() {
        return verifiee;
    }

    public void setVerifiee(boolean verifiee) {
        this.verifiee = verifiee;
    }
}

