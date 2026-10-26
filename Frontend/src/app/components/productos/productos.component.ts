
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { ProductoService } from '../../services/producto.service';
import { AuthService } from '../../services/auth.service';
import { Producto } from '../../models/producto.model';
import { Usuario } from '../../models/usuario.model';
import { catchError, of } from 'rxjs';

function precioVentaCoherente(group: AbstractControl): ValidationErrors | null {
  const costo = group.get('precioCosto')?.value;
  const venta = group.get('precioVenta')?.value;
  if (costo != null && venta != null && Number(venta) < Number(costo)) {
    return { ventaMenorQueCosto: true };
  }
  return null;
}

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './productos.component.html',
  styleUrls: ['./productos.component.css']
})
export class ProductosComponent implements OnInit {
  productos: Producto[] = [];
  productosFiltered: Producto[] = [];
  cargando = false;
  mensaje = '';
  
  // ===== BÚSQUEDA =====
  terminoBusqueda = '';
  tipoBusqueda: 'nombre' | 'sku' | 'categoria' | 'stock-bajo' = 'nombre';
  buscando = false;

  usuarioActual: Usuario | null = null;
  mostrarFormulario = false;
  productoEditando: Producto | null = null;
  productoForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private productoService: ProductoService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.usuarioActual = this.authService.getUsuarioActual();
    if (!this.usuarioActual) {
      this.router.navigate(['/login']);
      return;
    }
    this.crearFormulario();
    this.cargarProductos();
  }

  private cargarProductos(): void {
    this.cargando = true;
    this.productoService.obtenerTodos().pipe(
      catchError(error => {
        console.error('Error cargando productos:', error);
        this.cargando = false;
        this.mensaje = 'Error al cargar productos desde BD';
        this.cdr.markForCheck();
        return of([]);
      })
    ).subscribe(productos => {
      this.productos = productos;
      this.cargando = false;
      console.log(`✅ Cargados ${productos.length} productos desde BD`);
      this.cdr.markForCheck();
    });
  }

  private crearFormulario(): void {
    this.productoForm = this.fb.group(
      {
        nombreProducto: ['', [Validators.required, Validators.maxLength(150)]],
        sku: ['', [Validators.required, Validators.maxLength(50)]],
        categoria: [''],
        descripcion: [''],
        precioCosto: [0, [Validators.required, Validators.min(0)]],
        precioVenta: [0, [Validators.required, Validators.min(0.01)]],
        margenGananciaPct: [50, [Validators.min(0)]],
        stockTotal: [0, [Validators.required, Validators.min(0)]],
        stockMinimo: [5, [Validators.min(0)]],
        pesoKg: [0, [Validators.min(0)]],
        volumenM3: [0, [Validators.min(0)]],
        imagenUrl: [''],
        proveedor: [''],
        paisOrigen: [''],
        estado: ['activo', Validators.required]
      },
      { validators: precioVentaCoherente }
    );

    // Escuchar cambios en margen y precioCosto para recalcular precioVenta
    this.productoForm.get('margenGananciaPct')?.valueChanges.subscribe(() => {
      this.recalcularPrecioVenta();
    });

    this.productoForm.get('precioCosto')?.valueChanges.subscribe(() => {
      this.recalcularPrecioVenta();
    });
  }

  get f() { return this.productoForm.controls; }

  get totalProductos(): number {
    return this.productos.length;
  }

  get productosStockBajo(): number {
    return this.productos.filter((p) => this.esStockBajo(p)).length;
  }

  get valorInventario(): number {
    return this.productos.reduce((sum, p) => sum + (p.precioCosto * p.stockTotal), 0);
  }

  get margen(): number | null {
    const costo = Number(this.f['precioCosto'].value);
    const venta = Number(this.f['precioVenta'].value);
    if (costo > 0 && venta > 0) {
      return ((venta - costo) / costo) * 100;
    }
    return null;
  }

  calcularMargen(producto: Producto): number {
    if (!producto.precioCosto) return 0;
    return ((producto.precioVenta - producto.precioCosto) / producto.precioCosto) * 100;
  }

  /**
   * Recalcula el precio de venta basado en el margen deseado
   * Fórmula: precioVenta = precioCosto * (1 + margen/100)
   */
  recalcularPrecioVenta(): void {
    const precioCosto = Number(this.f['precioCosto'].value) || 0;
    const margenPct = Number(this.f['margenGananciaPct'].value) || 0;

    if (precioCosto > 0 && margenPct >= 0) {
      const precioVentaSugerido = precioCosto * (1 + margenPct / 100);
      this.f['precioVenta'].setValue(parseFloat(precioVentaSugerido.toFixed(2)), { emitEvent: false });
    }
  }

  esStockBajo(producto: Producto): boolean {
    return (producto.stockTotal || 0) <= (producto.stockMinimo || 5);
  }

  abrirFormulario(): void {
    this.productoEditando = null;
    this.productoForm.reset({
      nombreProducto: '',
      sku: '',
      categoria: '',
      descripcion: '',
      precioCosto: 0,
      precioVenta: 0,
      stockTotal: 0,
      stockMinimo: 5,
      pesoKg: 0,
      volumenM3: 0,
      imagenUrl: '',
      proveedor: '',
      paisOrigen: '',
      estado: 'activo'
    });
    this.mostrarFormulario = true;
  }

  editarProducto(producto: Producto): void {
    this.productoEditando = producto;
    const margenCalculado = this.calcularMargen(producto);
    this.productoForm.reset({
      nombreProducto: producto.nombreProducto,
      sku: producto.sku,
      categoria: producto.categoria ?? '',
      descripcion: producto.descripcion ?? '',
      precioCosto: producto.precioCosto,
      precioVenta: producto.precioVenta,
      margenGananciaPct: margenCalculado,
      stockTotal: producto.stockTotal,
      stockMinimo: producto.stockMinimo ?? 5,
      pesoKg: producto.pesoKg ?? 0,
      volumenM3: producto.volumenM3 ?? 0,
      imagenUrl: producto.imagenUrl ?? '',
      proveedor: producto.proveedor ?? '',
      paisOrigen: producto.paisOrigen ?? '',
      estado: producto.estado ?? 'activo'
    }, { emitEvent: false });
    this.mostrarFormulario = true;
  }

  guardarProducto(): void {
    if (this.productoForm.invalid) {
      this.productoForm.markAllAsTouched();
      return;
    }

    const margen = this.margen ?? undefined;
    const datos: Producto = { ...this.productoForm.value, margenGananciaPct: margen };

    if (this.productoEditando) {
      // UPDATE existente
      const editId = this.productoEditando.idProducto!;
      this.productoService.actualizar(editId, datos).pipe(
        catchError(error => {
          console.error('Error actualizando producto:', error);
          this.mensaje = 'Error al actualizar producto';
          this.cdr.markForCheck();
          return of(null);
        })
      ).subscribe((productoActualizado) => {
        if (productoActualizado) {
          this.productos = this.productos.map((p) =>
            p.idProducto === editId ? productoActualizado : p
          );
          this.mensaje = '✅ Producto actualizado correctamente';
          this.cerrarFormulario();
          this.cdr.markForCheck();
          setTimeout(() => {
            this.mensaje = '';
            this.cdr.markForCheck();
          }, 3000);
        }
      });
    } else {
      // CREATE nuevo
      this.productoService.crear(datos).pipe(
        catchError(error => {
          console.error('Error creando producto:', error);
          this.mensaje = 'Error al crear producto';
          this.cdr.markForCheck();
          return of(null);
        })
      ).subscribe((nuevoProducto) => {
        if (nuevoProducto) {
          this.productos.push(nuevoProducto);
          this.mensaje = '✅ Producto creado correctamente';
          this.cerrarFormulario();
          this.cdr.markForCheck();
          setTimeout(() => {
            this.mensaje = '';
            this.cdr.markForCheck();
          }, 3000);
        }
      });
    }
  }

  eliminarProducto(id: number | undefined): void {
    if (!id) return;
    
    if (confirm('¿Estás seguro de eliminar este producto del inventario?')) {
      this.productoService.eliminar(id).pipe(
        catchError(error => {
          console.error('Error eliminando producto:', error);
          this.mensaje = 'Error al eliminar producto';
          this.cdr.markForCheck();
          return of(void 0);
        })
      ).subscribe(() => {
        this.productos = this.productos.filter((p) => p.idProducto !== id);
        this.mensaje = '✅ Producto eliminado correctamente';
        console.log(`✅ Producto ${id} eliminado`);
        this.cdr.markForCheck();
        setTimeout(() => {
          this.mensaje = '';
          this.cdr.markForCheck();
        }, 3000);
      });
    }
  }

  cerrarFormulario(): void {
    this.mostrarFormulario = false;
    this.productoEditando = null;
  }

  // ===== MÉTODOS DE BÚSQUEDA CON JPQL =====
  
  /**
   * Busca productos según el tipo de búsqueda seleccionado
   * Utiliza JPQL en el backend para consultas eficientes
   */
  buscarProductos(): void {
    if (!this.terminoBusqueda.trim() && this.tipoBusqueda !== 'stock-bajo') {
      this.mensaje = '⚠️ Ingresa un término de búsqueda';
      return;
    }

    this.buscando = true;
    this.mensaje = '';

    switch (this.tipoBusqueda) {
      case 'nombre':
        this.buscarPorNombre();
        break;
      case 'sku':
        this.buscarPorSKU();
        break;
      case 'categoria':
        this.buscarPorCategoria();
        break;
      case 'stock-bajo':
        this.buscarProductosConStockBajo();
        break;
    }
  }

  /**
   * Busca por nombre de producto (case-insensitive en BD)
   */
  private buscarPorNombre(): void {
    this.productoService.buscarPorNombre(this.terminoBusqueda).pipe(
      catchError(error => {
        console.error('Error buscando por nombre:', error);
        this.mensaje = '❌ Error en búsqueda por nombre';
        this.buscando = false;
        return of([]);
      })
    ).subscribe(productos => {
      this.productosFiltered = productos;
      this.mensaje = `✅ Se encontraron ${productos.length} resultado(s)`;
      this.buscando = false;
      this.cdr.markForCheck();
    });
  }

  /**
   * Busca por SKU o nombre (búsqueda general)
   */
  private buscarPorSKU(): void {
    this.productoService.buscarGeneral(this.terminoBusqueda).pipe(
      catchError(error => {
        console.error('Error en búsqueda general:', error);
        this.mensaje = '❌ Error en búsqueda';
        this.buscando = false;
        return of([]);
      })
    ).subscribe(productos => {
      this.productosFiltered = productos;
      this.mensaje = `✅ Se encontraron ${productos.length} resultado(s)`;
      this.buscando = false;
      this.cdr.markForCheck();
    });
  }

  /**
   * Busca por categoría
   */
  private buscarPorCategoria(): void {
    this.productoService.buscarPorCategoria(this.terminoBusqueda).pipe(
      catchError(error => {
        console.error('Error buscando por categoría:', error);
        this.mensaje = '❌ Error en búsqueda por categoría';
        this.buscando = false;
        return of([]);
      })
    ).subscribe(productos => {
      this.productosFiltered = productos;
      this.mensaje = `✅ Se encontraron ${productos.length} producto(s) en esta categoría`;
      this.buscando = false;
      this.cdr.markForCheck();
    });
  }

  /**
   * Obtiene productos con stock por debajo del mínimo
   */
  private buscarProductosConStockBajo(): void {
    this.productoService.obtenerProductosConStockBajo().pipe(
      catchError(error => {
        console.error('Error obteniendo productos con stock bajo:', error);
        this.mensaje = '❌ Error al obtener productos con stock bajo';
        this.buscando = false;
        return of([]);
      })
    ).subscribe(productos => {
      this.productosFiltered = productos;
      this.mensaje = `⚠️ ${productos.length} producto(s) con stock por debajo del mínimo`;
      this.buscando = false;
      this.cdr.markForCheck();
    });
  }

  /**
   * Limpia la búsqueda y muestra todos los productos
   */
  limpiarBusqueda(): void {
    this.terminoBusqueda = '';
    this.productosFiltered = [];
    this.mensaje = '';
    this.cdr.markForCheck();
  }

  /**
   * Maneja errores al cargar imágenes - reemplaza con emoji fallback
   */
  onImageError(event: any): void {
    event.target.style.display = 'none';
    const container = event.target.parentElement;
    if (container) {
      const fallback = document.createElement('div');
      fallback.className = 'product-icon-fallback';
      fallback.textContent = '📦';
      container.appendChild(fallback);
    }
  }

  volver(): void {
    this.router.navigate(['/dashboard']);
  }
}
