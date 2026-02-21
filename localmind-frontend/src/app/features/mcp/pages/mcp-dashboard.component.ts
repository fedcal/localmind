import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-mcp-dashboard',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslatePipe],
  template: `
    <div class="mcp-dashboard">
      <h1>{{ 'MCP.TITLE' | translate }}</h1>
      <p class="subtitle">{{ 'MCP.SUBTITLE' | translate }}</p>

      <div class="tab-nav">
        <a routerLink="servers" routerLinkActive="active" class="tab">{{ 'MCP.TAB_SERVERS' | translate }}</a>
        <a routerLink="tools" routerLinkActive="active" class="tab">{{ 'MCP.TAB_TOOLS' | translate }}</a>
        <a routerLink="scrum" routerLinkActive="active" class="tab">{{ 'MCP.TAB_SCRUM' | translate }}</a>
        <a routerLink="incidents" routerLinkActive="active" class="tab">{{ 'MCP.TAB_INCIDENTS' | translate }}</a>
        <a routerLink="time" routerLinkActive="active" class="tab">{{ 'MCP.TAB_TIME' | translate }}</a>
      </div>

      <div class="tab-content">
        <router-outlet />
      </div>
    </div>
  `,
  styles: [`
    .mcp-dashboard { max-width: 1200px; }
    h1 { margin: 0 0 0.25rem 0; font-size: 1.75rem; color: var(--color-text); }
    .subtitle { color: var(--color-text-secondary); margin: 0 0 1.5rem 0; }
    .tab-nav { display: flex; gap: 0; border-bottom: 2px solid var(--color-border-light); margin-bottom: 1.5rem; }
    .tab {
      padding: 0.75rem 1.5rem;
      text-decoration: none;
      color: var(--color-text-secondary);
      border-bottom: 2px solid transparent;
      margin-bottom: -2px;
      transition: all 0.2s;
    }
    .tab:hover { color: var(--color-text); }
    .tab.active { color: var(--color-primary); border-bottom-color: var(--color-primary); font-weight: 600; }
  `]
})
export class McpDashboardComponent {}
