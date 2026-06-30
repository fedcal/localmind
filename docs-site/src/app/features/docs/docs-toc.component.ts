import { Component, signal, inject, OnDestroy } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

interface TocItem {
  id: string;
  text: string;
  level: number;
}

@Component({
  selector: 'app-docs-toc',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    @if (items().length > 0) {
      <div class="toc">
        <h4 class="toc-title">{{ 'docs.toc.title' | translate }}</h4>
        <ul class="toc-list">
          @for (item of items(); track item.id) {
            <li [class.toc-h3]="item.level === 3">
              <a [href]="'#' + item.id" (click)="scrollTo($event, item.id)">{{ item.text }}</a>
            </li>
          }
        </ul>
      </div>
    }
  `,
  styles: [`
    .toc {
      padding-top: 1rem;
    }

    .toc-title {
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: var(--lm-text-muted, #94a3b8);
      margin: 0 0 1rem;
    }

    .toc-list {
      list-style: none;
      padding: 0;
      margin: 0;
      border-left: 1px solid var(--lm-border, rgba(148, 163, 184, 0.15));
    }

    .toc-list li {
      padding: 0;
    }

    .toc-list li a {
      display: block;
      padding: 0.3rem 0.75rem;
      font-size: 0.78rem;
      color: var(--lm-text-muted, #94a3b8);
      text-decoration: none;
      border-left: 2px solid transparent;
      margin-left: -1px;
      transition: all 0.2s ease;
      line-height: 1.4;
    }

    .toc-list li a:hover {
      color: var(--lm-primary, #60a5fa);
      border-left-color: var(--lm-primary, #60a5fa);
    }

    .toc-h3 a {
      padding-left: 1.5rem !important;
      font-size: 0.75rem !important;
    }
  `]
})
export class DocsTocComponent implements OnDestroy {
  private router = inject(Router);
  private sub: Subscription;

  items = signal<TocItem[]>([]);

  constructor() {
    this.sub = this.router.events.pipe(
      filter(e => e instanceof NavigationEnd)
    ).subscribe(() => {
      setTimeout(() => this.extractHeadings(), 100);
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  scrollTo(event: Event, id: string): void {
    event.preventDefault();
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  private extractHeadings(): void {
    const content = document.querySelector('.doc-content');
    if (!content) {
      this.items.set([]);
      return;
    }

    const headings = content.querySelectorAll('h2, h3');
    const tocItems: TocItem[] = [];

    headings.forEach((heading) => {
      const text = heading.textContent?.trim() || '';
      if (!text) return;

      let id = heading.id;
      if (!id) {
        id = this.githubSlugify(text);
        heading.id = id;
      }

      tocItems.push({
        id,
        text,
        level: heading.tagName === 'H3' ? 3 : 2
      });
    });

    this.items.set(tocItems);
  }

  private githubSlugify(text: string): string {
    return text
      .toLowerCase()
      .replace(/[^a-z0-9 -]/g, '')
      .replace(/\s+/g, '-')
      .replace(/-+/g, '-')
      .replace(/(^-|-$)/g, '');
  }
}
