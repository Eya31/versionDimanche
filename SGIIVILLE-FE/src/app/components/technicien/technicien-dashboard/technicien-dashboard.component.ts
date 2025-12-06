import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { TechnicienService } from '../../../services/technicien.service';
import { AuthService } from '../../../services/auth.service';
import { Intervention } from '../../../models/intervention.model';
import { MiniCalendarComponent } from '../../../components/mini-calendar/mini-calendar.component';
import { InterventionService } from '../../../services/intervention.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-technicien-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MiniCalendarComponent],
  templateUrl: './technicien-dashboard.component.html',
  styleUrls: ['./technicien-dashboard.component.css']
})
export class TechnicienDashboardComponent implements OnInit {
  interventions: Intervention[] = [];
  interventionsFiltrees: Intervention[] = [];
  loading = false;

  // Vue
  viewMode: 'list' | 'calendar' = 'list';
  selectedCalendarDate: Date | null = null;

  // Filtres
  filtreEtat: string = '';
  filtrePriorite: string = '';
  recherche: string = '';

  // Statistiques
  stats = {
    enAttente: 0,
    enCours: 0,
    terminees: 0,
    suspendues: 0
  };

  constructor(
    private technicienService: TechnicienService,
    private authService: AuthService,
    private router: Router,
    private interventionService: InterventionService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadMyInterventions();
  }

  loadMyInterventions(): void {
    this.loading = true;
    const filters: any = {};
    if (this.filtreEtat) filters.etat = this.filtreEtat;
    if (this.filtrePriorite) filters.priorite = this.filtrePriorite;

    this.technicienService.getMyInterventions(filters).subscribe({
      next: (data: Intervention[]) => {
        this.interventions = data || [];
        this.calculerStats();
        this.appliquerFiltres();
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Erreur chargement interventions:', err);
        this.loading = false;
        alert('Erreur lors du chargement des interventions. Vérifiez votre connexion.');
      }
    });
  }

  calculerStats(): void {
    this.stats = {
      enAttente: this.interventions.filter(i => i.etat === 'EN_ATTENTE').length,
      enCours: this.interventions.filter(i => i.etat === 'EN_COURS').length,
      terminees: this.interventions.filter(i => i.etat === 'TERMINEE').length,
      suspendues: this.interventions.filter(i => i.etat === 'SUSPENDUE').length
    };
  }

  appliquerFiltres(): void {
    this.interventionsFiltrees = this.interventions.filter(i => {
      const matchRecherche = !this.recherche ||
        i.description?.toLowerCase().includes(this.recherche.toLowerCase()) ||
        i.id.toString().includes(this.recherche) ||
        i.typeIntervention?.toLowerCase().includes(this.recherche.toLowerCase());
      return matchRecherche;
    });
  }

  confirmerReception(id: number): void {
    if (confirm('Confirmer la réception de cette intervention ?')) {
      this.technicienService.confirmerIntervention(id).subscribe({
        next: () => {
          alert('✅ Intervention confirmée avec succès');
          this.loadMyInterventions();
        },
        error: (err: any) => {
          console.error('Erreur confirmation:', err);
          alert('❌ Erreur lors de la confirmation: ' + (err.error?.message || err.message || 'Erreur inconnue'));
        }
      });
    }
  }

  voirDetails(id: number): void {
    this.router.navigate(['/technicien/intervention', id]);
  }

  allerAuProfil(): void {
    this.router.navigate(['/technicien/profil']);
  }

  actualiser(): void {
    this.loadMyInterventions();
  }

  trackByInterventionId(index: number, intervention: Intervention): number {
    return intervention.id;
  }

  toggleView(): void {
    this.viewMode = this.viewMode === 'list' ? 'calendar' : 'list';
  }

  onDateSelected(date: Date): void {
    this.selectedCalendarDate = date;
    // Filtrer les interventions pour la date sélectionnée
    const dateStr = date.toISOString().split('T')[0];
    this.interventionsFiltrees = this.interventions.filter(i => {
      if (!i.datePlanifiee) return false;
      const interventionDate = new Date(i.datePlanifiee).toISOString().split('T')[0];
      return interventionDate === dateStr;
    });
  }

  onInterventionClicked(intervention: Intervention): void {
    this.voirDetails(intervention.id);
  }

  getBadgeClass(etat: string): string {
    switch(etat) {
      case 'TERMINEE': return 'badge-success';
      case 'EN_COURS': return 'badge-info';
      case 'SUSPENDUE': return 'badge-warning';
      case 'EN_ATTENTE': return 'badge-warning';
      case 'REPORTEE': return 'badge-secondary';
      default: return 'badge-secondary';
    }
  }

  getEtatLabel(etat: string): string {
    switch(etat) {
      case 'EN_ATTENTE': return 'En Attente';
      case 'EN_COURS': return 'En Cours';
      case 'SUSPENDUE': return 'Suspendue';
      case 'TERMINEE': return 'Terminée';
      case 'REPORTEE': return 'Reportée';
      default: return etat;
    }
  }

  getPrioriteClass(priorite: string): string {
    switch(priorite?.toUpperCase()) {
      case 'URGENTE': return 'urgente';
      case 'CRITIQUE': return 'urgente';
      case 'HAUTE': return 'haute';
      case 'MOYENNE': return 'moyenne';
      case 'PLANIFIEE': return 'moyenne';
      case 'BASSE': return 'basse';
      default: return 'moyenne';
    }
  }

  getPrioriteLabel(priorite: string): string {
    switch(priorite?.toUpperCase()) {
      case 'URGENTE': return '🔴 Urgente';
      case 'CRITIQUE': return '🔴 Critique';
      case 'HAUTE': return '🟠 Haute';
      case 'MOYENNE': return '🟡 Moyenne';
      case 'PLANIFIEE': return '🟡 Planifiée';
      case 'BASSE': return '⚪ Basse';
      default: return '🟡 Normale';
    }
  }

  getPrioriteColor(priorite: string): string {
    switch(priorite?.toUpperCase()) {
      case 'URGENTE':
      case 'CRITIQUE':
        return '#F44336';
      case 'HAUTE':
        return '#FF9800';
      case 'MOYENNE':
      case 'PLANIFIEE':
        return '#2196F3';
      case 'BASSE':
        return '#9E9E9E';
      default:
        return '#2196F3';
    }
  }

  formatTemps(minutes: number): string {
    if (!minutes) return '0 min';
    const heures = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (heures > 0) {
      return `${heures}h ${mins}min`;
    }
    return `${mins} min`;
  }
// technicien-dashboard.component.ts
// Ajouter ces méthodes

// technicien-dashboard.component.ts
// AJOUTER CES MÉTHODES

verifierEtTerminerIntervention(intervention: Intervention): void {
  // 1. Vérifier d'abord si toutes les tâches sont terminées
  this.interventionService.verifierToutesTachesTerminees(intervention.id).subscribe({
    next: (result: any) => {
      if (result.toutesTerminees) {
        // 2. Confirmation du technicien
        if (confirm('Toutes les tâches sont terminées. Voulez-vous vérifier et terminer l\'intervention ?')) {
          this.terminerIntervention(intervention);
        }
      } else {
        alert('❌ Toutes les tâches ne sont pas encore terminées !');
      }
    },
    error: (err: any) => {
      console.error('Erreur vérification tâches:', err);
      alert('Erreur lors de la vérification des tâches');
    }
  });
}

private terminerIntervention(intervention: Intervention): void {
  this.interventionService.verifierIntervention(intervention.id).subscribe({
    next: (interventionMaj: Intervention) => {
      // Notifier le chef
      this.notifierChefInterventionTerminee(interventionMaj);
      alert('✅ Intervention vérifiée et terminée ! Le chef a été notifié.');
      this.loadMyInterventions();
    },
    error: (err: any) => {
      console.error('Erreur terminaison intervention:', err);
      alert('❌ Erreur lors de la terminaison');
    }
  });
}

private notifierChefInterventionTerminee(intervention: Intervention): void {
  const chefId = intervention.chefServiceId; // Récupérer l'ID du chef depuis l'intervention

  if (!chefId) {
    console.error('ID du chef non disponible');
    return;
  }

  const message = `🏁 Intervention #${intervention.id} terminée\n` +
                 `Type: ${intervention.typeIntervention || 'Non spécifié'}\n` +
                 `Technicien: #${intervention.technicienId}\n` +
                 `Date: ${new Date().toLocaleDateString('fr-FR')}`;

  this.notificationService.notifierChefInterventionTerminee(chefId, intervention.id, message).subscribe({
    next: (response: any) => {
      console.log('📨 Notification envoyée au chef:', response);
    },
    error: (error: any) => {
      console.error('❌ Erreur envoi notification chef:', error);
    }
  });
}
}
