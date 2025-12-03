import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Tache, TerminerTacheRequest, ChangerEtatTacheRequest } from '../models/tache.model';

@Injectable({
  providedIn: 'root'
})
export class MainDOeuvreTacheService {
  private apiUrl = `${environment.apiUrl}/main-doeuvre`;

  constructor(private http: HttpClient) {}

  /**
   * Récupérer toutes les tâches assignées à l'agent connecté
   */
  getMyTaches(filters?: { etat?: string }): Observable<Tache[]> {
    let params = new HttpParams();
    if (filters?.etat) params = params.set('etat', filters.etat);

    return this.http.get<Tache[]>(`${this.apiUrl}/taches`, { params });
  }

  /**
   * Récupérer les tâches d'une intervention assignées à l'agent
   */
  getTachesByIntervention(interventionId: number): Observable<Tache[]> {
    return this.http.get<Tache[]>(`${this.apiUrl}/interventions/${interventionId}/taches`);
  }

  /**
   * Récupérer les détails d'une tâche spécifique
   */
  getTacheById(tacheId: number): Observable<Tache> {
    return this.http.get<Tache>(`${this.apiUrl}/taches/${tacheId}`);
  }

  /**
   * Changer l'état d'une tâche
   */
  changerEtatTache(tacheId: number, request: ChangerEtatTacheRequest): Observable<Tache> {
    return this.http.put<Tache>(`${this.apiUrl}/taches/${tacheId}/etat`, request);
  }

  /**
   * Commencer une tâche (état A_FAIRE → EN_COURS)
   */
  commencerTache(tacheId: number): Observable<Tache> {
    const request: ChangerEtatTacheRequest = {
      nouvelEtat: 'EN_COURS',
      commentaire: 'Tâche commencée'
    };
    return this.changerEtatTache(tacheId, request);
  }

  /**
   * Terminer une tâche (état EN_COURS → TERMINEE)
   */
  terminerTache(tacheId: number, request: TerminerTacheRequest): Observable<Tache> {
    const changerEtatRequest: ChangerEtatTacheRequest = {
      nouvelEtat: 'TERMINEE',
      commentaire: request.commentaire,
      tempsPasseMinutes: request.tempsPasseMinutes
    };
    return this.changerEtatTache(tacheId, changerEtatRequest);
  }

  /**
   * Marquer une tâche comme vérifiée (état TERMINEE → VERIFIEE)
   */
  marquerCommeVerifiee(tacheId: number, commentaire?: string): Observable<Tache> {
    const request: ChangerEtatTacheRequest = {
      nouvelEtat: 'VERIFIEE',
      commentaire: commentaire || 'Tâche vérifiée'
    };
    return this.changerEtatTache(tacheId, request);
  }

  /**
   * Reporter une tâche (retour à A_FAIRE)
   */
  reporterTache(tacheId: number, commentaire: string): Observable<Tache> {
    const request: ChangerEtatTacheRequest = {
      nouvelEtat: 'A_FAIRE',
      commentaire: commentaire
    };
    return this.changerEtatTache(tacheId, request);
  }

  /**
   * Suspendre une tâche (état quelconque → SUSPENDUE)
   */
  suspendreTache(tacheId: number, commentaire: string): Observable<Tache> {
    const request: ChangerEtatTacheRequest = {
      nouvelEtat: 'SUSPENDUE',
      commentaire: commentaire
    };
    return this.changerEtatTache(tacheId, request);
  }

  /**
   * Reprendre une tâche suspendue (état SUSPENDUE → EN_COURS)
   */
  reprendreTache(tacheId: number, commentaire?: string): Observable<Tache> {
    const request: ChangerEtatTacheRequest = {
      nouvelEtat: 'EN_COURS',
      commentaire: commentaire || 'Tâche reprise'
    };
    return this.changerEtatTache(tacheId, request);
  }

  /**
   * Ajouter un commentaire à une tâche sans changer l'état
   */
  ajouterCommentaire(tacheId: number, commentaire: string): Observable<Tache> {
    return this.http.post<Tache>(`${this.apiUrl}/taches/${tacheId}/commentaire`, { commentaire });
  }

  /**
   * Récupérer l'historique des états d'une tâche
   */
  getHistoriqueTache(tacheId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/taches/${tacheId}/historique`);
  }

  /**
   * Méthodes originales maintenues pour la compatibilité
   */
  commencer(tacheId: number): Observable<Tache> {
    return this.commencerTache(tacheId);
  }

  terminer(tacheId: number, request: TerminerTacheRequest): Observable<Tache> {
    return this.terminerTache(tacheId, request);
  }
}
