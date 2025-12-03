import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MainDOeuvreService } from '../../../services/main-doeuvre.service';
import { MainDOeuvre } from '../../../models/main-doeuvre.model';
import { normalizeText } from '../../../utils/string.utils';

@Component({
  selector: 'app-main-doeuvre-gestion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './main-doeuvre-gestion.component.html',
  styleUrls: ['./main-doeuvre-gestion.component.css']
})
export class MainDOeuvreGestionComponent implements OnInit {
  mainDOeuvreListe: MainDOeuvre[] = [];
  mainDOeuvreFiltree: MainDOeuvre[] = [];
  
  isCreating = false;
  isEditing = false;
  selectedMainDOeuvre: MainDOeuvre | null = null;
  
  nouveauMainDOeuvre: MainDOeuvre = {
    id: 0,
    nom: '',
    email: '',
    prenom: '',
    matricule: '',
    cin: '',
    telephone: '',
    disponibilite: 'LIBRE',
    competence: ''
  };

  filtreCompetence = '';
  filtreDisponibilite = '';
  recherche = '';

  competencesDisponibles = ['Électricité', 'Hydraulique', 'Mécanique', 'Plomberie', 'Maçonnerie', 'Peinture', 'Télécom'];
  
  // Les champs suivants ont été supprimés du schéma XSD :
  // - habilitationsDisponibles
  // - horairesTravail, conges, absences
  // - photoPath, metier, active
  // - historiqueInterventions
  
  // Historique supprimé car historiqueInterventions n'est plus dans le schéma XSD

  constructor(
    private mainDOeuvreService: MainDOeuvreService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMainDOeuvre();
  }

  loadMainDOeuvre(): void {
    const filters: any = {};
    if (this.filtreCompetence) filters.competence = this.filtreCompetence;
    if (this.filtreDisponibilite) filters.disponibilite = this.filtreDisponibilite;

    // Utiliser l'endpoint technicien (pas admin) car on est dans l'interface technicien
    this.mainDOeuvreService.getAll(filters, false).subscribe({
      next: (data: MainDOeuvre[]) => {
        // Dédupliquer par ID pour éviter les doublons
        const uniqueData = this.deduplicateById(data || []);
        this.mainDOeuvreListe = uniqueData;
        this.appliquerFiltres();
      },
      error: (err: any) => {
        console.error('Erreur chargement main-d\'œuvre:', err);
        this.mainDOeuvreListe = [];
        this.mainDOeuvreFiltree = [];
        alert('Erreur lors du chargement des fiches. Vérifiez votre connexion.');
      }
    });
  }

  deduplicateById(data: MainDOeuvre[]): MainDOeuvre[] {
    const seen = new Map<number, MainDOeuvre>();
    data.forEach(item => {
      if (item.id && !seen.has(item.id)) {
        seen.set(item.id, item);
      }
    });
    return Array.from(seen.values());
  }

  trackByMainDOeuvreId(index: number, item: MainDOeuvre): number {
    return item.id || index;
  }

  appliquerFiltres(): void {
    const rechercheLower = normalizeText(this.recherche);
    this.mainDOeuvreFiltree = this.mainDOeuvreListe.filter(md => {
      if (!rechercheLower) return true;
      
      // Filtre par recherche (nom, prénom, matricule, compétence) - avec guards null
      const matchRecherche = 
        normalizeText(md.nom).includes(rechercheLower) ||
        normalizeText(md.prenom).includes(rechercheLower) ||
        normalizeText(md.matricule).includes(rechercheLower) ||
        normalizeText(md.competence || '').includes(rechercheLower);
      
      return matchRecherche;
    });
  }

  creerNouveau(): void {
    this.isCreating = true;
    this.nouveauMainDOeuvre = {
      id: 0,
      nom: '',
      prenom: '',
      matricule: '',
      cin: '',
      telephone: '',
      email: '',
      disponibilite: 'LIBRE',
      competence: ''
    };
  }

  editer(mainDOeuvre: MainDOeuvre): void {
    console.log('🔧 Édition de la fiche:', mainDOeuvre);
    // S'assurer qu'on n'est pas en mode création
    this.isCreating = false;
    // Créer une copie profonde de l'objet pour éviter les modifications directes
    this.selectedMainDOeuvre = {
      ...mainDOeuvre
    };
    this.isEditing = true;
    console.log('✅ Mode édition activé, selectedMainDOeuvre:', this.selectedMainDOeuvre);
    // Scroll vers le formulaire
    setTimeout(() => {
      const formElement = document.querySelector('.card');
      if (formElement && this.isEditing) {
        formElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 100);
  }

  sauvegarder(): void {
    // Validation
    const fiche = this.isCreating ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!fiche) return;

    // Validation champs obligatoires
    if (!fiche.nom || !fiche.prenom || !fiche.matricule || !fiche.cin || !fiche.telephone || !fiche.competence) {
      alert('⚠️ Veuillez remplir tous les champs obligatoires (Nom, Prénom, Matricule, CIN, Téléphone, Compétence)');
      return;
    }

    // Validation email obligatoire pour la création
    if (this.isCreating && (!fiche.email || fiche.email.trim() === '')) {
      alert('⚠️ L\'email est obligatoire pour créer un compte utilisateur.\n\nL\'agent aura besoin de cet email pour se connecter et consulter ses interventions.');
      return;
    }

    if (this.isCreating) {
      // Utiliser l'endpoint technicien (pas admin) car on est dans l'interface technicien
      this.mainDOeuvreService.create(this.nouveauMainDOeuvre, false).subscribe({
        next: (response: any) => {
          // Le backend retourne maintenant un objet avec mainDOeuvre, userId, defaultPassword, message
          const mainDOeuvre = response.mainDOeuvre || response;
          const userId = response.userId;
          const defaultPassword = response.defaultPassword;
          
          let message = '✅ Fiche et compte utilisateur créés avec succès !\n\n';
          message += '📋 Fiche Main d\'Œuvre:\n';
          message += '   • ID: #' + mainDOeuvre.id + '\n';
          message += '   • Nom: ' + mainDOeuvre.nom + ' ' + (mainDOeuvre.prenom || '') + '\n\n';
          
          if (userId && defaultPassword) {
            message += '👤 Compte Utilisateur Créé:\n';
            message += '   • Email: ' + mainDOeuvre.email + '\n';
            message += '   • Mot de passe par défaut: ' + defaultPassword + '\n';
            message += '   • ID Utilisateur: #' + userId + '\n\n';
            message += '⚠️ IMPORTANT: Communiquez ces identifiants à l\'agent.\n';
            message += 'L\'agent devra changer son mot de passe lors de la première connexion.';
          }
          
          alert(message);
          this.isCreating = false;
          this.nouveauMainDOeuvre = {
            id: 0,
            nom: '',
            prenom: '',
            matricule: '',
            cin: '',
            telephone: '',
            email: '',
            disponibilite: 'LIBRE',
            competence: ''
          };
          // Attendre un peu avant de recharger pour laisser le temps au backend de sauvegarder
          setTimeout(() => {
            this.loadMainDOeuvre();
          }, 500);
        },
        error: (err: any) => {
          console.error('Erreur création fiche:', err);
          const errorMessage = err.error?.error || err.error?.message || err.message || 'Erreur inconnue';
          alert('❌ Erreur lors de la création:\n\n' + errorMessage);
        }
      });
    } else if (this.isEditing && this.selectedMainDOeuvre) {
      // Utiliser useAdmin: false car on est dans l'interface technicien
      this.mainDOeuvreService.update(this.selectedMainDOeuvre.id, this.selectedMainDOeuvre, false).subscribe({
        next: () => {
          alert('✅ Fiche mise à jour avec succès !');
          this.isEditing = false;
          this.selectedMainDOeuvre = null;
          this.loadMainDOeuvre();
        },
        error: (err: any) => {
          console.error('Erreur mise à jour fiche:', err);
          const errorMessage = err.error?.error || err.error?.message || err.message || 'Erreur inconnue';
          alert('❌ Erreur lors de la mise à jour:\n\n' + errorMessage);
        }
      });
    }
  }

  archiver(mainDOeuvre: MainDOeuvre): void {
    const nomComplet = `${mainDOeuvre.nom} ${mainDOeuvre.prenom || ''}`;
    if (confirm(`Archiver la fiche de ${nomComplet} ?\n\nCette action désactivera la fiche et la rendra indisponible pour les nouvelles affectations.`)) {
      // Utiliser useAdmin: false car on est dans l'interface technicien
      this.mainDOeuvreService.archiver(mainDOeuvre.id, false).subscribe({
        next: () => {
          alert('✅ Fiche archivée avec succès !\n\nLa fiche est maintenant désactivée.');
          this.loadMainDOeuvre();
        },
        error: (err: any) => {
          console.error('Erreur archivage fiche:', err);
          alert('❌ Erreur lors de l\'archivage: ' + (err.error?.message || err.message || 'Erreur inconnue'));
        }
      });
    }
  }

  // Les méthodes pour gérer les compétences multiples, habilitations, horaires, congés et absences
  // ont été supprimées car elles ne font plus partie du schéma XSD
  // MainDOeuvre a maintenant une seule compétence obligatoire

  // Méthodes liées à l'historique supprimées car historiqueInterventions n'est plus dans le schéma XSD
  // voirHistorique, fermerHistorique, calculerTempsTotal, formatTempsTotal, formatTemps, compterTerminees

  annuler(): void {
    this.isCreating = false;
    this.isEditing = false;
    this.selectedMainDOeuvre = null;
    // Réinitialiser le formulaire de création
    this.nouveauMainDOeuvre = {
      id: 0,
      nom: '',
      prenom: '',
      matricule: '',
      cin: '',
      telephone: '',
      email: '',
      disponibilite: 'LIBRE',
      competence: ''
    };
  }

  getDisponibiliteLabel(disponibilite: string | null | undefined): string {
    if (!disponibilite) return 'Inconnu';
    switch(disponibilite.toUpperCase()) {
      case 'LIBRE': return 'Libre';
      case 'OCCUPE': return 'Occupé';
      case 'ARCHIVE': return 'Archivé';
      default: return disponibilite || 'Inconnu';
    }
  }

  getBadgeDisponibiliteClass(disponibilite: string | null | undefined): string {
    if (!disponibilite) return 'badge-secondary';
    switch(disponibilite.toUpperCase()) {
      case 'LIBRE': return 'badge-success';
      case 'OCCUPE': return 'badge-warning';
      case 'ARCHIVE': return 'badge-secondary';
      case 'DESACTIVE': return 'badge-secondary';
      default: return 'badge-secondary';
    }
  }

  retour(): void {
    this.router.navigate(['/technicien']);
  }
}

