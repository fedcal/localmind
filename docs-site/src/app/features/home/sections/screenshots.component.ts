import { Component } from '@angular/core';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

interface Screenshot {
  src: string;
  captionKey: string;
}

@Component({
  selector: 'app-screenshots',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    <section class="screenshots-section">
      <div class="section-container">
        <h2 class="section-title">{{ 'HOME.SCREENSHOTS.TITLE' | translate }}</h2>
        <p class="section-subtitle">{{ 'HOME.SCREENSHOTS.SUBTITLE' | translate }}</p>

        <div class="screenshots-grid">
          @for (shot of screenshots; track shot.src) {
            <div class="screenshot-card">
              <div class="screenshot-img-wrapper">
                <img [src]="shot.src" [alt]="shot.captionKey | translate" loading="lazy" />
              </div>
              <p class="screenshot-caption">{{ shot.captionKey | translate }}</p>
            </div>
          }
        </div>
      </div>
    </section>
  `,
  styles: [`
    .screenshots-section {
      padding: 5rem 2rem;
      background: var(--lm-bg);
    }

    .section-container {
      max-width: 1200px;
      margin: 0 auto;
    }

    .section-title {
      font-size: 2.2rem;
      font-weight: 700;
      text-align: center;
      margin: 0 0 0.75rem;
      color: var(--lm-text);
    }

    .section-subtitle {
      font-size: 1.1rem;
      color: #94a3b8;
      text-align: center;
      margin: 0 0 3rem;
      max-width: 600px;
      margin-left: auto;
      margin-right: auto;
    }

    .screenshots-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1.5rem;
    }

    .screenshot-card {
      border-radius: 14px;
      overflow: hidden;
      border: 1px solid var(--lm-border, rgba(148, 163, 184, 0.15));
      background: var(--lm-bg-surface);
      transition: all 0.3s ease;
    }

    .screenshot-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 36px rgba(0, 0, 0, 0.2);
    }

    .screenshot-img-wrapper {
      overflow: hidden;
      aspect-ratio: 16 / 10;
      background: #0f172a;
    }

    .screenshot-img-wrapper img {
      display: block;
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.4s ease;
    }

    .screenshot-card:hover .screenshot-img-wrapper img {
      transform: scale(1.03);
    }

    .screenshot-caption {
      padding: 1rem 1.25rem;
      margin: 0;
      font-size: 0.9rem;
      font-weight: 500;
      color: #94a3b8;
      text-align: center;
    }

    @media (max-width: 996px) {
      .screenshots-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }

    @media (max-width: 768px) {
      .screenshots-section {
        padding: 3.5rem 1.5rem;
      }

      .screenshots-grid {
        grid-template-columns: 1fr;
        max-width: 500px;
        margin-left: auto;
        margin-right: auto;
      }

      .section-title {
        font-size: 1.8rem;
      }
    }

    @media (max-width: 480px) {
      .screenshots-section {
        padding: 2.5rem 1rem;
      }

      .section-title {
        font-size: 1.6rem;
      }
    }
  `]
})
export class ScreenshotsComponent {
  screenshots: Screenshot[] = [
    {
      src: 'assets/img/screenshots/01.CreazionePassword.png',
      captionKey: 'HOME.SCREENSHOTS.PASSWORD'
    },
    {
      src: 'assets/img/screenshots/02-dashboard.png',
      captionKey: 'HOME.SCREENSHOTS.DASHBOARD'
    },
    {
      src: 'assets/img/screenshots/03-chatsection.png',
      captionKey: 'HOME.SCREENSHOTS.CHAT'
    },
    {
      src: 'assets/img/screenshots/04-documentisection.png',
      captionKey: 'HOME.SCREENSHOTS.DOCUMENTS'
    },
    {
      src: 'assets/img/screenshots/05-semanticSearch.png',
      captionKey: 'HOME.SCREENSHOTS.SEARCH'
    },
    {
      src: 'assets/img/screenshots/06-mcp.png',
      captionKey: 'HOME.SCREENSHOTS.MCP'
    }
  ];
}
