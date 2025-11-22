export interface PlanificationRequest {
  demandeId: number;
  datePlanifiee: string;
  techniciensIds: number[];
  equipementsIds: number[];
  ressourcesIds: number[];
  priorite: 'HAUTE' | 'MOYENNE' | 'BASSE';
  budget: number;
  notes?: string;
}

export interface InterventionPlanifiee {
  id: number;
  demandeId: number;
  datePlanifiee: string;
  priorite: string;
  etat: 'PLANIFIEE' | 'EN_COURS' | 'TERMINEE' | 'ANNULEE';
  techniciens: number[];
  equipements: number[];
  ressources: number[];
  budget: number;
  createdAt: string;
}
