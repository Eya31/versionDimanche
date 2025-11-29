import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { InterventionService } from '../../../../services/intervention.service';
import { Intervention } from '../../../../models/intervention.model';

@Component({
  selector: 'app-intervention-liste',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './intervention-liste.component.html',
  styleUrls: ['./intervention-liste.component.css']
})
export class InterventionListeComponent implements OnInit {
  interventions: Intervention[] = [];
  interventionsFiltrees: Intervention[] = [];
  paginatedInterventions: Intervention[] = [];

  // Filtres
  searchTerm: string = '';
  filtreEtat: string = '';
  filtrePriorite: string = '';
  dateDebut: string = '';
  dateFin: string = '';

  // Tri
  sortColumn: string = 'id';
  sortDirection: 'asc' | 'desc' = 'desc';

  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 25;
  totalPages: number = 1;

  // Stats
  statsEnCours: number = 0;
  statsEnAttente: number = 0;
  statsTerminee: number = 0;

  constructor(private interventionService: InterventionService) { }

  ngOnInit(): void {
    this.loadInterventions();
  }

  loadInterventions(): void {
    this.interventionService.getAllInterventions().subscribe({
      next: (data) => {
        this.interventions = data;
        this.calculateStats();
        this.applyFilters();
      },
      error: (err) => {
        console.error('Erreur chargement interventions:', err);
        alert('Erreur lors du chargement des interventions');
      }
    });
  }

  calculateStats(): void {
    this.statsEnCours = this.interventions.filter(i => i.etat === 'EN_COURS').length;
    this.statsEnAttente = this.interventions.filter(i => i.etat === 'EN_ATTENTE').length;
    this.statsTerminee = this.interventions.filter(i => i.etat === 'TERMINEE').length;
  }

  applyFilters(): void {
    let filtered = [...this.interventions];

    // Filtre de recherche
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(i =>
        i.id.toString().includes(term) ||
        i.description?.toLowerCase().includes(term) ||
        i.technicienId?.toString().includes(term)
      );
    }

    // Filtre par état
    if (this.filtreEtat) {
      filtered = filtered.filter(i => i.etat === this.filtreEtat);
    }

    // Filtre par priorité
    if (this.filtrePriorite) {
      filtered = filtered.filter(i => i.priorite === this.filtrePriorite);
    }

    // Filtre par date
    if (this.dateDebut) {
      filtered = filtered.filter(i =>
        new Date(i.datePlanifiee) >= new Date(this.dateDebut)
      );
    }

    if (this.dateFin) {
      filtered = filtered.filter(i =>
        new Date(i.datePlanifiee) <= new Date(this.dateFin)
      );
    }

    this.interventionsFiltrees = filtered;
    this.sortInterventions();
    this.currentPage = 1;
    this.updatePagination();
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.filtreEtat = '';
    this.filtrePriorite = '';
    this.dateDebut = '';
    this.dateFin = '';
    this.applyFilters();
  }

  sortBy(column: string): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.sortInterventions();
    this.updatePagination();
  }

  sortInterventions(): void {
    this.interventionsFiltrees.sort((a, b) => {
      let aValue: any = (a as any)[this.sortColumn];
      let bValue: any = (b as any)[this.sortColumn];

      // Gestion des dates
      if (this.sortColumn === 'datePlanifiee') {
        aValue = new Date(aValue).getTime();
        bValue = new Date(bValue).getTime();
      }

      if (aValue < bValue) return this.sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }

  // Pagination
  updatePagination(): void {
    this.totalPages = Math.ceil(this.interventionsFiltrees.length / this.itemsPerPage);
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.paginatedInterventions = this.interventionsFiltrees.slice(start, end);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updatePagination();
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePagination();
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.updatePagination();
  }

  onItemsPerPageChange(): void {
    this.currentPage = 1;
    this.updatePagination();
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxPages = 5;
    let start = Math.max(1, this.currentPage - Math.floor(maxPages / 2));
    let end = Math.min(this.totalPages, start + maxPages - 1);

    if (end - start < maxPages - 1) {
      start = Math.max(1, end - maxPages + 1);
    }

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  getStartIndex(): number {
    return (this.currentPage - 1) * this.itemsPerPage;
  }

  getEndIndex(): number {
    return Math.min(this.getStartIndex() + this.itemsPerPage, this.interventionsFiltrees.length);
  }

  // Helpers
  getEtatClass(etat: string): string {
    const classes: { [key: string]: string } = {
      'EN_COURS': 'badge-success',
      'EN_ATTENTE': 'badge-warning',
      'TERMINEE': 'badge-info',
      'ANNULEE': 'badge-danger'
    };
    return classes[etat] || 'badge-default';
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

  viewOnMap(intervention: Intervention): void {
    // Navigue vers la carte avec l'intervention sélectionnée
    // TODO: Implémenter la navigation avec paramètres
    alert(`Voir intervention #${intervention.id} sur la carte`);
  }
}
