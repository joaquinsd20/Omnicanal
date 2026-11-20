import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { authGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';

// Cada pantalla se carga bajo demanda (lazy loading) para que la primera carga sea más rápida.
export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent, title: 'Iniciar sesión | Omnicanal' },
  { path: 'privacidad', title: 'Privacidad | Omnicanal',
    loadComponent: () => import('./components/privacidad/privacidad.component').then(m => m.PrivacidadComponent) },
  { path: 'dashboard', canActivate: [authGuard], title: 'Dashboard | Omnicanal',
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent) },
  { path: 'admin-dashboard', canActivate: [authGuard, AdminGuard], title: 'Panel admin | Omnicanal',
    loadComponent: () => import('./components/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
  { path: 'productos', canActivate: [authGuard], title: 'Productos | Omnicanal',
    loadComponent: () => import('./components/productos/productos.component').then(m => m.ProductosComponent) },
  { path: 'ventas', canActivate: [authGuard], title: 'Ventas | Omnicanal',
    loadComponent: () => import('./components/ventas/ventas.component').then(m => m.VentasComponent) },
  { path: 'financiero', canActivate: [authGuard], title: 'Financiero | Omnicanal',
    loadComponent: () => import('./components/financiero/financiero.component').then(m => m.FinancieroComponent) },
  { path: 'importaciones', canActivate: [authGuard], title: 'Importaciones | Omnicanal',
    loadComponent: () => import('./components/importacion/importacion.component').then(m => m.ImportacionComponent) },
  { path: 'kardex', canActivate: [authGuard], title: 'Kardex | Omnicanal',
    loadComponent: () => import('./components/kardex/kardex.component').then(m => m.KardexComponent) },
  { path: 'sunat-test', canActivate: [authGuard], title: 'SUNAT | Omnicanal',
    loadComponent: () => import('./components/sunat-test/sunat-test.component').then(m => m.SunatTestComponent) },
  { path: 'administrar-usuarios', canActivate: [authGuard, AdminGuard], title: 'Usuarios | Omnicanal',
    loadComponent: () => import('./components/administrar-usuarios/administrar-usuarios.component').then(m => m.AdministrarUsuariosComponent) },
  { path: '**', title: 'Página no encontrada | Omnicanal',
    loadComponent: () => import('./components/not-found/not-found.component').then(m => m.NotFoundComponent) }
];
