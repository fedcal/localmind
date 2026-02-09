import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../../core/services/api.service';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-dashboard-page',
  imports: [RouterLink, TranslatePipe],
  template: `
    <div class="dashboard">
      <div class="page-header">
        <h1>{{ 'DASHBOARD.TITLE' | translate }}</h1>
        <button class="btn btn-secondary btn-sm" (click)="refresh()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M23 4v6h-6M1 20v-6h6"/>
            <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
          </svg>
          {{ 'COMMON.REFRESH' | translate }}
        </button>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon api">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-label">{{ 'DASHBOARD.STAT_API' | translate }}</span>
            <span class="stat-value" [class]="apiStatus() === 'UP' ? 'success' : 'error'">
              {{ apiStatus() }}
            </span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon docs">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-label">{{ 'DASHBOARD.STAT_DOCUMENTS' | translate }}</span>
            <span class="stat-value">{{ documentCount() }}</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon mcp">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3"/>
              <path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-label">{{ 'DASHBOARD.STAT_MCP' | translate }}</span>
            <span class="stat-value">{{ mcpServerCount() }}</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon chat">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-label">{{ 'DASHBOARD.STAT_LLM' | translate }}</span>
            <span class="stat-value">{{ llmProvider() }}</span>
          </div>
        </div>
      </div>

      <div class="quick-actions">
        <h2>{{ 'DASHBOARD.QUICK_ACTIONS' | translate }}</h2>
        <div class="actions-grid">
          <a routerLink="/chat" class="action-card">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
            </svg>
            <span class="action-title">{{ 'DASHBOARD.ACTION_CHAT' | translate }}</span>
            <span class="action-desc">{{ 'DASHBOARD.ACTION_CHAT_DESC' | translate }}</span>
          </a>
          <a routerLink="/documents" class="action-card">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
            </svg>
            <span class="action-title">{{ 'DASHBOARD.ACTION_UPLOAD' | translate }}</span>
            <span class="action-desc">{{ 'DASHBOARD.ACTION_UPLOAD_DESC' | translate }}</span>
          </a>
          <a routerLink="/search" class="action-card">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
            </svg>
            <span class="action-title">{{ 'DASHBOARD.ACTION_SEARCH' | translate }}</span>
            <span class="action-desc">{{ 'DASHBOARD.ACTION_SEARCH_DESC' | translate }}</span>
          </a>
          <a routerLink="/mcp" class="action-card">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3"/>
              <path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4"/>
            </svg>
            <span class="action-title">{{ 'DASHBOARD.ACTION_MCP' | translate }}</span>
            <span class="action-desc">{{ 'DASHBOARD.ACTION_MCP_DESC' | translate }}</span>
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard { max-width: 1100px; }
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1rem;
      margin-bottom: 2.5rem;
    }
    .stat-card {
      background: white;
      padding: 1.25rem;
      border-radius: 10px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08);
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .stat-icon {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .stat-icon.api { background: #e8f5e9; color: #2e7d32; }
    .stat-icon.docs { background: #e3f2fd; color: #1565c0; }
    .stat-icon.mcp { background: #f3e5f5; color: #7b1fa2; }
    .stat-icon.chat { background: #fff3e0; color: #e65100; }
    .stat-content { display: flex; flex-direction: column; }
    .stat-label { font-size: 0.8rem; color: #888; }
    .stat-value { font-size: 1.35rem; font-weight: 700; color: #1a1a2e; }
    .stat-value.success { color: #2e7d32; }
    .stat-value.error { color: #c62828; }

    .quick-actions h2 { font-size: 1.15rem; margin-bottom: 1rem; color: #1a1a2e; }
    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1rem;
    }
    .action-card {
      background: white;
      padding: 1.25rem;
      border-radius: 10px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08);
      text-decoration: none;
      color: inherit;
      transition: all 0.2s;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      border: 1px solid transparent;
    }
    .action-card:hover {
      border-color: #0f3460;
      box-shadow: 0 4px 12px rgba(15,52,96,0.12);
      transform: translateY(-1px);
    }
    .action-card svg { color: #0f3460; }
    .action-title { font-weight: 600; font-size: 0.95rem; }
    .action-desc { font-size: 0.8rem; color: #888; line-height: 1.3; }
  `]
})
export class DashboardPageComponent implements OnInit {
  private api = inject(ApiService);

  apiStatus = signal('...');
  documentCount = signal(0);
  mcpServerCount = signal(0);
  llmProvider = signal('Ollama');

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.api.get<{ status: string }>('/dashboard/health').subscribe({
      next: (res) => this.apiStatus.set(res.status ?? 'UP'),
      error: () => this.apiStatus.set('DOWN')
    });

    this.api.get<any[]>('/documents').subscribe({
      next: (docs) => this.documentCount.set(docs?.length ?? 0),
      error: () => this.documentCount.set(0)
    });

    this.api.get<any[]>('/mcp/servers').subscribe({
      next: (servers) => this.mcpServerCount.set(servers?.length ?? 0),
      error: () => this.mcpServerCount.set(0)
    });
  }
}
