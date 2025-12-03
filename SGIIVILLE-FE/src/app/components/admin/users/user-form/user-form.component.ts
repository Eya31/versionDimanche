import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AdminService, CreateTechnicienRequest, CreateChefServiceRequest } from '../../../../services/admin.service';
import { MainDOeuvreService } from '../../../../services/main-doeuvre.service';
import { CreateMainDOeuvreRequest, HabilitationDTO } from '../../../../models/main-doeuvre.model';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.css']
})
export class UserFormComponent {
  selectedRole: string = '';
  confirmPassword: string = '';
  competenceInput: string = '';
  habilitationInput: HabilitationDTO = {
    nom: '',
    dateObtention: '',
    dateExpiration: '',
    numeroCertificat: ''
  };

  formData: any = {
    nom: '',
    prenom: '',
    email: '',
    motDePasse: '',
    competences: [], // Pour technicien/chef service
    departement: '',
    // Champs spécifiques main d'œuvre
    cin: '',
    telephone: '',
    matricule: '',
    competence: '' // Compétence unique pour main d'œuvre
  };

  constructor(
    private adminService: AdminService,
    private mainDoeuvreService: MainDOeuvreService,
    private router: Router
  ) { }

  selectRole(role: string): void {
    this.selectedRole = role;
    this.resetForm();
  }

  resetForm(): void {
    this.formData = {
      nom: '',
      prenom: '',
      email: '',
      motDePasse: '',
      competences: [],
      departement: '',
      cin: '',
      telephone: '',
      matricule: '',
      competence: ''
    };
    this.confirmPassword = '';
    this.competenceInput = '';
  }

  addCompetence(): void {
    if (this.competenceInput.trim()) {
      if (!this.formData.competences) {
        this.formData.competences = [];
      }
      if (!this.formData.competences.includes(this.competenceInput.trim())) {
        this.formData.competences.push(this.competenceInput.trim());
      }
      this.competenceInput = '';
    }
  }

  removeCompetence(comp: string): void {
    const index = this.formData.competences.indexOf(comp);
    if (index > -1) {
      this.formData.competences.splice(index, 1);
    }
  }

  addHabilitation(): void {
    if (this.habilitationInput.nom.trim()) {
      if (!this.formData.habilitations) {
        this.formData.habilitations = [];
      }
      this.formData.habilitations.push({ ...this.habilitationInput });
      this.habilitationInput = {
        nom: '',
        dateObtention: '',
        dateExpiration: '',
        numeroCertificat: ''
      };
    }
  }

  removeHabilitation(index: number): void {
    this.formData.habilitations.splice(index, 1);
  }

  isFormValid(): boolean {
    const hasBasicInfo = this.formData.nom &&
      this.formData.email &&
      this.formData.motDePasse;

    const passwordsMatch = this.formData.motDePasse === this.confirmPassword;

    let roleSpecificValid = true;

    if (this.selectedRole === 'CHEF_SERVICE') {
      roleSpecificValid = !!this.formData.departement;
    } else if (this.selectedRole === 'MAIN_DOEUVRE') {
      roleSpecificValid = !!this.formData.cin && 
                         !!this.formData.telephone && 
                         (this.selectedRole === 'MAIN_DOEUVRE' ? !!this.formData.competence : this.formData.competences.length > 0);
    }

    return hasBasicInfo && passwordsMatch && roleSpecificValid;
  }

  onSubmit(): void {
    if (!this.isFormValid()) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    switch (this.selectedRole) {
      case 'TECHNICIEN':
        this.createTechnicien();
        break;
      case 'CHEF_SERVICE':
        this.createChefService();
        break;
      case 'MAIN_DOEUVRE':
        this.createMainDoeuvre();
        break;
      case 'CITOYEN':
      case 'ADMINISTRATEUR':
        alert('Création de ' + this.selectedRole + ' non implémentée côté backend');
        break;
    }
  }

  createTechnicien(): void {
    const request: CreateTechnicienRequest = {
      nom: this.formData.nom,
      email: this.formData.email,
      motDePasse: this.formData.motDePasse,
      competences: this.formData.competences || []
    };

    console.log('🔍 DEBUG - Données envoyées:', request);
    console.log('🔍 DEBUG - Compétences:', this.formData.competences);

    this.adminService.createTechnicien(request).subscribe({
      next: (response) => {
        console.log('✅ Réponse du serveur:', response);
        alert('✅ Technicien créé avec succès !');
        this.router.navigate(['/admin/users/techniciens']);
      },
      error: (err) => {
        console.error('❌ Erreur création technicien:', err);
        alert('❌ Erreur lors de la création du technicien');
      }
    });
  }

  createChefService(): void {
    const request: CreateChefServiceRequest = {
      nom: this.formData.nom,
      email: this.formData.email,
      motDePasse: this.formData.motDePasse,
      departement: this.formData.departement
    };

    this.adminService.createChefService(request).subscribe({
      next: () => {
        alert('✅ Chef de service créé avec succès !');
        this.router.navigate(['/admin/users/chefs']);
      },
      error: (err) => {
        console.error('Erreur création chef:', err);
        alert('❌ Erreur lors de la création du chef de service');
      }
    });
  }

  createMainDoeuvre(): void {
    const request: CreateMainDOeuvreRequest = {
      nom: this.formData.nom,
      prenom: this.formData.prenom || '',
      matricule: this.formData.matricule || '',
      cin: this.formData.cin,
      telephone: this.formData.telephone,
      email: this.formData.email,
      competence: this.formData.competence || ''
    };

    this.mainDoeuvreService.create(request, true).subscribe({
      next: (response) => {
        const message = response.message || 'Main d\'œuvre créée avec succès';
        const defaultPassword = response.defaultPassword || 'Vérifiez les logs';
        alert(`✅ ${message}\n\nMot de passe par défaut: ${defaultPassword}`);
        this.router.navigate(['/admin/main-doeuvre']);
      },
      error: (err) => {
        console.error('Erreur création main d\'œuvre:', err);
        // Essayer avec l'endpoint technicien en fallback
        if (err.status === 404 || err.status === 403 || err.status === 500) {
          this.mainDoeuvreService.create(request, false).subscribe({
            next: (response) => {
              const message = response.message || 'Main d\'œuvre créée avec succès';
              const defaultPassword = response.defaultPassword || 'Vérifiez les logs';
              alert(`✅ ${message}\n\nMot de passe par défaut: ${defaultPassword}`);
              this.router.navigate(['/admin/main-doeuvre']);
            },
            error: (err2) => {
              console.error('Erreur création main d\'œuvre (fallback):', err2);
              alert('❌ Erreur lors de la création de la main d\'œuvre');
            }
          });
        } else {
          alert('❌ Erreur lors de la création de la main d\'œuvre');
        }
      }
    });
  }
}
