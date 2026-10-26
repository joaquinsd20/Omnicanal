import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { ImportacionService, CrearImportacionRequest, ImportacionResponse, ProrrateoResponse } from '../../services/importacion.service';
import { ProductoService } from '../../services/producto.service';
import { AuthService } from '../../services/auth.service';
import { Producto } from '../../models/producto.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-importar-productos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './importar-productos.component.html',
  styleUrls: ['./importar-productos.component.css']
})
export class ImportarProductosComponent implements OnInit {
  
  importacionForm!: FormGroup;
  productos: Producto[] = [];
  prorrateoResultado: ProrrateoResponse[] = [];
  mostrarResultado = false;
  cargando = false;
  error: string | null = null;
  exito: string | null = null;
  parseFloat = parseFloat; // Disponible en el template
  usuarioActual: any = null;

  constructor(
    private fb: FormBuilder,
    private importacionService: ImportacionService,
    private productoService: ProductoService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.usuarioActual = this.authService.getUsuarioActual();
    if (!this.usuarioActual) {
      this.router.navigate(['/login']);
      return;
    }
    this.initForm();
    this.cargarProductos();
  }

  initForm(): void {
    this.importacionForm = this.fb.group({
      numeroFactura: ['', [Validators.required, Validators.minLength(3)]],
      fechaFactura: ['', Validators.required],
      proveedorInternacional: ['', [Validators.required, Validators.minLength(3)]],
      paisOrigen: ['', Validators.required],
      montoOriginal: ['', [Validators.required, Validators.min(0.01)]],
      monedaOriginal: ['USD', Validators.required],
      tipoCambio: ['3.85', [Validators.required, Validators.min(0.01)]],
      costoFlete: ['', [Validators.required, Validators.min(0)]],
      costoAduanas: ['', [Validators.required, Validators.min(0)]],
      costoAranceles: ['', [Validators.required, Validators.min(0)]],
      costoTransporteLocal: ['', Validators.min(0)],
      otrosCostos: ['', Validators.min(0)],
      cantidadItems: ['', [Validators.required, Validators.min(1)]],
      pesoTotalKg: [''],
      volumenTotalM3: [''],
      fechaLlegadaEstimada: [''],
      items: this.fb.array([])
    });

    // Agregar un item vacío por defecto
    this.agregarItem();
  }

  cargarProductos(): void {
    this.productoService.obtenerTodos().subscribe({
      next: (productos: Producto[]) => {
        this.productos = productos;
      },
      error: (err: any) => {
        console.error('Error al cargar productos:', err);
        this.error = 'No se pudieron cargar los productos';
      }
    });
  }

  get items(): FormArray {
    return this.importacionForm.get('items') as FormArray;
  }

  agregarItem(): void {
    const itemForm = this.fb.group({
      idProducto: ['', Validators.required],
      cantidad: ['', [Validators.required, Validators.min(1)]],
      precioUnitarioOriginal: ['', [Validators.required, Validators.min(0.01)]]
    });
    this.items.push(itemForm);
  }

  eliminarItem(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
    }
  }

  enviarImportacion(): void {
    if (this.importacionForm.invalid) {
      this.error = 'Por favor, completa todos los campos obligatorios';
      return;
    }

    this.cargando = true;
    this.error = null;
    this.exito = null;

    // Actualizar cantidadItems a la suma real de cantidades en items
    const cantidadTotal = this.items.value.reduce((sum: number, item: any) => sum + (parseInt(item.cantidad) || 0), 0);

    const request: CrearImportacionRequest = {
      numeroFactura: this.importacionForm.get('numeroFactura')?.value,
      fechaFactura: this.importacionForm.get('fechaFactura')?.value,
      proveedorInternacional: this.importacionForm.get('proveedorInternacional')?.value,
      paisOrigen: this.importacionForm.get('paisOrigen')?.value,
      montoOriginal: parseFloat(this.importacionForm.get('montoOriginal')?.value),
      monedaOriginal: this.importacionForm.get('monedaOriginal')?.value,
      tipoCambio: parseFloat(this.importacionForm.get('tipoCambio')?.value),
      costoFlete: parseFloat(this.importacionForm.get('costoFlete')?.value),
      costoAduanas: parseFloat(this.importacionForm.get('costoAduanas')?.value),
      costoAranceles: parseFloat(this.importacionForm.get('costoAranceles')?.value),
      costoTransporteLocal: this.importacionForm.get('costoTransporteLocal')?.value ? 
        parseFloat(this.importacionForm.get('costoTransporteLocal')?.value) : 0,
      otrosCostos: this.importacionForm.get('otrosCostos')?.value ? 
        parseFloat(this.importacionForm.get('otrosCostos')?.value) : 0,
      cantidadItems: cantidadTotal,
      pesoTotalKg: this.importacionForm.get('pesoTotalKg')?.value ? 
        parseFloat(this.importacionForm.get('pesoTotalKg')?.value) : undefined,
      volumenTotalM3: this.importacionForm.get('volumenTotalM3')?.value ? 
        parseFloat(this.importacionForm.get('volumenTotalM3')?.value) : undefined,
      fechaLlegadaEstimada: this.importacionForm.get('fechaLlegadaEstimada')?.value,
      items: this.items.value
    };

    this.importacionService.crearImportacion(request, this.usuarioActual.idUsuario).subscribe({
      next: (response: ImportacionResponse) => {
        this.prorrateoResultado = response.prorrateos;
        this.mostrarResultado = true;
        this.exito = `✅ Importación creada exitosamente. Total: S/ ${response.montoEnSoles}`;
        this.cargando = false;
        
        // Limpiar formulario después de 2 segundos
        setTimeout(() => {
          this.importacionForm.reset();
          this.items.clear();
          this.agregarItem();
          this.mostrarResultado = false;
        }, 2000);
      },
      error: (err: any) => {
        this.cargando = false;
        console.error('Error al crear importación:', err);
        this.error = err.error?.message || 'Error al crear la importación';
      }
    });
  }

  getNombreProducto(idProducto: number): string {
    const producto = this.productos.find(p => p.idProducto === idProducto);
    return producto?.nombreProducto || '';
  }

  calcularMontoEnSoles(): number {
    const monto = parseFloat(this.importacionForm.get('montoOriginal')?.value || '0');
    const tipoCambio = parseFloat(this.importacionForm.get('tipoCambio')?.value || '1');
    return monto * tipoCambio;
  }

  retroceder(): void {
    this.router.navigate(['/dashboard']);
  }
}
