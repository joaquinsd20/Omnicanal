import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';

export interface ItemProrrateo {
  idProducto: number;
  cantidad: number;
  precioUnitarioOriginal: number;
}

export interface CrearImportacionRequest {
  numeroFactura: string;
  fechaFactura: string;
  proveedorInternacional: string;
  paisOrigen: string;
  montoOriginal: number;
  monedaOriginal: string;
  tipoCambio: number;
  costoFlete: number;
  costoAduanas: number;
  costoAranceles: number;
  costoTransporteLocal?: number;
  otrosCostos?: number;
  cantidadItems: number;
  pesoTotalKg?: number;
  volumenTotalM3?: number;
  fechaLlegadaEstimada?: string;
  items: ItemProrrateo[];
}

export interface ProrrateoResponse {
  idProrrateo: number;
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitarioOriginal: number;
  costoFleteAsignado: number;
  costoAduanasAsignado: number;
  costoArancelesAsignado: number;
  costoUnitarioFinal: number;
}

export interface ImportacionResponse {
  idImportacion: number;
  idUsuario: number;
  numeroFactura: string;
  fechaFactura: string;
  proveedorInternacional: string;
  paisOrigen: string;
  montoOriginal: number;
  monedaOriginal: string;
  tipoCambio: number;
  montoEnSoles: number;
  costoFlete: number;
  costoAduanas: number;
  costoAranceles: number;
  costoTransporteLocal: number;
  otrosCostos: number;
  costoTotal: number;
  cantidadItems: number;
  pesoTotalKg?: number;
  volumenTotalM3?: number;
  estadoProcesamiento: string;
  estadoImportacion: string;
  prorrateos: ProrrateoResponse[];
  createdAt: string;
}

export interface MovimientoInventario {
  idMovimiento: number;
  idProducto: number;
  nombreProducto: string;
  tipoMovimiento: string;
  cantidad: number;
  referencia: string;
  descripcion: string;
  stockAnterior: number;
  stockPosterior: number;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class ImportacionService {
  private apiUrl = 'http://localhost:8080/api/importaciones';
  private kardexUrl = 'http://localhost:8080/api/kardex';

  constructor(private http: HttpClient) { }

  crearImportacion(request: CrearImportacionRequest, idUsuario: number): Observable<ImportacionResponse> {
    const headers = new HttpHeaders({
      'X-User-Id': idUsuario.toString()
    });
    return this.http.post<ImportacionResponse>(this.apiUrl, request, { headers });
  }

  obtenerImportaciones(idUsuario: number): Observable<ImportacionResponse[]> {
    const headers = new HttpHeaders({
      'X-User-Id': idUsuario.toString()
    });
    return this.http.get<ImportacionResponse[]>(this.apiUrl, { headers });
  }

  obtenerImportacion(id: number): Observable<ImportacionResponse> {
    return this.http.get<ImportacionResponse>(`${this.apiUrl}/${id}`);
  }

  obtenerKardex(idUsuario?: number): Observable<MovimientoInventario[]> {
    const headers = idUsuario ? new HttpHeaders({
      'X-User-Id': idUsuario.toString()
    }) : undefined;
    return this.http.get<MovimientoInventario[]>(this.kardexUrl, headers ? { headers } : {});
  }

  obtenerKardexPorProducto(idProducto: number): Observable<MovimientoInventario[]> {
    return this.http.get<MovimientoInventario[]>(`${this.kardexUrl}/producto/${idProducto}`);
  }

  obtenerKardexPorTipo(tipoMovimiento: string): Observable<MovimientoInventario[]> {
    return this.http.get<MovimientoInventario[]>(`${this.kardexUrl}/tipo/${tipoMovimiento}`);
  }
}
