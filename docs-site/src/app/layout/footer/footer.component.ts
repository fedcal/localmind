import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  template: `
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-grid">
          <div class="footer-col">
            <h4 class="footer-heading">{{ 'FOOTER.DOCUMENTATION' | translate }}</h4>
            <ul class="footer-links">
              <li>
                <a routerLink="/docs/01-project-overview/vision-and-objectives">{{ 'FOOTER.GETTING_STARTED' | translate }}</a>
              </li>
              <li>
                <a routerLink="/docs/04-architecture/general-architecture">{{ 'FOOTER.ARCHITECTURE' | translate }}</a>
              </li>
              <li>
                <a routerLink="/docs/07-api-reference/api-overview">{{ 'FOOTER.API_REFERENCE' | translate }}</a>
              </li>
            </ul>
          </div>

          <div class="footer-col">
            <h4 class="footer-heading">{{ 'FOOTER.COMMUNITY' | translate }}</h4>
            <ul class="footer-links">
              <li>
                <a
                  href="https://github.com/fedcal/localmind"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  GitHub
                  <span class="external-icon">&#8599;</span>
                </a>
              </li>
              <li>
                <a
                  href="https://github.com/fedcal/localmind/issues"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  {{ 'FOOTER.ISSUES' | translate }}
                  <span class="external-icon">&#8599;</span>
                </a>
              </li>
            </ul>
          </div>

          <div class="footer-col">
            <h4 class="footer-heading">{{ 'FOOTER.MORE' | translate }}</h4>
            <ul class="footer-links">
              <li>
                <a
                  href="https://github.com/fedcal/localmind/blob/master/LICENSE"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  {{ 'FOOTER.LICENSE' | translate }}
                  <span class="external-icon">&#8599;</span>
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div class="footer-bottom">
          <p class="copyright">
            &copy; {{ currentYear }} LocalMind. {{ 'FOOTER.RIGHTS' | translate }}
          </p>
          <p class="created-by">
            {{ 'FOOTER.CREATED_BY' | translate }}
            <a href="https://www.federicocalo.dev" target="_blank" rel="noopener noreferrer">
              Federico Calo
            </a>
          </p>
        </div>
      </div>
    </footer>
  `,
  styles: [`
    .footer {
      background: var(--lm-footer-bg, #0c1222);
      color: var(--lm-footer-text, #94a3b8);
      border-top: 1px solid var(--lm-border, rgba(148, 163, 184, 0.1));
      margin-top: auto;
    }

    .footer-container {
      max-width: 1280px;
      margin: 0 auto;
      padding: 48px 24px 24px;
    }

    .footer-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 40px;
      margin-bottom: 40px;
    }

    .footer-heading {
      font-size: 0.8rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--lm-footer-heading, #e2e8f0);
      margin: 0 0 16px;
    }

    .footer-links {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .footer-links a {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      text-decoration: none;
      color: var(--lm-footer-text, #94a3b8);
      font-size: 0.875rem;
      transition: color 0.2s ease;
    }

    .footer-links a:hover {
      color: var(--lm-primary);
    }

    .external-icon {
      font-size: 0.7rem;
      opacity: 0.5;
    }

    .footer-bottom {
      padding-top: 24px;
      border-top: 1px solid rgba(148, 163, 184, 0.1);
      text-align: center;
    }

    .copyright {
      font-size: 0.8rem;
      margin: 0;
      color: var(--lm-footer-text, rgba(148, 163, 184, 0.6));
    }

    .created-by {
      font-size: 0.8rem;
      margin: 8px 0 0;
      color: var(--lm-footer-text, rgba(148, 163, 184, 0.6));
    }

    .created-by a {
      color: var(--lm-primary, #6366f1);
      text-decoration: none;
      font-weight: 500;
      transition: color 0.2s ease;
    }

    .created-by a:hover {
      color: var(--lm-primary-light, #818cf8);
      text-decoration: underline;
    }

    @media (max-width: 640px) {
      .footer-grid {
        grid-template-columns: 1fr;
        gap: 32px;
      }

      .footer-container {
        padding: 32px 20px 20px;
      }
    }
  `]
})
export class FooterComponent {
  currentYear = new Date().getFullYear();
}
