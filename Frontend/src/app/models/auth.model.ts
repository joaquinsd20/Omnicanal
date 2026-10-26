import { EstadoUsuario, PlanSuscripcion, TipoUsuario } from './usuario.model';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nombreUsuario: string;
  email: string;
  password: string;
  telefono?: string;
  razonSocial?: string;
  ruc?: string;
  ciudad?: string;
  tipoUsuario?: TipoUsuario;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  expiraEnMs: number;
  idUsuario: number;
  nombreUsuario: string;
  email: string;
  tipoUsuario?: TipoUsuario;
  planSuscripcion?: PlanSuscripcion;
  estado?: EstadoUsuario;
}

/** Estructura del error JSON estandarizado que devuelve el backend. */
export interface ApiError {
  timestamp?: string;
  status: number;
  error: string;
  message: string;
  path?: string;
  errores?: { [campo: string]: string };
}
