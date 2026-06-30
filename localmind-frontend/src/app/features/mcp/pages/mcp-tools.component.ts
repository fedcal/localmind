import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { McpToolService } from '../services/mcp-tool.service';
import { McpTool, McpToolExecutionResult } from '../models/mcp.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../core/i18n/translation.service';
import { ChatStore } from '../../chat/state/chat.store';
import { ConversationService } from '../../chat/services/conversation.service';
import { LoadingSkeletonComponent } from '../../../shared/components/loading-skeleton/loading-skeleton.component';
import { JsonValidatorDirective } from '../../../shared/validators/json-validator.directive';
import { FieldErrorComponent } from '../../../shared/components/field-error/field-error.component';

@Component({
  selector: 'app-mcp-tools',
  standalone: true,
  imports: [FormsModule, SlicePipe, TranslatePipe, LoadingSkeletonComponent, JsonValidatorDirective, FieldErrorComponent],
  template: `
    <div class="tools-page">
      <div class="header">
        <h2>{{ 'MCP_TOOLS.TITLE' | translate }}</h2>
        <button class="btn btn-secondary" (click)="loadAll()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M23 4v6h-6M1 20v-6h6"/>
            <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
          </svg>
          {{ 'COMMON.REFRESH' | translate }}
        </button>
      </div>

      <div class="filter-bar">
        <div class="search-box">
          <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
          </svg>
          <input type="text"
            [placeholder]="'MCP_TOOLS.SEARCH_PLACEHOLDER' | translate"
            [value]="searchQuery()"
            (input)="searchQuery.set($any($event.target).value)" />
          @if (searchQuery()) {
            <button class="clear-search" (click)="searchQuery.set('')" [attr.aria-label]="'COMMON.CLOSE' | translate">&times;</button>
          }
        </div>
        <div class="category-chips">
          <button class="chip" [class.active]="selectedCategory() === null" (click)="selectedCategory.set(null)">
            {{ 'MCP_TOOLS.CATEGORY_ALL' | translate }}
            <span class="chip-count">{{ localTools().length }}</span>
          </button>
          @for (cat of categories; track cat.key) {
            <button class="chip" [class.active]="selectedCategory() === cat.key" (click)="selectedCategory.set(cat.key)">
              {{ 'MCP_TOOLS.CATEGORY_' + cat.key | translate }}
              <span class="chip-count">{{ getCategoryCount(cat.key) }}</span>
            </button>
          }
        </div>
      </div>

      <div class="section">
        <h3>{{ 'MCP_TOOLS.LOCAL' | translate }} <span class="count">({{ filteredLocalTools().length }} / {{ localTools().length }})</span></h3>
        @if (filteredLocalTools().length === 0) {
          @if (localTools().length === 0) {
            <p class="empty">{{ 'MCP_TOOLS.LOCAL_EMPTY' | translate }}</p>
          } @else {
            <p class="empty">{{ 'MCP_TOOLS.NO_RESULTS' | translate }}</p>
          }
        } @else {
          <div class="tool-grid">
            @for (tool of filteredLocalTools(); track tool.name) {
              <div class="tool-card local" (click)="selectTool(tool)" [class.selected]="selectedTool()?.name === tool.name && selectedTool()?.local">
                <div class="tool-header">
                  <div class="tool-name">{{ tool.name }}</div>
                  <span class="tool-category-badge">{{ 'MCP_TOOLS.CATEGORY_' + getToolCategory(tool.name) | translate }}</span>
                </div>
                <div class="tool-desc">{{ tool.description }}</div>
                <span class="tool-badge local">{{ 'MCP_TOOLS.BADGE_LOCAL' | translate }}</span>
              </div>
            }
          </div>
        }
      </div>

      <div class="section">
        <h3>{{ 'MCP_TOOLS.EXTERNAL' | translate }} <span class="count">({{ externalTools().length }})</span></h3>
        @if (loading()) {
          <app-loading-skeleton variant="card-grid" [count]="6" />
        } @else if (externalTools().length === 0) {
          <p class="empty">{{ 'MCP_TOOLS.EXTERNAL_EMPTY' | translate }}</p>
        } @else {
          <div class="tool-grid">
            @for (tool of externalTools(); track tool.name + tool.serverId) {
              <div class="tool-card external" (click)="selectTool(tool)" [class.selected]="selectedTool()?.name === tool.name && !selectedTool()?.local">
                <div class="tool-name">{{ tool.name }}</div>
                <div class="tool-desc">{{ tool.description }}</div>
                <div class="tool-meta">
                  <span class="tool-badge external">{{ 'MCP_TOOLS.BADGE_EXTERNAL' | translate }}</span>
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
          <h3>{{ 'MCP_TOOLS.EXEC_TITLE' | translate }} <span class="mono">{{ selectedTool()!.name }}</span></h3>
          <p class="tool-exec-desc">{{ selectedTool()!.description }}</p>

          @if (selectedTool()!.inputSchema) {
            <div class="schema-info">
              <span class="schema-label">{{ 'MCP_TOOLS.INPUT_SCHEMA' | translate }}</span>
              <pre class="schema-code">{{ selectedTool()!.inputSchema }}</pre>
            </div>
          }

          <div class="exec-form">
            <div class="form-group">
              <label>{{ 'MCP_TOOLS.ARGS_LABEL' | translate }}</label>
              <textarea [(ngModel)]="execArgs" name="execArgs" #execArgsCtrl="ngModel" rows="4" placeholder='{"key": "value"}' appJsonValidator></textarea>
              <app-field-error [control]="execArgsCtrl" />
            </div>
            <button class="btn btn-primary" (click)="executeTool()" [disabled]="executing()">
              @if (executing()) { <span class="spinner-sm"></span> }
              {{ 'MCP_TOOLS.EXECUTE' | translate }}
            </button>
            <button class="btn btn-secondary" (click)="selectedTool.set(null); execResult.set(null)">
              {{ 'COMMON.CLOSE' | translate }}
            </button>
          </div>

          @if (execResult()) {
            <div class="exec-result" [class.success]="execResult()!.success" [class.error]="!execResult()!.success">
              <div class="result-header">
                <span class="result-status">{{ execResult()!.success ? ('COMMON.SUCCESS' | translate) : ('COMMON.ERROR' | translate) }}</span>
                <span class="result-time">{{ execResult()!.executionTimeMs }}ms</span>
              </div>
              @if (execResult()!.errorMessage) {
                <div class="result-error">{{ execResult()!.errorMessage }}</div>
              }
              @if (execResult()!.result !== null && execResult()!.result !== undefined) {
                <pre class="result-data">{{ formatResult(execResult()!.result) }}</pre>
              }
              @if (execResult()!.success && chatStore.currentConversationId()) {
                <div class="send-to-chat-area">
                  <button class="btn btn-send-chat" (click)="sendToChat()" [disabled]="sendingToChat()">
                    @if (sendingToChat()) { <span class="spinner-sm"></span> }
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
                    </svg>
                    {{ 'MCP_TOOLS.SEND_TO_CHAT' | translate }}
                  </button>
                  @if (sentToChat()) {
                    <span class="sent-badge">{{ 'MCP_TOOLS.SENT_TO_CHAT' | translate }}</span>
                  }
                </div>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h2 { margin: 0; color: var(--color-text); }
    h3 { color: var(--color-text); font-size: 1.05rem; margin: 0 0 1rem 0; }
    .count { font-weight: 400; color: var(--color-text-muted); font-size: 0.85rem; }
    .section { margin-bottom: 2rem; }

    .filter-bar { margin-bottom: 1.5rem; display: flex; flex-direction: column; gap: 0.75rem; }
    .search-box {
      position: relative; display: flex; align-items: center;
    }
    .search-icon { position: absolute; left: 0.75rem; color: var(--color-text-muted); pointer-events: none; }
    .search-box input {
      width: 100%; padding: 0.6rem 2rem 0.6rem 2.25rem;
      border: 1px solid var(--color-border); border-radius: 8px; font-size: 0.9rem;
      font-family: inherit; background: var(--color-card-bg); transition: border-color 0.15s;
      box-sizing: border-box;
    }
    .search-box input:focus { outline: none; border-color: var(--color-primary); box-shadow: 0 0 0 2px rgba(15,52,96,0.1); }
    .search-box input::placeholder { color: var(--color-text-muted); }
    .clear-search {
      position: absolute; right: 0.5rem; background: none; border: none;
      font-size: 1.2rem; color: var(--color-text-muted); cursor: pointer; padding: 0 0.25rem;
      line-height: 1;
    }
    .clear-search:hover { color: var(--color-text); }

    .category-chips { display: flex; flex-wrap: wrap; gap: 0.4rem; }
    .chip {
      display: inline-flex; align-items: center; gap: 0.3rem;
      padding: 0.3rem 0.7rem; border: 1px solid var(--color-border); border-radius: 20px;
      background: var(--color-card-bg); font-size: 0.78rem; cursor: pointer;
      font-family: inherit; color: var(--color-text-secondary); transition: all 0.15s; white-space: nowrap;
    }
    .chip:hover { border-color: var(--color-primary); color: var(--color-primary); }
    .chip.active { background: var(--color-primary); color: white; border-color: var(--color-primary); }
    .chip-count {
      font-size: 0.7rem; background: rgba(0,0,0,0.08); padding: 0.05rem 0.35rem;
      border-radius: 10px; min-width: 1.1rem; text-align: center;
    }
    .chip.active .chip-count { background: rgba(255,255,255,0.25); }

    .tool-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 0.5rem; }
    .tool-category-badge {
      font-size: 0.6rem; padding: 0.1rem 0.4rem; border-radius: 4px;
      background: var(--color-skeleton-base); color: var(--color-text-secondary); text-transform: uppercase; font-weight: 600;
      white-space: nowrap; flex-shrink: 0;
    }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-light); }
    .btn-secondary { background: var(--color-card-bg); color: var(--color-text); border: 1px solid var(--color-border); }
    .btn-secondary:hover:not(:disabled) { background: var(--color-bg); }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }

    .tool-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 0.75rem; }
    .tool-card {
      background: var(--color-card-bg); padding: 1rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08); border-left: 3px solid var(--color-border);
      cursor: pointer; transition: all 0.15s;
    }
    .tool-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.12); }
    .tool-card.selected { border-color: var(--color-primary); box-shadow: 0 0 0 2px rgba(15,52,96,0.2); }
    .tool-card.local { border-left-color: var(--color-primary); }
    .tool-card.external { border-left-color: var(--color-msg-tool-border); }
    .tool-name { font-weight: 600; font-size: 0.9rem; margin-bottom: 0.25rem; font-family: monospace; }
    .tool-desc { font-size: 0.8rem; color: var(--color-text-secondary); margin-bottom: 0.5rem; line-height: 1.3; }
    .tool-meta { display: flex; gap: 0.5rem; align-items: center; }
    .tool-badge { font-size: 0.6rem; padding: 0.1rem 0.4rem; border-radius: 4px; text-transform: uppercase; font-weight: 600; }
    .tool-badge.local { background: var(--color-info-bg); color: var(--color-primary); }
    .tool-badge.external { background: var(--color-msg-tool-bg); color: var(--color-msg-tool-border); }
    .server-id { font-size: 0.7rem; color: var(--color-text-muted); font-family: monospace; }
    .empty { color: var(--color-text-muted); font-size: 0.85rem; }
    .loading-state { display: flex; align-items: center; gap: 0.5rem; color: var(--color-text-muted); font-size: 0.85rem; }
    .spinner { display: inline-block; width: 14px; height: 14px; border: 2px solid var(--color-border); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.6s linear infinite; }
    .spinner-sm { display: inline-block; width: 12px; height: 12px; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }

    .execution-panel {
      background: var(--color-card-bg); padding: 1.5rem; border-radius: 10px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1); border-top: 3px solid var(--color-primary);
    }
    .execution-panel h3 { margin-bottom: 0.25rem; }
    .mono { font-family: monospace; color: var(--color-primary); }
    .tool-exec-desc { font-size: 0.85rem; color: var(--color-text-secondary); margin-bottom: 1rem; }
    .schema-info { margin-bottom: 1rem; }
    .schema-label { font-size: 0.75rem; color: var(--color-text-muted); text-transform: uppercase; font-weight: 600; }
    .schema-code {
      background: var(--color-hover-bg); padding: 0.75rem; border-radius: 6px;
      font-size: 0.8rem; font-family: monospace; overflow-x: auto;
      border: 1px solid var(--color-border-light); margin-top: 0.25rem;
    }
    .exec-form { margin-bottom: 1rem; }
    .form-group { margin-bottom: 0.75rem; }
    .form-group label { display: block; font-size: 0.8rem; font-weight: 500; margin-bottom: 0.25rem; }
    .form-group textarea {
      width: 100%; padding: 0.5rem; border: 1px solid var(--color-border);
      border-radius: 4px; font-family: monospace; font-size: 0.85rem;
      resize: vertical; box-sizing: border-box;
    }
    .exec-form .btn { margin-right: 0.5rem; }

    .exec-result {
      padding: 1rem; border-radius: 8px; margin-top: 1rem;
    }
    .exec-result.success { background: var(--color-success-bg); border: 1px solid var(--color-success-bg); }
    .exec-result.error { background: var(--color-error-bg); border: 1px solid var(--color-error-bg); }
    .result-header { display: flex; justify-content: space-between; margin-bottom: 0.5rem; }
    .result-status { font-weight: 600; font-size: 0.85rem; }
    .exec-result.success .result-status { color: var(--color-success-text); }
    .exec-result.error .result-status { color: var(--color-error-text); }
    .result-time { font-size: 0.75rem; color: var(--color-text-muted); }
    .result-error { color: var(--color-error-text); font-size: 0.85rem; margin-bottom: 0.5rem; }
    .result-data {
      background: var(--color-code-bg); color: var(--color-code-text); padding: 0.75rem;
      border-radius: 6px; font-size: 0.8rem; font-family: monospace;
      overflow-x: auto; max-height: 300px; overflow-y: auto;
    }
    .send-to-chat-area { display: flex; align-items: center; gap: 0.5rem; margin-top: 0.75rem; }
    .btn-send-chat { background: var(--color-msg-tool-border); color: white; border: none; }
    .btn-send-chat:hover:not(:disabled) { background: var(--color-msg-tool-border); }
    .sent-badge { font-size: 0.75rem; color: var(--color-success-text); background: var(--color-success-bg); padding: 0.2rem 0.5rem; border-radius: 4px; }
  `]
})
export class McpToolsComponent implements OnInit {
  private mcpToolService = inject(McpToolService);
  private i18n = inject(TranslationService);
  chatStore = inject(ChatStore);
  private conversationService = inject(ConversationService);

  localTools = signal<McpTool[]>([]);
  externalTools = signal<McpTool[]>([]);
  loading = signal(false);
  selectedTool = signal<McpTool | null>(null);
  executing = signal(false);
  execResult = signal<McpToolExecutionResult | null>(null);
  execArgs = '{}';
  sendingToChat = signal(false);
  sentToChat = signal(false);

  searchQuery = signal('');
  selectedCategory = signal<string | null>(null);

  readonly categories: { key: string; prefixes: string[] }[] = [
    { key: 'CORE', prefixes: ['document_search', 'chat', 'list_models'] },
    { key: 'UTILITY', prefixes: ['regex', 'http', 'snippet'] },
    { key: 'CODE', prefixes: ['code', 'dependency', 'scaffold'] },
    { key: 'TEST', prefixes: ['test', 'benchmark'] },
    { key: 'DEVOPS', prefixes: ['docker', 'log', 'cicd', 'pipeline'] },
    { key: 'DATABASE', prefixes: ['schema', 'mock', 'dataset'] },
    { key: 'DOC', prefixes: ['api_doc', 'codebase'] },
    { key: 'PROJECT', prefixes: ['sprint', 'story', 'task', 'metric', 'time', 'budget', 'cost', 'retro', 'action'] },
    { key: 'COMMUNICATION', prefixes: ['standup', 'env', 'get_env', 'compare', 'validate', 'generate_env'] },
    { key: 'GOVERNANCE', prefixes: ['policy', 'access', 'role', 'audit', 'decision', 'link_decision'] },
    { key: 'OPERATIONS', prefixes: ['incident', 'workflow', 'toggle'] },
    { key: 'QUALITY', prefixes: ['gate', 'insight', 'dashboard', 'server_status', 'recent_activity', 'project_summary', 'register', 'discover', 'health', 'capabilities'] }
  ];

  filteredLocalTools = computed(() => {
    let tools = this.localTools();
    const query = this.searchQuery().toLowerCase().trim();
    const category = this.selectedCategory();

    if (query) {
      tools = tools.filter(t =>
        t.name.toLowerCase().includes(query) ||
        (t.description && t.description.toLowerCase().includes(query))
      );
    }

    if (category) {
      const cat = this.categories.find(c => c.key === category);
      if (cat) {
        tools = tools.filter(t => this.matchesCategory(t.name, cat.prefixes));
      }
    }

    return tools;
  });

  getToolCategory(toolName: string): string {
    for (const cat of this.categories) {
      if (this.matchesCategory(toolName, cat.prefixes)) {
        return cat.key;
      }
    }
    return 'CORE';
  }

  getCategoryCount(categoryKey: string): number {
    const cat = this.categories.find(c => c.key === categoryKey);
    if (!cat) return 0;
    return this.localTools().filter(t => this.matchesCategory(t.name, cat.prefixes)).length;
  }

  private matchesCategory(toolName: string, prefixes: string[]): boolean {
    const lowerName = toolName.toLowerCase();
    return prefixes.some(prefix => lowerName.startsWith(prefix));
  }

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
    this.sentToChat.set(false);
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
        errorMessage: this.i18n.instant('MCP_TOOLS.INVALID_JSON'),
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
          errorMessage: err.error?.message || this.i18n.instant('MCP_TOOLS.EXEC_ERROR'),
          executionTimeMs: 0
        });
        this.executing.set(false);
      }
    });
  }

  sendToChat() {
    const result = this.execResult();
    const tool = this.selectedTool();
    const convId = this.chatStore.currentConversationId();
    if (!result || !tool || !convId) return;

    this.sendingToChat.set(true);
    this.conversationService.addToolResult(convId, {
      toolName: tool.name,
      content: this.formatResult(result.result),
      metadata: {
        serverId: tool.serverId,
        executionTimeMs: result.executionTimeMs
      }
    }).subscribe({
      next: () => {
        this.sendingToChat.set(false);
        this.sentToChat.set(true);
      },
      error: () => {
        this.sendingToChat.set(false);
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
