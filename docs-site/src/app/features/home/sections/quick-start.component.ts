import { Component } from '@angular/core';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

interface Step {
  number: number;
  titleKey: string;
  descKey: string;
  code: string;
}

@Component({
  selector: 'app-quick-start',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    <section class="quickstart-section">
      <div class="section-container">
        <h2 class="section-title">{{ 'HOME.QUICKSTART.TITLE' | translate }}</h2>
        <p class="section-subtitle">{{ 'HOME.QUICKSTART.SUBTITLE' | translate }}</p>

        <div class="steps">
          @for (step of steps; track step.number) {
            <div class="step">
              <div class="step-header">
                <span class="step-number">{{ step.number }}</span>
                <div class="step-info">
                  <h3 class="step-title">{{ step.titleKey | translate }}</h3>
                  <p class="step-desc">{{ step.descKey | translate }}</p>
                </div>
              </div>
              <div class="step-code">
                <code>{{ step.code }}</code>
              </div>
            </div>
          }
        </div>
      </div>
    </section>
  `,
  styles: [`
    .quickstart-section {
      padding: 5rem 2rem;
      background: var(--lm-bg);
    }

    .section-container {
      max-width: 800px;
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

    .steps {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .step {
      background: var(--lm-bg-surface);
      border: 1px solid var(--lm-border, rgba(148, 163, 184, 0.15));
      border-radius: 14px;
      padding: 1.75rem;
      transition: border-color 0.3s ease;
    }

    .step:hover {
      border-color: rgba(59, 130, 246, 0.3);
    }

    .step-header {
      display: flex;
      align-items: flex-start;
      gap: 1rem;
      margin-bottom: 1.25rem;
    }

    .step-number {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      min-width: 36px;
      border-radius: 50%;
      background: linear-gradient(135deg, #3b82f6, #06b6d4);
      color: #fff;
      font-size: 0.95rem;
      font-weight: 700;
    }

    .step-info {
      flex: 1;
    }

    .step-title {
      font-size: 1.1rem;
      font-weight: 600;
      margin: 0 0 0.25rem;
      color: var(--lm-text);
    }

    .step-desc {
      font-size: 0.9rem;
      color: #94a3b8;
      margin: 0;
      line-height: 1.5;
    }

    .step-code {
      background: #1e293b;
      border-radius: 10px;
      padding: 1rem 1.25rem;
      overflow-x: auto;
    }

    .step-code code {
      font-family: 'JetBrains Mono', 'Fira Code', 'Cascadia Code', monospace;
      font-size: 0.85rem;
      line-height: 1.6;
      color: #e2e8f0;
      white-space: pre-wrap;
      word-break: break-all;
    }

    @media (max-width: 768px) {
      .quickstart-section {
        padding: 3.5rem 1.5rem;
      }

      .section-title {
        font-size: 1.8rem;
      }

      .step {
        padding: 1.25rem;
      }

      .step-code {
        padding: 0.75rem 1rem;
      }

      .step-code code {
        font-size: 0.8rem;
      }
    }

    @media (max-width: 480px) {
      .quickstart-section {
        padding: 2.5rem 1rem;
      }

      .section-title {
        font-size: 1.6rem;
      }

      .step-header {
        flex-direction: column;
        gap: 0.75rem;
      }
    }
  `]
})
export class QuickStartComponent {
  steps: Step[] = [
    {
      number: 1,
      titleKey: 'HOME.QUICKSTART.STEP1.TITLE',
      descKey: 'HOME.QUICKSTART.STEP1.DESC',
      code: 'git clone https://github.com/localmind/localmind.git && cp .env.example .env'
    },
    {
      number: 2,
      titleKey: 'HOME.QUICKSTART.STEP2.TITLE',
      descKey: 'HOME.QUICKSTART.STEP2.DESC',
      code: './scripts/start-backend.sh'
    },
    {
      number: 3,
      titleKey: 'HOME.QUICKSTART.STEP3.TITLE',
      descKey: 'HOME.QUICKSTART.STEP3.DESC',
      code: './scripts/start-frontend.sh'
    }
  ];
}
