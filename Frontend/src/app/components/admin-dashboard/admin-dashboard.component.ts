import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { catchError, finalize, timeout } from 'rxjs/operators';

import { AdminDashboardResumen, AdminDashboardService } from '../../services/admin-dashboard.service';
import { AuthService } from '../../services/auth.service';
import { Usuario } from '../../models/usuario.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  usuarioActual: Usuario | null = null;
  resumen: AdminDashboardResumen = this.crearResumenVacio();
  loading = true;
  error = '';
  modoFallback = false;

  constructor(
    private adminDashboardService: AdminDashboardService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.usuarioActual = this.authService.getUsuarioActual();

    if (!this.usuarioActual) {
      this.loading = false;
      this.router.navigate(['/login']);
      return;
    }

    if (!this.esAdminGlobal()) {
      this.loading = false;
      this.router.navigate(['/dashboard']);
      return;
    }

    this.cargarResumen();
  }

  cargarResumen(): void {
    // No bloquear la pantalla: se muestra el panel inmediatamente con valores base.
    // Si el backend responde, los datos se actualizan; si falla, queda visible y muestra aviso.
    this.loading = false;
    this.error = '';
    this.modoFallback = false;

    this.adminDashboardService.obtenerResumenGlobal()
      .pipe(
        timeout(5000),
        catchError((error: unknown) => {
          console.error('Error cargando dashboard administrativo:', error);
          this.modoFallback = true;
          this.error = 'El endpoint administrativo no respondió. El panel queda visible para no bloquear la exposición. Revisa que el backend tenga /api/admin/dashboard/resumen y reinicia Spring Boot.';
          return of(this.crearResumenVacio());
        }),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe((resumen: AdminDashboardResumen) => {
        this.resumen = this.normalizarResumen(resumen);
      });
  }

  private normalizarResumen(resumen: AdminDashboardResumen | null | undefined): AdminDashboardResumen {
    const vacio = this.crearResumenVacio();
    return {
      ...vacio,
      ...(resumen || {}),
      suscripcionesDetalle: resumen?.suscripcionesDetalle || [],
      topEmprendedores: resumen?.topEmprendedores || [],
      ultimosUsuarios: resumen?.ultimosUsuarios || [],
      ultimasVentas: resumen?.ultimasVentas || []
    };
  }

  private crearResumenVacio(): AdminDashboardResumen {
    return {
      mes: new Date().toISOString().slice(0, 7),
      totalUsuarios: 0,
      usuariosActivos: 0,
      usuariosBloqueados: 0,
      administradores: 0,
      emprendedoresActivos: 0,
      totalProductos: 0,
      totalImportaciones: 0,
      totalVentas: 0,
      ventasHoy: 0,
      ventasBrutasMes: 0,
      ventasBrutasTotales: 0,
      comisionPlataformaPct: 10,
      comisionesMes: 0,
      suscripcionesMes: 0,
      ingresosPlataformaMes: 0,
      utilidadEmprendedoresMes: 0,
      valorInventarioGlobal: 0,
      ticketPromedioGlobal: 0,
      suscripcionesDetalle: [],
      topEmprendedores: [],
      ultimosUsuarios: [],
      ultimasVentas: []
    };
  }

  esAdminGlobal(): boolean {
    return String(this.usuarioActual?.tipoUsuario || '').toUpperCase() === 'ADMINISTRADOR_GLOBAL';
  }

  dinero(valor: number | string | null | undefined): string {
    return `S/. ${Number(valor || 0).toFixed(2)}`;
  }

  porcentaje(valor: number | string | null | undefined): string {
    return `${Number(valor || 0).toFixed(1)}%`;
  }

  navegarA(ruta: string): void {
    this.router.navigate([ruta]);
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
