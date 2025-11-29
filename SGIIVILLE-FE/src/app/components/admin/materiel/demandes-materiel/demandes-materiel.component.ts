import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemandeAjoutService, DemandeAjout } from '../../../../services/demande-ajout.service';
import { AuthService } from '../../../../services/auth.service';
import { UtilisateurService } from '../../../../services/utilisateur.service';
import { InterventionService } from '../../../../services/intervention.service';
import { Intervention } from '../../../../models/intervention.model';

interface ChefInfo {
  id: number;
  nom: string;
  prenom: string;
  email?: string;
  role?: string;
  service?: string;
}

interface InterventionInfo {
  id: number;
  description?: string;
  typeIntervention?: string;
  priorite?: string;
  etat?: string;
  datePlanifiee?: string;
  localisation?: any;
  technicienId?: number;
}

@Component({
  selector: 'app-demandes-materiel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './demandes-materiel.component.html',
  styleUrls: ['./demandes-materiel.component.css']
})
export class DemandesMaterielComponent implements OnInit {
  demandesEnAttente: DemandeAjout[] = [];
  toutesLesDemandes: DemandeAjout[] = [];
  demandesFiltrees: DemandeAjout[] = [];
  
  activeTab: 'en-attente' | 'historique' = 'en-attente';
  filterEtat: 'TOUS' | 'EN_ATTENTE_ADMIN' | 'ACCEPTEE' | 'REFUSEE' = 'TOUS';
  searchTerm: string = '';
  
  loading = false;
  selectedDemande: DemandeAjout | null = null;
  showModal = false;
  
  // Pour le refus
  motifRefus: string = '';
  showRefusModal = false;
  demandeARefuser: DemandeAjout | null = null;
  
  // Informations complémentaires
  chefsInfo: Map<number, ChefInfo> = new Map();
  interventionsInfo: Map<number, InterventionInfo> = new Map();
  interventions: Intervention[] = [];
  
  currentAdminId: number | null = null;
  
  // Historique des demandes du demandeur
  demandesHistorique: Map<number, DemandeAjout[]> = new Map();

  constructor(
    private demandeAjoutService: DemandeAjoutService,
    private authService: AuthService,
    private utilisateurService: UtilisateurService,
    private interventionService: InterventionService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadDemandes();
  }

  loadCurrentUser(): void {
    const user = this.authService.currentUserValue;
    if (user && user.id) {
      this.currentAdminId = user.id;
    }
  }

  loadDemandes(): void {
    this.loading = true;
    
    // Charger les interventions
    this.interventionService.getAllInterventions().subscribe({
      next: (interventions) => {
        this.interventions = interventions || [];
        this.buildInterventionsInfo();
        this.loadDemandesData();
      },
      error: (err) => {
        console.error('Erreur chargement interventions:', err);
        this.interventions = [];
        this.loadDemandesData();
      }
    });
  }

  loadDemandesData(): void {
    // Charger les demandes en attente
    this.demandeAjoutService.getDemandesEnAttente().subscribe({
      next: (data) => {
        this.demandesEnAttente = data || [];
        this.loading = false;
        this.applyFilters();
      },
      error: (err) => {
        console.error('Erreur chargement demandes en attente:', err);
        this.demandesEnAttente = [];
        this.loading = false;
      }
    });

    // Charger toutes les demandes pour l'historique
    this.demandeAjoutService.getAllDemandes().subscribe({
      next: (data) => {
        this.toutesLesDemandes = data || [];
        this.loadChefsInfo();
        this.buildHistoriqueDemandeur();
        this.applyFilters();
      },
      error: (err) => {
        console.error('Erreur chargement toutes les demandes:', err);
        this.toutesLesDemandes = [];
      }
    });
  }

  buildInterventionsInfo(): void {
    this.interventions.forEach(intervention => {
      this.interventionsInfo.set(intervention.id, {
        id: intervention.id,
        description: intervention.description,
        typeIntervention: intervention.typeIntervention,
        priorite: intervention.priorite,
        etat: intervention.etat,
        datePlanifiee: intervention.datePlanifiee,
        localisation: intervention.localisation,
        technicienId: intervention.technicienId
      });
    });
  }

  buildHistoriqueDemandeur(): void {
    this.demandesHistorique.clear();
    this.toutesLesDemandes.forEach(demande => {
      if (!this.demandesHistorique.has(demande.chefId)) {
        this.demandesHistorique.set(demande.chefId, []);
      }
      this.demandesHistorique.get(demande.chefId)!.push(demande);
    });
  }

  loadChefsInfo(): void {
    // Charger les informations des chefs pour toutes les demandes
    const chefIds = new Set<number>();
    this.toutesLesDemandes.forEach(d => {
      if (d.chefId) {
        chefIds.add(d.chefId);
      }
    });

    chefIds.forEach(chefId => {
      this.utilisateurService.getUserById(chefId).subscribe({
        next: (user) => {
          if (user) {
            this.chefsInfo.set(chefId, {
              id: user.id,
              nom: user.nom || '',
              prenom: user.prenom || '',
              email: user.email
            });
          }
        },
        error: (err) => {
          console.error(`Erreur chargement chef ${chefId}:`, err);
        }
      });
    });
  }

  getChefInfo(chefId: number): ChefInfo | null {
    return this.chefsInfo.get(chefId) || null;
  }

  getChefName(chefId: number): string {
    const chef = this.getChefInfo(chefId);
    if (chef) {
      return `${chef.prenom} ${chef.nom}`;
    }
    return `Chef #${chefId}`;
  }

  getInterventionInfo(interventionId: number | undefined): InterventionInfo | null {
    if (!interventionId) return null;
    return this.interventionsInfo.get(interventionId) || null;
  }

  getHistoriqueDemandeur(chefId: number): DemandeAjout[] {
    return this.demandesHistorique.get(chefId) || [];
  }

  getPrioriteLabel(priorite: string | undefined): string {
    if (!priorite) return 'NORMALE';
    const labels: { [key: string]: string } = {
      'URGENTE': 'Urgente',
      'CRITIQUE': 'Critique',
      'PLANIFIEE': 'Planifiée',
      'NORMALE': 'Normale'
    };
    return labels[priorite] || priorite;
  }

  getPrioriteClass(priorite: string | undefined): string {
    if (!priorite) return 'priorite-normale';
    const classes: { [key: string]: string } = {
      'URGENTE': 'priorite-urgente',
      'CRITIQUE': 'priorite-critique',
      'PLANIFIEE': 'priorite-planifiee',
      'NORMALE': 'priorite-normale'
    };
    return classes[priorite] || 'priorite-normale';
  }

  applyFilters(): void {
    let filtered = this.activeTab === 'en-attente' 
      ? [...this.demandesEnAttente] 
      : [...this.toutesLesDemandes];

    // Filtre par état
    if (this.filterEtat !== 'TOUS') {
      filtered = filtered.filter(d => d.etat === this.filterEtat);
    }

    // Filtre par recherche
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(d => 
        d.designation?.toLowerCase().includes(term) ||
        d.justification?.toLowerCase().includes(term) ||
        this.getChefName(d.chefId).toLowerCase().includes(term)
      );
    }

    this.demandesFiltrees = filtered;
  }

  switchTab(tab: 'en-attente' | 'historique'): void {
    this.activeTab = tab;
    this.applyFilters();
  }

  accepterDemande(demande: DemandeAjout): void {
    if (!this.currentAdminId) {
      alert('Erreur: Admin ID non trouvé');
      return;
    }

    if (!confirm(`Voulez-vous accepter la demande "${demande.designation}" ?`)) {
      return;
    }

    this.loading = true;
    this.demandeAjoutService.accepterDemande(demande.id, {
      adminId: this.currentAdminId
    }).subscribe({
      next: (updatedDemande) => {
        alert('✅ Demande acceptée avec succès');
        this.loadDemandes();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur acceptation demande:', err);
        alert('❌ Erreur lors de l\'acceptation de la demande');
        this.loading = false;
      }
    });
  }

  ouvrirRefusModal(demande: DemandeAjout): void {
    this.demandeARefuser = demande;
    this.motifRefus = '';
    this.showRefusModal = true;
  }

  fermerRefusModal(): void {
    this.showRefusModal = false;
    this.demandeARefuser = null;
    this.motifRefus = '';
  }

  refuserDemande(): void {
    if (!this.demandeARefuser || !this.currentAdminId) {
      return;
    }

    if (!this.motifRefus.trim()) {
      alert('⚠️ Veuillez indiquer un motif de refus');
      return;
    }

    this.loading = true;
    this.demandeAjoutService.refuserDemande(this.demandeARefuser.id, {
      adminId: this.currentAdminId,
      motifRefus: this.motifRefus
    }).subscribe({
      next: (updatedDemande) => {
        alert('✅ Demande refusée');
        this.fermerRefusModal();
        this.loadDemandes();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur refus demande:', err);
        alert('❌ Erreur lors du refus de la demande');
        this.loading = false;
      }
    });
  }

  voirDetails(demande: DemandeAjout): void {
    this.selectedDemande = demande;
    this.showModal = true;
  }

  fermerModal(): void {
    this.showModal = false;
    this.selectedDemande = null;
  }

  getEtatClass(etat: string): string {
    switch (etat) {
      case 'EN_ATTENTE_ADMIN': return 'etat-attente';
      case 'ACCEPTEE': return 'etat-acceptee';
      case 'REFUSEE': return 'etat-refusee';
      default: return '';
    }
  }

  getEtatLabel(etat: string): string {
    switch (etat) {
      case 'EN_ATTENTE_ADMIN': return 'En attente';
      case 'ACCEPTEE': return 'Acceptée';
      case 'REFUSEE': return 'Refusée';
      default: return etat;
    }
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'EQUIPEMENT': return 'Équipement';
      case 'RESSOURCE': return 'Ressource';
      default: return type;
    }
  }

  getDemandesEnAttenteCount(): number {
    return this.demandesEnAttente.length;
  }

  getAccepteesCount(): number {
    return this.toutesLesDemandes.filter(d => d.etat === 'ACCEPTEE').length;
  }

  getRefuseesCount(): number {
    return this.toutesLesDemandes.filter(d => d.etat === 'REFUSEE').length;
  }

  formatDate(date: string | undefined): string {
    if (!date) return '-';
    try {
      return new Date(date).toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return date;
    }
  }
}

