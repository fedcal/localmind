import { Component } from '@angular/core';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

interface FeatureCard {
  icon: string;
  titleKey: string;
  descKey: string;
}

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    <section class="features-section">
      <div class="section-container">
        <h2 class="section-title">{{ 'HOME.FEATURES.TITLE' | translate }}</h2>
        <p class="section-subtitle">{{ 'HOME.FEATURES.SUBTITLE' | translate }}</p>

        <div class="features-grid">
          @for (feature of features; track feature.titleKey) {
            <div class="feature-card">
              <span class="feature-icon">{{ feature.icon }}</span>
              <h3 class="feature-title">{{ feature.titleKey | translate }}</h3>
              <p class="feature-desc">{{ feature.descKey | translate }}</p>
            </div>
          }
        </div>
      </div>
    </section>
  `,
  styles: [`
    .features-section {
      padding: 5rem 2rem;
      background: var(--lm-bg-surface);
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

    .features-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1.5rem;
    }

    .feature-card {
      padding: 2rem;
      background: var(--lm-bg);
      border: 1px solid var(--lm-border, rgba(148, 163, 184, 0.15));
      border-radius: 14px;
      transition: all 0.3s ease;
    }

    .feature-card:hover {
      transform: translateY(-4px);
      border-color: #06b6d4;
      box-shadow: 0 8px 30px rgba(6, 182, 212, 0.12);
    }

    .feature-icon {
      display: inline-block;
      font-size: 2.2rem;
      margin-bottom: 1rem;
    }

    .feature-title {
      font-size: 1.15rem;
      font-weight: 600;
      margin: 0 0 0.5rem;
      color: var(--lm-text);
    }

    .feature-desc {
      font-size: 0.95rem;
      line-height: 1.6;
      color: #94a3b8;
      margin: 0;
    }

    @media (max-width: 996px) {
      .features-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }

    @media (max-width: 768px) {
      .features-section {
        padding: 3.5rem 1.5rem;
      }

      .features-grid {
        grid-template-columns: 1fr;
      }

      .section-title {
        font-size: 1.8rem;
      }
    }

    @media (max-width: 480px) {
      .features-section {
        padding: 2.5rem 1rem;
      }

      .section-title {
        font-size: 1.6rem;
      }

      .feature-card {
        padding: 1.5rem;
      }
    }
  `]
})
export class FeaturesComponent {
  features: FeatureCard[] = [
    {
      icon: '\u{1F916}',
      titleKey: 'HOME.FEATURES.CHAT.TITLE',
      descKey: 'HOME.FEATURES.CHAT.DESC'
    },
    {
      icon: '\u{1F50D}',
      titleKey: 'HOME.FEATURES.SEARCH.TITLE',
      descKey: 'HOME.FEATURES.SEARCH.DESC'
    },
    {
      icon: '\u{1F4C4}',
      titleKey: 'HOME.FEATURES.DOCUMENTS.TITLE',
      descKey: 'HOME.FEATURES.DOCUMENTS.DESC'
    },
    {
      icon: '\u{1F50C}',
      titleKey: 'HOME.FEATURES.MCP.TITLE',
      descKey: 'HOME.FEATURES.MCP.DESC'
    },
    {
      icon: '\u{1F512}',
      titleKey: 'HOME.FEATURES.SECURITY.TITLE',
      descKey: 'HOME.FEATURES.SECURITY.DESC'
    },
    {
      icon: '\u{1F514}',
      titleKey: 'HOME.FEATURES.WEBHOOKS.TITLE',
      descKey: 'HOME.FEATURES.WEBHOOKS.DESC'
    }
  ];
}
