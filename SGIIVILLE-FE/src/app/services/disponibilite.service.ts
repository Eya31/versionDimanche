import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { TechnicienDisponibilite, RessourceDisponibilite, CreneauDisponibilite } from '../models/disponibilite.model';

@Injectable({
  providedIn: 'root'
})
export class DisponibiliteService {
  private apiUrl = `${environment.apiUrl}/api/disponibilite`;

  constructor(private http: HttpClient) {}

  // Récupérer le calendrier d'un technicien sur une période
  getCalendrierTechnicienPeriode(
    technicienId: number,
    startDate: Date,
    endDate: Date
  ): Observable<TechnicienDisponibilite[]> {
    const params = new HttpParams()
      .set('startDate', startDate.toISOString().split('T')[0])
      .set('endDate', endDate.toISOString().split('T')[0]);

    return this.http.get<TechnicienDisponibilite[]>(
      `${this.apiUrl}/technicien/${technicienId}/periode`,
      { params }
    ).pipe(
      catchError(error => {
        console.error('Erreur récupération calendrier technicien:', error);
        return of([]);
      })
    );
  }

  // Récupérer toutes les indisponibilités pour une période
  getIndisponibilites(startDate: Date, endDate: Date): Observable<any[]> {
    const params = new HttpParams()
      .set('startDate', startDate.toISOString().split('T')[0])
      .set('endDate', endDate.toISOString().split('T')[0]);

    return this.http.get<any[]>(`${this.apiUrl}/indisponibilites`, { params })
      .pipe(
        catchError(error => {
          console.error('Erreur récupération indisponibilités:', error);
          return of([]);
        })
      );
  }

  // Vérifier la disponibilité globale
  verifierDisponibiliteComplete(
    date: string,
    technicienIds?: number[],
    equipementIds?: number[],
    ressourceIds?: number[]
  ): Observable<RessourceDisponibilite> {
    let params = new HttpParams().set('date', date);

    if (technicienIds && technicienIds.length > 0) {
      params = params.set('technicienIds', technicienIds.join(','));
    }
    if (equipementIds && equipementIds.length > 0) {
      params = params.set('equipementIds', equipementIds.join(','));
    }
    if (ressourceIds && ressourceIds.length > 0) {
      params = params.set('ressourceIds', ressourceIds.join(','));
    }

    return this.http.post<RessourceDisponibilite>(`${this.apiUrl}/verifier`, {}, { params })
      .pipe(
        catchError(error => {
          console.error('Erreur vérification disponibilité:', error);
          return of({
            date,
            equipementsDisponibles: [],
            ressourcesDisponibles: [],
            tousDisponibles: false,
            message: 'Erreur lors de la vérification'
          });
        })
      );
  }

  // Marquer un technicien comme indisponible
  marquerIndisponible(
    technicienId: number,
    date: string,
    creneaux: { debut: string, fin: string }[],
    raison: string
  ): Observable<any> {
    return this.http.post(`${this.apiUrl}/indisponible`, {
      technicienId,
      date,
      creneaux,
      raison
    });
  }

  // Générer un rapport de disponibilité
  genererRapportDisponibilite(startDate: Date, endDate: Date): Observable<Blob> {
    const params = new HttpParams()
      .set('startDate', startDate.toISOString().split('T')[0])
      .set('endDate', endDate.toISOString().split('T')[0]);

    return this.http.get(`${this.apiUrl}/rapport`, {
      params,
      responseType: 'blob'
    });
  }

  // Calculer les statistiques de disponibilité
  getStatistiquesDisponibilite(mois: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/statistiques`, {
      params: { mois }
    }).pipe(
      catchError(error => {
        console.error('Erreur statistiques disponibilité:', error);
        return of({
          tauxDisponibilite: 0,
          techniciensDisponibles: 0,
          interventionsPlanifiees: 0
        });
      })
    );
  }
}
