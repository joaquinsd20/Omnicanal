import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(): boolean {
    const usuarioJSON = localStorage.getItem('omnicanal_usuario');

    if (!usuarioJSON) {
      this.router.navigate(['/login']);
      return false;
    }

    const usuario = JSON.parse(usuarioJSON);
    const tipo = String(usuario?.tipoUsuario || '').toUpperCase();
    const estado = String(usuario?.estado || 'ACTIVO').toUpperCase();

    if (tipo !== 'ADMINISTRADOR_GLOBAL') {
      this.router.navigate(['/dashboard']);
      return false;
    }

    if (estado === 'BLOQUEADO') {
      localStorage.removeItem('omnicanal_token');
      localStorage.removeItem('omnicanal_usuario');
      this.router.navigate(['/login']);
      return false;
    }

    return true;
  }
}
