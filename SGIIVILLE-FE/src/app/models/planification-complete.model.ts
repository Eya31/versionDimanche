export interface PlanificationCompleteRequest {
  demandeId: number;
  technicienId: number;
  datePlanifiee: string; // Format: YYYY-MM-DD
  heureDebut?: string; // Format: HH:mm
  heureFin?: string; // Format: HH:mm
  dureeMinutes?: number;
  priorite: 'URGENTE' | 'PLANIFIEE' | 'NORMALE' | 'CRITIQUE';
  budget: number;
  description?: string;
  typeIntervention?: string;
  equipementIds?: number[];
  ressourceIds?: number[];
  mainDOeuvreIds?: number[];
  remarques?: string;
}

