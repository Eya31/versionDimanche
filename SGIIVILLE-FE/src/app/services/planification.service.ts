import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';

export interface PlanificationRequest {
  demandeId: number;
  datePlanifiee: string;
  techniciensIds: number[];
  equipementsIds: number[];
  ressourcesIds: number[];
  priorite: 'HAUTE' | 'MOYENNE' | 'BASSE';
  budget: number;
}

export interface TechnicienDisponibilite {
  technicienId: number;
  nom: string;
  competences: string[];
  disponibilites: CreneauDisponibilite[];
  disponible: boolean;
}

export interface CreneauDisponibilite {
  date: string;
  creneaux: Creneau[];
}

export interface Creneau {
  debut: string;
  fin: string;
  disponible: boolean;
  interventionId?: number;
}

export interface RessourceDisponibilite {
  date: string;
  equipementsDisponibles: EquipementDisponibilite[];
  ressourcesDisponibles: RessourceMaterielleDisponibilite[];
  tousDisponibles: boolean;
  message?: string;
}

export interface EquipementDisponibilite {
  equipementId: number;
  type: string;
  disponible: boolean;
  raisonIndisponibilite?: string;
}

export interface RessourceMaterielleDisponibilite {
  ressourceId: number;
  designation: string;
  quantiteDisponible: number;
  quantiteDemandee: number;
  disponible: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class PlanificationService {
  private apiUrl = `${environment.apiUrl}/api/planification`;

  constructor(private http: HttpClient) {}

  // Récupérer les techniciens disponibles pour une date
  getTechniciensDisponibles(date: string, competences?: string): Observable<TechnicienDisponibilite[]> {
    let params = new HttpParams().set('date', date);
    if (competences) {
      params = params.set('competences', competences);
    }

    return this.http.get<TechnicienDisponibilite[]>(`${this.apiUrl}/techniciens/disponibles`, { params })
      .pipe(
        catchError(error => {
          console.error('Erreur récupération techniciens disponibles:', error);
          return of([]);
        })
      );
  }

  // Vérifier la disponibilité des ressources
  checkRessourcesDisponibles(
    date: string,
    equipementIds?: number[],
    ressourceIds?: number[]
  ): Observable<RessourceDisponibilite> {
    let params = new HttpParams().set('date', date);

    if (equipementIds && equipementIds.length > 0) {
      params = params.set('equipementIds', equipementIds.join(','));
    }

    if (ressourceIds && ressourceIds.length > 0) {
      params = params.set('ressourceIds', ressourceIds.join(','));
    }

    return this.http.get<RessourceDisponibilite>(`${this.apiUrl}/ressources/disponibles`, { params })
      .pipe(
        catchError(error => {
          console.error('Erreur vérification ressources:', error);
          return of({
            date,
            equipementsDisponibles: [],
            ressourcesDisponibles: [],
            tousDisponibles: false,
            message: 'Erreur lors de la vérification des disponibilités'
          });
        })
      );
  }

  // Planifier une intervention
  planifierIntervention(request: PlanificationRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/intervention`, request)
      .pipe(
        catchError(error => {
          console.error('Erreur planification intervention:', error);
          throw error;
        })
      );
  }

  // Récupérer le calendrier d'un technicien
  getCalendrierTechnicien(technicienId: number, mois: string): Observable<TechnicienDisponibilite[]> {
    return this.http.get<TechnicienDisponibilite[]>(`${this.apiUrl}/calendrier/${technicienId}`, {
      params: { mois }
    }).pipe(
      catchError(error => {
        console.error('Erreur récupération calendrier:', error);
        return of([]);
      })
    );
  }

  // Vérifier la disponibilité globale
  verifierDisponibiliteComplete(
    date: string,
    technicienIds: number[],
    equipementIds: number[],
    ressourceIds: number[]
  ): Observable<{techniciens: TechnicienDisponibilite[], ressources: RessourceDisponibilite}> {
    return new Observable(observer => {
      // Vérifier d'abord les techniciens
      this.getTechniciensDisponibles(date).subscribe(techniciens => {
        // Filtrer seulement les techniciens sélectionnés
        const techniciensFiltres = techniciens.filter(t =>
          technicienIds.includes(t.technicienId)
        );

        // Vérifier les ressources
        this.checkRessourcesDisponibles(date, equipementIds, ressourceIds).subscribe(ressources => {
          observer.next({
            techniciens: techniciensFiltres,
            ressources
          });
          observer.complete();
        });
      });
    });
  }
}
