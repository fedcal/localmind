import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { McpServerService } from '../services/mcp-server.service';
import { McpServer, CreateMcpServerRequest } from '../models/mcp.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../core/i18n/translation.service';
import { LoadingSkeletonComponent } from '../../../shared/components/loading-skeleton/loading-skeleton.component';
import { UrlValidatorDirective } from '../../../shared/validators/url-validator.directive';
import { FieldErrorComponent } from '../../../shared/components/field-error/field-error.component';

@Component({
  selector: 'app-mcp-servers',
  standalone: true,
  imports: [FormsModule, TranslatePipe, LoadingSkeletonComponent, UrlValidatorDirective, FieldErrorComponent],
  template: `
    <div class="servers-page">
      <div class="header">
        <h2>{{ 'MCP_SERVERS.TITLE' | translate }}</h2>
        <button class="btn btn-primary" (click)="showForm = !showForm">
          {{ showForm ? ('COMMON.CANCEL' | translate) : ('MCP_SERVERS.ADD' | translate) }}
        </button>
      </div>

      @if (showForm) {
        <div class="add-form">
          <h3>{{ 'MCP_SERVERS.FORM_TITLE' | translate }}</h3>
          <div class="form-grid">
            <div class="form-group">
              <label>{{ 'MCP_SERVERS.FORM_NAME' | translate }}</label>
              <input type="text" [(ngModel)]="newServer.name" name="serverName" #serverName="ngModel"
                     [placeholder]="'MCP_SERVERS.FORM_NAME_PLACEHOLDER' | translate" required minlength="2">
              @if (serverName.invalid && serverName.touched) {
                <span class="form-error">
                  @if (serverName.errors?.['required']) { {{ 'VALIDATION.REQUIRED' | translate }} }
                  @else if (serverName.errors?.['minlength']) { {{ 'VALIDATION.MIN_LENGTH' | translate:{ min: 2 } }} }
                </span>
              }
            </div>
            <div class="form-group">
              <label>{{ 'MCP_SERVERS.FORM_DESCRIPTION' | translate }}</label>
              <input type="text" [(ngModel)]="newServer.description" [placeholder]="'MCP_SERVERS.FORM_DESCRIPTION_PLACEHOLDER' | translate">
            </div>
            <div class="form-group">
              <label>{{ 'MCP_SERVERS.FORM_TYPE' | translate }}</label>
              <select [(ngModel)]="newServer.type" (ngModelChange)="onTypeChange()">
                <option value="STDIO">STDIO</option>
                <option value="SSE">SSE</option>
              </select>
            </div>
            @if (newServer.type === 'STDIO') {
              <div class="form-group">
                <label>{{ 'MCP_SERVERS.FORM_COMMAND' | translate }}</label>
                <input type="text" [(ngModel)]="newServer.command" name="serverCommand" #serverCommand="ngModel"
                       [placeholder]="'MCP_SERVERS.FORM_COMMAND_PLACEHOLDER' | translate" required>
                @if (serverCommand.invalid && serverCommand.touched) {
                  <span class="form-error">{{ 'VALIDATION.REQUIRED' | translate }}</span>
                }
              </div>
              <div class="form-group full-width">
                <label>{{ 'MCP_SERVERS.FORM_ARGS' | translate }}</label>
                <input type="text" [(ngModel)]="argsString" [placeholder]="'MCP_SERVERS.FORM_ARGS_PLACEHOLDER' | translate">
              </div>
            }
            @if (newServer.type === 'SSE') {
              <div class="form-group full-width">
                <label>{{ 'MCP_SERVERS.FORM_URL' | translate }}</label>
                <input type="text" [(ngModel)]="newServer.url" name="serverUrl" #serverUrl="ngModel"
                       [placeholder]="'MCP_SERVERS.FORM_URL_PLACEHOLDER' | translate" required appUrlValidator>
                <app-field-error [control]="serverUrl" />
              </div>
            }
            <div class="form-group">
              <label>{{ 'MCP_SERVERS.FORM_TIMEOUT' | translate }}</label>
              <input type="number" [(ngModel)]="newServer.timeoutSeconds" min="5" max="300">
            </div>
            <div class="form-group checkbox-group">
              <label>
                <input type="checkbox" [(ngModel)]="newServer.autoReconnect">
                {{ 'MCP_SERVERS.FORM_AUTO_RECONNECT' | translate }}
              </label>
            </div>
          </div>
          <button class="btn btn-primary" (click)="addServer()" [disabled]="!isFormValid() || adding()">
            @if (adding()) { <span class="spinner-sm"></span> }
            {{ 'MCP_SERVERS.REGISTER' | translate }}
          </button>
        </div>
      }

      @if (notification()) {
        <div class="notification" [class]="notification()!.type">
          {{ notification()!.message }}
        </div>
      }

      @if (loading()) {
        <app-loading-skeleton [count]="3" />
      } @else if (servers().length === 0) {
        <div class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <circle cx="12" cy="12" r="3"/><path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4"/>
          </svg>
          <h3>{{ 'MCP_SERVERS.EMPTY_TITLE' | translate }}</h3>
          <p>{{ 'MCP_SERVERS.EMPTY_DESC' | translate }}</p>
        </div>
      } @else {
        <div class="server-list">
          @for (server of servers(); track server.id) {
            <div class="server-card" [class]="'status-' + server.status.toLowerCase()">
              <div class="server-info">
                <div class="server-header">
                  <h3>{{ server.name }}</h3>
                  <span class="status-badge" [class]="server.status.toLowerCase()">
                    {{ 'ENUM.MCP_STATUS.' + server.status | translate }}
                  </span>
                </div>
                @if (server.description) {
                  <p class="description">{{ server.description }}</p>
                }
                <div class="meta">
                  <span class="type-badge">{{ server.type }}</span>
                  <span class="detail">
                    {{ server.type === 'SSE' ? server.config.url : server.config.command }}
                  </span>
                  @if (server.config.args && server.config.args.length > 0) {
                    <span class="detail mono">{{ server.config.args.join(' ') }}</span>
                  }
                </div>
              </div>
              <div class="server-actions">
                <button class="btn-sm" (click)="testServer(server.id)"
                        [disabled]="actionId() === server.id" [title]="'MCP_SERVERS.TEST_TITLE' | translate">
                  @if (actionId() === server.id && actionType() === 'test') { <span class="spinner-xs"></span> }
                  {{ 'COMMON.TEST' | translate }}
                </button>
                <button class="btn-sm" (click)="reconnectServer(server.id)"
                        [disabled]="actionId() === server.id" [title]="'MCP_SERVERS.RECONNECT' | translate">
                  @if (actionId() === server.id && actionType() === 'reconnect') { <span class="spinner-xs"></span> }
                  {{ 'MCP_SERVERS.RECONNECT' | translate }}
                </button>
                <button class="btn-sm btn-danger" (click)="removeServer(server.id)"
                        [disabled]="actionId() === server.id" [title]="'COMMON.REMOVE' | translate">
                  {{ 'COMMON.REMOVE' | translate }}
                </button>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h2 { margin: 0; color: var(--color-text); }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-light); }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .add-form {
      background: var(--color-card-bg); padding: 1.5rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 1.5rem;
    }
    .add-form h3 { margin: 0 0 1rem 0; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8rem; font-weight: 500; color: var(--color-text); }
    .form-group input, .form-group select {
      padding: 0.5rem; border: 1px solid var(--color-border); border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; width: 100%; box-sizing: border-box;
      background: var(--color-input-bg); color: var(--color-text);
    }
    .form-error { font-size: 0.75rem; color: var(--color-error); margin-top: 0.15rem; }
    .full-width { grid-column: 1 / -1; }
    .checkbox-group { display: flex; align-items: end; }
    .checkbox-group label { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-size: 0.85rem; }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: var(--color-success-bg); color: var(--color-success-text); }
    .notification.error { background: var(--color-error-bg); color: var(--color-error-text); }

    .server-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .server-card {
      background: var(--color-card-bg); padding: 1rem 1.25rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); display: flex;
      justify-content: space-between; align-items: center;
      border-left: 4px solid var(--color-border);
    }
    .server-card.status-connected { border-left-color: var(--color-success); }
    .server-card.status-disconnected { border-left-color: var(--color-text-muted); }
    .server-card.status-error { border-left-color: var(--color-error); }
    .server-card.status-connecting { border-left-color: var(--color-warning); }
    .server-header { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.25rem; }
    .server-header h3 { margin: 0; font-size: 1rem; }
    .status-badge {
      font-size: 0.65rem; padding: 0.15rem 0.5rem; border-radius: 10px;
      text-transform: uppercase; font-weight: 600;
    }
    .status-badge.connected { background: var(--color-success-bg); color: var(--color-success-text); }
    .status-badge.disconnected { background: var(--color-border); color: var(--color-text-secondary); }
    .status-badge.error { background: var(--color-error-bg); color: var(--color-error-text); }
    .status-badge.connecting { background: var(--color-warning-bg); color: var(--color-warning-text); }
    .description { color: var(--color-text-secondary); font-size: 0.85rem; margin: 0.25rem 0; }
    .meta { display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; color: var(--color-text-muted); flex-wrap: wrap; }
    .type-badge {
      background: var(--color-info-bg); color: var(--color-info-text); padding: 0.1rem 0.4rem;
      border-radius: 4px; font-size: 0.7rem; font-weight: 600;
    }
    .mono { font-family: monospace; font-size: 0.75rem; }
    .server-actions { display: flex; gap: 0.5rem; flex-shrink: 0; }
    .btn-sm {
      padding: 0.35rem 0.75rem; border: 1px solid var(--color-border); background: var(--color-card-bg);
      border-radius: 4px; cursor: pointer; font-size: 0.8rem; font-family: inherit;
      display: inline-flex; align-items: center; gap: 0.3rem;
    }
    .btn-sm:hover:not(:disabled) { background: var(--color-bg); }
    .btn-sm:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-danger { color: var(--color-error); border-color: var(--color-error); }
    .btn-danger:hover:not(:disabled) { background: var(--color-error-bg); }
    .loading, .empty-state {
      text-align: center; padding: 3rem; color: var(--color-text-secondary);
      background: var(--color-card-bg); border-radius: 8px;
    }
    .loading { display: flex; align-items: center; gap: 0.5rem; justify-content: center; }
    .empty-state svg { margin-bottom: 1rem; }
    .empty-state h3 { margin-bottom: 0.25rem; }
    .empty-state p { font-size: 0.85rem; color: var(--color-text-muted); }
    .spinner { display: inline-block; width: 16px; height: 16px; border: 2px solid var(--color-border); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.6s linear infinite; }
    .spinner-sm { display: inline-block; width: 12px; height: 12px; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
    .spinner-xs { display: inline-block; width: 10px; height: 10px; border: 2px solid var(--color-border); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class McpServersComponent implements OnInit {
  private mcpServerService = inject(McpServerService);
  private i18n = inject(TranslationService);

  servers = signal<McpServer[]>([]);
  loading = signal(false);
  adding = signal(false);
  actionId = signal<string | null>(null);
  actionType = signal<string | null>(null);
  notification = signal<{ type: string; message: string } | null>(null);
  showForm = false;
  argsString = '';

  newServer: CreateMcpServerRequest = {
    name: '', type: 'STDIO', timeoutSeconds: 30, autoReconnect: true
  };

  ngOnInit(): void { this.loadServers(); }

  loadServers(): void {
    this.loading.set(true);
    this.mcpServerService.listServers().subscribe({
      next: (servers) => { this.servers.set(servers); this.loading.set(false); },
      error: () => { this.loading.set(false); }
    });
  }

  onTypeChange(): void {
    this.newServer.command = undefined;
    this.newServer.args = undefined;
    this.newServer.url = undefined;
    this.argsString = '';
  }

  isFormValid(): boolean {
    if (!this.newServer.name) return false;
    if (this.newServer.type === 'STDIO' && !this.newServer.command) return false;
    if (this.newServer.type === 'SSE' && !this.newServer.url) return false;
    return true;
  }

  addServer(): void {
    if (this.argsString) {
      this.newServer.args = this.argsString.split(',').map(s => s.trim());
    }
    this.adding.set(true);
    this.mcpServerService.registerServer(this.newServer).subscribe({
      next: () => {
        this.adding.set(false);
        this.showForm = false;
        this.resetForm();
        this.showNotify('success', this.i18n.instant('MCP_SERVERS.ADD_SUCCESS'));
        this.loadServers();
      },
      error: () => { this.adding.set(false); this.showNotify('error', this.i18n.instant('MCP_SERVERS.ADD_ERROR')); }
    });
  }

  testServer(id: string): void {
    this.actionId.set(id);
    this.actionType.set('test');
    this.mcpServerService.testConnection(id).subscribe({
      next: (server) => {
        this.actionId.set(null);
        this.showNotify('success', this.i18n.instant('MCP_SERVERS.TEST_RESULT', { name: server.name, status: server.status }));
        this.loadServers();
      },
      error: () => { this.actionId.set(null); this.showNotify('error', this.i18n.instant('MCP_SERVERS.TEST_ERROR')); }
    });
  }

  reconnectServer(id: string): void {
    this.actionId.set(id);
    this.actionType.set('reconnect');
    this.mcpServerService.reconnect(id).subscribe({
      next: () => {
        this.actionId.set(null);
        this.showNotify('success', this.i18n.instant('MCP_SERVERS.RECONNECT_SUCCESS'));
        this.loadServers();
      },
      error: () => { this.actionId.set(null); this.showNotify('error', this.i18n.instant('MCP_SERVERS.RECONNECT_ERROR')); }
    });
  }

  removeServer(id: string): void {
    this.actionId.set(id);
    this.actionType.set('remove');
    this.mcpServerService.removeServer(id).subscribe({
      next: () => {
        this.actionId.set(null);
        this.showNotify('success', this.i18n.instant('MCP_SERVERS.REMOVE_SUCCESS'));
        this.loadServers();
      },
      error: () => { this.actionId.set(null); this.showNotify('error', this.i18n.instant('MCP_SERVERS.REMOVE_ERROR')); }
    });
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'CONNECTED': return 'Connesso';
      case 'DISCONNECTED': return 'Disconnesso';
      case 'ERROR': return 'Errore';
      case 'CONNECTING': return 'Connessione...';
      default: return status;
    }
  }

  private resetForm(): void {
    this.newServer = { name: '', type: 'STDIO', timeoutSeconds: 30, autoReconnect: true };
    this.argsString = '';
  }

  private showNotify(type: string, message: string) {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 4000);
  }
}
