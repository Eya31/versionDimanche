export interface CalendarEvent {
  id: number;
  title: string;
  start: Date;
  end: Date;
  color: EventColor;
  meta: EventMeta;
  resizable?: { beforeStart?: boolean; afterEnd?: boolean };
  draggable?: boolean;
}

export interface EventColor {
  primary: string;
  secondary: string;
  secondaryText?: string;
}

export interface EventMeta {
  type: 'INTERVENTION' | 'MAINTENANCE' | 'FORMATION' | 'CONGE';
  interventionId?: number;
  technicienIds: number[];
  priorite: 'HAUTE' | 'MOYENNE' | 'BASSE';
  etat: string;
  localisation?: { latitude: number; longitude: number };
  description?: string;
}

export interface DayView {
  date: Date;
  events: CalendarEvent[];
  isCurrentMonth?: boolean;
}

export interface WeekView {
  date: Date;
  days: DayView[];
}

export interface MonthView {
  weeks: WeekView[];
}
