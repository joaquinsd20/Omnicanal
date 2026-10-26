import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-financiero',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './financiero.component.html',
  styleUrls: ['./financiero.component.css']
})
export class FinancieroComponent implements OnInit {
  // Datos financieros de ejemplo
  dashboardFinanciero = {
    totalIngresos: 5240.50,
    totalCostos: 2850.30,
    gananciaNeta: 2390.20,
    margenNeto: 45.6,
    ventasDelDia: 12,
    ticketPromedio: 436.70,
    valorInventario: 8500.00,
    productosVendidos: 47
  };

  resumenDiario = {
    ingresos: 1250.50,
    costos: 680.30,
    ganancia: 570.20,
    ventasHoy: 4
  };

  ventasUltimos7Dias = [
    { fecha: 'Lunes', ingresos: 450 },
    { fecha: 'Martes', ingresos: 620 },
    { fecha: 'Miércoles', ingresos: 380 },
    { fecha: 'Jueves', ingresos: 910 },
    { fecha: 'Viernes', ingresos: 1250 },
    { fecha: 'Sábado', ingresos: 780 },
    { fecha: 'Hoy', ingresos: 850 }
  ];

  topProductos = [
    { nombre: 'Smartphone X200', vendidos: 12, ingresos: 3600 },
    { nombre: 'Auriculares Inalámbricos', vendidos: 25, ingresos: 1750 },
    { nombre: 'Mochila Impermeable', vendidos: 10, ingresos: 600 }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    if (!this.authService.getUsuarioActual()) {
      this.router.navigate(['/login']);
    }
  }

  volver() {
    this.router.navigate(['/dashboard']);
  }

  getMaxIngresos() {
    return Math.max(...this.ventasUltimos7Dias.map(v => v.ingresos));
  }

  getPorcentajeVenta(ingresos: number) {
    const max = this.getMaxIngresos();
    return (ingresos / max) * 100;
  }
}
