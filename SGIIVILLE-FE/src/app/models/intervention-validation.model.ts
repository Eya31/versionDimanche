export interface CompetenceRequise {
  competence: string;
  nombreTechniciens: number;
}

export interface MaterielRequis {
  designation: string;
  quantiteRequise: number;
}

export interface EquipementRequis {
  type: string;
  quantiteRequise: number;
}

export interface DateValidationRequest {
  dateDebut: string;
  dateFin: string;
  competencesRequises: CompetenceRequise[];
  materielsRequis: MaterielRequis[];
  equipementsRequis: EquipementRequis[];
}

export interface DateValidationResult {
  date: string;
  status: 'VERT' | 'JAUNE' | 'ROUGE';
  message: string;
  techniciensDisponibles: boolean;
  equipementsDisponibles: boolean;
  materielsDisponibles: boolean;
}
