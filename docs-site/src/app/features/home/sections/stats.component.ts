import { Component } from '@angular/core';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

interface StatItem {
  value: string;
  labelKey: string;
}

@Component({
  selector: 'app-stats',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    <section class="stats-section">
      <div class="stats-container">
        @for (stat of stats; track stat.labelKey) {
          <div class="stat-item">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.labelKey | translate }}</span>
          </div>
        }
      </div>
    </section>
  `,
  styles: [`
    .stats-section {
      padding: 4rem 2rem;
      background: var(--lm-bg);
      border-top: 1px solid var(--lm-border, rgba(148, 163, 184, 0.1));
      border-bottom: 1px solid var(--lm-border, rgba(148, 163, 184, 0.1));
    }

    .stats-container {
      max-width: 1000px;
      margin: 0 auto;
      display: flex;
      justify-content: space-around;
      align-items: center;
      gap: 2rem;
      padding: 2.5rem 3rem;
      background: linear-gradient(
        135deg,
        rgba(59, 130, 246, 0.04) 0%,
        rgba(6, 182, 212, 0.04) 100%
      );
      border: 1px solid var(--lm-border, rgba(148, 163, 184, 0.12));
      border-radius: 16px;
    }

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
    }

    .stat-value {
      font-size: 2.8rem;
      font-weight: 800;
      line-height: 1;
      background: linear-gradient(135deg, #3b82f6, #06b6d4);
      background-clip: text;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      letter-spacing: -0.02em;
    }

    .stat-label {
      font-size: 0.9rem;
      font-weight: 500;
      color: #94a3b8;
      text-align: center;
      white-space: nowrap;
    }

    @media (max-width: 768px) {
      .stats-container {
        flex-wrap: wrap;
        padding: 2rem 1.5rem;
        gap: 1.5rem;
      }

      .stat-item {
        flex: 1 1 40%;
      }

      .stat-value {
        font-size: 2.2rem;
      }
    }

    @media (max-width: 480px) {
      .stats-section {
        padding: 2.5rem 1rem;
      }

      .stats-container {
        flex-direction: column;
        padding: 1.5rem;
        gap: 1.5rem;
      }

      .stat-item {
        flex: 1 1 100%;
      }

      .stat-value {
        font-size: 2rem;
      }
    }
  `]
})
export class StatsComponent {
  stats: StatItem[] = [
    { value: '1443', labelKey: 'HOME.STATS.BACKEND_TESTS' },
    { value: '67', labelKey: 'HOME.STATS.E2E_TESTS' },
    { value: '132+', labelKey: 'HOME.STATS.MCP_TOOLS' },
    { value: '4', labelKey: 'HOME.STATS.LLM_PROVIDERS' }
  ];
}
