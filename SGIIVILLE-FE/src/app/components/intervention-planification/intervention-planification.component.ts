import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InterventionService } from '../../services/intervention.service';
import { TechnicienListService } from '../../services/technicien-list.service';
import { RessourceService } from '../../services/ressource.service';
import { EquipementService } from '../../services/equipement.service';
import { Technicien } from '../../models/technicien.model';
import { RessourceMaterielle } from '../../models/ressource.model';
import { Equipement } from '../../models/equipement.model';
import {
  CompetenceRequise,
  MaterielRequis,
  EquipementRequis,
  DateValidationRequest,
  DateValidationResult
} from '../../models/intervention-validation.model';

@Component({
  selector: 'app-intervention-planification',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './intervention-planification.component.html',
  styleUrls: ['./intervention-planification.component.css']
})
export class InterventionPlanificationComponent implements OnInit {
  @Input() demandeId!: number;
  @Output() planificationComplete = new EventEmitter<any>();
  @Output() cancelled = new EventEmitter<void>();

  // Infos d'intervention
  intervention: any = {
    description: '',
    priorite: 'PLANIFIEE',
    budget: 500,
    dateCreation: new Date() // Date d'aujourd'hui par défaut
  };

  // Exigences de l'intervention
  competencesRequises: CompetenceRequise[] = [];
  materielsRequis: MaterielRequis[] = [];
  equipementsRequis: EquipementRequis[] = [];

  // Listes disponibles depuis la base
  listeCompetencesDisponibles: string[] = [];
  listeMaterielsDisponibles: RessourceMaterielle[] = [];
  listeEquipementsDisponibles: string[] = [];

  // Nouvelle compétence/matériel/équipement à ajouter
  nouvelleCompetence: CompetenceRequise = { competence: '', nombreTechniciens: 1 };
  nouveauMateriel: MaterielRequis = { designation: '', quantiteRequise: 1 };
  nouvelEquipement: EquipementRequis = { type: '', quantiteRequise: 1 };

  // Période de recherche
  dateDebut: string = '';
  dateFin: string = '';

  // Résultats de validation
  validationResults: DateValidationResult[] = [];
  isLoading = false;
  validationEffectuee = false;

  // Étape actuelle
  etapeActuelle: 'infos' | 'exigences' | 'calendrier' | 'selection' | 'confirmation' = 'infos';

  // Date sélectionnée
  dateSelectionnee: string | null = null;

  // Ressources disponibles pour la date sélectionnée
  techniciensDisponibles: Technicien[] = [];
  equipementsDisponibles: Equipement[] = [];
  materielsDisponibles: RessourceMaterielle[] = [];

  // Ressources sélectionnées
  techniciensSelectionnes: number[] = [];
  equipementsSelectionnes: number[] = [];
  materielsSelectionnes: Map<number, number> = new Map(); // materielId -> quantité

  // Calendrier
  calendrierMois: any[] = [];
  moisActuel: Date = new Date();
  joursSemaine = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

  constructor(
    private interventionService: InterventionService,
    private technicienService: TechnicienListService,
    private ressourceService: RessourceService,
    private equipementService: EquipementService
  ) {}

  ngOnInit(): void {
    // Initialiser les dates (aujourd'hui + 1 mois)
    const today = new Date();
    this.dateDebut = today.toISOString().split('T')[0];
    
    const nextMonth = new Date(today);
    nextMonth.setMonth(nextMonth.getMonth() + 1);
    this.dateFin = nextMonth.toISOString().split('T')[0];

    // Charger les données depuis la base
    this.chargerDonneesBase();
  }

  // Charger les données depuis la base
  private chargerDonneesBase(): void {
    // Charger les compétences des techniciens
    this.technicienService.getAllTechniciens().subscribe({
      next: (techniciens: Technicien[]) => {
        // Extraire toutes les compétences uniques
        const competencesSet = new Set<string>();
        techniciens.forEach(tech => {
          tech.competences.forEach(comp => competencesSet.add(comp));
        });
        this.listeCompetencesDisponibles = Array.from(competencesSet).sort();
      },
      error: (error) => console.error('Erreur chargement techniciens:', error)
    });

    // Charger les matériels
    this.ressourceService.getAll().subscribe({
      next: (ressources: RessourceMaterielle[]) => {
        this.listeMaterielsDisponibles = ressources;
      },
      error: (error) => console.error('Erreur chargement matériels:', error)
    });

    // Charger les types d'équipements
    this.equipementService.getAllEquipements().subscribe({
      next: (equipements: Equipement[]) => {
        // Extraire les types uniques
        const typesSet = new Set<string>();
        equipements.forEach(eq => typesSet.add(eq.type));
        this.listeEquipementsDisponibles = Array.from(typesSet).sort();
      },
      error: (error) => console.error('Erreur chargement équipements:', error)
    });
  }

  // Ajouter une compétence
  ajouterCompetence(): void {
    console.log('🔍 Ajout compétence - Valeur:', this.nouvelleCompetence);
    if (this.nouvelleCompetence.competence.trim()) {
      this.competencesRequises.push({ ...this.nouvelleCompetence });
      console.log('✅ Compétence ajoutée. Liste:', this.competencesRequises);
      this.nouvelleCompetence = { competence: '', nombreTechniciens: 1 };
    } else {
      console.log('⚠️ Compétence vide, non ajoutée');
    }
  }

  // Supprimer une compétence
  supprimerCompetence(index: number): void {
    this.competencesRequises.splice(index, 1);
  }

  // Ajouter un matériel
  ajouterMateriel(): void {
    console.log('🔍 Ajout matériel - Valeur:', this.nouveauMateriel);
    if (this.nouveauMateriel.designation.trim()) {
      this.materielsRequis.push({ ...this.nouveauMateriel });
      console.log('✅ Matériel ajouté. Liste:', this.materielsRequis);
      this.nouveauMateriel = { designation: '', quantiteRequise: 1 };
    } else {
      console.log('⚠️ Matériel vide, non ajouté');
    }
  }

  // Supprimer un matériel
  supprimerMateriel(index: number): void {
    this.materielsRequis.splice(index, 1);
  }

  // Ajouter un équipement
  ajouterEquipement(): void {
    console.log('🔍 Ajout équipement - Valeur:', this.nouvelEquipement);
    if (this.nouvelEquipement.type.trim()) {
      this.equipementsRequis.push({ ...this.nouvelEquipement });
      console.log('✅ Équipement ajouté. Liste:', this.equipementsRequis);
      this.nouvelEquipement = { type: '', quantiteRequise: 1 };
    } else {
      console.log('⚠️ Équipement vide, non ajouté');
    }
  }

  // Supprimer un équipement
  supprimerEquipement(index: number): void {
    this.equipementsRequis.splice(index, 1);
  }

  // Valider les dates
  validerDates(): void {
    console.log('🔍 Validation - Compétences requises:', this.competencesRequises);
    console.log('🔍 Validation - Matériels requis:', this.materielsRequis);
    console.log('🔍 Validation - Équipements requis:', this.equipementsRequis);
    
    if (!this.dateDebut || !this.dateFin) {
      alert('Veuillez sélectionner une période');
      return;
    }

    if (this.competencesRequises.length === 0 && 
        this.materielsRequis.length === 0 && 
        this.equipementsRequis.length === 0) {
      alert('Veuillez définir au moins une exigence');
      return;
    }

    this.isLoading = true;
    const request: DateValidationRequest = {
      dateDebut: this.dateDebut,
      dateFin: this.dateFin,
      competencesRequises: this.competencesRequises,
      materielsRequis: this.materielsRequis,
      equipementsRequis: this.equipementsRequis
    };

    this.interventionService.validateDates(request).subscribe({
      next: (results) => {
        this.validationResults = results;
        this.validationEffectuee = true;
        this.isLoading = false;
        this.genererCalendrier();
        this.etapeActuelle = 'calendrier';
      },
      error: (error) => {
        console.error('Erreur validation dates:', error);
        alert('Erreur lors de la validation des dates');
        this.isLoading = false;
      }
    });
  }

  // Générer le calendrier mensuel
  genererCalendrier(): void {
    this.calendrierMois = [];
    const debut = new Date(this.dateDebut);
    const fin = new Date(this.dateFin);
    
    let currentDate = new Date(debut.getFullYear(), debut.getMonth(), 1);
    const endDate = new Date(fin.getFullYear(), fin.getMonth() + 1, 0);
    
    while (currentDate <= endDate) {
      const mois = {
        nom: currentDate.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' }),
        semaines: this.genererSemainesDuMois(new Date(currentDate))
      };
      this.calendrierMois.push(mois);
      currentDate.setMonth(currentDate.getMonth() + 1);
    }
  }

  // Générer les semaines d'un mois
  genererSemainesDuMois(date: Date): any[] {
    const annee = date.getFullYear();
    const mois = date.getMonth();
    const premierJour = new Date(annee, mois, 1);
    const dernierJour = new Date(annee, mois + 1, 0);
    
    // Obtenir le jour de la semaine (0 = Dimanche, 1 = Lundi, ...)
    let jourDebut = premierJour.getDay();
    jourDebut = jourDebut === 0 ? 6 : jourDebut - 1; // Convertir pour que Lundi = 0
    
    const semaines: any[] = [];
    let semaine: any[] = [];
    
    // Remplir les jours vides avant le premier jour du mois
    for (let i = 0; i < jourDebut; i++) {
      semaine.push({ vide: true });
    }
    
    // Remplir les jours du mois
    for (let jour = 1; jour <= dernierJour.getDate(); jour++) {
      const dateActuelle = new Date(annee, mois, jour);
      const dateStr = dateActuelle.toISOString().split('T')[0];
      const resultat = this.getResultatDate(dateStr);
      
      semaine.push({
        numero: jour,
        date: dateStr,
        status: resultat?.status || null,
        resultat: resultat
      });
      
      // Si on atteint dimanche (index 6), on commence une nouvelle semaine
      if (semaine.length === 7) {
        semaines.push(semaine);
        semaine = [];
      }
    }
    
    // Remplir les jours vides après le dernier jour du mois
    while (semaine.length > 0 && semaine.length < 7) {
      semaine.push({ vide: true });
    }
    
    if (semaine.length > 0) {
      semaines.push(semaine);
    }
    
    return semaines;
  }

  // Sélectionner une date
  selectionnerDate(date: string, status: string): void {
    if (status === 'ROUGE') {
      alert('Cette date ne remplit pas toutes les conditions requises');
      return;
    }
    this.dateSelectionnee = date;
    this.chargerRessourcesDisponibles(date);
    this.etapeActuelle = 'selection';
  }

  // Charger les ressources disponibles pour la date sélectionnée
  chargerRessourcesDisponibles(date: string): void {
    this.isLoading = true;

    // Charger techniciens avec les compétences requises
    this.technicienService.getAllTechniciens().subscribe({
      next: (techniciens) => {
        this.techniciensDisponibles = techniciens.filter(tech => {
          // Vérifier si disponible
          if (!tech.disponibilite) return false;
          
          // Vérifier si a au moins une compétence requise
          return this.competencesRequises.some(compReq => 
            tech.competences.some(compTech => 
              compTech.trim().toLowerCase() === compReq.competence.trim().toLowerCase()
            )
          );
        });
        console.log('Techniciens disponibles:', this.techniciensDisponibles);
      },
      error: (error) => console.error('Erreur chargement techniciens:', error)
    });

    // Charger équipements disponibles par type
    this.equipementService.getAllEquipements().subscribe({
      next: (equipements) => {
        console.log('Tous les équipements:', equipements);
        console.log('Équipements requis:', this.equipementsRequis);
        
        this.equipementsDisponibles = equipements.filter(eq => {
          // Vérifier si fonctionnel et du bon type (case-insensitive)
          const estFonctionnel = eq.etat && eq.etat.toLowerCase() === 'fonctionnel';
          const bonType = this.equipementsRequis.some(eqReq => 
            eq.type && eqReq.type && eq.type.toLowerCase() === eqReq.type.toLowerCase()
          );
          
          console.log(`Équipement ${eq.nom}: état=${eq.etat}, type=${eq.type}, fonctionnel=${estFonctionnel}, bonType=${bonType}`);
          
          return estFonctionnel && bonType;
        });
        console.log('Équipements disponibles filtrés:', this.equipementsDisponibles);
      },
      error: (error) => console.error('Erreur chargement équipements:', error)
    });

    // Charger matériels avec stock suffisant
    this.ressourceService.getAll().subscribe({
      next: (ressources) => {
        console.log('Tous les matériels:', ressources);
        console.log('Matériels requis:', this.materielsRequis);
        
        this.materielsDisponibles = ressources.filter(mat => 
          this.materielsRequis.some(matReq => {
            const designationMatch = mat.designation && matReq.designation && 
              mat.designation.toLowerCase().trim() === matReq.designation.toLowerCase().trim();
            const stockSuffisant = mat.quantiteEnStock >= matReq.quantiteRequise;
            
            if (designationMatch) {
              console.log(`Matériel ${mat.designation}: stock=${mat.quantiteEnStock}, requis=${matReq.quantiteRequise}, suffisant=${stockSuffisant}`);
            }
            
            return designationMatch && stockSuffisant;
          })
        );
        console.log('Matériels disponibles filtrés:', this.materielsDisponibles);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur chargement matériels:', error);
        this.isLoading = false;
      }
    });
  }

  // Toggle sélection technicien
  toggleTechnicien(technicienId: number): void {
    const index = this.techniciensSelectionnes.indexOf(technicienId);
    if (index > -1) {
      this.techniciensSelectionnes.splice(index, 1);
    } else {
      this.techniciensSelectionnes.push(technicienId);
    }
  }

  // Toggle sélection équipement
  toggleEquipement(equipementId: number): void {
    const index = this.equipementsSelectionnes.indexOf(equipementId);
    if (index > -1) {
      this.equipementsSelectionnes.splice(index, 1);
    } else {
      this.equipementsSelectionnes.push(equipementId);
    }
  }

  // Définir quantité matériel
  setQuantiteMateriel(materielId: number, quantite: number): void {
    if (quantite > 0) {
      this.materielsSelectionnes.set(materielId, quantite);
    } else {
      this.materielsSelectionnes.delete(materielId);
    }
  }

  // Vérifier si les sélections sont valides
  selectionsValides(): boolean {
    // Vérifier qu'on a assez de techniciens
    const nombreTechniciensRequis = this.competencesRequises.reduce((sum, comp) => sum + comp.nombreTechniciens, 0);
    if (this.techniciensSelectionnes.length < nombreTechniciensRequis) {
      return false;
    }

    // Vérifier qu'on a assez d'équipements
    const nombreEquipementsRequis = this.equipementsRequis.reduce((sum, eq) => sum + eq.quantiteRequise, 0);
    if (this.equipementsSelectionnes.length < nombreEquipementsRequis) {
      return false;
    }

    // Vérifier les matériels
    for (const matReq of this.materielsRequis) {
      const materiel = this.materielsDisponibles.find(m => m.designation === matReq.designation);
      if (!materiel) return false;
      
      const quantiteSelectionnee = this.materielsSelectionnes.get(materiel.id) || 0;
      if (quantiteSelectionnee < matReq.quantiteRequise) {
        return false;
      }
    }

    return true;
  }

  // Valider les sélections et passer à la confirmation
  validerSelections(): void {
    if (!this.selectionsValides()) {
      alert('Veuillez sélectionner toutes les ressources requises');
      return;
    }
    this.etapeActuelle = 'confirmation';
  }

  // Confirmer la planification
  confirmerPlanification(): void {
    if (!this.dateSelectionnee) {
      alert('Veuillez sélectionner une date');
      return;
    }

    // Préparer la requête complète (assignation + création intervention)
    const requestPlanification = {
      demandeId: this.demandeId,
      dateIntervention: this.dateSelectionnee,
      techniciensIds: this.techniciensSelectionnes,
      equipementsIds: this.equipementsSelectionnes,
      materiels: Array.from(this.materielsSelectionnes.entries()).map(([id, quantite]) => ({
        materielId: id,
        quantite: quantite
      })),
      // Ajouter les infos d'intervention
      description: this.intervention.description,
      priorite: this.intervention.priorite,
      dateCreation: this.intervention.dateCreation,
      budget: this.intervention.budget
    };

    console.log('Planification complète:', requestPlanification);
    this.isLoading = true;

    // Appeler le backend pour tout faire en une seule fois
    this.interventionService.planifierInterventionComplete(requestPlanification).subscribe({
      next: (response: any) => {
        console.log('Intervention créée avec succès:', response);
        this.isLoading = false;
        
        // Émettre l'événement de succès
        this.planificationComplete.emit({
          success: true,
          interventionId: response.interventionId,
          intervention: response.intervention
        });
      },
      error: (error) => {
        console.error('Erreur complète:', error);
        console.error('error.error:', error.error);
        console.error('error.message:', error.message);
        console.error('error.status:', error.status);
        
        this.isLoading = false;
        
        let errorMessage = 'Erreur lors de l\'assignation des ressources';
        
        if (error.error) {
          if (typeof error.error === 'string') {
            errorMessage += ': ' + error.error;
          } else if (error.error.message) {
            errorMessage += ': ' + error.error.message;
          } else {
            errorMessage += ': ' + JSON.stringify(error.error);
          }
        } else if (error.message) {
          errorMessage += ': ' + error.message;
        }
        
        alert(errorMessage);
      }
    });
  }

  // Retour à l'étape précédente
  retourEtape(): void {
    switch (this.etapeActuelle) {
      case 'exigences':
        this.etapeActuelle = 'infos';
        break;
      case 'calendrier':
        this.etapeActuelle = 'exigences';
        this.validationEffectuee = false;
        break;
      case 'selection':
        this.etapeActuelle = 'calendrier';
        this.dateSelectionnee = null;
        this.techniciensSelectionnes = [];
        this.equipementsSelectionnes = [];
        this.materielsSelectionnes.clear();
        break;
      case 'confirmation':
        this.etapeActuelle = 'selection';
        break;
    }
  }

  // Helper pour obtenir la quantité requise d'un matériel
  getQuantiteRequise(designation: string): number {
    const matReq = this.materielsRequis.find(m => m.designation === designation);
    return matReq?.quantiteRequise || 0;
  }

  // Helper pour obtenir la quantité sélectionnée
  getQuantiteSelectionnee(materielId: number): number {
    return this.materielsSelectionnes.get(materielId) || 0;
  }

  // Annuler
  annuler(): void {
    this.cancelled.emit();
  }

  // Obtenir la classe CSS pour une date
  getDateClass(status: string): string {
    switch (status) {
      case 'VERT': return 'date-vert';
      case 'JAUNE': return 'date-jaune';
      case 'ROUGE': return 'date-rouge';
      default: return '';
    }
  }

  // Obtenir le résultat d'une date spécifique
  getResultatDate(date: string): DateValidationResult | undefined {
    return this.validationResults.find(r => r.date === date);
  }

  // Formater une date
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', { 
      weekday: 'long', 
      day: 'numeric', 
      month: 'long', 
      year: 'numeric' 
    });
  }

  // Obtenir le nombre de dates par statut
  get nombreDatesVertes(): number {
    return this.validationResults.filter(r => r.status === 'VERT').length;
  }

  get nombreDatesJaunes(): number {
    return this.validationResults.filter(r => r.status === 'JAUNE').length;
  }

  get nombreDatesRouges(): number {
    return this.validationResults.filter(r => r.status === 'ROUGE').length;
  }

  // ====== ÉTAPE INFOS ======

  /**
   * Valide les informations d'intervention
   */
  infosValides(): boolean {
    return this.intervention.description?.trim().length > 0 &&
           this.intervention.priorite?.length > 0 &&
           this.intervention.budget >= 0;
  }

  /**
   * Valide et passe à l'étape exigences
   */
  validerInfos(): void {
    if (this.infosValides()) {
      this.etapeActuelle = 'exigences';
    }
  }
}

