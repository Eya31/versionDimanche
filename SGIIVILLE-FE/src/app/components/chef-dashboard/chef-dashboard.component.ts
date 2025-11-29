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
import { NotificationService, Notification } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { InterventionPlanificationComponent } from '../intervention-planification/intervention-planification.component';
import * as L from 'leaflet';
import { Subscription } from 'rxjs';
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

  // Stats
  demandesPendantes = 0;
  interventionsEnCoursCount = 0;
  demandesTraitees = 0;

  // Modals
  showInterventionModal = false;
  showFormModal = false;
  showDetailModal = false;
  showPlanificationModal = false;
  selectedDemande: Demande | null = null;

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

  constructor(
    private demandeService: DemandeService,
    private equipementService: EquipementService,
    private interventionService: InterventionService,
    private ressourceService: RessourceService,
    private technicienListService: TechnicienListService,
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAllData();
    this.loadTechniciens();
    this.loadNotifications();
    this.startNotificationPolling();
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

  // === GESTION DES NOTIFICATIONS ===
  loadNotifications(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.notificationService.getNotificationsByUser(userId).subscribe({
      next: (data) => {
        this.notifications = data.sort((a, b) =>
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.unreadCount = this.notifications.filter(n => !n.readable).length;
      },
      error: (err) => console.error('Erreur chargement notifications:', err)
    });
  }

  startNotificationPolling(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    // Poll toutes les 15 secondes pour le compteur
    this.unreadCountSubscription = this.notificationService.pollUnreadCount(userId).subscribe({
      next: (data) => {
        this.unreadCount = data.unreadCount;
      },
      error: (err) => console.error('Erreur polling notifications:', err)
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
      },
      error: (err) => console.error('Erreur marquage notification:', err)
    });
  }

  formatNotificationDate(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'À l\'instant';
    if (minutes < 60) return `Il y a ${minutes} min`;
    if (hours < 24) return `Il y a ${hours}h`;
    return `Il y a ${days}j`;
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


  loadAllData(): void {
    this.loadDemandes();
    this.loadEquipements();
    this.loadRessources();
    this.loadInterventions();
  }

  loadDemandes(): void {
    this.demandeService.getAllDemandes().subscribe({
      next: (data) => {
        this.demandes = data;
        this.appliquerFiltre();
        this.updateStats();
      },
      error: (error) => {
        console.error('Erreur chargement demandes:', error);
        alert('Erreur lors du chargement des demandes');
      }
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
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedDemande = null;
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
}
