package tn.SGII_Ville.dto;

/**
 * DTO pour assigner une tâche à une main-d'œuvre
 */
public class AssignerTacheRequest {
    private Integer mainDOeuvreId;

    public AssignerTacheRequest() {}

    public Integer getMainDOeuvreId() {
        return mainDOeuvreId;
    }

    public void setMainDOeuvreId(Integer mainDOeuvreId) {
        this.mainDOeuvreId = mainDOeuvreId;
    }
}

