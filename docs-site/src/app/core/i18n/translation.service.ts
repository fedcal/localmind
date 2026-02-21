import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

export type SupportedLang = 'en' | 'it' | 'fr' | 'de' | 'es' | 'pt';

export const LANGUAGES: { code: SupportedLang; label: string; flag: string }[] = [
  { code: 'en', label: 'English', flag: '🇬🇧' },
  { code: 'it', label: 'Italiano', flag: '🇮🇹' },
  { code: 'fr', label: 'Français', flag: '🇫🇷' },
  { code: 'de', label: 'Deutsch', flag: '🇩🇪' },
  { code: 'es', label: 'Español', flag: '🇪🇸' },
  { code: 'pt', label: 'Português', flag: '🇵🇹' },
];

const STORAGE_KEY = 'localmind-docs-lang';
const DEFAULT_LANG: SupportedLang = 'en';
const VALID_LANGS = new Set<string>(['en', 'it', 'fr', 'de', 'es', 'pt']);

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private cache = new Map<string, Record<string, string>>();
  private langChange$ = new Subject<SupportedLang>();

  currentLang = signal<SupportedLang>(this.getSavedLang());
  langChange = this.langChange$.asObservable();

  constructor(private http: HttpClient) {}

  init(): Promise<void> {
    return this.loadTranslations(this.currentLang()).then(() => {});
  }

  use(lang: SupportedLang): void {
    this.currentLang.set(lang);
    localStorage.setItem(STORAGE_KEY, lang);
    if (!this.cache.has(lang)) {
      this.loadTranslations(lang).then(() => this.langChange$.next(lang));
    } else {
      this.langChange$.next(lang);
    }
  }

  instant(key: string, params?: Record<string, any>): string {
    const translations = this.cache.get(this.currentLang());
    if (!translations) return key;
    const raw = translations[key];
    if (!raw) return key;
    return params ? this.interpolate(raw, params) : raw;
  }

  get(key: string, params?: Record<string, any>): Observable<string> {
    return this.langChange$.pipe(
      startWith(this.currentLang()),
      map(() => this.instant(key, params))
    );
  }

  stream(key: string, params?: Record<string, any>): Observable<string> {
    return this.langChange$.pipe(
      startWith(this.currentLang()),
      map(() => this.instant(key, params))
    );
  }

  private loadTranslations(lang: string): Promise<Record<string, string>> {
    if (this.cache.has(lang)) {
      return Promise.resolve(this.cache.get(lang)!);
    }
    return new Promise((resolve) => {
      this.http.get<Record<string, string>>(`./assets/i18n/${lang}.json`).subscribe({
        next: (data) => {
          this.cache.set(lang, data);
          resolve(data);
        },
        error: (err) => {
          console.error(`Failed to load translations for '${lang}'`, err);
          this.cache.set(lang, {});
          resolve({});
        }
      });
    });
  }

  private interpolate(text: string, params: Record<string, any>): string {
    return text.replace(/\{\{(\w+)\}\}/g, (_, key) => {
      return params[key] !== undefined ? String(params[key]) : `{{${key}}}`;
    });
  }

  private getSavedLang(): SupportedLang {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved && VALID_LANGS.has(saved)) return saved as SupportedLang;
    const browserLang = navigator.language?.substring(0, 2);
    if (browserLang && VALID_LANGS.has(browserLang)) return browserLang as SupportedLang;
    return DEFAULT_LANG;
  }
}
