package tn.SGII_Ville.dto;

public class MaterielRequis {
    private String designation;
    private int quantiteRequise;

    public MaterielRequis() {}

    public MaterielRequis(String designation, int quantiteRequise) {
        this.designation = designation;
        this.quantiteRequise = quantiteRequise;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public int getQuantiteRequise() {
        return quantiteRequise;
    }

    public void setQuantiteRequise(int quantiteRequise) {
        this.quantiteRequise = quantiteRequise;
    }

    @Override
    public String toString() {
        return "MaterielRequis{" +
                "designation='" + designation + '\'' +
                ", quantiteRequise=" + quantiteRequise +
                '}';
    }
}
