import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario } from '../models/usuario.model';

@Injectable({
  providedIn: 'root'
})
export class UsuariosService {

  private apiUrl = 'http://localhost:8080/api/usuarios';

  constructor(private http: HttpClient) { }

  /**
   * Obtener todos los usuarios
   */
  obtenerTodos(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/admin/todos`);
  }

  /**
   * Obtener solo usuarios activos
   */
  obtenerActivos(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/admin/activos`);
  }

  /**
   * Obtener usuarios bloqueados
   */
  obtenerBloqueados(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/admin/bloqueados`);
  }

  /**
   * Obtener administradores globales
   */
  obtenerAdministradores(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/admin/administradores`);
  }

  /**
   * Obtener usuario por ID
   */
  obtenerPorId(idUsuario: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/${idUsuario}`);
  }

  /**
   * Crear nuevo usuario (solo para OG)
   */
  crearUsuario(usuario: Usuario): Observable<any> {
    return this.http.post(`${this.apiUrl}/admin/crear`, usuario);
  }

  /**
   * Bloquear usuario
   */
  bloquearUsuario(idUsuario: number): Observable<Usuario> {
    return this.http.put<Usuario>(
      `${this.apiUrl}/admin/${idUsuario}/bloquear`,
      {}
    );
  }

  /**
   * Desbloquear usuario
   */
  desbloquearUsuario(idUsuario: number): Observable<Usuario> {
    return this.http.put<Usuario>(
      `${this.apiUrl}/admin/${idUsuario}/desbloquear`,
      {}
    );
  }

  /**
   * Obtener estadísticas de usuarios
   */
  obtenerEstadisticas(): Observable<any> {
    return this.http.get(`${this.apiUrl}/admin/estadisticas`);
  }
}
