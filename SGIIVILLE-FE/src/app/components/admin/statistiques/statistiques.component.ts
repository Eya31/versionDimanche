import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../../services/admin.service';
import { InterventionService } from '../../../services/intervention.service';
import { NotificationService } from '../../../services/notification.service';
import { AuthService } from '../../../services/auth.service';

interface StatCard {
  title: string;
  value: number | string;
  icon: string;
  color: string;
  trend?: string;
}

interface ChartData {
  label: string;
  value: number;
  color: string;
}

@Component({
  selector: 'app-statistiques',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './statistiques.component.html',
  styleUrls: ['./statistiques.component.css']
})
export class StatistiquesComponent implements OnInit {
  loading = false;
  
  // KPIs
  kpis: StatCard[] = [];
  
  // Interventions
  interventionsStats: ChartData[] = [];
  interventionsParMois: ChartData[] = [];
  
  // Utilisateurs
  usersStats: ChartData[] = [];
  
  // Performance
  performanceStats: any = {
    tempsMoyenIntervention: 0,
    tauxCompletion: 0,
    interventionsUrgentes: 0
  };

  totalInterventions = 0;
  totalUsers = 0;
  totalNotifications = 0;

  constructor(
    private adminService: AdminService,
    private interventionService: InterventionService,
    private notificationService: NotificationService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadAllStats();
  }

  loadAllStats(): void {
    this.loading = true;
    Promise.all([
      this.loadInterventions(),
      this.loadUsers(),
      this.loadNotifications()
    ]).finally(() => {
      this.updateKPIs();
      this.loading = false;
    });
  }

  loadInterventions(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.interventionService.getAllInterventions().subscribe({
        next: (interventions) => {
          this.totalInterventions = interventions.length;
          
          // Stats par état
          const etatCounts: { [key: string]: number } = {};
          interventions.forEach(i => {
            etatCounts[i.etat] = (etatCounts[i.etat] || 0) + 1;
          });

          this.interventionsStats = [
            { label: 'En cours', value: etatCounts['EN_COURS'] || 0, color: '#10b981' },
            { label: 'En attente', value: etatCounts['EN_ATTENTE'] || 0, color: '#f59e0b' },
            { label: 'Terminée', value: etatCounts['TERMINEE'] || 0, color: '#3b82f6' },
            { label: 'Suspendue', value: etatCounts['SUSPENDUE'] || 0, color: '#ef4444' }
          ];

          // Stats par mois (7 derniers mois)
          const moisCounts: { [key: string]: number } = {};
          const now = new Date();
          for (let i = 6; i >= 0; i--) {
            const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
            const moisKey = date.toLocaleDateString('fr-FR', { month: 'short', year: 'numeric' });
            moisCounts[moisKey] = 0;
          }

          interventions.forEach(i => {
            const date = new Date(i.datePlanifiee);
            const moisKey = date.toLocaleDateString('fr-FR', { month: 'short', year: 'numeric' });
            if (moisCounts.hasOwnProperty(moisKey)) {
              moisCounts[moisKey]++;
            }
          });

          this.interventionsParMois = Object.keys(moisCounts).map(mois => ({
            label: mois,
            value: moisCounts[mois],
            color: '#007bff'
          }));

          // Performance
          const terminees = interventions.filter(i => i.etat === 'TERMINEE');
          this.performanceStats.tauxCompletion = interventions.length > 0 
            ? Math.round((terminees.length / interventions.length) * 100) 
            : 0;
          this.performanceStats.interventionsUrgentes = interventions.filter(i => 
            i.priorite === 'URGENTE' || i.priorite === 'CRITIQUE'
          ).length;

          resolve();
        },
        error: (err) => {
          console.error('Erreur chargement interventions:', err);
          reject(err);
        }
      });
    });
  }

  loadUsers(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.adminService.getAllUsers().subscribe({
        next: (users) => {
          this.totalUsers = users.length;
          
          const roleCounts: { [key: string]: number } = {};
          users.forEach(u => {
            roleCounts[u.role] = (roleCounts[u.role] || 0) + 1;
          });

          this.usersStats = Object.keys(roleCounts).map(role => ({
            label: this.getRoleLabel(role),
            value: roleCounts[role],
            color: this.getRoleColor(role)
          }));

          resolve();
        },
        error: (err) => {
          console.error('Erreur chargement utilisateurs:', err);
          reject(err);
        }
      });
    });
  }

  loadNotifications(): Promise<void> {
    return new Promise((resolve, reject) => {
      const userId = this.authService.getUserId();
      if (!userId) {
        resolve();
        return;
      }

      this.notificationService.getNotificationsByUser(userId).subscribe({
        next: (notifications) => {
          this.totalNotifications = notifications.length;
          resolve();
        },
        error: (err) => {
          console.error('Erreur chargement notifications:', err);
          resolve(); // Non bloquant
        }
      });
    });
  }

  // KPIs
  updateKPIs(): void {
    this.kpis = [
      {
        title: 'Total interventions',
        value: this.totalInterventions,
        icon: '🧭',
        color: '#007bff'
      },
      {
        title: 'Taux de complétion',
        value: `${this.performanceStats.tauxCompletion}%`,
        icon: '✅',
        color: '#10b981'
      },
      {
        title: 'Total utilisateurs',
        value: this.totalUsers,
        icon: '👥',
        color: '#3b82f6'
      },
      {
        title: 'Interventions urgentes',
        value: this.performanceStats.interventionsUrgentes,
        icon: '🚨',
        color: '#ef4444'
      }
    ];
  }

  getRoleLabel(role: string): string {
    const labels: { [key: string]: string } = {
      'ADMINISTRATEUR': 'Administrateurs',
      'CHEF_SERVICE': 'Chefs de service',
      'TECHNICIEN': 'Techniciens',
      'CITOYEN': 'Citoyens',
      'MAIN_DOEUVRE': 'Main d\'œuvre'
    };
    return labels[role] || role;
  }

  getRoleColor(role: string): string {
    const colors: { [key: string]: string } = {
      'ADMINISTRATEUR': '#dc3545',
      'CHEF_SERVICE': '#17a2b8',
      'TECHNICIEN': '#28a745',
      'CITOYEN': '#6c757d',
      'MAIN_DOEUVRE': '#ffc107'
    };
    return colors[role] || '#6c757d';
  }

  calculateBarHeight(value: number, maxValue: number): number {
    if (maxValue === 0) return 0;
    return (value / maxValue) * 100;
  }

  getMaxValue(data: ChartData[]): number {
    if (data.length === 0) return 1;
    return Math.max(...data.map(d => d.value), 1);
  }

  refresh(): void {
    this.loadAllStats();
  }
}

