import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { VentaService } from '../../services/venta.service';
import { ProductoService } from '../../services/producto.service';
import { ImportacionService } from '../../services/importacion.service';
import { DashboardService, ResumenDiario } from '../../services/dashboard.service';
import { Usuario } from '../../models/usuario.model';
import { Venta } from '../../models/venta.model';
import { Producto } from '../../models/producto.model';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { IconComponent } from '../icon/icon.component';
import { CountUpDirective } from '../../directives/count-up.directive';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [IconComponent, CountUpDirective, CommonModule],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
    usuarioActual: Usuario | null = null;
    ventasHoy: number = 0;
    ventasRecientes: Venta[] = [];
    productosStockBajo: Producto[] = [];
    importacionesRecientes: any[] = [];
    movimientosRecientes: any[] = [];
    ventasPorCanal: any[] = [];
    totalIngresos: number = 0;
    totalCostos: number = 0;
    gananciaNeta: number = 0;
    menuAbierto: boolean = false;
    loading: boolean = true;

    constructor(
        private authService: AuthService,
        private ventaService: VentaService,
        private productoService: ProductoService,
        private importacionService: ImportacionService,
        private dashboardService: DashboardService,
        private router: Router
    ) { }

    ngOnInit() {
        this.usuarioActual = this.authService.getUsuarioActual();
        if (!this.usuarioActual) {
            this.router.navigate(['/login']);
            return;
        }

        this.cargarDatos();
    }

    cargarDatos() {
        const idUsuario = this.usuarioActual?.idUsuario;
        if (!idUsuario) return;

        forkJoin({
            diario: this.dashboardService.obtenerResumenDiario().pipe(catchError(() => of(null as ResumenDiario | null))),
            ventas: this.ventaService.obtenerPorUsuario(idUsuario).pipe(catchError(() => of([] as Venta[]))),
            stockBajo: this.productoService.obtenerProductosConStockBajo().pipe(catchError(() => of([] as Producto[]))),
            importaciones: this.importacionService.obtenerImportaciones(idUsuario).pipe(catchError(() => of([] as any[]))),
            kardex: this.importacionService.obtenerKardex(idUsuario).pipe(catchError(() => of([] as any[]))),
            canales: this.ventaService.obtenerVentasPorCanal().pipe(catchError(() => of([] as any[])))
        }).subscribe(({ diario, ventas, stockBajo, importaciones, kardex, canales }) => {
            // Indicadores del día (calculados en el backend)
            this.ventasHoy = diario?.ventasHoy ?? 0;
            this.totalIngresos = diario?.ingresos ?? 0;
            this.totalCostos = diario?.costos ?? 0;
            this.gananciaNeta = diario?.ganancia ?? 0;

            // Listas: lo más reciente primero
            this.ventasRecientes = [...ventas]
                .sort((x, y) => new Date(y.fechaVenta ?? 0).getTime() - new Date(x.fechaVenta ?? 0).getTime())
                .slice(0, 5);
            this.productosStockBajo = stockBajo.slice(0, 5);
            this.importacionesRecientes = importaciones.slice(0, 3);
            this.movimientosRecientes = kardex.slice(0, 5);
            this.ventasPorCanal = canales;
            this.loading = false;
        });
    }

    navegarA(ruta: string) {
        this.router.navigate([ruta]);
    }

    cerrarSesion() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }

    toggleMenu() {
        this.menuAbierto = !this.menuAbierto;
    }
}