import { Component, signal } from '@angular/core';
import { RouterOutlet, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('Omnicanal');
  isAuthenticated = signal(false);
  currentUser = signal<any>(null);
  showMenu = signal(false);

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.checkAuthentication();
    this.authService.usuarioActual$.subscribe((usuario) => {
      this.currentUser.set(usuario);
      this.isAuthenticated.set(!!usuario && this.authService.estaAutenticado());
    });
  }

  checkAuthentication(): void {
    const usuario = this.authService.getUsuarioActual();
    this.isAuthenticated.set(!!usuario && this.authService.estaAutenticado());
    this.currentUser.set(usuario);
  }

  esAdminGlobal(): boolean {
    return String(this.currentUser()?.tipoUsuario || '').toUpperCase() === 'ADMINISTRADOR_GLOBAL';
  }

  logout(): void {
    this.authService.logout();
    this.isAuthenticated.set(false);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  toggleMenu(): void {
    this.showMenu.set(!this.showMenu());
  }
}
