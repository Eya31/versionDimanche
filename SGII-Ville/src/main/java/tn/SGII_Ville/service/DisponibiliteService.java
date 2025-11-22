package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SGII_Ville.model.*;
import tn.SGII_Ville.repository.PlanningRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibiliteService {
    
    private final PlanningRepository planningRepository;
    
    public DisponibiliteService(PlanningRepository planningRepository) {
        this.planningRepository = planningRepository;
    }
    
    /**
     * Récupère les disponibilités d'un technicien pour une date
     */
    public List<CreneauDisponibilite> getDisponibilitesTechnicien(Integer technicienId, String date) {
        return planningRepository.getDisponibilitesTechnicien(technicienId, date);
    }
    
    /**
     * Marque un technicien comme indisponible pour une date
     */
    public void marquerIndisponible(Integer technicienId, String date, String raison, Integer interventionId) {
        planningRepository.marquerIndisponible(technicienId, date, raison, interventionId);
    }
    
    /**
     * Récupère toutes les indisponibilités pour une période
     */
    public List<CreneauDisponibilite> getIndisponibilites(String startDate, String endDate) {
        return planningRepository.getIndisponibilitesPeriod(startDate, endDate);
    }
    
    /**
     * Génère les créneaux par défaut pour une journée
     */
    public List<Creneau> getCreneauxParDefaut() {
        List<Creneau> creneaux = new ArrayList<>();
        
        // Créneau matin
        Creneau matin = new Creneau();
        matin.setDebut("08:00");
        matin.setFin("12:00");
        matin.setDisponible(true);
        creneaux.add(matin);
        
        // Créneau après-midi
        Creneau apresMidi = new Creneau();
        apresMidi.setDebut("14:00");
        apresMidi.setFin("18:00");
        apresMidi.setDisponible(true);
        creneaux.add(apresMidi);
        
        return creneaux;
    }
    
    /**
     * Vérifie si un créneau est disponible
     */
    public boolean isCreneauDisponible(Integer technicienId, String date, String debut, String fin) {
        List<CreneauDisponibilite> disponibilites = getDisponibilitesTechnicien(technicienId, date);
        
        return disponibilites.stream()
            .flatMap(d -> d.getCreneaux().stream())
            .filter(c -> c.getDebut().equals(debut) && c.getFin().equals(fin))
            .anyMatch(Creneau::isDisponible);
    }
}