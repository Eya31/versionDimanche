import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, forkJoin } from 'rxjs';
import { CalendarService } from '../../services/calendar.service';
import { DisponibiliteService } from '../../services/disponibilite.service';
import { CalendarEvent, MonthView } from '../../models/calendar-event.model';
import { TechnicienDisponibilite } from '../../models/disponibilite.model';

@Component({
  selector: 'app-calendar-view',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './calendar-view.component.html',
  styleUrls: ['./calendar-view.component.css']
})
export class CalendarViewComponent implements OnInit, OnDestroy {

  // Vue calendrier
  currentDate: Date = new Date();
  monthView: MonthView = { weeks: [] };
  selectedDate: Date | null = null;

  // Données
  events: CalendarEvent[] = [];
  techniciensDisponibilite: TechnicienDisponibilite[] = [];

  // Filtres
  viewMode: 'month' | 'week' | 'day' = 'month';
  filtreTechnicien: number | 'all' = 'all';
  filtreType: string = 'all';

  // États
  loading = false;
  showEventModal = false;
  selectedEvent: CalendarEvent | null = null;

  // Souscriptions
  private subscriptions: Subscription[] = [];

  constructor(
    private calendarService: CalendarService,
    private disponibiliteService: DisponibiliteService
  ) {}

  ngOnInit(): void {
    this.loadCalendarAndTechniciens();

    const dateSub = this.calendarService.currentDate$.subscribe(date => {
      this.currentDate = date;
      this.loadCalendarAndTechniciens();
    });

    this.subscriptions.push(dateSub);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  // Charger simultanément calendrier et techniciens
  loadCalendarAndTechniciens(): void {
    this.loading = true;

    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();

    const monthView$ = this.calendarService.getMonthView(year, month);
    const startDate = new Date(year, month, 1);
    const endDate = new Date(year, month + 1, 0);
    const techniciens$ = this.disponibiliteService.getIndisponibilites(startDate, endDate);

    forkJoin([monthView$, techniciens$]).subscribe({
      next: ([monthView, indisponibilites]) => {
        this.monthView = monthView || { weeks: [] };
        this.techniciensDisponibilite = indisponibilites || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement calendrier ou techniciens:', err);
        this.loading = false;
      }
    });
  }

  // Navigation
  previousMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    this.calendarService.setCurrentDate(this.currentDate);
  }

  nextMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    this.calendarService.setCurrentDate(this.currentDate);
  }

  today(): void {
    this.currentDate = new Date();
    this.calendarService.setCurrentDate(this.currentDate);
  }

  // Sélection de date
  selectDate(date: Date, events: CalendarEvent[]): void {
    this.selectedDate = date;
    this.showDayDetails(date, events);
  }

  showDayDetails(date: Date, events: CalendarEvent[]): void {
    console.log('Détails journée:', date, events);
  }

  selectEvent(event: CalendarEvent): void {
    this.selectedEvent = event;
    this.showEventModal = true;
  }

  closeEventModal(): void {
    this.showEventModal = false;
    this.selectedEvent = null;
  }

  // Appliquer les filtres
  applyFilters(): void {
    this.loadCalendarAndTechniciens();
  }

  generateReport(): void {
    const startDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), 1);
    const endDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 0);

    this.disponibiliteService.genererRapportDisponibilite(startDate, endDate).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `rapport-disponibilite-${this.currentDate.getFullYear()}-${this.currentDate.getMonth() + 1}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Erreur génération rapport:', error);
        alert('Erreur lors de la génération du rapport');
      }
    });
  }

  // Méthodes utilitaires pour le calendrier
  getMonthName(): string {
    return this.currentDate.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
  }

  getDayNames(): string[] {
    return ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  }

  isToday(date: Date): boolean {
    const today = new Date();
    return date.getDate() === today.getDate() &&
           date.getMonth() === today.getMonth() &&
           date.getFullYear() === today.getFullYear();
  }

  isCurrentMonth(date: Date): boolean {
    return date.getMonth() === this.currentDate.getMonth() &&
           date.getFullYear() === this.currentDate.getFullYear();
  }

  isSameDay(date1: Date, date2: Date): boolean {
    return date1.getFullYear() === date2.getFullYear() &&
           date1.getMonth() === date2.getMonth() &&
           date1.getDate() === date2.getDate();
  }

  getEventCount(events: CalendarEvent[]): number {
    return events?.length || 0;
  }

  getEventTypes(events: CalendarEvent[]): string[] {
    return [...new Set(events?.map(e => e.meta.type) || [])];
  }

  getEventTypeColor(type: string): string {
    switch (type) {
      case 'INTERVENTION': return '#2196F3';
      case 'MAINTENANCE': return '#FF9800';
      case 'FORMATION': return '#9C27B0';
      case 'CONGE': return '#4CAF50';
      default: return '#757575';
    }
  }
}
