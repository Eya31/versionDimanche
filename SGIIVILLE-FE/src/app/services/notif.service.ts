import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval, of } from 'rxjs';
import { switchMap, catchError, startWith } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface Notification {
  idNotification: number;
  message: string;
  createdAt: string;
  userId: number;
  readable: boolean;
}

export interface NotificationStats {
  unreadCountV1: number;
  unreadCountV2: number;
  totalNotificationsV1: number;
  totalNotificationsV2: number;
  userId: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotifService {
  // CHANGER LE BASE URL POUR CORRESPONDRE AU NOUVEAU CONTROLLEUR
  private baseUrl = `${environment.apiUrl}/notif`;

  constructor(private http: HttpClient) {}

  // === MÉTHODES PRINCIPALES (Service NotifService - V1) ===

  /**
   * Récupère toutes les notifications d'un utilisateur (V1)
   */
  getNotificationsByUser(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/user/${userId}`);
  }

  /**
   * Compte les notifications non lues d'un utilisateur (V1)
   */
  getUnreadCountByUser(userId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/user/${userId}/unread-count`);
  }

  /**
   * Marque une notification comme lue (V1)
   */
  markAsRead(notificationId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${notificationId}/read`, {});
  }

  // === MÉTHODES ALTERNATIVES (Service NotificationService - V2) ===

  /**
   * Récupère les notifications d'un utilisateur (NotificationService)
   */
  getNotificationsByUserV2(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/ns/user/${userId}`);
  }

  /**
   * Compte les notifications non lues d'un utilisateur (NotificationService)
   */
  getUnreadCountByUserV2(userId: number): Observable<{unreadCount: number}> {
    return this.http.get<{unreadCount: number}>(`${this.baseUrl}/ns/user/${userId}/unread-count`);
  }

  /**
   * Marque une notification comme lue (NotificationService)
   */
  markAsReadV2(notificationId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/ns/${notificationId}/mark-read`, {});
  }

  // === MÉTHODES COMBINÉES ===

  /**
   * Récupère les statistiques complètes des notifications
   */
  getNotificationStats(userId: number): Observable<NotificationStats> {
    return this.http.get<NotificationStats>(`${this.baseUrl}/user/${userId}/stats`);
  }

  /**
   * Marque comme lu avec les deux services
   */
  markAsReadAllServices(notificationId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${notificationId}/read-all`, {});
  }

  // === SYSTÈME DE POLLING ===

  /**
   * Polling automatique du compteur de notifications non lues (V1)
   */
  pollUnreadCount(userId: number): Observable<number> {
    return interval(10000).pipe(
      startWith(0),
      switchMap(() =>
        this.getUnreadCountByUser(userId).pipe(
          catchError(error => {
            console.error('❌ Erreur polling notifications:', error);
            return of(0);
          })
        )
      )
    );
  }

  /**
   * Polling du compteur de notifications non lues (V2)
   */
  pollUnreadCountV2(userId: number): Observable<number> {
    return interval(10000).pipe(
      startWith(0),
      switchMap(() =>
        this.getUnreadCountByUserV2(userId).pipe(
          switchMap(response => of(response.unreadCount)),
          catchError(error => {
            console.error('❌ Erreur polling notifications V2:', error);
            return of(0);
          })
        )
      )
    );
  }

  /**
   * Polling des notifications complètes (liste V1)
   */
  pollNotifications(userId: number): Observable<Notification[]> {
    return interval(15000).pipe(
      startWith(0),
      switchMap(() =>
        this.getNotificationsByUser(userId).pipe(
          catchError(error => {
            console.error('❌ Erreur polling liste notifications:', error);
            return of([]);
          })
        )
      )
    );
  }

  /**
   * Polling des notifications complètes (liste V2)
   */
  pollNotificationsV2(userId: number): Observable<Notification[]> {
    return interval(15000).pipe(
      startWith(0),
      switchMap(() =>
        this.getNotificationsByUserV2(userId).pipe(
          catchError(error => {
            console.error('❌ Erreur polling liste notifications V2:', error);
            return of([]);
          })
        )
      )
    );
  }

  /**
   * Polling des statistiques complètes
   */
  pollNotificationStats(userId: number): Observable<NotificationStats> {
    return interval(20000).pipe(
      startWith(0),
      switchMap(() =>
        this.getNotificationStats(userId).pipe(
          catchError(error => {
            console.error('❌ Erreur polling stats notifications:', error);
            // Retourner des stats par défaut en cas d'erreur
            return of({
              unreadCountV1: 0,
              unreadCountV2: 0,
              totalNotificationsV1: 0,
              totalNotificationsV2: 0,
              userId: userId
            });
          })
        )
      )
    );
  }

  // === MÉTHODES UTILITAIRES ===

  /**
   * Test de création de notification
   */
  testCreateNotification(userId: number, message: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/test-create`, {
      userId: userId,
      message: message
    });
  }

  /**
   * Récupère toutes les notifications (debug)
   */
  getAllNotificationsDebug(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/debug/all`);
  }

  /**
   * Marquer toutes les notifications comme lues pour un utilisateur
   */
  markAllAsRead(userId: number): Observable<any> {
    return this.getNotificationsByUser(userId).pipe(
      switchMap(notifications => {
        const unreadNotifications = notifications.filter(n => !n.readable);
        const markRequests = unreadNotifications.map(notification =>
          this.markAsReadAllServices(notification.idNotification)
        );

        if (markRequests.length === 0) {
          return of({ message: 'Aucune notification non lue' });
        }

        // Exécuter toutes les requêtes en parallèle
        return new Observable(observer => {
          let completed = 0;
          const results: any[] = [];

          markRequests.forEach(request => {
            request.subscribe({
              next: (result) => {
                results.push(result);
                completed++;
                if (completed === markRequests.length) {
                  observer.next({
                    message: `${completed} notifications marquées comme lues`,
                    details: results
                  });
                  observer.complete();
                }
              },
              error: (error) => {
                completed++;
                results.push({ error: error.message });
                if (completed === markRequests.length) {
                  observer.next({
                    message: `Opération terminée avec des erreurs`,
                    details: results
                  });
                  observer.complete();
                }
              }
            });
          });
        });
      }),
      catchError(error => {
        console.error('❌ Erreur marquage multiple:', error);
        return of({ error: 'Erreur lors du marquage multiple' });
      })
    );
  }

  /**
   * Filtrer les notifications non lues
   */
  getUnreadNotifications(userId: number): Observable<Notification[]> {
    return this.getNotificationsByUser(userId).pipe(
      switchMap(notifications =>
        of(notifications.filter(n => !n.readable))
      ),
      catchError(error => {
        console.error('❌ Erreur filtrage notifications non lues:', error);
        return of([]);
      })
    );
  }

  /**
   * Filtrer les notifications non lues (V2)
   */
  getUnreadNotificationsV2(userId: number): Observable<Notification[]> {
    return this.getNotificationsByUserV2(userId).pipe(
      switchMap(notifications =>
        of(notifications.filter(n => !n.readable))
      ),
      catchError(error => {
        console.error('❌ Erreur filtrage notifications non lues V2:', error);
        return of([]);
      })
    );
  }

  /**
   * Formater la date pour l'affichage
   */
  formatNotificationDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'À l\'instant';
    if (minutes < 60) return `Il y a ${minutes} min`;
    if (hours < 24) return `Il y a ${hours}h`;
    if (days === 1) return 'Hier';
    if (days < 7) return `Il y a ${days}j`;

    return date.toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });
  }

  /**
   * Trier les notifications par date (plus récentes en premier)
   */
  sortNotificationsByDate(notifications: Notification[]): Notification[] {
    return notifications.sort((a, b) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
  }

  /**
   * Grouper les notifications par date
   */
  groupNotificationsByDate(notifications: Notification[]): { [key: string]: Notification[] } {
    const grouped: { [key: string]: Notification[] } = {};

    notifications.forEach(notification => {
      const date = new Date(notification.createdAt).toLocaleDateString('fr-FR');

      if (!grouped[date]) {
        grouped[date] = [];
      }

      grouped[date].push(notification);
    });

    return grouped;
  }
}
