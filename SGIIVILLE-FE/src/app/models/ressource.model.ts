export interface RessourceMaterielle {
  id: number;
  designation: string;
  quantiteEnStock: number;
  valeurAchat: number;
  fournisseurId?: number;
  unite?: string;
}
