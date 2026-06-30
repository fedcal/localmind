import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  template: `
    <section class="hero">
      <div class="hero-glow"></div>
      <div class="hero-glow hero-glow--secondary"></div>

      <div class="hero-content">
        <span class="badge">
          <span class="badge-dot"></span>
          v1.0.0 &mdash; GA Release
        </span>

        <h1 class="hero-title">LocalMind</h1>
        <p class="hero-subtitle">{{ 'HOME.HERO.SUBTITLE' | translate }}</p>

        <div class="hero-actions">
          <a class="btn btn-primary" [routerLink]="['/docs/01-project-overview/vision-and-objectives']">
            {{ 'HOME.HERO.GET_STARTED' | translate }}
          </a>
          <a class="btn btn-outline" [routerLink]="['/docs']">
            {{ 'HOME.HERO.READ_DOCS' | translate }}
          </a>
          <a
            class="btn btn-outline"
            href="https://github.com/fedcal/localmind"
            target="_blank"
            rel="noopener noreferrer"
          >
            <svg class="github-icon" viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
              <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205
                11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555
                -3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02
                -.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305
                3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925
                0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315
                3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23
                3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0
                4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015
                2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63
                -5.37-12-12-12z"/>
            </svg>
            GitHub
          </a>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .hero {
      position: relative;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 85vh;
      padding: 6rem 2rem 4rem;
      background: linear-gradient(135deg, #0a0f1a 0%, #111827 50%, #0a0f1a 100%);
    }

    .hero-glow {
      position: absolute;
      top: 20%;
      left: 50%;
      width: 700px;
      height: 700px;
      transform: translate(-50%, -30%);
      background: radial-gradient(circle, rgba(59, 130, 246, 0.15) 0%, transparent 70%);
      pointer-events: none;
      animation: glowPulse 6s ease-in-out infinite;
    }

    .hero-glow--secondary {
      top: 40%;
      width: 500px;
      height: 500px;
      background: radial-gradient(circle, rgba(6, 182, 212, 0.1) 0%, transparent 70%);
      animation: glowPulse 8s ease-in-out infinite reverse;
    }

    @keyframes glowPulse {
      0%, 100% {
        opacity: 0.6;
        transform: translate(-50%, -30%) scale(1);
      }
      50% {
        opacity: 1;
        transform: translate(-50%, -30%) scale(1.15);
      }
    }

    .hero-content {
      position: relative;
      z-index: 1;
      text-align: center;
      max-width: 800px;
    }

    .badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 6px 16px;
      margin-bottom: 2rem;
      font-size: 0.85rem;
      font-weight: 500;
      color: #94a3b8;
      background: rgba(59, 130, 246, 0.08);
      border: 1px solid rgba(59, 130, 246, 0.2);
      border-radius: 9999px;
      letter-spacing: 0.02em;
    }

    .badge-dot {
      display: inline-block;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #3b82f6;
      animation: badgePulse 2s ease-in-out infinite;
    }

    @keyframes badgePulse {
      0%, 100% {
        box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.4);
      }
      50% {
        box-shadow: 0 0 0 6px rgba(59, 130, 246, 0);
      }
    }

    .hero-title {
      font-size: 4.5rem;
      font-weight: 800;
      line-height: 1.1;
      margin: 0 0 1.5rem;
      background: linear-gradient(135deg, #ffffff 0%, #60a5fa 50%, #22d3ee 100%);
      background-clip: text;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      letter-spacing: -0.03em;
    }

    .hero-subtitle {
      font-size: 1.25rem;
      line-height: 1.7;
      color: #94a3b8;
      margin: 0 0 2.5rem;
      max-width: 600px;
      margin-left: auto;
      margin-right: auto;
    }

    .hero-actions {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .btn {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 12px 28px;
      font-size: 0.95rem;
      font-weight: 600;
      border-radius: 10px;
      text-decoration: none;
      cursor: pointer;
      transition: all 0.25s ease;
      border: none;
    }

    .btn-primary {
      background: linear-gradient(135deg, #3b82f6, #06b6d4);
      color: #fff;
      box-shadow: 0 4px 20px rgba(59, 130, 246, 0.3);
    }

    .btn-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 30px rgba(59, 130, 246, 0.45);
    }

    .btn-outline {
      background: transparent;
      color: #e2e8f0;
      border: 1px solid rgba(148, 163, 184, 0.3);
    }

    .btn-outline:hover {
      border-color: #3b82f6;
      color: #60a5fa;
      background: rgba(59, 130, 246, 0.05);
      transform: translateY(-2px);
    }

    .github-icon {
      width: 18px;
      height: 18px;
      flex-shrink: 0;
    }

    @media (max-width: 768px) {
      .hero {
        min-height: 70vh;
        padding: 5rem 1.5rem 3rem;
      }

      .hero-title {
        font-size: 3rem;
      }

      .hero-subtitle {
        font-size: 1.1rem;
      }

      .hero-actions {
        flex-direction: column;
      }

      .btn {
        width: 100%;
        justify-content: center;
        max-width: 280px;
      }
    }

    @media (max-width: 480px) {
      .hero-title {
        font-size: 2.4rem;
      }

      .hero-subtitle {
        font-size: 1rem;
      }
    }
  `]
})
export class HeroComponent {}
