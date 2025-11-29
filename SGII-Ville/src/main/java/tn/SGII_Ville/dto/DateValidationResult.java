package tn.SGII_Ville.dto;

import java.time.LocalDate;

public class DateValidationResult {
    private LocalDate date;
    private String status; // "VERT", "JAUNE", "ROUGE"
    private String message;
    private boolean techniciensDisponibles;
    private boolean equipementsDisponibles;
    private boolean materielsDisponibles;

    public DateValidationResult() {}

    public DateValidationResult(LocalDate date, String status, String message) {
        this.date = date;
        this.status = status;
        this.message = message;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isTechniciensDisponibles() {
        return techniciensDisponibles;
    }

    public void setTechniciensDisponibles(boolean techniciensDisponibles) {
        this.techniciensDisponibles = techniciensDisponibles;
    }

    public boolean isEquipementsDisponibles() {
        return equipementsDisponibles;
    }

    public void setEquipementsDisponibles(boolean equipementsDisponibles) {
        this.equipementsDisponibles = equipementsDisponibles;
    }

    public boolean isMaterielsDisponibles() {
        return materielsDisponibles;
    }

    public void setMaterielsDisponibles(boolean materielsDisponibles) {
        this.materielsDisponibles = materielsDisponibles;
    }

    @Override
    public String toString() {
        return "DateValidationResult{" +
                "date=" + date +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", techniciensDisponibles=" + techniciensDisponibles +
                ", equipementsDisponibles=" + equipementsDisponibles +
                ", materielsDisponibles=" + materielsDisponibles +
                '}';
    }
}
