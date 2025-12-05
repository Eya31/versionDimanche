import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemandeFormComponent } from '../demande-form/demande-form.component';
import { Router } from '@angular/router';
import { DemandeService } from '../../services/demande.service';
import { AuthService } from '../../services/auth.service';
import { NotificationService, Notification } from '../../services/notification.service';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslationService } from '../../services/translation.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-citoyen-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, DemandeFormComponent, TranslatePipe],
  templateUrl: './citoyen-dashboard.component.html',
  styleUrls: ['./citoyen-dashboard.component.css'],
  changeDetection: ChangeDetectionStrategy.Default
})
export class CitoyenDashboardComponent implements OnInit, OnDestroy {
  demandes: any[] = [];
  showCreateForm = false;
  selectedDemande: any = null;

  stats = {
    total: 0,
    enCours: 0,
    traitee: 0
  };

  // Notifications
  notifications: Notification[] = [];
  unreadCount = 0;
  showNotificationsDropdown = false;
  private notificationSubscription?: Subscription;
  private unreadCountSubscription?: Subscription;
  private languageSubscription?: Subscription;

  constructor(
    private demandeService: DemandeService,
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService,
    public translationService: TranslationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadMesDemandes();
    this.loadNotifications();
    this.startNotificationPolling();
    this.subscribeToLanguageChanges();
  }

  ngOnDestroy(): void {
    this.notificationSubscription?.unsubscribe();
    this.unreadCountSubscription?.unsubscribe();
    this.languageSubscription?.unsubscribe();
  }

  private subscribeToLanguageChanges(): void {
    this.languageSubscription = this.translationService.currentLanguage$.subscribe(() => {
      console.log('[CitoyenDashboard] Langue changée, marquant pour re-render');
      this.cdr.markForCheck();
    });
  }

  // === GESTION DES NOTIFICATIONS ===
  loadNotifications(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.notificationService.getNotificationsByUser(userId).subscribe({
      next: (data) => {
        this.notifications = data.sort((a, b) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.unreadCount = this.notifications.filter(n => !n.readable).length;
      },
      error: (err) => console.error('Erreur chargement notifications:', err)
    });
  }

  startNotificationPolling(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.unreadCountSubscription = this.notificationService.pollUnreadCount(userId).subscribe({
      next: (data) => {
        this.unreadCount = data.unreadCount;
      },
      error: (err) => console.error('Erreur polling notifications:', err)
    });
  }

  toggleNotificationsDropdown(): void {
    this.showNotificationsDropdown = !this.showNotificationsDropdown;
    if (this.showNotificationsDropdown) {
      this.loadNotifications();
    }
  }

  markAsRead(notification: Notification): void {
    if (notification.readable) return;

    this.notificationService.markAsRead(notification.idNotification).subscribe({
      next: () => {
        notification.readable = true;
        this.unreadCount = this.notifications.filter(n => !n.readable).length;
      },
      error: (err) => console.error('Erreur marquage notification:', err)
    });
  }

  formatNotificationDate(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'À l\'instant';
    if (minutes < 60) return `Il y a ${minutes} min`;
    if (hours < 24) return `Il y a ${hours}h`;
    return `Il y a ${days}j`;
  }

  loadMesDemandes(): void {
    const currentUser = this.authService.currentUserValue;
    if (!currentUser || !currentUser.id) {
      console.error('Utilisateur non connecté');
      this.router.navigate(['/login']);
      return;
    }

    // Charger uniquement les demandes du citoyen connecté
    this.demandeService.getDemandesByCitoyen(currentUser.id).subscribe({
      next: (data) => {
        this.demandes = data;
        this.calculateStats();
      },
      error: (err) => {
        console.error('Erreur chargement demandes:', err);
        this.demandes = [];
      }
    });
  }

  calculateStats(): void {
    if (!this.demandes) return;
    this.stats.total = this.demandes.length;
    this.stats.traitee = this.demandes.filter(d => 
      d.etat === 'TRAITEE' || d.etat === 'DONE' || d.etat === 'RESOLUE'
    ).length;
    this.stats.enCours = this.stats.total - this.stats.traitee;
  }

  openDetails(demande: any): void {
    this.selectedDemande = demande;
  }

  closeDetails(): void {
    this.selectedDemande = null;
  }

  getStatusClass(status: string): string {
    if (!status) return 'new';
    switch(status.toUpperCase()) {
      case 'TRAITEE':
      case 'DONE':
      case 'RESOLUE':
        return 'traitee';
      case 'EN_COURS':
      case 'IN_PROGRESS':
        return 'pending';
      default: return 'new';
    }
  }

  getPriorityClass(prio: string): string {
    if (!prio) return 'low';
    switch(prio.toUpperCase()) {
      case 'HIGH': return 'high';
      case 'MEDIUM': return 'medium';
      default: return 'low';
    }
  }

  // Fonction pour obtenir l'historique des états d'une demande
  getDemandeTimeline(demande: any): any[] {
    const timeline = [];
    
    // État 1: Soumise
    timeline.push({
      status: 'SOUMISE',
      label: 'Demande soumise',
      date: demande.dateSoumission,
      icon: '📝',
      completed: true,
      description: 'Votre demande a été enregistrée dans le système'
    });

    // État 2: En cours de traitement
    const isInProgress = demande.etat === 'EN_COURS' || demande.etat === 'IN_PROGRESS';
    const isTreated = demande.etat === 'TRAITEE' || demande.etat === 'DONE' || demande.etat === 'RESOLUE';
    
    timeline.push({
      status: 'EN_COURS',
      label: 'En cours de traitement',
      date: isInProgress || isTreated ? demande.dateTraitement || 'En cours' : null,
      icon: '🔧',
      completed: isInProgress || isTreated,
      current: isInProgress,
      description: 'Une équipe technique a été assignée à votre demande'
    });

    // État 3: Intervention planifiée
    timeline.push({
      status: 'PLANIFIEE',
      label: 'Intervention planifiée',
      date: demande.datePlanification || null,
      icon: '📅',
      completed: isTreated,
      current: false,
      description: 'L\'intervention a été programmée'
    });

    // État 4: Résolue
    timeline.push({
      status: 'RESOLUE',
      label: 'Problème résolu',
      date: isTreated ? (demande.dateResolution || demande.dateTraitement) : null,
      icon: '✅',
      completed: isTreated,
      current: isTreated,
      description: 'Votre demande a été traitée avec succès'
    });

    return timeline;
  }

  getProgressPercentage(demande: any): number {
    const status = demande.etat?.toUpperCase();
    if (!status) return 25;
    
    switch(status) {
      case 'SOUMISE':
      case 'NON_TRAITEE':
        return 25;
      case 'EN_COURS':
      case 'IN_PROGRESS':
        return 50;
      case 'PLANIFIEE':
        return 75;
      case 'TRAITEE':
      case 'DONE':
      case 'RESOLUE':
        return 100;
      default:
        return 25;
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
