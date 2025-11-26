import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService, CreateTechnicienRequest, CreateChefServiceRequest } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { NotificationService, Notification } from '../../services/notification.service';
import { InterventionService } from '../../services/intervention.service';
import { Intervention } from '../../models/intervention.model';
import { Subscription } from 'rxjs';
import * as L from 'leaflet';

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

  newTechnicien: CreateTechnicienRequest = {
    nom: '',
    email: '',
    motDePasse: '',
    competences: []
  };

  newChef: CreateChefServiceRequest = {
    nom: '',
    email: '',
    motDePasse: '',
    departement: ''
  };

  competenceInput = '';

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService,
    private interventionService: InterventionService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadNotifications();
    this.startNotificationPolling();
    this.loadInterventionsEnCours();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.initMap(), 100);
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
}
