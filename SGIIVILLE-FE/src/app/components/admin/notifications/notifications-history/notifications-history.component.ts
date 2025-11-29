import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NotificationService, Notification } from '../../../../services/notification.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-notifications-history',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './notifications-history.component.html',
  styleUrls: ['./notifications-history.component.css']
})
export class NotificationsHistoryComponent implements OnInit {
  allNotifications: Notification[] = [];
  filteredNotifications: Notification[] = [];
  loading = false;

  // Filtres
  filterRead: string = 'all'; // 'all', 'read', 'unread'
  searchTerm: string = '';

  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 20;
  totalPages: number = 1;

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadAllNotifications();
  }

  loadAllNotifications(): void {
    this.loading = true;
    const userId = this.authService.getUserId();
    if (!userId) {
      this.loading = false;
      return;
    }

    this.notificationService.getNotificationsByUser(userId).subscribe({
      next: (notifications) => {
        this.allNotifications = notifications.sort((a, b) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement notifications:', err);
        this.loading = false;
        alert('Erreur lors du chargement des notifications');
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.allNotifications];

    // Filtre par état de lecture
    if (this.filterRead === 'read') {
      filtered = filtered.filter(n => n.readable);
    } else if (this.filterRead === 'unread') {
      filtered = filtered.filter(n => !n.readable);
    }

    // Filtre par recherche
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(n => 
        n.message.toLowerCase().includes(term)
      );
    }

    this.filteredNotifications = filtered;
    this.currentPage = 1;
    this.updatePagination();
  }

  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredNotifications.length / this.itemsPerPage);
  }

  get paginatedNotifications(): Notification[] {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.filteredNotifications.slice(start, end);
  }

  markAsRead(notification: Notification): void {
    if (notification.readable) return;

    this.notificationService.markAsRead(notification.idNotification).subscribe({
      next: () => {
        notification.readable = true;
        this.applyFilters();
      },
      error: (err) => {
        console.error('Erreur marquage notification:', err);
        alert('Erreur lors du marquage de la notification');
      }
    });
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getRelativeTime(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'À l\'instant';
    if (minutes < 60) return `Il y a ${minutes} min`;
    if (hours < 24) return `Il y a ${hours}h`;
    if (days < 7) return `Il y a ${days}j`;
    return this.formatDate(dateStr);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxPages = 5;
    let start = Math.max(1, this.currentPage - Math.floor(maxPages / 2));
    let end = Math.min(this.totalPages, start + maxPages - 1);

    if (end - start < maxPages - 1) {
      start = Math.max(1, end - maxPages + 1);
    }

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  resetFilters(): void {
    this.filterRead = 'all';
    this.searchTerm = '';
    this.applyFilters();
  }
}

