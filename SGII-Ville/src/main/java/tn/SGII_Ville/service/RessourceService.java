package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.SGII_Ville.model.RessourceMaterielle;
import tn.SGII_Ville.repository.RessourceRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RessourceService {
    
    @Autowired
    private RessourceRepository ressourceRepository;
    
    public List<RessourceMaterielle> findAll() {
        return ressourceRepository.findAll();
    }
    
    public Optional<RessourceMaterielle> findById(Integer id) {
        return ressourceRepository.findById(id);
    }
    
    public List<RessourceMaterielle> findRessourcesDisponibles() {
        return ressourceRepository.findAll().stream()
            .filter(r -> r.getQuantiteEnStock() > 0)
            .toList();
    }
    
    public RessourceMaterielle save(RessourceMaterielle ressource) {
        return ressourceRepository.save(ressource);
    }
    
    public void deleteById(Integer id) {
        ressourceRepository.deleteById(id);
    }
}