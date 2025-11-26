package tn.SGII_Ville.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO pour représenter les disponibilités d'un agent
 */
public class DisponibiliteDTO {
    private DayOfWeek jour;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private boolean disponible; // true si jour travaillé, false si congé/absence

    public DisponibiliteDTO() {}

    public DisponibiliteDTO(DayOfWeek jour, LocalTime heureDebut, LocalTime heureFin, boolean disponible) {
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.disponible = disponible;
    }

    // Getters et Setters
    public DayOfWeek getJour() {
        return jour;
    }

    public void setJour(DayOfWeek jour) {
        this.jour = jour;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}

