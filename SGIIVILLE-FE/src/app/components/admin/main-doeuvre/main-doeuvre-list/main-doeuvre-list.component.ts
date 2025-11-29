import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MainDOeuvreService } from '../../../../services/main-doeuvre.service';
import { MainDOeuvre } from '../../../../models/main-doeuvre.model';

@Component({
  selector: 'app-main-doeuvre-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './main-doeuvre-list.component.html',
  styleUrls: ['./main-doeuvre-list.component.css']
})
export class MainDoeuvreListComponent implements OnInit {
  mainDoeuvre: MainDOeuvre[] = [];
  mainDoeuvreFiltres: MainDOeuvre[] = [];
  loading = false;

  // Filtres
  searchTerm: string = '';
  filtreDisponibilite: string = 'all';
  filtreCompetence: string = '';

  constructor(private mainDoeuvreService: MainDOeuvreService) { }

  ngOnInit(): void {
    this.loadMainDoeuvre();
  }

  loadMainDoeuvre(): void {
    this.loading = true;
    // Essayer d'abord l'endpoint admin
    this.mainDoeuvreService.getAll(undefined, true).subscribe({
      next: (data) => {
        this.mainDoeuvre = data || [];
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement main d\'œuvre (admin):', err);
        // Si erreur 500 ou autre, essayer avec l'endpoint technicien comme fallback
        console.log('Tentative avec endpoint technicien...');
        this.mainDoeuvreService.getAll(undefined, false).subscribe({
          next: (data) => {
            this.mainDoeuvre = data || [];
            this.applyFilters();
            this.loading = false;
            console.log('Chargement réussi via endpoint technicien');
          },
          error: (err2) => {
            console.error('Erreur chargement main d\'œuvre (technicien aussi):', err2);
            this.loading = false;
            // Afficher une liste vide au lieu d'une erreur
            this.mainDoeuvre = [];
            this.applyFilters();
            alert('Aucune main d\'œuvre trouvée. Le fichier XML est peut-être vide ou n\'existe pas encore.');
          }
        });
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.mainDoeuvre];

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(m =>
        m.nom?.toLowerCase().includes(term) ||
        m.prenom?.toLowerCase().includes(term) ||
        m.cin?.toLowerCase().includes(term) ||
        m.telephone?.includes(term) ||
        m.email?.toLowerCase().includes(term)
      );
    }

    if (this.filtreDisponibilite !== 'all') {
      filtered = filtered.filter(m => m.disponibilite === this.filtreDisponibilite);
    }

    if (this.filtreCompetence) {
      const competence = this.filtreCompetence.toLowerCase();
      filtered = filtered.filter(m =>
        m.competences?.some(c => c.toLowerCase().includes(competence))
      );
    }

    this.mainDoeuvreFiltres = filtered;
  }

  deleteMainDoeuvre(mainDoeuvre: MainDOeuvre): void {
    if (!confirm(`Êtes-vous sûr de vouloir archiver ${mainDoeuvre.nom} ?`)) {
      return;
    }

    this.mainDoeuvreService.archiver(mainDoeuvre.id, true).subscribe({
      next: () => {
        alert('Main d\'œuvre archivée avec succès');
        this.loadMainDoeuvre();
      },
      error: (err) => {
        console.error('Erreur archivage:', err);
        // Si l'endpoint admin n'existe pas, essayer avec l'endpoint technicien
        if (err.status === 404 || err.status === 403) {
          this.mainDoeuvreService.archiver(mainDoeuvre.id, false).subscribe({
            next: () => {
              alert('Main d\'œuvre archivée avec succès');
              this.loadMainDoeuvre();
            },
            error: (err2) => {
              console.error('Erreur archivage (fallback):', err2);
              alert('Erreur lors de l\'archivage. Vérifiez que les endpoints admin sont configurés.');
            }
          });
        } else {
          alert('Erreur lors de l\'archivage');
        }
      }
    });
  }

  getDisponibiliteClass(disponibilite: string): string {
    const classes: { [key: string]: string } = {
      'DISPONIBLE': 'badge-success',
      'OCCUPE': 'badge-warning',
      'CONFLIT': 'badge-danger',
      'EN_CONGE': 'badge-info',
      'ABSENT': 'badge-secondary',
      'HORS_HABILITATION': 'badge-danger',
      'ARCHIVE': 'badge-dark',
      'DESACTIVE': 'badge-dark'
    };
    return classes[disponibilite] || 'badge-default';
  }

  getDisponibiliteLabel(disponibilite: string): string {
    const labels: { [key: string]: string } = {
      'DISPONIBLE': 'Disponible',
      'OCCUPE': 'Occupé',
      'CONFLIT': 'Conflit',
      'EN_CONGE': 'En congé',
      'ABSENT': 'Absent',
      'HORS_HABILITATION': 'Hors habilitation',
      'ARCHIVE': 'Archivé',
      'DESACTIVE': 'Désactivé'
    };
    return labels[disponibilite] || disponibilite;
  }

  getCompetencesString(competences: string[]): string {
    if (!competences || competences.length === 0) return 'Aucune compétence';
    return competences.join(', ');
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.filtreDisponibilite = 'all';
    this.filtreCompetence = '';
    this.applyFilters();
  }
}
