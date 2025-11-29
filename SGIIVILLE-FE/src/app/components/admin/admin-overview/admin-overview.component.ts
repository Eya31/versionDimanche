import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../../services/admin.service';
import { InterventionService } from '../../../services/intervention.service';
import { NotificationService } from '../../../services/notification.service';
import { AuthService } from '../../../services/auth.service';

interface KPI {
  title: string;
  value: number;
  icon: string;
  color: string;
  route?: string;
}

interface ChartData {
  label: string;
  count: number;
}

interface StatParEtat {
  label: string;
  count: number;
  color: string;
  percentage: number;
}

@Component({
  selector: 'app-admin-overview',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-overview.component.html',
  styleUrls: ['./admin-overview.component.css']
})
export class AdminOverviewComponent implements OnInit {
  kpis: KPI[] = [
    {
      title: 'Interventions en cours',
      value: 0,
      icon: '🟢',
      color: '#10b981',
      route: '/admin/interventions/liste'
    },
    {
      title: 'Interventions en attente',
      value: 0,
      icon: '🟠',
      color: '#f59e0b',
      route: '/admin/interventions/liste'
    },
    {
      title: 'Total utilisateurs',
      value: 0,
      icon: '👥',
      color: '#3b82f6',
      route: '/admin/users'
    },
    {
      title: 'Notifications non lues',
      value: 0,
      icon: '🔔',
      color: '#ef4444',
      route: '/admin/notifications/unread'
    }
  ];

  chartData: ChartData[] = [];
  statsParEtat: StatParEtat[] = [];
  recentInterventions: any[] = [];
  recentUsers: any[] = [];

  constructor(
    private adminService: AdminService,
    private interventionService: InterventionService,
    private notificationService: NotificationService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadAllData();
  }

  loadAllData(): void {
    this.loadInterventions();
    this.loadUsers();
    this.loadNotifications();
  }

  loadInterventions(): void {
    this.interventionService.getAllInterventions().subscribe({
      next: (interventions) => {
        // KPIs Interventions
        this.kpis[0].value = interventions.filter(i => i.etat === 'EN_COURS').length;
        this.kpis[1].value = interventions.filter(i => i.etat === 'EN_ATTENTE').length;

        // Dernières interventions (5 plus récentes)
        this.recentInterventions = [...interventions]
          .sort((a, b) => new Date(b.datePlanifiee).getTime() - new Date(a.datePlanifiee).getTime())
          .slice(0, 5);

        // Graphique 7 derniers jours
        this.generateChartData(interventions);

        // Stats par état
        this.generateStatsParEtat(interventions);
      },
      error: (err) => console.error('Erreur chargement interventions:', err)
    });
  }

  loadUsers(): void {
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        // KPI Utilisateurs
        this.kpis[2].value = users.length;

        // Derniers utilisateurs (5 plus récents)
        this.recentUsers = users.slice(-5).reverse();
      },
      error: (err) => console.error('Erreur chargement utilisateurs:', err)
    });
  }

  loadNotifications(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.notificationService.getNotificationsByUser(userId).subscribe({
      next: (notifications) => {
        this.kpis[3].value = notifications.filter(n => !n.readable).length;
      },
      error: (err) => console.error('Erreur chargement notifications:', err)
    });
  }

  generateChartData(interventions: any[]): void {
    const today = new Date();
    const days: ChartData[] = [];

    for (let i = 6; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      const dateStr = date.toISOString().split('T')[0];

      const count = interventions.filter(intervention => {
        const intervDate = new Date(intervention.datePlanifiee).toISOString().split('T')[0];
        return intervDate === dateStr;
      }).length;

      days.push({
        label: this.getShortDayLabel(date),
        count: count
      });
    }

    this.chartData = days;
  }

  generateStatsParEtat(interventions: any[]): void {
    const etatCounts = {
      'EN_COURS': 0,
      'EN_ATTENTE': 0,
      'TERMINEE': 0,
      'ANNULEE': 0
    };

    interventions.forEach(i => {
      if (etatCounts.hasOwnProperty(i.etat)) {
        etatCounts[i.etat as keyof typeof etatCounts]++;
      }
    });

    const total = interventions.length || 1;

    this.statsParEtat = [
      {
        label: 'En cours',
        count: etatCounts.EN_COURS,
        color: '#10b981',
        percentage: (etatCounts.EN_COURS / total) * 100
      },
      {
        label: 'En attente',
        count: etatCounts.EN_ATTENTE,
        color: '#f59e0b',
        percentage: (etatCounts.EN_ATTENTE / total) * 100
      },
      {
        label: 'Terminée',
        count: etatCounts.TERMINEE,
        color: '#3b82f6',
        percentage: (etatCounts.TERMINEE / total) * 100
      },
      {
        label: 'Annulée',
        count: etatCounts.ANNULEE,
        color: '#ef4444',
        percentage: (etatCounts.ANNULEE / total) * 100
      }
    ];
  }

  getShortDayLabel(date: Date): string {
    const days = ['Dim', 'Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam'];
    return days[date.getDay()];
  }

  calculateBarHeight(count: number): number {
    if (this.chartData.length === 0) return 0;
    const maxCount = Math.max(...this.chartData.map(d => d.count), 1);
    return (count / maxCount) * 100;
  }

  refreshData(): void {
    this.loadAllData();
  }

  getEtatClass(etat: string): string {
    const classes: { [key: string]: string } = {
      'EN_COURS': 'badge-success',
      'EN_ATTENTE': 'badge-warning',
      'TERMINEE': 'badge-info',
      'ANNULEE': 'badge-danger'
    };
    return classes[etat] || 'badge-default';
  }

  getRoleClass(role: string): string {
    const classes: { [key: string]: string } = {
      'ADMINISTRATEUR': 'badge-danger',
      'CHEF_SERVICE': 'badge-info',
      'TECHNICIEN': 'badge-success',
      'CITOYEN': 'badge-secondary',
      'MAIN_DOEUVRE': 'badge-warning'
    };
    return classes[role] || 'badge-default';
  }

  getRoleIcon(role: string): string {
    const icons: { [key: string]: string } = {
      'ADMINISTRATEUR': '👑',
      'CHEF_SERVICE': '👔',
      'TECHNICIEN': '🧑‍🔧',
      'CITOYEN': '👤',
      'MAIN_DOEUVRE': '🟡'
    };
    return icons[role] || '👤';
  }
}
