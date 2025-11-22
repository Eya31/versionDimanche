import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DemandeAjout {
  id: number;
  typeDemande: 'EQUIPEMENT' | 'RESSOURCE';
  designation: string;
  quantite: number;
  budget: number;
  justification: string;
  etat: 'EN_ATTENTE_ADMIN' | 'ACCEPTEE' | 'REFUSEE';
  dateDemande: string;
  chefId: number;
  adminId?: number;
  dateTraitement?: string;
  motifRefus?: string;
}

export interface CreateDemandeRequest {
  typeDemande: 'EQUIPEMENT' | 'RESSOURCE';
  designation: string;
  quantite: number;
  budget: number;
  justification: string;
  chefId: number;
}

export interface TraitementDemandeRequest {
  adminId: number;
}

export interface RefusDemandeRequest {
  adminId: number;
  motifRefus: string;
}

@Injectable({
  providedIn: 'root'
})
export class DemandeAjoutService {
  private apiUrl = 'http://localhost:8080/api/demandes-ajout';

  constructor(private http: HttpClient) {}

  // === CHEF DE SERVICE ===
  creerDemande(demande: CreateDemandeRequest): Observable<DemandeAjout> {
    return this.http.post<DemandeAjout>(this.apiUrl, demande);
  }

  getDemandesParChef(chefId: number): Observable<DemandeAjout[]> {
    return this.http.get<DemandeAjout[]>(`${this.apiUrl}/chef/${chefId}`);
  }

  // === ADMINISTRATEUR ===
  getDemandesEnAttente(): Observable<DemandeAjout[]> {
    return this.http.get<DemandeAjout[]>(`${this.apiUrl}/admin/en-attente`);
  }

  getAllDemandes(): Observable<DemandeAjout[]> {
    return this.http.get<DemandeAjout[]>(`${this.apiUrl}/admin`);
  }

  accepterDemande(demandeId: number, request: TraitementDemandeRequest): Observable<DemandeAjout> {
    return this.http.post<DemandeAjout>(`${this.apiUrl}/admin/${demandeId}/accepter`, request);
  }

  refuserDemande(demandeId: number, request: RefusDemandeRequest): Observable<DemandeAjout> {
    return this.http.post<DemandeAjout>(`${this.apiUrl}/admin/${demandeId}/refuser`, request);
  }
}
