import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InterventionService } from '../../../../services/intervention.service';
import { TechnicienListService } from '../../../../services/technicien-list.service';
import { AdminService } from '../../../../services/admin.service';
import { Intervention } from '../../../../models/intervention.model';
import { Technicien } from '../../../../models/technicien.model';

interface ChartData {
  label: string;
  value: number;
  color: string;
  percentage?: number;
}

interface PerformanceTechnicien {
  technicienId: number;
  nom: string;
  interventionsTerminees: number;
  dureeMoyenne: number;
  tauxReussite: number;
  score: number;
}

interface ChargeService {
  serviceId: number;
  serviceName: string;
  interventions: number;
  dureeMoyenne: number;
  charge: number;
}

@Component({
  selector: 'app-analytics-kpis',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './analytics-kpis.component.html',
  styleUrls: ['./analytics-kpis.component.css']
})
export class AnalyticsKpisComponent implements OnInit {
  loading = false;
  
  // Données brutes
  interventions: Intervention[] = [];
  techniciens: Technicien[] = [];
  
  // Graphiques
  interventionsParType: ChartData[] = [];
  dureeMoyenneResolution: number = 0;
  performanceTechniciens: PerformanceTechnicien[] = [];
  chargesParService: ChargeService[] = [];
  evolutionMensuelle: ChartData[] = [];
  
  // KPIs
  totalInterventions = 0;
  interventionsTerminees = 0;
  dureeMoyenneGlobale = 0;
  techniciensActifs = 0;
  
  // Filtres
  selectedPeriod: '7j' | '30j' | '90j' | '12m' = '30j';
  selectedService: string = 'TOUS';

  constructor(
    private interventionService: InterventionService,
    private technicienService: TechnicienListService,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    
    Promise.all([
      this.loadInterventions(),
      this.loadTechniciens()
    ]).finally(() => {
      this.calculateAllStats();
      this.loading = false;
    });
  }

  loadInterventions(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.interventionService.getAllInterventions().subscribe({
        next: (data) => {
          this.interventions = data || [];
          this.totalInterventions = this.interventions.length;
          this.interventionsTerminees = this.interventions.filter(i => i.etat === 'TERMINEE').length;
          resolve();
        },
        error: (err) => {
          console.error('Erreur chargement interventions:', err);
          this.interventions = [];
          resolve();
        }
      });
    });
  }

  loadTechniciens(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.technicienService.getAllTechniciens().subscribe({
        next: (data) => {
          this.techniciens = data || [];
          resolve();
        },
        error: (err) => {
          console.error('Erreur chargement techniciens:', err);
          this.techniciens = [];
          resolve();
        }
      });
    });
  }

  calculateAllStats(): void {
    this.calculateInterventionsParType();
    this.calculateDureeMoyenneResolution();
    this.calculatePerformanceTechniciens();
    this.calculateChargesParService();
    this.calculateEvolutionMensuelle();
  }

  calculateInterventionsParType(): void {
    const typeCounts: { [key: string]: number } = {};
    
    this.interventions.forEach(intervention => {
      const type = intervention.typeIntervention || 'Non spécifié';
      typeCounts[type] = (typeCounts[type] || 0) + 1;
    });

    const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4'];
    let colorIndex = 0;

    this.interventionsParType = Object.keys(typeCounts).map(type => {
      const value = typeCounts[type];
      const percentage = this.totalInterventions > 0 
        ? Math.round((value / this.totalInterventions) * 100) 
        : 0;
      
      return {
        label: type,
        value: value,
        color: colors[colorIndex++ % colors.length],
        percentage: percentage
      };
    }).sort((a, b) => b.value - a.value);
  }

  calculateDureeMoyenneResolution(): void {
    const terminees = this.interventions.filter(i => 
      i.etat === 'TERMINEE' && 
      i.dateDebut && 
      i.dateFin
    );

    if (terminees.length === 0) {
      this.dureeMoyenneResolution = 0;
      return;
    }

    const durees = terminees.map(intervention => {
      const debut = new Date(intervention.dateDebut!);
      const fin = new Date(intervention.dateFin!);
      const diffMs = fin.getTime() - debut.getTime();
      return diffMs / (1000 * 60 * 60); // Convertir en heures
    });

    this.dureeMoyenneResolution = durees.reduce((sum, d) => sum + d, 0) / durees.length;
    this.dureeMoyenneGlobale = this.dureeMoyenneResolution;
  }

  calculatePerformanceTechniciens(): void {
    const performanceMap: Map<number, {
      nom: string;
      terminees: number;
      durees: number[];
      total: number;
    }> = new Map();

    // Initialiser avec tous les techniciens
    this.techniciens.forEach(tech => {
      performanceMap.set(tech.id, {
        nom: tech.nom,
        terminees: 0,
        durees: [],
        total: 0
      });
    });

    // Calculer les stats par technicien
    this.interventions.forEach(intervention => {
      if (intervention.technicienId) {
        const stats = performanceMap.get(intervention.technicienId);
        if (stats) {
          stats.total++;
          if (intervention.etat === 'TERMINEE') {
            stats.terminees++;
            if (intervention.dateDebut && intervention.dateFin) {
              const debut = new Date(intervention.dateDebut);
              const fin = new Date(intervention.dateFin);
              const duree = (fin.getTime() - debut.getTime()) / (1000 * 60 * 60); // heures
              stats.durees.push(duree);
            }
          }
        }
      }
    });

    // Convertir en tableau de PerformanceTechnicien
    this.performanceTechniciens = Array.from(performanceMap.entries())
      .map(([technicienId, stats]) => {
        const dureeMoyenne = stats.durees.length > 0
          ? stats.durees.reduce((sum, d) => sum + d, 0) / stats.durees.length
          : 0;
        
        const tauxReussite = stats.total > 0
          ? Math.round((stats.terminees / stats.total) * 100)
          : 0;

        // Score de performance (combinaison de taux de réussite et vitesse)
        const score = Math.round((tauxReussite * 0.7) + ((100 - Math.min(dureeMoyenne / 24 * 10, 100)) * 0.3));

        return {
          technicienId,
          nom: stats.nom,
          interventionsTerminees: stats.terminees,
          dureeMoyenne,
          tauxReussite,
          score
        };
      })
      .filter(p => {
        const hasInterventions = this.interventions.some(i => i.technicienId === p.technicienId);
        return p.interventionsTerminees > 0 || hasInterventions;
      })
      .sort((a, b) => b.score - a.score)
      .slice(0, 10); // Top 10

    this.techniciensActifs = this.performanceTechniciens.filter(p => p.interventionsTerminees > 0).length;
  }

  calculateChargesParService(): void {
    const serviceMap: Map<number, {
      serviceName: string;
      interventions: number;
      durees: number[];
    }> = new Map();

    this.interventions.forEach(intervention => {
      const serviceId = intervention.chefServiceId || 0;
      const serviceName = `Service #${serviceId}`;
      
      if (!serviceMap.has(serviceId)) {
        serviceMap.set(serviceId, {
          serviceName,
          interventions: 0,
          durees: []
        });
      }

      const service = serviceMap.get(serviceId)!;
      service.interventions++;

      if (intervention.dateDebut && intervention.dateFin) {
        const debut = new Date(intervention.dateDebut);
        const fin = new Date(intervention.dateFin);
        const duree = (fin.getTime() - debut.getTime()) / (1000 * 60 * 60); // heures
        service.durees.push(duree);
      }
    });

    this.chargesParService = Array.from(serviceMap.entries())
      .map(([serviceId, service]) => {
        const dureeMoyenne = service.durees.length > 0
          ? service.durees.reduce((sum, d) => sum + d, 0) / service.durees.length
          : 0;
        
        // Charge = nombre d'interventions * durée moyenne
        const charge = service.interventions * dureeMoyenne;

        return {
          serviceId,
          serviceName: service.serviceName,
          interventions: service.interventions,
          dureeMoyenne,
          charge
        };
      })
      .sort((a, b) => b.charge - a.charge);
  }

  calculateEvolutionMensuelle(): void {
    const moisCounts: { [key: string]: number } = {};
    const now = new Date();
    const monthsToShow = this.selectedPeriod === '12m' ? 12 : 
                        this.selectedPeriod === '90j' ? 3 : 
                        this.selectedPeriod === '30j' ? 1 : 1;
    
    // Initialiser les mois
    for (let i = monthsToShow - 1; i >= 0; i--) {
      const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const moisKey = date.toLocaleDateString('fr-FR', { month: 'short', year: 'numeric' });
      moisCounts[moisKey] = 0;
    }

    // Compter les interventions par mois
    this.interventions.forEach(intervention => {
      const date = new Date(intervention.datePlanifiee);
      const moisKey = date.toLocaleDateString('fr-FR', { month: 'short', year: 'numeric' });
      if (moisCounts.hasOwnProperty(moisKey)) {
        moisCounts[moisKey]++;
      }
    });

    this.evolutionMensuelle = Object.keys(moisCounts).map(mois => ({
      label: mois,
      value: moisCounts[mois],
      color: '#3b82f6'
    }));
  }

  // Utilitaires pour les graphiques
  calculateBarHeight(value: number, maxValue: number): number {
    if (maxValue === 0) return 0;
    return (value / maxValue) * 100;
  }

  getMaxValue(data: ChartData[]): number {
    if (data.length === 0) return 1;
    return Math.max(...data.map(d => d.value), 1);
  }

  getMaxCharge(): number {
    if (this.chargesParService.length === 0) return 1;
    return Math.max(...this.chargesParService.map(s => s.charge), 1);
  }

  getMaxPerformance(): number {
    if (this.performanceTechniciens.length === 0) return 100;
    return Math.max(...this.performanceTechniciens.map(p => p.score), 100);
  }

  formatDuree(heures: number): string {
    if (heures < 1) {
      return `${Math.round(heures * 60)} min`;
    } else if (heures < 24) {
      return `${heures.toFixed(1)} h`;
    } else {
      const jours = Math.floor(heures / 24);
      const heuresRestantes = Math.round(heures % 24);
      return `${jours}j ${heuresRestantes}h`;
    }
  }

  getPerformanceColor(score: number): string {
    if (score >= 80) return '#10b981';
    if (score >= 60) return '#f59e0b';
    return '#ef4444';
  }

  getChargeColor(charge: number, maxCharge: number): string {
    const percentage = (charge / maxCharge) * 100;
    if (percentage >= 80) return '#ef4444';
    if (percentage >= 50) return '#f59e0b';
    return '#10b981';
  }

  getLinePoints(): string {
    if (this.evolutionMensuelle.length < 2) return '';
    
    const points: string[] = [];
    const maxValue = this.getMaxValue(this.evolutionMensuelle);
    const stepX = 100 / (this.evolutionMensuelle.length - 1);
    
    this.evolutionMensuelle.forEach((stat, index) => {
      const x = index * stepX;
      const y = 100 - (maxValue > 0 ? (stat.value / maxValue) * 100 : 0);
      points.push(`${x},${y}`);
    });
    
    return points.join(' ');
  }

  onPeriodChange(): void {
    this.calculateEvolutionMensuelle();
  }

  refresh(): void {
    this.loadData();
  }

  // Exposer Math pour le template
  Math = Math;
}

