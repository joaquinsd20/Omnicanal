
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { VentaService } from '../../services/venta.service';
import { ProductoService } from '../../services/producto.service';
import { CanalService } from '../../services/canal.service';
import { Venta } from '../../models/venta.model';
import { Canal } from '../../models/canal.model';
import { Usuario } from '../../models/usuario.model';
import { catchError, of } from 'rxjs';

interface Producto {
  idProducto: number;
  nombreProducto: string;
  precioVenta: number;
  stockTotal: number;
  categoria: string;
  sku: string;
  descripcion?: string;
}

interface ItemVenta {
  idProducto: number;
  nombreProducto: string;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
}

@Component({
  selector: 'app-ventas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './ventas.component.html',
  styleUrls: ['./ventas.component.css']
})
export class VentasComponent implements OnInit {
  ventas: Venta[] = [];
  productos: Producto[] = [];
  productosFiltrados: Producto[] = [];
  categorias: string[] = [];
  canales: Canal[] = [];
  itemsVenta: ItemVenta[] = [];
  cargando = false;
  mensaje = '';
  mostrarFormulario = false;
  ventaForm!: FormGroup;
  usuarioActual: Usuario | null = null;
  productoSeleccionado: number | null = null;
  cantidadSeleccionada: number = 1;
  categoriaSeleccionada: string = '';
  textoBusqueda: string = '';
  mostrarListaProductos: boolean = false;

  constructor(
    private authService: AuthService,
    private ventaService: VentaService,
    private productoService: ProductoService,
    private canalService: CanalService,
    private router: Router,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.usuarioActual = this.authService.getUsuarioActual();
    if (!this.usuarioActual) {
      this.router.navigate(['/login']);
      return;
    }
    this.cargarProductos();
    this.cargarCanales();
    this.cargarVentas();
    this.crearFormulario();
  }

  private cargarCanales(): void {
    if (!this.usuarioActual || !this.usuarioActual.idUsuario) return;
    this.canalService.obtenerCanalesDelUsuario(this.usuarioActual.idUsuario as number).pipe(
      catchError(error => {
        console.error('Error cargando canales:', error);
        return of([]);
      })
    ).subscribe((canales: Canal[]) => {
      this.canales = canales.filter(c => c.estado === 'activo');
      console.log(`✅ Cargados ${this.canales.length} canales`);
      // Si hay canales, seleccionar el primero por defecto
      if (this.canales.length > 0 && this.canales[0].idCanal) {
        this.ventaForm?.patchValue({ idCanal: this.canales[0].idCanal });
      }
      this.cdr.markForCheck();
    });
  }

  private cargarProductos(): void {
    this.productoService.obtenerTodos().pipe(
      catchError(error => {
        console.error('Error cargando productos:', error);
        return of([]);
      })
    ).subscribe((productos: any[]) => {
      this.productos = productos.filter(p => p.stockTotal > 0);
      console.log(`✅ Cargados ${this.productos.length} productos con stock`);
      
      // Extraer categorías únicas y ordenarlas
      const categoriasSet = new Set(this.productos.map(p => p.categoria).filter(c => c));
      this.categorias = Array.from(categoriasSet).sort();
      console.log(`📂 Categorías disponibles: ${this.categorias.join(', ')}`);
      
      // Inicializar lista filtrada
      this.productosFiltrados = this.productos;
      this.cdr.markForCheck();
    });
  }

  private cargarVentas(): void {
    this.cargando = true;
    this.ventaService.obtenerTodas().pipe(
      catchError(error => {
        console.error('Error cargando ventas:', error);
        this.cargando = false;
        this.mensaje = 'Error al cargar ventas desde BD';
        this.cdr.markForCheck();
        return of([]);
      })
    ).subscribe((ventas: Venta[]) => {
      this.ventas = ventas;
      this.cargando = false;
      console.log(`✅ Cargadas ${ventas.length} ventas desde BD`);
      this.cdr.markForCheck();
    });
  }

  private crearFormulario(): void {
    this.ventaForm = this.fb.group({
      idCanal: [null, [Validators.required]],
      nombreCliente: ['', [Validators.required, Validators.minLength(3)]],
      emailCliente: ['', [Validators.email]],
      telefonoCliente: ['', [Validators.pattern(/^$|^\+?[0-9\s]{7,15}$/)]],
      provinciaDestino: ['', [Validators.required]],
      distritoDestino: [''],
      direccionEntrega: [''],
      agenciaEnvio: ['Shalom', [Validators.required]],
      costoEnvio: [0, [Validators.min(0)]],
      descuento: [0, [Validators.min(0)]],
      estadoPago: ['pendiente', [Validators.required]]
    });
  }

  get f() { return this.ventaForm.controls; }

  get ingresoTotal(): number {
    return this.ventas.reduce((sum, v) => sum + (v.total || 0), 0);
  }

  get ventasPendientes(): number {
    return this.ventas.filter((v) => v.estadoVenta === 'pendiente').length;
  }

  get ticketPromedio(): number {
    return this.ventas.length ? this.ingresoTotal / this.ventas.length : 0;
  }

  get subtotalItems(): number {
    return this.itemsVenta.reduce((sum, item) => sum + item.subtotal, 0);
  }

  get totalFormulario(): number {
    const envio = Number(this.ventaForm?.get('costoEnvio')?.value || 0);
    const descuento = Number(this.ventaForm?.get('descuento')?.value || 0);
    return Math.max(this.subtotalItems + envio - descuento, 0);
  }

  filtrarProductos(): void {
    let resultado = this.productos;

    // Filtro 1: Por categoría
    if (this.categoriaSeleccionada) {
      resultado = resultado.filter(p => p.categoria === this.categoriaSeleccionada);
    }

    // Filtro 2: Por búsqueda (nombre o SKU)
    if (this.textoBusqueda.trim()) {
      const busca = this.textoBusqueda.toLowerCase();
      resultado = resultado.filter(p =>
        p.nombreProducto.toLowerCase().includes(busca) ||
        p.sku.toLowerCase().includes(busca)
      );
    }

    this.productosFiltrados = resultado;
    this.mostrarListaProductos = resultado.length > 0;
    this.cdr.markForCheck();
  }

  seleccionarProducto(producto: Producto): void {
    this.productoSeleccionado = producto.idProducto;
    this.textoBusqueda = ''; // Limpia búsqueda
    this.mostrarListaProductos = false;
    this.cdr.markForCheck();
  }

  cambiarCategoria(categoria: string): void {
    this.categoriaSeleccionada = categoria;
    this.textoBusqueda = '';
    this.productoSeleccionado = null;
    this.mostrarListaProductos = false;
    this.filtrarProductos();
  }

  agregarProductoAVenta(): void {
    if (!this.productoSeleccionado || this.cantidadSeleccionada < 1) {
      this.mensaje = '⚠️ Selecciona producto y cantidad';
      this.cdr.markForCheck();
      return;
    }

    const producto = this.productos.find(p => p.idProducto === this.productoSeleccionado);
    if (!producto) return;

    if (this.cantidadSeleccionada > producto.stockTotal) {
      this.mensaje = `⚠️ Stock insuficiente. Disponible: ${producto.stockTotal}`;
      this.cdr.markForCheck();
      return;
    }

    const itemExistente = this.itemsVenta.find(i => i.idProducto === this.productoSeleccionado);
    if (itemExistente) {
      itemExistente.cantidad += this.cantidadSeleccionada;
      itemExistente.subtotal = itemExistente.cantidad * itemExistente.precioUnitario;
    } else {
      this.itemsVenta.push({
        idProducto: producto.idProducto,
        nombreProducto: producto.nombreProducto,
        precioUnitario: producto.precioVenta,
        cantidad: this.cantidadSeleccionada,
        subtotal: producto.precioVenta * this.cantidadSeleccionada
      });
    }

    this.productoSeleccionado = null;
    this.cantidadSeleccionada = 1;
    this.mensaje = '✅ Producto agregado';
    this.cdr.markForCheck();
    setTimeout(() => { this.mensaje = ''; }, 2000);
  }

  removerProducto(index: number): void {
    this.itemsVenta.splice(index, 1);
    this.cdr.markForCheck();
  }

  abrirFormulario(): void {
    this.itemsVenta = [];
    this.productoSeleccionado = null;
    this.cantidadSeleccionada = 1;
    this.categoriaSeleccionada = '';
    this.textoBusqueda = '';
    this.productosFiltrados = this.productos;
    this.mostrarListaProductos = false;
    const defaultCanal = this.canales.length > 0 && this.canales[0].idCanal ? this.canales[0].idCanal : 1;
    this.ventaForm.reset({
      idCanal: defaultCanal,
      nombreCliente: '',
      emailCliente: '',
      telefonoCliente: '',
      provinciaDestino: '',
      distritoDestino: '',
      direccionEntrega: '',
      agenciaEnvio: 'Shalom',
      costoEnvio: 0,
      descuento: 0,
      estadoPago: 'pendiente'
    });
    this.mostrarFormulario = true;
  }

  guardarVenta(): void {
    if (!this.itemsVenta.length) {
      this.mensaje = '⚠️ Debes agregar al menos 1 producto';
      this.cdr.markForCheck();
      return;
    }

    if (this.ventaForm.invalid) {
      this.ventaForm.markAllAsTouched();
      return;
    }

    if (!this.usuarioActual) {
      this.mensaje = 'Error: Usuario no autenticado';
      this.cdr.markForCheck();
      return;
    }

    const idCanalValue = this.ventaForm.get('idCanal')?.value;
    const nuevaVenta: any = {
      idCanal: idCanalValue || 1,
      nombreCliente: this.ventaForm.get('nombreCliente')?.value,
      emailCliente: this.ventaForm.get('emailCliente')?.value || null,
      telefonoCliente: this.ventaForm.get('telefonoCliente')?.value,
      provinciaDestino: this.ventaForm.get('provinciaDestino')?.value,
      distritoDestino: this.ventaForm.get('distritoDestino')?.value || null,
      direccionEntrega: this.ventaForm.get('direccionEntrega')?.value || null,
      agenciaEnvio: this.ventaForm.get('agenciaEnvio')?.value,
      // numeroVenta se genera automáticamente en el servidor (no enviar desde cliente)
      subtotal: this.subtotalItems,
      costoEnvio: Number(this.ventaForm.get('costoEnvio')?.value || 0),
      descuento: Number(this.ventaForm.get('descuento')?.value || 0),
      total: this.totalFormulario,
      estadoVenta: 'pendiente',
      estadoPago: this.ventaForm.get('estadoPago')?.value || 'pendiente',
      fechaVenta: new Date().toISOString(),
      detalles: this.itemsVenta.map(item => ({
        idProducto: item.idProducto,
        cantidad: item.cantidad,
        precioUnitario: item.precioUnitario,
        subtotal: item.subtotal
      })),
      usuario: {
        idUsuario: this.usuarioActual?.idUsuario || 0
      }
    };

    this.ventaService.crear(nuevaVenta).pipe(
      catchError(error => {
        console.error('Error creando venta:', error);
        this.mensaje = `Error al crear venta: ${error.message}`;
        this.cdr.markForCheck();
        return of(null);
      })
    ).subscribe((ventaCreada) => {
      if (ventaCreada) {
        this.ventas = [ventaCreada, ...this.ventas];
        this.mensaje = '✅ Venta registrada y stock descontado';
        this.cargarProductos();
        this.cerrarFormulario();
        this.cdr.markForCheck();
        setTimeout(() => {
          this.mensaje = '';
          this.cdr.markForCheck();
        }, 3000);
      }
    });
  }

  cambiarEstado(idVenta: number | undefined, nuevoEstado: string): void {
    if (!idVenta) return;
    
    const venta = this.ventas.find(v => v.idVenta === idVenta);
    if (!venta) return;

    const ventaActualizada = { ...venta, estadoVenta: nuevoEstado };
    
    this.ventaService.actualizar(idVenta, ventaActualizada).pipe(
      catchError(error => {
        console.error('Error actualizando estado de venta:', error);
        this.mensaje = 'Error al actualizar estado';
        this.cdr.markForCheck();
        return of(null);
      })
    ).subscribe(() => {
      const index = this.ventas.findIndex(v => v.idVenta === idVenta);
      if (index > -1) {
        this.ventas[index].estadoVenta = nuevoEstado;
      }
      this.mensaje = '✅ Estado actualizado correctamente';
      this.cdr.markForCheck();
      setTimeout(() => {
        this.mensaje = '';
        this.cdr.markForCheck();
      }, 3000);
    });
  }

  cerrarFormulario(): void {
    this.mostrarFormulario = false;
  }

  volver(): void {
    this.router.navigate(['/dashboard']);
  }
}
