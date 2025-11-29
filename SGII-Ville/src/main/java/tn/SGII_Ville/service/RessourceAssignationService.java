package tn.SGII_Ville.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.SGII_Ville.dto.AssignerRessourcesRequest;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RessourceAssignationService {

    private final UserXmlService userXmlService;
    private final EquipementXmlService equipementXmlService;
    private final RessourceMaterielleXmlService ressourceMaterielleXmlService;

    public void assignerRessources(AssignerRessourcesRequest request) {
        log.info("Début de l'assignation des ressources pour la date: {}", request.getDateIntervention());
        
        List<String> erreurs = new ArrayList<>();
        
        try {
            // 1. Marquer les techniciens comme indisponibles
            if (request.getTechniciensIds() != null && !request.getTechniciensIds().isEmpty()) {
                for (Integer techId : request.getTechniciensIds()) {
                    try {
                        log.info("Marquage du technicien {} comme indisponible", techId);
                        userXmlService.marquerTechnicienIndisponible(techId.longValue(), request.getDateIntervention());
                    } catch (Exception e) {
                        String msg = "Erreur lors du marquage du technicien " + techId + ": " + e.getMessage();
                        log.error(msg, e);
                        erreurs.add(msg);
                    }
                }
            }
            
            // 2. Marquer les équipements comme indisponibles
            if (request.getEquipementsIds() != null && !request.getEquipementsIds().isEmpty()) {
                for (Integer equipId : request.getEquipementsIds()) {
                    try {
                        log.info("Marquage de l'équipement {} comme indisponible", equipId);
                        equipementXmlService.ajouterDateIndisponibilite(equipId, request.getDateIntervention());
                    } catch (Exception e) {
                        String msg = "Erreur lors du marquage de l'équipement " + equipId + ": " + e.getMessage();
                        log.error(msg, e);
                        erreurs.add(msg);
                    }
                }
            }
            
            // 3. Réduire les stocks de matériels
            if (request.getMateriels() != null && !request.getMateriels().isEmpty()) {
                for (AssignerRessourcesRequest.MaterielQuantite materielQte : request.getMateriels()) {
                    try {
                        Integer materielId = materielQte.getMaterielId();
                        Integer quantite = materielQte.getQuantite();
                        
                        log.info("Réduction du stock du matériel {} de {} unités", materielId, quantite);
                        
                        ressourceMaterielleXmlService.reduireStock(materielId, quantite);
                        
                        log.info("Stock du matériel {} réduit de {} unités", materielId, quantite);
                    } catch (Exception e) {
                        String msg = "Erreur lors de la réduction du stock du matériel " + 
                            materielQte.getMaterielId() + ": " + e.getMessage();
                        log.error(msg, e);
                        erreurs.add(msg);
                    }
                }
            }
            
            if (!erreurs.isEmpty()) {
                throw new RuntimeException("Erreurs lors de l'assignation: " + String.join("; ", erreurs));
            }
            
            log.info("Assignation des ressources terminée avec succès");
            
        } catch (Exception e) {
            log.error("Erreur critique lors de l'assignation des ressources", e);
            throw new RuntimeException("Échec de l'assignation des ressources: " + e.getMessage(), e);
        }
    }
}
