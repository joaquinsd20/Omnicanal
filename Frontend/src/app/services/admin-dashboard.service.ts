import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SuscripcionDetalle {
  plan: string;
  cantidad: number;
  precioUnitario: number;
  totalMensual: number;
}

export interface TopEmprendedor {
  idUsuario: number;
  nombreUsuario: string;
  email: string;
  plan: string;
  ventas: number;
  totalVendido: number;
  comisionGenerada: number;
}

export interface UsuarioRecienteAdmin {
  idUsuario: number;
  nombreUsuario: string;
  email: string;
  tipoUsuario: string;
  planSuscripcion: string;
  estado: string;
  createdAt: string;
}

export interface VentaRecienteAdmin {
  numeroVenta: string;
  fechaVenta: string;
  emprendedor: string;
  cliente: string;
  total: number;
  comision: number;
}

export interface AdminDashboardResumen {
  mes: string;
  totalUsuarios: number;
  usuariosActivos: number;
  usuariosBloqueados: number;
  administradores: number;
  emprendedoresActivos: number;
  totalProductos: number;
  totalImportaciones: number;
  totalVentas: number;
  ventasHoy: number;
  ventasBrutasMes: number;
  ventasBrutasTotales: number;
  comisionPlataformaPct: number;
  comisionesMes: number;
  suscripcionesMes: number;
  ingresosPlataformaMes: number;
  utilidadEmprendedoresMes: number;
  valorInventarioGlobal: number;
  ticketPromedioGlobal: number;
  suscripcionesDetalle: SuscripcionDetalle[];
  topEmprendedores: TopEmprendedor[];
  ultimosUsuarios: UsuarioRecienteAdmin[];
  ultimasVentas: VentaRecienteAdmin[];
}

@Injectable({
  providedIn: 'root'
})
export class AdminDashboardService {
  private readonly apiUrl = 'http://localhost:8080/api/admin/dashboard';

  constructor(private http: HttpClient) {}

  obtenerResumenGlobal(): Observable<AdminDashboardResumen> {
    return this.http.get<AdminDashboardResumen>(`${this.apiUrl}/resumen`);
  }
}
