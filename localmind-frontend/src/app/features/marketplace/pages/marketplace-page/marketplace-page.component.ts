import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarketplaceService } from '../../services/marketplace.service';
import { MarketplaceAgent, MarketplaceReview, AGENT_CATEGORIES, AgentCategory } from '../../models/marketplace.model';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { LoadingSkeletonComponent } from '../../../../shared/components/loading-skeleton/loading-skeleton.component';

@Component({
  selector: 'app-marketplace-page',
  standalone: true,
  imports: [TranslatePipe, LoadingSkeletonComponent, FormsModule, DecimalPipe],
  template: `
    <div class="marketplace-page">
      <div class="header">
        <div>
          <h2>{{ 'MARKETPLACE.TITLE' | translate }}</h2>
          <p class="subtitle">{{ 'MARKETPLACE.SUBTITLE' | translate }}</p>
        </div>
        <button class="btn btn-primary" (click)="showPublishForm.set(!showPublishForm())">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          {{ 'MARKETPLACE.PUBLISH_AGENT' | translate }}
        </button>
      </div>

      @if (notification()) {
        <div class="notification" [class]="notification()!.type">
          {{ notification()!.message }}
        </div>
      }

      @if (showPublishForm()) {
        <div class="publish-form">
          <h3>{{ 'MARKETPLACE.PUBLISH_AGENT' | translate }}</h3>
          <div class="form-grid">
            <div class="form-group">
              <label>{{ 'MARKETPLACE.AGENT_NAME' | translate }} *</label>
              <input type="text" [(ngModel)]="formName" [placeholder]="i18n.instant('MARKETPLACE.AGENT_NAME')">
            </div>
            <div class="form-group">
              <label>{{ 'MARKETPLACE.AUTHOR' | translate }}</label>
              <input type="text" [(ngModel)]="formAuthor" [placeholder]="i18n.instant('MARKETPLACE.AUTHOR')">
            </div>
            <div class="form-group">
              <label>{{ 'MARKETPLACE.VERSION' | translate }}</label>
              <input type="text" [(ngModel)]="formVersion" placeholder="1.0.0">
            </div>
            <div class="form-group">
              <label>{{ 'MARKETPLACE.CATEGORY' | translate }} *</label>
              <select [(ngModel)]="formCategory">
                @for (cat of categories; track cat) {
                  <option [value]="cat">{{ 'MARKETPLACE.CAT_' + cat | translate }}</option>
                }
              </select>
            </div>
            <div class="form-group full-width">
              <label>{{ 'MARKETPLACE.DESCRIPTION' | translate }}</label>
              <textarea [(ngModel)]="formDescription" rows="3"
                        [placeholder]="i18n.instant('MARKETPLACE.DESCRIPTION')"></textarea>
            </div>
            <div class="form-group full-width">
              <label>Download URL</label>
              <input type="text" [(ngModel)]="formDownloadUrl" placeholder="https://...">
            </div>
          </div>
          <div class="form-actions">
            <button class="btn btn-primary" (click)="publishAgent()"
                    [disabled]="!formName || !formCategory || publishing()">
              @if (publishing()) { <span class="spinner-xs"></span> }
              {{ 'MARKETPLACE.PUBLISH' | translate }}
            </button>
            <button class="btn btn-secondary" (click)="showPublishForm.set(false)">
              {{ 'COMMON.CANCEL' | translate }}
            </button>
          </div>
        </div>
      }

      <div class="filters">
        <div class="search-bar">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
          </svg>
          <input type="text" [(ngModel)]="searchQuery"
                 [placeholder]="i18n.instant('MARKETPLACE.SEARCH_PLACEHOLDER')"
                 (input)="onSearch()">
        </div>
        <div class="category-filters">
          <button class="cat-btn" [class.active]="!selectedCategory()"
                  (click)="filterByCategory(null)">
            {{ 'MARKETPLACE.ALL_CATEGORIES' | translate }}
          </button>
          @for (cat of categories; track cat) {
            <button class="cat-btn" [class.active]="selectedCategory() === cat"
                    (click)="filterByCategory(cat)">
              {{ 'MARKETPLACE.CAT_' + cat | translate }}
            </button>
          }
        </div>
      </div>

      @if (loading()) {
        <app-loading-skeleton [count]="4" />
      } @else if (agents().length === 0) {
        <div class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
          </svg>
          <h3>{{ 'MARKETPLACE.NO_AGENTS' | translate }}</h3>
        </div>
      } @else {
        <div class="agent-grid">
          @for (agent of agents(); track agent.id) {
            <div class="agent-card" [class.expanded]="expandedAgent() === agent.id">
              <div class="card-header" (click)="toggleAgent(agent.id)">
                <div class="card-top">
                  <span class="category-badge" [attr.data-cat]="agent.category">
                    {{ 'MARKETPLACE.CAT_' + agent.category | translate }}
                  </span>
                  <span class="version">v{{ agent.version }}</span>
                </div>
                <h3 class="agent-name">{{ agent.name }}</h3>
                @if (agent.description) {
                  <p class="agent-desc">{{ agent.description }}</p>
                }
                <div class="agent-meta">
                  @if (agent.author) {
                    <span class="meta-item">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                        <circle cx="12" cy="7" r="4"/>
                      </svg>
                      {{ agent.author }}
                    </span>
                  }
                  <span class="meta-item">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
                      <polyline points="7 10 12 15 17 10"/>
                      <line x1="12" y1="15" x2="12" y2="3"/>
                    </svg>
                    {{ agent.downloadCount }}
                  </span>
                  <span class="meta-item rating">
                    @for (star of [1,2,3,4,5]; track star) {
                      <svg width="14" height="14" viewBox="0 0 24 24"
                           [attr.fill]="star <= Math.round(agent.avgRating) ? '#f59e0b' : 'none'"
                           stroke="#f59e0b" stroke-width="2">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                      </svg>
                    }
                    <span class="rating-value">{{ agent.avgRating | number:'1.1-1' }}</span>
                  </span>
                </div>
              </div>
              <div class="card-actions">
                <button class="btn btn-sm btn-install" (click)="installAgent(agent.id); $event.stopPropagation()"
                        [disabled]="installingId() === agent.id">
                  @if (installingId() === agent.id) { <span class="spinner-xs"></span> }
                  {{ 'MARKETPLACE.INSTALL' | translate }}
                </button>
                <button class="btn btn-sm btn-danger" (click)="deleteAgent(agent.id); $event.stopPropagation()">
                  {{ 'MARKETPLACE.DELETE' | translate }}
                </button>
              </div>

              @if (expandedAgent() === agent.id) {
                <div class="agent-details">
                  <div class="reviews-section">
                    <h4>{{ 'MARKETPLACE.REVIEWS' | translate }}</h4>
                    @if (reviewsLoading()) {
                      <p class="loading-text">{{ 'MARKETPLACE.LOADING' | translate }}</p>
                    } @else if (reviews().length === 0) {
                      <p class="no-reviews">{{ 'MARKETPLACE.NO_REVIEWS' | translate }}</p>
                    } @else {
                      @for (review of reviews(); track review.id) {
                        <div class="review-card">
                          <div class="review-stars">
                            @for (star of [1,2,3,4,5]; track star) {
                              <svg width="12" height="12" viewBox="0 0 24 24"
                                   [attr.fill]="star <= review.rating ? '#f59e0b' : 'none'"
                                   stroke="#f59e0b" stroke-width="2">
                                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                              </svg>
                            }
                          </div>
                          @if (review.comment) {
                            <p class="review-comment">{{ review.comment }}</p>
                          }
                        </div>
                      }
                    }

                    <div class="add-review">
                      <h4>{{ 'MARKETPLACE.ADD_REVIEW' | translate }}</h4>
                      <div class="review-form">
                        <div class="star-input">
                          <label>{{ 'MARKETPLACE.REVIEW_RATING' | translate }}</label>
                          <div class="star-select">
                            @for (star of [1,2,3,4,5]; track star) {
                              <button class="star-btn" (click)="reviewRating.set(star)">
                                <svg width="20" height="20" viewBox="0 0 24 24"
                                     [attr.fill]="star <= reviewRating() ? '#f59e0b' : 'none'"
                                     stroke="#f59e0b" stroke-width="2">
                                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                                </svg>
                              </button>
                            }
                          </div>
                        </div>
                        <div class="form-group">
                          <label>{{ 'MARKETPLACE.REVIEW_COMMENT' | translate }}</label>
                          <textarea [(ngModel)]="reviewComment" rows="2"
                                    [placeholder]="i18n.instant('MARKETPLACE.REVIEW_COMMENT')"></textarea>
                        </div>
                        <button class="btn btn-primary btn-sm" (click)="submitReview(agent.id)"
                                [disabled]="reviewRating() === 0 || submittingReview()">
                          @if (submittingReview()) { <span class="spinner-xs"></span> }
                          {{ 'MARKETPLACE.ADD_REVIEW' | translate }}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
    .header h2 { margin: 0; color: var(--color-text); }
    .subtitle { margin: 0.25rem 0 0; font-size: 0.85rem; color: var(--color-text-secondary); }

    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-light); }
    .btn-secondary { background: var(--color-card-bg); color: var(--color-text); border: 1px solid var(--color-border); }
    .btn-secondary:hover:not(:disabled) { background: var(--color-bg); }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-sm { padding: 0.3rem 0.6rem; font-size: 0.8rem; }
    .btn-install { background: var(--color-success); color: white; }
    .btn-install:hover:not(:disabled) { opacity: 0.9; }
    .btn-danger { color: var(--color-error); border: 1px solid var(--color-error); background: transparent; }
    .btn-danger:hover:not(:disabled) { background: var(--color-error-bg); }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: var(--color-success-bg); color: var(--color-success-text); }
    .notification.error { background: var(--color-error-bg); color: var(--color-error-text); }

    .publish-form {
      background: var(--color-card-bg); padding: 1.25rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 1.5rem;
    }
    .publish-form h3 { margin: 0 0 1rem; font-size: 1rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.3rem; }
    .form-group.full-width { grid-column: 1 / -1; }
    .form-group label { font-size: 0.8rem; font-weight: 600; color: var(--color-text-secondary); }
    .form-group input, .form-group select, .form-group textarea {
      padding: 0.5rem 0.75rem; border: 1px solid var(--color-border); border-radius: 6px;
      font-size: 0.85rem; font-family: inherit; background: var(--color-bg); color: var(--color-text);
    }
    .form-group textarea { resize: vertical; }
    .form-actions { display: flex; gap: 0.5rem; margin-top: 1rem; }

    .filters { margin-bottom: 1.25rem; display: flex; flex-direction: column; gap: 0.75rem; }
    .search-bar {
      display: flex; align-items: center; gap: 0.5rem;
      background: var(--color-card-bg); border: 1px solid var(--color-border);
      border-radius: 8px; padding: 0.5rem 0.75rem;
    }
    .search-bar svg { flex-shrink: 0; color: var(--color-text-muted); }
    .search-bar input { flex: 1; border: none; outline: none; font-size: 0.85rem; background: transparent; color: var(--color-text); }
    .category-filters { display: flex; gap: 0.4rem; flex-wrap: wrap; }
    .cat-btn {
      padding: 0.35rem 0.75rem; border: 1px solid var(--color-border); background: var(--color-card-bg);
      border-radius: 20px; cursor: pointer; font-size: 0.75rem; font-family: inherit;
      color: var(--color-text-secondary); transition: all 0.2s;
    }
    .cat-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
    .cat-btn.active { background: var(--color-primary); color: white; border-color: var(--color-primary); }

    .agent-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 1rem; }
    .agent-card {
      background: var(--color-card-bg); border-radius: 10px;
      box-shadow: 0 1px 4px rgba(0,0,0,0.08); overflow: hidden;
      transition: box-shadow 0.2s;
    }
    .agent-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.12); }
    .agent-card.expanded { grid-column: 1 / -1; }
    .card-header { padding: 1rem 1.25rem; cursor: pointer; }
    .card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
    .category-badge {
      font-size: 0.65rem; padding: 0.15rem 0.5rem; border-radius: 10px;
      text-transform: uppercase; font-weight: 600;
      background: var(--color-info-bg); color: var(--color-info-text);
    }
    .category-badge[data-cat="CHAT"] { background: #dbeafe; color: #1e40af; }
    .category-badge[data-cat="DOCUMENT_PROCESSING"] { background: #fef3c7; color: #92400e; }
    .category-badge[data-cat="CODE_ASSISTANT"] { background: #d1fae5; color: #065f46; }
    .category-badge[data-cat="DATA_ANALYSIS"] { background: #ede9fe; color: #5b21b6; }
    .category-badge[data-cat="AUTOMATION"] { background: #fce7f3; color: #9d174d; }
    .category-badge[data-cat="INTEGRATION"] { background: #e0e7ff; color: #3730a3; }
    .category-badge[data-cat="CUSTOM"] { background: var(--color-border); color: var(--color-text-secondary); }
    .version { font-size: 0.75rem; color: var(--color-text-muted); }
    .agent-name { margin: 0 0 0.25rem; font-size: 1rem; }
    .agent-desc { margin: 0 0 0.5rem; font-size: 0.82rem; color: var(--color-text-secondary); line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
    .agent-meta { display: flex; align-items: center; gap: 1rem; font-size: 0.8rem; color: var(--color-text-muted); flex-wrap: wrap; }
    .meta-item { display: flex; align-items: center; gap: 0.3rem; }
    .rating { display: flex; align-items: center; gap: 0.15rem; }
    .rating-value { font-weight: 600; margin-left: 0.2rem; }

    .card-actions { padding: 0 1.25rem 1rem; display: flex; gap: 0.5rem; }

    .agent-details { padding: 0 1.25rem 1.25rem; border-top: 1px solid var(--color-border); }
    .reviews-section { margin-top: 1rem; }
    .reviews-section h4 { margin: 0 0 0.75rem; font-size: 0.9rem; }
    .loading-text { font-size: 0.8rem; color: var(--color-text-muted); }
    .no-reviews { font-size: 0.8rem; color: var(--color-text-muted); font-style: italic; }

    .review-card {
      padding: 0.6rem; background: var(--color-bg); border-radius: 6px; margin-bottom: 0.5rem;
    }
    .review-stars { display: flex; gap: 0.1rem; margin-bottom: 0.25rem; }
    .review-comment { margin: 0; font-size: 0.82rem; color: var(--color-text-secondary); }

    .add-review { margin-top: 1rem; padding-top: 0.75rem; border-top: 1px solid var(--color-border); }
    .review-form { display: flex; flex-direction: column; gap: 0.5rem; }
    .star-input label { font-size: 0.8rem; font-weight: 600; color: var(--color-text-secondary); margin-bottom: 0.25rem; display: block; }
    .star-select { display: flex; gap: 0.2rem; }
    .star-btn { background: none; border: none; cursor: pointer; padding: 0.1rem; display: flex; }

    .empty-state {
      text-align: center; padding: 3rem; color: var(--color-text-secondary);
      background: var(--color-card-bg); border-radius: 8px;
    }
    .empty-state svg { margin-bottom: 1rem; }
    .empty-state h3 { margin-bottom: 0.25rem; }

    .spinner-xs { display: inline-block; width: 10px; height: 10px; border: 2px solid var(--color-border); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }

    @media (max-width: 768px) {
      .form-grid { grid-template-columns: 1fr; }
      .agent-grid { grid-template-columns: 1fr; }
      .category-filters { overflow-x: auto; flex-wrap: nowrap; }
    }
  `]
})
export class MarketplacePageComponent implements OnInit {
  private marketplaceService = inject(MarketplaceService);
  i18n = inject(TranslationService);
  Math = Math;

  agents = signal<MarketplaceAgent[]>([]);
  reviews = signal<MarketplaceReview[]>([]);
  loading = signal(false);
  reviewsLoading = signal(false);
  publishing = signal(false);
  submittingReview = signal(false);
  installingId = signal<string | null>(null);
  expandedAgent = signal<string | null>(null);
  selectedCategory = signal<string | null>(null);
  showPublishForm = signal(false);
  notification = signal<{ type: string; message: string } | null>(null);

  searchQuery = '';
  formName = '';
  formDescription = '';
  formAuthor = '';
  formVersion = '1.0.0';
  formCategory = 'CHAT';
  formDownloadUrl = '';
  reviewComment = '';
  reviewRating = signal(0);

  categories = AGENT_CATEGORIES;
  private searchTimeout: any;

  ngOnInit(): void {
    this.loadAgents();
  }

  loadAgents(): void {
    this.loading.set(true);
    const category = this.selectedCategory() || undefined;
    const search = this.searchQuery || undefined;
    this.marketplaceService.listAgents(category, search).subscribe({
      next: (agents) => { this.agents.set(agents); this.loading.set(false); },
      error: () => { this.loading.set(false); this.showNotify('error', this.i18n.instant('COMMON.ERROR')); }
    });
  }

  onSearch(): void {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => this.loadAgents(), 300);
  }

  filterByCategory(category: string | null): void {
    this.selectedCategory.set(category);
    this.loadAgents();
  }

  toggleAgent(agentId: string): void {
    if (this.expandedAgent() === agentId) {
      this.expandedAgent.set(null);
      return;
    }
    this.expandedAgent.set(agentId);
    this.loadReviews(agentId);
  }

  loadReviews(agentId: string): void {
    this.reviewsLoading.set(true);
    this.reviews.set([]);
    this.reviewRating.set(0);
    this.reviewComment = '';
    this.marketplaceService.getReviews(agentId).subscribe({
      next: (reviews) => { this.reviews.set(reviews); this.reviewsLoading.set(false); },
      error: () => { this.reviewsLoading.set(false); }
    });
  }

  publishAgent(): void {
    this.publishing.set(true);
    this.marketplaceService.publishAgent({
      name: this.formName,
      description: this.formDescription,
      author: this.formAuthor,
      version: this.formVersion,
      category: this.formCategory,
      downloadUrl: this.formDownloadUrl
    }).subscribe({
      next: () => {
        this.publishing.set(false);
        this.showPublishForm.set(false);
        this.resetForm();
        this.showNotify('success', this.i18n.instant('MARKETPLACE.PUBLISH_SUCCESS'));
        this.loadAgents();
      },
      error: () => {
        this.publishing.set(false);
        this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
      }
    });
  }

  installAgent(agentId: string): void {
    this.installingId.set(agentId);
    this.marketplaceService.installAgent(agentId).subscribe({
      next: () => {
        this.installingId.set(null);
        this.showNotify('success', this.i18n.instant('MARKETPLACE.INSTALL_SUCCESS'));
        this.loadAgents();
      },
      error: () => {
        this.installingId.set(null);
        this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
      }
    });
  }

  submitReview(agentId: string): void {
    this.submittingReview.set(true);
    this.marketplaceService.addReview(agentId, this.reviewRating(), this.reviewComment).subscribe({
      next: () => {
        this.submittingReview.set(false);
        this.showNotify('success', this.i18n.instant('MARKETPLACE.REVIEW_SUCCESS'));
        this.loadReviews(agentId);
        this.loadAgents();
      },
      error: () => {
        this.submittingReview.set(false);
        this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
      }
    });
  }

  deleteAgent(agentId: string): void {
    if (!confirm(this.i18n.instant('MARKETPLACE.DELETE_CONFIRM'))) return;
    this.marketplaceService.deleteAgent(agentId).subscribe({
      next: () => {
        this.showNotify('success', this.i18n.instant('MARKETPLACE.DELETE_SUCCESS'));
        if (this.expandedAgent() === agentId) this.expandedAgent.set(null);
        this.loadAgents();
      },
      error: () => { this.showNotify('error', this.i18n.instant('COMMON.ERROR')); }
    });
  }

  private resetForm(): void {
    this.formName = '';
    this.formDescription = '';
    this.formAuthor = '';
    this.formVersion = '1.0.0';
    this.formCategory = 'CHAT';
    this.formDownloadUrl = '';
  }

  private showNotify(type: string, message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 4000);
  }
}
