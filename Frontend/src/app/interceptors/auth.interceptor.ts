import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Adjunta automáticamente el JWT y el id del usuario autenticado.
 *
 * IMPORTANTE:
 * Los endpoints del dashboard/financiero/reportes del backend usan:
 *   @RequestHeader("X-User-Id") Long idUsuario
 * Si ese header no viaja, el backend responde 400 y el frontend muestra datos en 0.
 *
 * Por eso este interceptor obtiene el id desde varias fuentes:
 * 1. usuarioActual.idUsuario
 * 2. usuarioActual.id_usuario / id / usuarioId, por compatibilidad con datos antiguos
 * 3. claim idUsuario dentro del JWT
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const esRutaAuth =
    req.url.includes('/auth/login') ||
    req.url.includes('/auth/register');

  if (!token || esRutaAuth) {
    return next(req);
  }

  const headers: Record<string, string> = {
    Authorization: `Bearer ${token}`
  };

  const idUsuario = obtenerIdUsuario(authService, token);

  if (idUsuario) {
    headers['X-User-Id'] = String(idUsuario);
  } else {
    console.warn('No se pudo obtener X-User-Id para la petición:', req.url);
  }

  return next(req.clone({ setHeaders: headers }));
};

function obtenerIdUsuario(authService: AuthService, token: string): number | null {
  const usuario: any = authService.getUsuarioActual();

  const idDesdeUsuario =
    usuario?.idUsuario ??
    usuario?.id_usuario ??
    usuario?.id ??
    usuario?.usuarioId;

  if (idDesdeUsuario !== undefined && idDesdeUsuario !== null && idDesdeUsuario !== '') {
    return Number(idDesdeUsuario);
  }

  const idDesdeToken = obtenerClaimIdUsuario(token);
  if (idDesdeToken !== null) {
    return idDesdeToken;
  }

  return null;
}

function obtenerClaimIdUsuario(token: string): number | null {
  try {
    const payloadBase64 = token.split('.')[1];
    if (!payloadBase64) {
      return null;
    }

    const payload = JSON.parse(atob(payloadBase64));
    const id =
      payload?.idUsuario ??
      payload?.id_usuario ??
      payload?.id ??
      payload?.usuarioId;

    if (id === undefined || id === null || id === '') {
      return null;
    }

    return Number(id);
  } catch (error) {
    console.warn('No se pudo leer idUsuario desde el JWT:', error);
    return null;
  }
}
