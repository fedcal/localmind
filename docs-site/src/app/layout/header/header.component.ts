import { Component, signal, HostListener } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '../../core/i18n/translate.pipe';
import { LanguageSwitcherComponent } from '../../shared/components/language-switcher/language-switcher.component';
import { ThemeToggleComponent } from '../../shared/components/theme-toggle/theme-toggle.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, TranslatePipe, LanguageSwitcherComponent, ThemeToggleComponent],
  template: `
    <header class="header">
      <nav class="nav-container">
        <div class="nav-left">
          <a class="brand" routerLink="/">
            <span class="brand-icon">&#9671;</span>
            <span class="brand-text">LocalMind</span>
          </a>
        </div>

        <div class="nav-center" [class.open]="mobileMenuOpen()">
          <a
            class="nav-link"
            routerLink="/docs"
            routerLinkActive="active"
            (click)="closeMobileMenu()"
          >
            {{ 'NAV.DOCUMENTATION' | translate }}
          </a>
          <a
            class="nav-link external"
            href="https://github.com/fedcal/localmind"
            target="_blank"
            rel="noopener noreferrer"
            (click)="closeMobileMenu()"
          >
            GitHub
            <span class="external-icon">&#8599;</span>
          </a>

          <div class="mobile-actions">
            <app-language-switcher />
            <app-theme-toggle />
          </div>
        </div>

        <div class="nav-right">
          <app-language-switcher />
          <app-theme-toggle />

          <button
            class="hamburger"
            (click)="toggleMobileMenu()"
            [attr.aria-expanded]="mobileMenuOpen()"
            aria-label="Toggle navigation menu"
          >
            <span class="hamburger-line" [class.open]="mobileMenuOpen()"></span>
            <span class="hamburger-line" [class.open]="mobileMenuOpen()"></span>
            <span class="hamburger-line" [class.open]="mobileMenuOpen()"></span>
          </button>
        </div>
      </nav>
    </header>

    @if (mobileMenuOpen()) {
      <div class="overlay" (click)="closeMobileMenu()"></div>
    }
  `,
  styles: [`
    .header {
      position: sticky;
      top: 0;
      z-index: 100;
      background: var(--lm-header-bg, rgba(var(--lm-bg-rgb, 15, 23, 42), 0.8));
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--lm-border, rgba(148, 163, 184, 0.15));
    }

    .nav-container {
      display: flex;
      align-items: center;
      justify-content: space-between;
      max-width: 1280px;
      margin: 0 auto;
      padding: 0 24px;
      height: 64px;
    }

    .nav-left {
      flex-shrink: 0;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 10px;
      text-decoration: none;
      color: var(--lm-text);
      font-weight: 700;
      font-size: 1.25rem;
      transition: opacity 0.2s;
    }

    .brand:hover {
      opacity: 0.85;
    }

    .brand-icon {
      font-size: 1.4rem;
      color: var(--lm-primary);
    }

    .brand-text {
      background: linear-gradient(135deg, var(--lm-primary), var(--lm-accent));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .nav-center {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .nav-link {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 8px 14px;
      text-decoration: none;
      color: var(--lm-text-muted);
      font-size: 0.9rem;
      font-weight: 500;
      border-radius: 6px;
      transition: all 0.2s ease;
    }

    .nav-link:hover {
      color: var(--lm-text);
      background: rgba(148, 163, 184, 0.1);
    }

    .nav-link.active {
      color: var(--lm-primary);
      background: rgba(59, 130, 246, 0.1);
    }

    .external-icon {
      font-size: 0.75rem;
      opacity: 0.6;
    }

    .nav-right {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-shrink: 0;
    }

    .mobile-actions {
      display: none;
    }

    .hamburger {
      display: none;
      flex-direction: column;
      justify-content: center;
      gap: 5px;
      width: 38px;
      height: 38px;
      padding: 8px;
      background: var(--lm-bg-surface);
      border: 1px solid var(--lm-border, rgba(148, 163, 184, 0.2));
      border-radius: 8px;
      cursor: pointer;
      transition: border-color 0.2s;
    }

    .hamburger:hover {
      border-color: var(--lm-primary);
    }

    .hamburger-line {
      display: block;
      width: 100%;
      height: 2px;
      background: var(--lm-text);
      border-radius: 1px;
      transition: all 0.3s ease;
      transform-origin: center;
    }

    .hamburger-line.open:nth-child(1) {
      transform: translateY(7px) rotate(45deg);
    }

    .hamburger-line.open:nth-child(2) {
      opacity: 0;
    }

    .hamburger-line.open:nth-child(3) {
      transform: translateY(-7px) rotate(-45deg);
    }

    .overlay {
      display: none;
    }

    /* Desktop: hide language/theme inside nav-right only on large screens */
    :host ::ng-deep .nav-right > app-language-switcher,
    :host ::ng-deep .nav-right > app-theme-toggle {
      display: inline-flex;
    }

    @media (max-width: 768px) {
      .nav-center {
        position: fixed;
        top: 64px;
        left: 0;
        right: 0;
        flex-direction: column;
        align-items: stretch;
        padding: 16px;
        background: var(--lm-bg-surface);
        border-bottom: 1px solid var(--lm-border, rgba(148, 163, 184, 0.15));
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
        transform: translateY(-100%);
        opacity: 0;
        pointer-events: none;
        transition: all 0.3s ease;
        z-index: 99;
      }

      .nav-center.open {
        transform: translateY(0);
        opacity: 1;
        pointer-events: auto;
      }

      .nav-link {
        padding: 12px 16px;
        font-size: 1rem;
        border-radius: 8px;
      }

      .mobile-actions {
        display: flex;
        align-items: center;
        gap: 8px;
        padding-top: 12px;
        margin-top: 8px;
        border-top: 1px solid var(--lm-border, rgba(148, 163, 184, 0.15));
      }

      :host ::ng-deep .nav-right > app-language-switcher,
      :host ::ng-deep .nav-right > app-theme-toggle {
        display: none;
      }

      .hamburger {
        display: flex;
      }

      .overlay {
        display: block;
        position: fixed;
        inset: 0;
        top: 64px;
        background: rgba(0, 0, 0, 0.3);
        z-index: 98;
      }
    }
  `]
})
export class HeaderComponent {
  mobileMenuOpen = signal(false);

  toggleMobileMenu(): void {
    this.mobileMenuOpen.update(v => !v);
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen.set(false);
  }

  @HostListener('window:resize')
  onResize(): void {
    if (window.innerWidth > 768 && this.mobileMenuOpen()) {
      this.mobileMenuOpen.set(false);
    }
  }
}
