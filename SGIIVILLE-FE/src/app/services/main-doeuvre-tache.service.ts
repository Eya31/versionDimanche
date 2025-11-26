import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Tache, TerminerTacheRequest } from '../models/tache.model';

@Injectable({
  providedIn: 'root'
})
export class MainDOeuvreTacheService {
  private apiUrl = `${environment.apiUrl}/main-doeuvre`;

  constructor(private http: HttpClient) {}

  /**
   * Récupérer toutes les tâches assignées à l'agent connecté
   */
  getMyTaches(): Observable<Tache[]> {
    return this.http.get<Tache[]>(`${this.apiUrl}/taches`);
  }

  /**
   * Récupérer les tâches d'une intervention assignées à l'agent
   */
  getTachesByIntervention(interventionId: number): Observable<Tache[]> {
    return this.http.get<Tache[]>(`${this.apiUrl}/interventions/${interventionId}/taches`);
  }

  /**
   * Commencer une tâche
   */
  commencer(tacheId: number): Observable<Tache> {
    return this.http.post<Tache>(`${this.apiUrl}/taches/${tacheId}/commencer`, {});
  }

  /**
   * Terminer une tâche
   */
  terminer(tacheId: number, request: TerminerTacheRequest): Observable<Tache> {
    return this.http.post<Tache>(`${this.apiUrl}/taches/${tacheId}/terminer`, request);
  }
}

