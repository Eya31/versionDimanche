import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, RouterOutlet, NavigationEnd } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../../services/auth.service';
import { NotificationService, Notification } from '../../../services/notification.service';

interface MenuItem {
  title: string;
  icon: string;
  route?: string;
  exact?: boolean;
  badge?: string;
  action?: string;
  expanded?: boolean;
  submenu?: SubMenuItem[];
}

interface SubMenuItem {
  title: string;
  icon: string;
  route: string;
  badge?: string;
}

@Component({
  selector: 'app-technicien-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterOutlet],
  templateUrl: './technicien-layout.component.html',
  styleUrls: ['./technicien-layout.component.css']
})
export class TechnicienLayoutComponent implements OnInit, OnDestroy {
  sidebarCollapsed = false;
  currentUser: any = null;
  unreadCount = 0;
  currentRoute = '';
  pageTitle = 'Tableau de bord';
  showNotificationsDropdown = false;
  notifications: Notification[] = [];
  private unreadCountSubscription?: Subscription;
  private routeSubscription?: Subscription;
  private notificationsSubscription?: Subscription;

  menuItems: MenuItem[] = [
    {
      title: 'Tableau de bord',
      icon: '📊',
      route: '/technicien',
      exact: true
    },
    {
      title: 'Interventions',
      icon: '🧭',
      expanded: false,
      submenu: [
        { title: 'Mes interventions', icon: '📋', route: '/technicien/interventions' },
        { title: 'Interventions en cours', icon: '🟢', route: '/technicien/interventions/en-cours' },
        { title: 'Rapports finaux', icon: '📄', route: '/technicien/rapports-finaux' }
      ]
    },
    {
      title: 'Main d\'œuvre',
      icon: '🛠️',
      route: '/technicien/main-doeuvre'
    },
    {
      title: 'Mon profil',
      icon: '👤',
      route: '/technicien/profil'
    },
    {
      title: 'Déconnexion',
      icon: '🚪',
      action: 'logout'
    }
  ];

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadNotifications();
    this.startNotificationPolling();
    this.trackRoute();
    this.setupClickOutside();
  }

  setupClickOutside(): void {
    // Fermer le dropdown si on clique en dehors
    document.addEventListener('click', (event: any) => {
      if (this.showNotificationsDropdown) {
        const target = event.target as HTMLElement;
        if (!target.closest('.notification-container')) {
          this.showNotificationsDropdown = false;
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.unreadCountSubscription?.unsubscribe();
    this.routeSubscription?.unsubscribe();
    this.notificationsSubscription?.unsubscribe();
  }

  trackRoute(): void {
    this.currentRoute = this.router.url;
    this.updatePageTitle();
    
    this.routeSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.currentRoute = event.url;
        this.updatePageTitle();
      });
  }

  updatePageTitle(): void {
    if (this.currentRoute.includes('/profil')) {
      this.pageTitle = 'Mon Profil';
    } else if (this.currentRoute.includes('/main-doeuvre')) {
      this.pageTitle = 'Gestion Main d\'Œuvre';
    } else if (this.currentRoute.includes('/intervention/') && this.currentRoute.includes('/rapport')) {
      this.pageTitle = 'Rapport Final';
    } else if (this.currentRoute.includes('/intervention/')) {
      this.pageTitle = 'Détails Intervention';
    } else if (this.currentRoute.includes('/interventions/en-cours')) {
      this.pageTitle = 'Interventions en Cours';
    } else if (this.currentRoute.includes('/interventions')) {
      this.pageTitle = 'Mes Interventions';
    } else if (this.currentRoute.includes('/rapports-finaux')) {
      this.pageTitle = 'Rapports Finaux';
    } else {
      this.pageTitle = 'Tableau de bord';
    }
  }

  loadCurrentUser(): void {
    const userId = this.authService.getUserId();
    if (userId) {
      this.currentUser = {
        nom: localStorage.getItem('userName') || 'Technicien'
      };
    }
  }

  loadNotifications(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.notificationService.getNotificationsByUser(userId).subscribe({
      next: (data: Notification[]) => {
        this.notifications = (data || []).sort((a: Notification, b: Notification) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return dateB - dateA;
        });
        this.updateUnreadCount();
      },
      error: (err: any) => {
        console.error('Erreur chargement notifications:', err);
        this.notifications = [];
      }
    });
  }

  startNotificationPolling(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    // Charger immédiatement le compteur de notifications non lues
    this.notificationService.getUnreadCount(userId).subscribe({
      next: (data: { unreadCount: number }) => {
        this.unreadCount = data.unreadCount || 0;
      },
      error: (err: any) => console.error('Erreur chargement compteur notifications:', err)
    });

    // Polling du compteur de notifications non lues toutes les 15 secondes
    this.unreadCountSubscription = this.notificationService.pollUnreadCount(userId).subscribe({
      next: (data: { unreadCount: number }) => {
        this.unreadCount = data.unreadCount || 0;
      },
      error: (err: any) => console.error('Erreur polling notifications:', err)
    });

    // Polling des notifications toutes les 30 secondes
    this.notificationsSubscription = this.notificationService.pollNotifications(userId).subscribe({
      next: (data: Notification[]) => {
        this.notifications = (data || []).sort((a: Notification, b: Notification) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return dateB - dateA;
        });
        this.updateUnreadCount();
      },
      error: (err: any) => console.error('Erreur polling notifications:', err)
    });
  }

  updateUnreadCount(): void {
    this.unreadCount = this.notifications.filter(n => !n.readable).length;
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
        this.updateUnreadCount();
      },
      error: (err: any) => console.error('Erreur marquer notification comme lue:', err)
    });
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
    
    return date.toLocaleDateString('fr-FR', { 
      day: 'numeric', 
      month: 'short',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  toggleSubmenu(item: MenuItem): void {
    item.expanded = !item.expanded;
  }

  getBadgeValue(badgeKey: string): number {
    if (badgeKey === 'unreadCount') {
      return this.unreadCount;
    }
    return 0;
  }

  handleAction(action: string): void {
    if (action === 'logout') {
      if (confirm('Voulez-vous vraiment vous déconnecter ?')) {
        this.authService.logout();
        this.router.navigate(['/login']);
      }
    }
  }
}
