import { Component, signal, computed, output, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslationService } from '../../core/i18n/translation.service';
import { SIDEBAR_DATA, SidebarCategory } from './sidebar-data';

@Component({
  selector: 'app-docs-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="sidebar-nav">
      @for (category of categories; track category.id) {
        <div class="sidebar-category">
          <button class="category-header" (click)="toggleCategory(category.id)" [class.expanded]="expandedCategories().has(category.id)">
            <span class="category-arrow">&#9656;</span>
            <span class="category-label">{{ getCategoryLabel(category) }}</span>
          </button>
          @if (expandedCategories().has(category.id)) {
            <ul class="category-docs">
              @for (doc of category.docs; track doc.slug) {
                <li>
                  <a [routerLink]="doc.path"
                     routerLinkActive="active"
                     (click)="docSelected.emit()">
                    {{ getDocLabel(doc) }}
                  </a>
                </li>
              }
            </ul>
          }
        </div>
      }
    </nav>
  `,
  styles: [`
    .sidebar-nav {
      padding: 0 1rem;
    }

    .sidebar-category {
      margin-bottom: 0.25rem;
    }

    .category-header {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      width: 100%;
      padding: 0.5rem 0.75rem;
      border: none;
      background: transparent;
      color: var(--lm-text, #e2e8f0);
      font-size: 0.85rem;
      font-weight: 600;
      cursor: pointer;
      border-radius: 0.375rem;
      transition: background 0.2s ease;
      text-align: left;
    }

    .category-header:hover {
      background: var(--lm-bg-surface, rgba(148, 163, 184, 0.08));
    }

    .category-arrow {
      font-size: 0.7rem;
      transition: transform 0.2s ease;
      color: var(--lm-text-muted, #94a3b8);
    }

    .category-header.expanded .category-arrow {
      transform: rotate(90deg);
    }

    .category-docs {
      list-style: none;
      padding: 0;
      margin: 0.25rem 0 0.5rem 1.25rem;
    }

    .category-docs li a {
      display: block;
      padding: 0.35rem 0.75rem;
      font-size: 0.8rem;
      color: var(--lm-text-muted, #94a3b8);
      text-decoration: none;
      border-radius: 0.25rem;
      border-left: 2px solid transparent;
      transition: all 0.2s ease;
    }

    .category-docs li a:hover {
      color: var(--lm-text, #e2e8f0);
      background: var(--lm-bg-surface, rgba(148, 163, 184, 0.05));
    }

    .category-docs li a.active {
      color: var(--lm-primary, #60a5fa);
      background: rgba(59, 130, 246, 0.08);
      border-left-color: var(--lm-primary, #60a5fa);
      font-weight: 500;
    }
  `]
})
export class DocsSidebarComponent {
  private i18n = inject(TranslationService);
  docSelected = output<void>();
  categories: SidebarCategory[] = SIDEBAR_DATA;

  expandedCategories = signal<Set<string>>(new Set(this.categories.map(c => c.id)));

  toggleCategory(id: string): void {
    const current = new Set(this.expandedCategories());
    if (current.has(id)) {
      current.delete(id);
    } else {
      current.add(id);
    }
    this.expandedCategories.set(current);
  }

  getCategoryLabel(category: SidebarCategory): string {
    const lang = this.i18n.currentLang();
    return category.labels?.[lang] ?? category.labels?.['en'] ?? category.id;
  }

  getDocLabel(doc: { labels?: Record<string, string>; slug: string }): string {
    const lang = this.i18n.currentLang();
    return doc.labels?.[lang] ?? doc.labels?.['en'] ?? doc.slug;
  }
}
