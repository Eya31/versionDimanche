export interface MainDOeuvre {
  id: number;
  nom: string;
  prenom?: string;
  matricule?: string;
  cin: string;
  telephone: string;
  email?: string;
  metier?: string;
  competences: string[];
  habilitations: string[];
  habilitationsExpiration?: { [key: string]: string }; // Map habilitation -> date expiration (ISO string)
  disponibilite: 'DISPONIBLE' | 'OCCUPE' | 'CONFLIT' | 'EN_CONGE' | 'ABSENT' | 'HORS_HABILITATION' | 'ARCHIVE' | 'DESACTIVE';
  active: boolean;
  photoPath?: string;
  horairesTravail?: { [key: string]: string }; // Map jour -> "08:00-17:00"
  conges?: string[]; // Dates ISO
  absences?: string[]; // Dates ISO
  historiqueInterventionIds?: number[];
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
  prenom?: string;
  matricule?: string;
  cin: string;
  telephone: string;
  email?: string;
  metier?: string;
  competences: string[];
  habilitations: HabilitationDTO[];
  horairesTravail?: { [key: string]: string };
  photoPath?: string;
}

