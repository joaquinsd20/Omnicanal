import { Directive, ElementRef, Input, OnChanges, OnDestroy } from '@angular/core';

/**
 * Anima un número desde su valor anterior hasta el nuevo (≈0.8 s).
 * Uso: <strong [countUp]="total" prefix="S/ " [decimals]="2"></strong>
 * Si el usuario pidió reducir movimiento, muestra el valor final directamente.
 */
@Directive({ selector: '[countUp]', standalone: true })
export class CountUpDirective implements OnChanges, OnDestroy {
  @Input('countUp') value: number | null | undefined = 0;
  @Input() prefix = '';
  @Input() decimals = 0;
  @Input() duration = 800;

  private from = 0;
  private frame = 0;

  constructor(private el: ElementRef<HTMLElement>) {}

  ngOnChanges(): void {
    const to = Number(this.value ?? 0);
    const reduce = typeof matchMedia !== 'undefined' && matchMedia('(prefers-reduced-motion: reduce)').matches;
    cancelAnimationFrame(this.frame);
    if (reduce || this.from === to) {
      this.render(to);
      this.from = to;
      return;
    }
    const start = performance.now();
    const from = this.from;
    const step = (now: number) => {
      const t = Math.min(1, (now - start) / this.duration);
      const eased = 1 - Math.pow(1 - t, 3);
      this.render(from + (to - from) * eased);
      if (t < 1) {
        this.frame = requestAnimationFrame(step);
      } else {
        this.from = to;
      }
    };
    this.frame = requestAnimationFrame(step);
  }

  ngOnDestroy(): void {
    cancelAnimationFrame(this.frame);
  }

  private render(n: number): void {
    this.el.nativeElement.textContent = this.prefix + n.toLocaleString('en-US', {
      minimumFractionDigits: this.decimals,
      maximumFractionDigits: this.decimals
    });
  }
}
