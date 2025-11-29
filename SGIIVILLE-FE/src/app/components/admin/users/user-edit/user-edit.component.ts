import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AdminService } from '../../../../services/admin.service';

@Component({
  selector: 'app-user-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './user-edit.component.html',
  styleUrls: ['./user-edit.component.css']
})
export class UserEditComponent implements OnInit {
  userId: number | null = null;
  user: any = null;
  loading = false;
  saving = false;
  competenceInput: string = '';

  formData: any = {
    nom: '',
    email: '',
    motDePasse: '',
    competences: [],
    departement: ''
  };

  constructor(
    private adminService: AdminService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.userId = +params['id'];
      if (this.userId) {
        this.loadUser();
      }
    });
  }

  loadUser(): void {
    this.loading = true;
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.user = users.find(u => u.id === this.userId);
        if (this.user) {
          this.formData = {
            nom: this.user.nom || '',
            email: this.user.email || '',
            motDePasse: '',
            competences: this.user.competences || [],
            departement: this.user.departement || ''
          };
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement utilisateur:', err);
        this.loading = false;
        alert('Erreur lors du chargement de l\'utilisateur');
      }
    });
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

  onSubmit(): void {
    if (!this.formData.nom || !this.formData.email) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.saving = true;
    // TODO: Implémenter la mise à jour via l'API
    setTimeout(() => {
      this.saving = false;
      alert('Utilisateur mis à jour avec succès');
      this.router.navigate(['/admin/users']);
    }, 1000);
  }

  cancel(): void {
    this.router.navigate(['/admin/users']);
  }

  getRoleLabel(role: string): string {
    const labels: { [key: string]: string } = {
      'ADMINISTRATEUR': 'Administrateur',
      'CHEF_SERVICE': 'Chef de service',
      'TECHNICIEN': 'Technicien',
      'CITOYEN': 'Citoyen',
      'MAIN_DOEUVRE': 'Main d\'œuvre'
    };
    return labels[role] || role;
  }
}
