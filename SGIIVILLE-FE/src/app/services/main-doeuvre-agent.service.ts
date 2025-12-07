import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { of } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class MainDOeuvreAgentService {
  private apiUrl = `${environment.apiUrl}/main-doeuvre`;

  constructor(private http: HttpClient) {}

  /**
   * Test de connexion API
   */
  testConnection(): Observable<any> {
    console.log('🔗 Test connexion API:', `${this.apiUrl}/test`);
    return this.http.get(`${this.apiUrl}/test`).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error('❌ Test API échoué:', error);
        return throwError(() => new Error(`API non disponible: ${error.status} ${error.statusText}`));
      })
    );
  }

  /**
   * Debug API
   */
  debug(): Observable<any> {
    console.log('🐛 Debug API:', `${this.apiUrl}/debug`);
    return this.http.get(`${this.apiUrl}/debug`).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error('❌ Debug API échoué:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Récupère le profil de l'agent connecté
   */
  getProfil(): Observable<any> {
    console.log('📤 Appel API profil:', `${this.apiUrl}/profil`);
    return this.http.get(`${this.apiUrl}/profil`).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error('❌ Erreur récupération profil:', error);
        console.error('URL:', error.url);
        console.error('Status:', error.status, error.statusText);

        // Pour le débogage, retournez un profil fictif
        const profilFictif = {
          id: 1,
          nom: 'Dupont',
          prenom: 'Jean',
          email: 'jean.dupont@example.com',
          matricule: 'MD001',
          cin: '12345678',
          telephone: '0123456789',
          role: 'MAIN_DOEUVRE',
          mainDOeuvreId: 1,
          competences: ['Plomberie', 'Électricité']
        };

        // Retourner le profil fictif pour tester le frontend
        return throwError(() => new Error(`Erreur ${error.status}: ${error.message}`));
      })
    );
  }

  /**
   * Récupère toutes les tâches assignées à l'agent
   */
  getMyTaches(filters?: { etat?: string }): Observable<any[]> {
    let params = new HttpParams();
    if (filters?.etat) {
      params = params.set('etat', filters.etat);
    }

    console.log('📤 Appel API taches:', `${this.apiUrl}/taches`, filters);

    return this.http.get<any[]>(`${this.apiUrl}/taches`, { params }).pipe(
      map(taches => {
        console.log('✅ Tâches reçues:', taches?.length || 0, 'tâches');
        // Si pas de tâches, retourner un tableau vide
        return taches || [];
      }),
      catchError((error: HttpErrorResponse) => {
        console.error('❌ Erreur récupération tâches:', error);
        console.error('URL:', error.url);
        console.error('Status:', error.status, error.statusText);

        // Pour le débogage, retournez des tâches fictives
        const tachesFictives = [
          {
            id: 1,
            libelle: 'Réparation fuite eau',
            description: 'Réparer la fuite dans la salle de bain',
            etat: 'A_FAIRE',
            interventionId: 1,
            mainDOeuvreId: 1,
            dateCreation: new Date().toISOString(),
            dateDebut: null,
            dateFin: null,
            interventionInfo: {
              id: 1,
              description: 'Intervention urgente - Fuite eau',
              datePlanifiee: new Date().toISOString(),
              etat: 'PLANIFIEE'
            }
          },
          {
            id: 2,
            libelle: 'Changement robinet',
            description: 'Remplacer le robinet de la cuisine',
            etat: 'EN_COURS',
            interventionId: 2,
            mainDOeuvreId: 1,
            dateCreation: new Date().toISOString(),
            dateDebut: new Date().toISOString(),
            dateFin: null,
            interventionInfo: {
              id: 2,
              description: 'Maintenance préventive',
              datePlanifiee: new Date().toISOString(),
              etat: 'EN_COURS'
            }
          }
        ];

        // Retourner des tâches fictives pour tester le frontend
        // return of(tachesFictives);
        return throwError(() => new Error(`Erreur ${error.status}: ${error.message}`));
      })
    );
  }
  /**
   * Récupère les détails d'une intervention spécifique
   */
  getInterventionDetails(interventionId: number): Observable<any> {
    console.log('📤 Appel API détails intervention:', `${this.apiUrl}/interventions/${interventionId}`);

    return this.http.get<any>(`${this.apiUrl}/interventions/${interventionId}`).pipe(
      map(intervention => {
        console.log('✅ Détails intervention reçus:', intervention);
        return intervention || {};
      }),
      catchError((error: HttpErrorResponse) => {
        console.error('❌ Erreur récupération détails intervention:', error);
        console.error('URL:', error.url);
        console.error('Status:', error.status, error.statusText);

        // Pour le débogage, retournez une intervention fictive
        const interventionFictive = {
          id: interventionId,
          description: 'Intervention de test',
          datePlanifiee: new Date().toISOString(),
          etat: 'PLANIFIEE',
          adresse: '123 Rue de Test, Ville',
          demandeur: 'Service Technique',
          taches: [
            {
              id: 1,
              libelle: 'Tâche de test',
              description: 'Description de la tâche de test',
              etat: 'A_FAIRE'
            }
          ]
        };

        // Retourner une intervention fictive pour tester le frontend
        // return of(interventionFictive);
        return throwError(() => new Error(`Erreur ${error.status}: Impossible de récupérer les détails de l'intervention`));
      })
    );
  }}
