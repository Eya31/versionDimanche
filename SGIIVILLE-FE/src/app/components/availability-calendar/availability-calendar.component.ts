import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainDOeuvre } from '../../models/main-doeuvre.model';
import { Intervention } from '../../models/intervention.model';

export interface AvailabilitySlot {
  date: Date;
  time: string;
  isAvailable: boolean;
  hasConflict: boolean;
  conflictingInterventions: Intervention[];
}

@Component({
  selector: 'app-availability-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './availability-calendar.component.html',
  styleUrls: ['./availability-calendar.component.css']
})
export class AvailabilityCalendarComponent implements OnInit {
  @Input() mainDOeuvre: MainDOeuvre | null = null;
  @Input() currentIntervention: Intervention | null = null;
  @Input() allInterventions: Intervention[] = [];
  @Output() slotSelected = new EventEmitter<{ date: Date; time: string }>();

  currentDate: Date = new Date();
  selectedDate: Date | null = null;
  availabilitySlots: AvailabilitySlot[] = [];
  weekDays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  timeSlots = ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00'];

  ngOnInit(): void {
    if (this.currentIntervention?.datePlanifiee) {
      this.selectedDate = new Date(this.currentIntervention.datePlanifiee);
      this.currentDate = new Date(this.selectedDate);
    }
    this.generateAvailabilitySlots();
  }

  ngOnChanges(): void {
    this.generateAvailabilitySlots();
  }

  generateAvailabilitySlots(): void {
    if (!this.selectedDate) {
      this.selectedDate = new Date();
    }

    this.availabilitySlots = this.timeSlots.map(time => {
      const slotDate = new Date(this.selectedDate!);
      const [hours, minutes] = time.split(':').map(Number);
      slotDate.setHours(hours, minutes, 0, 0);

      const conflicts = this.checkConflicts(slotDate);
      const isAvailable = conflicts.length === 0 && this.mainDOeuvre?.disponibilite === 'DISPONIBLE';

      return {
        date: slotDate,
        time,
        isAvailable,
        hasConflict: conflicts.length > 0,
        conflictingInterventions: conflicts
      };
    });
  }

  checkConflicts(slotDate: Date): Intervention[] {
    if (!this.mainDOeuvre || !this.allInterventions) return [];

    return this.allInterventions.filter(intervention => {
      // Exclure l'intervention actuelle
      if (this.currentIntervention && intervention.id === this.currentIntervention.id) {
        return false;
      }

      // Vérifier si la main-d'œuvre est affectée à cette intervention
      if (!intervention.mainDOeuvreIds?.includes(this.mainDOeuvre!.id)) {
        return false;
      }

      // Vérifier si les dates se chevauchent
      if (!intervention.datePlanifiee) return false;

      const interventionDate = new Date(intervention.datePlanifiee);
      const interventionStart = new Date(interventionDate);
      interventionStart.setHours(8, 0, 0, 0); // Début par défaut

      const interventionEnd = new Date(interventionDate);
      interventionEnd.setHours(17, 0, 0, 0); // Fin par défaut

      // Si dateDebut et dateFin existent, les utiliser
      if (intervention.dateDebut) {
        interventionStart.setTime(new Date(intervention.dateDebut).getTime());
      }
      if (intervention.dateFin) {
        interventionEnd.setTime(new Date(intervention.dateFin).getTime());
      }

      return slotDate >= interventionStart && slotDate <= interventionEnd;
    });
  }

  selectDate(date: Date): void {
    this.selectedDate = date;
    this.generateAvailabilitySlots();
  }

  selectSlot(slot: AvailabilitySlot): void {
    if (slot.isAvailable) {
      this.slotSelected.emit({ date: slot.date, time: slot.time });
    }
  }

  previousWeek(): void {
    const newDate = new Date(this.currentDate);
    newDate.setDate(newDate.getDate() - 7);
    this.currentDate = newDate;
    if (!this.selectedDate) {
      this.selectedDate = new Date(this.currentDate);
      this.generateAvailabilitySlots();
    }
  }

  nextWeek(): void {
    const newDate = new Date(this.currentDate);
    newDate.setDate(newDate.getDate() + 7);
    this.currentDate = newDate;
    if (!this.selectedDate) {
      this.selectedDate = new Date(this.currentDate);
      this.generateAvailabilitySlots();
    }
  }

  goToToday(): void {
    this.currentDate = new Date();
    this.selectedDate = new Date();
    this.generateAvailabilitySlots();
  }

  getWeekDates(): Date[] {
    const dates: Date[] = [];
    const startOfWeek = new Date(this.currentDate);
    const day = startOfWeek.getDay();
    const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1); // Lundi
    startOfWeek.setDate(diff);

    for (let i = 0; i < 7; i++) {
      const date = new Date(startOfWeek);
      date.setDate(startOfWeek.getDate() + i);
      dates.push(date);
    }
    return dates;
  }

  isToday(date: Date): boolean {
    const today = new Date();
    return date.getDate() === today.getDate() &&
           date.getMonth() === today.getMonth() &&
           date.getFullYear() === today.getFullYear();
  }

  isSelected(date: Date): boolean {
    if (!this.selectedDate) return false;
    return date.getDate() === this.selectedDate.getDate() &&
           date.getMonth() === this.selectedDate.getMonth() &&
           date.getFullYear() === this.selectedDate.getFullYear();
  }

  checkSlotAvailability(date: Date, time: string): boolean {
    if (!this.mainDOeuvre || this.mainDOeuvre.disponibilite !== 'DISPONIBLE') return false;
    
    const slotDate = new Date(date);
    const [hours, minutes] = time.split(':').map(Number);
    slotDate.setHours(hours, minutes, 0, 0);
    
    const conflicts = this.checkConflicts(slotDate);
    return conflicts.length === 0;
  }

  checkSlotConflict(date: Date, time: string): boolean {
    if (!this.mainDOeuvre) return false;
    
    const slotDate = new Date(date);
    const [hours, minutes] = time.split(':').map(Number);
    slotDate.setHours(hours, minutes, 0, 0);
    
    const conflicts = this.checkConflicts(slotDate);
    return conflicts.length > 0;
  }

  getSlotTooltip(date: Date, time: string): string {
    const slotDate = new Date(date);
    const [hours, minutes] = time.split(':').map(Number);
    slotDate.setHours(hours, minutes, 0, 0);
    
    const conflicts = this.checkConflicts(slotDate);
    if (conflicts.length > 0) {
      return `Conflit avec ${conflicts.length} intervention(s)`;
    }
    if (this.mainDOeuvre?.disponibilite === 'DISPONIBLE') {
      return 'Disponible';
    }
    return 'Indisponible';
  }

  selectSlotForDate(date: Date, time: string): void {
    if (this.checkSlotAvailability(date, time)) {
      const slotDate = new Date(date);
      const [hours, minutes] = time.split(':').map(Number);
      slotDate.setHours(hours, minutes, 0, 0);
      this.slotSelected.emit({ date: slotDate, time });
    }
  }
}

