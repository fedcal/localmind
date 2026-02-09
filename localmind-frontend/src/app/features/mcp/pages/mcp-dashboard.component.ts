import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-mcp-dashboard',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="mcp-dashboard">
      <h1>MCP Integration</h1>
      <p class="subtitle">Model Context Protocol - Gestione Server e Tool</p>

      <div class="tab-nav">
        <a routerLink="servers" routerLinkActive="active" class="tab">Server Esterni</a>
        <a routerLink="tools" routerLinkActive="active" class="tab">Tool Disponibili</a>
      </div>

      <div class="tab-content">
        <router-outlet />
      </div>
    </div>
  `,
  styles: [`
    .mcp-dashboard { max-width: 1200px; }
    h1 { margin: 0 0 0.25rem 0; font-size: 1.75rem; color: #1a1a2e; }
    .subtitle { color: #666; margin: 0 0 1.5rem 0; }
    .tab-nav { display: flex; gap: 0; border-bottom: 2px solid #e0e0e0; margin-bottom: 1.5rem; }
    .tab {
      padding: 0.75rem 1.5rem;
      text-decoration: none;
      color: #666;
      border-bottom: 2px solid transparent;
      margin-bottom: -2px;
      transition: all 0.2s;
    }
    .tab:hover { color: #1a1a2e; }
    .tab.active { color: #0f3460; border-bottom-color: #0f3460; font-weight: 600; }
  `]
})
export class McpDashboardComponent {}
