import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TechnicienService } from '../../../../services/technicien.service';
import { Intervention } from '../../../../models/intervention.model';

@Component({
  selector: 'app-rapports-finaux',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './rapports-finaux.component.html',
  styleUrls: ['./rapports-finaux.component.css']
})
export class RapportsFinauxComponent implements OnInit {
  interventions: Intervention[] = [];
  interventionsFiltrees: Intervention[] = [];
  loading = false;

  // Filtres
  searchTerm: string = '';
  filtrePriorite: string = '';
  dateDebut: string = '';
  dateFin: string = '';

  // Stats
  totalTerminees: number = 0;

  constructor(private technicienService: TechnicienService) {}

  ngOnInit(): void {
    this.loadInterventionsTerminees();
  }

  loadInterventionsTerminees(): void {
    this.loading = true;
    this.technicienService.getMyInterventions({ etat: 'TERMINEE' }).subscribe({
      next: (data: Intervention[]) => {
        this.interventions = data || [];
        this.totalTerminees = this.interventions.length;
        this.applyFilters();
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Erreur chargement interventions terminées:', err);
        this.loading = false;
        alert('Erreur lors du chargement des interventions terminées');
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

    // Filtre par date
    if (this.dateDebut) {
      filtered = filtered.filter(i =>
        i.dateFin && new Date(i.dateFin) >= new Date(this.dateDebut)
      );
    }

    if (this.dateFin) {
      filtered = filtered.filter(i =>
        i.dateFin && new Date(i.dateFin) <= new Date(this.dateFin)
      );
    }

    this.interventionsFiltrees = filtered;
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.filtrePriorite = '';
    this.dateDebut = '';
    this.dateFin = '';
    this.applyFilters();
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

  hasRapport(intervention: Intervention): boolean {
    // Vérifier si l'intervention a un rapport final
    // On peut vérifier la présence de certains champs du rapport
    return intervention.etat === 'TERMINEE' && !!intervention.dateFin;
  }
}

