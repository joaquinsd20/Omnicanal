import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
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

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Si ya hay sesión válida, vamos directo al dashboard.
    if (this.authService.estaAutenticado()) {
      this.router.navigate(['/dashboard']);
      return;
    }

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
      ruc: ['', [Validators.pattern(/^$|^[0-9]{11}$/)]],
      tipoUsuario: ['EMPRENDEDOR'] // Nuevo: selector de tipo de usuario
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

    this.loading = true;
    this.authService.login(this.loginForm.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.error = err?.message || 'Email o contraseña incorrectos';
        this.loading = false;
      }
    });
  }

  registro(): void {
    this.error = '';
    if (this.registroForm.invalid) {
      this.registroForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.authService.registro(this.registroForm.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.error = err?.message || 'Error al registrarse. Intenta de nuevo.';
        this.loading = false;
      }
    });
  }

  toggleForm(): void {
    this.isRegistering = !this.isRegistering;
    this.error = '';
    this.info = '';
    this.loginForm.reset();
    this.registroForm.reset();
  }
}
