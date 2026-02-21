import { Component, inject, signal, HostListener } from '@angular/core';
import { TranslationService, LANGUAGES, SupportedLang } from '../../../core/i18n/translation.service';

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  template: `
    <div class="lang-switcher">
      <button
        class="lang-btn"
        (click)="toggleDropdown($event)"
        [attr.aria-expanded]="isOpen()"
        aria-haspopup="listbox"
      >
        <span class="current-flag">{{ currentFlag() }}</span>
        <span class="current-code">{{ currentLang().toUpperCase() }}</span>
        <span class="chevron" [class.open]="isOpen()">&#9662;</span>
      </button>

      @if (isOpen()) {
        <ul class="dropdown" role="listbox">
          @for (lang of languages; track lang.code) {
            <li role="option" [attr.aria-selected]="lang.code === currentLang()">
              <button
                class="dropdown-item"
                [class.active]="lang.code === currentLang()"
                (click)="selectLang(lang.code)"
              >
                <span class="flag">{{ lang.flag }}</span>
                <span class="label">{{ lang.label }}</span>
                <span class="code">{{ lang.code.toUpperCase() }}</span>
              </button>
            </li>
          }
        </ul>
      }
    </div>
  `,
  styles: [`
    :host {
      position: relative;
      display: inline-block;
    }

    .lang-switcher {
      position: relative;
    }

    .lang-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      background: var(--lm-bg-surface);
      border: 1px solid var(--lm-border, rgba(148, 163, 184, 0.2));
      border-radius: 8px;
      color: var(--lm-text);
      font-size: 0.875rem;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .lang-btn:hover {
      border-color: var(--lm-primary);
      background: var(--lm-bg-surface);
    }

    .current-flag {
      font-size: 1.1rem;
    }

    .current-code {
      font-weight: 500;
    }

    .chevron {
      font-size: 0.7rem;
      transition: transform 0.2s ease;
      color: var(--lm-text-muted);
    }

    .chevron.open {
      transform: rotate(180deg);
    }

    .dropdown {
      position: absolute;
      top: calc(100% + 4px);
      right: 0;
      min-width: 180px;
      padding: 4px;
      margin: 0;
      list-style: none;
      background: var(--lm-bg-surface);
      border: 1px solid var(--lm-border, rgba(148, 163, 184, 0.2));
      border-radius: 10px;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      z-index: 1000;
      animation: dropdownFadeIn 0.15s ease;
    }

    @keyframes dropdownFadeIn {
      from {
        opacity: 0;
        transform: translateY(-4px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .dropdown li {
      margin: 0;
    }

    .dropdown-item {
      display: flex;
      align-items: center;
      gap: 10px;
      width: 100%;
      padding: 8px 12px;
      background: none;
      border: none;
      border-radius: 6px;
      color: var(--lm-text);
      font-size: 0.875rem;
      cursor: pointer;
      transition: background 0.15s ease;
    }

    .dropdown-item:hover {
      background: var(--lm-primary);
      color: #fff;
    }

    .dropdown-item:hover .code {
      color: rgba(255, 255, 255, 0.7);
    }

    .dropdown-item.active {
      background: rgba(59, 130, 246, 0.1);
      color: var(--lm-primary);
      font-weight: 500;
    }

    .dropdown-item.active:hover {
      background: var(--lm-primary);
      color: #fff;
    }

    .flag {
      font-size: 1.2rem;
    }

    .label {
      flex: 1;
    }

    .code {
      font-size: 0.75rem;
      color: var(--lm-text-muted);
      font-weight: 500;
    }
  `]
})
export class LanguageSwitcherComponent {
  private i18n = inject(TranslationService);

  languages = LANGUAGES;
  isOpen = signal(false);
  currentLang = this.i18n.currentLang;

  currentFlag = () => {
    const lang = LANGUAGES.find(l => l.code === this.currentLang());
    return lang?.flag ?? '';
  };

  toggleDropdown(event: Event): void {
    event.stopPropagation();
    this.isOpen.update(v => !v);
  }

  selectLang(code: SupportedLang): void {
    this.i18n.use(code);
    this.isOpen.set(false);
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    if (this.isOpen()) {
      this.isOpen.set(false);
    }
  }
}
