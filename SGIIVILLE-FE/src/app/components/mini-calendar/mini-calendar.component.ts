import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Intervention } from '../../models/intervention.model';

export interface CalendarDay {
  date: Date;
  interventions: Intervention[];
  isToday: boolean;
  isCurrentMonth: boolean;
  isSelected: boolean;
}

@Component({
  selector: 'app-mini-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mini-calendar.component.html',
  styleUrls: ['./mini-calendar.component.css']
})
export class MiniCalendarComponent implements OnInit, OnChanges {
  @Input() interventions: Intervention[] = [];
  @Input() viewMode: 'month' | 'week' = 'month';
  @Input() selectedDate: Date | null = null;
  @Output() dateSelected = new EventEmitter<Date>();
  @Output() interventionClicked = new EventEmitter<Intervention>();

  currentDate: Date = new Date();
  calendarDays: CalendarDay[] = [];
  weekDays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  monthNames = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 
                'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];

  ngOnInit(): void {
    this.generateCalendar();
  }

  ngOnChanges(): void {
    this.generateCalendar();
  }

  generateCalendar(): void {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    const today = new Date();
    
    // Premier jour du mois
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    
    // Premier lundi de la grille (peut être du mois précédent)
    const startDate = new Date(firstDay);
    const dayOfWeek = firstDay.getDay();
    const daysToSubtract = dayOfWeek === 0 ? 6 : dayOfWeek - 1; // Lundi = 0
    startDate.setDate(firstDay.getDate() - daysToSubtract);
    
    // Dernier dimanche de la grille (peut être du mois suivant)
    const endDate = new Date(lastDay);
    const lastDayOfWeek = lastDay.getDay();
    const daysToAdd = lastDayOfWeek === 0 ? 0 : 7 - lastDayOfWeek;
    endDate.setDate(lastDay.getDate() + daysToAdd);
    
    this.calendarDays = [];
    const current = new Date(startDate);
    
    while (current <= endDate) {
      const dayDate = new Date(current);
      const dayInterventions = this.getInterventionsForDate(dayDate);
      
      this.calendarDays.push({
        date: dayDate,
        interventions: dayInterventions,
        isToday: this.isSameDay(dayDate, today),
        isCurrentMonth: dayDate.getMonth() === month,
        isSelected: this.selectedDate ? this.isSameDay(dayDate, this.selectedDate) : false
      });
      
      current.setDate(current.getDate() + 1);
    }
  }

  getInterventionsForDate(date: Date): Intervention[] {
    if (!this.interventions || this.interventions.length === 0) return [];
    
    return this.interventions.filter(intervention => {
      if (!intervention.datePlanifiee) return false;
      const interventionDate = new Date(intervention.datePlanifiee);
      return this.isSameDay(interventionDate, date);
    });
  }

  isSameDay(date1: Date, date2: Date): boolean {
    return date1.getFullYear() === date2.getFullYear() &&
           date1.getMonth() === date2.getMonth() &&
           date1.getDate() === date2.getDate();
  }

  selectDate(day: CalendarDay): void {
    if (!day.isCurrentMonth) {
      // Naviguer vers le mois précédent/suivant
      if (day.date < this.currentDate) {
        this.previousMonth();
      } else {
        this.nextMonth();
      }
    }
    this.dateSelected.emit(day.date);
  }

  selectIntervention(intervention: Intervention, event: Event): void {
    event.stopPropagation();
    this.interventionClicked.emit(intervention);
  }

  previousMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    this.generateCalendar();
  }

  nextMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    this.generateCalendar();
  }

  goToToday(): void {
    this.currentDate = new Date();
    this.generateCalendar();
    this.dateSelected.emit(new Date());
  }

  getMonthYear(): string {
    return `${this.monthNames[this.currentDate.getMonth()]} ${this.currentDate.getFullYear()}`;
  }

  getPriorityColor(priorite: string): string {
    switch(priorite?.toUpperCase()) {
      case 'URGENTE':
      case 'CRITIQUE':
        return '#F44336'; // Rouge
      case 'HAUTE':
        return '#FF9800'; // Orange
      case 'MOYENNE':
      case 'PLANIFIEE':
        return '#2196F3'; // Bleu
      case 'BASSE':
        return '#4CAF50'; // Vert
      default:
        return '#9E9E9E'; // Gris
    }
  }

  getInterventionCount(day: CalendarDay): number {
    return day.interventions.length;
  }
}

