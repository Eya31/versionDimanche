import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PublicService, DemandePublique } from '../../../services/public.service';

@Component({
  selector: 'app-demandes-terminees',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './demandes-terminees.component.html',
  styleUrl: './demandes-terminees.component.css'
})
export class DemandesTermineesComponent implements OnInit {
  demandes: DemandePublique[] = [];
  demandesFiltrees: DemandePublique[] = [];
  loading = true;
  
  // Filtres
  categoryFilter: string = '';
  dateDebutFilter: string = '';
  dateFinFilter: string = '';
  
  // Catégories disponibles
  categories: string[] = [];

  constructor(private publicService: PublicService) {}

  ngOnInit(): void {
    this.loadDemandes();
  }

  loadDemandes(): void {
    this.loading = true;
    this.publicService.getDemandesTerminees().subscribe({
      next: (demandes) => {
        this.demandes = demandes;
        this.demandesFiltrees = demandes;
        this.extractCategories();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des demandes', err);
        this.loading = false;
      }
    });
  }

  extractCategories(): void {
    const cats = new Set<string>();
    this.demandes.forEach(d => {
      if (d.category) {
        cats.add(d.category);
      }
    });
    this.categories = Array.from(cats).sort();
  }

  applyFilters(): void {
    this.loading = true;
    const filters: any = {};
    
    if (this.categoryFilter) {
      filters.category = this.categoryFilter;
    }
    if (this.dateDebutFilter) {
      filters.dateDebut = this.dateDebutFilter;
    }
    if (this.dateFinFilter) {
      filters.dateFin = this.dateFinFilter;
    }

    this.publicService.getDemandesTerminees(filters).subscribe({
      next: (demandes) => {
        this.demandesFiltrees = demandes;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du filtrage', err);
        this.loading = false;
      }
    });
  }

  clearFilters(): void {
    this.categoryFilter = '';
    this.dateDebutFilter = '';
    this.dateFinFilter = '';
    this.demandesFiltrees = this.demandes;
  }
}

