import { Component, OnInit, inject, signal } from '@angular/core';
import { SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { McpToolService } from '../services/mcp-tool.service';
import { McpTool, McpToolExecutionResult } from '../models/mcp.model';

@Component({
  selector: 'app-mcp-tools',
  standalone: true,
  imports: [FormsModule, SlicePipe],
  template: `
    <div class="tools-page">
      <div class="header">
        <h2>Tool MCP Disponibili</h2>
        <button class="btn btn-secondary" (click)="loadAll()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M23 4v6h-6M1 20v-6h6"/>
            <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
          </svg>
          Aggiorna
        </button>
      </div>

      <div class="section">
        <h3>Tool Locali <span class="count">({{ localTools().length }})</span></h3>
        @if (localTools().length === 0) {
          <p class="empty">Nessun tool locale configurato.</p>
        } @else {
          <div class="tool-grid">
            @for (tool of localTools(); track tool.name) {
              <div class="tool-card local" (click)="selectTool(tool)" [class.selected]="selectedTool()?.name === tool.name && selectedTool()?.local">
                <div class="tool-name">{{ tool.name }}</div>
                <div class="tool-desc">{{ tool.description }}</div>
                <span class="tool-badge local">Locale</span>
              </div>
            }
          </div>
        }
      </div>

      <div class="section">
        <h3>Tool Esterni <span class="count">({{ externalTools().length }})</span></h3>
        @if (loading()) {
          <div class="loading-state">
            <span class="spinner"></span>
            Caricamento tool esterni...
          </div>
        } @else if (externalTools().length === 0) {
          <p class="empty">Nessun tool esterno disponibile. Connetti un server MCP per vedere i tool.</p>
        } @else {
          <div class="tool-grid">
            @for (tool of externalTools(); track tool.name + tool.serverId) {
              <div class="tool-card external" (click)="selectTool(tool)" [class.selected]="selectedTool()?.name === tool.name && !selectedTool()?.local">
                <div class="tool-name">{{ tool.name }}</div>
                <div class="tool-desc">{{ tool.description }}</div>
                <div class="tool-meta">
                  <span class="tool-badge external">Esterno</span>
                  @if (tool.serverId) {
                    <span class="server-id">{{ tool.serverId | slice:0:8 }}...</span>
                  }
                </div>
              </div>
            }
          </div>
        }
      </div>

      @if (selectedTool()) {
        <div class="execution-panel">
          <h3>Esegui Tool: <span class="mono">{{ selectedTool()!.name }}</span></h3>
          <p class="tool-exec-desc">{{ selectedTool()!.description }}</p>

          @if (selectedTool()!.inputSchema) {
            <div class="schema-info">
              <span class="schema-label">Input Schema:</span>
              <pre class="schema-code">{{ selectedTool()!.inputSchema }}</pre>
            </div>
          }

          <div class="exec-form">
            <div class="form-group">
              <label>Argomenti (JSON)</label>
              <textarea [(ngModel)]="execArgs" rows="4" placeholder='{"key": "value"}'></textarea>
            </div>
            <button class="btn btn-primary" (click)="executeTool()" [disabled]="executing()">
              @if (executing()) { <span class="spinner-sm"></span> }
              Esegui
            </button>
            <button class="btn btn-secondary" (click)="selectedTool.set(null); execResult.set(null)">
              Chiudi
            </button>
          </div>

          @if (execResult()) {
            <div class="exec-result" [class.success]="execResult()!.success" [class.error]="!execResult()!.success">
              <div class="result-header">
                <span class="result-status">{{ execResult()!.success ? 'Successo' : 'Errore' }}</span>
                <span class="result-time">{{ execResult()!.executionTimeMs }}ms</span>
              </div>
              @if (execResult()!.errorMessage) {
                <div class="result-error">{{ execResult()!.errorMessage }}</div>
              }
              @if (execResult()!.result !== null && execResult()!.result !== undefined) {
                <pre class="result-data">{{ formatResult(execResult()!.result) }}</pre>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h2 { margin: 0; color: #1a1a2e; }
    h3 { color: #333; font-size: 1.05rem; margin: 0 0 1rem 0; }
    .count { font-weight: 400; color: #999; font-size: 0.85rem; }
    .section { margin-bottom: 2rem; }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: #0f3460; color: white; }
    .btn-primary:hover:not(:disabled) { background: #1a4a7a; }
    .btn-secondary { background: white; color: #333; border: 1px solid #ddd; }
    .btn-secondary:hover:not(:disabled) { background: #f5f5f5; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }

    .tool-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 0.75rem; }
    .tool-card {
      background: white; padding: 1rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08); border-left: 3px solid #ddd;
      cursor: pointer; transition: all 0.15s;
    }
    .tool-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.12); }
    .tool-card.selected { border-color: #0f3460; box-shadow: 0 0 0 2px rgba(15,52,96,0.2); }
    .tool-card.local { border-left-color: #0f3460; }
    .tool-card.external { border-left-color: #8e44ad; }
    .tool-name { font-weight: 600; font-size: 0.9rem; margin-bottom: 0.25rem; font-family: monospace; }
    .tool-desc { font-size: 0.8rem; color: #666; margin-bottom: 0.5rem; line-height: 1.3; }
    .tool-meta { display: flex; gap: 0.5rem; align-items: center; }
    .tool-badge { font-size: 0.6rem; padding: 0.1rem 0.4rem; border-radius: 4px; text-transform: uppercase; font-weight: 600; }
    .tool-badge.local { background: #e3f2fd; color: #0f3460; }
    .tool-badge.external { background: #f3e5f5; color: #8e44ad; }
    .server-id { font-size: 0.7rem; color: #999; font-family: monospace; }
    .empty { color: #888; font-size: 0.85rem; }
    .loading-state { display: flex; align-items: center; gap: 0.5rem; color: #999; font-size: 0.85rem; }
    .spinner { display: inline-block; width: 14px; height: 14px; border: 2px solid #ddd; border-top-color: #0f3460; border-radius: 50%; animation: spin 0.6s linear infinite; }
    .spinner-sm { display: inline-block; width: 12px; height: 12px; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }

    .execution-panel {
      background: white; padding: 1.5rem; border-radius: 10px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1); border-top: 3px solid #0f3460;
    }
    .execution-panel h3 { margin-bottom: 0.25rem; }
    .mono { font-family: monospace; color: #0f3460; }
    .tool-exec-desc { font-size: 0.85rem; color: #666; margin-bottom: 1rem; }
    .schema-info { margin-bottom: 1rem; }
    .schema-label { font-size: 0.75rem; color: #888; text-transform: uppercase; font-weight: 600; }
    .schema-code {
      background: #f8f9fa; padding: 0.75rem; border-radius: 6px;
      font-size: 0.8rem; font-family: monospace; overflow-x: auto;
      border: 1px solid #eee; margin-top: 0.25rem;
    }
    .exec-form { margin-bottom: 1rem; }
    .form-group { margin-bottom: 0.75rem; }
    .form-group label { display: block; font-size: 0.8rem; font-weight: 500; margin-bottom: 0.25rem; }
    .form-group textarea {
      width: 100%; padding: 0.5rem; border: 1px solid #ddd;
      border-radius: 4px; font-family: monospace; font-size: 0.85rem;
      resize: vertical; box-sizing: border-box;
    }
    .exec-form .btn { margin-right: 0.5rem; }

    .exec-result {
      padding: 1rem; border-radius: 8px; margin-top: 1rem;
    }
    .exec-result.success { background: #f0fff0; border: 1px solid #d4edda; }
    .exec-result.error { background: #fff5f5; border: 1px solid #f8d7da; }
    .result-header { display: flex; justify-content: space-between; margin-bottom: 0.5rem; }
    .result-status { font-weight: 600; font-size: 0.85rem; }
    .exec-result.success .result-status { color: #155724; }
    .exec-result.error .result-status { color: #721c24; }
    .result-time { font-size: 0.75rem; color: #999; }
    .result-error { color: #c62828; font-size: 0.85rem; margin-bottom: 0.5rem; }
    .result-data {
      background: #1e1e2e; color: #cdd6f4; padding: 0.75rem;
      border-radius: 6px; font-size: 0.8rem; font-family: monospace;
      overflow-x: auto; max-height: 300px; overflow-y: auto;
    }
  `]
})
export class McpToolsComponent implements OnInit {
  private mcpToolService = inject(McpToolService);

  localTools = signal<McpTool[]>([]);
  externalTools = signal<McpTool[]>([]);
  loading = signal(false);
  selectedTool = signal<McpTool | null>(null);
  executing = signal(false);
  execResult = signal<McpToolExecutionResult | null>(null);
  execArgs = '{}';

  ngOnInit(): void { this.loadAll(); }

  loadAll(): void {
    this.loading.set(true);
    this.mcpToolService.listLocalTools().subscribe({
      next: (tools) => this.localTools.set(tools),
      error: () => {}
    });
    this.mcpToolService.listAllTools().subscribe({
      next: (tools) => { this.externalTools.set(tools); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  selectTool(tool: McpTool) {
    this.selectedTool.set(tool);
    this.execResult.set(null);
    this.execArgs = '{}';
  }

  executeTool() {
    const tool = this.selectedTool();
    if (!tool || !tool.serverId) return;

    let args: Record<string, unknown> = {};
    try {
      args = JSON.parse(this.execArgs);
    } catch {
      this.execResult.set({
        toolName: tool.name,
        result: null,
        success: false,
        errorMessage: 'JSON argomenti non valido',
        executionTimeMs: 0
      });
      return;
    }

    this.executing.set(true);
    this.mcpToolService.executeTool({
      toolName: tool.name,
      serverId: tool.serverId,
      arguments: args
    }).subscribe({
      next: (result) => { this.execResult.set(result); this.executing.set(false); },
      error: (err) => {
        this.execResult.set({
          toolName: tool.name,
          result: null,
          success: false,
          errorMessage: err.error?.message || 'Errore nell\'esecuzione del tool',
          executionTimeMs: 0
        });
        this.executing.set(false);
      }
    });
  }

  formatResult(result: unknown): string {
    try {
      return JSON.stringify(result, null, 2);
    } catch {
      return String(result);
    }
  }
}
