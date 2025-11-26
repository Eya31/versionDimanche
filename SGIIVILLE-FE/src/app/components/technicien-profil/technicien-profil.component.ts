import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { TechnicienService } from '../../services/technicien.service';
import { TechnicienProfil, UpdateProfilTechnicienRequest, StatistiquesTechnicien } from '../../models/technicien-profil.model';

@Component({
  selector: 'app-technicien-profil',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './technicien-profil.component.html',
  styleUrls: ['./technicien-profil.component.css']
})
export class TechnicienProfilComponent implements OnInit {
  profil: TechnicienProfil | null = null;
  statistiques: StatistiquesTechnicien | null = null;
  updateRequest: UpdateProfilTechnicienRequest = {};
  isEditing = false;

  competencesDisponibles = ['Électricité', 'Hydraulique', 'Mécanique', 'Plomberie', 'Maçonnerie'];
  habilitationsDisponibles = ['Électrique', 'CACES', 'Habilitation H0', 'Habilitation H1', 'Habilitation H2'];

  constructor(
    private technicienService: TechnicienService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProfil();
    this.loadStatistiques();
  }

  loadProfil(): void {
    this.technicienService.getProfil().subscribe({
      next: (data) => {
        this.profil = data;
        this.updateRequest = {
          nom: data.nom,
          prenom: data.prenom,
          email: data.email,
          telephone: data.telephone,
          competences: [...data.competences],
          habilitations: [...data.habilitations]
        };
      },
      error: (err) => console.error('Erreur:', err)
    });
  }

  loadStatistiques(): void {
    this.technicienService.getStatistiques().subscribe({
      next: (data) => this.statistiques = data,
      error: (err) => console.error('Erreur:', err)
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    if (!this.isEditing) {
      this.loadProfil();
    }
  }

  toggleCompetence(competence: string): void {
    if (!this.updateRequest.competences) {
      this.updateRequest.competences = [];
    }
    const index = this.updateRequest.competences.indexOf(competence);
    if (index > -1) {
      this.updateRequest.competences.splice(index, 1);
    } else {
      this.updateRequest.competences.push(competence);
    }
  }

  toggleHabilitation(habilitation: string): void {
    if (!this.updateRequest.habilitations) {
      this.updateRequest.habilitations = [];
    }
    const index = this.updateRequest.habilitations.indexOf(habilitation);
    if (index > -1) {
      this.updateRequest.habilitations.splice(index, 1);
    } else {
      this.updateRequest.habilitations.push(habilitation);
    }
  }

  updateProfil(): void {
    this.technicienService.updateProfil(this.updateRequest).subscribe({
      next: () => {
        alert('Profil mis à jour avec succès');
        this.isEditing = false;
        this.loadProfil();
      },
      error: (err) => alert('Erreur: ' + err.message)
    });
  }

  retour(): void {
    this.router.navigate(['/technicien']);
  }
}

