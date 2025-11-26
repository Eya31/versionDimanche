import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Tache, CreateTacheRequest, AssignerTacheRequest, TerminerTacheRequest, VerifierTacheRequest } from '../models/tache.model';

@Injectable({
  providedIn: 'root'
})
export class TacheService {
  private apiUrl = `${environment.apiUrl}/technicien`;

  constructor(private http: HttpClient) {}

  /**
   * Créer une nouvelle tâche pour une intervention
   */
  create(interventionId: number, request: CreateTacheRequest): Observable<Tache> {
    return this.http.post<Tache>(`${this.apiUrl}/interventions/${interventionId}/taches`, request);
  }

  /**
   * Récupérer toutes les tâches d'une intervention
   */
  getByIntervention(interventionId: number): Observable<Tache[]> {
    return this.http.get<Tache[]>(`${this.apiUrl}/interventions/${interventionId}/taches`);
  }

  /**
   * Assigner une tâche à une main-d'œuvre
   */
  assigner(tacheId: number, request: AssignerTacheRequest): Observable<Tache> {
    return this.http.put<Tache>(`${this.apiUrl}/taches/${tacheId}/assigner`, request);
  }

  /**
   * Vérifier une tâche (côté technicien)
   */
  verifier(tacheId: number, request: VerifierTacheRequest): Observable<Tache> {
    return this.http.post<Tache>(`${this.apiUrl}/taches/${tacheId}/verifier`, request);
  }

  /**
   * Terminer une intervention (uniquement si toutes les tâches sont vérifiées)
   */
  terminerIntervention(interventionId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/interventions/${interventionId}/terminer`, {});
  }
}

