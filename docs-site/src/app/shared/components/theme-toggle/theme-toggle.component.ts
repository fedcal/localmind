import { Component, inject, computed } from '@angular/core';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  template: `
    <button
      class="theme-btn"
      (click)="theme.toggle()"
      [attr.aria-label]="isDark() ? 'Switch to light mode' : 'Switch to dark mode'"
      [title]="isDark() ? 'Light mode' : 'Dark mode'"
    >
      <span class="icon" [class.sun]="isDark()" [class.moon]="!isDark()">
        {{ isDark() ? '\u2600\uFE0F' : '\uD83C\uDF19' }}
      </span>
    </button>
  `,
  styles: [`
    .theme-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 38px;
      height: 38px;
      padding: 0;
      background: var(--lm-bg-surface);
      border: 1px solid var(--lm-border, rgba(148, 163, 184, 0.2));
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .theme-btn:hover {
      border-color: var(--lm-primary);
      transform: scale(1.05);
    }

    .icon {
      font-size: 1.15rem;
      line-height: 1;
      transition: transform 0.3s ease;
    }

    .icon.sun {
      animation: sunRise 0.3s ease;
    }

    .icon.moon {
      animation: moonRise 0.3s ease;
    }

    @keyframes sunRise {
      from {
        transform: rotate(-90deg) scale(0.5);
        opacity: 0;
      }
      to {
        transform: rotate(0) scale(1);
        opacity: 1;
      }
    }

    @keyframes moonRise {
      from {
        transform: rotate(90deg) scale(0.5);
        opacity: 0;
      }
      to {
        transform: rotate(0) scale(1);
        opacity: 1;
      }
    }
  `]
})
export class ThemeToggleComponent {
  protected theme = inject(ThemeService);
  protected isDark = computed(() => this.theme.currentTheme() === 'dark');
}
