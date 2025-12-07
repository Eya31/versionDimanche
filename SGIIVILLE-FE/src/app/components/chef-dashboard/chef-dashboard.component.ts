import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemandeService } from '../../services/demande.service';
import { EquipementService } from '../../services/equipement.service';
import { InterventionService } from '../../services/intervention.service';
import { RessourceService } from '../../services/ressource.service';
import { Demande } from '../../models/demande.model';
import { Equipement } from '../../models/equipement.model';
import { Intervention } from '../../models/intervention.model';
import { RessourceMaterielle } from '../../models/ressource.model';
import { Technicien } from '../../models/technicien.model';
import { TechnicienListService } from '../../services/technicien-list.service';
import { AuthService } from '../../services/auth.service';
import { InterventionPlanificationComponent } from '../intervention-planification/intervention-planification.component';
import * as L from 'leaflet';
import { Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { NotifService, Notification } from '../../services/notif.service';
import { RegisterRequest, RoleType } from '../../models/auth.model';

import { DemandeAjoutService , DemandeAjout} from '../../services/demande-ajout.service';
import {
  DemandeAjoutMaterielService,  // Nom du service changé
  DemandeRessource,
  CreateDemandeRessourceRequest,
  DemandeAjoutMateriel
} from "../../services/demande-ajout-materiel.service" // Nom du fichier changé
import { map } from 'rxjs/operators';
import { Router } from '@angular/router';

@Component({
  selector: 'app-chef-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, InterventionPlanificationComponent],
  templateUrl: './chef-dashboard.component.html',
  styleUrls: ['./chef-dashboard.component.css']
})

export class ChefDashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  showEquipementForm = false;
techniciens: Technicien[] = [];
  showTechniciensModal = false;
  demandes: Demande[] = [];
  demandesFiltrees: Demande[] = [];
  equipements: Equipement[] = [];
  interventions: Intervention[] = [];
  ressources: RessourceMaterielle[] = [];
showEquipementModal = false;
showRessourceModal = false;
showEquipementFormModal = false;
showRessourceFormModal = false;
  // Cartes Leaflet
  private mapDemandes?: L.Map;
  private mapInterventions?: L.Map;
  showMapModal = false;
  showMapInterventionsModal = false;
  demandesNonTraitees: Demande[] = [];
  interventionsEnCours: Intervention[] = [];

  // Notifications
  notifications: Notification[] = [];
  unreadCount = 0;
  showNotificationsDropdown = false;
  private notificationSubscription?: Subscription;
  private unreadCountSubscription?: Subscription;
citoyenInfos = new Map<number, any>(); // Stocke les infos citoyen par ID de demande

  // Stats
  demandesPendantes = 0;
  interventionsEnCoursCount = 0;
  demandesTraitees = 0;

  // Modals
  showInterventionModal = false;
  showFormModal = false;
  showDetailModal = false;
  showPlanificationModal = false;
selectedDemande: any = null;
citoyenDetails: any = null; // Nouvelle propriété pour stocker les détails citoyen
  // Planification
  planificationData: any = {
    technicienId: null,
    datePlanifiee: '',
    heureDebut: '',
    heureFin: '',
    dureeMinutes: 60,
    priorite: 'NORMALE',
    budget: 500,
    description: '',
    typeIntervention: '',
    equipementIds: [],
    ressourceIds: [],
    mainDOeuvreIds: [],
    remarques: ''
  };
  techniciensDisponibles: any[] = [];
  editingEquipement: Equipement | null = null;
  editingRessource: RessourceMaterielle | null = null;

 currentEquipement: Equipement = {
  id: 0,
  nom: '',
  type: '',
  etat: 'FONCTIONNEL',
  fournisseurId: undefined,
  valeurAchat: 0,
  localisation: undefined,
  dateAchat: new Date().toISOString().split('T')[0],
  disponible: true,
  indisponibilites: []
};

currentRessource: RessourceMaterielle = {
  id: 0,
  designation: '',
  quantiteEnStock: 0,
  valeurAchat: 0,
  fournisseurId: undefined,
  unite: ''
};

  filtreActif: 'TOUS' | 'NON_TRAITEES' | 'TRAITEES' = 'TOUS';

  // Sidebar state
  sidebarCollapsed = true;

  // ✅ FORMULAIRE D'ENREGISTREMENT TECHNICIEN
  showRegisterTechnicienForm = false;
  technicienRegisterData: RegisterRequest & { disponibilite?: boolean } = {
    nom: '',
    prenom: '',
    email: '',
    motDePasse: '',
    role: RoleType.TECHNICIEN,
    telephone: '',
    competence: '',
    disponibilite: true
  };
  technicienConfirmPassword: string = '';
  technicienErrorMessage: string = '';
  technicienSuccessMessage: string = '';
  technicienIsLoading: boolean = false;

  competencesDisponibles = [
    'Plomberie',
    'Électricité',
    'Menuiserie',
    'Maçonnerie',
    'Peinture',
    'Carrelage',
    'Couverture',
    'Chauffage',
    'Climatisation',
    'Tuyauterie',
    'Ferronnerie',
    'Vitrerie',
    'Serrurerie',
    'Charpenterie'
  ];

  constructor(
    private demandeService: DemandeService,
    private equipementService: EquipementService,
    private interventionService: InterventionService,
    private ressourceService: RessourceService,
    private technicienListService: TechnicienListService,
    private authService: AuthService,
      private demandeAjoutService: DemandeAjoutService,
      private Router: Router,
private demandeAjoutMaterielService: DemandeAjoutMaterielService, // Utiliser le nouveau service seulement
private notificationService: NotifService,
  ) {}

  ngOnInit(): void {
    this.loadAllData();
    this.loadTechniciens();
    this.loadNotifications(); // AJOUTER CETTE LIGNE
  this.startNotificationPolling(); // AJOUTER CETTE LIGNE
  }

  ngAfterViewInit(): void {
    if (this.showMapModal) {
      setTimeout(() => this.initMap(), 100);
    }
  }

  ngOnDestroy(): void {
    this.notificationSubscription?.unsubscribe();
    this.unreadCountSubscription?.unsubscribe();
  }

  // Méthode pour charger les notifications
loadNotifications(): void {
  const userId = this.authService.getUserId();
  if (!userId) return;

  this.notificationService.getNotificationsByUser(userId).subscribe({
    next: (data: Notification[]) => {
      this.notifications = data;
      this.unreadCount = data.filter(n => !n.readable).length;
      console.log('📨 Notifications chargées:', data.length);
    },
    error: (err:any) => console.error('Erreur chargement notifications:', err)
  });
}

  private startNotificationPolling(): void {
  const userId = this.authService.getUserId();
  if (!userId) return;

  // Poll toutes les 10 secondes
  this.notificationSubscription = this.notificationService.pollUnreadCount(userId).pipe(
    switchMap((response: { unreadCount: number }) => of(response.unreadCount))
  ).subscribe({
    next: (unreadCount: number) => {
      this.unreadCount = unreadCount;
    },
    error: (err: any) => console.error('Erreur polling notifications:', err)
  });
}
toggleNotificationsDropdown(): void {
  this.showNotificationsDropdown = !this.showNotificationsDropdown;
  if (this.showNotificationsDropdown) {
    this.loadNotifications();
  }
}

  markAsRead(notification: Notification): void {
  if (notification.readable) return;

  this.notificationService.markAsRead(notification.idNotification).subscribe({
    next: () => {
      notification.readable = true;
      this.unreadCount = this.notifications.filter(n => !n.readable).length;
      console.log('✅ Notification marquée comme lue');
    },
    error: (err:any) => console.error('Erreur marquage notification:', err)
  });
}

  formatNotificationDate(dateStr: string): string {
  return this.notificationService.formatNotificationDate(dateStr);
}

loadTechniciens(): void {
    this.technicienListService.getAllTechniciens().subscribe({
      next: (data) => {
        this.techniciens = data || [];
        console.log('Techniciens chargés:', this.techniciens.length);
      },
      error: (err) => {
        console.error('Erreur chargement techniciens:', err);
        console.error('Détails erreur:', err.status, err.message, err.error);
        this.techniciens = []; // Initialiser à vide en cas d'erreur
        alert('Impossible de charger la liste des techniciens. Vérifiez que le backend est démarré et accessible.');
      }
    });
  }
  openTechniciensModal(): void {
    this.showTechniciensModal = true;
    this.loadTechniciens(); // recharge à chaque ouverture
  }

  openAddTechnicienForm(): void {
    this.openRegisterTechnicienForm();
  }

  loadAllData(): void {
    this.loadDemandes();
    this.loadEquipements();
    this.loadRessources();
    this.loadInterventions();
  }

loadDemandes(): void {
    this.demandeService.getAllDemandesWithCitoyenNames().subscribe({
        next: (data: any[]) => {
            // Transformer les données pour qu'elles correspondent à l'interface Demande
            this.demandes = data.map(item => {
                const demande = item.demande;
                // Ajouter les informations du citoyen à la demande
                demande.citoyenNom = item.citoyenNom;
                demande.citoyenEmail = item.citoyenEmail;
                demande.citoyenTelephone = item.citoyenTelephone;
                demande.citoyenAdresse = item.citoyenAdresse;
                return demande;
            });
            this.appliquerFiltre();
            this.updateStats();
        },
        error: (error) => {
            console.error('Erreur chargement demandes avec noms:', error);
            // Fallback à l'ancienne méthode
            this.demandeService.getAllDemandes().subscribe({
                next: (data) => {
                    this.demandes = data;
                    this.appliquerFiltre();
                    this.updateStats();
                },
                error: (err) => {
                    console.error('Erreur chargement demandes de base:', err);
                    alert('Erreur lors du chargement des demandes');
                }
            });
        }
    });
}
// Nouvelle méthode pour charger tous les noms des citoyens
  loadCitoyenNames(): void {
    if (this.demandes.length === 0) return;

    // Filtrer les demandes qui ont un citoyenId non anonyme
    const demandesAvecCitoyen = this.demandes.filter(d =>
      !d.isAnonymous && d.citoyenId
    );

    if (demandesAvecCitoyen.length === 0) return;

    // Créer un Set pour éviter les doublons (si plusieurs demandes du même citoyen)
    const citoyenIds = new Set<number>();
    demandesAvecCitoyen.forEach(d => {
      const id = typeof d.citoyenId === 'string'
        ? parseInt(d.citoyenId)
        : (d.citoyenId as number);
      if (id) citoyenIds.add(id);
    });

    // Charger les informations pour chaque citoyen
    Array.from(citoyenIds).forEach(citoyenId => {
      this.loadCitoyenInfoForAllDemandes(citoyenId);
    });
  }
// Méthode pour charger les infos d'un citoyen et les associer à toutes ses demandes
  loadCitoyenInfoForAllDemandes(citoyenId: number): void {
    // Trouver toutes les demandes de ce citoyen
    const demandesDuCitoyen = this.demandes.filter(d => {
      if (d.isAnonymous) return false;
      const id = typeof d.citoyenId === 'string'
        ? parseInt(d.citoyenId)
        : (d.citoyenId as number);
      return id === citoyenId;
    });

    if (demandesDuCitoyen.length === 0) return;

    // Prendre une demande pour récupérer les infos (elles sont les mêmes pour toutes)
    const premiereDemande = demandesDuCitoyen[0];

    this.demandeService.getCitoyenDetails(premiereDemande.id).subscribe({
      next: (data) => {
        // Associer ces infos à toutes les demandes de ce citoyen
        demandesDuCitoyen.forEach(demande => {
          this.citoyenInfos.set(demande.id, data);
        });
      },
      error: (err) => {
        console.error('Erreur chargement info citoyen:', err);
      }
    });
  }

  // Méthode pour obtenir le nom complet du citoyen
  getCitoyenName(demandeId: number): string {
    const demande = this.demandes.find(d => d.id === demandeId);
    if (!demande) return 'Inconnu';

    if (demande.isAnonymous) {
        return '🎭 Anonyme';
    }

    // Vérifier si on a déjà les détails du citoyen
    if (this.citoyenInfos.has(demandeId)) {
        const info = this.citoyenInfos.get(demandeId);
        if (info.anonyme) return '🎭 Anonyme';
        if (info.nom) {
            const prenom = info.prenom || '';
            return prenom ? `${prenom} ${info.nom}` : info.nom;
        }
    }

    // Sinon, charger les détails
    if (demande.citoyenId && !this.citoyenInfos.has(demandeId)) {
        this.loadCitoyenDetailsOnDemand(demandeId);
    }

    // Retourner temporairement l'ID
    return demande.citoyenId ? `Citoyen #${demande.citoyenId}` : 'Non renseigné';
}

// Nouvelle méthode pour charger les détails à la demande
loadCitoyenDetailsOnDemand(demandeId: number): void {
    this.demandeService.getCitoyenDetails(demandeId).subscribe({
        next: (data) => {
            this.citoyenInfos.set(demandeId, data);
            // Forcer la mise à jour de l'affichage
            this.demandesFiltrees = [...this.demandesFiltrees];
        },
        error: (err) => console.error('Erreur chargement détails:', err)
    });
}


  loadEquipements(): void {
    this.equipementService.getAllEquipements().subscribe({
      next: (data) => this.equipements = data,
      error: (error) => {
        console.error('Erreur chargement équipements:', error);
        alert('Erreur lors du chargement des équipements');
      }
    });
  }

  loadInterventions(): void {
    this.interventionService.getAllInterventions().subscribe({
      next: (data) => {
        this.interventions = data;
        this.updateStats(); // Mettre à jour les statistiques après le chargement
      },
      error: (error) => {
        console.error('Erreur chargement interventions:', error);
        alert('Erreur lors du chargement des interventions');
      }
    });
  }

  loadRessources(): void {
    this.ressourceService.getAll().subscribe({
      next: (data) => this.ressources = data,
      error: (error) => {
        console.error('Erreur chargement matériels:', error);
      }
    });
  }

  updateStats(): void {
    this.demandesPendantes = this.demandes.filter(d =>
      d.etat === 'SOUMISE' || d.etat === 'EN_ATTENTE'
    ).length;
    this.demandesTraitees = this.demandes.filter(d => d.etat === 'TRAITEE').length;
    this.interventionsEnCoursCount = this.interventions.filter(i =>
      i.etat === 'EN_ATTENTE' || i.etat === 'EN_COURS'
    ).length;
  }

  filtrerDemandes(filtre: 'TOUS' | 'NON_TRAITEES' | 'TRAITEES'): void {
    this.filtreActif = filtre;
    this.appliquerFiltre();
  }

  private appliquerFiltre(): void {
    switch (this.filtreActif) {
      case 'NON_TRAITEES':
        this.demandesFiltrees = this.demandes.filter(d =>
          d.etat === 'SOUMISE' || d.etat === 'EN_ATTENTE'
        );
        break;
      case 'TRAITEES':
        this.demandesFiltrees = this.demandes.filter(d => d.etat === 'TRAITEE');
        break;
      default:
        this.demandesFiltrees = this.demandes;
    }
  }

  planifierDemande(demande: Demande): void {
    if (demande.etat === 'TRAITEE') {
      alert('Cette demande est déjà planifiée !');
      return;
    }

    // Ouvrir le modal de planification
    this.selectedDemande = demande;
    this.planificationData = {
      demandeId: demande.id,
      technicienId: null,
      datePlanifiee: new Date().toISOString().split('T')[0], // Aujourd'hui par défaut
      heureDebut: '09:00',
      heureFin: '17:00',
      dureeMinutes: 60,
      priorite: 'NORMALE',
      budget: 500,
      description: demande.description,
      typeIntervention: demande.category || '',
      equipementIds: [],
      ressourceIds: [],
      mainDOeuvreIds: [],
      remarques: ''
    };
    this.loadTechniciensDisponibles();
    this.showPlanificationModal = true;
  }

  loadTechniciensDisponibles(): void {
    // Charger tous les techniciens disponibles
    this.technicienListService.getAllTechniciens().subscribe({
      next: (data) => {
        this.techniciensDisponibles = data.filter(t => t.disponibilite !== false);
      },
      error: (err) => {
        console.error('Erreur chargement techniciens:', err);
        this.techniciensDisponibles = [];
      }
    });
  }

  validerPlanification(): void {
    if (!this.selectedDemande) {
      alert('Aucune demande sélectionnée');
      return;
    }

    // Validation des champs obligatoires
    if (!this.planificationData.technicienId) {
      alert('Veuillez sélectionner un technicien');
      return;
    }

    if (!this.planificationData.datePlanifiee) {
      alert('Veuillez sélectionner une date');
      return;
    }

    if (!this.planificationData.budget || this.planificationData.budget <= 0) {
      alert('Veuillez entrer un budget valide');
      return;
    }

    // Confirmation
    if (!confirm('Valider la planification de cette intervention ?')) {
      return; // L'utilisateur a annulé
    }

    // Préparer les données pour l'envoi
    const requestData: any = {
      demandeId: this.selectedDemande.id,
      technicienId: Number(this.planificationData.technicienId),
      datePlanifiee: this.planificationData.datePlanifiee,
      priorite: this.convertPrioriteToBackend(this.planificationData.priorite || 'NORMALE'),
      budget: Number(this.planificationData.budget),
      description: this.planificationData.description || this.selectedDemande.description,
      typeIntervention: this.planificationData.typeIntervention || '',
      remarques: this.planificationData.remarques || '',
      equipementIds: this.planificationData.equipementIds || [],
      ressourceIds: this.planificationData.ressourceIds || [],
      mainDOeuvreIds: this.planificationData.mainDOeuvreIds || []
    };

    // Ajouter heureDebut si fournie
    if (this.planificationData.heureDebut) {
      requestData.heureDebut = this.planificationData.heureDebut;
    }

    // Ajouter heureFin si fournie
    if (this.planificationData.heureFin) {
      requestData.heureFin = this.planificationData.heureFin;
    }

    // Ajouter dureeMinutes si fournie
    if (this.planificationData.dureeMinutes) {
      requestData.dureeMinutes = Number(this.planificationData.dureeMinutes);
    }

    console.log('=== DÉBUT PLANIFICATION ===');
    console.log('Données à envoyer:', JSON.stringify(requestData, null, 2));
    console.log('Demande sélectionnée:', this.selectedDemande);

    // Appeler le nouvel endpoint de planification complète
    this.demandeService.planifierInterventionComplete(requestData).subscribe({
      next: (intervention) => {
        console.log('=== PLANIFICATION RÉUSSIE ===');
        console.log('Intervention créée:', intervention);

        if (this.selectedDemande) {
          this.selectedDemande.etat = 'TRAITEE';
        }

        this.updateStats();
        this.appliquerFiltre();
        this.loadInterventions();
        this.loadAllData(); // Recharger toutes les données

        alert('✅ Intervention planifiée avec succès !\n\n' +
              'Intervention #' + intervention.id + '\n' +
              'Technicien notifié\n' +
              'État: Planifiée');

        this.closePlanificationModal();
        this.closeDetailModal();
      },
      error: (error) => {
        console.error('=== ERREUR PLANIFICATION ===');
        console.error('Erreur complète:', error);
        console.error('Status:', error.status);
        console.error('Message:', error.message);
        console.error('Error body:', error.error);
        console.error('URL appelée:', error.url);

        let message = '❌ Erreur lors de la planification\n\n';

        if (error?.status === 0) {
          message += 'Impossible de contacter le serveur.\nVérifiez que le backend est démarré.';
        } else if (error?.status === 404) {
          message += 'Endpoint non trouvé.\nVérifiez la configuration du backend.';
        } else if (error?.status === 500) {
          message += 'Erreur serveur.\nVérifiez les logs du backend.';
        } else if (error?.error?.error) {
          message += 'Erreur: ' + error.error.error;
          if (error.error.details) {
            message += '\nDétails: ' + error.error.details;
          }
        } else if (error?.error?.details) {
          message += 'Détails: ' + error.error.details;
        } else if (error?.error?.message) {
          message += 'Message: ' + error.error.message;
        } else if (error?.message) {
          message += 'Message: ' + error.message;
        } else {
          message += 'Erreur inconnue. Vérifiez la console pour plus de détails.';
        }

        alert(message);
      }
    });
  }

  closePlanificationModal(): void {
    this.showPlanificationModal = false;
    this.selectedDemande = null;
    this.planificationData = {
      technicienId: null,
      datePlanifiee: '',
      heureDebut: '',
      heureFin: '',
      dureeMinutes: 60,
      priorite: 'NORMALE',
      budget: 500,
      equipementIds: [],
      ressourceIds: [],
      mainDOeuvreIds: [],
      remarques: ''
    };
  }

  onPlanificationComplete(result: any): void {
    console.log('Planification complétée:', result);

    if (result.success) {
      alert('✅ Planification enregistrée avec succès! Intervention #' + result.interventionId + ' créée.');
      this.closePlanificationModal();
      this.loadDemandes();
      this.loadInterventions();
    } else {
      alert('❌ Erreur lors de la planification');
    }
  }

  toggleEquipement(id: number): void {
    const index = this.planificationData.equipementIds.indexOf(id);
    if (index > -1) {
      this.planificationData.equipementIds.splice(index, 1);
    } else {
      this.planificationData.equipementIds.push(id);
    }
  }

  toggleRessource(id: number): void {
    const index = this.planificationData.ressourceIds.indexOf(id);
    if (index > -1) {
      this.planificationData.ressourceIds.splice(index, 1);
    } else {
      this.planificationData.ressourceIds.push(id);
    }
  }

  openAddEquipement(): void {
  this.editingEquipement = null;
  this.resetEquipementForm();
  this.showEquipementFormModal = true;
}

  // === MÉTHODES CRUD ===
editEquipement(equipement: Equipement): void {
  this.editingEquipement = equipement;
  this.currentEquipement = { ...equipement };
  this.showEquipementFormModal = true;
}
// === MÉTHODES POUR FERMER LES MODALS ===
closeEquipementForm(): void {
  this.showEquipementFormModal = false;
  this.editingEquipement = null;
  this.resetEquipementForm();
}

closeRessourceForm(): void {
  this.showRessourceFormModal = false;
  this.editingRessource = null;
  this.resetRessourceForm();
}

closeModal(): void {
  this.showTechniciensModal = false;
  this.showEquipementModal = false;
  this.showRessourceModal = false;
  this.showInterventionModal = false;
  this.showDetailModal = false;
  this.showPlanificationModal = false;
}

  // === DÉTAILS DEMANDE ===
  openDetailModal(demande: Demande): void {
    this.selectedDemande = demande;
    this.showDetailModal = true;
    this.citoyenDetails = null; // Réinitialiser

    // Si la demande n'est pas anonyme, charger les détails du citoyen
    if (!demande.isAnonymous && demande.citoyenId) {
        this.loadCitoyenDetails(demande.id);}
  }
// Méthode pour charger les détails du citoyen
loadCitoyenDetails(demandeId: number): void {
    this.demandeService.getCitoyenDetails(demandeId).subscribe({
        next: (data) => {
            this.citoyenDetails = data;
            console.log('Détails citoyen chargés:', data);
        },
        error: (err) => {
            console.error('Erreur chargement détails citoyen:', err);
            this.citoyenDetails = {
                message: 'Impossible de charger les informations'
            };
        }
    });
}
  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedDemande = null;
    this.citoyenDetails = null;
  }
// Méthode pour obtenir le nom complet du citoyen
getCitoyenFullName(): string {
    if (!this.citoyenDetails) return '';

    if (this.citoyenDetails.anonyme) {
        return 'Citoyen anonyme';
    }

    const nom = this.citoyenDetails.nom || '';
    const prenom = this.citoyenDetails.prenom || '';

    if (nom && prenom) {
        return `${prenom} ${nom}`;
    } else if (nom) {
        return nom;
    } else if (prenom) {
        return prenom;
    }

    return `Citoyen #${this.selectedDemande.citoyenId}`;
}

  // === GESTION ÉQUIPEMENTS ===
  openEquipementsModal(): void {
  this.showEquipementModal = true;
  this.loadEquipements();
}

  openInterventionsModal(): void {
    this.showInterventionModal = true;
    this.loadInterventions();
  }

  // === GESTION RESSOURCES (MATERIELS) ===
  openRessourcesModal(): void {
  this.showRessourceModal = true;
  this.loadRessources();
}

 openAddRessource(): void {
  this.editingRessource = null;
  this.resetRessourceForm();
  this.showRessourceFormModal = true;
}

  editRessource(ressource: RessourceMaterielle): void {
  this.editingRessource = ressource;
  this.currentRessource = { ...ressource };
  this.showRessourceFormModal = true;
}
private resetEquipementForm(): void {
  this.currentEquipement = {
    id: 0,
    nom: '',
    type: '',
    etat: 'FONCTIONNEL',
    fournisseurId: undefined,
    valeurAchat: 0,
    localisation: undefined,
    dateAchat: new Date().toISOString().split('T')[0],
    disponible: true,
    indisponibilites: []
  };
}

  deleteRessource(id: number): void {
  if (confirm('Êtes-vous sûr de vouloir supprimer ce matériel ?')) {
    this.ressourceService.delete(id).subscribe({
      next: () => {
        this.ressources = this.ressources.filter(r => r.id !== id);
        alert('✅ Matériel supprimé avec succès');
      },
      error: (error) => {
        console.error('Erreur suppression matériel:', error);
        alert('❌ Erreur lors de la suppression du matériel');
      }
    });
  }
}


 private resetForm(): void {
  this.resetEquipementForm();
  this.resetRessourceForm();
}

  private resetRessourceForm(): void {
  this.currentRessource = {
    id: 0,
    designation: '',
    quantiteEnStock: 0,
    valeurAchat: 0,
    fournisseurId: undefined,
    unite: ''
  };
}


  saveEquipement(): void {
  if (!this.currentEquipement.nom || !this.currentEquipement.type) {
    alert('Veuillez remplir le nom et le type d\'équipement');
    return;
  }

  const equipementToSave: Equipement = {
    ...this.currentEquipement,
    etat: this.currentEquipement.etat || 'FONCTIONNEL',
    dateAchat: this.currentEquipement.dateAchat || new Date().toISOString().split('T')[0],
    disponible: true // Toujours disponible à la création
  };

  const action = this.editingEquipement
    ? this.equipementService.updateEquipement(this.editingEquipement.id, equipementToSave)
    : this.equipementService.createEquipement(equipementToSave);

  action.subscribe({
    next: (savedEquipement) => {
      alert(this.editingEquipement ? '✅ Équipement modifié !' : '✅ Équipement ajouté !');
      this.closeEquipementForm();
      this.loadEquipements();
    },
    error: (error) => {
      console.error('Erreur sauvegarde équipement:', error);
      alert('❌ Erreur lors de la sauvegarde: ' + (error.error?.message || error.message));
    }
  });
}


  saveRessource(): void {
  if (!this.currentRessource.designation) {
    alert('Veuillez remplir la désignation');
    return;
  }

  if (this.currentRessource.quantiteEnStock < 0) {
    alert('La quantité ne peut pas être négative');
    return;
  }

  if (this.currentRessource.valeurAchat < 0) {
    alert('Le prix ne peut pas être négatif');
    return;
  }

  const action = this.editingRessource
    ? this.ressourceService.update(this.editingRessource.id, this.currentRessource)
    : this.ressourceService.create(this.currentRessource);

  action.subscribe({
    next: (savedRessource) => {
      alert(this.editingRessource ? '✅ Matériel modifié !' : '✅ Matériel ajouté !');
      this.closeRessourceForm();
      this.loadRessources();
    },
    error: (error) => {
      console.error('Erreur sauvegarde matériel:', error);
      alert('❌ Erreur lors de la sauvegarde: ' + (error.error?.message || error.message));
    }
  });
}

  // Open photo in a new browser tab (simple viewer fallback)
  openPhotoModal(url: string, name?: string): void {
    const fullUrl = url.startsWith('http') ? url : 'http://localhost:8080' + url;
    window.open(fullUrl, '_blank');
  }

 deleteEquipement(id: number): void {
  if (confirm('Êtes-vous sûr de vouloir supprimer cet équipement ?')) {
    this.equipementService.deleteEquipement(id).subscribe({
      next: () => {
        this.equipements = this.equipements.filter(e => e.id !== id);
        alert('✅ Équipement supprimé avec succès');
      },
      error: (error) => {
        console.error('Erreur suppression équipement:', error);
        alert('❌ Erreur lors de la suppression de l\'équipement');
      }
    });
  }
}

  refreshAll(): void {
    this.loadAllData();
    alert('Données actualisées !');
  }

  // Carte des demandes non traitées
  openMapModal(): void {
    this.demandesNonTraitees = this.demandes.filter(d =>
      (d.etat === 'SOUMISE' || d.etat === 'EN_ATTENTE') &&
      d.localisation &&
      d.localisation.latitude &&
      d.localisation.longitude
    );

    if (this.demandesNonTraitees.length === 0) {
      alert('Aucune demande non traitée avec localisation disponible');
      return;
    }

    this.showMapModal = true;
    setTimeout(() => this.initMap(), 100);
  }

  initMap(): void {
    // Initialiser la carte centrée sur Tunis
    this.mapDemandes = L.map('mapChef').setView([36.8065, 10.1815], 12);

    // Ajouter la couche OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.mapDemandes);

    // Ajouter les marqueurs pour chaque demande non traitée
    this.demandesNonTraitees.forEach(demande => {
      if (demande.localisation) {
        const color = this.getMarkerColor(demande.priority);
        const icon = this.createColoredIcon(color);

        const marker = L.marker(
          [demande.localisation.latitude, demande.localisation.longitude],
          { icon: icon }
        ).addTo(this.mapDemandes!);

        // Popup avec informations
        const popupContent = `
          <div style="font-family: Arial; padding: 5px;">
            <strong style="color: ${color};">Demande #${demande.id}</strong><br/>
            <strong>Priorité:</strong> ${this.getPriorityText(demande.priority)}<br/>
            <strong>Description:</strong> ${demande.description.substring(0, 50)}...<br/>
            <strong>Catégorie:</strong> ${demande.category || 'N/A'}<br/>
            <strong>Date:</strong> ${demande.dateSoumission}
          </div>
        `;
        marker.bindPopup(popupContent);
      }
    });

    // Ajuster la vue pour inclure tous les marqueurs
    if (this.demandesNonTraitees.length > 0) {
      const bounds = L.latLngBounds(
        this.demandesNonTraitees
          .filter(d => d.localisation)
          .map(d => [d.localisation!.latitude, d.localisation!.longitude] as [number, number])
      );
      this.mapDemandes.fitBounds(bounds, { padding: [50, 50] });
    }
  }

  createColoredIcon(color: string): L.DivIcon {
    return L.divIcon({
      className: 'custom-marker',
      html: `
        <div style="
          background-color: ${color};
          width: 30px;
          height: 30px;
          border-radius: 50%;
          border: 3px solid white;
          box-shadow: 0 2px 8px rgba(0,0,0,0.3);
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
          color: white;
          font-size: 16px;
        ">!</div>
      `,
      iconSize: [30, 30],
      iconAnchor: [15, 15]
    });
  }

  getMarkerColor(priority: string | undefined): string {
    switch (priority?.toUpperCase()) {
      case 'HIGH':
      case 'URGENT':
      case 'HAUTE':
        return '#f44336'; // Rouge
      case 'MEDIUM':
      case 'MOYENNE':
      case 'MOYEN':
        return '#FFC107'; // Jaune
      case 'LOW':
      case 'BAS':
      case 'BASSE':
        return '#4CAF50'; // Vert
      default:
        return '#FFC107'; // Jaune par défaut
    }
  }

  getPriorityText(priority: string | undefined): string {
    switch (priority?.toUpperCase()) {
      case 'HIGH':
      case 'URGENT':
      case 'HAUTE':
        return '🔴 Urgent';
      case 'MEDIUM':
      case 'MOYENNE':
      case 'MOYEN':
        return '🟡 Moyen';
      case 'LOW':
      case 'BAS':
      case 'BASSE':
        return '🟢 Bas';
      default:
        return '🟡 Moyen';
    }
  }

  closeMapModal(): void {
    if (this.mapDemandes) {
      this.mapDemandes.remove();
      this.mapDemandes = undefined;
    }
    this.showMapModal = false;
  }

  // Carte des interventions en cours
  openMapInterventionsModal(): void {
    // Filtrer les interventions en cours et trouver leurs demandes associées
    this.interventionsEnCours = this.interventions.filter(i =>
      i.etat === 'EN_COURS' || i.etat === 'EN_ATTENTE'
    );

    if (this.interventionsEnCours.length === 0) {
      alert('Aucune intervention en cours disponible');
      return;
    }

    this.showMapInterventionsModal = true;
    setTimeout(() => this.initMapInterventions(), 100);
  }

  initMapInterventions(): void {
    // Initialiser la carte centrée sur Tunis
    this.mapInterventions = L.map('mapInterventions').setView([36.8065, 10.1815], 12);

    // Ajouter la couche OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.mapInterventions);

    let markersAdded = 0;
    const bounds: [number, number][] = [];

    // Ajouter les marqueurs pour chaque intervention en cours
    this.interventionsEnCours.forEach(intervention => {
      // Trouver la demande associée pour obtenir la localisation
      const demande = this.demandes.find(d => d.id === intervention.demandeId);

      if (demande?.localisation?.latitude && demande?.localisation?.longitude) {
        const color = this.getInterventionColor(intervention.etat);
        const icon = this.createInterventionIcon(color);

        const marker = L.marker(
          [demande.localisation.latitude, demande.localisation.longitude],
          { icon: icon }
        ).addTo(this.mapInterventions!);

        bounds.push([demande.localisation.latitude, demande.localisation.longitude]);
        markersAdded++;

        // Popup avec informations
        const popupContent = `
          <div style="font-family: Arial; padding: 5px;">
            <strong style="color: ${color};">Intervention #${intervention.id}</strong><br/>
            <strong>État:</strong> ${this.getInterventionEtatText(intervention.etat)}<br/>
            <strong>Priorité:</strong> ${intervention.priorite}<br/>
            <strong>Date planifiée:</strong> ${intervention.datePlanifiee}<br/>
            <strong>Budget:</strong> ${intervention.budget} DT<br/>
            <strong>Demande associée:</strong> #${demande.id}
          </div>
        `;
        marker.bindPopup(popupContent);
      }
    });

    if (markersAdded === 0) {
      alert('Aucune intervention avec localisation disponible');
      this.closeMapInterventionsModal();
      return;
    }

    // Ajuster la vue pour inclure tous les marqueurs
    if (bounds.length > 0) {
      const latLngBounds = L.latLngBounds(bounds);
      this.mapInterventions.fitBounds(latLngBounds, { padding: [50, 50] });
    }
  }

  createInterventionIcon(color: string): L.DivIcon {
    return L.divIcon({
      className: 'custom-marker',
      html: `
        <div style="
          background-color: ${color};
          width: 32px;
          height: 32px;
          border-radius: 50%;
          border: 3px solid white;
          box-shadow: 0 2px 8px rgba(0,0,0,0.3);
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
          color: white;
          font-size: 18px;
        ">🔧</div>
      `,
      iconSize: [32, 32],
      iconAnchor: [16, 16]
    });
  }

  getInterventionColor(etat: string | undefined): string {
    switch (etat?.toUpperCase()) {
      case 'EN_COURS':
        return '#2196F3'; // Bleu
      case 'EN_ATTENTE':
        return '#FF9800'; // Orange
      case 'TERMINEE':
        return '#4CAF50'; // Vert
      default:
        return '#9E9E9E'; // Gris
    }
  }

  getInterventionEtatText(etat: string | undefined): string {
    switch (etat?.toUpperCase()) {
      case 'EN_COURS':
        return '🔵 En cours';
      case 'EN_ATTENTE':
        return '🟠 En attente';
      case 'TERMINEE':
        return '🟢 Terminée';
      default:
        return '⚪ Inconnu';
    }
  }

  closeMapInterventionsModal(): void {
    if (this.mapInterventions) {
      this.mapInterventions.remove();
      this.mapInterventions = undefined;
    }
    this.showMapInterventionsModal = false;
  }

  // Helper pour afficher les états
  getEtatBadgeClass(etat: string): string {
    switch (etat) {
      case 'SOUMISE':
      case 'EN_ATTENTE':
        return 'status pending';
      case 'TRAITEE':
        return 'status done';
      case 'FONCTIONNEL':
        return 'status done';
      case 'DEFECTUEUX':
        return 'status error';
      case 'EN_MAINTENANCE':
        return 'status warning';
      default:
        return 'status pending';
    }
  }

  getEtatText(etat: string): string {
    switch (etat) {
      case 'SOUMISE': return 'En attente';
      case 'EN_ATTENTE': return 'En attente';
      case 'TRAITEE': return 'Planifiée';
      case 'FONCTIONNEL': return 'Fonctionnel';
      case 'DEFECTUEUX': return 'Défectueux';
      case 'EN_MAINTENANCE': return 'En maintenance';
      default: return etat;
    }
  }

  /**
   * Convertit la priorité du frontend vers le format backend
   * Backend accepte seulement: URGENTE, PLANIFIEE
   */
  convertPrioriteToBackend(priorite: string): string {
    switch (priorite?.toUpperCase()) {
      case 'URGENTE':
      case 'CRITIQUE':
      case 'HAUTE':
        return 'URGENTE';
      case 'PLANIFIEE':
      case 'NORMALE':
      case 'MOYENNE':
      case 'BASSE':
      default:
        return 'PLANIFIEE';
    }
  }

  getPhotoUrl(url: string): string {
    if (!url) return '';
    // Si l'URL commence par /api, ajouter le baseURL
    if (url.startsWith('/api')) {
      return `http://localhost:8080${url}`;
    }
    // Si l'URL commence par http, la retourner telle quelle
    if (url.startsWith('http')) {
      return url;
    }
    // Sinon, ajouter le préfixe complet
    return `http://localhost:8080/api/demandes/${url}`;
  }

  handleImageError(event: any): void {
    event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2VjZjBmMSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTQiIGZpbGw9IiM5NWE1YTYiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj5JbWFnZSBub24gZGlzcG9uaWJsZTwvdGV4dD48L3N2Zz4=';
  }

  // === SIDEBAR TOGGLE ===
  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  exportReport(): void {
    alert('Fonctionnalité d\'export en cours de développement');
    // TODO: Implémenter l'export des rapports
  }





// Ajouter ces variables dans la classe ChefDashboardComponent
showDemandeRessourceModal = false;
mesDemandesRessources: DemandeRessource[] = [];

// Données du formulaire (SEULEMENT RESSOURCE)
demandeRessourceData: CreateDemandeRessourceRequest = {
  designation: '',
  quantite: 1,
  budget: 0,
  justification: '',
  chefId: 0
};

// Injecter le service dans le constructeur


// Méthodes pour la modal de demande de ressource


closeDemandeRessourceModal(): void {
  this.showDemandeRessourceModal = false;
  this.resetDemandeRessourceForm();
}

resetDemandeRessourceForm(): void {
  const userId = this.authService.getUserId();

  this.demandeRessourceData = {
    designation: '',
    quantite: 1,
    budget: 0,
    justification: '',
    chefId: userId || 0  // ✅ Garder l'ID si disponible
  };
}




// Utiliser comme ceci dans openDemandeRessourceModal():
openDemandeRessourceModal(): void {
    const userId = this.authService.getUserId();
    if (!userId) {
      alert('Veuillez vous connecter');
      this.Router.navigate(['/login']); // ✅ Utiliser router
      return;
    }

    this.demandeRessourceData = {
      designation: '',
      quantite: 1,
      budget: 0,
      justification: '',
      chefId: userId // ✅ Toujours inclure chefId
    };

    this.showDemandeRessourceModal = true;
    this.loadMesDemandesRessources();
  }

  // Corriger submitDemandeRessource
  submitDemandeRessource(): void {
    // Validation du chefId
    const userId = this.authService.getUserId();
    if (!this.demandeRessourceData.chefId || this.demandeRessourceData.chefId <= 0) {
        alert('Erreur: Vous devez être connecté!');
        this.Router.navigate(['/login']);
        return;
    }
    if (!userId || userId <= 0) {
      alert('Erreur: Vous devez être connecté pour soumettre une demande.');
      this.Router.navigate(['/login']); // ✅ Utiliser router
      return;
    }

    // S'assurer que chefId est inclus
    this.demandeRessourceData.chefId = userId;

    // Autres validations...
    if (!this.demandeRessourceData.designation.trim()) {
      alert('Veuillez saisir la désignation de la ressource');
      return;
    }

    if (this.demandeRessourceData.quantite <= 0) {
      alert('La quantité doit être supérieure à 0');
      return;
    }

    if (this.demandeRessourceData.budget <= 0) {
      alert('Le budget doit être supérieur à 0');
      return;
    }

    if (!this.demandeRessourceData.justification.trim()) {
      alert('Veuillez justifier votre demande');
      return;
    }

    console.log('📨 Envoi de la demande de ressource:', this.demandeRessourceData);

    this.demandeAjoutMaterielService.creerDemandeRessource(this.demandeRessourceData).subscribe({
  next: (response: DemandeRessource) => { // Ajouter le type
        console.log('✅ Demande de ressource créée:', response);
        alert('✅ Demande soumise avec succès !\n\n' +
              'Votre demande a été envoyée à l\'administration pour validation.\n' +
              'Vous serez notifié de la décision.');
this.sendNotificationToAdmins(response);

        // Recharger les demandes
        this.loadMesDemandesRessources();

        // Fermer la modal
        this.closeDemandeRessourceModal();

        // Recharger les notifications
        this.loadNotifications();
      },
      error: (error: any) => {
        console.error('❌ Erreur création demande:', error);
        let errorMessage = 'Erreur lors de la soumission de la demande';

        if (error.status === 401 || error.status === 403) {
          errorMessage = 'Session expirée. Veuillez vous reconnecter.';
          this.authService.logout();
          this.Router.navigate(['/login']); // ✅ Utiliser Router
        } else if (error.error?.error) {
          errorMessage = error.error.error;
        }

        alert(`❌ ${errorMessage}`);
      }
    });
  }
// Méthodes pour les statuts
getEtatBadgeClassDemande(etat: string): string {
  switch (etat) {
    case 'EN_ATTENTE_ADMIN': return 'status pending';
    case 'ACCEPTEE': return 'status done';
    case 'REFUSEE': return 'status error';
    default: return 'status pending';
  }
}

getEtatTextDemande(etat: string): string {
  switch (etat) {
    case 'EN_ATTENTE_ADMIN': return '⏳ En attente admin';
    case 'ACCEPTEE': return '✅ Acceptée';
    case 'REFUSEE': return '❌ Refusée';
    default: return etat;
  }
}
voirMotifRefus(demande: DemandeRessource): void {
    if (demande.motifRefus) {
      alert(`📝 Motif de refus - Demande #${demande.id}\n\n${demande.motifRefus}`);
    } else {
      alert('Aucun motif de refus disponible.');
    }
  }
// Ajoutez après la méthode voirMotifRefus() :

loadMesDemandesRessources(): void {
  const chefId = this.authService.getUserId();
  if (!chefId) return;

  // Utiliser la méthode spécifique pour les ressources du chef
  this.demandeAjoutMaterielService.getDemandesRessourcesParChef(chefId).subscribe({
  next: (demandes: DemandeRessource[]) => {
      this.mesDemandesRessources = demandes.sort((a, b) =>
        new Date(b.dateDemande).getTime() - new Date(a.dateDemande).getTime()
      );
      console.log('📦 Mes demandes de ressources:', this.mesDemandesRessources.length);
    },
    error: (error: any) => {
      console.error('Erreur chargement demandes:', error);
    }
  });
}

voirDetailsDemandeRessource(demande: DemandeRessource): void {
  let message = `📦 Détails de la demande #${demande.id}\n\n`;
  message += `Désignation: ${demande.designation}\n`;
  message += `Quantité demandée: ${demande.quantite} unités\n`;
  message += `Budget estimé: ${demande.budget} DT\n`;
  message += `Date de demande: ${new Date(demande.dateDemande).toLocaleDateString('fr-FR')}\n`;
  message += `Statut: ${this.getEtatTextDemande(demande.etat)}\n`;
  message += `Justification:\n${demande.justification}\n`;

  if (demande.motifRefus) {
    message += `\n❌ Motif du refus:\n${demande.motifRefus}`;
  }

  if (demande.dateTraitement) {
    message += `\n📅 Traitée le: ${new Date(demande.dateTraitement).toLocaleDateString('fr-FR')}`;
  }

  alert(message);
}
private sendNotificationToAdmins(demande: DemandeRessource): void {
  const chefId = this.authService.getUserId();
  if (!chefId) return;

  const message = `📦 Nouvelle demande de ressource #${demande.id}\n` +
                 `Désignation: ${demande.designation}\n` +
                 `Quantité: ${demande.quantite} unités\n` +
                 `Budget: ${demande.budget} DT\n` +
                 `Chef: #${chefId}`;

  // Utiliser la méthode testCreateNotification au lieu de createNotificationForDemand
  this.notificationService.testCreateNotification(chefId, message).subscribe({
    next: (response: any) => {
      console.log('📨 Notification envoyée:', response);
    },
    error: (error: any) => {
      console.error('❌ Erreur envoi notification:', error);
    }
  });
}

  // ✅ MÉTHODES POUR L'ENREGISTREMENT TECHNICIEN
  openRegisterTechnicienForm(): void {
    this.showRegisterTechnicienForm = true;
  }

  closeRegisterTechnicienForm(): void {
    this.showRegisterTechnicienForm = false;
    this.resetTechnicienForm();
    this.loadTechniciens();
  }

  resetTechnicienForm(): void {
    this.technicienRegisterData = {
      nom: '',
      prenom: '',
      email: '',
      motDePasse: '',
      role: RoleType.TECHNICIEN,
      telephone: '',
      competence: '',
      disponibilite: true
    };
    this.technicienConfirmPassword = '';
    this.technicienErrorMessage = '';
    this.technicienSuccessMessage = '';
  }

  ajouterCompetenceTechnicien(competence: string): void {
    this.technicienRegisterData.competence = competence;
  }

  supprimerCompetenceTechnicien(): void {
    this.technicienRegisterData.competence = '';
  }

  submitRegisterTechnicien(): void {
    this.technicienErrorMessage = '';
    this.technicienSuccessMessage = '';

    // Validation
    if (!this.technicienRegisterData.nom?.trim()) {
      this.technicienErrorMessage = 'Le nom est obligatoire';
      return;
    }

    if (!this.technicienRegisterData.prenom?.trim()) {
      this.technicienErrorMessage = 'Le prénom est obligatoire';
      return;
    }

    if (!this.technicienRegisterData.email?.trim()) {
      this.technicienErrorMessage = 'L\'email est obligatoire';
      return;
    }

    if (!this.technicienRegisterData.motDePasse) {
      this.technicienErrorMessage = 'Le mot de passe est obligatoire';
      return;
    }

    if (!this.technicienRegisterData.telephone?.trim()) {
      this.technicienErrorMessage = 'Le téléphone est obligatoire';
      return;
    }

    if (!this.technicienRegisterData.competence?.trim()) {
      this.technicienErrorMessage = 'La compétence est obligatoire';
      return;
    }

    if (this.technicienRegisterData.motDePasse !== this.technicienConfirmPassword) {
      this.technicienErrorMessage = 'Les mots de passe ne correspondent pas';
      return;
    }

    if (this.technicienRegisterData.motDePasse.length < 6) {
      this.technicienErrorMessage = 'Le mot de passe doit contenir au moins 6 caractères';
      return;
    }

    const emailRegex = /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/;
    if (!emailRegex.test(this.technicienRegisterData.email)) {
      this.technicienErrorMessage = 'Format d\'email invalide';
      return;
    }

    this.technicienIsLoading = true;

    this.authService.register(this.technicienRegisterData).subscribe({
      next: (response) => {
        console.log('✅ Technicien enregistré avec succès', response);
        this.technicienSuccessMessage = 'Technicien enregistré avec succès!';
        this.technicienIsLoading = false;

        setTimeout(() => {
          this.closeRegisterTechnicienForm();
        }, 1500);
      },
      error: (error) => {
        console.error('❌ Erreur d\'enregistrement', error);
        this.technicienErrorMessage = this.getTechnicienErrorMessage(error);
        this.technicienIsLoading = false;
      }
    });
  }

  private getTechnicienErrorMessage(error: any): string {
    if (error.status === 0) {
      return 'Impossible de se connecter au serveur. Vérifiez que le serveur est démarré.';
    }

    if (error.error?.message) {
      return error.error.message;
    }

    if (error.message) {
      return error.message;
    }

    return 'Erreur lors de l\'enregistrement. Veuillez réessayer.';
  }
  // Dans chef-dashboard.component.ts
// Ajouter cette méthode
// Dans la classe ChefDashboardComponent
interventionsTerminees: Intervention[] = [];
showInterventionsTermineesModal = false;
ouvrirModalInterventionsTerminees(): void {
  this.interventionsTerminees = this.interventions.filter(i => i.etat === 'TERMINEE');

  if (this.interventionsTerminees.length === 0) {
    alert('Aucune intervention terminée');
    return;
  }

  this.showInterventionsTermineesModal = true;
}
closeInterventionsTermineesModal(): void {
  this.showInterventionsTermineesModal = false;
  this.interventionsTerminees = [];
}

}
