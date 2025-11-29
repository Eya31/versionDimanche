import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InterventionService } from '../../../../services/intervention.service';
import { Intervention } from '../../../../models/intervention.model';

interface StatCard {
  title: string;
  value: number;
  icon: string;
  color: string;
  trend?: string;
}

interface ChartData {
  label: string;
  count: number;
  color: string;
}

@Component({
  selector: 'app-intervention-stats',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './intervention-stats.component.html',
  styleUrls: ['./intervention-stats.component.css']
})
export class InterventionStatsComponent implements OnInit {
  loading = false;
  interventions: Intervention[] = [];

  // KPIs
  kpis: StatCard[] = [];

  // Graphiques
  statsParEtat: ChartData[] = [];
  statsParPriorite: ChartData[] = [];
  statsParMois: ChartData[] = [];

  // Détails
  interventionsUrgentes: Intervention[] = [];
  interventionsEnRetard: Intervention[] = [];

  constructor(private interventionService: InterventionService) { }

  ngOnInit(): void {
    this.loadInterventions();
  }

  loadInterventions(): void {
    this.loading = true;
    this.interventionService.getAllInterventions().subscribe({
      next: (interventions) => {
        this.interventions = interventions;
        this.calculateStats();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement interventions:', err);
        this.loading = false;
        alert('Erreur lors du chargement des interventions');
      }
    });
  }

  calculateStats(): void {
    // KPIs
    this.kpis = [
      {
        title: 'Total interventions',
        value: this.interventions.length,
        icon: '🧭',
        color: '#007bff'
      },
      {
        title: 'En cours',
        value: this.interventions.filter(i => i.etat === 'EN_COURS').length,
        icon: '🟢',
        color: '#10b981'
      },
      {
        title: 'En attente',
        value: this.interventions.filter(i => i.etat === 'EN_ATTENTE').length,
        icon: '🟠',
        color: '#f59e0b'
      },
      {
        title: 'Terminées',
        value: this.interventions.filter(i => i.etat === 'TERMINEE').length,
        icon: '✅',
        color: '#3b82f6'
      },
      {
        title: 'Urgentes',
        value: this.interventions.filter(i => i.priorite === 'URGENTE' || i.priorite === 'CRITIQUE').length,
        icon: '🚨',
        color: '#ef4444'
      }
    ];

    // Stats par état
    const etatCounts: { [key: string]: number } = {};
    this.interventions.forEach(i => {
      etatCounts[i.etat] = (etatCounts[i.etat] || 0) + 1;
    });

    this.statsParEtat = [
      { label: 'En cours', count: etatCounts['EN_COURS'] || 0, color: '#10b981' },
      { label: 'En attente', count: etatCounts['EN_ATTENTE'] || 0, color: '#f59e0b' },
      { label: 'Terminée', count: etatCounts['TERMINEE'] || 0, color: '#3b82f6' },
      { label: 'Suspendue', count: etatCounts['SUSPENDUE'] || 0, color: '#ef4444' }
    ];

    // Stats par priorité
    const prioriteCounts: { [key: string]: number } = {};
    this.interventions.forEach(i => {
      prioriteCounts[i.priorite] = (prioriteCounts[i.priorite] || 0) + 1;
    });

    this.statsParPriorite = [
      { label: 'Urgente', count: prioriteCounts['URGENTE'] || 0, color: '#ef4444' },
      { label: 'Critique', count: prioriteCounts['CRITIQUE'] || 0, color: '#dc3545' },
      { label: 'Normale', count: prioriteCounts['NORMALE'] || 0, color: '#3b82f6' },
      { label: 'Planifiée', count: prioriteCounts['PLANIFIEE'] || 0, color: '#6c757d' }
    ];

    // Stats par mois (7 derniers mois)
    const moisCounts: { [key: string]: number } = {};
    const now = new Date();
    for (let i = 6; i >= 0; i--) {
      const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const moisKey = date.toLocaleDateString('fr-FR', { month: 'short', year: 'numeric' });
      moisCounts[moisKey] = 0;
    }

    this.interventions.forEach(i => {
      const date = new Date(i.datePlanifiee);
      const moisKey = date.toLocaleDateString('fr-FR', { month: 'short', year: 'numeric' });
      if (moisCounts.hasOwnProperty(moisKey)) {
        moisCounts[moisKey]++;
      }
    });

    this.statsParMois = Object.keys(moisCounts).map(mois => ({
      label: mois,
      count: moisCounts[mois],
      color: '#007bff'
    }));

    // Interventions urgentes
    this.interventionsUrgentes = this.interventions
      .filter(i => i.priorite === 'URGENTE' || i.priorite === 'CRITIQUE')
      .slice(0, 10);

    // Interventions en retard (en attente depuis plus de 7 jours)
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
    this.interventionsEnRetard = this.interventions
      .filter(i => {
        const datePlanifiee = new Date(i.datePlanifiee);
        return i.etat === 'EN_ATTENTE' && datePlanifiee < sevenDaysAgo;
      })
      .slice(0, 10);
  }

  calculateBarHeight(value: number, maxValue: number): number {
    if (maxValue === 0) return 0;
    return (value / maxValue) * 100;
  }

  getMaxValue(data: ChartData[]): number {
    if (data.length === 0) return 1;
    return Math.max(...data.map(d => d.count), 1);
  }

  refresh(): void {
    this.loadInterventions();
  }
}
