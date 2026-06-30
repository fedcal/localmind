import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SearchService } from '../../services/search.service';
import { SearchResult } from '../../models/search.model';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { LoadingSkeletonComponent } from '../../../../shared/components/loading-skeleton/loading-skeleton.component';

@Component({
  selector: 'app-search-page',
  imports: [FormsModule, TranslatePipe, LoadingSkeletonComponent],
  template: `
    <div class="search-page">
      <div class="page-header">
        <h1>{{ 'SEARCH.TITLE' | translate }}</h1>
        <p class="subtitle">{{ 'SEARCH.SUBTITLE' | translate }}</p>
      </div>

      <div class="search-bar">
        <div class="search-input-wrap">
          <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
          </svg>
          <input
            type="text"
            [(ngModel)]="query"
            (keyup.enter)="search()"
            [placeholder]="'SEARCH.PLACEHOLDER' | translate"
            [disabled]="loading()"
          />
        </div>
        <div class="search-options">
          <label>
            {{ 'SEARCH.TOP_K' | translate }}
            <select [(ngModel)]="topK">
              <option [value]="3">3</option>
              <option [value]="5">5</option>
              <option [value]="10">10</option>
              <option [value]="20">20</option>
            </select>
          </label>
          <button class="btn btn-primary" (click)="search()" [disabled]="!query.trim() || loading()">
            @if (loading()) {
              <span class="loading-spinner"></span>
            }
            {{ 'SEARCH.BUTTON' | translate }}
          </button>
        </div>
      </div>

      @if (hasSearched() && !loading()) {
        <div class="results-header">
          <span [innerHTML]="'SEARCH.RESULTS_COUNT' | translate:{ count: results().length, query: lastQuery() }"></span>
        </div>
      }

      @if (loading()) {
        <app-loading-skeleton [count]="3" />
      } @else if (hasSearched() && results().length === 0) {
        <div class="empty-state card">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
          </svg>
          <h3>{{ 'SEARCH.EMPTY_TITLE' | translate }}</h3>
          <p>{{ 'SEARCH.EMPTY_DESC' | translate }}</p>
        </div>
      } @else {
        <div class="results-list">
          @for (result of results(); track $index) {
            <div class="result-card">
              <div class="result-header">
                <div class="result-score-wrap">
                  <div class="score-bar">
                    <div class="score-fill" [style.width.%]="result.score * 100"></div>
                  </div>
                  <span class="score-value">{{ (result.score * 100).toFixed(1) }}%</span>
                </div>
                <span class="result-doc">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                  </svg>
                  {{ result.documentName }}
                </span>
              </div>
              <div class="result-content">{{ result.content }}</div>
            </div>
          }
        </div>
      }

      @if (!hasSearched()) {
        <div class="hint-area">
          <h3>{{ 'SEARCH.HINTS_TITLE' | translate }}</h3>
          <div class="hints">
            <button class="hint" (click)="useHint('SEARCH.HINT_SUMMARIZE')">
              {{ 'SEARCH.HINT_SUMMARIZE' | translate }}
            </button>
            <button class="hint" (click)="useHint('SEARCH.HINT_CONCEPTS')">
              {{ 'SEARCH.HINT_CONCEPTS' | translate }}
            </button>
            <button class="hint" (click)="useHint('SEARCH.HINT_CONFIG')">
              {{ 'SEARCH.HINT_CONFIG' | translate }}
            </button>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .search-page { max-width: 800px; }
    .page-header { margin-bottom: 1.5rem; }
    .page-header h1 { margin: 0 0 0.25rem 0; font-size: 1.5rem; }
    .subtitle { color: var(--color-text-muted); font-size: 0.85rem; margin: 0; }

    .search-bar {
      background: var(--color-card-bg); padding: 1.25rem;
      border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);
      margin-bottom: 1.5rem;
    }
    .search-input-wrap {
      display: flex; align-items: center; gap: 0.5rem;
      border: 1px solid var(--color-border); border-radius: 8px;
      padding: 0 0.75rem; margin-bottom: 0.75rem;
    }
    .search-icon { color: var(--color-text-muted); flex-shrink: 0; }
    .search-input-wrap input {
      flex: 1; border: none; padding: 0.75rem 0;
      font-size: 0.95rem; outline: none; background: none;
      width: 100%;
    }
    .search-options { display: flex; align-items: center; justify-content: space-between; }
    .search-options label { font-size: 0.8rem; color: var(--color-text-muted); display: flex; align-items: center; gap: 0.35rem; }
    .search-options select { padding: 0.3rem 0.5rem; border: 1px solid var(--color-border); border-radius: 4px; font-size: 0.8rem; }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1.25rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-light); }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .loading-spinner {
      display: inline-block; width: 14px; height: 14px;
      border: 2px solid rgba(255,255,255,0.3); border-top-color: white;
      border-radius: 50%; animation: spin 0.6s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .results-header { font-size: 0.85rem; color: var(--color-text-muted); margin-bottom: 1rem; }
    .loading-state {
      display: flex; align-items: center; gap: 0.5rem;
      justify-content: center; padding: 3rem; color: var(--color-text-muted);
    }
    .loading-state .loading-spinner { border-color: var(--color-border); border-top-color: var(--color-primary); }
    .empty-state { text-align: center; padding: 3rem; }
    .empty-state svg { margin-bottom: 1rem; }
    .empty-state h3 { color: var(--color-text-secondary); margin-bottom: 0.25rem; }
    .empty-state p { font-size: 0.85rem; color: var(--color-text-muted); }

    .results-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .result-card {
      background: var(--color-card-bg); padding: 1.25rem;
      border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.06);
    }
    .result-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
    .result-score-wrap { display: flex; align-items: center; gap: 0.5rem; }
    .score-bar { width: 80px; height: 6px; background: var(--color-border-light); border-radius: 3px; overflow: hidden; }
    .score-fill { height: 100%; background: var(--color-primary); border-radius: 3px; }
    .score-value { font-size: 0.75rem; font-weight: 600; color: var(--color-primary); }
    .result-doc { font-size: 0.75rem; color: var(--color-text-muted); display: flex; align-items: center; gap: 0.25rem; }
    .result-content {
      font-size: 0.88rem; line-height: 1.6; color: var(--color-text-secondary);
      overflow: hidden; display: -webkit-box;
      -webkit-line-clamp: 4; -webkit-box-orient: vertical;
    }

    .hint-area { margin-top: 2rem; }
    .hint-area h3 { font-size: 0.95rem; color: var(--color-text-muted); margin-bottom: 0.75rem; }
    .hints { display: flex; flex-wrap: wrap; gap: 0.5rem; }
    .hint {
      padding: 0.5rem 1rem; background: var(--color-card-bg); border: 1px solid var(--color-border);
      border-radius: 20px; cursor: pointer; font-size: 0.8rem;
      color: var(--color-text-secondary); font-family: inherit; transition: all 0.15s;
    }
    .hint:hover { border-color: var(--color-primary); color: var(--color-primary); }

    .card { background: var(--color-card-bg); border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
  `]
})
export class SearchPageComponent {
  private searchService = inject(SearchService);
  private i18n = inject(TranslationService);

  query = '';
  topK = 5;
  results = signal<SearchResult[]>([]);
  loading = signal(false);
  hasSearched = signal(false);
  lastQuery = signal('');

  search() {
    const q = this.query.trim();
    if (!q) return;

    this.loading.set(true);
    this.hasSearched.set(true);
    this.lastQuery.set(q);

    this.searchService.search({ query: q, topK: this.topK }).subscribe({
      next: (results) => { this.results.set(results); this.loading.set(false); },
      error: () => { this.results.set([]); this.loading.set(false); }
    });
  }

  useHint(key: string) {
    this.query = this.i18n.instant(key);
    this.search();
  }
}
