import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, Subject } from 'rxjs';
import { map, shareReplay, switchMap, startWith } from 'rxjs/operators';

export type SupportedLang = 'it' | 'en';

const STORAGE_KEY = 'localmind-lang';
const DEFAULT_LANG: SupportedLang = 'it';

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
    return of(this.instant(key, params));
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
    return new Promise((resolve, reject) => {
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
    if (saved === 'it' || saved === 'en') return saved;
    return DEFAULT_LANG;
  }
}
