import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

@Component({
  selector: 'app-not-found-page',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  template: `
    <div class="not-found">
      <div class="not-found-card">
        <div class="error-code">404</div>
        <h1>{{ 'NOT_FOUND.TITLE' | translate }}</h1>
        <p>{{ 'NOT_FOUND.DESC' | translate }}</p>
        <a routerLink="/" class="back-btn">{{ 'NOT_FOUND.BACK' | translate }}</a>
      </div>
    </div>
  `,
  styles: [`
    .not-found {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background: var(--color-bg, #f5f7fa);
    }
    .not-found-card {
      text-align: center;
      padding: 3rem;
    }
    .error-code {
      font-size: 6rem;
      font-weight: 800;
      color: #ddd;
      line-height: 1;
      margin-bottom: 0.5rem;
    }
    h1 {
      font-size: 1.5rem;
      color: #333;
      margin: 0 0 0.75rem;
    }
    p {
      color: #888;
      font-size: 0.95rem;
      margin: 0 0 2rem;
    }
    .back-btn {
      display: inline-block;
      padding: 0.75rem 2rem;
      background: linear-gradient(135deg, #0f3460, #3498db);
      color: white;
      text-decoration: none;
      border-radius: 8px;
      font-weight: 600;
      transition: opacity 0.2s;
    }
    .back-btn:hover { opacity: 0.9; }
  `]
})
export class NotFoundPageComponent {}
