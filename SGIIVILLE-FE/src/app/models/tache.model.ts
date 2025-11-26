export interface Tache {
  id: number;
  interventionId: number;
  libelle: string;
  description?: string;
  mainDOeuvreId?: number;
  etat: 'A_FAIRE' | 'EN_COURS' | 'TERMINEE' | 'VERIFIEE';
  dateCreation?: string;
  dateDebut?: string;
  dateFin?: string;
  dateVerification?: string;
  tempsPasseMinutes?: number;
  commentaireMainDOeuvre?: string;
  commentaireTechnicien?: string;
  ordre?: number;
  verifiee: boolean;
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

export interface VerifierTacheRequest {
  commentaire?: string;
  validee: boolean;
}

