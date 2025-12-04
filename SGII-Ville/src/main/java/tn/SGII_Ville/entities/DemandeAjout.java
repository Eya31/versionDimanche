
package tn.SGII_Ville.entities;


import tn.SGII_Ville.model.enums.EtatDemandeAjoutType;
import tn.SGII_Ville.model.enums.TypeDemandeAjout;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DemandeAjout {
    private int id;
    private TypeDemandeAjout typeDemande;
    private String designation;
    private int quantite;
    private double budget;
    private String justification;
    private int chefId;
    private Integer adminId;
    private LocalDateTime dateTraitement;
    private String motifRefus;
    
    // Anciens champs pour compatibilité
    private String typeObjet;
    private int idChefService;
    private LocalDate dateSoumission;
        private LocalDateTime dateDemande = LocalDateTime.now(); // ✅ INITIALISÉ

    private EtatDemandeAjoutType etat = EtatDemandeAjoutType.EN_ATTENTE_ADMIN; // ✅ INITIALISÉ

    // Constructeurs
public DemandeAjout() {
        // Initialisation par défaut
        this.etat = EtatDemandeAjoutType.EN_ATTENTE_ADMIN;
        this.dateDemande = LocalDateTime.now();
    }
    public DemandeAjout(int id, String typeObjet, int idChefService, 
                       EtatDemandeAjoutType etat, LocalDate dateSoumission) {
        this.id = id;
        this.typeObjet = typeObjet;
        this.idChefService = idChefService;
        this.etat = etat;
        this.dateSoumission = dateSoumission;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TypeDemandeAjout getTypeDemande() {
        return typeDemande;
    }

    public void setTypeDemande(TypeDemandeAjout typeDemande) {
        this.typeDemande = typeDemande;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public int getChefId() {
        return chefId;
    }

    public void setChefId(int chefId) {
        this.chefId = chefId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public LocalDateTime getDateTraitement() {
        return dateTraitement;
    }

    public void setDateTraitement(LocalDateTime dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    public String getMotifRefus() {
        return motifRefus;
    }

    public void setMotifRefus(String motifRefus) {
        this.motifRefus = motifRefus;
    }

    public LocalDateTime getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
    }

    // Anciens getters/setters pour compatibilité
    public String getTypeObjet() {
        return typeObjet;
    }

    public void setTypeObjet(String typeObjet) {
        this.typeObjet = typeObjet;
    }

    public int getIdChefService() {
        return idChefService;
    }

    public void setIdChefService(int idChefService) {
        this.idChefService = idChefService;
    }

    public EtatDemandeAjoutType getEtat() {
        return etat;
    }

    public void setEtat(EtatDemandeAjoutType etat) {
        this.etat = etat;
    }

    public LocalDate getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDate dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    @Override
    public String toString() {
        return "DemandeAjout{" +
                "id=" + id +
                ", typeDemande=" + typeDemande +
                ", designation='" + designation + '\'' +
                ", quantite=" + quantite +
                ", budget=" + budget +
                ", etat=" + etat +
                ", chefId=" + chefId +
                ", dateDemande=" + dateDemande +
                '}';
    }
}