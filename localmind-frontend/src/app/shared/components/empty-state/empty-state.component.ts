import { Component, input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="empty-state">
      <div class="empty-icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          @switch (icon()) {
            @case ('document') {
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            }
            @case ('search') {
              <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
            }
            @case ('folder') {
              <path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/>
            }
            @case ('chat') {
              <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
            }
            @default {
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            }
          }
        </svg>
      </div>
      <h3 class="empty-title">{{ title() }}</h3>
      @if (description()) {
        <p class="empty-desc">{{ description() }}</p>
      }
      <ng-content />
    </div>
  `,
  styles: [`
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem 1.5rem;
      text-align: center;
    }
    .empty-icon {
      color: var(--color-border);
      margin-bottom: 1rem;
    }
    .empty-title {
      margin: 0 0 0.5rem;
      font-size: 1.1rem;
      color: var(--color-text-secondary);
      font-weight: 600;
    }
    .empty-desc {
      margin: 0;
      font-size: 0.85rem;
      color: var(--color-text-muted);
      max-width: 300px;
    }
  `]
})
export class EmptyStateComponent {
  icon = input<string>('default');
  title = input.required<string>();
  description = input<string>('');
}
