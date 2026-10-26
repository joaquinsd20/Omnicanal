import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface SunatTokenResponse {
  access_token: string;
  token_type: string;
  expires_in: number;
}

export interface SunatValidationRequest {
  numRuc: string;
  codComp: string; // "01" = Factura, "03" = Boleta
  numeroSerie: string;
  numero: number;
  fechaEmision: string; // dd/mm/yyyy
  monto: string;
}

export interface SunatValidationResponse {
  success: boolean;
  message: string;
  data?: {
    estadoCp: number; // 0=No existe, 1=Aceptado, 2=Anulado, 3=Autorizado, 4=No autorizado
    estadoRuc: string;
    condDomiRuc: string;
    Observaciones?: string[];
  };
  errorCode?: string;
}

@Injectable({
  providedIn: 'root'
})
export class SunatTestService {
  
  // Usar endpoints del backend (proxy) para evitar CORS
  private readonly API_BASE = 'http://localhost:8080/api/sunat/test';

  constructor(private http: HttpClient) {}

  /**
   * Generar Token SUNAT mediante backend (proxy)
   * El backend evita el problema CORS de la API SUNAT
   */
  generateToken(clientId: string, clientSecret: string): Observable<SunatTokenResponse> {
    const url = `${this.API_BASE}/token`;

    return this.http.post<SunatTokenResponse>(url, null, {
      params: {
        clientId: clientId,
        clientSecret: clientSecret
      }
    })
      .pipe(
        catchError(error => {
          console.error('Error generando token SUNAT:', error);
          return throwError(() => ({
            message: `Error al generar token: ${error.error?.message || error.message}`,
            error
          }));
        })
      );
  }

  /**
   * Validar Comprobante (Factura o Boleta) mediante backend (proxy)
   * El backend evita el problema CORS de la API SUNAT
   */
  validateComprobante(
    rucConsultante: string,
    token: string,
    request: SunatValidationRequest
  ): Observable<SunatValidationResponse> {
    const url = `${this.API_BASE}/validar`;

    return this.http.post<SunatValidationResponse>(url, request, {
      params: {
        token: token
      }
    })
      .pipe(
        catchError(error => {
          console.error('Error validando comprobante SUNAT:', error);
          return throwError(() => ({
            message: `Error al validar comprobante: ${error.error?.message || error.message}`,
            error
          }));
        })
      );
  }

  /**
   * Helper: Mapear código de estado del comprobante
   */
  getEstadoComprobanteDescripcion(codigo: number): string {
    const estados: { [key: number]: string } = {
      0: '🔴 NO EXISTE - Comprobante no informado',
      1: '🟢 ACEPTADO - Comprobante aceptado',
      2: '🟡 ANULADO - Comunicado en una baja',
      3: '🔵 AUTORIZADO - Con autorización de imprenta',
      4: '🔴 NO AUTORIZADO - No autorizado por imprenta'
    };
    return estados[codigo] || 'Estado desconocido';
  }

  /**
   * Helper: Mapear código de estado del RUC
   */
  getEstadoRucDescripcion(codigo: string): string {
    const estados: { [key: string]: string } = {
      '00': 'ACTIVO',
      '01': 'BAJA PROVISIONAL',
      '02': 'BAJA PROV. POR OFICIO',
      '03': 'SUSPENSION TEMPORAL',
      '10': 'BAJA DEFINITIVA',
      '11': 'BAJA DE OFICIO',
      '22': 'INHABILITADO-VENTA ÚNICA'
    };
    return estados[codigo] || 'Estado desconocido: ' + codigo;
  }

  /**
   * Helper: Mapear código de condición de domicilio
   */
  getCondDomiRucDescripcion(codigo: string): string {
    const condiciones: { [key: string]: string } = {
      '00': 'HABIDO',
      '09': 'PENDIENTE',
      '11': 'POR VERIFICAR',
      '12': 'NO HABIDO',
      '20': 'NO HALLADO'
    };
    return condiciones[codigo] || 'Condición desconocida: ' + codigo;
  }
}
