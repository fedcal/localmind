import { Component, inject, input } from '@angular/core';
import { TranslationService, SupportedLang } from '../../../core/i18n/translation.service';

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  template: `
    <div class="lang-switcher" [class.collapsed]="collapsed()">
      <button
        class="lang-btn"
        [class.active]="i18n.currentLang() === 'it'"
        (click)="switchLang('it')"
        title="Italiano">
        IT
      </button>
      <button
        class="lang-btn"
        [class.active]="i18n.currentLang() === 'en'"
        (click)="switchLang('en')"
        title="English">
        EN
      </button>
    </div>
  `,
  styles: [`
    .lang-switcher {
      display: flex;
      gap: 4px;
      padding: 4px;
      background: rgba(255,255,255,0.06);
      border-radius: 8px;
    }
    .lang-switcher.collapsed {
      flex-direction: column;
    }
    .lang-btn {
      flex: 1;
      padding: 4px 8px;
      border: none;
      border-radius: 6px;
      background: transparent;
      color: rgba(255,255,255,0.45);
      font-size: 0.7rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
      letter-spacing: 0.5px;
    }
    .lang-btn:hover {
      background: rgba(255,255,255,0.1);
      color: rgba(255,255,255,0.8);
    }
    .lang-btn.active {
      background: rgba(255,255,255,0.15);
      color: white;
    }
  `]
})
export class LanguageSwitcherComponent {
  collapsed = input(false);
  i18n = inject(TranslationService);

  switchLang(lang: SupportedLang): void {
    this.i18n.use(lang);
  }
}
