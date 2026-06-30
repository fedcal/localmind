import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

export interface DocNavLink {
  path: string;
  label: string;
}

@Component({
  selector: 'app-doc-nav',
  standalone: true,
  imports: [RouterLink],
  template: `
    <nav class="doc-nav" aria-label="Documentation navigation">
      <div class="nav-link-wrapper prev-wrapper">
        @if (prev(); as p) {
          <a class="nav-link prev" [routerLink]="p.path">
            <span class="direction">&larr; Previous</span>
            <span class="label">{{ p.label }}</span>
          </a>
        }
      </div>

      <div class="nav-link-wrapper next-wrapper">
        @if (next(); as n) {
          <a class="nav-link next" [routerLink]="n.path">
            <span class="direction">Next &rarr;</span>
            <span class="label">{{ n.label }}</span>
          </a>
        }
      </div>
    </nav>
  `,
  styles: [`
    .doc-nav {
      display: flex;
      justify-content: space-between;
      gap: 16px;
      margin-top: 48px;
      padding-top: 24px;
      border-top: 1px solid var(--lm-border, rgba(148, 163, 184, 0.2));
    }

    .nav-link-wrapper {
      flex: 1;
      min-width: 0;
    }

    .next-wrapper {
      text-align: right;
    }

    .nav-link {
      display: inline-flex;
      flex-direction: column;
      gap: 4px;
      padding: 14px 18px;
      border: 1px solid var(--lm-border, rgba(148, 163, 184, 0.2));
      border-radius: 10px;
      text-decoration: none;
      color: var(--lm-text);
      transition: all 0.2s ease;
      max-width: 100%;
    }

    .nav-link:hover {
      border-color: var(--lm-primary);
      background: rgba(59, 130, 246, 0.05);
    }

    .nav-link.next {
      align-items: flex-end;
      margin-left: auto;
    }

    .nav-link.prev {
      align-items: flex-start;
    }

    .direction {
      font-size: 0.75rem;
      font-weight: 500;
      color: var(--lm-primary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .label {
      font-size: 0.95rem;
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 280px;
    }

    @media (max-width: 640px) {
      .doc-nav {
        flex-direction: column;
      }

      .nav-link {
        width: 100%;
      }

      .nav-link.next {
        align-items: flex-end;
      }

      .label {
        max-width: 100%;
      }
    }
  `]
})
export class DocNavComponent {
  prev = input<DocNavLink | null>(null);
  next = input<DocNavLink | null>(null);
}
