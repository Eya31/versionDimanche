import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { 
  MainDOeuvre, 
  CreateMainDOeuvreRequest, 
  VerificationAffectationDTO,
  HistoriqueInterventionDTO 
} from '../models/main-doeuvre.model';

@Injectable({
  providedIn: 'root'
})
export class MainDOeuvreService {
  private apiUrl = `${environment.apiUrl}/technicien/main-doeuvre`;

  constructor(private http: HttpClient) {}

  getAll(filters?: { competence?: string; disponibilite?: string }): Observable<MainDOeuvre[]> {
    let params = new HttpParams();
    if (filters?.competence) params = params.set('competence', filters.competence);
    if (filters?.disponibilite) params = params.set('disponibilite', filters.disponibilite);
    return this.http.get<MainDOeuvre[]>(this.apiUrl, { params });
  }

  getById(id: number): Observable<MainDOeuvre> {
    return this.http.get<MainDOeuvre>(`${this.apiUrl}/${id}`);
  }

  create(mainDOeuvre: CreateMainDOeuvreRequest | MainDOeuvre): Observable<any> {
    // Le backend retourne maintenant un objet avec mainDOeuvre, userId, defaultPassword, message
    return this.http.post<any>(this.apiUrl, mainDOeuvre);
  }

  update(id: number, mainDOeuvre: MainDOeuvre): Observable<MainDOeuvre> {
    return this.http.put<MainDOeuvre>(`${this.apiUrl}/${id}`, mainDOeuvre);
  }

  archiver(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Vérifier si un agent peut être affecté à une intervention
   */
  verifierAffectation(interventionId: number, mainDOeuvreId: number): Observable<VerificationAffectationDTO> {
    return this.http.post<VerificationAffectationDTO>(
      `${environment.apiUrl}/technicien/interventions/${interventionId}/verifier-affectation`,
      { mainDOeuvreId }
    );
  }

  /**
   * Obtenir l'historique des interventions d'un agent
   */
  getHistorique(id: number): Observable<HistoriqueInterventionDTO[]> {
    return this.http.get<HistoriqueInterventionDTO[]>(`${environment.apiUrl}/technicien/main-doeuvre/${id}/historique`);
  }
}

