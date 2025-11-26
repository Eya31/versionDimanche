import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MainDOeuvreAgentService, StatistiquesMainDOeuvre } from '../../services/main-doeuvre-agent.service';
import { MainDOeuvreTacheService } from '../../services/main-doeuvre-tache.service';
import { NotificationService, Notification } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { Intervention } from '../../models/intervention.model';
import { MainDOeuvre } from '../../models/main-doeuvre.model';
import { Tache, TerminerTacheRequest } from '../../models/tache.model';
import { normalizeText } from '../../utils/string.utils';
import { Subscription, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-main-doeuvre-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, DatePipe],
  templateUrl: './main-doeuvre-dashboard.component.html',
  styleUrls: ['./main-doeuvre-dashboard.component.css']
})
export class MainDOeuvreDashboardComponent implements OnInit, OnDestroy {
  profil: MainDOeuvre | null = null;
  interventions: Intervention[] = [];
  interventionsFiltrees: Intervention[] = [];
  statistiques: StatistiquesMainDOeuvre | null = null;
  loading = false;

  // Filtres
  filtreEtat: string = '';
  recherche: string = '';

  // Stats calculées
  stats = {
    enAttente: 0,
    enCours: 0,
    terminees: 0,
    suspendues: 0
  };

  // Gestion des tâches
  tachesParIntervention: Map<number, Tache[]> = new Map();
  selectedInterventionForTaches: Intervention | null = null;
  terminerTacheRequest: TerminerTacheRequest = {
    commentaire: '',
    tempsPasseMinutes: undefined
  };
  selectedTacheForTerminer: Tache | null = null;

  // Notifications
  notifications: Notification[] = [];
  unreadCount: number = 0;
  showNotifications: boolean = false;
  private notificationSubscriptions: Subscription[] = [];

  constructor(
    private mainDOeuvreService: MainDOeuvreAgentService,
    private tacheService: MainDOeuvreTacheService,
    private notificationService: NotificationService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProfil();
    this.loadMyInterventions();
    this.loadStatistiques();
    this.loadNotifications();
    this.startNotificationPolling();
  }

  ngOnDestroy(): void {
    this.notificationSubscriptions.forEach(sub => {
      if (sub && !sub.closed) {
        sub.unsubscribe();
      }
    });
  }

  loadProfil(): void {
    this.mainDOeuvreService.getProfil().subscribe({
      next: (data) => {
        this.profil = data;
      },
      error: (err) => {
        console.error('Erreur chargement profil:', err);
        alert('Erreur lors du chargement du profil');
      }
    });
  }

  loadMyInterventions(): void {
    this.loading = true;
    const filters: any = {};
    if (this.filtreEtat) filters.etat = this.filtreEtat;

    this.mainDOeuvreService.getMyInterventions(filters).subscribe({
      next: (data) => {
        this.interventions = data || [];
        this.calculerStats();
        this.appliquerFiltres();
        // Charger les tâches pour chaque intervention
        this.loadTachesForAllInterventions();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement interventions:', err);
        this.loading = false;
        alert('Erreur lors du chargement des interventions. Vérifiez votre connexion.');
      }
    });
  }

  loadTachesForAllInterventions(): void {
    this.interventions.forEach(intervention => {
      this.loadTaches(intervention.id);
    });
  }

  loadTaches(interventionId: number): void {
    this.tacheService.getTachesByIntervention(interventionId).subscribe({
      next: (data) => {
        this.tachesParIntervention.set(interventionId, data || []);
      },
      error: (err) => {
        console.error('Erreur chargement tâches:', err);
        this.tachesParIntervention.set(interventionId, []);
      }
    });
  }

  getTachesForIntervention(interventionId: number): Tache[] {
    return this.tachesParIntervention.get(interventionId) || [];
  }

  loadStatistiques(): void {
    this.mainDOeuvreService.getStatistiques().subscribe({
      next: (data) => {
        this.statistiques = data;
      },
      error: (err) => {
        console.error('Erreur chargement statistiques:', err);
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
    const rechercheLower = normalizeText(this.recherche);
    this.interventionsFiltrees = this.interventions.filter(i => {
      if (!rechercheLower) return true;
      
      const matchRecherche = 
        normalizeText(i.description).includes(rechercheLower) ||
        i.id.toString().includes(rechercheLower) ||
        normalizeText(i.typeIntervention).includes(rechercheLower);
      
      return matchRecherche;
    });
  }

  voirDetails(id: number, event?: Event): void {
    if (event) {
      event.stopPropagation();
      event.preventDefault();
    }
    
    console.log('Navigation vers intervention:', id);
    this.router.navigate(['/main-doeuvre/intervention', id]).then(
      (success) => {
        if (success) {
          console.log('Navigation réussie vers intervention', id);
        } else {
          console.error('Navigation échouée vers intervention', id);
          alert('Impossible de naviguer vers les détails de l\'intervention. Vérifiez que vous avez les permissions nécessaires.');
        }
      }
    ).catch(err => {
      console.error('Erreur navigation:', err);
      alert('Erreur lors de la navigation vers les détails de l\'intervention: ' + (err.message || 'Erreur inconnue'));
    });
  }

  getEtatLabel(etat: string | undefined): string {
    if (!etat) return 'Inconnu';
    switch(etat) {
      case 'EN_ATTENTE': return '⏳ En attente';
      case 'EN_COURS': return '🔄 En cours';
      case 'TERMINEE': return '✅ Terminée';
      case 'SUSPENDUE': return '⏸️ Suspendue';
      default: return etat;
    }
  }

  getEtatClass(etat: string | undefined): string {
    if (!etat) return '';
    return `badge-etat-${etat.toLowerCase()}`;
  }

  getPrioriteLabel(priorite: string | undefined): string {
    if (!priorite) return 'Normale';
    switch(priorite) {
      case 'URGENTE': return '🔴 Urgente';
      case 'CRITIQUE': return '⚫ Critique';
      case 'NORMALE': return '🟢 Normale';
      case 'PLANIFIEE': return '🔵 Planifiée';
      default: return priorite;
    }
  }

  getPrioriteClass(priorite: string | undefined): string {
    if (!priorite) return 'badge-priorite-normale';
    return `badge-priorite-${priorite.toLowerCase()}`;
  }

  formatTemps(minutes: number | undefined): string {
    if (!minutes) return '0 min';
    const heures = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (heures > 0) {
      return `${heures}h ${mins}min`;
    }
    return `${mins}min`;
  }

  // ==================== GESTION DES TÂCHES ====================

  commencerTache(tache: Tache): void {
    if (!confirm('Voulez-vous commencer cette tâche ?\n\nLe technicien sera notifié.')) {
      return;
    }

    this.tacheService.commencer(tache.id).subscribe({
      next: () => {
        alert('✅ Tâche commencée !\n\nLe technicien a été notifié.');
        if (tache.interventionId) {
          this.loadTaches(tache.interventionId);
        }
      },
      error: (err) => {
        console.error('Erreur début tâche:', err);
        alert('❌ Erreur lors du début de la tâche: ' + (err.error?.message || err.message));
      }
    });
  }

  ouvrirTerminerTache(tache: Tache): void {
    this.selectedTacheForTerminer = tache;
    this.terminerTacheRequest = {
      commentaire: '',
      tempsPasseMinutes: undefined
    };
  }

  terminerTache(): void {
    if (!this.selectedTacheForTerminer) return;

    if (!confirm('Voulez-vous marquer cette tâche comme terminée ?\n\nLe technicien devra vérifier votre travail.')) {
      return;
    }

    this.tacheService.terminer(this.selectedTacheForTerminer.id, this.terminerTacheRequest).subscribe({
      next: () => {
        alert('✅ Tâche marquée comme terminée !\n\nLe technicien a été notifié et va vérifier votre travail.');
        if (this.selectedTacheForTerminer!.interventionId) {
          this.loadTaches(this.selectedTacheForTerminer!.interventionId);
        }
        this.selectedTacheForTerminer = null;
      },
      error: (err) => {
        console.error('Erreur terminaison tâche:', err);
        alert('❌ Erreur lors de la terminaison: ' + (err.error?.message || err.message));
      }
    });
  }

  getEtatTacheLabel(etat: string): string {
    switch (etat) {
      case 'A_FAIRE': return '⏳ À faire';
      case 'EN_COURS': return '🔧 En cours';
      case 'TERMINEE': return '✅ Terminée';
      case 'VERIFIEE': return '✓ Vérifiée';
      default: return etat;
    }
  }

  getEtatTacheClass(etat: string): string {
    switch (etat) {
      case 'A_FAIRE': return 'etat-a-faire';
      case 'EN_COURS': return 'etat-en-cours';
      case 'TERMINEE': return 'etat-terminee';
      case 'VERIFIEE': return 'etat-verifiee';
      default: return '';
    }
  }

  deconnexion(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }

  // Méthodes trackBy pour améliorer les performances
  trackByInterventionId(index: number, intervention: Intervention): number {
    return intervention.id;
  }

  trackByTacheId(index: number, tache: Tache): number {
    return tache.id;
  }

  trackByNotificationId(index: number, notification: Notification): number {
    return notification.idNotification;
  }

  // ==================== BARRE DE PROGRESSION D'ÉTAT ====================

  /**
   * Retourne le pourcentage de progression selon l'état de la tâche
   */
  getEtatProgress(etat: string): number {
    switch (etat) {
      case 'A_FAIRE': return 0;
      case 'EN_COURS': return 33;
      case 'TERMINEE': return 66;
      case 'VERIFIEE': return 100;
      default: return 0;
    }
  }

  /**
   * Vérifie si un état est actif (état actuel de la tâche)
   */
  isEtatActive(etatActuel: string, etat: string): boolean {
    return etatActuel === etat;
  }

  /**
   * Vérifie si un état est complété (déjà passé)
   */
  isEtatCompleted(etatActuel: string, etat: string): boolean {
    const ordreEtats = ['A_FAIRE', 'EN_COURS', 'TERMINEE', 'VERIFIEE'];
    const indexActuel = ordreEtats.indexOf(etatActuel);
    const indexEtat = ordreEtats.indexOf(etat);
    return indexActuel > indexEtat;
  }

  // ==================== GESTION DES NOTIFICATIONS ====================

  loadNotifications(): void {
    const user = this.authService.currentUserValue;
    if (!user) {
      console.warn('Aucun utilisateur connecté trouvé');
      this.notifications = [];
      return;
    }

    const userId = user.id;
    console.log('🔔 Chargement des notifications pour userId:', userId);
    console.log('🔔 Utilisateur:', user.email, 'Rôle:', user.role);
    
    if (!userId) {
      console.warn('Aucun ID utilisateur trouvé pour charger les notifications');
      this.notifications = [];
      return;
    }

    this.notificationService.getNotificationsByUser(userId).subscribe({
      next: (notifications) => {
        console.log('✅ Notifications reçues du serveur:', notifications);
        console.log('📊 Nombre de notifications:', notifications?.length || 0);
        this.notifications = notifications || [];
        console.log('📋 Notifications assignées au composant:', this.notifications.length);
        
        // Trier par date (plus récentes en premier)
        this.notifications.sort((a, b) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return dateB - dateA;
        });
        
        this.updateUnreadCount();
        console.log('🔔 Compteur non lues:', this.unreadCount);
      },
      error: (err) => {
        console.error('❌ Erreur chargement notifications:', err);
        console.error('❌ Détails erreur:', err.error || err.message);
        this.notifications = [];
        // Ne pas afficher d'alerte pour éviter d'être trop intrusif
        // alert('Erreur lors du chargement des notifications. Veuillez réessayer.');
      }
    });
  }

  startNotificationPolling(): void {
    const user = this.authService.currentUserValue;
    if (!user || !user.id) {
      console.warn('Aucun utilisateur connecté pour le polling des notifications');
      return;
    }

    const userId = user.id;

    // Polling du compteur toutes les 15 secondes
    const countSub = interval(15000).pipe(
      switchMap(() => this.notificationService.getUnreadCount(userId))
    ).subscribe({
      next: (result) => {
        const newCount = result.unreadCount || 0;
        if (newCount !== this.unreadCount) {
          console.log('🔔 Nouveau compteur de notifications:', newCount);
          this.unreadCount = newCount;
        }
      },
      error: (err) => {
        console.error('Erreur polling compteur notifications:', err);
      }
    });

    // Polling des notifications toutes les 30 secondes
    const notifSub = interval(30000).pipe(
      switchMap(() => this.notificationService.getNotificationsByUser(userId))
    ).subscribe({
      next: (notifications) => {
        const sortedNotifications = (notifications || []).sort((a, b) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return dateB - dateA;
        });
        
        if (sortedNotifications.length !== this.notifications.length) {
          console.log('🔔 Mise à jour des notifications:', sortedNotifications.length);
          this.notifications = sortedNotifications;
          this.updateUnreadCount();
        }
      },
      error: (err) => {
        console.error('Erreur polling notifications:', err);
      }
    });

    this.notificationSubscriptions.push(countSub, notifSub);
  }

  updateUnreadCount(): void {
    this.unreadCount = this.notifications.filter(n => !n.readable).length;
  }

  toggleNotifications(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      this.loadNotifications();
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.notifications-container')) {
      this.showNotifications = false;
    }
  }

  markAsRead(notification: Notification): void {
    if (notification.readable) return;

    this.notificationService.markAsRead(notification.idNotification).subscribe({
      next: () => {
        notification.readable = true;
        this.updateUnreadCount();
      },
      error: (err) => {
        console.error('Erreur marquage notification:', err);
      }
    });
  }

  markAllAsRead(): void {
    const unreadNotifications = this.notifications.filter(n => !n.readable);
    unreadNotifications.forEach(notif => {
      this.notificationService.markAsRead(notif.idNotification).subscribe({
        next: () => {
          notif.readable = true;
        },
        error: (err) => {
          console.error('Erreur marquage notification:', err);
        }
      });
    });
    this.updateUnreadCount();
  }

  formatNotificationDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'À l\'instant';
    if (diffMins < 60) return `Il y a ${diffMins} min`;
    if (diffHours < 24) return `Il y a ${diffHours}h`;
    if (diffDays < 7) return `Il y a ${diffDays}j`;
    return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
  }
}

