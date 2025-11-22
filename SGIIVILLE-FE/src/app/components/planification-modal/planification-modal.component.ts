import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Demande } from '../../models/demande.model';
import { Technicien } from '../../models/technicien.model';
import { Equipement } from '../../models/equipement.model';
import { RessourceMaterielle } from '../../models/ressource.model';
import { PlanificationService, PlanificationRequest, TechnicienDisponibilite, RessourceDisponibilite } from '../../services/planification.service';
import { TechnicienListService } from '../../services/technicien-list.service';
import { EquipementService } from '../../services/equipement.service';
import { RessourceService } from '../../services/ressource.service';

@Component({
  selector: 'app-planification-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './planification-modal.component.html',
  styleUrls: ['./planification-modal.component.css']
})
export class PlanificationModalComponent implements OnInit {
  @Input() demande!: Demande;
  @Output() planified = new EventEmitter<any>();
  @Output() closed = new EventEmitter<void>();

  // Données disponibles
  techniciens: Technicien[] = [];
  equipements: Equipement[] = [];
  ressources: RessourceMaterielle[] = [];

  // Données de planification
  planification: PlanificationRequest = {
    demandeId: 0,
    datePlanifiee: '',
    techniciensIds: [],
    equipementsIds: [],
    ressourcesIds: [],
    priorite: 'MOYENNE',
    budget: 0
  };

  // Disponibilités
  techniciensDisponibles: TechnicienDisponibilite[] = [];
  ressourcesDisponibilite: RessourceDisponibilite | null = null;
  disponibiliteVerifiee = false;
  verificationEnCours = false;

  // Étapes du modal
  etapeActuelle: 'selection' | 'verification' | 'confirmation' = 'selection';

  // Filtres
  dateSelectionnee = '';
  competencesFiltre = '';

  constructor(
    public activeModal: NgbActiveModal,
    private planificationService: PlanificationService,
    private technicienService: TechnicienListService,
    private equipementService: EquipementService,
    private ressourceService: RessourceService
  ) {}

  ngOnInit(): void {
    this.planification.demandeId = this.demande.id;
    this.planification.priorite = this.getPrioriteFromDemande(this.demande.priority);
    this.planification.budget = this.calculerBudgetEstime();

    // Initialiser la date (aujourd'hui + 2 jours)
    const date = new Date();
    date.setDate(date.getDate() + 2);
    this.dateSelectionnee = date.toISOString().split('T')[0];
    this.planification.datePlanifiee = this.dateSelectionnee;

    this.loadDonneesPlanification();
  }

  loadDonneesPlanification(): void {
    // Charger techniciens
    this.technicienService.getAllTechniciens().subscribe(techs => {
      this.techniciens = techs;
    });

    // Charger équipements
    this.equipementService.getAllEquipements().subscribe(eqs => {
      this.equipements = eqs.filter(e => e.etat === 'FONCTIONNEL');
    });

    // Charger ressources
    this.ressourceService.getAll().subscribe(ress => {
      this.ressources = ress.filter(r => r.quantiteEnStock > 0);
    });
  }

  // Vérifier les disponibilités
  verifierDisponibilites(): void {
    if (!this.dateSelectionnee) {
      alert('Veuillez sélectionner une date');
      return;
    }

    this.verificationEnCours = true;
    this.planification.datePlanifiee = this.dateSelectionnee;

    // Vérifier techniciens disponibles
    this.planificationService.getTechniciensDisponibles(
      this.dateSelectionnee,
      this.competencesFiltre
    ).subscribe(techDispos => {
      this.techniciensDisponibles = techDispos;

      // Vérifier ressources disponibles
      this.planificationService.checkRessourcesDisponibles(
        this.dateSelectionnee,
        this.planification.equipementsIds,
        this.planification.ressourcesIds
      ).subscribe(ressDispos => {
        this.ressourcesDisponibilite = ressDispos;
        this.disponibiliteVerifiee = true;
        this.verificationEnCours = false;
        this.etapeActuelle = 'verification';
      });
    });
  }

  // Sélectionner/désélectionner un technicien
  toggleTechnicien(technicienId: number): void {
    const index = this.planification.techniciensIds.indexOf(technicienId);
    if (index > -1) {
      this.planification.techniciensIds.splice(index, 1);
    } else {
      this.planification.techniciensIds.push(technicienId);
    }
  }

  // Vérifier si un technicien est disponible
  isTechnicienDisponible(technicienId: number): boolean {
    const techDispo = this.techniciensDisponibles.find(t => t.technicienId === technicienId);
    return techDispo ? techDispo.disponible : false;
  }

  // Obtenir les créneaux disponibles pour un technicien
  getCreneauxTechnicien(technicienId: number): string {
    const techDispo = this.techniciensDisponibles.find(t => t.technicienId === technicienId);
    if (!techDispo || !techDispo.disponible) return '❌ Indisponible';

    const creneaux = techDispo.disponibilites.flatMap(d => d.creneaux)
      .filter(c => c.disponible)
      .map(c => `${c.debut}-${c.fin}`)
      .join(', ');

    return creneaux || '✅ Disponible toute la journée';
  }

  // Vérifier si une ressource est disponible
  isRessourceDisponible(ressourceId: number): boolean {
    if (!this.ressourcesDisponibilite) return true;

    const ressource = this.ressourcesDisponibilite.ressourcesDisponibles.find(r => r.ressourceId === ressourceId);
    return ressource ? ressource.disponible : true;
  }

  // Confirmer la planification
  confirmerPlanification(): void {
    if (this.planification.techniciensIds.length === 0) {
      alert('Veuillez sélectionner au moins un technicien');
      return;
    }

    this.etapeActuelle = 'confirmation';
  }

  // Finaliser la planification
  finaliserPlanification(): void {
    this.planificationService.planifierIntervention(this.planification).subscribe({
      next: (intervention) => {
        this.planified.emit(intervention);
        this.activeModal.close();
      },
      error: (error) => {
        console.error('Erreur planification:', error);
        alert('❌ Erreur lors de la planification: ' + (error.error?.message || error.message));
      }
    });
  }

  // Retour à l'étape précédente
  retourEtapePrecedente(): void {
    switch (this.etapeActuelle) {
      case 'verification':
        this.etapeActuelle = 'selection';
        break;
      case 'confirmation':
        this.etapeActuelle = 'verification';
        break;
    }
  }

  // Fermer le modal
  close(): void {
    this.closed.emit();
    this.activeModal.dismiss();
  }

  // Helper methods
  private getPrioriteFromDemande(priority: string | undefined): 'HAUTE' | 'MOYENNE' | 'BASSE' {
    switch (priority?.toUpperCase()) {
      case 'HIGH':
      case 'URGENT':
      case 'HAUTE':
        return 'HAUTE';
      case 'LOW':
      case 'BAS':
      case 'BASSE':
        return 'BASSE';
      default:
        return 'MOYENNE';
    }
  }

  private calculerBudgetEstime(): number {
    switch (this.planification.priorite) {
      case 'HAUTE': return 1500;
      case 'MOYENNE': return 800;
      case 'BASSE': return 400;
      default: return 500;
    }
  }

  // Vérifier si on peut passer à l'étape suivante
  peutPasserVerification(): boolean {
    return this.dateSelectionnee !== '' &&
           this.planification.techniciensIds.length > 0;
  }

  // Vérifier si toutes les ressources sont disponibles
  get tousRessourcesDisponibles(): boolean {
    return this.ressourcesDisponibilite?.tousDisponibles ?? true;
  }

  // Obtenir le nombre de techniciens disponibles
  get nombreTechniciensDisponibles(): number {
    return this.techniciensDisponibles.filter(t => t.disponible).length;
  }

  // Méthodes pour la gestion des équipements et ressources
  toggleEquipementSelection(equipementId: number): void {
    const index = this.planification.equipementsIds.indexOf(equipementId);
    if (index > -1) {
      this.planification.equipementsIds.splice(index, 1);
    } else {
      this.planification.equipementsIds.push(equipementId);
    }
  }

  toggleRessourceSelection(ressourceId: number): void {
    const index = this.planification.ressourcesIds.indexOf(ressourceId);
    if (index > -1) {
      this.planification.ressourcesIds.splice(index, 1);
    } else {
      this.planification.ressourcesIds.push(ressourceId);
    }
  }

  getDateMin(): string {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  }

  getTechName(technicienId: number): string {
    const tech = this.techniciens.find(t => t.id === technicienId);
    return tech ? tech.nom : 'Technicien inconnu';
  }
}
