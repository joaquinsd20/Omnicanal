import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DashboardResumen {
  totalIngresos: number;
  totalVentas: number;
  ticketPromedio: number;
  totalCostos: number;
  costoImportaciones: number;
  costoEnvios: number;
  gananciaNeta: number;
  margenNetoPct: number;
  valorInventario: number;
  mes: string;
}

export interface VentasPorCanal {
  nombreCanal: string;
  tipoCanal?: string;
  cantidadVentas: number;
  totalVentas?: number;
  montoTotal?: number;
  porcentajeDelTotal?: number;
}

export interface ProductoTop {
  idProducto: number;
  nombreProducto: string;
  precioCosto: number;
  precioVenta: number;
  stockActual: number;
  margenGanancia: number;
  cantidadVendida: number;
  ingresos: number;
}

export interface InventarioResumen {
  totalProductos: number;
  productosConStockBajo: number;
  productosSinStock: number;
  valorTotalInventario: number;
  productosAlertaDetalle: any[];
}

export interface TendenciaDia {
  fecha: string;
  fechaIso?: string;
  ingresos: number;
}

export interface ResumenDiario {
  ingresos: number;
  costos: number;
  ganancia: number;
  ventasHoy: number;
  productosVendidos: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private apiUrl = 'http://localhost:8080/api/dashboard';

  constructor(private http: HttpClient) {}

  obtenerResumen(): Observable<DashboardResumen> {
    return this.http.get<DashboardResumen>(`${this.apiUrl}/resumen`);
  }

  obtenerVentasPorCanal(): Observable<VentasPorCanal[]> {
    return this.http.get<VentasPorCanal[]>(`${this.apiUrl}/ventas-por-canal`);
  }

  obtenerTopProductos(limite = 5): Observable<ProductoTop[]> {
    return this.http.get<ProductoTop[]>(`${this.apiUrl}/productos-top`, {
      params: { limite }
    });
  }

  obtenerInventario(): Observable<InventarioResumen> {
    return this.http.get<InventarioResumen>(`${this.apiUrl}/inventario`);
  }

  obtenerTendencia7Dias(): Observable<TendenciaDia[]> {
    return this.http.get<TendenciaDia[]>(`${this.apiUrl}/tendencia-7-dias`);
  }

  obtenerResumenDiario(): Observable<ResumenDiario> {
    return this.http.get<ResumenDiario>(`${this.apiUrl}/resumen-diario`);
  }
}
