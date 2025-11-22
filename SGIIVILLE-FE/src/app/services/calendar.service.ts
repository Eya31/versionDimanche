import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError, of, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
import { CalendarEvent, EventMeta, MonthView, WeekView, DayView } from '../models/calendar-event.model';

@Injectable({
  providedIn: 'root'
})
export class CalendarService {
  private apiUrl = `${environment.apiUrl}/api/planification`;
  private currentDateSubject = new BehaviorSubject<Date>(new Date());
  public currentDate$ = this.currentDateSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Récupérer les événements du calendrier pour un mois
  getEventsForMonth(year: number, month: number): Observable<CalendarEvent[]> {
    return this.http.get<CalendarEvent[]>(`${this.apiUrl}/calendrier/${year}/${month}`)
      .pipe(
        map(events => events.map(event => this.mapToCalendarEvent(event))),
        catchError(error => {
          console.error('Erreur récupération événements:', error);
          return of([]);
        })
      );
  }

  // Récupérer les événements pour un technicien spécifique
  getEventsForTechnicien(technicienId: number, startDate: Date, endDate: Date): Observable<CalendarEvent[]> {
    const params = new HttpParams()
      .set('startDate', startDate.toISOString())
      .set('endDate', endDate.toISOString());

    return this.http.get<CalendarEvent[]>(`${this.apiUrl}/calendrier/technicien/${technicienId}`, { params })
      .pipe(
        map(events => events.map(event => this.mapToCalendarEvent(event))),
        catchError(error => {
          console.error('Erreur récupération événements technicien:', error);
          return of([]);
        })
      );
  }

  // Récupérer la vue mois
  getMonthView(year: number, month: number): Observable<MonthView> {
    return this.getEventsForMonth(year, month).pipe(
      map(events => this.generateMonthView(year, month, events))
    );
  }

  // Ajouter un événement au calendrier
  addEvent(event: CalendarEvent): Observable<CalendarEvent> {
    return this.http.post<CalendarEvent>(`${this.apiUrl}/calendrier/events`, event)
      .pipe(
        map(newEvent => this.mapToCalendarEvent(newEvent)),
        catchError(error => {
          console.error('Erreur ajout événement:', error);
          throw error;
        })
      );
  }

  // Mettre à jour un événement
  updateEvent(eventId: number, event: Partial<CalendarEvent>): Observable<CalendarEvent> {
    return this.http.put<CalendarEvent>(`${this.apiUrl}/calendrier/events/${eventId}`, event)
      .pipe(
        map(updatedEvent => this.mapToCalendarEvent(updatedEvent)),
        catchError(error => {
          console.error('Erreur mise à jour événement:', error);
          throw error;
        })
      );
  }

  // Supprimer un événement
  deleteEvent(eventId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/calendrier/events/${eventId}`)
      .pipe(
        catchError(error => {
          console.error('Erreur suppression événement:', error);
          throw error;
        })
      );
  }

  // Changer le mois courant
  setCurrentDate(date: Date): void {
    this.currentDateSubject.next(date);
  }

  // Générer la vue mois
  private generateMonthView(year: number, month: number, events: CalendarEvent[]): MonthView {
    const weeks: WeekView[] = [];
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);

    let currentWeek: DayView[] = [];
    const currentDate = new Date(firstDay);

    // Remplir les jours du mois précédent si nécessaire
    const startDay = firstDay.getDay();
    const startOffset = startDay === 0 ? 6 : startDay - 1;
    for (let i = 0; i < startOffset; i++) {
      const prevDate = new Date(firstDay);
      prevDate.setDate(prevDate.getDate() - (startOffset - i));
      currentWeek.push({ date: new Date(prevDate), events: [], isCurrentMonth: false });
    }

    // Remplir les jours du mois
    while (currentDate <= lastDay) {
      const dateEvents = events.filter(event =>
        this.isSameDay(event.start, currentDate)
      );

      currentWeek.push({
        date: new Date(currentDate),
        events: dateEvents,
        isCurrentMonth: true
      });

      if (currentWeek.length === 7) {
        weeks.push({ date: new Date(currentWeek[0].date), days: [...currentWeek] });
        currentWeek = [];
      }

      currentDate.setDate(currentDate.getDate() + 1);
    }

    // Remplir les jours du mois suivant si nécessaire
    if (currentWeek.length > 0) {
      while (currentWeek.length < 7) {
        const nextDate = new Date(currentWeek[currentWeek.length - 1].date);
        nextDate.setDate(nextDate.getDate() + 1);
        currentWeek.push({ date: nextDate, events: [], isCurrentMonth: false });
      }
      weeks.push({ date: new Date(currentWeek[0].date), days: [...currentWeek] });
    }

    return { weeks };
  }

  // Mapper les données API vers CalendarEvent
  private mapToCalendarEvent(data: any): CalendarEvent {
    return {
      id: data.id,
      title: data.title,
      start: new Date(data.start),
      end: new Date(data.end),
      color: data.color || this.getDefaultColor(data.meta?.type),
      meta: data.meta || this.getDefaultMeta(),
      resizable: data.resizable !== undefined ? data.resizable : true,
      draggable: data.draggable !== undefined ? data.draggable : true
    };
  }

  private getDefaultMeta(): EventMeta {
    return {
      type: 'INTERVENTION',
      technicienIds: [],
      priorite: 'MOYENNE',
      etat: 'PLANIFIEE'
    };
  }

  // Couleurs par défaut selon le type
  private getDefaultColor(type: string): { primary: string; secondary: string } {
    switch (type) {
      case 'INTERVENTION':
        return { primary: '#2196F3', secondary: '#E3F2FD' };
      case 'MAINTENANCE':
        return { primary: '#FF9800', secondary: '#FFF3E0' };
      case 'FORMATION':
        return { primary: '#9C27B0', secondary: '#F3E5F5' };
      case 'CONGE':
        return { primary: '#4CAF50', secondary: '#E8F5E8' };
      default:
        return { primary: '#757575', secondary: '#F5F5F5' };
    }
  }

  // Vérifier si deux dates sont le même jour
  private isSameDay(date1: Date, date2: Date): boolean {
    return date1.getFullYear() === date2.getFullYear() &&
           date1.getMonth() === date2.getMonth() &&
           date1.getDate() === date2.getDate();
  }

  // Récupérer les statistiques du mois
  getMonthStats(year: number, month: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/calendrier/stats/${year}/${month}`)
      .pipe(
        catchError(error => {
          console.error('Erreur statistiques mois:', error);
          return of({ interventions: 0, maintenances: 0, formations: 0 });
        })
      );
  }
}
