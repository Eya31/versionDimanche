export interface UpdateEtatInterventionRequest {
  nouvelEtat: 'EN_ATTENTE' | 'EN_COURS' | 'SUSPENDUE' | 'TERMINEE';
  tempsPasseMinutes?: number;
  notes?: string;
  commentaire?: string;
}

