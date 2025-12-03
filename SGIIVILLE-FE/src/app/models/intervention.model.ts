export interface Intervention {
  id: number;
  description?: string;
  typeIntervention?: string;
  priorite: 'URGENTE' | 'PLANIFIEE' | 'NORMALE' | 'CRITIQUE';
  etat: 'EN_ATTENTE' | 'EN_COURS' | 'SUSPENDUE' | 'TERMINEE';
  datePlanifiee: string;
  dateDebut?: string;
  dateFin?: string;
  budget: number;
  technicienId?: number;
  demandeId?: number;
  chefServiceId?: number;
  localisation?: {
    latitude: number;
    longitude: number;
    address?: string;
  };
  mainDOeuvreIds?: number[];
  equipementIds?: number[];
  ressourceIds?: number[];
  photoIds?: number[];
  commentaire?: string;
  rapportFinal?: string;
  tempsPasseMinutes?: number;
  signatureElectronique?: string;
}
