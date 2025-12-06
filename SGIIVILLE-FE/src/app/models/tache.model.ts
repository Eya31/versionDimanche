export interface Tache {
  id: number;
  interventionId: number;
  libelle: string;
  description?: string;
  mainDOeuvreId?: number;
  technicienId?: number; // ← Ajouter cette ligne
  etat: 'A_FAIRE' | 'EN_COURS' | 'TERMINEE' | 'VERIFIEE' | 'SUSPENDUE' | 'REPORTEE';
  
  dateCreation?: string;
  dateDebut?: string;
  dateFin?: string;
  dateVerification?: string;
  tempsPasseMinutes?: number;
  commentaireMainDOeuvre?: string;
  commentaireTechnicien?: string;
  ordre?: number;
  verifiee: boolean;

  // Nouveaux champs
  intervention?: {
    id: number;
    description?: string;
    typeIntervention?: string;
    priorite?: string;
  };
  historiqueEtats?: HistoriqueEtatTache[];
}

export interface HistoriqueEtatTache {
  id: number;
  tacheId: number;
  etat: string;
  dateChangement: string;
  commentaire?: string;
  utilisateurId?: number;
  utilisateurNom?: string;
  tempsPasseMinutes?: number;
}

export interface CreateTacheRequest {
  libelle: string;
  description?: string;
  mainDOeuvreId?: number;
  ordre?: number;
}

export interface AssignerTacheRequest {
  mainDOeuvreId: number;
}

export interface TerminerTacheRequest {
  commentaire?: string;
  tempsPasseMinutes?: number;
}

export interface ChangerEtatTacheRequest {
  nouvelEtat: string;
  commentaire?: string;
  tempsPasseMinutes?: number;
}

export interface VerifierTacheRequest {
  commentaire?: string;
  validee: boolean;
}
