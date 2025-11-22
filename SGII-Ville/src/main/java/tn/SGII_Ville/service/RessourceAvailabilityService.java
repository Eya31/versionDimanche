package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SGII_Ville.model.*;
import tn.SGII_Ville.repository.EquipementRepository;
import tn.SGII_Ville.repository.RessourceRepository;
import tn.SGII_Ville.repository.InterventionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RessourceAvailabilityService {
    
    @Autowired
    private EquipementRepository equipementRepository;
    
    @Autowired
    private RessourceRepository ressourceRepository;
    
    @Autowired
    private InterventionRepository interventionRepository;
    
    /**
     * Vérifie la disponibilité globale des ressources
     */
    public RessourceDisponibilite verifierDisponibiliteGlobale(String date, 
                                                              List<Integer> technicienIds,
                                                              List<Integer> equipementIds, 
                                                              List<Integer> ressourceIds) {
        RessourceDisponibilite result = new RessourceDisponibilite();
        result.setDate(date);
        
        // Vérifier équipements
        List<EquipementDisponibilite> equipementsDispos = new ArrayList<>();
        if (equipementIds != null) {
            for (Integer equipementId : equipementIds) {
                equipementsDispos.add(verifierDisponibiliteEquipement(equipementId, date));
            }
        }
        result.setEquipementsDisponibles(equipementsDispos);
        
        // Vérifier ressources matérielles
        List<RessourceMaterielleDisponibilite> ressourcesDispos = new ArrayList<>();
        if (ressourceIds != null) {
            for (Integer ressourceId : ressourceIds) {
                ressourcesDispos.add(verifierDisponibiliteRessource(ressourceId));
            }
        }
        result.setRessourcesDisponibles(ressourcesDispos);
        
        // Déterminer si toutes les ressources sont disponibles
        boolean tousEquipementsDispos = equipementsDispos.stream().allMatch(EquipementDisponibilite::getDisponible);
        boolean toutesRessourcesDispos = ressourcesDispos.stream().allMatch(RessourceMaterielleDisponibilite::getDisponible);
        result.setTousDisponibles(tousEquipementsDispos && toutesRessourcesDispos);
        
        return result;
    }
    
    /**
     * Vérifie la disponibilité d'un équipement spécifique
     */
    public EquipementDisponibilite verifierDisponibiliteEquipement(Integer equipementId, String date) {
        Optional<Equipement> equipementOpt = equipementRepository.findById(equipementId);
        
        if (equipementOpt.isEmpty()) {
            EquipementDisponibilite indisponible = new EquipementDisponibilite();
            indisponible.setEquipementId(equipementId);
            indisponible.setDisponible(false);
            indisponible.setRaisonIndisponibilite("Équipement non trouvé");
            return indisponible;
        }
        
        Equipement equipement = equipementOpt.get();
        EquipementDisponibilite result = new EquipementDisponibilite();
        result.setEquipementId(equipementId);
        result.setType(equipement.getType());
        result.setEtat(equipement.getEtat());
        
        // Vérifier si l'équipement est fonctionnel
        if (!"FONCTIONNEL".equals(equipement.getEtat())) {
            result.setDisponible(false);
            result.setRaisonIndisponibilite("Équipement " + equipement.getEtat().toLowerCase());
            return result;
        }
        
        // Vérifier si l'équipement n'est pas déjà utilisé
        boolean estUtilise = interventionRepository.findByDate(date).stream()
            .anyMatch(intervention -> 
                intervention.getEquipements() != null && 
                intervention.getEquipements().contains(equipementId)
            );
        
        result.setDisponible(!estUtilise);
        if (estUtilise) {
            result.setRaisonIndisponibilite("Équipement déjà réservé pour cette date");
        }
        
        return result;
    }
    
    /**
     * Vérifie la disponibilité d'une ressource matérielle
     */
    public RessourceMaterielleDisponibilite verifierDisponibiliteRessource(Integer ressourceId) {
        Optional<RessourceMaterielle> ressourceOpt = ressourceRepository.findById(ressourceId);
        
        if (ressourceOpt.isEmpty()) {
            RessourceMaterielleDisponibilite indisponible = new RessourceMaterielleDisponibilite();
            indisponible.setRessourceId(ressourceId);
            indisponible.setDisponible(false);
            return indisponible;
        }
        
        RessourceMaterielle ressource = ressourceOpt.get();
        RessourceMaterielleDisponibilite result = new RessourceMaterielleDisponibilite();
        result.setRessourceId(ressourceId);
        result.setDesignation(ressource.getDesignation());
        result.setQuantiteDisponible(ressource.getQuantiteEnStock());
        result.setQuantiteDemandee(1); // Par défaut
        result.setDisponible(ressource.getQuantiteEnStock() > 0);
        
        return result;
    }
    
    /**
     * Met à jour le stock après utilisation
     */
    public void mettreAJourStock(Integer ressourceId, Integer quantiteUtilisee) {
        Optional<RessourceMaterielle> ressourceOpt = ressourceRepository.findById(ressourceId);
        if (ressourceOpt.isPresent()) {
            RessourceMaterielle ressource = ressourceOpt.get();
            int nouveauStock = ressource.getQuantiteEnStock() - quantiteUtilisee;
            ressource.setQuantiteEnStock(Math.max(0, nouveauStock));
            ressourceRepository.save(ressource);
        }
    }
    
    /**
     * Récupère les ressources en seuil d'alerte
     */
    public List<RessourceMaterielleDisponibilite> getRessourcesAlerteStock() {
        List<RessourceMaterielle> toutesRessources = ressourceRepository.findAll();
        List<RessourceMaterielleDisponibilite> alertes = new ArrayList<>();
        
        for (RessourceMaterielle ressource : toutesRessources) {
            if (ressource.getQuantiteEnStock() < 5) { // Seuil d'alerte
                RessourceMaterielleDisponibilite alerte = new RessourceMaterielleDisponibilite();
                alerte.setRessourceId(ressource.getId());
                alerte.setDesignation(ressource.getDesignation());
                alerte.setQuantiteDisponible(ressource.getQuantiteEnStock());
                alerte.setQuantiteDemandee(0);
                alerte.setDisponible(true);
                alerte.setSeuilAlerte(true);
                alertes.add(alerte);
            }
        }
        
        return alertes;
    }
}