import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-privacidad',
  standalone: true,
  imports: [RouterLink],
  template: `
    <article class="legal">
      <a routerLink="/login">← Volver</a>
      <h1>Política de privacidad y aviso legal</h1>
      <p><em>Última actualización: noviembre de 2026</em></p>

      <h2>Aviso legal</h2>
      <p>Omnicanal es un proyecto académico de demostración desarrollado por Daniel Joaquín Sánchez Delgado,
        estudiante de Ingeniería de Sistemas Computacionales de la Universidad Privada del Norte (Trujillo, Perú).
        No es un servicio comercial. Los datos de la cuenta demo son ficticios.</p>

      <h2>Datos que tratamos</h2>
      <p>Si creas una cuenta, guardamos tu nombre, correo, teléfono, razón social y RUC (si los ingresas),
        y la información de productos y ventas que registres. Tu contraseña se guarda cifrada con BCrypt y nunca en texto plano.</p>

      <h2>Para qué los usamos</h2>
      <p>Solo para que puedas usar el sistema. No vendemos ni compartimos tus datos con terceros y no enviamos publicidad.</p>

      <h2>Cookies y almacenamiento</h2>
      <p>Este sitio no usa cookies de seguimiento ni de publicidad. Tu sesión se guarda en el almacenamiento local del navegador
        y se borra al cerrar sesión.</p>

      <h2>Tus derechos</h2>
      <p>De acuerdo con la Ley N.° 29733 de Protección de Datos Personales del Perú, puedes pedir el acceso, la corrección o la
        eliminación de tus datos escribiendo al autor a través de su perfil de GitHub
        (<a href="https://github.com/joaquinsd20" target="_blank" rel="noopener">github.com/joaquinsd20</a>).</p>

      <h2>Seguridad</h2>
      <p>La comunicación viaja cifrada por HTTPS y el acceso a la API está protegido con tokens JWT.
        Aun así, al ser una demo, te recomendamos no registrar información real o sensible.</p>
    </article>
  `,
  styles: [`
    .legal { max-width: 760px; margin: 0 auto; padding: 32px 20px 64px; color: var(--text); line-height: 1.6; }
    h1 { font-size: 28px; margin: 16px 0 4px; }
    h2 { font-size: 18px; margin-top: 28px; }
    p { color: #334155; }
    a { color: var(--primary); }
  `]
})
export class PrivacidadComponent {}
