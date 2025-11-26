package tn.SGII_Ville.dto;

import java.util.List;

public class AffecterMainDOeuvreRequest {
    private List<Integer> ouvrierIds;
    private Integer interventionId;

    public AffecterMainDOeuvreRequest() {}

    public List<Integer> getOuvrierIds() {
        return ouvrierIds;
    }

    public void setOuvrierIds(List<Integer> ouvrierIds) {
        this.ouvrierIds = ouvrierIds;
    }

    public Integer getInterventionId() {
        return interventionId;
    }

    public void setInterventionId(Integer interventionId) {
        this.interventionId = interventionId;
    }
}

