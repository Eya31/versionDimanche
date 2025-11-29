package tn.SGII_Ville.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssignerRessourcesRequest {
    private Integer demandeId;
    private String dateIntervention;
    private List<Integer> techniciensIds;
    private List<Integer> equipementsIds;
    private List<MaterielQuantite> materiels;
    
    @Data
    public static class MaterielQuantite {
        private Integer materielId;
        private Integer quantite;
    }
}
