import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../../../services/admin.service';

@Component({
  selector: 'app-techniciens-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './techniciens-list.component.html',
  styleUrls: ['./techniciens-list.component.css']
})
export class TechniciensListComponent implements OnInit {
  techniciens: any[] = [];
  techniciensFiltres: any[] = [];
  loading = false;
  searchTerm: string = '';

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.loadTechniciens();
  }

  loadTechniciens(): void {
    this.loading = true;
    this.adminService.getAllTechniciens().subscribe({
      next: (data) => {
        this.techniciens = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement techniciens:', err);
        this.loading = false;
        alert('Erreur lors du chargement des techniciens');
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.techniciens];

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(t =>
        t.nom?.toLowerCase().includes(term) ||
        t.email?.toLowerCase().includes(term) ||
        t.competences?.some((c: string) => c.toLowerCase().includes(term))
      );
    }

    this.techniciensFiltres = filtered;
  }

  deleteTechnicien(technicien: any): void {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer ${technicien.nom} ?`)) {
      return;
    }

    this.adminService.deleteUser(technicien.id).subscribe({
      next: () => {
        alert('Technicien supprimé avec succès');
        this.loadTechniciens();
      },
      error: (err) => {
        console.error('Erreur suppression:', err);
        alert('Erreur lors de la suppression');
      }
    });
  }

  getCompetencesString(competences: string[]): string {
    if (!competences || competences.length === 0) return 'Aucune compétence';
    return competences.join(', ');
  }
}
