export interface TechnicienDisponibilite {
  technicienId: number;
  nom: string;
  email: string;
  competences: string[];
  disponibilites: CreneauDisponibilite[];
  disponible: boolean;
  tauxDisponibilite: number;
}

export interface CreneauDisponibilite {
  date: string;
  creneaux: Creneau[];
}

export interface Creneau {
  debut: string;
  fin: string;
  disponible: boolean;
  interventionId?: number;
  typeIndisponibilite?: 'INTERVENTION' | 'CONGE' | 'FORMATION';
}

export interface RessourceDisponibilite {
  date: string;
  equipementsDisponibles: EquipementDisponibilite[];
  ressourcesDisponibles: RessourceMaterielleDisponibilite[];
  tousDisponibles: boolean;
  message?: string;
}

export interface EquipementDisponibilite {
  equipementId: number;
  type: string;
  etat: string;
  disponible: boolean;
  raisonIndisponibilite?: string;
  dateRetour?: string;
}

export interface RessourceMaterielleDisponibilite {
  ressourceId: number;
  designation: string;
  quantiteDisponible: number;
  quantiteDemandee: number;
  disponible: boolean;
  seuilAlerte: boolean;
}
