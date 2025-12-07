export interface GeoPoint {
  value?: string;
  latitude: number;
  longitude: number;
  address?: string;
}

export interface Photo {
  idPhoto: number;
  url: string;
  nom: string;
}

export interface Demande {
  id: number;
  description: string;
  dateSoumission: string;
  etat: 'SOUMISE' | 'EN_ATTENTE' | 'TRAITEE' | 'REJETEE';
  citoyenId?: number | string | null;

  // Informations citoyen (peuvent être anonymes)
  citoyenNom?: string;
  citoyenEmail?: string;
  citoyenTelephone?: string;
  citoyenAdresse?: string;

  // Flag anonymat
  isAnonymous?: boolean;

  // Champs existants
  photos?: Photo[];
  localisation: GeoPoint;
  category?: string;
  subCategory?: string;
  priority?: string;
  contactEmail?: string;
  address?: string;

  // Compatibilité pour anciens uploads
  attachments?: any[];
}
