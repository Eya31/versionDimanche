import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval, of } from 'rxjs';
import { switchMap, startWith } from 'rxjs/operators';
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
   * Commence immédiatement avec un premier appel
   */
 pollNotifications(userId: number): Observable<Notification[]> {
    return interval(30000).pipe(
      startWith(0),
      switchMap(() => this.getNotificationsByUser(userId))
    );
  }

  /**
   * Polling du compteur de notifications non lues toutes les 15 secondes
   * Commence immédiatement avec un premier appel
   */
  pollUnreadCount(userId: number): Observable<{ unreadCount: number }> {
    return interval(15000).pipe(
      startWith(0),
      switchMap(() => this.getUnreadCount(userId))
    );
  }

 notifierTechnicienChangementTache(data: {
    technicienId: number;
    tacheId: number;
    libelleTache: string;
    mainDOeuvreNom: string;
    ancienEtat: string;
    nouvelEtat: string;
    details?: string;
  }): Observable<any> {
    return this.http.post(`${this.baseUrl}/notifier-technicien-tache`, data);
  }
// notification.service.ts
// Ajouter ces méthodes

// Ajouter ces méthodes
notifierChefInterventionTerminee(chefId: number, interventionId: number, message: string): Observable<any> {
  return this.http.post(`${this.baseUrl}/notifier-chef-intervention-terminee`, {
    chefId: chefId,
    interventionId: interventionId,
    message: message
  });
}

notifierTechnicienVerification(technicienId: number, interventionId: number, message: string): Observable<any> {
  return this.http.post(`${this.baseUrl}/notifier-technicien-verification`, {
    technicienId: technicienId,
    interventionId: interventionId,
    message: message
  });
}
 /**
   * Test de création de notification (pour le frontend)
   */
  testCreateNotification(userId: number, message: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/test`, {
      userId: userId,
      message: message
    });
  }

  createNotification(userId: number, message: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, {
      userId: userId,
      message: message
    });
  }
  /**
   * Méthode pour le chef : charger ses propres notifications
   */
  getChefNotifications(chefId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/user/${chefId}`);
  }

  /**
   * Marquer comme lue
   */
  markNotificationAsRead(notificationId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${notificationId}/mark-read`, {});
  }
}
