import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../services/auth.service';

import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [IconComponent, CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  registroForm!: FormGroup;

  isRegistering = false;
  error = '';
  info = '';
  loading = false;
  servidorLento = signal(false);
  private lentoTimer?: ReturnType<typeof setTimeout>;

  /** Cuenta pública de demostración (datos de ejemplo). */
  private readonly demo = { email: 'juan@omnicanal.pe', password: 'Demo1234' };

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    // Si ya hay sesión válida, vamos directo al dashboard.
    if (this.authService.estaAutenticado()) {
      this.router.navigate(['/dashboard']);
      return;
    }

    // Despierta el backend (Render free se duerme tras 15 min sin uso)
    this.http.get(`${environment.apiUrl}/health`).subscribe({ error: () => {} });

    if (this.route.snapshot.queryParamMap.get('sesionExpirada')) {
      this.info = 'Tu sesión expiró. Inicia sesión nuevamente.';
    }

    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.registroForm = this.fb.group({
      nombreUsuario: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      telefono: [''],
      razonSocial: [''],
      ruc: ['', [Validators.pattern(/^$|^[0-9]{11}$/)]]
    });
  }

  // Accesos rápidos para el template
  get l() { return this.loginForm.controls; }
  get r() { return this.registroForm.controls; }

  login(): void {
    this.error = '';
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.iniciarCarga();
    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.terminarCarga();
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.error = err?.message || 'Email o contraseña incorrectos';
        this.terminarCarga();
      }
    });
  }

  registro(): void {
    this.error = '';
    if (this.registroForm.invalid) {
      this.registroForm.markAllAsTouched();
      return;
    }

    this.iniciarCarga();
    this.authService.registro(this.registroForm.value).subscribe({
      next: () => {
        this.terminarCarga();
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.error = err?.message || 'Error al registrarse. Intenta de nuevo.';
        this.terminarCarga();
      }
    });
  }

  usarCuentaDemo(): void {
    this.loginForm.setValue(this.demo);
    this.login();
  }

  private iniciarCarga(): void {
    this.loading = true;
    this.servidorLento.set(false);
    this.lentoTimer = setTimeout(() => (this.servidorLento.set(true)), 4000);
  }

  private terminarCarga(): void {
    this.loading = false;
    this.servidorLento.set(false);
    clearTimeout(this.lentoTimer);
  }

  toggleForm(): void {
    this.isRegistering = !this.isRegistering;
    this.error = '';
    this.info = '';
    this.loginForm.reset();
    this.registroForm.reset();
  }
}
