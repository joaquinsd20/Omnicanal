import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { UsuariosService } from '../../services/usuariosService';
import { Usuario } from '../../models/usuario.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-administrar-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './administrar-usuarios.component.html',
  styleUrls: ['./administrar-usuarios.component.css']
})
export class AdministrarUsuariosComponent implements OnInit, OnDestroy {

  usuarios: Usuario[] = [];
  usuariosActivos: Usuario[] = [];
  usuariosBloqueados: Usuario[] = [];
  administradores: Usuario[] = [];
  estadisticas: any = {};

  seccionActiva: 'activos' | 'bloqueados' | 'administradores' | 'todos' = 'activos';
  mostrarModalCrear = false;
  mostrarModalDetalles = false;
  usuarioSeleccionado: Usuario | null = null;

  formularioCrear: FormGroup;
  cargando = false;
  mensajeExito = '';
  mensajeError = '';

  private destroy$ = new Subject<void>();

  constructor(
    private usuariosService: UsuariosService,
    private fb: FormBuilder
  ) {
    this.formularioCrear = this.fb.group({
      nombreUsuario: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      razonSocial: ['', Validators.required],
      ruc: [''],
      ciudad: ['Lima', Validators.required],
      tipoUsuario: ['EMPRENDEDOR', Validators.required],
      planSuscripcion: ['PLAN_EMPRENDEDOR', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Cargar todos los datos
   */
  cargarDatos(): void {
    this.cargando = true;

    this.usuariosService.obtenerActivos()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (usuarios) => {
          this.usuariosActivos = usuarios;
          this.usuarios = usuarios;
        },
        error: (err) => console.error('Error al cargar usuarios activos:', err),
        complete: () => this.cargando = false
      });

    this.usuariosService.obtenerBloqueados()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (usuarios) => {
          this.usuariosBloqueados = usuarios;
        },
        error: (err) => console.error('Error al cargar usuarios bloqueados:', err)
      });

    this.usuariosService.obtenerAdministradores()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (usuarios) => {
          this.administradores = usuarios;
        },
        error: (err) => console.error('Error al cargar administradores:', err)
      });

    this.usuariosService.obtenerEstadisticas()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (stats) => {
          this.estadisticas = stats;
        },
        error: (err) => console.error('Error al cargar estadísticas:', err)
      });
  }

  /**
   * Cambiar sección activa
   */
  cambiarSeccion(seccion: 'activos' | 'bloqueados' | 'administradores' | 'todos'): void {
    this.seccionActiva = seccion;

    if (seccion === 'activos') {
      this.usuarios = this.usuariosActivos;
    } else if (seccion === 'bloqueados') {
      this.usuarios = this.usuariosBloqueados;
    } else if (seccion === 'administradores') {
      this.usuarios = this.administradores;
    } else if (seccion === 'todos') {
      this.usuarios = [...this.usuariosActivos, ...this.usuariosBloqueados];
    }
  }

  /**
   * Abrir modal para crear usuario
   */
  abrirModalCrear(): void {
    this.mostrarModalCrear = true;
    this.formularioCrear.reset({
      ciudad: 'Lima',
      tipoUsuario: 'EMPRENDEDOR',
      planSuscripcion: 'PLAN_EMPRENDEDOR'
    });
    this.mensajeError = '';
  }

  /**
   * Cerrar modal de crear usuario
   */
  cerrarModalCrear(): void {
    this.mostrarModalCrear = false;
    this.formularioCrear.reset();
  }

  /**
   * Guardar nuevo usuario
   */
  guardarUsuario(): void {
    if (this.formularioCrear.invalid) {
      this.mensajeError = 'Por favor, completa todos los campos requeridos';
      return;
    }

    this.cargando = true;
    const usuarioForm = this.formularioCrear.value;
    
    const nuevoUsuario: Usuario = {
      nombreUsuario: usuarioForm.nombreUsuario,
      email: usuarioForm.email,
      password: usuarioForm.password,
      razonSocial: usuarioForm.razonSocial,
      ruc: usuarioForm.ruc,
      ciudad: usuarioForm.ciudad,
      tipoUsuario: String(usuarioForm.tipoUsuario).toUpperCase() as any,
      planSuscripcion: String(usuarioForm.planSuscripcion).toUpperCase() as any,
      estado: 'ACTIVO' as const
    };

    this.usuariosService.crearUsuario(nuevoUsuario)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (respuesta) => {
          this.mensajeExito = `Usuario ${respuesta.nombre_usuario} creado exitosamente`;
          this.cerrarModalCrear();
          this.cargarDatos();
          setTimeout(() => this.mensajeExito = '', 5000);
        },
        error: (err) => {
          this.mensajeError = err.error?.error || 'Error al crear usuario';
          this.cargando = false;
        }
      });
  }

  /**
   * Ver detalles de un usuario
   */
  verDetalles(usuario: Usuario): void {
    this.usuarioSeleccionado = usuario;
    this.mostrarModalDetalles = true;
  }

  /**
   * Cerrar modal de detalles
   */
  cerrarModalDetalles(): void {
    this.mostrarModalDetalles = false;
    this.usuarioSeleccionado = null;
  }

  /**
   * Bloquear usuario
   */
  bloquearUsuario(usuario: Usuario): void {
    if (confirm(`¿Deseas bloquear a ${usuario.nombreUsuario}? No podrá iniciar sesión.`)) {
      this.usuariosService.bloquearUsuario(usuario.idUsuario!)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.mensajeExito = `${usuario.nombreUsuario} ha sido bloqueado`;
            this.cargarDatos();
            setTimeout(() => this.mensajeExito = '', 5000);
          },
          error: (err) => {
            this.mensajeError = err.error?.error || 'Error al bloquear usuario';
          }
        });
    }
  }

  /**
   * Desbloquear usuario
   */
  desbloquearUsuario(usuario: Usuario): void {
    if (confirm(`¿Deseas desbloquear a ${usuario.nombreUsuario}? Podrá volver a iniciar sesión.`)) {
      this.usuariosService.desbloquearUsuario(usuario.idUsuario!)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.mensajeExito = `${usuario.nombreUsuario} ha sido desbloqueado`;
            this.cargarDatos();
            setTimeout(() => this.mensajeExito = '', 5000);
          },
          error: (err) => {
            this.mensajeError = err.error?.error || 'Error al desbloquear usuario';
          }
        });
    }
  }

  /**
   * Obtener usuarios para la tabla actual
   */
  get usuariosParaMostrar(): Usuario[] {
    return this.usuarios;
  }
}
