import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { Usuario } from '../models/usuario.model';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  private readonly TOKEN_KEY = 'omnicanal_token';
  private readonly USER_KEY = 'omnicanal_usuario';

  private usuarioActualSubject = new BehaviorSubject<Usuario | null>(null);
  public usuarioActual$ = this.usuarioActualSubject.asObservable();

  constructor(private http: HttpClient) {
    this.cargarUsuarioDelStorage();
  }

  /** Inicia sesión contra /api/auth/login y persiste token + usuario. */
  login(credenciales: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credenciales).pipe(
      tap((res) => this.guardarSesion(res))
    );
  }

  /** Registra un usuario contra /api/auth/register y persiste la sesión. */
  registro(datos: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, datos).pipe(
      tap((res) => this.guardarSesion(res))
    );
  }

  private guardarSesion(res: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, res.token);

    const usuario: Usuario = {
      idUsuario: res.idUsuario,
      nombreUsuario: res.nombreUsuario,
      email: res.email,
      password: '',
      tipoUsuario: (res.tipoUsuario || 'EMPRENDEDOR') as any,
      planSuscripcion: (res.planSuscripcion || 'PLAN_EMPRENDEDOR') as any,
      estado: (res.estado || 'ACTIVO') as any
    };
    localStorage.setItem(this.USER_KEY, JSON.stringify(usuario));
    this.usuarioActualSubject.next(usuario);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUsuarioActual(): Usuario | null {
    const actual = this.usuarioActualSubject.value;
    if (actual) {
      return actual;
    }
    // Fallback: intentar cargar desde localStorage si el BehaviorSubject está vacío
    const usuarioStorage = localStorage.getItem(this.USER_KEY);
    if (usuarioStorage) {
      try {
        const usuario = JSON.parse(usuarioStorage);
        this.usuarioActualSubject.next(usuario);
        return usuario;
      } catch {
        return null;
      }
    }
    return null;
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.usuarioActualSubject.next(null);
  }

  /** Está autenticado si hay token y no está expirado. */
  estaAutenticado(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    if (this.tokenExpirado(token)) {
      this.logout();
      return false;
    }
    return true;
  }

  /** Decodifica el payload del JWT (sin verificar firma) para leer exp. */
  private tokenExpirado(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (!payload.exp) {
        return false;
      }
      const ahora = Math.floor(Date.now() / 1000);
      return payload.exp < ahora;
    } catch {
      return true;
    }
  }

  private cargarUsuarioDelStorage(): void {
    const token = this.getToken();
    const usuario = localStorage.getItem(this.USER_KEY);
    if (token && !this.tokenExpirado(token) && usuario) {
      this.usuarioActualSubject.next(JSON.parse(usuario));
    } else if (token && this.tokenExpirado(token)) {
      this.logout();
    }
  }
}
