import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MainDOeuvreService } from '../../../../services/main-doeuvre.service';
import { CreateMainDOeuvreRequest } from '../../../../models/main-doeuvre.model';

@Component({
  selector: 'app-main-doeuvre-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './main-doeuvre-form.component.html',
  styleUrls: ['./main-doeuvre-form.component.css']
})
export class MainDoeuvreFormComponent implements OnInit {
  formData: CreateMainDOeuvreRequest = {
    nom: '',
    prenom: '',
    matricule: '',
    cin: '',
    telephone: '',
    email: '',
    competence: ''
  };

  competencesDisponibles = ['Électricité', 'Hydraulique', 'Mécanique', 'Plomberie', 'Maçonnerie', 'Peinture', 'Télécom'];

  loading = false;
  saving = false;

  constructor(
    private mainDoeuvreService: MainDOeuvreService,
    private router: Router
  ) { }

  ngOnInit(): void {
  }

  // Les méthodes addCompetence, removeCompetence, addHabilitation, removeHabilitation
  // ont été supprimées car le modèle utilise maintenant une compétence unique (string)

  isFormValid(): boolean {
    return !!(
      this.formData.nom &&
      this.formData.cin &&
      this.formData.telephone &&
      this.formData.competence
    );
  }

  onSubmit(): void {
    if (!this.isFormValid()) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.saving = true;
    this.mainDoeuvreService.create(this.formData, true).subscribe({
      next: (response) => {
        this.saving = false;
        alert('Main d\'œuvre créée avec succès !');
        this.router.navigate(['/admin/main-doeuvre']);
      },
      error: (err) => {
        console.error('Erreur création main d\'œuvre:', err);
        // Si l'endpoint admin n'existe pas, essayer avec l'endpoint technicien
        if (err.status === 404 || err.status === 403) {
          this.mainDoeuvreService.create(this.formData, false).subscribe({
            next: (response) => {
              this.saving = false;
              alert('Main d\'œuvre créée avec succès !');
              this.router.navigate(['/admin/main-doeuvre']);
            },
            error: (err2) => {
              console.error('Erreur création main d\'œuvre (fallback):', err2);
              this.saving = false;
              alert('Erreur lors de la création de la main d\'œuvre. Vérifiez que les endpoints admin sont configurés.');
            }
          });
        } else {
          this.saving = false;
          alert('Erreur lors de la création de la main d\'œuvre');
        }
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/admin/main-doeuvre']);
  }
}
