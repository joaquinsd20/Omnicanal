import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ImportacionService, MovimientoInventario } from '../../services/importacion.service';
import { AuthService } from '../../services/auth.service';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-kardex',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './kardex.component.html',
  styleUrls: ['./kardex.component.css']
})
export class KardexComponent implements OnInit {

  movimientos: MovimientoInventario[] = [];
  movimientosFiltrados: MovimientoInventario[] = [];
  cargando = false;
  error: string | null = null;

  // Filtros
  filtroTipo = '';
  filtroProducto = '';
  tiposDisponibles = ['entrada', 'salida', 'ajuste', 'devolución'];

  constructor(
    private importacionService: ImportacionService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarKardex();
  }

  cargarKardex(): void {
    this.cargando = true;
    this.error = null;

    const usuario = this.authService.getUsuarioActual();
    this.importacionService.obtenerKardex(usuario?.idUsuario).subscribe({
      next: (datos) => {
        this.movimientos = datos;
        this.filtroTipo = ''; // Asegurar que carga todos por defecto
        this.aplicarFiltros();
        this.cargando = false;
      },
      error: (err) => {
        this.cargando = false;
        console.error('Error al cargar kardex:', err);
        this.error = 'No se pudieron cargar los movimientos de inventario';
      }
    });
  }

  aplicarFiltros(): void {
    this.movimientosFiltrados = this.movimientos.filter(mov => {
      const cumpleTipo = !this.filtroTipo || mov.tipoMovimiento === this.filtroTipo;
      const cumpleProducto = !this.filtroProducto || 
        mov.nombreProducto.toLowerCase().includes(this.filtroProducto.toLowerCase());
      return cumpleTipo && cumpleProducto;
    });
  }

  cambioFiltro(): void {
    this.aplicarFiltros();
  }

  limpiarFiltros(): void {
    this.filtroTipo = '';
    this.filtroProducto = '';
    this.aplicarFiltros();
  }

  obtenerClaseMovimiento(tipo: string): string {
    switch (tipo.toLowerCase()) {
      case 'entrada':
        return 'badge-success';
      case 'salida':
        return 'badge-danger';
      case 'ajuste':
        return 'badge-info';
      case 'devolución':
        return 'badge-warning';
      default:
        return 'badge-secondary';
    }
  }

  obtenerIconoMovimiento(tipo: string): string {
    switch (tipo.toLowerCase()) {
      case 'entrada':
        return '📥';
      case 'salida':
        return '📤';
      case 'ajuste':
        return '🔧';
      case 'devolución':
        return '↩️';
      default:
        return '•';
    }
  }

  exportarKardex(): void {
    // Headers
    const headers = ['Fecha', 'Tipo', 'Producto', 'Cantidad', 'Stock Anterior', 'Stock Posterior', 'Referencia', 'Descripción'];
    
    // Convertir datos
    const data = this.movimientosFiltrados.map(mov => [
      new Date(mov.createdAt).toLocaleDateString('es-PE'),
      mov.tipoMovimiento || '',
      mov.nombreProducto || '',
      mov.cantidad || 0,
      mov.stockAnterior || 0,
      mov.stockPosterior || 0,
      mov.referencia || '',
      mov.descripcion || ''
    ]);

    // Crear workbook y hoja
    const ws = XLSX.utils.aoa_to_sheet([headers, ...data]);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Kardex');

    // Aplicar formato
    this.formatearHojaExcel(ws, headers.length, data.length + 1);

    // Ajustar ancho de columnas
    const colWidths = [15, 12, 20, 12, 15, 15, 20, 25];
    ws['!cols'] = colWidths.map(w => ({ wch: w }));

    // Guardar archivo
    XLSX.writeFile(wb, `kardex_${new Date().toISOString().split('T')[0]}.xlsx`);
  }

  private formatearHojaExcel(ws: XLSX.WorkSheet, cols: number, rows: number): void {
    // Colores y estilos
    const headerFill = { patternType: 'solid', fgColor: { rgb: '2E7D32' } }; // Verde
    const headerFont = { bold: true, color: { rgb: 'FFFFFF' } }; // Blanco
    const headerAlignment = { horizontal: 'center', vertical: 'center', wrapText: true };
    
    const border = {
      top: { style: 'thin', color: { rgb: '000000' } },
      bottom: { style: 'thin', color: { rgb: '000000' } },
      left: { style: 'thin', color: { rgb: '000000' } },
      right: { style: 'thin', color: { rgb: '000000' } }
    };

    // Aplicar formato a headers (fila 1)
    for (let c = 0; c < cols; c++) {
      const cellRef = XLSX.utils.encode_cell({ r: 0, c });
      if (ws[cellRef]) {
        ws[cellRef].fill = headerFill;
        ws[cellRef].font = headerFont;
        ws[cellRef].alignment = headerAlignment;
        ws[cellRef].border = border;
      }
    }

    // Aplicar bordes a datos
    for (let r = 1; r < rows; r++) {
      for (let c = 0; c < cols; c++) {
        const cellRef = XLSX.utils.encode_cell({ r, c });
        if (ws[cellRef]) {
          ws[cellRef].border = border;
          // Alternancia de color para mejor lectura
          if (r % 2 === 0) {
            ws[cellRef].fill = { patternType: 'solid', fgColor: { rgb: 'F5F5F5' } };
          }
        }
      }
    }

    // Congelar la fila de headers
    ws['!freeze'] = { xSplit: 0, ySplit: 1 };
  }

  retroceder(): void {
    this.router.navigate(['/dashboard']);
  }
}
