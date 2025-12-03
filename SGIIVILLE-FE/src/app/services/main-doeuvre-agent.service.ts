import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Intervention } from '../models/intervention.model';
import { MainDOeuvre } from '../models/main-doeuvre.model';

export interface StatistiquesMainDOeuvre {
  totalInterventions: number;
  interventionsTerminees: number;
  tauxReussite: number;
  tempsTotalMinutes: number;
}

@Injectable({
  providedIn: 'root'
})
export class MainDOeuvreAgentService {
  private apiUrl = `${environment.apiUrl}/main-doeuvre`;

  constructor(private http: HttpClient) {}

  /**
   * Récupère le profil de l'agent connecté
   */
  getProfil(): Observable<MainDOeuvre> {
    return this.http.get<MainDOeuvre>(`${this.apiUrl}/profil`);
  }

  /**
   * Récupère toutes les interventions auxquelles l'agent est affecté
   */
  getMyInterventions(filters?: { etat?: string }): Observable<Intervention[]> {
    let params = new HttpParams();
    if (filters?.etat) params = params.set('etat', filters.etat);
    return this.http.get<Intervention[]>(`${this.apiUrl}/interventions`, { params });
  }

  /**
   * Récupère les détails d'une intervention
   */
  getInterventionDetails(id: number): Observable<Intervention> {
    return this.http.get<Intervention>(`${this.apiUrl}/interventions/${id}`);
  }

  /**
   * Récupère les statistiques de l'agent
   */
  getStatistiques(): Observable<StatistiquesMainDOeuvre> {
    return this.http.get<StatistiquesMainDOeuvre>(`${this.apiUrl}/statistiques`);
  }
}
