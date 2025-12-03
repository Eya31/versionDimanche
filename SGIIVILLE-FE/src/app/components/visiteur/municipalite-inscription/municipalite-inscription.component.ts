import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PublicService, InscriptionMunicipalite } from '../../../services/public.service';

@Component({
  selector: 'app-municipalite-inscription',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './municipalite-inscription.component.html',
  styleUrl: './municipalite-inscription.component.css'
})
export class MunicipaliteInscriptionComponent {
  demande: InscriptionMunicipalite = {
    nom: '',
    adresse: '',
    email: '',
    numeroAdministratif: '',
    message: ''
  };

  submitted = false;
  success = false;
  error: string | null = null;
  loading = false;

  constructor(private publicService: PublicService) {}

  onSubmit(): void {
    if (!this.isFormValid()) {
      this.error = 'Veuillez remplir tous les champs obligatoires.';
      return;
    }

    this.loading = true;
    this.error = null;

    this.publicService.soumettreInscriptionMunicipalite(this.demande).subscribe({
      next: (response) => {
        this.success = true;
        this.submitted = true;
        this.loading = false;
        this.resetForm();
      },
      error: (err) => {
        console.error('Erreur lors de la soumission', err);
        this.error = 'Une erreur est survenue. Veuillez réessayer plus tard.';
        this.loading = false;
      }
    });
  }

  isFormValid(): boolean {
    return !!(
      this.demande.nom &&
      this.demande.adresse &&
      this.demande.email &&
      this.demande.numeroAdministratif &&
      this.demande.message
    );
  }

  resetForm(): void {
    this.demande = {
      nom: '',
      adresse: '',
      email: '',
      numeroAdministratif: '',
      message: ''
    };
  }
}

