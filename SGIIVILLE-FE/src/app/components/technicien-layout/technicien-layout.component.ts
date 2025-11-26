import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, RouterOutlet, NavigationEnd } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { NotificationService, Notification } from '../../services/notification.service';

@Component({
  selector: 'app-technicien-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterOutlet],
  templateUrl: './technicien-layout.component.html',
  styleUrls: ['./technicien-layout.component.css']
})
export class TechnicienLayoutComponent implements OnInit, OnDestroy {
  currentUser: any = null;
  currentRoute = '';
  showNotificationsDropdown = false;
  notifications: Notification[] = [];
  unreadCount = 0;
  private unreadCountSubscription?: Subscription;
  private notificationsSubscription?: Subscription;

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    
    // Suivre la route actuelle
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.currentRoute = event.url;
      });
    
    this.currentRoute = this.router.url;
    
    // Charger les notifications
    this.loadNotifications();
    this.startNotificationPolling();
  }

  ngOnDestroy(): void {
    this.unreadCountSubscription?.unsubscribe();
    this.notificationsSubscription?.unsubscribe();
  }

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

    // Polling du compteur toutes les 15 secondes
    this.unreadCountSubscription = this.notificationService.pollUnreadCount(userId).subscribe({
      next: (data) => {
        this.unreadCount = data.unreadCount;
        if (data.unreadCount > 0) {
          this.loadNotifications(); // Recharger si nouvelles notifications
        }
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
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      },
      error: (err) => console.error('Erreur marquer comme lu:', err)
    });
  }

  formatNotificationDate(date: string): string {
    const now = new Date();
    const notifDate = new Date(date);
    const diffMs = now.getTime() - notifDate.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'À l\'instant';
    if (diffMins < 60) return `Il y a ${diffMins} min`;
    if (diffHours < 24) return `Il y a ${diffHours}h`;
    if (diffDays < 7) return `Il y a ${diffDays}j`;
    return notifDate.toLocaleDateString('fr-FR');
  }

  logout(): void {
    if (confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
      this.authService.logout();
      this.router.navigate(['/login']);
    }
  }

  isActiveRoute(route: string): boolean {
    return this.currentRoute.includes(route);
  }
}

