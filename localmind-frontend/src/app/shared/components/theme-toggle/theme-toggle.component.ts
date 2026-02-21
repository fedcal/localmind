import { Component, inject, input } from '@angular/core';
import { ThemeService } from '../../../core/services/theme.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    <button class="theme-toggle" [class.collapsed]="collapsed()"
            (click)="theme.toggle()"
            [title]="(theme.isDark() ? 'THEME.LIGHT' : 'THEME.DARK') | translate">
      @if (theme.isDark()) {
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/>
          <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
          <line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/>
          <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
        </svg>
      } @else {
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/>
        </svg>
      }
      @if (!collapsed()) {
        <span class="toggle-label">{{ (theme.isDark() ? 'THEME.LIGHT' : 'THEME.DARK') | translate }}</span>
      }
    </button>
  `,
  styles: [`
    .theme-toggle {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      width: 100%;
      padding: 6px 10px;
      background: rgba(255,255,255,0.06);
      border: none;
      border-radius: 8px;
      color: rgba(255,255,255,0.55);
      cursor: pointer;
      font-size: 0.75rem;
      font-weight: 500;
      transition: all 0.2s ease;
    }
    .theme-toggle:hover {
      background: rgba(255,255,255,0.12);
      color: rgba(255,255,255,0.9);
    }
    .theme-toggle.collapsed {
      justify-content: center;
      padding: 6px;
    }
    .toggle-label {
      white-space: nowrap;
    }
  `]
})
export class ThemeToggleComponent {
  collapsed = input(false);
  theme = inject(ThemeService);
}
