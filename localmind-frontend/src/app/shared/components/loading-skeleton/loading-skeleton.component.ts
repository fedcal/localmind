import { Component, input, computed } from '@angular/core';

@Component({
  selector: 'app-loading-skeleton',
  standalone: true,
  template: `
    <div class="skeleton-container">
      @for (item of skeletonItems(); track $index) {
        <div class="skeleton-card">
          <div class="skeleton-line skeleton-title"></div>
          <div class="skeleton-line skeleton-text"></div>
          <div class="skeleton-line skeleton-text short"></div>
        </div>
      }
    </div>
  `,
  styles: [`
    .skeleton-container {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    .skeleton-card {
      background: white;
      border-radius: 12px;
      padding: 1.25rem;
      border: 1px solid #eee;
    }
    .skeleton-line {
      height: 14px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
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
    @keyframes shimmer {
      0% { background-position: 200% 0; }
      100% { background-position: -200% 0; }
    }
  `]
})
export class LoadingSkeletonComponent {
  count = input(3);
  skeletonItems = computed(() => Array.from({ length: this.count() }, (_, i) => i));
}
