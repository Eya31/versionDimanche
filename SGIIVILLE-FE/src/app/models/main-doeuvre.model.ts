export interface MainDOeuvre {
  id: number;
  nom: string;
  email: string;
  motDePasse?: string;
  role?: string;
  prenom: string;
  matricule: string;
  cin: string;
  telephone: string;
  disponibilite: 'LIBRE' | 'OCCUPE' | 'ARCHIVE';
  competence: string; // Compétence unique (obligatoire)
}

export interface HabilitationDTO {
  nom: string;
  dateObtention?: string; // ISO date
  dateExpiration?: string; // ISO date
  numeroCertificat?: string;
  valide?: boolean;
}

export interface DisponibiliteDTO {
  jour: string; // DayOfWeek
  heureDebut: string; // HH:mm
  heureFin: string; // HH:mm
  disponible: boolean;
}

export interface HistoriqueInterventionDTO {
  interventionId: number;
  description: string;
  dateDebut: string; // ISO datetime
  dateFin?: string; // ISO datetime
  tempsPasseMinutes: number;
  etat: string;
  competencesUtilisees: string[];
  resultat?: string;
}

export interface VerificationAffectationDTO {
  disponible: boolean;
  competencesOk: boolean;
  habilitationsOk: boolean;
  pasDeConflit: boolean;
  pasDejaAffecte: boolean;
  horairesOk: boolean;
  erreurs: string[];
  avertissements: string[];
}

/**
 * Fonction helper pour vérifier si une vérification d'affectation est valide
 */
export function isVerificationValide(verification: VerificationAffectationDTO): boolean {
  return verification.disponible && 
         verification.competencesOk && 
         verification.habilitationsOk && 
         verification.pasDeConflit && 
         verification.pasDejaAffecte && 
         verification.horairesOk;
}

export interface CreateMainDOeuvreRequest {
  nom: string;
  email: string;
  prenom: string;
  matricule: string;
  cin: string;
  telephone: string;
  competence: string; // Compétence unique (obligatoire)
}

