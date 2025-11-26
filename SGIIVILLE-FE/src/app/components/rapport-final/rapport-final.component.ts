import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TechnicienService } from '../../services/technicien.service';
import { Intervention } from '../../models/intervention.model';
import { RapportFinalRequest, RessourceUtilisee, EquipementUtilise } from '../../models/rapport-final.model';

@Component({
  selector: 'app-rapport-final',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './rapport-final.component.html',
  styleUrls: ['./rapport-final.component.css']
})
export class RapportFinalComponent implements OnInit {
  intervention: Intervention | null = null;
  rapport: RapportFinalRequest = {
    resultatObtenu: '',
    tempsTotalMinutes: 0,
    ressourcesUtilisees: [],
    equipementsUtilises: [],
    problemesRencontres: '',
    photoIds: [],
    signatureElectronique: ''
  };

  nouvelleRessource: RessourceUtilisee = {
    ressourceId: 0,
    type: '',
    quantite: 0
  };

  nouvelEquipement: EquipementUtilise = {
    equipementId: 0,
    type: '',
    dureeUtilisationMinutes: 0
  };

  selectedFiles: File[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private technicienService: TechnicienService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadIntervention(+id);
    }
  }

  loadIntervention(id: number): void {
    this.technicienService.getInterventionDetails(id).subscribe({
      next: (data) => {
        this.intervention = data;
        if (data.tempsPasseMinutes) {
          this.rapport.tempsTotalMinutes = data.tempsPasseMinutes;
        }
        if (data.photoIds) {
          this.rapport.photoIds = data.photoIds;
        }
      },
      error: (err) => console.error('Erreur:', err)
    });
  }

  ajouterRessource(): void {
    if (this.nouvelleRessource.ressourceId && this.nouvelleRessource.quantite > 0) {
      this.rapport.ressourcesUtilisees.push({...this.nouvelleRessource});
      this.nouvelleRessource = { ressourceId: 0, type: '', quantite: 0 };
    }
  }

  supprimerRessource(index: number): void {
    this.rapport.ressourcesUtilisees.splice(index, 1);
  }

  ajouterEquipement(): void {
    if (this.nouvelEquipement.equipementId && this.nouvelEquipement.dureeUtilisationMinutes > 0) {
      this.rapport.equipementsUtilises.push({...this.nouvelEquipement});
      this.nouvelEquipement = { equipementId: 0, type: '', dureeUtilisationMinutes: 0 };
    }
  }

  supprimerEquipement(index: number): void {
    this.rapport.equipementsUtilises.splice(index, 1);
  }

  onFileSelected(event: any): void {
    this.selectedFiles = Array.from(event.target.files);
  }

  soumettreRapport(): void {
    if (!this.intervention) return;

    if (!this.rapport.resultatObtenu.trim()) {
      alert('Veuillez remplir le résultat obtenu');
      return;
    }

    if (confirm('Soumettre le rapport final et clôturer l\'intervention ?')) {
      this.technicienService.soumettreRapportFinal(this.intervention.id, this.rapport).subscribe({
        next: () => {
          alert('Rapport final soumis avec succès');
          this.router.navigate(['/technicien']);
        },
        error: (err) => alert('Erreur: ' + err.message)
      });
    }
  }

  retour(): void {
    if (this.intervention) {
      this.router.navigate(['/technicien/intervention', this.intervention.id]);
    }
  }
}

