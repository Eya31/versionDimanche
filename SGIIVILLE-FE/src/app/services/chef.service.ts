import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface Technicien {
  id: number;
  nom: string;
  prenom: string;
  // ajoute d'autres champs si besoin
}

@Injectable({
  providedIn: 'root'
})
export class ChefService {

  private apiUrl = 'http://localhost:8080/api/chef'; // <--- URL backend correcte

  constructor(private http: HttpClient) { }

  getTechniciens(): Observable<Technicien[]> {
    return this.http.get<Technicien[]>(`${this.apiUrl}/techniciens`)
      .pipe(
        catchError(err => {
          console.error('Erreur chargement techniciens:', err);
          throw err;
        })
      );
  }
}
