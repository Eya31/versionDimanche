package tn.SGII_Ville.dto;

import java.util.List;

public class AffecterMainDOeuvreRequest {
    private List<Integer> mainDOeuvreIds;
    private Integer interventionId;

    public AffecterMainDOeuvreRequest() {}

    public List<Integer> getMainDOeuvreIds() {
        return mainDOeuvreIds;
    }

    public void setMainDOeuvreIds(List<Integer> mainDOeuvreIds) {
        this.mainDOeuvreIds = mainDOeuvreIds;
    }

    public Integer getInterventionId() {
        return interventionId;
    }

    public void setInterventionId(Integer interventionId) {
        this.interventionId = interventionId;
    }
}

