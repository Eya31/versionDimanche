import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';

// Services
import { MainDOeuvreAgentService } from '../../services/main-doeuvre-agent.service';
import { MainDOeuvreTacheService } from '../../services/main-doeuvre-tache.service';
import { AuthService } from '../../services/auth.service';

// Modèles
import { Tache, TerminerTacheRequest, ChangerEtatTacheRequest } from '../../models/tache.model';
import { MainDOeuvre } from '../../models/main-doeuvre.model';

@Component({
  selector: 'app-main-doeuvre-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './main-doeuvre-dashboard.component.html',
  styleUrls: ['./main-doeuvre-dashboard.component.css']
})
export class MainDOeuvreDashboardComponent implements OnInit, OnDestroy {
  // Données
  taches: Tache[] = [];
  tachesFiltrees: Tache[] = [];
  profil: MainDOeuvre | null = null;

  // États UI
  loading = false;
  showNotifications = false;

  // Filtres
  filtreEtat: string = '';
  recherche: string = '';

  // Modals
  selectedTache: Tache | null = null;
  modalAction: 'terminer' | 'commenter' | 'reporter' | 'suspendre' | null = null;

  // Formulaires
  terminerTacheRequest: TerminerTacheRequest = {
    commentaire: '',
    tempsPasseMinutes: 0
  };

  commentaireTache: string = '';
  raisonReport: string = '';
  raisonSuspension: string = '';

  // Statistiques
  stats = {
    aFaire: 0,
    enCours: 0,
    terminees: 0,
    verifiees: 0,
    suspendues: 0
  };

  private subscriptions: Subscription[] = [];

  constructor(
    private mainDoeuvreAgentService: MainDOeuvreAgentService,
    private mainDoeuvreTacheService: MainDOeuvreTacheService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProfil();
    this.loadMyTaches();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  // ========== CHARGEMENT DES DONNÉES ==========
  loadProfil(): void {
    this.loading = true;
    const sub = this.mainDoeuvreAgentService.getProfil().subscribe({
      next: (profil) => {
        this.profil = profil;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement profil:', err);
        this.loading = false;
      }
    });
    this.subscriptions.push(sub);
  }

  loadMyTaches(): void {
    this.loading = true;
    const filters: any = {};
    if (this.filtreEtat) filters.etat = this.filtreEtat;

    const sub = this.mainDoeuvreTacheService.getMyTaches(filters).subscribe({
      next: (taches) => {
        this.taches = taches || [];
        this.calculerStats();
        this.appliquerFiltres();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement tâches:', err);
        this.loading = false;
        alert('Erreur lors du chargement des tâches. Vérifiez votre connexion.');
      }
    });
    this.subscriptions.push(sub);
  }

  // ========== GESTION DES ÉTATS ==========
  commencerTache(tache: Tache): void {
    if (confirm(`Commencer la tâche "${tache.libelle}" ?`)) {
      const sub = this.mainDoeuvreTacheService.commencerTache(tache.id).subscribe({
        next: (tacheMaj) => {
          this.mettreAJourTache(tacheMaj);
          alert('✅ Tâche commencée avec succès');
        },
        error: (err) => {
          console.error('Erreur démarrage tâche:', err);
          alert('❌ Erreur: ' + (err.error?.message || err.message));
        }
      });
      this.subscriptions.push(sub);
    }
  }

  ouvrirModalTerminer(tache: Tache): void {
    this.selectedTache = tache;
    this.modalAction = 'terminer';
    this.terminerTacheRequest = {
      commentaire: '',
      tempsPasseMinutes: tache.tempsPasseMinutes || 0
    };
  }

  terminerTache(): void {
    if (!this.selectedTache) return;

    const sub = this.mainDoeuvreTacheService.terminerTache(
      this.selectedTache.id,
      this.terminerTacheRequest
    ).subscribe({
      next: (tacheMaj) => {
        this.mettreAJourTache(tacheMaj);
        this.fermerModal();
        alert('✅ Tâche terminée avec succès');
      },
      error: (err) => {
        console.error('Erreur terminaison tâche:', err);
        alert('❌ Erreur: ' + (err.error?.message || err.message));
      }
    });
    this.subscriptions.push(sub);
  }

  ouvrirModalCommenter(tache: Tache): void {
    this.selectedTache = tache;
    this.modalAction = 'commenter';
    this.commentaireTache = tache.commentaireMainDOeuvre || '';
  }

  ajouterCommentaire(): void {
    if (!this.selectedTache || !this.commentaireTache.trim()) return;

    const sub = this.mainDoeuvreTacheService.ajouterCommentaire(
      this.selectedTache.id,
      this.commentaireTache
    ).subscribe({
      next: (tacheMaj) => {
        this.mettreAJourTache(tacheMaj);
        this.fermerModal();
        alert('✅ Commentaire ajouté avec succès');
      },
      error: (err) => {
        console.error('Erreur ajout commentaire:', err);
        alert('❌ Erreur: ' + (err.error?.message || err.message));
      }
    });
    this.subscriptions.push(sub);
  }

  ouvrirModalReporter(tache: Tache): void {
    this.selectedTache = tache;
    this.modalAction = 'reporter';
    this.raisonReport = '';
  }

  reporterTache(): void {
    if (!this.selectedTache || !this.raisonReport.trim()) {
      alert('Veuillez saisir une raison pour le report');
      return;
    }

    const sub = this.mainDoeuvreTacheService.reporterTache(
      this.selectedTache.id,
      this.raisonReport
    ).subscribe({
      next: (tacheMaj) => {
        this.mettreAJourTache(tacheMaj);
        this.fermerModal();
        alert('✅ Tâche reportée avec succès');
      },
      error: (err) => {
        console.error('Erreur report tâche:', err);
        alert('❌ Erreur: ' + (err.error?.message || err.message));
      }
    });
    this.subscriptions.push(sub);
  }

  ouvrirModalSuspendre(tache: Tache): void {
    this.selectedTache = tache;
    this.modalAction = 'suspendre';
    this.raisonSuspension = '';
  }

  suspendreTache(): void {
    if (!this.selectedTache || !this.raisonSuspension.trim()) {
      alert('Veuillez saisir une raison pour la suspension');
      return;
    }

    const sub = this.mainDoeuvreTacheService.suspendreTache(
      this.selectedTache.id,
      this.raisonSuspension
    ).subscribe({
      next: (tacheMaj) => {
        this.mettreAJourTache(tacheMaj);
        this.fermerModal();
        alert('✅ Tâche suspendue avec succès');
      },
      error: (err) => {
        console.error('Erreur suspension tâche:', err);
        alert('❌ Erreur: ' + (err.error?.message || err.message));
      }
    });
    this.subscriptions.push(sub);
  }

  reprendreTache(tache: Tache): void {
    if (confirm(`Reprendre la tâche "${tache.libelle}" ?`)) {
      const sub = this.mainDoeuvreTacheService.reprendreTache(tache.id).subscribe({
        next: (tacheMaj) => {
          this.mettreAJourTache(tacheMaj);
          alert('✅ Tâche reprise avec succès');
        },
        error: (err) => {
          console.error('Erreur reprise tâche:', err);
          alert('❌ Erreur: ' + (err.error?.message || err.message));
        }
      });
      this.subscriptions.push(sub);
    }
  }

  // ========== UTILITAIRES ==========
  private mettreAJourTache(tacheMaj: Tache): void {
    const index = this.taches.findIndex(t => t.id === tacheMaj.id);
    if (index !== -1) {
      this.taches[index] = tacheMaj;
      this.calculerStats();
      this.appliquerFiltres();
    }
  }

  calculerStats(): void {
    this.stats = {
      aFaire: this.taches.filter(t => t.etat === 'A_FAIRE').length,
      enCours: this.taches.filter(t => t.etat === 'EN_COURS').length,
      terminees: this.taches.filter(t => t.etat === 'TERMINEE').length,
      verifiees: this.taches.filter(t => t.etat === 'VERIFIEE').length,
      suspendues: this.taches.filter(t => t.etat === 'SUSPENDUE').length
    };
  }

  appliquerFiltres(): void {
    this.tachesFiltrees = this.taches.filter(t => {
      const matchRecherche = !this.recherche ||
        t.libelle.toLowerCase().includes(this.recherche.toLowerCase()) ||
        t.description?.toLowerCase().includes(this.recherche.toLowerCase()) ||
        t.intervention?.description?.toLowerCase().includes(this.recherche.toLowerCase());
      return matchRecherche;
    });
  }

  fermerModal(): void {
    this.selectedTache = null;
    this.modalAction = null;
    this.terminerTacheRequest = { commentaire: '', tempsPasseMinutes: 0 };
    this.commentaireTache = '';
    this.raisonReport = '';
    this.raisonSuspension = '';
  }

  // ========== UI HELPERS ==========
  getEtatTacheLabel(etat: string): string {
    switch(etat) {
      case 'A_FAIRE': return '⏳ À Faire';
      case 'EN_COURS': return '🔧 En Cours';
      case 'TERMINEE': return '✅ Terminée';
      case 'VERIFIEE': return '✔️ Vérifiée';
      case 'SUSPENDUE': return '⏸️ Suspendue';
      case 'REPORTEE': return '📅 Reportée';
      default: return etat;
    }
  }

  getEtatTacheClass(etat: string): string {
    switch(etat) {
      case 'A_FAIRE': return 'etat-a-faire';
      case 'EN_COURS': return 'etat-en-cours';
      case 'TERMINEE': return 'etat-terminee';
      case 'VERIFIEE': return 'etat-verifiee';
      case 'SUSPENDUE': return 'etat-suspendue';
      case 'REPORTEE': return 'etat-reportee';
      default: return 'etat-inconnu';
    }
  }

  getEtatProgress(etat: string): number {
    switch(etat) {
      case 'A_FAIRE': return 25;
      case 'EN_COURS': return 50;
      case 'TERMINEE': return 75;
      case 'VERIFIEE': return 100;
      case 'SUSPENDUE': return 30;
      case 'REPORTEE': return 10;
      default: return 0;
    }
  }

  isEtatCompleted(etat: string, etatCible: string): boolean {
    const ordreEtats = ['A_FAIRE', 'EN_COURS', 'TERMINEE', 'VERIFIEE'];
    const indexEtat = ordreEtats.indexOf(etat);
    const indexCible = ordreEtats.indexOf(etatCible);
    return indexEtat >= indexCible;
  }

  isEtatActive(etat: string, etatCible: string): boolean {
    return etat === etatCible;
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

  formatDate(dateStr: string): string {
    if (!dateStr) return 'Non définie';
    return new Date(dateStr).toLocaleDateString('fr-FR');
  }

  trackByTacheId(index: number, tache: Tache): number {
    return tache.id;
  }

  // ========== NAVIGATION ==========
  voirDetailsIntervention(interventionId: number): void {
    this.router.navigate(['/main-doeuvre/intervention', interventionId]);
  }

  allerAuProfil(): void {
    this.router.navigate(['/main-doeuvre/profil']);
  }

  deconnexion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  toggleNotifications(event: Event): void {
    event.stopPropagation();
    this.showNotifications = !this.showNotifications;
  }
}
