import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ImportacionService, CrearImportacionRequest, ImportacionResponse } from '../../services/importacion.service';
import { ProductoService } from '../../services/producto.service';
import { AuthService } from '../../services/auth.service';
import { Producto } from '../../models/producto.model';

@Component({
  selector: 'app-importacion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './importacion.component.html',
  styleUrls: ['./importacion.component.css']
})
export class ImportacionComponent implements OnInit {
  importaciones: ImportacionResponse[] = [];
  productos: Producto[] = [];
  cargando = false;
  mensaje = '';
  
  usuarioActual: any = null;
  mostrarFormulario = false;
  importacionForm!: FormGroup;
  
  // Items seleccionados para la importación
  itemsImportacion: any[] = [];
  productoSeleccionado: number | null = null;
  cantidadItem = 1;
  precioUnitarioItem = 0;

  // Para visualizar el prorrateo
  importacionGuardada: ImportacionResponse | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private importacionService: ImportacionService,
    private productoService: ProductoService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.inicializarFormulario();
  }

  ngOnInit(): void {
    this.usuarioActual = this.authService.getUsuarioActual();
    if (!this.usuarioActual) {
      this.router.navigate(['/login']);
      return;
    }
    this.cargarImportaciones();
    this.cargarProductos();
  }

  inicializarFormulario(): void {
    this.importacionForm = this.fb.group({
      numeroFactura: ['', [Validators.required, Validators.minLength(3)]],
      fechaFactura: ['', Validators.required],
      proveedorInternacional: ['', Validators.required],
      paisOrigen: ['China', Validators.required],
      montoOriginal: [0, [Validators.required, Validators.min(0.01)]],
      monedaOriginal: ['USD', Validators.required],
      tipoCambio: [3.85, [Validators.required, Validators.min(0.01)]],
      costoFlete: [0, [Validators.required, Validators.min(0)]],
      costoAduanas: [0, [Validators.required, Validators.min(0)]],
      costoAranceles: [0, [Validators.required, Validators.min(0)]],
      costoTransporteLocal: [0, [Validators.min(0)]],
      otrosCostos: [0, [Validators.min(0)]],
      pesoTotalKg: [0, [Validators.required, Validators.min(0.01)]],
      volumenTotalM3: [0, [Validators.required, Validators.min(0.001)]],
      fechaLlegadaEstimada: ['', Validators.required]
    });
  }

  get f() {
    return this.importacionForm.controls;
  }

  cargarProductos(): void {
    if (!this.usuarioActual?.idUsuario) return;

    this.productoService.obtenerTodos().subscribe(
      (productos: Producto[]) => {
        this.productos = productos || [];
        this.cdr.markForCheck();
      },
      (error: any) => {
        this.mensaje = '❌ Error al cargar productos';
        console.error(error);
      }
    );
  }

  cargarImportaciones(): void {
    if (!this.usuarioActual?.idUsuario) return;

    this.cargando = true;
    this.importacionService.obtenerImportaciones(this.usuarioActual.idUsuario).subscribe(
      (importaciones: ImportacionResponse[]) => {
        this.importaciones = importaciones || [];
        this.cargando = false;
        this.cdr.markForCheck();
      },
      (error: any) => {
        this.mensaje = '❌ Error al cargar importaciones';
        console.error(error);
        this.cargando = false;
      }
    );
  }

  abrirFormulario(): void {
    this.mostrarFormulario = true;
    this.itemsImportacion = [];
    this.importacionGuardada = null;
    this.importacionForm.reset({
      numeroFactura: '',
      fechaFactura: '',
      proveedorInternacional: '',
      paisOrigen: 'China',
      montoOriginal: 0,
      monedaOriginal: 'USD',
      tipoCambio: 3.85,
      costoFlete: 0,
      costoAduanas: 0,
      costoAranceles: 0,
      costoTransporteLocal: 0,
      otrosCostos: 0,
      pesoTotalKg: 0,
      volumenTotalM3: 0,
      fechaLlegadaEstimada: ''
    });
  }

  cerrarFormulario(): void {
    this.mostrarFormulario = false;
    this.itemsImportacion = [];
    this.importacionGuardada = null;
  }

  agregarItem(): void {
    if (!this.productoSeleccionado || this.cantidadItem <= 0 || this.precioUnitarioItem <= 0) {
      this.mensaje = '⚠️ Selecciona producto, cantidad y precio válidos';
      return;
    }

    const producto = this.productos.find(p => p.idProducto === this.productoSeleccionado);
    if (!producto) {
      this.mensaje = '❌ Producto no encontrado';
      return;
    }

    this.itemsImportacion.push({
      idProducto: this.productoSeleccionado,
      nombreProducto: producto.nombreProducto,
      cantidad: this.cantidadItem,
      precioUnitarioOriginal: this.precioUnitarioItem
    });

    this.mensaje = `✅ ${producto.nombreProducto} agregado`;
    this.productoSeleccionado = null;
    this.cantidadItem = 1;
    this.precioUnitarioItem = 0;
    this.cdr.markForCheck();

    setTimeout(() => { this.mensaje = ''; }, 2000);
  }

  eliminarItem(index: number): void {
    this.itemsImportacion.splice(index, 1);
    this.cdr.markForCheck();
  }

  guardarImportacion(): void {
    if (this.importacionForm.invalid) {
      this.mensaje = '⚠️ Completa todos los campos requeridos';
      return;
    }

    if (this.itemsImportacion.length === 0) {
      this.mensaje = '⚠️ Agrega al menos un producto';
      return;
    }

    const formValue = this.importacionForm.value;
    const request: CrearImportacionRequest = {
      numeroFactura: formValue.numeroFactura,
      fechaFactura: formValue.fechaFactura,
      proveedorInternacional: formValue.proveedorInternacional,
      paisOrigen: formValue.paisOrigen,
      montoOriginal: formValue.montoOriginal,
      monedaOriginal: formValue.monedaOriginal,
      tipoCambio: formValue.tipoCambio,
      costoFlete: formValue.costoFlete,
      costoAduanas: formValue.costoAduanas,
      costoAranceles: formValue.costoAranceles,
      costoTransporteLocal: formValue.costoTransporteLocal,
      otrosCostos: formValue.otrosCostos,
      cantidadItems: this.itemsImportacion.reduce((sum, item) => sum + item.cantidad, 0),
      pesoTotalKg: formValue.pesoTotalKg,
      volumenTotalM3: formValue.volumenTotalM3,
      fechaLlegadaEstimada: formValue.fechaLlegadaEstimada,
      items: this.itemsImportacion.map(item => ({
        idProducto: item.idProducto,
        cantidad: item.cantidad,
        precioUnitarioOriginal: item.precioUnitarioOriginal
      }))
    };

    this.cargando = true;
    this.importacionService.crearImportacion(request, this.usuarioActual.idUsuario).subscribe(
      (respuesta: ImportacionResponse) => {
        this.importacionGuardada = respuesta;
        this.mensaje = '✅ Importación guardada correctamente';
        this.cargando = false;
        this.cdr.markForCheck();

        setTimeout(() => {
          this.cargarImportaciones();
          this.cerrarFormulario();
        }, 2000);
      },
      (error: any) => {
        this.mensaje = `❌ Error: ${error.error?.message || 'No se pudo guardar'}`;
        this.cargando = false;
        this.cdr.markForCheck();
      }
    );
  }

  calcularMontoTotalInvertido(): number {
    return this.importaciones.reduce((sum, imp) => sum + (imp.montoEnSoles || 0), 0);
  }

  calcularCostosTotalAdicionales(): number {
    return this.importaciones.reduce((sum, imp) => sum + (imp.costoTotal || 0), 0);
  }

  obtenerDetalles(idImportacion: number): void {
    this.importacionService.obtenerImportacion(idImportacion).subscribe(
      (respuesta: ImportacionResponse) => {
        this.importacionGuardada = respuesta;
        this.mostrarFormulario = true;
        this.cdr.markForCheck();
      },
      (error: any) => {
        this.mensaje = `❌ Error al obtener detalles: ${error.error?.message || 'Error'}`;
      }
    );
  }

  calcularMontoEnSoles(): number {
    const monto = this.f['montoOriginal'].value || 0;
    const cambio = this.f['tipoCambio'].value || 1;
    return monto * cambio;
  }

  calcularCostoTotal(): number {
    return (this.f['costoFlete'].value || 0) +
           (this.f['costoAduanas'].value || 0) +
           (this.f['costoAranceles'].value || 0) +
           (this.f['costoTransporteLocal'].value || 0) +
           (this.f['otrosCostos'].value || 0);
  }

  calcularCostoUnitarioPorItem(item: any): number {
    if (!this.importacionGuardada) return 0;
    const prorrateo = this.importacionGuardada.prorrateos?.find(p => p.idProducto === item.idProducto);
    return prorrateo?.costoUnitarioFinal || item.precioUnitarioOriginal;
  }

  volver(): void {
    this.router.navigate(['/dashboard']);
  }
}
