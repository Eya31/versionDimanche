import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

// ✅ Corriger le nom de l'interface (supprimer les fautes de frappe)
export interface DemandeAjoutMateriel {
  id: number;
  typeDemande: 'EQUIPEMENT' | 'RESSOURCE';
  designation: string;
  quantite: number;
  budget: number;
  justification: string;
  etat: 'EN_ATTENTE_ADMIN' | 'ACCEPTEE' | 'REFUSEE';
  dateDemande: string;
  chefId: number;
  adminId?: number;
  dateTraitement?: string;
  motifRefus?: string;
}

export interface DemandeRessource extends Omit<DemandeAjoutMateriel, 'typeDemande'> {
  typeDemande: 'RESSOURCE';
}

export interface DemandeEquipement extends Omit<DemandeAjoutMateriel, 'typeDemande'> {
  typeDemande: 'EQUIPEMENT';
}

export interface CreateDemandeRequest {
  typeDemande: 'EQUIPEMENT' | 'RESSOURCE';
  designation: string;
  quantite: number;
  budget: number;
  justification: string;
  chefId: number;
}

export interface CreateDemandeRessourceRequest extends Omit<CreateDemandeRequest, 'typeDemande'> {
  typeDemande?: 'RESSOURCE';
}

export interface CreateDemandeEquipementRequest extends Omit<CreateDemandeRequest, 'typeDemande'> {
  typeDemande?: 'EQUIPEMENT';
}

export interface TraitementDemandeRequest {
  adminId: number;
}

export interface RefusDemandeRequest {
  adminId: number;
  motifRefus: string;
}

@Injectable({
  providedIn: 'root'
})
export class DemandeAjoutMaterielService {
  private baseUrl = 'http://localhost:8080/api/demandes-ajout';

  constructor(private http: HttpClient) {}

  // === MÉTHODES GÉNÉRALES POUR TOUTES LES DEMANDES ===

  // Créer une demande (ressource ou équipement)
  creerDemande(demande: CreateDemandeRequest): Observable<DemandeAjoutMateriel> {
    return this.http.post<DemandeAjoutMateriel>(this.baseUrl, demande);
  }

  // Obtenir toutes les demandes d'un chef
  getDemandesParChef(chefId: number): Observable<DemandeAjoutMateriel[]> {
    return this.http.get<DemandeAjoutMateriel[]>(`${this.baseUrl}/chef/${chefId}`);
  }

  // Obtenir toutes les demandes en attente (admin)
  getDemandesEnAttente(): Observable<DemandeAjoutMateriel[]> {
    return this.http.get<DemandeAjoutMateriel[]>(`${this.baseUrl}/admin/en-attente`);
  }

  // Obtenir toutes les demandes (admin)
  getAllDemandes(): Observable<DemandeAjoutMateriel[]> {
    return this.http.get<DemandeAjoutMateriel[]>(`${this.baseUrl}/admin`);
  }

  // Accepter une demande (générique)
  accepterDemande(demandeId: number, adminId: number): Observable<DemandeAjoutMateriel> {
    return this.http.post<DemandeAjoutMateriel>(
      `${this.baseUrl}/admin/${demandeId}/accepter`,
      { adminId }
    );
  }

  // Refuser une demande (générique)
  refuserDemande(demandeId: number, adminId: number, motifRefus: string): Observable<DemandeAjoutMateriel> {
    return this.http.post<DemandeAjoutMateriel>(
      `${this.baseUrl}/admin/${demandeId}/refuser`,
      { adminId, motifRefus }
    );
  }

  // === MÉTHODES SPÉCIFIQUES AUX RESSOURCES ===

  // Créer une demande de ressource (surcharge pour simplifier)
  creerDemandeRessource(demande: CreateDemandeRessourceRequest): Observable<DemandeRessource> {
    const fullDemande: CreateDemandeRequest = {
      ...demande,
      typeDemande: 'RESSOURCE'
    };
    return this.http.post<DemandeRessource>(this.baseUrl, fullDemande);
  }

  // Obtenir les demandes de ressources d'un chef
  getDemandesRessourcesParChef(chefId: number): Observable<DemandeRessource[]> {
    return this.getDemandesParChef(chefId).pipe(
      map((demandes: DemandeAjoutMateriel[]) =>
        demandes.filter(d => d.typeDemande === 'RESSOURCE') as DemandeRessource[]
      )
    );
  }

  // Obtenir toutes les demandes de ressources (admin)
  getAllDemandesRessources(): Observable<DemandeRessource[]> {
    return this.getAllDemandes().pipe(
      map((demandes: DemandeAjoutMateriel[]) =>
        demandes.filter(d => d.typeDemande === 'RESSOURCE') as DemandeRessource[]
      )
    );
  }

  // Obtenir les demandes de ressources en attente (admin)
  getDemandesRessourcesEnAttente(): Observable<DemandeRessource[]> {
    return this.http.get<DemandeRessource[]>(`${this.baseUrl}/admin/ressources/en-attente`);
  }

  // Obtenir les demandes d'équipements en attente (admin)
  getDemandesEquipementsEnAttente(): Observable<DemandeEquipement[]> {
    return this.http.get<DemandeEquipement[]>(`${this.baseUrl}/admin/equipements/en-attente`);
  }

  // Accepter une demande de ressource avec mise à jour du stock
  accepterDemandeRessource(demandeId: number, adminId: number): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/admin/${demandeId}/accepter-et-mettre-a-jour`,
      { adminId }
    );
  }

  // === MÉTHODES SPÉCIFIQUES AUX ÉQUIPEMENTS ===

  // Créer une demande d'équipement (surcharge pour simplifier)
  creerDemandeEquipement(demande: CreateDemandeEquipementRequest): Observable<DemandeEquipement> {
    const fullDemande: CreateDemandeRequest = {
      ...demande,
      typeDemande: 'EQUIPEMENT'
    };
    return this.http.post<DemandeEquipement>(this.baseUrl, fullDemande);
  }

  // Obtenir les demandes d'équipements d'un chef
  getDemandesEquipementsParChef(chefId: number): Observable<DemandeEquipement[]> {
    return this.getDemandesParChef(chefId).pipe(
      map((demandes: DemandeAjoutMateriel[]) =>
        demandes.filter(d => d.typeDemande === 'EQUIPEMENT') as DemandeEquipement[]
      )
    );
  }

  // Obtenir toutes les demandes d'équipements (admin)
  getAllDemandesEquipements(): Observable<DemandeEquipement[]> {
    return this.getAllDemandes().pipe(
      map((demandes: DemandeAjoutMateriel[]) =>
        demandes.filter(d => d.typeDemande === 'EQUIPEMENT') as DemandeEquipement[]
      )
    );
  }

  // === MÉTHODES UTILITAIRES ===

  // Statistiques (si endpoint disponible)
  getStatsDemandes(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/admin/stats`);
  }

  // Test notification (si endpoint disponible)
  testNotification(chefId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/test-notification-chef`, { chefId });
  }

  // Debug (si endpoint disponible)
  debugTestNotificationChef(chefId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/debug/test-notification-chef`, { chefId });
  }

  // Info debug (si endpoint disponible)
  debugInfo(userId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/debug/info/${userId}`);
  }
}