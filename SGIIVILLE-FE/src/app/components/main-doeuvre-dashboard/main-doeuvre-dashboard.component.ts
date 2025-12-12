import { Intervention } from './../../models/intervention.model';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';

// Services
import { MainDOeuvreAgentService } from '../../services/main-doeuvre-agent.service';
import { MainDOeuvreTacheService } from '../../services/main-doeuvre-tache.service';
import { AuthService } from '../../services/auth.service';
import { InterventionService } from '../../services/intervention.service';
import { NotificationService } from '../../services/notification.service';

// Modèles
import { Tache, TerminerTacheRequest } from '../../models/tache.model';

// Interface étendue pour inclure interventionInfo
interface TacheEtendue extends Tache {
  interventionInfo?: {
    id: number;
    description: string;
    datePlanifiee: string;
    etat: string;
    technicienId?: number;
  };
}

@Component({
  selector: 'app-main-doeuvre-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './main-doeuvre-dashboard.component.html',
  styleUrls: ['./main-doeuvre-dashboard.component.css']
})
export class MainDOeuvreDashboardComponent implements OnInit, OnDestroy {
  // Données
  taches: TacheEtendue[] = [];
  tachesFiltrees: TacheEtendue[] = [];
  profil: any = null;

  // États UI
  loading = false;
  showNotifications = false;
  isSubmitting = false; // Flag pour éviter les doubles soumissions

  // Filtres
  filtreEtat: string = '';
  recherche: string = '';

  // Modals
  selectedTache: TacheEtendue | null = null;
  modalAction: 'terminer' | 'commenter' | 'reporter' | 'suspendre' | null = null;

  // Formulaires
  terminerTacheRequest: TerminerTacheRequest = {
    commentaire: '',
    tempsPasseMinutes: 0
  };

  commentaireTache: string = '';
  raisonReport: string = '';
  raisonSuspension: string = '';

  // Ajouter ces propriétés
  technicienId: number | null = null;
  mainDOeuvreNom: string = '';

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
    private router: Router,
    private notificationService: NotificationService,
    private interventionService: InterventionService
  ) {}

  ngOnInit(): void {
    console.log('🚀 Initialisation dashboard main-d\'œuvre');
    this.testConnection();
    this.loadProfil();
    this.loadMyTaches();
    this.chargerDonneesNotification();
  }

  testConnection(): void {
    console.log('🔗 Test connexion API...');
    this.mainDoeuvreAgentService.testConnection().subscribe({
      next: (response: any) => {
        console.log('✅ Test API réussi:', response);
      },
      error: (error: any) => {
        console.error('❌ Test API échoué:', error);
        alert('Erreur de connexion à l\'API. Vérifiez que le serveur est démarré.');
      }
    });
  }

  private chargerDonneesNotification(): void {
    // Charger l'ID du technicien (exemple depuis localStorage)
    this.technicienId = localStorage.getItem('currentTechnicienId')
      ? parseInt(localStorage.getItem('currentTechnicienId')!)
      : null;

    // Charger le nom de la main-d'œuvre
    if (this.profil) {
      this.mainDOeuvreNom = `${this.profil.nom} ${this.profil.prenom}`;
    }
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
        this.mainDOeuvreNom = `${profil.nom} ${profil.prenom}`;
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
        // Cast les tâches vers TacheEtendue
        this.taches = (taches as TacheEtendue[]) || [];
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
  commencerTache(tache: TacheEtendue): void {
    if (confirm(`Commencer la tâche "${tache.libelle}" ?`)) {
      const sub = this.mainDoeuvreTacheService.commencerTache(tache.id).subscribe({
        next: (tacheMaj) => {
          this.mettreAJourTache(tacheMaj as TacheEtendue);
          alert('✅ Tâche commencée avec succès');
        },
        error: (err) => {
          console.error('Erreur démarrage tâche:', err);
          const errorMsg = err.error?.error || err.error?.message || err.message || 'Erreur inconnue';
          alert('❌ Erreur: ' + errorMsg);
        }
      });
      this.subscriptions.push(sub);
    }
  }

  ouvrirModalTerminer(tache: TacheEtendue): void {
    this.selectedTache = tache;
    this.modalAction = 'terminer';
    this.terminerTacheRequest = {
      commentaire: '',
      tempsPasseMinutes: tache.tempsPasseMinutes || 0
    };
  }

  terminerTache(): void {
    if (!this.selectedTache || this.isSubmitting) return;

    this.isSubmitting = true;

    const sub = this.mainDoeuvreTacheService.terminerTache(
        this.selectedTache.id,
        this.terminerTacheRequest
    ).subscribe({
        next: (tacheMaj) => {
            this.mettreAJourTache(tacheMaj as TacheEtendue);
            this.fermerModal();
            alert('✅ Tâche terminée avec succès');

            // Afficher une notification spéciale si c'était la dernière tâche
            this.mainDoeuvreTacheService.verifierToutesTachesTerminees(tacheMaj.interventionId)
                .subscribe({
                    next: (result: any) => {
                        if (result.toutesTerminees) {
                            this.showSuccessMessage(
                                '🎉 FÉLICITATIONS !',
                                `Vous avez terminé toutes les tâches de l'intervention #${tacheMaj.interventionId}.
                                Le technicien a été notifié et va maintenant vérifier l'intervention.`
                            );
                        }
                    }
                });
        },
        error: (err) => {
            console.error('Erreur terminaison tâche:', err);
            console.error('Full error response:', JSON.stringify(err, null, 2));
            const errorMsg = err.error?.error || err.error?.message || err.message || 'Erreur inconnue';
            alert('❌ Erreur: ' + errorMsg);
        },
        complete: () => {
            this.isSubmitting = false;
        }
    });
    this.subscriptions.push(sub);
}

private showSuccessMessage(title: string, message: string): void {
    // Vous pouvez utiliser un service de toast ou une alerte stylée
    const toast = document.createElement('div');
    toast.className = 'success-toast';
    toast.innerHTML = `
        <div class="toast-header">
            <strong>${title}</strong>
        </div>
        <div class="toast-body">
            ${message}
        </div>
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 5000);
}
  ouvrirModalCommenter(tache: TacheEtendue): void {
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
        this.mettreAJourTache(tacheMaj as TacheEtendue);
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

  ouvrirModalReporter(tache: TacheEtendue): void {
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
        this.mettreAJourTache(tacheMaj as TacheEtendue);
        this.fermerModal();
        alert('✅ Tâche reportée avec succès');
      },
      error: (err) => {
        console.error('Erreur report tâche:', err);
        const errorMsg = err.error?.error || err.error?.message || err.message || 'Erreur inconnue';
        alert('❌ Erreur: ' + errorMsg);
      }
    });
    this.subscriptions.push(sub);
  }

  ouvrirModalSuspendre(tache: TacheEtendue): void {
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
        this.mettreAJourTache(tacheMaj as TacheEtendue);
        this.fermerModal();
        alert('✅ Tâche suspendue avec succès');
      },
      error: (err) => {
        console.error('Erreur suspension tâche:', err);
        const errorMsg = err.error?.error || err.error?.message || err.message || 'Erreur inconnue';
        alert('❌ Erreur: ' + errorMsg);
      }
    });
    this.subscriptions.push(sub);
  }

  reprendreTache(tache: TacheEtendue): void {
    if (confirm(`Reprendre la tâche "${tache.libelle}" ?`)) {
      const sub = this.mainDoeuvreTacheService.reprendreTache(tache.id).subscribe({
        next: (tacheMaj) => {
          this.mettreAJourTache(tacheMaj as TacheEtendue);
          alert('✅ Tâche reprise avec succès');
        },
        error: (err) => {
          console.error('Erreur vérification tâche:', err);
          const errorMsg = err.error?.error || err.error?.message || err.message || 'Erreur inconnue';
          alert('❌ Erreur: ' + errorMsg);
        }
      });
      this.subscriptions.push(sub);
    }
  }

  // ========== UTILITAIRES ==========
  private mettreAJourTache(tacheMaj: TacheEtendue): void {
    const index = this.taches.findIndex(t => t.id === tacheMaj.id);
    if (index !== -1) {
      const ancienEtat = this.taches[index].etat;
      this.taches[index] = tacheMaj;
      this.calculerStats();
      this.appliquerFiltres();

      // Envoyer notification au technicien
      this.envoyerNotificationTechnicien(tacheMaj, ancienEtat);
    }
  }

  private envoyerNotificationTechnicien(tache: TacheEtendue, ancienEtat: string): void {
    // Si technicienId n'est pas déjà chargé, essayer de le récupérer depuis l'interventionInfo
    if (!this.technicienId && tache.interventionInfo) {
      if (tache.interventionInfo.technicienId) {
        this.technicienId = tache.interventionInfo.technicienId;
      }
    }

    if (!this.technicienId) return;

    const notificationData = {
      technicienId: this.technicienId,
      tacheId: tache.id,
      libelleTache: tache.libelle,
      mainDOeuvreNom: this.mainDOeuvreNom,
      ancienEtat: ancienEtat,
      nouvelEtat: tache.etat,
      details: tache.commentaireMainDOeuvre || 'Changement d\'état effectué'
    };

    this.notificationService.notifierTechnicienChangementTache(notificationData).subscribe({
      next: () => console.log('📢 Notification envoyée au technicien'),
      error: (err: any) => console.error('❌ Erreur envoi notification:', err)
    });
  }

  private verifierEtNotifierSiToutesTachesTerminees(interventionId: number): void {
    if (!interventionId) return;

    this.interventionService.verifierToutesTachesTerminees(interventionId).subscribe({
      next: (result: any) => {
        if (result.toutesTerminees) {
          // Récupérer l'intervention pour avoir l'ID du technicien
          this.interventionService.getInterventionById(interventionId).subscribe({
            next: (intervention: Intervention) => {
              if (intervention.technicienId) {
                // Notifier le technicien pour vérification
                const message = `🔍 Toutes les tâches de l'intervention #${interventionId} sont terminées.\n` +
                  `Veuillez vérifier et terminer l'intervention.`;

                this.notificationService.notifierTechnicienVerification(
                  intervention.technicienId,
                  interventionId,
                  message
                ).subscribe({
                  next: () => console.log('📢 Technicien notifié pour vérification'),
                  error: (err) => console.error('❌ Erreur notification technicien:', err)
                });
              }
            }
          });
        }
      },
      error: (err: any) => console.error('Erreur vérification tâches:', err)
    });
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
        (t.interventionInfo &&
          t.interventionInfo.description?.toLowerCase().includes(this.recherche.toLowerCase()));
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
    switch (etat) {
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
    switch (etat) {
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
    switch (etat) {
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

  trackByTacheId(index: number, tache: TacheEtendue): number {
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
