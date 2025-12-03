import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PublicStats {
  demandesTraitees: number;
  totalDemandes: number;
}

export interface DemandePublique {
  id: number;
  description: string;
  dateSoumission: string;
  category?: string;
  subCategory?: string;
  priority?: string;
  localisation?: {
    latitude: number;
    longitude: number;
    address?: string;
  };
  photos?: Array<{
    idPhoto: number;
    url: string;
    nom: string;
  }>;
  intervention?: {
    id: number;
    typeIntervention: string;
    dateDebut: string;
    dateFin: string;
    rapportFinal: string;
  };
}

export interface InscriptionMunicipalite {
  nom: string;
  adresse: string;
  email: string;
  numeroAdministratif: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class PublicService {
  private baseUrl = 'http://localhost:8080/api/public';

  constructor(private http: HttpClient) {}

  /**
   * Récupère les statistiques publiques
   */
  getStats(): Observable<PublicStats> {
    return this.http.get<PublicStats>(`${this.baseUrl}/stats`);
  }

  /**
   * Récupère toutes les demandes terminées (publiques)
   */
  getDemandesTerminees(filters?: {
    category?: string;
    dateDebut?: string;
    dateFin?: string;
  }): Observable<DemandePublique[]> {
    let params = new HttpParams();
    
    if (filters?.category) {
      params = params.set('category', filters.category);
    }
    if (filters?.dateDebut) {
      params = params.set('dateDebut', filters.dateDebut);
    }
    if (filters?.dateFin) {
      params = params.set('dateFin', filters.dateFin);
    }

    return this.http.get<DemandePublique[]>(`${this.baseUrl}/demandes-terminees`, { params });
  }

  /**
   * Récupère les détails d'une demande publique
   */
  getDemandePublique(id: number): Observable<DemandePublique> {
    return this.http.get<DemandePublique>(`${this.baseUrl}/demandes/${id}`);
  }

  /**
   * Soumet une demande d'inscription pour une municipalité
   */
  soumettreInscriptionMunicipalite(demande: InscriptionMunicipalite): Observable<any> {
    return this.http.post(`${this.baseUrl}/municipalite-inscription`, demande);
  }
}

