package tn.SGII_Ville.dto;

public class CompetenceRequise {
    private String competence;
    private int nombreTechniciens;

    public CompetenceRequise() {
        this.nombreTechniciens = 1;
    }

    public CompetenceRequise(String competence, int nombreTechniciens) {
        this.competence = competence;
        this.nombreTechniciens = nombreTechniciens;
    }

    public String getCompetence() {
        return competence;
    }

    public void setCompetence(String competence) {
        this.competence = competence;
    }

    public int getNombreTechniciens() {
        return nombreTechniciens;
    }

    public void setNombreTechniciens(int nombreTechniciens) {
        this.nombreTechniciens = nombreTechniciens;
    }

    @Override
    public String toString() {
        return "CompetenceRequise{" +
                "competence='" + competence + '\'' +
                ", nombreTechniciens=" + nombreTechniciens +
                '}';
    }
}
