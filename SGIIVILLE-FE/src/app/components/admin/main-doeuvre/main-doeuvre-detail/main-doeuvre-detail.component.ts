import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { MainDOeuvreService } from '../../../../services/main-doeuvre.service';
import { MainDOeuvre, HistoriqueInterventionDTO } from '../../../../models/main-doeuvre.model';

@Component({
  selector: 'app-main-doeuvre-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './main-doeuvre-detail.component.html',
  styleUrls: ['./main-doeuvre-detail.component.css']
})
export class MainDoeuvreDetailComponent implements OnInit {
  agent: MainDOeuvre | null = null;
  historique: HistoriqueInterventionDTO[] = [];
  loading = false;
  loadingHistorique = false;

  constructor(
    private mainDoeuvreService: MainDOeuvreService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const id = +params['id'];
      if (id) {
        this.loadAgent(id);
        this.loadHistorique(id);
      }
    });
  }

  loadAgent(id: number): void {
    this.loading = true;
    this.mainDoeuvreService.getById(id, true).subscribe({
      next: (agent) => {
        this.agent = agent;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement agent:', err);
        // Si l'endpoint admin n'existe pas, essayer avec l'endpoint technicien
        if (err.status === 404 || err.status === 403) {
          this.mainDoeuvreService.getById(id, false).subscribe({
            next: (agent) => {
              this.agent = agent;
              this.loading = false;
            },
            error: (err2) => {
              console.error('Erreur chargement agent (fallback):', err2);
              this.loading = false;
              alert('Erreur lors du chargement de l\'agent. Vérifiez que les endpoints admin sont configurés.');
            }
          });
        } else {
          this.loading = false;
          alert('Erreur lors du chargement de l\'agent');
        }
      }
    });
  }

  loadHistorique(id: number): void {
    this.loadingHistorique = true;
    this.mainDoeuvreService.getHistorique(id).subscribe({
      next: (historique) => {
        this.historique = historique;
        this.loadingHistorique = false;
      },
      error: (err) => {
        console.error('Erreur chargement historique:', err);
        this.loadingHistorique = false;
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

  getCompetencesString(competence: string | undefined): string {
    if (!competence) return 'Aucune compétence';
    return competence;
  }

  goBack(): void {
    this.router.navigate(['/admin/main-doeuvre']);
  }
}
