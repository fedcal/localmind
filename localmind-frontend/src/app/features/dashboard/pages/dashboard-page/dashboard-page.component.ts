import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { ApiService } from '../../../../core/services/api.service';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';

interface AnalyticsData {
  totalConversations: number;
  totalDocuments: number;
  totalTokens: number;
  totalCost: number;
  avgResponseTimeMs: number;
  tokensByProvider: Record<string, number>;
  costByProvider: Record<string, number>;
  documentsByStatus: Record<string, number>;
  usageTimeline: { date: string; tokens: number; cost: number }[];
  topModels: { model: string; provider: string; totalTokens: number; requestCount: number }[];
}

@Component({
  selector: 'app-dashboard-page',
  imports: [RouterLink, TranslatePipe, NgClass],
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

        <div class="stat-card clickable" (click)="goToSettings()">
          <div class="stat-icon ollama">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="4" y="4" width="16" height="16" rx="2"/>
              <path d="M9 9h6M9 13h6M9 17h4"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-label">{{ 'DASHBOARD.STAT_OLLAMA' | translate }}</span>
            <span class="stat-value" [class]="ollamaStatus() === 'UP' ? 'success' : ollamaStatus() === 'DOWN' ? 'error' : ''">
              {{ ollamaStatus() }}
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

      <!-- Analytics Section -->
      @if (analytics()) {
        <div class="analytics-section">
          <div class="analytics-header">
            <h2>{{ 'DASHBOARD.ANALYTICS_TITLE' | translate }}</h2>
            <div class="period-selector">
              <button class="btn btn-sm"
                      [ngClass]="selectedPeriod() === 7 ? 'btn-primary' : 'btn-secondary'"
                      (click)="loadAnalytics(7)">
                {{ 'DASHBOARD.PERIOD_7D' | translate }}
              </button>
              <button class="btn btn-sm"
                      [ngClass]="selectedPeriod() === 30 ? 'btn-primary' : 'btn-secondary'"
                      (click)="loadAnalytics(30)">
                {{ 'DASHBOARD.PERIOD_30D' | translate }}
              </button>
              <button class="btn btn-sm"
                      [ngClass]="selectedPeriod() === 90 ? 'btn-primary' : 'btn-secondary'"
                      (click)="loadAnalytics(90)">
                {{ 'DASHBOARD.PERIOD_90D' | translate }}
              </button>
            </div>
          </div>

          <div class="analytics-stats-grid">
            <div class="analytics-card">
              <span class="analytics-label">{{ 'DASHBOARD.TOTAL_TOKENS' | translate }}</span>
              <span class="analytics-value">{{ formatNumber(analytics()!.totalTokens) }}</span>
            </div>
            <div class="analytics-card">
              <span class="analytics-label">{{ 'DASHBOARD.TOTAL_COST' | translate }}</span>
              <span class="analytics-value">\${{ analytics()!.totalCost.toFixed(4) }}</span>
            </div>
            <div class="analytics-card">
              <span class="analytics-label">{{ 'DASHBOARD.AVG_RESPONSE' | translate }}</span>
              <span class="analytics-value">{{ analytics()!.avgResponseTimeMs.toFixed(0) }} ms</span>
            </div>
            <div class="analytics-card">
              <span class="analytics-label">{{ 'DASHBOARD.TOTAL_CONVERSATIONS' | translate }}</span>
              <span class="analytics-value">{{ analytics()!.totalConversations }}</span>
            </div>
            <div class="analytics-card">
              <span class="analytics-label">{{ 'DASHBOARD.TOTAL_DOCUMENTS' | translate }}</span>
              <span class="analytics-value">{{ analytics()!.totalDocuments }}</span>
            </div>
          </div>

          <!-- Tokens by Provider -->
          @if (providerEntries().length > 0) {
            <div class="analytics-panel">
              <h3>{{ 'DASHBOARD.TOKENS_BY_PROVIDER' | translate }}</h3>
              <div class="bar-chart">
                @for (entry of providerEntries(); track entry.name) {
                  <div class="bar-row">
                    <span class="bar-label">{{ entry.name }}</span>
                    <div class="bar-track">
                      <div class="bar-fill" [style.width.%]="entry.percent"></div>
                    </div>
                    <span class="bar-value">{{ formatNumber(entry.value) }}</span>
                  </div>
                }
              </div>
            </div>
          }

          <!-- Cost by Provider -->
          @if (costEntries().length > 0) {
            <div class="analytics-panel">
              <h3>{{ 'DASHBOARD.COST_BY_PROVIDER' | translate }}</h3>
              <table class="analytics-table">
                <thead>
                  <tr>
                    <th>{{ 'DASHBOARD.PROVIDER' | translate }}</th>
                    <th>{{ 'DASHBOARD.COST' | translate }}</th>
                  </tr>
                </thead>
                <tbody>
                  @for (entry of costEntries(); track entry.name) {
                    <tr>
                      <td>{{ entry.name }}</td>
                      <td>\${{ entry.value.toFixed(4) }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }

          <!-- Top Models -->
          @if (analytics()!.topModels && analytics()!.topModels.length > 0) {
            <div class="analytics-panel">
              <h3>{{ 'DASHBOARD.TOP_MODELS' | translate }}</h3>
              <table class="analytics-table">
                <thead>
                  <tr>
                    <th>{{ 'DASHBOARD.MODEL' | translate }}</th>
                    <th>{{ 'DASHBOARD.PROVIDER' | translate }}</th>
                    <th>{{ 'DASHBOARD.TOKENS' | translate }}</th>
                    <th>{{ 'DASHBOARD.REQUESTS' | translate }}</th>
                  </tr>
                </thead>
                <tbody>
                  @for (model of analytics()!.topModels; track model.model) {
                    <tr>
                      <td class="mono">{{ model.model }}</td>
                      <td>{{ model.provider }}</td>
                      <td>{{ formatNumber(model.totalTokens) }}</td>
                      <td>{{ model.requestCount }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>
      }

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
      background: var(--color-card-bg);
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
    .stat-icon.api { background: var(--color-success-bg); color: var(--color-success-text); }
    .stat-icon.docs { background: var(--color-info-bg); color: var(--color-info-text); }
    .stat-icon.mcp { background: var(--color-msg-tool-bg); color: #7b1fa2; }
    .stat-icon.ollama { background: var(--color-success-bg); color: var(--color-success-text); }
    .stat-icon.chat { background: #fff3e0; color: #e65100; }
    .stat-card.clickable { cursor: pointer; transition: all 0.2s; border: 1px solid transparent; }
    .stat-card.clickable:hover { border-color: var(--color-primary); box-shadow: 0 4px 12px rgba(15,52,96,0.12); }
    .stat-content { display: flex; flex-direction: column; }
    .stat-label { font-size: 0.8rem; color: var(--color-text-muted); }
    .stat-value { font-size: 1.35rem; font-weight: 700; color: var(--color-text); }
    .stat-value.success { color: var(--color-success-text); }
    .stat-value.error { color: var(--color-error-text); }

    /* Analytics Section */
    .analytics-section { margin-bottom: 2.5rem; }
    .analytics-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 1.25rem;
    }
    .analytics-header h2 { font-size: 1.15rem; color: var(--color-text); margin: 0; }
    .period-selector { display: flex; gap: 0.5rem; }
    .analytics-stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }
    .analytics-card {
      background: var(--color-card-bg);
      padding: 1rem 1.25rem;
      border-radius: 10px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08);
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
    }
    .analytics-label { font-size: 0.78rem; color: var(--color-text-muted); text-transform: uppercase; letter-spacing: 0.3px; }
    .analytics-value { font-size: 1.5rem; font-weight: 700; color: var(--color-text); }

    .analytics-panel {
      background: var(--color-card-bg);
      padding: 1.25rem;
      border-radius: 10px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08);
      margin-bottom: 1rem;
    }
    .analytics-panel h3 { font-size: 0.95rem; margin: 0 0 1rem 0; color: var(--color-text); }

    /* Bar chart */
    .bar-chart { display: flex; flex-direction: column; gap: 0.6rem; }
    .bar-row { display: flex; align-items: center; gap: 0.75rem; }
    .bar-label { width: 90px; font-size: 0.82rem; font-weight: 600; color: var(--color-text); text-align: right; flex-shrink: 0; }
    .bar-track { flex: 1; height: 22px; background: var(--color-bg); border-radius: 6px; overflow: hidden; }
    .bar-fill { height: 100%; background: var(--color-primary); border-radius: 6px; transition: width 0.4s ease; min-width: 2px; }
    .bar-value { width: 80px; font-size: 0.82rem; color: var(--color-text-muted); font-weight: 600; }

    /* Table */
    .analytics-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; }
    .analytics-table th {
      text-align: left;
      padding: 0.6rem 0.75rem;
      border-bottom: 2px solid var(--color-border);
      color: var(--color-text-muted);
      font-weight: 600;
      text-transform: uppercase;
      font-size: 0.75rem;
      letter-spacing: 0.3px;
    }
    .analytics-table td {
      padding: 0.6rem 0.75rem;
      border-bottom: 1px solid var(--color-border);
      color: var(--color-text);
    }
    .analytics-table .mono { font-family: 'JetBrains Mono', monospace; font-size: 0.82rem; }

    .quick-actions h2 { font-size: 1.15rem; margin-bottom: 1rem; color: var(--color-text); }
    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1rem;
    }
    .action-card {
      background: var(--color-card-bg);
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
      border-color: var(--color-primary);
      box-shadow: 0 4px 12px rgba(15,52,96,0.12);
      transform: translateY(-1px);
    }
    .action-card svg { color: var(--color-primary); }
    .action-title { font-weight: 600; font-size: 0.95rem; }
    .action-desc { font-size: 0.8rem; color: var(--color-text-muted); line-height: 1.3; }
  `]
})
export class DashboardPageComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);

  apiStatus = signal('...');
  ollamaStatus = signal('...');
  documentCount = signal(0);
  mcpServerCount = signal(0);
  llmProvider = signal('Ollama');
  analytics = signal<AnalyticsData | null>(null);
  selectedPeriod = signal(30);
  providerEntries = signal<{ name: string; value: number; percent: number }[]>([]);
  costEntries = signal<{ name: string; value: number }[]>([]);

  ngOnInit() {
    this.refresh();
  }

  goToSettings() {
    this.router.navigate(['/settings']);
  }

  refresh() {
    this.api.get<{ status: string; services?: Record<string, string> }>('/dashboard/health').subscribe({
      next: (res) => {
        this.apiStatus.set(res.services?.['api'] ?? res.status ?? 'UP');
        this.ollamaStatus.set(res.services?.['ollama'] ?? 'N/A');
      },
      error: () => {
        this.apiStatus.set('DOWN');
        this.ollamaStatus.set('DOWN');
      }
    });

    this.api.get<any[]>('/documents').subscribe({
      next: (docs) => this.documentCount.set(docs?.length ?? 0),
      error: () => this.documentCount.set(0)
    });

    this.api.get<any[]>('/mcp/servers').subscribe({
      next: (servers) => this.mcpServerCount.set(servers?.length ?? 0),
      error: () => this.mcpServerCount.set(0)
    });

    this.loadAnalytics(this.selectedPeriod());
  }

  loadAnalytics(days: number) {
    this.selectedPeriod.set(days);
    const from = new Date(Date.now() - days * 86400000).toISOString();
    const to = new Date().toISOString();
    this.api.get<AnalyticsData>(`/dashboard/analytics?from=${from}&to=${to}`).subscribe({
      next: (data) => {
        this.analytics.set(data);
        this.computeProviderEntries(data);
        this.computeCostEntries(data);
      },
      error: () => {}
    });
  }

  formatNumber(value: number): string {
    if (value >= 1_000_000) {
      return (value / 1_000_000).toFixed(1) + 'M';
    }
    if (value >= 1_000) {
      return (value / 1_000).toFixed(1) + 'K';
    }
    return value.toString();
  }

  private computeProviderEntries(data: AnalyticsData) {
    if (!data.tokensByProvider) {
      this.providerEntries.set([]);
      return;
    }
    const entries = Object.entries(data.tokensByProvider);
    const maxVal = Math.max(...entries.map(([, v]) => v), 1);
    this.providerEntries.set(
      entries.map(([name, value]) => ({
        name,
        value,
        percent: (value / maxVal) * 100
      }))
    );
  }

  private computeCostEntries(data: AnalyticsData) {
    if (!data.costByProvider) {
      this.costEntries.set([]);
      return;
    }
    this.costEntries.set(
      Object.entries(data.costByProvider).map(([name, value]) => ({ name, value }))
    );
  }
}
