package tn.SGII_Ville.model;

import java.util.List;


public class RessourceDisponibilite {
    private String date;
    private List<EquipementDisponibilite> equipementsDisponibles;
    private List<RessourceMaterielleDisponibilite> ressourcesDisponibles;
    private Boolean tousDisponibles;
    private String message;

    // Constructeurs
    public RessourceDisponibilite() {}

    public RessourceDisponibilite(String date, List<EquipementDisponibilite> equipementsDisponibles, 
                                 List<RessourceMaterielleDisponibilite> ressourcesDisponibles, 
                                 Boolean tousDisponibles) {
        this.date = date;
        this.equipementsDisponibles = equipementsDisponibles;
        this.ressourcesDisponibles = ressourcesDisponibles;
        this.tousDisponibles = tousDisponibles;
    }

    // Getters et Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<EquipementDisponibilite> getEquipementsDisponibles() { return equipementsDisponibles; }
    public void setEquipementsDisponibles(List<EquipementDisponibilite> equipementsDisponibles) { 
        this.equipementsDisponibles = equipementsDisponibles;
    }

    public List<RessourceMaterielleDisponibilite> getRessourcesDisponibles() { return ressourcesDisponibles; }
    public void setRessourcesDisponibles(List<RessourceMaterielleDisponibilite> ressourcesDisponibles) { 
        this.ressourcesDisponibles = ressourcesDisponibles;
    }

    public Boolean getTousDisponibles() { return tousDisponibles; }
    public void setTousDisponibles(Boolean tousDisponibles) { this.tousDisponibles = tousDisponibles; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}