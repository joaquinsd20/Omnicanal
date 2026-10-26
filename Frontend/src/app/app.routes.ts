import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ProductosComponent } from './components/productos/productos.component';
import { VentasComponent } from './components/ventas/ventas.component';
import { FinancieroComponent } from './components/financiero/financiero.component';
import { KardexComponent } from './components/kardex/kardex.component';
import { SunatTestComponent } from './components/sunat-test/sunat-test.component';
import { ImportacionComponent } from './components/importacion/importacion.component';
import { AdministrarUsuariosComponent } from './components/administrar-usuarios/administrar-usuarios.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { authGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'admin-dashboard', component: AdminDashboardComponent, canActivate: [authGuard, AdminGuard] },
  { path: 'productos', component: ProductosComponent, canActivate: [authGuard] },
  { path: 'ventas', component: VentasComponent, canActivate: [authGuard] },
  { path: 'financiero', component: FinancieroComponent, canActivate: [authGuard] },
  { path: 'importaciones', component: ImportacionComponent, canActivate: [authGuard] },
  { path: 'kardex', component: KardexComponent, canActivate: [authGuard] },
  { path: 'sunat-test', component: SunatTestComponent, canActivate: [authGuard] },
  { path: 'administrar-usuarios', component: AdministrarUsuariosComponent, canActivate: [AdminGuard] },
  { path: '**', redirectTo: '/login' }
];
