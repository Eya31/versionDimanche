import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { InterventionService } from '../../../../services/intervention.service';
import { Intervention } from '../../../../models/intervention.model';
import * as L from 'leaflet';

@Component({
  selector: 'app-intervention-carte',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './intervention-carte.component.html',
  styleUrls: ['./intervention-carte.component.css']
})
export class InterventionCarteComponent implements OnInit, AfterViewInit, OnDestroy {
  interventions: Intervention[] = [];
  loading = false;
  private map?: L.Map;
  private markers: L.Marker[] = [];

  // Filtres
  filtreEtat: string = 'all';
  filtrePriorite: string = 'all';

  constructor(private interventionService: InterventionService) { }

  ngOnInit(): void {
    this.loadInterventions();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.initMap(), 100);
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  loadInterventions(): void {
    this.loading = true;
    this.interventionService.getAllInterventions().subscribe({
      next: (interventions) => {
        this.interventions = interventions;
        this.applyFilters();
        this.loading = false;
        setTimeout(() => this.updateMap(), 100);
      },
      error: (err) => {
        console.error('Erreur chargement interventions:', err);
        this.loading = false;
        alert('Erreur lors du chargement des interventions');
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.interventions];

    if (this.filtreEtat !== 'all') {
      filtered = filtered.filter(i => i.etat === this.filtreEtat);
    }

    if (this.filtrePriorite !== 'all') {
      filtered = filtered.filter(i => i.priorite === this.filtrePriorite);
    }

    this.interventions = filtered;
    this.updateMap();
  }

  initMap(): void {
    const mapElement = document.getElementById('interventionMap');
    if (!mapElement) return;

    if (this.map) {
      this.map.remove();
    }

    // Centre sur Tunis par défaut
    this.map = L.map('interventionMap').setView([36.8065, 10.1815], 12);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    this.updateMap();
  }

  updateMap(): void {
    if (!this.map) return;

    // Supprimer les anciens marqueurs
    this.markers.forEach(marker => marker.remove());
    this.markers = [];

    const interventionsAvecLocalisation = this.interventions.filter(i =>
      i.localisation && i.localisation.latitude && i.localisation.longitude
    );

    if (interventionsAvecLocalisation.length === 0) return;

    interventionsAvecLocalisation.forEach(intervention => {
      if (!intervention.localisation) return;

      const color = this.getColorByEtat(intervention.etat);
      const icon = L.divIcon({
        className: 'custom-marker',
        html: `<div style="background-color: ${color}; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
        iconSize: [20, 20],
        iconAnchor: [10, 10]
      });

      const marker = L.marker(
        [intervention.localisation.latitude, intervention.localisation.longitude],
        { icon }
      ).addTo(this.map!);

      const popupContent = `
        <div style="min-width: 200px;">
          <h4 style="margin: 0 0 10px 0; color: #2c3e50;">
            Intervention #${intervention.id}
          </h4>
          <p style="margin: 5px 0;"><strong>État:</strong> ${intervention.etat}</p>
          <p style="margin: 5px 0;"><strong>Priorité:</strong> ${intervention.priorite || 'N/A'}</p>
          <p style="margin: 5px 0;"><strong>Date:</strong> ${new Date(intervention.datePlanifiee).toLocaleDateString('fr-FR')}</p>
          ${intervention.description ? `<p style="margin: 5px 0;"><strong>Description:</strong> ${intervention.description.substring(0, 100)}${intervention.description.length > 100 ? '...' : ''}</p>` : ''}
          <a href="/admin/interventions/${intervention.id}" style="display: inline-block; margin-top: 10px; padding: 5px 10px; background: #007bff; color: white; text-decoration: none; border-radius: 4px;">Voir détails</a>
        </div>
      `;

      marker.bindPopup(popupContent);
      this.markers.push(marker);
    });

    // Ajuster la vue pour afficher tous les marqueurs
    if (interventionsAvecLocalisation.length > 0) {
      const bounds = L.latLngBounds(
        interventionsAvecLocalisation.map(i =>
          [i.localisation!.latitude, i.localisation!.longitude] as [number, number]
        )
      );
      this.map.fitBounds(bounds, { padding: [50, 50] });
    }
  }

  getColorByEtat(etat: string): string {
    const colors: { [key: string]: string } = {
      'EN_COURS': '#10b981',
      'EN_ATTENTE': '#f59e0b',
      'TERMINEE': '#3b82f6',
      'SUSPENDUE': '#ef4444'
    };
    return colors[etat] || '#6c757d';
  }

  resetFilters(): void {
    this.filtreEtat = 'all';
    this.filtrePriorite = 'all';
    this.loadInterventions();
  }
}
