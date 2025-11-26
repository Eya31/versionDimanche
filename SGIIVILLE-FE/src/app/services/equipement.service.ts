import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Equipement } from '../models/equipement.model';

@Injectable({
  providedIn: 'root'
})
export class EquipementService {
  private readonly baseUrl = 'http://localhost:8080/api/equipements';

  constructor(private http: HttpClient) {}

  getAllEquipements(): Observable<Equipement[]> {
    return this.http.get<Equipement[]>(this.baseUrl);
  }

  getEquipementById(id: number): Observable<Equipement> {
    return this.http.get<Equipement>(`${this.baseUrl}/${id}`);
  }

  createEquipement(equipement: Equipement): Observable<Equipement> {
    // S'assurer que l'équipement est disponible à la création
    const equipementToCreate = {
      ...equipement,
      disponible: true
    };
    return this.http.post<Equipement>(this.baseUrl, equipementToCreate);
  }

  updateEquipement(id: number, equipement: Equipement): Observable<Equipement> {
    return this.http.put<Equipement>(`${this.baseUrl}/${id}`, equipement);
  }

  deleteEquipement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
