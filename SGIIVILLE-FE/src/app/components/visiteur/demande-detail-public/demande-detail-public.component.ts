import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { PublicService, DemandePublique } from '../../../services/public.service';

@Component({
  selector: 'app-demande-detail-public',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './demande-detail-public.component.html',
  styleUrl: './demande-detail-public.component.css'
})
export class DemandeDetailPublicComponent implements OnInit {
  demande: DemandePublique | null = null;
  loading = true;
  error: string | null = null;

  constructor(
    private publicService: PublicService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadDemande(parseInt(id));
    }
  }

  loadDemande(id: number): void {
    this.loading = true;
    this.publicService.getDemandePublique(id).subscribe({
      next: (demande) => {
        this.demande = demande;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement de la demande', err);
        this.error = 'Impossible de charger cette demande. Elle n\'est peut-être pas accessible publiquement.';
        this.loading = false;
      }
    });
  }
}

