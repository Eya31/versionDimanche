export interface RapportFinalRequest {
  resultatObtenu: string;
  tempsTotalMinutes: number;
  ressourcesUtilisees: RessourceUtilisee[];
  equipementsUtilises: EquipementUtilise[];
  problemesRencontres: string;
  photoIds: number[];
  signatureElectronique?: string;
  commentairePersonnalise?: string;
  analyseEtRecommandations?: string;
}

export interface RessourceUtilisee {
  ressourceId: number;
  type: string;
  quantite: number;
  reference?: string;
  numeroLot?: string;
}

export interface EquipementUtilise {
  equipementId: number;
  type: string;
  reference?: string;
  dureeUtilisationMinutes: number;
}

