package tn.SGII_Ville.dto;

public class EquipementRequis {
    private String type;
    private int quantiteRequise;

    public EquipementRequis() {
        this.quantiteRequise = 1;
    }

    public EquipementRequis(String type, int quantiteRequise) {
        this.type = type;
        this.quantiteRequise = quantiteRequise;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantiteRequise() {
        return quantiteRequise;
    }

    public void setQuantiteRequise(int quantiteRequise) {
        this.quantiteRequise = quantiteRequise;
    }

    @Override
    public String toString() {
        return "EquipementRequis{" +
                "type='" + type + '\'' +
                ", quantiteRequise=" + quantiteRequise +
                '}';
    }
}
