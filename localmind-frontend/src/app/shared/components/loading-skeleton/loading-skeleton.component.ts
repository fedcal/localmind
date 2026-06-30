import { Component, input, computed } from '@angular/core';

@Component({
  selector: 'app-loading-skeleton',
  standalone: true,
  template: `
    @switch (variant()) {
      @case ('card-grid') {
        <div class="skeleton-grid">
          @for (item of skeletonItems(); track $index) {
            <div class="skeleton-card">
              <div class="skeleton-line skeleton-icon"></div>
              <div class="skeleton-line skeleton-title"></div>
              <div class="skeleton-line skeleton-text"></div>
              <div class="skeleton-line skeleton-text short"></div>
            </div>
          }
        </div>
      }
      @case ('table') {
        <div class="skeleton-table">
          <div class="skeleton-table-header">
            @for (col of [1,2,3,4]; track col) {
              <div class="skeleton-line skeleton-col"></div>
            }
          </div>
          @for (item of skeletonItems(); track $index) {
            <div class="skeleton-table-row">
              @for (col of [1,2,3,4]; track col) {
                <div class="skeleton-line skeleton-cell"></div>
              }
            </div>
          }
        </div>
      }
      @case ('stat') {
        <div class="skeleton-stats">
          @for (item of skeletonItems(); track $index) {
            <div class="skeleton-stat-card">
              <div class="skeleton-line skeleton-stat-value"></div>
              <div class="skeleton-line skeleton-stat-label"></div>
            </div>
          }
        </div>
      }
      @case ('chat-sidebar') {
        <div class="skeleton-container">
          @for (item of skeletonItems(); track $index) {
            <div class="skeleton-chat-item">
              <div class="skeleton-line skeleton-chat-title"></div>
              <div class="skeleton-line skeleton-chat-preview"></div>
            </div>
          }
        </div>
      }
      @default {
        <div class="skeleton-container">
          @for (item of skeletonItems(); track $index) {
            <div class="skeleton-card">
              <div class="skeleton-line skeleton-title"></div>
              <div class="skeleton-line skeleton-text"></div>
              <div class="skeleton-line skeleton-text short"></div>
            </div>
          }
        </div>
      }
    }
  `,
  styles: [`
    .skeleton-container {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    .skeleton-card {
      background: var(--color-card-bg);
      border-radius: 12px;
      padding: 1.25rem;
      border: 1px solid var(--color-border-light);
    }
    .skeleton-line {
      height: 14px;
      background: linear-gradient(90deg, var(--color-skeleton-base) 25%, var(--color-skeleton-shine) 50%, var(--color-skeleton-base) 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: 4px;
      margin-bottom: 0.75rem;
    }
    .skeleton-title {
      width: 40%;
      height: 18px;
    }
    .skeleton-text {
      width: 80%;
    }
    .skeleton-text.short {
      width: 55%;
      margin-bottom: 0;
    }

    /* card-grid variant */
    .skeleton-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1rem;
    }
    .skeleton-grid .skeleton-card {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    .skeleton-icon {
      width: 32px;
      height: 32px;
      border-radius: 8px;
    }

    /* table variant */
    .skeleton-table {
      background: var(--color-card-bg);
      border-radius: 12px;
      border: 1px solid var(--color-border-light);
      overflow: hidden;
    }
    .skeleton-table-header {
      display: grid;
      grid-template-columns: 2fr 1fr 1fr 1fr;
      gap: 1rem;
      padding: 1rem 1.25rem;
      background: var(--color-hover-bg);
    }
    .skeleton-table-row {
      display: grid;
      grid-template-columns: 2fr 1fr 1fr 1fr;
      gap: 1rem;
      padding: 0.875rem 1.25rem;
      border-top: 1px solid var(--color-border-light);
    }
    .skeleton-col {
      height: 12px;
      width: 70%;
      margin-bottom: 0;
    }
    .skeleton-cell {
      height: 12px;
      width: 85%;
      margin-bottom: 0;
    }

    /* stat variant */
    .skeleton-stats {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 1rem;
    }
    .skeleton-stat-card {
      background: var(--color-card-bg);
      border-radius: 12px;
      padding: 1.25rem;
      border: 1px solid var(--color-border-light);
      text-align: center;
    }
    .skeleton-stat-value {
      width: 50%;
      height: 28px;
      margin: 0 auto 0.75rem;
    }
    .skeleton-stat-label {
      width: 70%;
      height: 12px;
      margin: 0 auto;
    }

    /* chat-sidebar variant */
    .skeleton-chat-item {
      padding: 0.75rem 1rem;
      border-bottom: 1px solid var(--color-border-light);
    }
    .skeleton-chat-title {
      width: 65%;
      height: 14px;
    }
    .skeleton-chat-preview {
      width: 90%;
      height: 10px;
      margin-bottom: 0;
    }

    @keyframes shimmer {
      0% { background-position: 200% 0; }
      100% { background-position: -200% 0; }
    }
  `]
})
export class LoadingSkeletonComponent {
  count = input(3);
  variant = input<'default' | 'card-grid' | 'table' | 'stat' | 'chat-sidebar'>('default');
  skeletonItems = computed(() => Array.from({ length: this.count() }, (_, i) => i));
}
