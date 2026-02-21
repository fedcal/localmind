import { Injectable, signal, effect } from '@angular/core';

export type Theme = 'light' | 'dark';

const STORAGE_KEY = 'localmind-docs-theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  currentTheme = signal<Theme>(this.getInitialTheme());

  constructor() {
    effect(() => {
      const theme = this.currentTheme();
      document.documentElement.setAttribute('data-theme', theme);
      localStorage.setItem(STORAGE_KEY, theme);
    });
  }

  toggle(): void {
    this.currentTheme.set(this.currentTheme() === 'light' ? 'dark' : 'light');
  }

  setTheme(theme: Theme): void {
    this.currentTheme.set(theme);
  }

  isDark(): boolean {
    return this.currentTheme() === 'dark';
  }

  private getInitialTheme(): Theme {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved === 'light' || saved === 'dark') return saved;
    if (window.matchMedia?.('(prefers-color-scheme: dark)').matches) {
      return 'dark';
    }
    return 'dark';
  }
}
