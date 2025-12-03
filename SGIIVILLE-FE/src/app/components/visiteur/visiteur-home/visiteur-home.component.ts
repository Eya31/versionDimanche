import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PublicService, PublicStats, DemandePublique } from '../../../services/public.service';

@Component({
  selector: 'app-visiteur-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './visiteur-home.component.html',
  styleUrl: './visiteur-home.component.css'
})
export class VisiteurHomeComponent implements OnInit {
  stats: PublicStats | null = null;
  demandesTerminees: DemandePublique[] = [];
  loading = true;

  constructor(private publicService: PublicService) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadDemandesTerminees();
  }

  loadStats(): void {
    this.publicService.getStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des statistiques', err);
      }
    });
  }

  loadDemandesTerminees(): void {
    this.publicService.getDemandesTerminees().subscribe({
      next: (demandes) => {
        // Limiter à 3 pour l'affichage sur la page d'accueil
        this.demandesTerminees = demandes.slice(0, 3);
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des demandes terminées', err);
        this.loading = false;
      }
    });
  }
}

