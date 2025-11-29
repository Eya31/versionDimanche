import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EquipementService } from '../../../../services/equipement.service';
import { RessourceService } from '../../../../services/ressource.service';
import { InterventionService } from '../../../../services/intervention.service';
import { Equipement } from '../../../../models/equipement.model';
import { RessourceMaterielle } from '../../../../models/ressource.model';
import { Intervention } from '../../../../models/intervention.model';

interface MaterielComplet {
  id: number;
  type: 'EQUIPEMENT' | 'RESSOURCE';
  nom: string;
  categorie: string;
  reference: string;
  etat: string;
  quantiteTotale: number;
  quantiteAffectee: number;
  quantiteDisponible: number;
  description?: string;
  seuilMinimum: number; // Toujours défini maintenant
  unite?: string;
  valeurAchat?: number;
}

interface StockAlert {
  id: number;
  type: 'EQUIPEMENT' | 'RESSOURCE';
  designation: string;
  quantiteActuelle: number;
  quantiteDisponible: number;
  seuilMinimum: number;
  niveau: 'CRITIQUE' | 'FAIBLE' | 'NORMAL';
}

interface MouvementStock {
  id: number;
  date: string;
  type: 'EQUIPEMENT' | 'RESSOURCE';
  designation: string;
  mouvement: 'ENTREE' | 'SORTIE' | 'RETOUR' | 'AJUSTEMENT';
  quantite: number;
  quantiteAvant: number;
  quantiteApres: number;
  utilisateur?: string;
  raison?: string;
  interventionId?: number;
}

@Component({
  selector: 'app-materiel-stocks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './materiel-stocks.component.html',
  styleUrls: ['./materiel-stocks.component.css']
})
export class MaterielStocksComponent implements OnInit {
  equipements: Equipement[] = [];
  ressources: RessourceMaterielle[] = [];
  interventions: Intervention[] = [];
  materielComplet: MaterielComplet[] = [];
  stockAlerts: StockAlert[] = [];
  mouvements: MouvementStock[] = [];
  
  loading = false;
  activeTab: 'liste' | 'alertes' | 'mouvements' = 'liste';
  filterType: 'TOUS' | 'EQUIPEMENT' | 'RESSOURCE' = 'TOUS';
  filterCategorie: string = 'TOUS';
  filterEtat: string = 'TOUS';
  searchTerm: string = '';
  
  // Seuils minimum pour les alertes (configurables par matériel)
  seuilsMinimum: Map<number, number> = new Map();
  seuilParDefautRessource = 10;
  seuilParDefautEquipement = 5;
  
  // Modal pour configurer les seuils
  showSeuilModal = false;
  selectedMaterielForSeuil: MaterielComplet | null = null;
  nouveauSeuil: number = 0;

  constructor(
    private equipementService: EquipementService,
    private ressourceService: RessourceService,
    private interventionService: InterventionService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    
    // Charger interventions pour calculer les quantités affectées
    this.interventionService.getAllInterventions().subscribe({
      next: (interventions) => {
        this.interventions = interventions || [];
        this.loadEquipements();
        this.loadRessources();
      },
      error: (err) => {
        console.error('Erreur chargement interventions:', err);
        this.interventions = [];
        this.loadEquipements();
        this.loadRessources();
      }
    });
  }

  loadEquipements(): void {
    this.equipementService.getAllEquipements().subscribe({
      next: (data) => {
        this.equipements = data || [];
        this.buildMaterielComplet();
        this.updateAlerts();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement équipements:', err);
        this.equipements = [];
        this.loading = false;
      }
    });
  }

  loadRessources(): void {
    this.ressourceService.getAll().subscribe({
      next: (data) => {
        this.ressources = data || [];
        this.buildMaterielComplet();
        this.updateAlerts();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement ressources:', err);
        this.ressources = [];
        this.loading = false;
      }
    });
  }

  buildMaterielComplet(): void {
    this.materielComplet = [];
    
    // Ajouter les équipements
    this.equipements.forEach(equipement => {
      const quantiteAffectee = this.calculateQuantiteAffectee('EQUIPEMENT', equipement.id);
      const quantiteTotale = equipement.disponible ? 1 : 0;
      const quantiteDisponible = Math.max(0, quantiteTotale - quantiteAffectee);
      const seuilMin = this.seuilsMinimum.get(equipement.id) ?? this.seuilParDefautEquipement;
      
      this.materielComplet.push({
        id: equipement.id,
        type: 'EQUIPEMENT',
        nom: equipement.nom || 'Équipement sans nom',
        categorie: this.getCategorieFromType(equipement.type),
        reference: `EQ-${equipement.id.toString().padStart(4, '0')}`,
        etat: equipement.etat || 'NON_DEFINI',
        quantiteTotale,
        quantiteAffectee,
        quantiteDisponible,
        description: `${equipement.type} - ${equipement.etat}`,
        seuilMinimum: seuilMin,
        valeurAchat: equipement.valeurAchat
      });
    });

    // Ajouter les ressources
    this.ressources.forEach(ressource => {
      const quantiteAffectee = this.calculateQuantiteAffectee('RESSOURCE', ressource.id);
      const quantiteTotale = ressource.quantiteEnStock || 0;
      const quantiteDisponible = Math.max(0, quantiteTotale - quantiteAffectee);
      const seuilMin = this.seuilsMinimum.get(ressource.id) ?? this.seuilParDefautRessource;
      
      this.materielComplet.push({
        id: ressource.id,
        type: 'RESSOURCE',
        nom: ressource.designation,
        categorie: this.getCategorieFromDesignation(ressource.designation),
        reference: `RS-${ressource.id.toString().padStart(4, '0')}`,
        etat: quantiteDisponible > 0 ? 'DISPONIBLE' : 'RUPTURE_STOCK',
        quantiteTotale,
        quantiteAffectee,
        quantiteDisponible,
        description: `Ressource matérielle - ${ressource.unite || 'unité'}`,
        seuilMinimum: seuilMin,
        unite: ressource.unite,
        valeurAchat: ressource.valeurAchat
      });
    });
  }

  calculateQuantiteAffectee(type: 'EQUIPEMENT' | 'RESSOURCE', materielId: number): number {
    let count = 0;
    
    this.interventions.forEach(intervention => {
      if (intervention.etat === 'EN_COURS' || intervention.etat === 'EN_ATTENTE') {
        if (type === 'EQUIPEMENT' && intervention.equipementIds?.includes(materielId)) {
          count++;
        } else if (type === 'RESSOURCE' && intervention.ressourceIds?.includes(materielId)) {
          // Pour les ressources, on compte 1 par intervention (peut être ajusté)
          count++;
        }
      }
    });
    
    return count;
  }

  getCategorieFromType(type: string): string {
    const categories: { [key: string]: string } = {
      'OUTILLAGE': 'Outillage',
      'PROTECTION': 'Protection',
      'CONSOMMABLE': 'Consommable',
      'MACHINE': 'Machine',
      'VEHICULE': 'Véhicule'
    };
    return categories[type?.toUpperCase()] || type || 'Autre';
  }

  getCategorieFromDesignation(designation: string): string {
    const des = designation?.toLowerCase() || '';
    if (des.includes('cable') || des.includes('fil') || des.includes('ciment') || des.includes('peinture')) {
      return 'Consommable';
    }
    if (des.includes('gant') || des.includes('casque') || des.includes('chaussure')) {
      return 'Protection';
    }
    return 'Consommable';
  }

  updateAlerts(): void {
    this.stockAlerts = [];
    
    this.materielComplet.forEach(materiel => {
      const seuil = materiel.seuilMinimum ?? 
        (materiel.type === 'RESSOURCE' ? this.seuilParDefautRessource : this.seuilParDefautEquipement);
      
      let niveau: 'CRITIQUE' | 'FAIBLE' | 'NORMAL' = 'NORMAL';
      
      if (materiel.quantiteDisponible === 0) {
        niveau = 'CRITIQUE';
      } else if (materiel.quantiteDisponible < seuil) {
        niveau = 'FAIBLE';
      }
      
      if (niveau !== 'NORMAL') {
        this.stockAlerts.push({
          id: materiel.id,
          type: materiel.type,
          designation: materiel.nom,
          quantiteActuelle: materiel.quantiteTotale,
          quantiteDisponible: materiel.quantiteDisponible,
          seuilMinimum: seuil,
          niveau
        });
      }
    });

    // Trier par niveau de criticité
    this.stockAlerts.sort((a, b) => {
      const order = { 'CRITIQUE': 0, 'FAIBLE': 1, 'NORMAL': 2 };
      return order[a.niveau] - order[b.niveau];
    });
  }

  get filteredMateriel(): MaterielComplet[] {
    let filtered = this.materielComplet;
    
    // Filtre par type
    if (this.filterType !== 'TOUS') {
      filtered = filtered.filter(m => m.type === this.filterType);
    }
    
    // Filtre par catégorie
    if (this.filterCategorie !== 'TOUS') {
      filtered = filtered.filter(m => m.categorie === this.filterCategorie);
    }
    
    // Filtre par état
    if (this.filterEtat !== 'TOUS') {
      filtered = filtered.filter(m => m.etat === this.filterEtat);
    }
    
    // Recherche
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(m => 
        m.nom?.toLowerCase().includes(term) ||
        m.reference?.toLowerCase().includes(term) ||
        m.categorie?.toLowerCase().includes(term) ||
        m.description?.toLowerCase().includes(term)
      );
    }
    
    return filtered;
  }

  get categories(): string[] {
    const cats = new Set<string>();
    this.materielComplet.forEach(m => cats.add(m.categorie));
    return Array.from(cats).sort();
  }

  get etats(): string[] {
    const etats = new Set<string>();
    this.materielComplet.forEach(m => etats.add(m.etat));
    return Array.from(etats).sort();
  }

  getAlertCount(): number {
    return this.stockAlerts.length;
  }

  getCritiqueCount(): number {
    return this.stockAlerts.filter(a => a.niveau === 'CRITIQUE').length;
  }

  getFaibleCount(): number {
    return this.stockAlerts.filter(a => a.niveau === 'FAIBLE').length;
  }

  getTotalValeurStock(): number {
    const valeurEquipements = this.equipements.reduce((sum, e) => 
      sum + (e.valeurAchat || 0), 0);
    const valeurRessources = this.ressources.reduce((sum, r) => 
      sum + ((r.valeurAchat || 0) * (r.quantiteEnStock || 0)), 0);
    return valeurEquipements + valeurRessources;
  }

  getTotalRessources(): number {
    return this.ressources.reduce((sum, r) => sum + (r.quantiteEnStock || 0), 0);
  }

  getEquipementsDisponibles(): number {
    return this.equipements.filter(e => e.disponible !== false).length;
  }

  switchTab(tab: 'liste' | 'alertes' | 'mouvements'): void {
    this.activeTab = tab;
    if (tab === 'mouvements') {
      this.loadMouvements();
    }
  }

  loadMouvements(): void {
    // Construire l'historique des mouvements à partir des interventions
    this.mouvements = [];
    
    // Mouvements depuis les interventions (sorties)
    this.interventions.forEach(intervention => {
      if (intervention.equipementIds) {
        intervention.equipementIds.forEach(equipId => {
          const equipement = this.equipements.find(e => e.id === equipId);
          if (equipement) {
            this.mouvements.push({
              id: this.mouvements.length + 1,
              date: intervention.datePlanifiee || new Date().toISOString(),
              type: 'EQUIPEMENT',
              designation: equipement.nom,
              mouvement: 'SORTIE',
              quantite: 1,
              quantiteAvant: 1,
              quantiteApres: 0,
              raison: `Intervention #${intervention.id}`,
              interventionId: intervention.id
            });
          }
        });
      }
      
      if (intervention.ressourceIds) {
        intervention.ressourceIds.forEach(ressId => {
          const ressource = this.ressources.find(r => r.id === ressId);
          if (ressource) {
            const quantite = 1; // Par défaut 1, peut être ajusté
            this.mouvements.push({
              id: this.mouvements.length + 1,
              date: intervention.datePlanifiee || new Date().toISOString(),
              type: 'RESSOURCE',
              designation: ressource.designation,
              mouvement: 'SORTIE',
              quantite: quantite,
              quantiteAvant: ressource.quantiteEnStock + quantite,
              quantiteApres: ressource.quantiteEnStock,
              raison: `Intervention #${intervention.id}`,
              interventionId: intervention.id
            });
          }
        });
      }
    });
    
    // Trier par date décroissante
    this.mouvements.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  }

  configurerSeuil(materiel: MaterielComplet): void {
    this.selectedMaterielForSeuil = materiel;
    this.nouveauSeuil = materiel.seuilMinimum ?? 
      (materiel.type === 'RESSOURCE' ? this.seuilParDefautRessource : this.seuilParDefautEquipement);
    this.showSeuilModal = true;
  }

  sauvegarderSeuil(): void {
    if (this.selectedMaterielForSeuil && this.nouveauSeuil >= 0) {
      this.seuilsMinimum.set(this.selectedMaterielForSeuil.id, this.nouveauSeuil);
      this.buildMaterielComplet();
      this.updateAlerts();
      this.showSeuilModal = false;
      this.selectedMaterielForSeuil = null;
    }
  }

  fermerSeuilModal(): void {
    this.showSeuilModal = false;
    this.selectedMaterielForSeuil = null;
  }

  getAlertClass(niveau: string): string {
    switch (niveau) {
      case 'CRITIQUE': return 'alert-critique';
      case 'FAIBLE': return 'alert-faible';
      default: return '';
    }
  }

  getMouvementClass(mouvement: string): string {
    switch (mouvement) {
      case 'ENTREE': return 'mouvement-entree';
      case 'SORTIE': return 'mouvement-sortie';
      case 'AJUSTEMENT': return 'mouvement-ajustement';
      default: return '';
    }
  }
}

