export interface TechnicienProfil {
  id: number;
  nom: string;
  prenom?: string;
  email: string;
  telephone?: string;
  matricule?: string;
  competences: string[];
  habilitations: string[];
  disponibilite: boolean;
}

export interface UpdateProfilTechnicienRequest {
  nom?: string;
  prenom?: string;
  telephone?: string;
  email?: string;
  competences?: string[];
  habilitations?: string[];
  ancienMotDePasse?: string;
  nouveauMotDePasse?: string;
}

export interface StatistiquesTechnicien {
  totalInterventions: number;
  interventionsTerminees: number;
  tauxReussite: number;
  tempsTotalMinutes: number;
}

