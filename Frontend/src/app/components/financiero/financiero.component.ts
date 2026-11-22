import { Component, OnInit } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DashboardService, DashboardResumen, ResumenDiario, TendenciaDia, ProductoTop } from '../../services/dashboard.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-financiero',
  standalone: true,
  imports: [IconComponent, CommonModule],
  templateUrl: './financiero.component.html',
  styleUrls: ['./financiero.component.css']
})
export class FinancieroComponent implements OnInit {
  // Todo se carga desde la API (/dashboard/*); arranca en cero mientras llega.
  cargando = true;

  dashboardFinanciero = {
    totalIngresos: 0,
    totalCostos: 0,
    gananciaNeta: 0,
    margenNeto: 0,
    ventasDelDia: 0,
    ticketPromedio: 0,
    valorInventario: 0,
    productosVendidos: 0
  };

  resumenDiario = { ingresos: 0, costos: 0, ganancia: 0, ventasHoy: 0 };

  ventasUltimos7Dias: { fecha: string; ingresos: number }[] = [];

  topProductos: { nombre: string; vendidos: number; ingresos: number }[] = [];

  private readonly dias: Record<string, string> = {
    MON: 'Lun', TUE: 'Mar', WED: 'Mié', THU: 'Jue', FRI: 'Vie', SAT: 'Sáb', SUN: 'Dom', Hoy: 'Hoy'
  };

  constructor(
    private authService: AuthService,
    private router: Router,
    private dashboardService: DashboardService
  ) { }

  ngOnInit() {
    if (!this.authService.getUsuarioActual()) {
      this.router.navigate(['/login']);
      return;
    }
    forkJoin({
      resumen: this.dashboardService.obtenerResumen().pipe(catchError(() => of(null as DashboardResumen | null))),
      diario: this.dashboardService.obtenerResumenDiario().pipe(catchError(() => of(null as ResumenDiario | null))),
      tendencia: this.dashboardService.obtenerTendencia7Dias().pipe(catchError(() => of([] as TendenciaDia[]))),
      top: this.dashboardService.obtenerTopProductos(5).pipe(catchError(() => of([] as ProductoTop[])))
    }).subscribe(({ resumen, diario, tendencia, top }) => {
      const n = (v: unknown) => Number(v ?? 0);
      this.dashboardFinanciero = {
        totalIngresos: n(resumen?.totalIngresos),
        totalCostos: n(resumen?.totalCostos),
        gananciaNeta: n(resumen?.gananciaNeta),
        margenNeto: n(resumen?.margenNetoPct),
        ventasDelDia: n(resumen?.totalVentas),
        ticketPromedio: n(resumen?.ticketPromedio),
        valorInventario: n(resumen?.valorInventario),
        productosVendidos: n(diario?.productosVendidos)
      };
      this.resumenDiario = {
        ingresos: n(diario?.ingresos),
        costos: n(diario?.costos),
        ganancia: n(diario?.ganancia),
        ventasHoy: n(diario?.ventasHoy)
      };
      this.ventasUltimos7Dias = tendencia.map(d => ({ fecha: this.dias[d.fecha] ?? d.fecha, ingresos: n(d.ingresos) }));
      this.topProductos = top.map(p => ({ nombre: p.nombreProducto, vendidos: n(p.cantidadVendida), ingresos: n(p.ingresos) }));
      this.cargando = false;
    });
  }

  volver() {
    this.router.navigate(['/dashboard']);
  }

  getMaxIngresos() {
    return Math.max(1, ...this.ventasUltimos7Dias.map(v => v.ingresos));
  }

  getPorcentajeVenta(ingresos: number) {
    const max = this.getMaxIngresos();
    return (ingresos / max) * 100;
  }
}
