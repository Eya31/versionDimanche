package tn.SGII_Ville.dto;

import java.util.List;

public class RapportFinalRequest {
    private String resultatObtenu;
    private Integer tempsTotalMinutes;
    private List<RessourceUtilisee> ressourcesUtilisees;
    private List<EquipementUtilise> equipementsUtilises;
    private String problemesRencontres;
    private List<Integer> photoIds;
    private String signatureElectronique;

    public RapportFinalRequest() {}

    public String getResultatObtenu() {
        return resultatObtenu;
    }

    public void setResultatObtenu(String resultatObtenu) {
        this.resultatObtenu = resultatObtenu;
    }

    public Integer getTempsTotalMinutes() {
        return tempsTotalMinutes;
    }

    public void setTempsTotalMinutes(Integer tempsTotalMinutes) {
        this.tempsTotalMinutes = tempsTotalMinutes;
    }

    public List<RessourceUtilisee> getRessourcesUtilisees() {
        return ressourcesUtilisees;
    }

    public void setRessourcesUtilisees(List<RessourceUtilisee> ressourcesUtilisees) {
        this.ressourcesUtilisees = ressourcesUtilisees;
    }

    public List<EquipementUtilise> getEquipementsUtilises() {
        return equipementsUtilises;
    }

    public void setEquipementsUtilises(List<EquipementUtilise> equipementsUtilises) {
        this.equipementsUtilises = equipementsUtilises;
    }

    public String getProblemesRencontres() {
        return problemesRencontres;
    }

    public void setProblemesRencontres(String problemesRencontres) {
        this.problemesRencontres = problemesRencontres;
    }

    public List<Integer> getPhotoIds() {
        return photoIds;
    }

    public void setPhotoIds(List<Integer> photoIds) {
        this.photoIds = photoIds;
    }

    public String getSignatureElectronique() {
        return signatureElectronique;
    }

    public void setSignatureElectronique(String signatureElectronique) {
        this.signatureElectronique = signatureElectronique;
    }

    // Classes internes pour les ressources et équipements utilisés
    public static class RessourceUtilisee {
        private Integer ressourceId;
        private String type;
        private Double quantite;
        private String reference;
        private String numeroLot;

        public RessourceUtilisee() {}

        public Integer getRessourceId() { return ressourceId; }
        public void setRessourceId(Integer ressourceId) { this.ressourceId = ressourceId; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Double getQuantite() { return quantite; }
        public void setQuantite(Double quantite) { this.quantite = quantite; }

        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }

        public String getNumeroLot() { return numeroLot; }
        public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }
    }

    public static class EquipementUtilise {
        private Integer equipementId;
        private String type;
        private String reference;
        private Integer dureeUtilisationMinutes;

        public EquipementUtilise() {}

        public Integer getEquipementId() { return equipementId; }
        public void setEquipementId(Integer equipementId) { this.equipementId = equipementId; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }

        public Integer getDureeUtilisationMinutes() { return dureeUtilisationMinutes; }
        public void setDureeUtilisationMinutes(Integer dureeUtilisationMinutes) { this.dureeUtilisationMinutes = dureeUtilisationMinutes; }
    }
}

