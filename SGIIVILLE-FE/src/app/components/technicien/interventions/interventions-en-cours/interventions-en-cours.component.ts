import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TechnicienService } from '../../../../services/technicien.service';
import { AuthService } from '../../../../services/auth.service';
import { Intervention } from '../../../../models/intervention.model';

@Component({
  selector: 'app-interventions-en-cours',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './interventions-en-cours.component.html',
  styleUrls: ['./interventions-en-cours.component.css']
})
export class InterventionsEnCoursComponent implements OnInit {
  interventions: Intervention[] = [];
  interventionsFiltrees: Intervention[] = [];
  loading = false;

  // Filtres
  searchTerm: string = '';
  filtrePriorite: string = '';

  // Stats
  totalEnCours: number = 0;

  constructor(
    private technicienService: TechnicienService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadInterventionsEnCours();
  }

  loadInterventionsEnCours(): void {
    this.loading = true;
    this.technicienService.getMyInterventions({ etat: 'EN_COURS' }).subscribe({
      next: (data: Intervention[]) => {
        this.interventions = data || [];
        this.totalEnCours = this.interventions.length;
        this.applyFilters();
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Erreur chargement interventions en cours:', err);
        this.loading = false;
        alert('Erreur lors du chargement des interventions en cours');
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.interventions];

    // Filtre de recherche
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(i =>
        i.id.toString().includes(term) ||
        i.description?.toLowerCase().includes(term) ||
        i.typeIntervention?.toLowerCase().includes(term)
      );
    }

    // Filtre par priorité
    if (this.filtrePriorite) {
      filtered = filtered.filter(i => i.priorite === this.filtrePriorite);
    }

    this.interventionsFiltrees = filtered;
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.filtrePriorite = '';
    this.applyFilters();
  }

  getEtatClass(etat: string): string {
    return 'badge-success';
  }

  getPrioriteClass(priorite: string): string {
    const classes: { [key: string]: string } = {
      'URGENTE': 'badge-danger',
      'CRITIQUE': 'badge-warning',
      'NORMALE': 'badge-info',
      'PLANIFIEE': 'badge-secondary'
    };
    return classes[priorite] || 'badge-default';
  }
}

