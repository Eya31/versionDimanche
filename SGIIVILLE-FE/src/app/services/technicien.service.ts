import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Intervention } from '../models/intervention.model';
import { UpdateEtatInterventionRequest } from '../models/update-etat.model';
import { RapportFinalRequest } from '../models/rapport-final.model';
import { TechnicienProfil, UpdateProfilTechnicienRequest, StatistiquesTechnicien } from '../models/technicien-profil.model';
import { AffecterMainDOeuvreRequest } from '../models/affecter-main-doeuvre.model';

@Injectable({
  providedIn: 'root'
})
export class TechnicienService {
  private apiUrl = `${environment.apiUrl}/technicien`;

  constructor(private http: HttpClient) {}

  // T2 - Tableau de bord
  getMyInterventions(filters?: { etat?: string; priorite?: string; date?: string }): Observable<Intervention[]> {
    let params = new HttpParams();
    if (filters?.etat) params = params.set('etat', filters.etat);
    if (filters?.priorite) params = params.set('priorite', filters.priorite);
    if (filters?.date) params = params.set('date', filters.date);
    return this.http.get<Intervention[]>(`${this.apiUrl}/interventions`, { params });
  }

  getInterventionDetails(id: number): Observable<Intervention> {
    return this.http.get<Intervention>(`${this.apiUrl}/interventions/${id}`);
  }

  confirmerIntervention(id: number): Observable<Intervention> {
    return this.http.post<Intervention>(`${this.apiUrl}/interventions/${id}/confirmer`, {});
  }

  // T3 - Gestion intervention
  commencerIntervention(id: number): Observable<Intervention> {
    return this.http.post<Intervention>(`${this.apiUrl}/interventions/${id}/commencer`, {});
  }

  mettreEnPause(id: number): Observable<Intervention> {
    return this.http.post<Intervention>(`${this.apiUrl}/interventions/${id}/pause`, {});
  }

  reprendreIntervention(id: number): Observable<Intervention> {
    return this.http.post<Intervention>(`${this.apiUrl}/interventions/${id}/reprendre`, {});
  }

  ajouterPhotos(id: number, files: File[]): Observable<Intervention> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    return this.http.post<Intervention>(`${this.apiUrl}/interventions/${id}/photos`, formData);
  }

  ajouterCommentaire(id: number, commentaire: string): Observable<Intervention> {
    return this.http.post<Intervention>(`${this.apiUrl}/interventions/${id}/commentaire`, commentaire);
  }

  // T4 - Mise à jour état
  updateEtat(id: number, request: UpdateEtatInterventionRequest): Observable<Intervention> {
    return this.http.patch<Intervention>(`${this.apiUrl}/interventions/${id}/etat`, request);
  }

  // T7 - Rapport final
  soumettreRapportFinal(id: number, request: RapportFinalRequest): Observable<Intervention> {
    return this.http.post<Intervention>(`${this.apiUrl}/interventions/${id}/rapport-final`, request);
  }

  // T8 - Profil
  getProfil(): Observable<TechnicienProfil> {
    return this.http.get<TechnicienProfil>(`${this.apiUrl}/profil`);
  }

  updateProfil(request: UpdateProfilTechnicienRequest): Observable<TechnicienProfil> {
    return this.http.put<TechnicienProfil>(`${this.apiUrl}/profil`, request);
  }

  getStatistiques(): Observable<StatistiquesTechnicien> {
    return this.http.get<StatistiquesTechnicien>(`${this.apiUrl}/statistiques`);
  }

  // Main-d'œuvre
  affecterMainDOeuvre(id: number, request: AffecterMainDOeuvreRequest): Observable<Intervention> {
    return this.http.post<Intervention>(`${this.apiUrl}/interventions/${id}/affecter-main-doeuvre`, request);
  }

  desaffecterMainDOeuvre(interventionId: number, mainDOeuvreId: number): Observable<Intervention> {
    return this.http.delete<Intervention>(`${this.apiUrl}/interventions/${interventionId}/main-doeuvre/${mainDOeuvreId}`);
  }
}
