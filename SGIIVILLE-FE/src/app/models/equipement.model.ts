export interface Equipement {
  id: number;
  nom: string;
  type: string;
  etat: string;
  fournisseurId?: number;
  valeurAchat?: number;
  localisation?: Localisation;
  dateAchat?: string;
  disponible?: boolean;
  indisponibilites?: PeriodeIndisponibilite[];
}

export interface Localisation {
  latitude: number;
  longitude: number;
}

export interface PeriodeIndisponibilite {
  debut: string;
  fin: string;
}
