import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Intervention } from '../models/intervention.model';
import { DateValidationRequest, DateValidationResult } from '../models/intervention-validation.model';

@Injectable({ providedIn: 'root' })
export class InterventionService {
  private baseUrl = 'http://localhost:8080/api/interventions';

  constructor(private http: HttpClient) {}

  getAllInterventions(): Observable<Intervention[]> {
    return this.http.get<Intervention[]>(this.baseUrl);
  }

  // Dans intervention.service.ts


  updateStatut(interventionId: number, statut: string): Observable<Intervention> {
    return this.http.patch<Intervention>(`${this.baseUrl}/${interventionId}`, { statut: statut });
  }

  affecterTechnicien(interventionId: number, technicienId: number): Observable<Intervention> {
    return this.http.put<Intervention>(`${this.baseUrl}/${interventionId}/affecter`, { technicienId });
  }

  validateDates(request: DateValidationRequest): Observable<DateValidationResult[]> {
    return this.http.post<DateValidationResult[]>(`${this.baseUrl}/valider-dates`, request);
  }

  assignerRessources(request: {
    dateIntervention: string,
    techniciensIds: number[],
    equipementsIds: number[],
    materiels: { materielId: number, quantite: number }[]
  }): Observable<any> {
    return this.http.post(`${this.baseUrl}/assigner-ressources`, request);
  }

  planifierInterventionComplete(request: {
    demandeId: number,
    dateIntervention: string,
    techniciensIds: number[],
    equipementsIds: number[],
    materiels: { materielId: number, quantite: number }[]
  }): Observable<any> {
    return this.http.post(`${this.baseUrl}/planifier-complete`, request);
  }

  // Ajouter ces méthodes dans votre intervention.service.ts






// Ajouter ces méthodes
terminerIntervention(interventionId: number): Observable<Intervention> {
  return this.http.patch<Intervention>(`${this.baseUrl}/${interventionId}/terminer`, {});
}

verifierIntervention(interventionId: number): Observable<Intervention> {
  return this.http.patch<Intervention>(`${this.baseUrl}/${interventionId}/verifier`, {});
}

verifierToutesTachesTerminees(interventionId: number): Observable<{ toutesTerminees: boolean }> {
  return this.http.get<{ toutesTerminees: boolean }>(`${this.baseUrl}/${interventionId}/taches/statut`);
}

getInterventionById(id: number): Observable<Intervention> {
  return this.http.get<Intervention>(`${this.baseUrl}/${id}`);
}
}
