import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MainDOeuvreService } from '../../services/main-doeuvre.service';
import { MainDOeuvre, HabilitationDTO, HistoriqueInterventionDTO } from '../../models/main-doeuvre.model';
import { normalizeText } from '../../utils/string.utils';

@Component({
  selector: 'app-main-doeuvre-gestion',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, DatePipe],
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
    prenom: '',
    matricule: '',
    cin: '',
    telephone: '',
    email: '',
    metier: '',
    competences: [],
    habilitations: [],
    habilitationsExpiration: {},
    disponibilite: 'DISPONIBLE',
    active: true,
    photoPath: '',
    horairesTravail: {},
    conges: [],
    absences: [],
    historiqueInterventionIds: []
  };

  filtreCompetence = '';
  filtreDisponibilite = '';
  recherche = '';

  competencesDisponibles = ['Électricité', 'Hydraulique', 'Mécanique', 'Plomberie', 'Maçonnerie', 'Peinture', 'Télécom'];
  habilitationsDisponibles = ['Électrique', 'CACES', 'Habilitation H0', 'Habilitation H1', 'Habilitation H2', 'Travail en hauteur'];
  
  // Gestion des habilitations avec dates
  nouvellesHabilitations: HabilitationDTO[] = [];
  showHistorique = false;
  historique: HistoriqueInterventionDTO[] = [];
  selectedMainDOeuvreForHistorique: MainDOeuvre | null = null;
  
  // Horaires de travail
  joursSemaine = ['LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI', 'DIMANCHE'];
  
  // Congés et absences
  nouveauConge: string = '';
  nouvelleAbsence: string = '';

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
      next: (data) => {
        // Dédupliquer par ID pour éviter les doublons
        const uniqueData = this.deduplicateById(data || []);
        this.mainDOeuvreListe = uniqueData;
        this.appliquerFiltres();
      },
      error: (err) => {
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
        (md.competences || []).some(c => normalizeText(c).includes(rechercheLower)) ||
        normalizeText(md.metier).includes(rechercheLower);
      
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
      metier: '',
      competences: [],
      habilitations: [],
      habilitationsExpiration: {},
      disponibilite: 'DISPONIBLE',
      active: true,
      horairesTravail: {},
      conges: [],
      absences: [],
      historiqueInterventionIds: []
    };
    this.nouvellesHabilitations = [];
  }

  editer(mainDOeuvre: MainDOeuvre): void {
    this.selectedMainDOeuvre = {...mainDOeuvre};
    this.isEditing = true;
  }

  sauvegarder(): void {
    // Validation
    const fiche = this.isCreating ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!fiche) return;

    // Validation champs obligatoires
    if (!fiche.nom || !fiche.cin || !fiche.telephone) {
      alert('⚠️ Veuillez remplir tous les champs obligatoires (Nom, CIN, Téléphone)');
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
            metier: '',
            competences: [],
            habilitations: [],
            habilitationsExpiration: {},
            disponibilite: 'DISPONIBLE',
            active: true,
            horairesTravail: {},
            conges: [],
            absences: [],
            historiqueInterventionIds: []
          };
          this.nouvellesHabilitations = [];
          // Attendre un peu avant de recharger pour laisser le temps au backend de sauvegarder
          setTimeout(() => {
            this.loadMainDOeuvre();
          }, 500);
        },
        error: (err) => {
          console.error('Erreur création fiche:', err);
          const errorMessage = err.error?.error || err.error?.message || err.message || 'Erreur inconnue';
          alert('❌ Erreur lors de la création:\n\n' + errorMessage);
        }
      });
    } else if (this.isEditing && this.selectedMainDOeuvre) {
      this.mainDOeuvreService.update(this.selectedMainDOeuvre.id, this.selectedMainDOeuvre).subscribe({
        next: () => {
          alert('✅ Fiche mise à jour avec succès !');
          this.isEditing = false;
          this.selectedMainDOeuvre = null;
          this.loadMainDOeuvre();
        },
        error: (err) => {
          console.error('Erreur mise à jour fiche:', err);
          alert('❌ Erreur lors de la mise à jour: ' + (err.error?.message || err.message || 'Erreur inconnue'));
        }
      });
    }
  }

  archiver(mainDOeuvre: MainDOeuvre): void {
    const nomComplet = `${mainDOeuvre.nom} ${mainDOeuvre.prenom || ''}`;
    if (confirm(`Archiver la fiche de ${nomComplet} ?\n\nCette action désactivera la fiche et la rendra indisponible pour les nouvelles affectations.`)) {
      this.mainDOeuvreService.archiver(mainDOeuvre.id).subscribe({
        next: () => {
          alert('✅ Fiche archivée avec succès !\n\nLa fiche est maintenant désactivée.');
          this.loadMainDOeuvre();
        },
        error: (err) => {
          console.error('Erreur archivage fiche:', err);
          alert('❌ Erreur lors de l\'archivage: ' + (err.error?.message || err.message || 'Erreur inconnue'));
        }
      });
    }
  }

  toggleCompetence(competence: string, isNouveau: boolean = false): void {
    const target = isNouveau ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!target) return;
    
    if (!target.competences) target.competences = [];
    const index = target.competences.indexOf(competence);
    if (index > -1) {
      target.competences.splice(index, 1);
    } else {
      target.competences.push(competence);
    }
  }

  toggleHabilitation(habilitation: string, isNouveau: boolean = false): void {
    const target = isNouveau ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!target || !habilitation) return;
    
    if (!target.habilitations) target.habilitations = [];
    if (!target.habilitationsExpiration) target.habilitationsExpiration = {};
    
    const index = target.habilitations.indexOf(habilitation);
    if (index > -1) {
      target.habilitations.splice(index, 1);
      delete target.habilitationsExpiration[habilitation];
    } else {
      target.habilitations.push(habilitation);
      // Initialiser avec date d'expiration vide
      target.habilitationsExpiration[habilitation] = '';
    }
  }

  updateHabilitationExpiration(habilitation: string, dateExpiration: string, isNouveau: boolean = false): void {
    const target = isNouveau ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!target || !target.habilitationsExpiration) return;
    target.habilitationsExpiration[habilitation] = dateExpiration;
  }

  getHabilitationExpiration(habilitation: string, isNouveau: boolean = false): string {
    const target = isNouveau ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!target || !target.habilitationsExpiration) return '';
    return target.habilitationsExpiration[habilitation] || '';
  }

  isHabilitationExpiree(habilitation: string, mainDOeuvre: MainDOeuvre): boolean {
    if (!mainDOeuvre.habilitationsExpiration || !mainDOeuvre.habilitationsExpiration[habilitation]) {
      return false;
    }
    const dateExp = new Date(mainDOeuvre.habilitationsExpiration[habilitation]);
    return dateExp < new Date();
  }

  updateHorairesTravail(jour: string, horaires: string, isNouveau: boolean = false): void {
    const target = isNouveau ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!target) return;
    if (!target.horairesTravail) target.horairesTravail = {};
    target.horairesTravail[jour] = horaires;
  }

  getHorairesTravail(jour: string, isNouveau: boolean = false): string {
    const target = isNouveau ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!target || !target.horairesTravail) return '';
    return target.horairesTravail[jour] || '';
  }

  ajouterConge(isNouveau: boolean = false): void {
    if (!this.nouveauConge) return;
    const target = isNouveau ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!target) return;
    if (!target.conges) target.conges = [];
    if (!target.conges.includes(this.nouveauConge)) {
      target.conges.push(this.nouveauConge);
      this.nouveauConge = '';
    }
  }

  retirerConge(date: string, isNouveau: boolean = false): void {
    const target = isNouveau ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!target || !target.conges) return;
    const index = target.conges.indexOf(date);
    if (index > -1) {
      target.conges.splice(index, 1);
    }
  }

  ajouterAbsence(isNouveau: boolean = false): void {
    if (!this.nouvelleAbsence) return;
    const target = isNouveau ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!target) return;
    if (!target.absences) target.absences = [];
    if (!target.absences.includes(this.nouvelleAbsence)) {
      target.absences.push(this.nouvelleAbsence);
      this.nouvelleAbsence = '';
    }
  }

  retirerAbsence(date: string, isNouveau: boolean = false): void {
    const target = isNouveau ? this.nouveauMainDOeuvre : this.selectedMainDOeuvre;
    if (!target || !target.absences) return;
    const index = target.absences.indexOf(date);
    if (index > -1) {
      target.absences.splice(index, 1);
    }
  }

  voirHistorique(mainDOeuvre: MainDOeuvre): void {
    this.selectedMainDOeuvreForHistorique = mainDOeuvre;
    this.showHistorique = true;
    this.mainDOeuvreService.getHistorique(mainDOeuvre.id).subscribe({
      next: (data) => {
        this.historique = data || [];
      },
      error: (err) => {
        console.error('Erreur chargement historique:', err);
        this.historique = [];
        alert('Erreur lors du chargement de l\'historique');
      }
    });
  }

  fermerHistorique(): void {
    this.showHistorique = false;
    this.selectedMainDOeuvreForHistorique = null;
    this.historique = [];
  }

  hasHoraires(horairesTravail?: { [key: string]: string }): boolean {
    if (!horairesTravail) return false;
    return Object.keys(horairesTravail).length > 0;
  }

  calculerTempsTotal(): number {
    return this.historique.reduce((total, hist) => total + (hist.tempsPasseMinutes || 0), 0);
  }

  formatTempsTotal(): string {
    const total = this.calculerTempsTotal();
    const heures = Math.floor(total / 60);
    const minutes = total % 60;
    if (heures > 0) {
      return `${heures}h ${minutes}min`;
    }
    return `${minutes}min`;
  }

  formatTemps(minutes: number): string {
    if (!minutes) return '0 min';
    const heures = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (heures > 0) {
      return `${heures}h ${mins}min`;
    }
    return `${mins}min`;
  }

  compterTerminees(): number {
    return this.historique.filter(h => h.etat === 'TERMINEE' || h.resultat === 'Succès').length;
  }

  annuler(): void {
    this.isCreating = false;
    this.isEditing = false;
    this.selectedMainDOeuvre = null;
  }

  getDisponibiliteLabel(disponibilite: string | null | undefined): string {
    if (!disponibilite) return 'Inconnu';
    switch(disponibilite.toUpperCase()) {
      case 'DISPONIBLE': return '✅ Disponible';
      case 'OCCUPE': return '🔄 Occupé';
      case 'CONFLIT': return '⚠️ Conflit d\'horaires';
      case 'EN_CONGE': return '🏖️ En congé';
      case 'ABSENT': return '🏥 Absent';
      case 'HORS_HABILITATION': return '⛔ Hors habilitation';
      case 'ARCHIVE': return '🗄️ Archivé';
      case 'DESACTIVE': return '❌ Désactivé';
      default: return disponibilite || 'Inconnu';
    }
  }

  retour(): void {
    this.router.navigate(['/technicien']);
  }
}

