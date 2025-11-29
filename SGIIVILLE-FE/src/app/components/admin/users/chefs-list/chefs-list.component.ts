import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../../../services/admin.service';

@Component({
  selector: 'app-chefs-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './chefs-list.component.html',
  styleUrls: ['./chefs-list.component.css']
})
export class ChefsListComponent implements OnInit {
  chefs: any[] = [];
  chefsFiltres: any[] = [];
  loading = false;
  searchTerm: string = '';

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.loadChefs();
  }

  loadChefs(): void {
    this.loading = true;
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.chefs = users.filter(u => u.role === 'CHEF_SERVICE');
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement chefs:', err);
        this.loading = false;
        alert('Erreur lors du chargement des chefs de service');
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.chefs];

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(c =>
        c.nom?.toLowerCase().includes(term) ||
        c.email?.toLowerCase().includes(term) ||
        c.departement?.toLowerCase().includes(term)
      );
    }

    this.chefsFiltres = filtered;
  }

  deleteChef(chef: any): void {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer ${chef.nom} ?`)) {
      return;
    }

    this.adminService.deleteUser(chef.id).subscribe({
      next: () => {
        alert('Chef de service supprimé avec succès');
        this.loadChefs();
      },
      error: (err) => {
        console.error('Erreur suppression:', err);
        alert('Erreur lors de la suppression');
      }
    });
  }
}
