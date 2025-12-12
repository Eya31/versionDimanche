import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService, CreateTechnicienRequest, CreateChefServiceRequest } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { InterventionService } from '../../services/intervention.service';
import { Intervention } from '../../models/intervention.model';
import { Subscription } from 'rxjs';
import * as L from 'leaflet';
import { switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { Observable } from 'rxjs';
import { RessourceService } from '../../services/ressource.service';
import { DemandeAjoutService } from '../../services/demande-ajout.service';
import {
  DemandeAjoutMaterielService,
  DemandeRessource,
  DemandeAjoutMateriel
} from '../../services/demande-ajout-materiel.service';
import { RessourceMaterielle } from '../../models/ressource.model';
import { NotificationService, Notification } from '../../services/notification.service'; // IMPORT CORRIGÉ

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit, OnDestroy, AfterViewInit {
  users: any[] = [];
  showCreateTechForm = false;
  showCreateChefForm = false;

  // Interventions et carte
  interventionsEnCours: Intervention[] = [];
  private mapInterventions?: L.Map;

  // Notifications

  notifications: Notification[] = [];
  unreadCount = 0;
  showNotificationsDropdown = false;
  private notificationSubscription?: Subscription;
  private unreadCountSubscription?: Subscription;
// Ajouter ces variables dans la classe AdminDashboardComponent
demandesRessources: DemandeRessource[] = [];
demandesRessourcesEnAttente: DemandeRessource[] = [];
demandesRessourcesTraitees: DemandeRessource[] = [];
demandeRessourceFilter: string = 'TOUTES';
showRefusRessourceModal = false;
selectedDemandeRessource: DemandeRessource | null = null;
motifRefusRessource: string = '';
  newTechnicien: CreateTechnicienRequest = {
    nom: '',
    email: '',
    motDePasse: '',
    competences: []
  };
  loading = false;

  newChef: CreateChefServiceRequest = {
    nom: '',
    email: '',
    motDePasse: '',
    departement: ''
  };

  competenceInput = '';
 showMenu = false;

  // ✅ Ajouter cette propriété si vous voulez stocker les ressources
  stockRessources: RessourceMaterielle[] = [];

  loadingStock = false;
  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router,
    private interventionService: InterventionService,
    private demandeAjoutService: DemandeAjoutService,
    private demandeAjoutMaterielService: DemandeAjoutMaterielService,
    private ressourceService: RessourceService,
    private notificationService: NotificationService // AJOUTEZ CECI
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadNotifications();
    this.startNotificationPolling();
    this.loadInterventionsEnCours();
    this.loadDemandesRessources();
  }
 // === GESTION DES NOTIFICATIONS ===
  loadNotifications(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.notificationService.getNotificationsByUser(userId).subscribe({
      next: (data: Notification[]) => {
        this.notifications = data;
        this.unreadCount = data.filter(n => !n.readable).length;
        console.log('📨 Notifications chargées:', data.length);
      },
      error: (err: any) => console.error('Erreur chargement notifications:', err)
    });
  }

  private startNotificationPolling(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    // Poll toutes les 30 secondes
    this.notificationSubscription = this.notificationService.pollNotifications(userId).subscribe({
      next: (notifications: Notification[]) => {
        this.notifications = notifications;
        this.unreadCount = notifications.filter(n => !n.readable).length;
      },
      error: (err: any) => console.error('Erreur polling notifications:', err)
    });
  }
  ngAfterViewInit(): void {
    setTimeout(() => this.initMap(), 100);
  }
 loadStock(): void {
  this.ressourceService.getAll().subscribe({
    next: (ressources: RessourceMaterielle[]) => { // ✅ Ajouter le type
      this.stockRessources = ressources;
      console.log('Stock chargé:', ressources.length);
    },
    error: (error: any) => { // ✅ Ajouter le type
      console.error('Erreur chargement stock:', error);
    }
  });
}

  loadInterventionsEnCours(): void {
    this.interventionService.getAllInterventions().subscribe({
      next: (data: Intervention[]) => {
        this.interventionsEnCours = data.filter((i: Intervention) =>
          i.etat === 'EN_COURS' || i.etat === 'EN_ATTENTE'
        );
        setTimeout(() => this.initMap(), 100);
      },
      error: (err: any) => console.error('Erreur chargement interventions:', err)
    });
  }

  initMap(): void {
    const mapElement = document.getElementById('mapInterventionsAdmin');
    if (!mapElement || this.interventionsEnCours.length === 0) return;

    if (this.mapInterventions) {
      this.mapInterventions.remove();
    }

    this.mapInterventions = L.map('mapInterventionsAdmin').setView([36.8065, 10.1815], 12);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(this.mapInterventions);

    const interventionsAvecLocalisation = this.interventionsEnCours.filter(i =>
      i.localisation && i.localisation.latitude && i.localisation.longitude
    );

    interventionsAvecLocalisation.forEach((intervention: Intervention) => {
      if (intervention.localisation) {
        const color = intervention.etat === 'EN_COURS' ? '#2196F3' : '#FF9800';

        const marker = L.circleMarker(
          [intervention.localisation.latitude, intervention.localisation.longitude],
          {
            radius: 10,
            fillColor: color,
            color: 'white',
            weight: 2,
            opacity: 1,
            fillOpacity: 0.8
          }
        ).addTo(this.mapInterventions!);

        const popupContent = `
          <div style="min-width: 200px;">
            <h4 style="margin: 0 0 10px 0; color: #2c3e50; font-size: 16px;">
              Intervention #${intervention.id}
            </h4>
            <p style="margin: 5px 0;"><strong>État:</strong> ${intervention.etat}</p>
            <p style="margin: 5px 0;"><strong>Priorité:</strong> ${intervention.priorite || 'N/A'}</p>
            <p style="margin: 5px 0;"><strong>Date:</strong> ${new Date(intervention.datePlanifiee).toLocaleDateString('fr-FR')}</p>
            ${intervention.description ? `<p style="margin: 5px 0;"><strong>Description:</strong> ${intervention.description}</p>` : ''}
            ${intervention.budget ? `<p style="margin: 5px 0;"><strong>Budget:</strong> ${intervention.budget} DT</p>` : ''}
          </div>
        `;

        marker.bindPopup(popupContent);
      }
    });

    if (interventionsAvecLocalisation.length > 0) {
      const bounds = L.latLngBounds(
        interventionsAvecLocalisation.map(i =>
          [i.localisation!.latitude, i.localisation!.longitude] as [number, number]
        )
      );
      this.mapInterventions.fitBounds(bounds, { padding: [50, 50] });
    }
  }

  ngOnDestroy(): void {
    this.notificationSubscription?.unsubscribe();
    this.unreadCountSubscription?.unsubscribe();
    if (this.mapInterventions) {
      this.mapInterventions.remove();
    }
  }

  // === GESTION DES NOTIFICATIONS ===




  addCompetence(): void {
    if (this.competenceInput.trim()) {
      this.newTechnicien.competences.push(this.competenceInput.trim());
      this.competenceInput = '';
    }
  }

  removeCompetence(index: number): void {
    this.newTechnicien.competences.splice(index, 1);
  }

  deleteUser(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      this.adminService.deleteUser(id).subscribe({
        next: () => {
          alert('Utilisateur supprimé');
          this.loadUsers();
        },
        error: (error) => {
          console.error('Erreur suppression:', error);
          alert('Erreur lors de la suppression');
        }
      });
    }
  }

  resetTechForm(): void {
    this.newTechnicien = {
      nom: '',
      email: '',
      motDePasse: '',
      competences: []
    };
  }

  resetChefForm(): void {
    this.newChef = {
      nom: '',
      email: '',
      motDePasse: '',
      departement: ''
    };
  }

  getPriorityClass(priorite: string): string {
    switch(priorite?.toUpperCase()) {
      case 'URGENTE':
      case 'CRITIQUE':
        return 'priority-urgent';
      case 'PLANIFIEE':
      case 'NORMALE':
        return 'priority-normal';
      default:
        return 'priority-normal';
    }
  }

  getStatusClass(etat: string): string {
    switch(etat?.toUpperCase()) {
      case 'EN_COURS':
        return 'status-progress';
      case 'EN_ATTENTE':
        return 'status-pending';
      case 'TERMINEE':
        return 'status-done';
      default:
        return 'status-pending';
    }
  }

  getRoleClass(role: string): string {
    const roleMap: { [key: string]: string } = {
      'ADMIN': 'admin',
      'CHEF_SERVICE': 'chef',
      'TECHNICIEN': 'technicien',
      'CITOYEN': 'citoyen'
    };
    return roleMap[role?.toUpperCase()] || 'citoyen';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
// Méthodes pour les demandes de ressources
loadDemandesRessources(): void {
  this.loading = true;

  // Changer :
  // this.demandeAjoutService.getAllDemandesRessources().subscribe({

  // Par :
  this.demandeAjoutMaterielService.getAllDemandesRessources().subscribe({
    next: (demandes: DemandeRessource[]) => {
      this.demandesRessources = demandes.filter(d =>
        d.etat === 'EN_ATTENTE_ADMIN' ||
        d.etat === 'ACCEPTEE' ||
        d.etat === 'REFUSEE'
      );
      this.loading = false;
    },
    error: (error: any) => {
      console.error('Erreur chargement demandes:', error);
      alert('Erreur lors du chargement des demandes de ressources');
      this.loading = false;
    }
  });
}
filtrerDemandesRessources(): void {
  switch (this.demandeRessourceFilter) {
    case 'EN_ATTENTE':
      this.demandesRessourcesEnAttente = this.demandesRessources.filter(
        d => d.etat === 'EN_ATTENTE_ADMIN'
      );
      break;
    case 'TRAITEES':
      this.demandesRessourcesTraitees = this.demandesRessources.filter(
        d => d.etat === 'ACCEPTEE' || d.etat === 'REFUSEE'
      );
      break;
    default:
      // Toutes
      break;
  }
}

// Accepter une demande de ressource
// Dans admin-dashboard.component.ts, modifiez la méthode accepterDemandeRessource :

accepterDemandeRessource(demande: DemandeRessource): void {
  if (!confirm(`Accepter la demande "${demande.designation}" ?`)) {
    return;
  }

  const adminId = this.authService.getUserId();
  if (!adminId) {
    alert('Erreur: Admin non identifié');
    return;
  }

  this.loading = true;

  // 🔥 CORRECTION : Utiliser l'endpoint qui met à jour le stock
  this.demandeAjoutMaterielService.accepterDemandeRessource(demande.id, adminId).subscribe({
    next: (response: any) => {
      alert('✅ Demande acceptée avec succès !\nLe stock a été mis à jour.');
            this.sendAcceptNotificationToChef(demande, adminId);

      this.loadDemandesRessources();
      this.loadStock(); // Recharger le stock affiché
    },
    error: (error: any) => {
      console.error('Erreur acceptation demande:', error);

      let errorMessage = 'Erreur lors de l\'acceptation de la demande';
      if (error.status === 500 && error.error?.error?.includes("stock")) {
        errorMessage = 'Erreur lors de la mise à jour du stock. Vérifiez les logs serveur.';
      }

      alert(`❌ ${errorMessage}`);
      this.loading = false;
    }
  });
}

// Refuser une demande de ressource
refuserDemandeRessource(demande: DemandeRessource): void {
  const adminId = this.authService.getUserId();
  if (!adminId) {
    alert('Admin non connecté');
    return;
  }
  this.selectedDemandeRessource = demande;
  this.motifRefusRessource = '';
  this.showRefusRessourceModal = true;
}

confirmerRefusRessource(): void {
  if (!this.selectedDemandeRessource || !this.motifRefusRessource) return;

  const adminId = this.authService.getUserId();
  if (!adminId) {
    alert('Admin non connecté');
    return;
  }

  this.demandeAjoutMaterielService.refuserDemande(
    this.selectedDemandeRessource.id,
    adminId,
    this.motifRefusRessource
  ).subscribe({
    next: (response) => {
      console.log('❌ Demande refusée:', response);
      this.sendRefusNotificationToChef(this.selectedDemandeRessource!, this.motifRefusRessource);
      alert('✅ Demande refusée !\n\nLe chef a été notifié du motif.');
      this.closeRefusRessourceModal();
      this.loadDemandesRessources();
      this.loadNotifications();
    },
    error: (error) => {
      console.error('❌ Erreur refus:', error);
      alert('❌ Erreur lors du refus');
    }
  });
}

closeRefusRessourceModal(): void {
  this.showRefusRessourceModal = false;
  this.selectedDemandeRessource = null;
  this.motifRefusRessource = '';
}

// Voir détails d'une demande
voirDetailsDemandeRessourceAdmin(demande: DemandeRessource): void {
  let message = `📦 Détails de la demande #${demande.id}\n\n`;
  message += `Ressource: ${demande.designation}\n`;
  message += `Quantité demandée: ${demande.quantite} unités\n`;
  message += `Budget: ${demande.budget} DT\n`;
  message += `Chef: #${demande.chefId}\n`;
  message += `Date: ${new Date(demande.dateDemande).toLocaleDateString('fr-FR')}\n`;
  message += `Statut: ${this.getEtatTextDemande(demande.etat)}\n\n`;
  message += `Justification:\n${demande.justification}`;

  if (demande.motifRefus) {
    message += `\n\n❌ Motif du refus:\n${demande.motifRefus}`;
  }

  alert(message);
}


refuserDemande(demande: DemandeRessource): void {
  const motifRefus = prompt('Motif du refus :');
  if (!motifRefus || motifRefus.trim() === '') {
    alert('Veuillez saisir un motif de refus');
    return;
  }

  const adminId = this.authService.getUserId();
  if (!adminId) {
    alert('Erreur: Admin non identifié');
    return;
  }

  if (!confirm(`Refuser la demande "${demande.designation}" ?`)) {
    return;
  }

  // Changer :
  // this.demandeAjoutService.refuserDemande(demande.id, adminId, motifRefus).subscribe({

  // Par :
  this.demandeAjoutMaterielService.refuserDemande(demande.id, adminId, motifRefus).subscribe({
    next: (demandeMaj: DemandeAjoutMateriel) => {
      alert('✅ Demande refusée avec succès');
      this.loadDemandesRessources();
    },
    error: (error: any) => {
      console.error('Erreur refus demande:', error);
      alert('❌ Erreur lors du refus de la demande');
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
    case 'EN_ATTENTE_ADMIN': return '⏳ En attente';
    case 'ACCEPTEE': return '✅ Acceptée';
    case 'REFUSEE': return '❌ Refusée';
    default: return etat;
  }
}
// Ajoutez ces méthodes dans la classe AdminDashboardComponent :

get demandesEnAttenteCount(): number {
  return this.demandesRessources.filter(d => d.etat === 'EN_ATTENTE_ADMIN').length;
}

get demandesAccepteesCount(): number {
  return this.demandesRessources.filter(d => d.etat === 'ACCEPTEE').length;
}

get demandesRefuseesCount(): number {
  return this.demandesRessources.filter(d => d.etat === 'REFUSEE').length;
}

get demandesTraiteesCount(): number {
  return this.demandesRessources.filter(d =>
    d.etat === 'ACCEPTEE' || d.etat === 'REFUSEE'
  ).length;
}

getDemandesFiltrees(): DemandeRessource[] {
  switch (this.demandeRessourceFilter) {
    case 'EN_ATTENTE':
      return this.demandesRessources.filter(d => d.etat === 'EN_ATTENTE_ADMIN');
    case 'TRAITEES':
      return this.demandesRessources.filter(d =>
        d.etat === 'ACCEPTEE' || d.etat === 'REFUSEE'
      );
    default:
      return this.demandesRessources;
  }
}


markAsRead(notification: Notification): void {
    if (notification.readable) return;

    this.notificationService.markAsRead(notification.idNotification).subscribe({
      next: () => {
        notification.readable = true;
        this.unreadCount = this.notifications.filter(n => !n.readable).length;
      },
      error: (err: any) => console.error('Erreur marquage notification:', err)
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

toggleNotificationsDropdown(): void {
    this.showNotificationsDropdown = !this.showNotificationsDropdown;
    if (this.showNotificationsDropdown) {
      this.loadNotifications();
    }
  }

// Méthode pour envoyer notification d'acceptation au chef
  private sendAcceptNotificationToChef(demande: DemandeRessource, adminId: number): void {
    const message = `✅ Votre demande de ressource a été ACCEPTÉE !\n` +
                   `📋 Détails:\n` +
                   `   • Référence: #${demande.id}\n` +
                   `   • Désignation: ${demande.designation}\n` +
                   `   • Quantité: ${demande.quantite} unités\n` +
                   `   • Budget: ${demande.budget} DT\n` +
                   `🎉 La ressource a été ajoutée au stock avec succès.`;

    // Utilisez l'endpoint /create de NotificationController
    this.notificationService.testCreateNotification(demande.chefId, message).subscribe({
      next: (response: any) => {
        console.log('📨 Notification d\'acceptation envoyée:', response);
      },
      error: (error: any) => {
        console.error('❌ Erreur envoi notification d\'acceptation:', error);
      }
    });
  }

  // Méthode pour envoyer notification de refus
  private sendRefusNotificationToChef(demande: DemandeRessource, motif: string): void {
    const message = `❌ Votre demande de ressource a été REFUSÉE\n` +
                   `📋 Détails:\n` +
                   `   • Référence: #${demande.id}\n` +
                   `   • Désignation: ${demande.designation}\n` +
                   `   • Motif: ${motif}\n` +
                   `💡 Vous pouvez soumettre une nouvelle demande avec les corrections nécessaires.`;

    // Utilisez l'endpoint /create de NotificationController
    this.notificationService.testCreateNotification(demande.chefId, message).subscribe({
      next: (response: any) => {
        console.log('📨 Notification de refus envoyée:', response);
      },
      error: (error: any) => {
        console.error('❌ Erreur envoi notification de refus:', error);
      }
    });
  }
// ===============================================
// AJOUTER CES MÉTHODES MANQUANTES
// ===============================================

loadUsers(): void {
  this.adminService.getAllUsers().subscribe({
    next: (users) => {
      this.users = users;
    },
    error: (error) => {
      console.error('Erreur chargement utilisateurs:', error);
    }
  });
}

createTechnicien(): void {
  if (!this.newTechnicien.nom || !this.newTechnicien.email || !this.newTechnicien.motDePasse) {
    alert('Veuillez remplir tous les champs');
    return;
  }

  this.adminService.createTechnicien(this.newTechnicien).subscribe({
    next: () => {
      alert('Technicien créé avec succès');
      this.showCreateTechForm = false;
      this.resetTechForm();
      this.loadUsers();
    },
    error: (error) => {
      console.error('Erreur création technicien:', error);
      alert('Erreur lors de la création du technicien');
    }
  });
}

createChef(): void {
  if (!this.newChef.nom || !this.newChef.email || !this.newChef.motDePasse || !this.newChef.departement) {
    alert('Veuillez remplir tous les champs');
    return;
  }

  this.adminService.createChefService(this.newChef).subscribe({
    next: () => {
      alert('Chef de service créé avec succès');
      this.showCreateChefForm = false;
      this.resetChefForm();
      this.loadUsers();
    },
    error: (error) => {
      console.error('Erreur création chef:', error);
      alert('Erreur lors de la création du chef de service');
    }
  });
}
// Dans notif.service.ts, ajoutez cette méthode

}
