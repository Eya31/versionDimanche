import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Demande } from '../models/demande.model';
import { catchError, throwError } from 'rxjs';
import { forkJoin, of } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class DemandeService {
  private baseUrl = 'http://localhost:8080/api/demandes';
 private citoyenCache = new Map<number, any>(); // Cache pour éviter les appels multiples
  constructor(private http: HttpClient) {}

  /** --------------------------------------------
   *  RÉCUPÉRER TOUTES LES DEMANDES
   * --------------------------------------------- */
  getAllDemandes(): Observable<Demande[]> {
    return this.http.get<Demande[]>(this.baseUrl);
  }

  /** --------------------------------------------
   *  RÉCUPÉRER LES DEMANDES D'UN CITOYEN
   * --------------------------------------------- */
  getDemandesByCitoyen(citoyenId: number): Observable<Demande[]> {
    return this.http.get<Demande[]>(`${this.baseUrl}/citoyen/${citoyenId}`);
  }

  /** --------------------------------------------
   *  RÉCUPÉRER UNE DEMANDE PAR ID
   * --------------------------------------------- */
  getDemandeById(id: number): Observable<Demande> {
    return this.http.get<Demande>(`${this.baseUrl}/${id}`);
  }

  /** --------------------------------------------
   *  CRÉER UNE DEMANDE (AVEC FILES OPTIONNELS)
   * --------------------------------------------- */
  createDemande(demande: Partial<Demande> | any, files?: File[]): Observable<any> {
    if (files && files.length > 0) {
      const fd = new FormData();
      fd.append('demande', JSON.stringify(demande));
      files.forEach(f => fd.append('files', f, f.name));
      return this.http.post<any>(this.baseUrl, fd);
    }
    return this.http.post<any>(this.baseUrl, demande);
  }

  /** --------------------------------------------
   * PLANIFIER UNE INTERVENTION (SIMPLE)
   * priorite = 'PLANIFIEE' | 'URGENTE'
   * --------------------------------------------- */
  planifierIntervention(demandeId: number): Observable<any> {
  return this.http.post<any>(`${this.baseUrl}/planifier/${demandeId}`, {}).pipe(
    catchError(error => {
      console.error('Erreur API planifier:', error);
      // Tu peux transformer l'erreur ici si besoin
      return throwError(() => error);
    })
  );
}

  /** --------------------------------------------
   * PLANIFIER UNE INTERVENTION COMPLÈTE
   * Avec technicien, ressources, détails
   * --------------------------------------------- */
  planifierInterventionComplete(request: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/planifier-complete`, request).pipe(
      catchError(error => {
        console.error('Erreur API planification complète:', error);
        return throwError(() => error);
      })
    );
  }

  /** --------------------------------------------
   * UPLOAD DE PHOTOS POUR UNE DEMANDE EXISTANTE
   * --------------------------------------------- */
  uploadPhotos(demandeId: number, files: File[]): Observable<any> {
    const fd = new FormData();
    files.forEach(f => fd.append('files', f, f.name));
    return this.http.post<any>(`${this.baseUrl}/${demandeId}/photos`, fd);
  }
  // demande.service.ts - Ajoutez cette méthode
/** --------------------------------------------
 *  RÉCUPÉRER LES INFOS CITOYEN POUR UNE DEMANDE
 * --------------------------------------------- */
;

// Méthode pour charger tous les noms de citoyens en parallèle
  loadAllCitoyenNames(demandes: Demande[]): Observable<Map<number, any>> {
    const requests: Observable<any>[] = [];
    const results = new Map<number, any>();

    demandes.forEach(demande => {
      if (!demande.isAnonymous && demande.citoyenId) {
        const id = typeof demande.citoyenId === 'string'
          ? parseInt(demande.citoyenId)
          : demande.citoyenId;

        requests.push(
          this.getCitoyenDetails(demande.id).pipe(
            map(data => ({ demandeId: demande.id, data }))
          )
        );
      }
    });

    if (requests.length === 0) {
      return of(results);
    }

    return forkJoin(requests).pipe(
      map(responses  => {
        responses.forEach(response => {
          results.set(response.demandeId, response.data);
        });
        return results;
      })
    );
  }
  getAllDemandesWithCitoyenInfo(): Observable<Demande[]> {
    return this.http.get<Demande[]>(this.baseUrl).pipe(
      map(demandes => {
        // Retourner les demandes sans bloquer le chargement
        return demandes;
      })
    );
  }

  // Récupérer les détails du citoyen pour une demande
  getCitoyenDetails(demandeId: number): Observable<any> {
    // Vérifier le cache d'abord
    if (this.citoyenCache.has(demandeId)) {
      return of(this.citoyenCache.get(demandeId));
    }

    return this.http.get<any>(`${this.baseUrl}/${demandeId}/citoyen-details`).pipe(
      map(data => {
        // Mettre en cache
        this.citoyenCache.set(demandeId, data);
        return data;
      }),
      catchError(error => {
        console.error('Erreur récupération détails citoyen:', error);
        return of(null);
      })
    );
  }
/** --------------------------------------------
 *  RÉCUPÉRER TOUTES LES DEMANDES AVEC NOMS CITOYENS
 * --------------------------------------------- */
getAllDemandesWithCitoyenNames(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/with-citoyen-names`);
}
}
