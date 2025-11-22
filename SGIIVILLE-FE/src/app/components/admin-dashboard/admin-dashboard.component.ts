import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService, CreateTechnicienRequest, CreateChefServiceRequest } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { NotificationService, Notification } from '../../services/notification.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  users: any[] = [];
  showCreateTechForm = false;
  showCreateChefForm = false;

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
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadNotifications();
    this.startNotificationPolling();
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

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
