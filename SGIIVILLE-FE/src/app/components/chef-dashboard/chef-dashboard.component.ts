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
import * as L from 'leaflet';
import { Subscription } from 'rxjs';
@Component({
  selector: 'app-chef-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
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
  showEquipementModal = false;
  showInterventionModal = false;
  showFormModal = false;
  showRessourceModal = false;
  showDetailModal = false;
  selectedDemande: Demande | null = null;
  editingEquipement: Equipement | null = null;
  editingRessource: RessourceMaterielle | null = null;

  currentEquipement: Equipement = {
    id: 0,
    type: '',
    etat: 'FONCTIONNEL',
    valeurAchat: 0,
    localisation: { latitude: 36.8065, longitude: 10.1815 }
  };

  currentRessource: RessourceMaterielle = {
    id: 0,
    designation: '',
    quantiteEnStock: 0,
    valeurAchat: 0
  } as RessourceMaterielle;

  filtreActif: 'TOUS' | 'NON_TRAITEES' | 'TRAITEES' = 'TOUS';

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
        this.techniciens = data;
      },
      error: (err) => {
        console.error('Erreur chargement techniciens:', err);
        alert('Impossible de charger la liste des techniciens');
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
      next: (data) => this.interventions = data,
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

  if (confirm(`Planifier l'intervention pour la demande #${demande.id} ?`)) {
    this.demandeService.planifierIntervention(demande.id).subscribe({
      next: (intervention) => {
        demande.etat = 'TRAITEE';  // Mise à jour immédiate dans l'interface
        this.updateStats();
        this.appliquerFiltre();
        this.loadInterventions();  // Recharge la liste des interventions
        alert('Intervention planifiée avec succès ! Intervention #' + intervention.id);
        this.closeDetailModal();
      },
      error: (error) => {
        console.error('Erreur complète:', error);

        // MESSAGE CLAIR AU LIEU DE [object Object]
        let message = 'Erreur lors de la planification';

        if (error?.error?.error) {
          message += ' : ' + error.error.error;
        } else if (error?.error?.details) {
          message += ' : ' + error.error.details;
        } else if (error?.error) {
          message += ' : ' + error.error;
        } else if (error?.message) {
          message += ' : ' + error.message;
        } else if (error?.status === 500) {
          message = 'Erreur serveur (500) – Contactez l’administrateur';
        }

        alert(message);
      }
    });
  }
}

  openAddEquipement() {
    this.editingEquipement = null;
    this.showEquipementForm = true;
  }

  editEquipement(equipement: any): void {
    this.editingEquipement = equipement;
    this.showEquipementForm = true;
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
    this.showFormModal = true;
  }

  editRessource(r: RessourceMaterielle): void {
    this.editingRessource = r;
    this.currentRessource = { ...r };
    this.showFormModal = true;
  }

  deleteRessource(id: number): void {
    if (!confirm('Supprimer ce matériel ?')) return;
    this.ressourceService.delete(id).subscribe({
      next: () => {
        this.ressources = this.ressources.filter(x => x.id !== id);
        alert('Matériel supprimé');
      },
      error: (error) => {
        console.error('Erreur suppression matériel:', error);
        alert('Erreur lors de la suppression');
      }
    });
  }

  closeModal(): void {
        this.showTechniciensModal = false;

    this.showEquipementModal = false;
    this.showInterventionModal = false;
    this.showFormModal = false;
    this.showRessourceModal = false;
    this.showDetailModal = false;
    this.editingEquipement = null;
    this.editingRessource = null;
    this.selectedDemande = null;
    this.resetForm();
  }

  private resetForm(): void {
    this.currentEquipement = {
      id: 0,
      type: '',
      etat: 'FONCTIONNEL',
      valeurAchat: 0,
      localisation: { latitude: 36.8065, longitude: 10.1815 }
    };
    this.resetRessourceForm();
  }

  private resetRessourceForm(): void {
    this.currentRessource = {
      id: 0,
      designation: '',
      quantiteEnStock: 0,
      valeurAchat: 0
    } as RessourceMaterielle;
  }

  saveEquipement(): void {
    if (!this.currentEquipement.type) {
      alert('Veuillez remplir le type d\'équipement');
      return;
    }

    const equipementData = {
      ...this.currentEquipement,
      fournisseur: this.currentEquipement.fournisseur || {
        id: 1,
        nom: 'Fournisseur Standard',
        email: 'contact@fournisseur.tn',
        telephone: '+216 70 000 000',
        adresse: ''
      }
    };

    const action = this.editingEquipement
      ? this.equipementService.updateEquipement(this.editingEquipement.id, equipementData)
      : this.equipementService.createEquipement(equipementData);

    action.subscribe({
      next: () => {
        alert(this.editingEquipement ? 'Équipement modifié !' : 'Équipement ajouté !');
        this.closeModal();
        this.loadEquipements();
      },
      error: (error) => {
        console.error('Erreur sauvegarde équipement:', error);
        alert('Erreur lors de la sauvegarde');
      }
    });
  }

  saveRessource(): void {
    const payload = { ...this.currentRessource } as RessourceMaterielle;
    if (this.editingRessource) {
      this.ressourceService.update(this.editingRessource.id, payload).subscribe({
        next: () => { this.loadRessources(); this.closeModal(); alert('Matériel modifié !'); },
        error: (error) => { console.error(error); alert('Erreur lors de la sauvegarde'); }
      });
    } else {
      this.ressourceService.create(payload).subscribe({
        next: () => { this.loadRessources(); this.closeModal(); alert('Matériel ajouté !'); },
        error: (error) => { console.error(error); alert('Erreur lors de la sauvegarde'); }
      });
    }
  }

  // Open photo in a new browser tab (simple viewer fallback)
  openPhotoModal(url: string, name?: string): void {
    const fullUrl = url.startsWith('http') ? url : 'http://localhost:8080' + url;
    window.open(fullUrl, '_blank');
  }

  deleteEquipement(id: number): void {
    if (confirm('Supprimer cet équipement ?')) {
      this.equipementService.deleteEquipement(id).subscribe({
        next: () => { this.equipements = this.equipements.filter(e => e.id !== id); alert('Équipement supprimé'); },
        error: (error) => { console.error('Erreur suppression:', error); alert('Erreur lors de la suppression'); }
      });
    }
  }

  refreshAll(): void {
    this.loadAllData();
    alert('Données actualisées !');
  }

  exportReport(): void {
    alert('Fonctionnalité export PDF en cours de développement...');
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
}
