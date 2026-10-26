import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SunatTestService, SunatValidationRequest, SunatValidationResponse } from '../../services/sunat-test.service';

@Component({
  selector: 'app-sunat-test',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './sunat-test.component.html',
  styleUrls: ['./sunat-test.component.css']
})
export class SunatTestComponent implements OnInit {
  
  // Formulario para credenciales
  credencialesForm: FormGroup;
  
  // Formulario para validar comprobante
  comprobanteForm: FormGroup;

  // Estados
  token: string = '';
  tokenExpiration: number = 0;
  loadingToken: boolean = false;
  loadingValidation: boolean = false;
  
  // Respuestas
  tokenResponse: any = null;
  validationResponse: SunatValidationResponse | null = null;
  
  // Mensajes de error
  tokenError: string = '';
  validationError: string = '';

  // Tipos de comprobante
  tiposComprobante = [
    { code: '01', label: 'FACTURA' },
    { code: '03', label: 'BOLETA DE VENTA' }
  ];

  constructor(
    private fb: FormBuilder,
    private sunatService: SunatTestService
  ) {
    // Inicializar formularios en el constructor
    this.credencialesForm = this.fb.group({
      clientId: ['', [Validators.required, Validators.minLength(10)]],
      clientSecret: ['', [Validators.required, Validators.minLength(10)]]
    });

    this.comprobanteForm = this.fb.group({
      rucConsultante: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
      numRuc: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
      codComp: ['01', Validators.required],
      numeroSerie: ['F001', [Validators.required, Validators.minLength(1)]],
      numero: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      fechaEmision: ['', Validators.required],
      monto: ['', [Validators.required, Validators.pattern(/^\d+(\.\d{2})?$/)]]
    });
  }

  ngOnInit(): void {
    this.loadCredentialsFromStorage();
  }

  /**
   * Guardar credenciales en localStorage
   */
  saveCredentials(): void {
    if (this.credencialesForm.valid) {
      const creds = this.credencialesForm.value;
      localStorage.setItem('sunat_credentials', JSON.stringify(creds));
      alert('✅ Credenciales guardadas localmente');
    }
  }

  /**
   * Cargar credenciales desde localStorage
   */
  loadCredentialsFromStorage(): void {
    const stored = localStorage.getItem('sunat_credentials');
    if (stored) {
      try {
        const creds = JSON.parse(stored);
        this.credencialesForm.patchValue(creds);
      } catch (e) {
        console.error('Error cargando credenciales:', e);
      }
    }
  }

  /**
   * Paso 1: Generar Token
   */
  generarToken(): void {
    if (!this.credencialesForm.valid) {
      this.tokenError = '❌ Completa todos los campos de credenciales';
      return;
    }

    this.loadingToken = true;
    this.tokenError = '';
    this.tokenResponse = null;

    const { clientId, clientSecret } = this.credencialesForm.value;

    this.sunatService.generateToken(clientId, clientSecret)
      .subscribe({
        next: (response) => {
          this.token = response.access_token;
          this.tokenExpiration = response.expires_in;
          this.tokenResponse = response;
          this.loadingToken = false;
          console.log('✅ Token generado:', response);
        },
        error: (err) => {
          this.tokenError = `❌ ${err.message || 'Error al generar token'}`;
          this.loadingToken = false;
          console.error(err);
        }
      });
  }

  /**
   * Paso 2: Validar Comprobante
   */
  validarComprobante(): void {
    if (!this.comprobanteForm.valid) {
      this.validationError = '❌ Completa todos los campos del comprobante';
      return;
    }

    if (!this.token) {
      this.validationError = '❌ Primero debes generar un token válido';
      return;
    }

    this.loadingValidation = true;
    this.validationError = '';
    this.validationResponse = null;

    const formValue = this.comprobanteForm.value;
    const request: SunatValidationRequest = {
      numRuc: formValue.numRuc,
      codComp: formValue.codComp,
      numeroSerie: formValue.numeroSerie,
      numero: parseInt(formValue.numero),
      fechaEmision: formValue.fechaEmision,
      monto: formValue.monto
    };

    this.sunatService.validateComprobante(formValue.rucConsultante, this.token, request)
      .subscribe({
        next: (response) => {
          this.validationResponse = response;
          this.loadingValidation = false;
          console.log('✅ Validación completada:', response);
        },
        error: (err) => {
          this.validationError = `❌ ${err.message || 'Error al validar comprobante'}`;
          this.loadingValidation = false;
          console.error(err);
        }
      });
  }

  /**
   * Obtener descripción del estado del comprobante
   */
  getEstadoDescripcion(codigo: number): string {
    return this.sunatService.getEstadoComprobanteDescripcion(codigo);
  }

  /**
   * Obtener descripción del estado del RUC
   */
  getEstadoRucDescripcion(codigo: string): string {
    return this.sunatService.getEstadoRucDescripcion(codigo);
  }

  /**
   * Obtener descripción de condición de domicilio
   */
  getCondDomiDescripcion(codigo: string): string {
    return this.sunatService.getCondDomiRucDescripcion(codigo);
  }

  /**
   * Limpiar formularios
   */
  limpiar(): void {
    this.comprobanteForm.reset();
    this.validationResponse = null;
    this.validationError = '';
  }

  /**
   * Usar datos de ejemplo (para prueba rápida)
   */
  usarEjemplo(): void {
    this.comprobanteForm.patchValue({
      rucConsultante: '20123456789',
      numRuc: '20123456789',
      codComp: '01',
      numeroSerie: 'F001',
      numero: '1',
      fechaEmision: '01/01/2024',
      monto: '100.00'
    });
  }
}
