import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { NotificationService } from '../../../services/notification.service';
import { Subscription } from 'rxjs';

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
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.css']
})
export class AdminLayoutComponent implements OnInit, OnDestroy {
  sidebarCollapsed = false;
  currentUser: any = null;
  unreadCount = 0;
  private unreadCountSubscription?: Subscription;

  menuItems: MenuItem[] = [
    {
      title: 'Dashboard',
      icon: '📊',
      route: '/admin/dashboard',
      exact: true
    },
    {
      title: 'Interventions',
      icon: '🧭',
      expanded: false,
      submenu: [
        { title: 'Carte des interventions', icon: '📍', route: '/admin/interventions/carte' },
        { title: 'Liste des interventions', icon: '📋', route: '/admin/interventions/liste' },
        { title: 'Statistiques & KPIs', icon: '🧮', route: '/admin/interventions/stats' }
      ]
    },
    {
      title: 'Utilisateurs',
      icon: '👥',
      expanded: false,
      submenu: [
        { title: 'Tous les utilisateurs', icon: '👤', route: '/admin/users' },
        { title: 'Techniciens', icon: '🧑‍🔧', route: '/admin/users/techniciens' },
        { title: 'Chefs de service', icon: '👔', route: '/admin/users/chefs' },
        { title: 'Main d\'œuvre', icon: '🛠️', route: '/admin/main-doeuvre' },
        { title: 'Ajouter un utilisateur', icon: '✨', route: '/admin/users/add' },
        { title: 'Ajouter main d\'œuvre', icon: '➕', route: '/admin/main-doeuvre/add' }
      ]
    },
    {
      title: 'Notifications',
      icon: '🔔',
      badge: 'unreadCount',
      expanded: false,
      submenu: [
        { title: 'Notifications non lues', icon: '📭', route: '/admin/notifications/unread', badge: 'unreadCount' },
        { title: 'Historique', icon: '🗃️', route: '/admin/notifications/history' }
      ]
    },
    {
      title: 'Statistiques',
      icon: '📈',
      expanded: false,
      submenu: [
        { title: 'KPIs du système', icon: '📊', route: '/admin/statistiques' },
        { title: 'Analytics & KPIs', icon: '📈', route: '/admin/analytics' }
      ]
    },
    {
      title: 'Administration',
      icon: '🗃️',
      expanded: false,
      submenu: [
        { title: 'Configuration', icon: '⚙️', route: '/admin/systeme/config' },
        { title: 'Gestion XML', icon: '📁', route: '/admin/systeme/xml' },
        { title: 'Sauvegardes', icon: '💾', route: '/admin/systeme/backup' }
      ]
    },
    {
      title: 'Journaux',
      icon: '📜',
      expanded: false,
      submenu: [
        { title: 'Logs de connexion', icon: '🔐', route: '/admin/logs/connexion' },
        { title: 'Audit admin', icon: '📝', route: '/admin/logs/audit' },
        { title: 'Activité système', icon: '💻', route: '/admin/logs/systeme' }
      ]
    },
    {
      title: 'Sécurité',
      icon: '🔐',
      expanded: false,
      submenu: [
        { title: 'Sessions actives', icon: '🟢', route: '/admin/securite/sessions' },
        { title: 'Droits & rôles', icon: '🎭', route: '/admin/securite/roles' }
      ]
    },
    {
      title: 'Matériel & Stocks',
      icon: '🛠️',
      expanded: false,
      submenu: [
        { title: 'Liste matériel & stocks', icon: '📦', route: '/admin/materiel/stocks' },
        { title: 'Gestion demandes matériel', icon: '📋', route: '/admin/materiel/demandes' }
      ]
    },
    {
      title: 'Déconnexion',
      icon: '🚪',
      action: 'logout'
    }
  ];

  constructor(
    private router: Router,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.startNotificationPolling();
  }

  ngOnDestroy(): void {
    this.unreadCountSubscription?.unsubscribe();
  }

  loadCurrentUser(): void {
    const userId = this.authService.getUserId();
    if (userId) {
      // Pour l'instant, juste utiliser les infos du localStorage
      this.currentUser = {
        nom: localStorage.getItem('userName') || 'Administrateur'
      };
    }
  }

  startNotificationPolling(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.unreadCountSubscription = this.notificationService.pollUnreadCount(userId).subscribe({
      next: (data) => {
        this.unreadCount = data.unreadCount || 0;
      },
      error: (err) => console.error('Erreur polling notifications:', err)
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
