import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { McpServerService } from '../services/mcp-server.service';
import { McpServer, CreateMcpServerRequest } from '../models/mcp.model';

@Component({
  selector: 'app-mcp-servers',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="servers-page">
      <div class="header">
        <h2>Server MCP Esterni</h2>
        <button class="btn btn-primary" (click)="showForm = !showForm">
          {{ showForm ? 'Annulla' : '+ Aggiungi Server' }}
        </button>
      </div>

      @if (showForm) {
        <div class="add-form">
          <h3>Registra Nuovo Server</h3>
          <div class="form-grid">
            <div class="form-group">
              <label>Nome *</label>
              <input type="text" [(ngModel)]="newServer.name" placeholder="Nome del server">
            </div>
            <div class="form-group">
              <label>Descrizione</label>
              <input type="text" [(ngModel)]="newServer.description" placeholder="Descrizione opzionale">
            </div>
            <div class="form-group">
              <label>Tipo *</label>
              <select [(ngModel)]="newServer.type" (ngModelChange)="onTypeChange()">
                <option value="STDIO">STDIO</option>
                <option value="SSE">SSE</option>
              </select>
            </div>
            @if (newServer.type === 'STDIO') {
              <div class="form-group">
                <label>Comando *</label>
                <input type="text" [(ngModel)]="newServer.command" placeholder="es. npx">
              </div>
              <div class="form-group full-width">
                <label>Argomenti (separati da virgola)</label>
                <input type="text" [(ngModel)]="argsString" placeholder="es. -y,@modelcontextprotocol/server-filesystem,./">
              </div>
            }
            @if (newServer.type === 'SSE') {
              <div class="form-group full-width">
                <label>URL *</label>
                <input type="text" [(ngModel)]="newServer.url" placeholder="es. http://localhost:8082/sse">
              </div>
            }
            <div class="form-group">
              <label>Timeout (secondi)</label>
              <input type="number" [(ngModel)]="newServer.timeoutSeconds" min="5" max="300">
            </div>
            <div class="form-group checkbox-group">
              <label>
                <input type="checkbox" [(ngModel)]="newServer.autoReconnect">
                Auto-riconnessione
              </label>
            </div>
          </div>
          <button class="btn btn-primary" (click)="addServer()" [disabled]="!isFormValid() || adding()">
            @if (adding()) { <span class="spinner-sm"></span> }
            Registra Server
          </button>
        </div>
      }

      @if (notification()) {
        <div class="notification" [class]="notification()!.type">
          {{ notification()!.message }}
        </div>
      }

      @if (loading()) {
        <div class="loading">
          <span class="spinner"></span>
          Caricamento server...
        </div>
      } @else if (servers().length === 0) {
        <div class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <circle cx="12" cy="12" r="3"/><path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4"/>
          </svg>
          <h3>Nessun server MCP registrato</h3>
          <p>Aggiungi un server esterno per estendere le capacita' di LocalMind.</p>
        </div>
      } @else {
        <div class="server-list">
          @for (server of servers(); track server.id) {
            <div class="server-card" [class]="'status-' + server.status.toLowerCase()">
              <div class="server-info">
                <div class="server-header">
                  <h3>{{ server.name }}</h3>
                  <span class="status-badge" [class]="server.status.toLowerCase()">
                    {{ getStatusLabel(server.status) }}
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
                        [disabled]="actionId() === server.id" title="Test connessione">
                  @if (actionId() === server.id && actionType() === 'test') { <span class="spinner-xs"></span> }
                  Test
                </button>
                <button class="btn-sm" (click)="reconnectServer(server.id)"
                        [disabled]="actionId() === server.id" title="Riconnetti">
                  @if (actionId() === server.id && actionType() === 'reconnect') { <span class="spinner-xs"></span> }
                  Riconnetti
                </button>
                <button class="btn-sm btn-danger" (click)="removeServer(server.id)"
                        [disabled]="actionId() === server.id" title="Rimuovi">
                  Rimuovi
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
    h2 { margin: 0; color: #1a1a2e; }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: #0f3460; color: white; }
    .btn-primary:hover:not(:disabled) { background: #1a4a7a; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .add-form {
      background: white; padding: 1.5rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 1.5rem;
    }
    .add-form h3 { margin: 0 0 1rem 0; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8rem; font-weight: 500; color: #333; }
    .form-group input, .form-group select {
      padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; width: 100%; box-sizing: border-box;
    }
    .full-width { grid-column: 1 / -1; }
    .checkbox-group { display: flex; align-items: end; }
    .checkbox-group label { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-size: 0.85rem; }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: #d4edda; color: #155724; }
    .notification.error { background: #f8d7da; color: #721c24; }

    .server-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .server-card {
      background: white; padding: 1rem 1.25rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); display: flex;
      justify-content: space-between; align-items: center;
      border-left: 4px solid #ddd;
    }
    .server-card.status-connected { border-left-color: #2ecc71; }
    .server-card.status-disconnected { border-left-color: #95a5a6; }
    .server-card.status-error { border-left-color: #e74c3c; }
    .server-card.status-connecting { border-left-color: #f39c12; }
    .server-header { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.25rem; }
    .server-header h3 { margin: 0; font-size: 1rem; }
    .status-badge {
      font-size: 0.65rem; padding: 0.15rem 0.5rem; border-radius: 10px;
      text-transform: uppercase; font-weight: 600;
    }
    .status-badge.connected { background: #d4edda; color: #155724; }
    .status-badge.disconnected { background: #e2e3e5; color: #383d41; }
    .status-badge.error { background: #f8d7da; color: #721c24; }
    .status-badge.connecting { background: #fff3cd; color: #856404; }
    .description { color: #666; font-size: 0.85rem; margin: 0.25rem 0; }
    .meta { display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; color: #888; flex-wrap: wrap; }
    .type-badge {
      background: #e8eaf6; color: #3f51b5; padding: 0.1rem 0.4rem;
      border-radius: 4px; font-size: 0.7rem; font-weight: 600;
    }
    .mono { font-family: monospace; font-size: 0.75rem; }
    .server-actions { display: flex; gap: 0.5rem; flex-shrink: 0; }
    .btn-sm {
      padding: 0.35rem 0.75rem; border: 1px solid #ddd; background: white;
      border-radius: 4px; cursor: pointer; font-size: 0.8rem; font-family: inherit;
      display: inline-flex; align-items: center; gap: 0.3rem;
    }
    .btn-sm:hover:not(:disabled) { background: #f5f5f5; }
    .btn-sm:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-danger { color: #e74c3c; border-color: #e74c3c; }
    .btn-danger:hover:not(:disabled) { background: #fdf2f2; }
    .loading, .empty-state {
      text-align: center; padding: 3rem; color: #666;
      background: white; border-radius: 8px;
    }
    .loading { display: flex; align-items: center; gap: 0.5rem; justify-content: center; }
    .empty-state svg { margin-bottom: 1rem; }
    .empty-state h3 { margin-bottom: 0.25rem; }
    .empty-state p { font-size: 0.85rem; color: #999; }
    .spinner { display: inline-block; width: 16px; height: 16px; border: 2px solid #ddd; border-top-color: #0f3460; border-radius: 50%; animation: spin 0.6s linear infinite; }
    .spinner-sm { display: inline-block; width: 12px; height: 12px; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
    .spinner-xs { display: inline-block; width: 10px; height: 10px; border: 2px solid #ddd; border-top-color: #0f3460; border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class McpServersComponent implements OnInit {
  private mcpServerService = inject(McpServerService);

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
        this.showNotify('success', 'Server registrato con successo');
        this.loadServers();
      },
      error: () => { this.adding.set(false); this.showNotify('error', 'Errore nella registrazione'); }
    });
  }

  testServer(id: string): void {
    this.actionId.set(id);
    this.actionType.set('test');
    this.mcpServerService.testConnection(id).subscribe({
      next: (server) => {
        this.actionId.set(null);
        this.showNotify('success', `Test ${server.name}: ${server.status}`);
        this.loadServers();
      },
      error: () => { this.actionId.set(null); this.showNotify('error', 'Test connessione fallito'); }
    });
  }

  reconnectServer(id: string): void {
    this.actionId.set(id);
    this.actionType.set('reconnect');
    this.mcpServerService.reconnect(id).subscribe({
      next: () => {
        this.actionId.set(null);
        this.showNotify('success', 'Riconnessione avviata');
        this.loadServers();
      },
      error: () => { this.actionId.set(null); this.showNotify('error', 'Errore nella riconnessione'); }
    });
  }

  removeServer(id: string): void {
    this.actionId.set(id);
    this.actionType.set('remove');
    this.mcpServerService.removeServer(id).subscribe({
      next: () => {
        this.actionId.set(null);
        this.showNotify('success', 'Server rimosso');
        this.loadServers();
      },
      error: () => { this.actionId.set(null); this.showNotify('error', 'Errore nella rimozione'); }
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
