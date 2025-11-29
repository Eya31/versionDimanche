package tn.SGII_Ville.dto;

import java.time.LocalDate;

public class DateValidationRequest {
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private CompetenceRequise[] competencesRequises;
    private MaterielRequis[] materielsRequis;
    private EquipementRequis[] equipementsRequis;

    public DateValidationRequest() {}

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public CompetenceRequise[] getCompetencesRequises() {
        return competencesRequises;
    }

    public void setCompetencesRequises(CompetenceRequise[] competencesRequises) {
        this.competencesRequises = competencesRequises;
    }

    public MaterielRequis[] getMaterielsRequis() {
        return materielsRequis;
    }

    public void setMaterielsRequis(MaterielRequis[] materielsRequis) {
        this.materielsRequis = materielsRequis;
    }

    public EquipementRequis[] getEquipementsRequis() {
        return equipementsRequis;
    }

    public void setEquipementsRequis(EquipementRequis[] equipementsRequis) {
        this.equipementsRequis = equipementsRequis;
    }
}
