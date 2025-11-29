import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../../../services/admin.service';

@Component({
  selector: 'app-users-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.css']
})
export class UsersListComponent implements OnInit {
  users: any[] = [];
  usersFiltres: any[] = [];
  searchTerm: string = '';
  filtreRole: string = '';

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.applyFilters();
      },
      error: (err) => {
        console.error('Erreur chargement utilisateurs:', err);
        alert('Erreur lors du chargement des utilisateurs');
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.users];

    // Filtre par recherche
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(u =>
        u.nom?.toLowerCase().includes(term) ||
        u.email?.toLowerCase().includes(term)
      );
    }

    // Filtre par rôle
    if (this.filtreRole) {
      filtered = filtered.filter(u => u.role === this.filtreRole);
    }

    this.usersFiltres = filtered;
  }

  filterByRole(role: string): void {
    this.filtreRole = role;
    this.applyFilters();
  }

  countByRole(role: string): number {
    return this.users.filter(u => u.role === role).length;
  }

  getRoleClass(role: string): string {
    const classes: { [key: string]: string } = {
      'ADMINISTRATEUR': 'badge-danger',
      'CHEF_SERVICE': 'badge-info',
      'TECHNICIEN': 'badge-success',
      'CITOYEN': 'badge-secondary',
      'MAIN_DOEUVRE': 'badge-warning'
    };
    return classes[role] || 'badge-default';
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

  deleteUser(user: any): void {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer ${user.nom} ?`)) {
      return;
    }

    this.adminService.deleteUser(user.id).subscribe({
      next: () => {
        alert('Utilisateur supprimé avec succès');
        this.loadUsers();
      },
      error: (err) => {
        console.error('Erreur suppression:', err);
        alert('Erreur lors de la suppression');
      }
    });
  }
}
