import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RegisterRequest, RoleType } from '../../models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerData: RegisterRequest = {
    nom: '',
    email: '',
    motDePasse: '',
    role: RoleType.CITOYEN,
    adresse: '',
    telephone: '',
    departement: '',
    prenom: '',
    matricule: '',
    cin: '',
    competence: ''
  };

  confirmPassword: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  roles = [
    { value: RoleType.CITOYEN, label: 'Citoyen' },
    { value: RoleType.TECHNICIEN, label: 'Technicien' },
    { value: RoleType.CHEF_SERVICE, label: 'Chef de Service' },
    { value: RoleType.ADMINISTRATEUR, label: 'Administrateur' },
    { value: RoleType.MAIN_DOEUVRE, label: 'Agent Main d\'Œuvre' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    // Reset messages
    this.errorMessage = '';
    this.successMessage = '';

    // Validation de base
    if (!this.registerData.nom?.trim() || !this.registerData.email?.trim() || !this.registerData.motDePasse) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    if (this.registerData.motDePasse !== this.confirmPassword) {
      this.errorMessage = 'Les mots de passe ne correspondent pas';
      return;
    }

    if (this.registerData.motDePasse.length < 6) {
      this.errorMessage = 'Le mot de passe doit contenir au moins 6 caractères';
      return;
    }

    // Validation email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.registerData.email)) {
      this.errorMessage = 'Format d\'email invalide';
      return;
    }

    // Validation spécifique par rôle
    if (this.registerData.role === RoleType.CITOYEN) {
      if (!this.registerData.adresse?.trim() || !this.registerData.telephone?.trim()) {
        this.errorMessage = 'Adresse et téléphone sont obligatoires pour un citoyen';
        return;
      }
    }

    if (this.registerData.role === RoleType.CHEF_SERVICE || this.registerData.role === RoleType.TECHNICIEN) {
      if (!this.registerData.departement?.trim()) {
        this.errorMessage = 'Le département est obligatoire';
        return;
      }
    }

    // Validation spécifique pour MAIN_DOEUVRE
    if (this.registerData.role === RoleType.MAIN_DOEUVRE) {
      if (!this.registerData.nom?.trim() || !this.registerData.prenom?.trim() ||
          !this.registerData.cin?.trim() || !this.registerData.telephone?.trim() ||
          !this.registerData.competence?.trim()) {
        this.errorMessage = 'Tous les champs sont obligatoires pour un agent main d\'œuvre';
        return;
      }
    }

    this.isLoading = true;

    this.authService.register(this.registerData).subscribe({
      next: (response) => {
        console.log('✅ Inscription réussie', response);
        this.successMessage = 'Inscription réussie! Redirection...';
        this.isLoading = false;

        setTimeout(() => {
          this.router.navigate(['/home']);
        }, 2000);
      },
      error: (error) => {
        console.error('❌ Erreur d\'inscription', error);
        this.errorMessage = this.getErrorMessage(error);
        this.isLoading = false;
      }
    });
  }

  private getErrorMessage(error: any): string {
    if (error.status === 0) {
      return 'Impossible de se connecter au serveur. Vérifiez que le serveur est démarré.';
    }

    if (error.error?.message) {
      return error.error.message;
    }

    if (error.message) {
      return error.message;
    }

    return 'Erreur lors de l\'inscription. Veuillez réessayer.';
  }

  get isCitoyen(): boolean {
    return this.registerData.role === RoleType.CITOYEN;
  }

  get isChefService(): boolean {
    return this.registerData.role === RoleType.CHEF_SERVICE;
  }

  get isTechnicien(): boolean {
    return this.registerData.role === RoleType.TECHNICIEN;
  }

  get isMainDoeuvre(): boolean {
    return this.registerData.role === RoleType.MAIN_DOEUVRE;
  }

  onRoleChange(): void {
    // Réinitialiser les champs spécifiques quand le rôle change
    this.registerData.adresse = '';
    this.registerData.telephone = '';
    this.registerData.departement = '';
    this.registerData.prenom = '';
    this.registerData.matricule = '';
    this.registerData.cin = '';
    this.registerData.competence = '';
  }
}
