import { Component } from '@angular/core';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

interface TechItem {
  icon: string;
  name: string;
  version: string;
}

@Component({
  selector: 'app-tech-stack',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    <section class="tech-section">
      <div class="section-container">
        <h2 class="section-title">{{ 'HOME.TECH.TITLE' | translate }}</h2>
        <p class="section-subtitle">{{ 'HOME.TECH.SUBTITLE' | translate }}</p>

        <div class="tech-grid">
          @for (tech of techItems; track tech.name) {
            <div class="tech-item">
              <span class="tech-icon">{{ tech.icon }}</span>
              <span class="tech-name">{{ tech.name }}</span>
              <span class="tech-version">{{ tech.version }}</span>
            </div>
          }
        </div>
      </div>
    </section>
  `,
  styles: [`
    .tech-section {
      padding: 5rem 2rem;
      background: var(--lm-bg-surface);
    }

    .section-container {
      max-width: 1000px;
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

    .tech-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1.25rem;
    }

    .tech-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
      padding: 1.75rem 1rem;
      background: var(--lm-bg);
      border: 1px solid var(--lm-border, rgba(148, 163, 184, 0.15));
      border-radius: 12px;
      transition: all 0.3s ease;
    }

    .tech-item:hover {
      border-color: rgba(59, 130, 246, 0.3);
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(59, 130, 246, 0.08);
    }

    .tech-icon {
      font-size: 2rem;
    }

    .tech-name {
      font-size: 0.95rem;
      font-weight: 600;
      color: var(--lm-text);
      text-align: center;
    }

    .tech-version {
      font-size: 0.8rem;
      font-weight: 500;
      color: #64748b;
      background: rgba(59, 130, 246, 0.08);
      padding: 2px 10px;
      border-radius: 9999px;
    }

    @media (max-width: 996px) {
      .tech-grid {
        grid-template-columns: repeat(3, 1fr);
      }
    }

    @media (max-width: 768px) {
      .tech-section {
        padding: 3.5rem 1.5rem;
      }

      .tech-grid {
        grid-template-columns: repeat(2, 1fr);
      }

      .section-title {
        font-size: 1.8rem;
      }
    }

    @media (max-width: 480px) {
      .tech-section {
        padding: 2.5rem 1rem;
      }

      .section-title {
        font-size: 1.6rem;
      }

      .tech-item {
        padding: 1.25rem 0.75rem;
      }
    }
  `]
})
export class TechStackComponent {
  techItems: TechItem[] = [
    { icon: '\u{1F343}', name: 'Spring Boot', version: '3.4.2' },
    { icon: '\u{1F9E0}', name: 'Spring AI', version: '1.0.0' },
    { icon: '\u{1F170}\u{FE0F}', name: 'Angular', version: '21' },
    { icon: '\u{2615}', name: 'Java', version: '17' },
    { icon: '\u{1F42C}', name: 'MySQL', version: '8.0' },
    { icon: '\u{1F4D0}', name: 'Qdrant', version: 'Latest' },
    { icon: '\u{1F999}', name: 'Ollama', version: 'Latest' },
    { icon: '\u{2708}\u{FE0F}', name: 'Flyway', version: '10.x' }
  ];
}
