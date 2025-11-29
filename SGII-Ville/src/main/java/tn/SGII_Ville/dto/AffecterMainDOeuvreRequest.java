package tn.SGII_Ville.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AffecterMainDOeuvreRequest {
    @JsonProperty("ouvrierIds")
    private List<Integer> ouvrierIds;
    
    @JsonProperty("mainDOeuvreIds")
    private List<Integer> mainDOeuvreIds; // Alias pour compatibilité frontend
    
    private Integer interventionId;

    public AffecterMainDOeuvreRequest() {}

    public List<Integer> getOuvrierIds() {
        // Si ouvrierIds est null mais mainDOeuvreIds est défini, utiliser mainDOeuvreIds
        if (ouvrierIds == null && mainDOeuvreIds != null) {
            return mainDOeuvreIds;
        }
        return ouvrierIds;
    }

    public void setOuvrierIds(List<Integer> ouvrierIds) {
        this.ouvrierIds = ouvrierIds;
    }

    public List<Integer> getMainDOeuvreIds() {
        return mainDOeuvreIds;
    }

    public void setMainDOeuvreIds(List<Integer> mainDOeuvreIds) {
        this.mainDOeuvreIds = mainDOeuvreIds;
        // Si ouvrierIds n'est pas défini, utiliser mainDOeuvreIds
        if (this.ouvrierIds == null) {
            this.ouvrierIds = mainDOeuvreIds;
        }
    }

    public Integer getInterventionId() {
        return interventionId;
    }

    public void setInterventionId(Integer interventionId) {
        this.interventionId = interventionId;
    }
}

