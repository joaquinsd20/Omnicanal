import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Producto } from '../models/producto.model';

@Injectable({
  providedIn: 'root'
})
export class ProductoService {
  private apiUrl = 'http://localhost:8080/api/productos';

  constructor(private http: HttpClient) { }

  obtenerTodos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl);
  }

  obtenerPorId(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`);
  }

  obtenerPorUsuario(idUsuario: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/usuario/${idUsuario}`);
  }

  crear(producto: Producto): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, producto);
  }

  actualizar(id: number, producto: Producto): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, producto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ===== MÉTODOS DE BÚSQUEDA CON JPQL =====

  /**
   * Busca productos por nombre
   */
  buscarPorNombre(nombre: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/buscar/nombre`, {
      params: { q: nombre }
    });
  }

  /**
   * Búsqueda general (nombre o SKU)
   */
  buscarGeneral(busqueda: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/buscar/general`, {
      params: { q: busqueda }
    });
  }

  /**
   * Busca productos por categoría
   */
  buscarPorCategoria(categoria: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/buscar/categoria`, {
      params: { categoria: categoria }
    });
  }

  /**
   * Obtiene productos con stock por debajo del mínimo
   */
  obtenerProductosConStockBajo(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/stock-bajo`);
  }
}
