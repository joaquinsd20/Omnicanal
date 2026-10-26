import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Captura los errores HTTP de forma centralizada:
 *  - 401: la sesión expiró o el token es inválido -> cierra sesión y
 *    redirige al login.
 *  - Otros: extrae el mensaje del cuerpo JSON estandarizado del backend
 *    para que los componentes muestren un texto legible.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        const esLogin = req.url.includes('/auth/login') || req.url.includes('/auth/register');
        if (!esLogin) {
          authService.logout();
          router.navigate(['/login'], { queryParams: { sesionExpirada: true } });
        }
      }

      // Mensaje legible: usa el campo "message" del ApiError si existe.
      const mensaje =
        error.error?.message ||
        error.error?.error ||
        (error.status === 0
          ? 'No se pudo conectar con el servidor.'
          : 'Ocurrió un error inesperado.');

      return throwError(() => ({
        status: error.status,
        message: mensaje,
        errores: error.error?.errores ?? null
      }));
    })
  );
};
