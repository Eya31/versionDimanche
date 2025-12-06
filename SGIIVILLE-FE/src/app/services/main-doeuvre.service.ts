import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  MainDOeuvre,
  CreateMainDOeuvreRequest,
  VerificationAffectationDTO
} from '../models/main-doeuvre.model';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class MainDOeuvreService {
  private apiUrl = `${environment.apiUrl}/technicien/main-doeuvre`;
  private adminApiUrl = `${environment.apiUrl}/admin/main-doeuvre`;

  constructor(private http: HttpClient) {}

  /**
   * Récupère toutes les fiches de main-d'œuvre
   * Utilise l'endpoint admin si disponible, sinon l'endpoint technicien
   */
  getAll(filters?: { competence?: string; disponibilite?: string }, useAdmin: boolean = false): Observable<MainDOeuvre[]> {
    let params = new HttpParams();
    if (filters?.competence) params = params.set('competence', filters.competence);
    if (filters?.disponibilite) params = params.set('disponibilite', filters.disponibilite);
    const url = useAdmin ? this.adminApiUrl : this.apiUrl;
    return this.http.get<MainDOeuvre[]>(url, { params });
  }

  /**
   * Récupère une fiche de main-d'œuvre par ID
   */
  getById(id: number, useAdmin: boolean = false): Observable<MainDOeuvre> {
    const url = useAdmin ? this.adminApiUrl : this.apiUrl;
    return this.http.get<MainDOeuvre>(`${url}/${id}`);
  }

  /**
   * Crée une nouvelle fiche de main-d'œuvre
   */
  create(mainDOeuvre: CreateMainDOeuvreRequest | MainDOeuvre, useAdmin: boolean = false): Observable<any> {
    const url = useAdmin ? this.adminApiUrl : this.apiUrl;
    // Le backend retourne maintenant un objet avec mainDOeuvre, userId, defaultPassword, message
    return this.http.post<any>(url, mainDOeuvre);
  }

  /**
   * Met à jour une fiche de main-d'œuvre
   */
  update(id: number, mainDOeuvre: MainDOeuvre, useAdmin: boolean = false): Observable<MainDOeuvre> {
    const url = useAdmin ? this.adminApiUrl : this.apiUrl;
    return this.http.put<MainDOeuvre>(`${url}/${id}`, mainDOeuvre);
  }

  /**
   * Archive une fiche de main-d'œuvre
   */
  archiver(id: number, useAdmin: boolean = false): Observable<void> {
    const url = useAdmin ? this.adminApiUrl : this.apiUrl;
    return this.http.delete<void>(`${url}/${id}`);
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
   * @deprecated HistoriqueInterventions supprimé du schéma XSD
   */
  getHistorique(id: number): Observable<any[]> {
    // HistoriqueInterventions n'est plus dans le schéma XSD - retourner un tableau vide
    return new Observable(observer => {
      observer.next([]);
      observer.complete();
    });
  }
  getProfil(): Observable<MainDOeuvre> {
    console.log('URL appelée:', `${this.apiUrl}/profil`);
    return this.http.get<MainDOeuvre>(`${this.apiUrl}/profil`).pipe(
        catchError(error => {
            console.error('Erreur détaillée:', error);
            return throwError(() => error);
        })
    );
}
}

