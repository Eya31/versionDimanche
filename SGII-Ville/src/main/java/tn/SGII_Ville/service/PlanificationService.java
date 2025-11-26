package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SGII_Ville.model.*;
import tn.SGII_Ville.repository.InterventionRepository;
import tn.SGII_Ville.repository.TechnicienRepository;
import tn.SGII_Ville.repository.EquipementRepository;
import tn.SGII_Ville.repository.RessourceRepository;
import tn.SGII_Ville.exception.RessourceIndisponibleException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanificationService {
    
    @Autowired
    private InterventionRepository interventionRepository;
    
    @Autowired
    private TechnicienRepository technicienRepository;
    
    @Autowired
    private EquipementRepository equipementRepository;
    
    @Autowired
    private RessourceRepository ressourceRepository;
    
    @Autowired
    private DisponibiliteService disponibiliteService;
    
    /**
     * Récupère tous les techniciens disponibles pour une date donnée
     */
    public List<DisponibiliteTechnicien> getTechniciensDisponibles(String date, String competences) {
        List<Technicien> tousTechniciens = technicienRepository.findAll();
        List<DisponibiliteTechnicien> result = new ArrayList<>();
        
        for (Technicien tech : tousTechniciens) {
            // Vérifier si le technicien a les compétences requises
            if (competences != null && !competences.isEmpty()) {
                List<String> competencesRequises = Arrays.asList(competences.split(","));
                if (!hasCompetences(tech, competencesRequises)) {
                    continue;
                }
            }
            
            // Vérifier la disponibilité
            List<CreneauDisponibilite> disponibilites = disponibiliteService.getDisponibilitesTechnicien(tech.getId(), date);
            boolean disponible = disponibilites.stream()
                .flatMap(d -> d.getCreneaux().stream())
                .anyMatch(Creneau::isDisponible);
            
            DisponibiliteTechnicien dispoTech = new DisponibiliteTechnicien(
                tech.getId(),
                tech.getNom(),
                tech.getEmail(),
                tech.getCompetences(),
                disponibilites,
                disponible
            );
            
            result.add(dispoTech);
        }
        
        return result;
    }
    
    /**
     * Vérifie la disponibilité des ressources pour une date
     */
    public RessourceDisponibilite checkRessourcesDisponibles(String date, 
                                                            List<Integer> equipementIds, 
                                                            List<Integer> ressourceIds) {
        RessourceDisponibilite result = new RessourceDisponibilite();
        result.setDate(date);
        
        // Vérifier disponibilité des équipements
        List<EquipementDisponibilite> equipementsDispos = new ArrayList<>();
        if (equipementIds != null) {
            for (Integer equipementId : equipementIds) {
                Optional<Equipement> equipementOpt = equipementRepository.findById(equipementId);
                if (equipementOpt.isPresent()) {
                    Equipement equipement = equipementOpt.get();
                    boolean disponible = isEquipementDisponible(equipement, date);
                    
                    EquipementDisponibilite equipDispo = new EquipementDisponibilite(
                        equipement.getId(),
                        equipement.getType(),
                        equipement.getEtat(),
                        disponible
                    );
                    
                    if (!disponible) {
                        equipDispo.setRaisonIndisponibilite("Équipement en intervention");
                    }
                    
                    equipementsDispos.add(equipDispo);
                }
            }
        }
        result.setEquipementsDisponibles(equipementsDispos);
        
        // Vérifier disponibilité des ressources matérielles
        List<RessourceMaterielleDisponibilite> ressourcesDispos = new ArrayList<>();
        if (ressourceIds != null) {
            for (Integer ressourceId : ressourceIds) {
                Optional<RessourceMaterielle> ressourceOpt = ressourceRepository.findById(ressourceId);
                if (ressourceOpt.isPresent()) {
                    RessourceMaterielle ressource = ressourceOpt.get();
                    boolean disponible = ressource.getQuantiteEnStock() > 0;
                    
                    RessourceMaterielleDisponibilite resDispo = new RessourceMaterielleDisponibilite(
                        ressource.getId(),
                        ressource.getDesignation(),
                        ressource.getQuantiteEnStock(),
                        1, // Quantité demandée par défaut
                        disponible
                    );
                    
                    ressourcesDispos.add(resDispo);
                }
            }
        }
        result.setRessourcesDisponibles(ressourcesDispos);
        
        // Vérifier si toutes les ressources sont disponibles
        boolean tousEquipementsDispos = equipementsDispos.stream().allMatch(EquipementDisponibilite::getDisponible);
        boolean toutesRessourcesDispos = ressourcesDispos.stream().allMatch(RessourceMaterielleDisponibilite::getDisponible);
        result.setTousDisponibles(tousEquipementsDispos && toutesRessourcesDispos);
        
        if (!result.getTousDisponibles()) {
            result.setMessage("Certaines ressources ne sont pas disponibles pour la date sélectionnée");
        }
        
        return result;
    }
    
    /**
     * Planifie une intervention avec vérification des disponibilités
     */
    public Intervention planifierIntervention(PlanificationRequest request) throws RessourceIndisponibleException {
        // Vérifier la disponibilité des techniciens
        for (Integer technicienId : request.getTechniciensIds()) {
            if (!isTechnicienDisponible(technicienId, request.getDatePlanifiee())) {
                throw new RessourceIndisponibleException("Le technicien #" + technicienId + " n'est pas disponible à cette date");
            }
        }
        
        // Vérifier la disponibilité des équipements
        for (Integer equipementId : request.getEquipementsIds()) {
            if (!isEquipementDisponible(equipementId, request.getDatePlanifiee())) {
                throw new RessourceIndisponibleException("L'équipement #" + equipementId + " n'est pas disponible à cette date");
            }
        }
        
        // Vérifier la disponibilité des ressources
        for (Integer ressourceId : request.getRessourcesIds()) {
            if (!isRessourceDisponible(ressourceId)) {
                throw new RessourceIndisponibleException("La ressource #" + ressourceId + " n'est pas disponible");
            }
        }
        
        // Créer l'intervention
        Intervention intervention = new Intervention();
        intervention.setPriorite(request.getPriorite());
        intervention.setEtat("PLANIFIEE");
        intervention.setDatePlanifiee(request.getDatePlanifiee());
        intervention.setBudget(request.getBudget());
        intervention.setDemandeId(request.getDemandeId());
        intervention.setTechniciens(request.getTechniciensIds());
        intervention.setEquipements(request.getEquipementsIds());
        intervention.setRessources(request.getRessourcesIds());
        
        // Sauvegarder l'intervention
        interventionRepository.save(intervention);
        
        // Marquer les ressources comme utilisées
        updateDisponibilitesAfterPlanification(request);
        
        return intervention;
    }
    
    /**
     * Récupère les techniciens disponibles pour une période
     */
    public List<DisponibiliteTechnicien> getTechniciensDisponiblesForPeriod(Integer technicienId, 
                                                                           LocalDate startDate, 
                                                                           LocalDate endDate) {
        List<DisponibiliteTechnicien> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(formatter);
            List<DisponibiliteTechnicien> dispoDuJour = getTechniciensDisponibles(dateStr, null);
            result.addAll(dispoDuJour);
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }
    
    // Méthodes privées utilitaires
    private boolean hasCompetences(Technicien technicien, List<String> competencesRequises) {
        if (technicien.getCompetences() == null) return false;
        
        return technicien.getCompetences().stream()
            .anyMatch(competencesRequises::contains);
    }
    
    private boolean isTechnicienDisponible(Integer technicienId, String date) {
        List<CreneauDisponibilite> disponibilites = disponibiliteService.getDisponibilitesTechnicien(technicienId, date);
        return disponibilites.stream()
            .flatMap(d -> d.getCreneaux().stream())
            .anyMatch(Creneau::isDisponible);
    }
    
    private boolean isEquipementDisponible(Integer equipementId, String date) {
        Optional<Equipement> equipementOpt = equipementRepository.findById(equipementId);
        if (equipementOpt.isEmpty()) return false;
        
        return isEquipementDisponible(equipementOpt.get(), date);
    }
    
    private boolean isEquipementDisponible(Equipement equipement, String date) {
        // Vérifier si l'équipement est fonctionnel
        if (!"FONCTIONNEL".equals(equipement.getEtat())) {
            return false;
        }
        
        // Vérifier si l'équipement n'est pas déjà utilisé dans une intervention ce jour-là
        List<Intervention> interventions = interventionRepository.findByDate(date);
        for (Intervention intervention : interventions) {
            if (intervention.getEquipements() != null && 
                intervention.getEquipements().contains(equipement.getId())) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isRessourceDisponible(Integer ressourceId) {
        Optional<RessourceMaterielle> ressourceOpt = ressourceRepository.findById(ressourceId);
        return ressourceOpt.isPresent() && ressourceOpt.get().getQuantiteEnStock() > 0;
    }
    
    private void updateDisponibilitesAfterPlanification(PlanificationRequest request) {
        // Mettre à jour les disponibilités des techniciens
        for (Integer technicienId : request.getTechniciensIds()) {
            disponibiliteService.marquerIndisponible(technicienId, request.getDatePlanifiee(), 
                                                   "INTERVENTION", request.getDemandeId());
        }
        
        // Mettre à jour les quantités de ressources
        for (Integer ressourceId : request.getRessourcesIds()) {
            Optional<RessourceMaterielle> ressourceOpt = ressourceRepository.findById(ressourceId);
            if (ressourceOpt.isPresent()) {
                RessourceMaterielle ressource = ressourceOpt.get();
                ressource.setQuantiteEnStock(ressource.getQuantiteEnStock() - 1);
                ressourceRepository.save(ressource);
            }
        }
    }
}