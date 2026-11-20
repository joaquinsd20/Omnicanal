import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="nf">
      <p class="code">404</p>
      <h1>Página no encontrada</h1>
      <p>La dirección que buscas no existe o fue movida.</p>
      <a routerLink="/dashboard" class="btn-primary">Volver al inicio</a>
    </section>
  `,
  styles: [`
    .nf { min-height: 70vh; display: grid; place-content: center; text-align: center; gap: 12px; padding: 24px; }
    .code { font-size: 72px; font-weight: 800; color: var(--primary); margin: 0; line-height: 1; }
    h1 { margin: 0; color: var(--text); }
    p { color: var(--muted); margin: 0 0 8px; }
    a { justify-self: center; text-decoration: none; }
  `]
})
export class NotFoundComponent {}
