import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface Notification {
  idNotification: number;
  message: string;
  createdAt: string;
  userId: number;
  readable: boolean;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private baseUrl = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  /**
   * Récupère les notifications d'un utilisateur
   */
  getNotificationsByUser(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/user/${userId}`);
  }

  /**
   * Compte les notifications non lues
   */
  getUnreadCount(userId: number): Observable<{ unreadCount: number }> {
    return this.http.get<{ unreadCount: number }>(`${this.baseUrl}/user/${userId}/unread-count`);
  }

  /**
   * Marque une notification comme lue
   */
  markAsRead(notificationId: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${notificationId}/mark-read`, {});
  }

  /**
   * Polling des notifications toutes les 30 secondes
   */
  pollNotifications(userId: number): Observable<Notification[]> {
    return interval(30000).pipe(
      switchMap(() => this.getNotificationsByUser(userId))
    );
  }

  /**
   * Polling du compteur de notifications non lues toutes les 15 secondes
   */
  pollUnreadCount(userId: number): Observable<{ unreadCount: number }> {
    return interval(15000).pipe(
      switchMap(() => this.getUnreadCount(userId))
    );
  }
}
