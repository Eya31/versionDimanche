import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService, Notification } from '../../../../services/notification.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-notifications-unread',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './notifications-unread.component.html',
  styleUrls: ['./notifications-unread.component.css']
})
export class NotificationsUnreadComponent implements OnInit {
  notifications: Notification[] = [];
  loading = false;

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadUnreadNotifications();
  }

  loadUnreadNotifications(): void {
    this.loading = true;
    const userId = this.authService.getUserId();
    if (!userId) {
      this.loading = false;
      return;
    }

    this.notificationService.getNotificationsByUser(userId).subscribe({
      next: (notifications) => {
        this.notifications = notifications
          .filter(n => !n.readable)
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement notifications:', err);
        this.loading = false;
        alert('Erreur lors du chargement des notifications');
      }
    });
  }

  markAsRead(notification: Notification): void {
    if (notification.readable) return;

    this.notificationService.markAsRead(notification.idNotification).subscribe({
      next: () => {
        notification.readable = true;
        this.notifications = this.notifications.filter(n => !n.readable);
      },
      error: (err) => {
        console.error('Erreur marquage notification:', err);
        alert('Erreur lors du marquage de la notification');
      }
    });
  }

  markAllAsRead(): void {
    if (this.notifications.length === 0) return;

    const unreadIds = this.notifications
      .filter(n => !n.readable)
      .map(n => n.idNotification);

    let completed = 0;
    unreadIds.forEach(id => {
      this.notificationService.markAsRead(id).subscribe({
        next: () => {
          completed++;
          if (completed === unreadIds.length) {
            this.loadUnreadNotifications();
          }
        },
        error: (err) => console.error('Erreur marquage notification:', err)
      });
    });
  }

  formatDate(dateStr: string): string {
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
    return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  refresh(): void {
    this.loadUnreadNotifications();
  }
}

