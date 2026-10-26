import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { VentaService } from '../../services/venta.service';
import { ProductoService } from '../../services/producto.service';
import { ImportacionService } from '../../services/importacion.service';
import { Usuario } from '../../models/usuario.model';
import { Venta } from '../../models/venta.model';
import { Producto } from '../../models/producto.model';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule],
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
        if (!this.usuarioActual) return;

        // Datos mock para KPIs
        this.ventasHoy = 4;
        this.totalIngresos = 1250.50;
        this.totalCostos = 680.30;
        this.gananciaNeta = this.totalIngresos - this.totalCostos;

        this.ventasRecientes = [
            {
                idVenta: 1,
                numeroVenta: 'V-2026-001',
                nombreCliente: 'Juan Pérez',
                subtotal: 381.36,
                total: 450.00,
                estadoVenta: 'entregada',
                estadoPago: 'pagado',
                fechaVenta: new Date()
            },
            {
                idVenta: 2,
                numeroVenta: 'V-2026-002',
                nombreCliente: 'María García',
                subtotal: 271.61,
                total: 320.50,
                estadoVenta: 'enviada',
                estadoPago: 'pagado',
                fechaVenta: new Date()
            },
            {
                idVenta: 3,
                numeroVenta: 'V-2026-003',
                nombreCliente: 'Carlos López',
                subtotal: 406.78,
                total: 480.00,
                estadoVenta: 'pendiente',
                estadoPago: 'pendiente',
                fechaVenta: new Date()
            }
        ];

        this.productosStockBajo = [
            {
                idProducto: 1,
                nombreProducto: 'Smartphone X200',
                sku: 'SKU-PHONE-001',
                stockTotal: 3,
                precioCosto: 150,
                precioVenta: 300
            },
            {
                idProducto: 2,
                nombreProducto: 'Auriculares Inalámbricos',
                sku: 'SKU-AUDIO-001',
                stockTotal: 8,
                precioCosto: 25,
                precioVenta: 70
            }
        ];

        // Mostrar dashboard rápido (dejar de cargar)
        this.loading = false;

        // Cargar datos del backend después
        const idUsuario = this.usuarioActual?.idUsuario;
        
        if (idUsuario) {
            // Cargar importaciones
            this.importacionService.obtenerImportaciones(idUsuario).subscribe({
                next: (importaciones) => {
                    console.log('Importaciones cargadas:', importaciones);
                    this.importacionesRecientes = importaciones.slice(0, 3);
                },
                error: (error) => {
                    console.error('Error importaciones:', error);
                    this.importacionesRecientes = [];
                }
            });

            // Cargar kardex
            this.importacionService.obtenerKardex(idUsuario).subscribe({
                next: (movimientos) => {
                    console.log('Kardex cargado:', movimientos);
                    this.movimientosRecientes = movimientos.slice(0, 5);
                },
                error: (error) => {
                    console.error('Error kardex:', error);
                    this.movimientosRecientes = [];
                }
            });

            // Cargar productos con stock bajo desde backend
            this.productoService.obtenerProductosConStockBajo().subscribe({
                next: (productos) => {
                    console.log('Productos con stock bajo:', productos);
                    this.productosStockBajo = productos.slice(0, 5);
                },
                error: (error) => {
                    console.error('Error stock bajo:', error);
                    this.productosStockBajo = [];
                }
            });

            // Cargar ventas por canal
            this.ventaService.obtenerVentasPorCanal().subscribe({
                next: (ventasPorCanal) => {
                    console.log('Ventas por canal:', ventasPorCanal);
                    this.ventasPorCanal = ventasPorCanal;
                },
                error: (error) => {
                    console.error('Error ventas por canal:', error);
                    this.ventasPorCanal = [];
                }
            });
        }
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