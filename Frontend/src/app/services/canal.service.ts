import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Canal } from '../models/canal.model';

@Injectable({
  providedIn: 'root'
})
export class CanalService {
  private apiUrl = 'http://localhost:8080/api/canales';

  constructor(private http: HttpClient) { }

  obtenerCanalesDelUsuario(idUsuario: number): Observable<Canal[]> {
    const headers = new HttpHeaders({
      'X-User-Id': idUsuario.toString()
    });
    return this.http.get<Canal[]>(`${this.apiUrl}/usuario/${idUsuario}`, { headers });
  }

  obtenerCanal(idCanal: number): Observable<Canal> {
    return this.http.get<Canal>(`${this.apiUrl}/${idCanal}`);
  }

  crearCanal(canal: Canal, idUsuario: number): Observable<Canal> {
    const headers = new HttpHeaders({
      'X-User-Id': idUsuario.toString()
    });
    return this.http.post<Canal>(this.apiUrl, canal, { headers });
  }

  actualizarCanal(idCanal: number, canal: Canal): Observable<Canal> {
    return this.http.put<Canal>(`${this.apiUrl}/${idCanal}`, canal);
  }

  eliminarCanal(idCanal: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${idCanal}`);
  }
}
