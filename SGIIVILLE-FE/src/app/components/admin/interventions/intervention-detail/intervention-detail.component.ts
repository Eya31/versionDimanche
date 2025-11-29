import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { InterventionService } from '../../../../services/intervention.service';
import { Intervention } from '../../../../models/intervention.model';

@Component({
  selector: 'app-intervention-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './intervention-detail.component.html',
  styleUrls: ['./intervention-detail.component.css']
})
export class InterventionDetailComponent implements OnInit {
  intervention: Intervention | null = null;
  loading = false;

  constructor(
    private interventionService: InterventionService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const id = +params['id'];
      if (id) {
        this.loadIntervention(id);
      }
    });
  }

  loadIntervention(id: number): void {
    this.loading = true;
    this.interventionService.getAllInterventions().subscribe({
      next: (interventions) => {
        this.intervention = interventions.find(i => i.id === id) || null;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement intervention:', err);
        this.loading = false;
        alert('Erreur lors du chargement de l\'intervention');
      }
    });
  }

  getEtatClass(etat: string): string {
    const classes: { [key: string]: string } = {
      'EN_COURS': 'badge-success',
      'EN_ATTENTE': 'badge-warning',
      'TERMINEE': 'badge-info',
      'SUSPENDUE': 'badge-danger'
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

  goBack(): void {
    this.router.navigate(['/admin/interventions/liste']);
  }
}
