import { Component, signal, HostListener, ElementRef, inject, OnDestroy } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { DocsSidebarComponent } from './docs-sidebar.component';
import { DocsTocComponent } from './docs-toc.component';

@Component({
  selector: 'app-docs-layout',
  standalone: true,
  imports: [RouterOutlet, DocsSidebarComponent, DocsTocComponent],
  template: `
    <div class="docs-layout">
      <button class="sidebar-toggle" (click)="toggleSidebar()" [class.active]="sidebarOpen()">
        <span></span><span></span><span></span>
      </button>

      @if (sidebarOpen()) {
        <div class="sidebar-overlay" (click)="sidebarOpen.set(false)"></div>
      }

      <aside class="docs-sidebar" [class.open]="sidebarOpen()">
        <app-docs-sidebar (docSelected)="sidebarOpen.set(false)" />
      </aside>

      <main class="docs-main">
        <router-outlet />
      </main>

      <aside class="docs-toc">
        <app-docs-toc />
      </aside>
    </div>
  `,
  styles: [`
    .docs-layout {
      display: flex;
      min-height: calc(100vh - var(--lm-header-height, 64px) - 200px);
      max-width: 1400px;
      margin: 0 auto;
      position: relative;
    }

    .docs-sidebar {
      width: var(--lm-sidebar-width, 280px);
      flex-shrink: 0;
      border-right: 1px solid var(--lm-border, rgba(148, 163, 184, 0.15));
      padding: 1.5rem 0;
      position: sticky;
      top: var(--lm-header-height, 64px);
      height: calc(100vh - var(--lm-header-height, 64px));
      overflow-y: auto;
    }

    .docs-main {
      flex: 1;
      min-width: 0;
      padding: 2rem 3rem;
      max-width: 850px;
    }

    .docs-toc {
      width: var(--lm-toc-width, 220px);
      flex-shrink: 0;
      padding: 1.5rem 1rem;
      position: sticky;
      top: var(--lm-header-height, 64px);
      height: calc(100vh - var(--lm-header-height, 64px));
      overflow-y: auto;
    }

    .sidebar-toggle {
      display: none;
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      z-index: 100;
      width: 48px;
      height: 48px;
      border-radius: 50%;
      border: none;
      background: linear-gradient(135deg, var(--lm-primary, #3b82f6), var(--lm-accent, #06b6d4));
      cursor: pointer;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 4px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    }

    .sidebar-toggle span {
      display: block;
      width: 20px;
      height: 2px;
      background: white;
      border-radius: 1px;
      transition: all 0.3s ease;
    }

    .sidebar-toggle.active span:nth-child(1) { transform: translateY(6px) rotate(45deg); }
    .sidebar-toggle.active span:nth-child(2) { opacity: 0; }
    .sidebar-toggle.active span:nth-child(3) { transform: translateY(-6px) rotate(-45deg); }

    .sidebar-overlay {
      display: none;
    }

    @media (max-width: 996px) {
      .docs-toc { display: none; }
      .docs-main { padding: 1.5rem 2rem; }
    }

    @media (max-width: 768px) {
      .sidebar-toggle { display: flex; }

      .docs-sidebar {
        position: fixed;
        left: 0;
        top: var(--lm-header-height, 64px);
        z-index: 90;
        background: var(--lm-bg, #0a0f1a);
        transform: translateX(-100%);
        transition: transform 0.3s ease;
        border-right: 1px solid var(--lm-border);
      }

      .docs-sidebar.open { transform: translateX(0); }

      .sidebar-overlay {
        display: block;
        position: fixed;
        inset: 0;
        top: var(--lm-header-height, 64px);
        z-index: 80;
        background: rgba(0, 0, 0, 0.5);
      }

      .docs-main { padding: 1.5rem; }
    }
  `]
})
export class DocsLayoutComponent implements OnDestroy {
  private el = inject(ElementRef);
  private router = inject(Router);
  private routerSub: Subscription;
  sidebarOpen = signal(false);

  constructor() {
    this.routerSub = this.router.events.pipe(
      filter(e => e instanceof NavigationEnd)
    ).subscribe(() => {
      // Scroll to top quando si cambia pagina docs
      window.scrollTo({ top: 0 });
    });
  }

  ngOnDestroy(): void {
    this.routerSub.unsubscribe();
  }

  toggleSidebar(): void {
    this.sidebarOpen.set(!this.sidebarOpen());
  }

  @HostListener('click', ['$event'])
  onContentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const anchor = target.closest('a');
    if (!anchor) return;

    const href = anchor.getAttribute('href');
    if (!href) return;

    // Link interni /docs/... : naviga via Router (necessario con HashLocation)
    if (href.startsWith('/docs/')) {
      event.preventDefault();
      const [path, fragment] = href.split('#');
      this.router.navigateByUrl(path).then(() => {
        if (fragment) {
          setTimeout(() => this.scrollToId(fragment), 150);
        }
      });
      return;
    }

    // Anchor link #... : scroll invece di navigazione
    if (href.startsWith('#')) {
      event.preventDefault();
      this.scrollToId(href.substring(1));
      return;
    }
  }

  private scrollToId(id: string): void {
    // Cerca prima per ID esistente
    let el = document.getElementById(id);

    // Se non trovato, cerca tra gli heading e assegna l'ID (stile GitHub)
    if (!el) {
      const content = this.el.nativeElement.querySelector('.docs-main');
      if (content) {
        const headings = content.querySelectorAll('h1, h2, h3, h4, h5, h6');
        for (const heading of headings) {
          const text = (heading as HTMLElement).textContent?.trim() || '';
          const generatedId = this.githubSlugify(text);
          if (!heading.id) {
            heading.id = generatedId;
          }
          if (heading.id === id || generatedId === id) {
            el = heading as HTMLElement;
            break;
          }
        }
      }
    }

    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
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
