import { Component, inject, signal, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChatStore } from '../../state/chat.store';
import { ChatService } from '../../services/chat.service';
import { ChatStreamService } from '../../services/chat-stream.service';
import { ConversationService } from '../../services/conversation.service';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { ChatSidebarComponent } from '../../components/chat-sidebar/chat-sidebar.component';

@Component({
  selector: 'app-chat-page',
  imports: [FormsModule, TranslatePipe, ChatSidebarComponent],
  template: `
    <div class="chat-with-sidebar">
      <app-chat-sidebar #sidebar />
      <div class="chat-layout">
        <div class="chat-header">
          <div class="header-left">
            <h2>{{ store.currentConversationTitle() || ('CHAT.TITLE' | translate) }}</h2>
            <span class="msg-count">{{ 'CHAT.MESSAGES_COUNT' | translate:{ count: store.messageCount() } }}</span>
          </div>
          <div class="header-controls">
            <button class="mode-toggle" [class.advanced]="store.advancedMode()"
                    (click)="store.toggleAdvancedMode()" [title]="'CHAT.MODE_TOGGLE' | translate">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                @if (store.advancedMode()) {
                  <circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15"/>
                } @else {
                  <path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/>
                }
              </svg>
              {{ store.advancedMode() ? ('CHAT.MODE_ADVANCED' | translate) : ('CHAT.MODE_SIMPLE' | translate) }}
            </button>
            @if (store.advancedMode()) {
              <button class="system-prompt-toggle"
                      [class.active]="store.currentSystemPrompt()"
                      (click)="toggleSystemPrompt()"
                      [title]="'CHAT.SYSTEM_PROMPT' | translate">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/>
                </svg>
                {{ 'CHAT.SYSTEM_PROMPT' | translate }}
              </button>
              <button class="tool-calling-toggle"
                      [class.active]="store.toolCallingEnabled()"
                      (click)="store.toggleToolCalling()"
                      [title]="'CHAT.TOGGLE_TOOL_CALLING' | translate">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14.7 6.3a1 1 0 000 1.4l1.6 1.6-3.3 3.3-1.6-1.6a1 1 0 00-1.4 0l-4 4a1 1 0 101.4 1.4L11 13l1.6 1.6a1 1 0 001.4 0l4-4a1 1 0 000-1.4L16.4 7.6l1.3-1.3a1 1 0 00-1.4-1.4L15 6.3z"/>
                </svg>
                {{ store.toolCallingEnabled() ? ('CHAT.TOOL_CALLING_ENABLED' | translate) : ('CHAT.TOOL_CALLING_DISABLED' | translate) }}
              </button>
              <button class="rag-toggle"
                      [class.active]="store.ragEnabled()"
                      (click)="store.toggleRag()"
                      [title]="'CHAT.TOGGLE_RAG' | translate">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/>
                </svg>
                {{ store.ragEnabled() ? ('CHAT.RAG_ON' | translate) : ('CHAT.RAG_OFF' | translate) }}
              </button>
              <div class="context-window-control">
                <label>{{ 'CHAT.CONTEXT_WINDOW' | translate }}</label>
                <input type="number"
                       class="context-input"
                       [value]="store.maxContextMessages() ?? 50"
                       [placeholder]="'CHAT.CONTEXT_WINDOW_PLACEHOLDER' | translate"
                       min="1"
                       max="500"
                       (change)="onContextWindowChange($event)" />
                @if (store.maxContextMessages() !== null && store.messageCount() > store.maxContextMessages()!) {
                  <span class="context-indicator">
                    {{ 'CHAT.CONTEXT_INDICATOR' | translate:{ count: store.maxContextMessages(), total: store.messageCount() } }}
                  </span>
                }
              </div>
            }
            <div class="control-group">
              <label>{{ 'CHAT.PROVIDER' | translate }}</label>
              <select [ngModel]="store.selectedProvider()" (ngModelChange)="store.setProvider($event)">
                <option value="OLLAMA">Ollama</option>
                <option value="OPENAI">OpenAI</option>
                <option value="ANTHROPIC">Anthropic</option>
              </select>
            </div>
            <div class="control-group">
              <label>{{ 'CHAT.MODEL' | translate }}</label>
              <select [ngModel]="store.selectedModel()" (ngModelChange)="store.setModel($event)">
                @for (model of availableModels(); track model) {
                  <option [value]="model">{{ model }}</option>
                }
              </select>
            </div>
          </div>
        </div>

        @if (showSystemPrompt() && store.advancedMode()) {
          <div class="system-prompt-panel">
            <div class="sp-header">
              <span class="sp-label">{{ 'CHAT.SYSTEM_PROMPT' | translate }}</span>
              @if (store.currentSystemPrompt()) {
                <span class="sp-active-badge">{{ 'CHAT.SYSTEM_PROMPT_ACTIVE' | translate }}</span>
              }
            </div>
            <textarea
              class="sp-textarea"
              [(ngModel)]="systemPromptInput"
              [placeholder]="'CHAT.SYSTEM_PROMPT_PLACEHOLDER' | translate"
              rows="3"
              maxlength="5000"
              name="systemPrompt"
            ></textarea>
            <div class="sp-char-counter" [class.near-limit]="systemPromptInput.length > 4500" [class.at-limit]="systemPromptInput.length >= 5000">
              {{ systemPromptInput.length }} / 5000
            </div>
            <div class="sp-actions">
              <button class="sp-btn save" (click)="saveSystemPrompt()" [disabled]="!systemPromptDirty()">
                {{ 'COMMON.SAVE' | translate }}
              </button>
              <button class="sp-btn clear" (click)="clearSystemPrompt()" [disabled]="!store.currentSystemPrompt()">
                {{ 'CHAT.SYSTEM_PROMPT_CLEAR' | translate }}
              </button>
            </div>
          </div>
        }

        <div class="messages-area" #messagesArea>
          @if (store.messages().length === 0 && !store.isLoading()) {
            <div class="welcome">
              <div class="welcome-icon">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
                </svg>
              </div>
              <h3>{{ 'CHAT.WELCOME_TITLE' | translate }}</h3>
              <p>{{ 'CHAT.WELCOME_SUBTITLE' | translate }}</p>
              <div class="suggestions">
                <button class="suggestion" (click)="useSuggestion(i18n.instant('CHAT.SUGGESTION_RAG'))">
                  {{ 'CHAT.SUGGESTION_RAG' | translate }}
                </button>
                <button class="suggestion" (click)="useSuggestion(i18n.instant('CHAT.SUGGESTION_SUMMARIZE'))">
                  {{ 'CHAT.SUGGESTION_SUMMARIZE' | translate }}
                </button>
                <button class="suggestion" (click)="useSuggestion(i18n.instant('CHAT.SUGGESTION_MODELS'))">
                  {{ 'CHAT.SUGGESTION_MODELS' | translate }}
                </button>
              </div>
            </div>
          } @else {
            @for (msg of store.messages(); track $index) {
              <div class="message" [class]="msg.role.toLowerCase()">
                <div class="msg-avatar">
                  @if (msg.role === 'USER') {
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
                  } @else if (msg.role === 'TOOL') {
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M14.7 6.3a1 1 0 000 1.4l1.6 1.6-3.3 3.3-1.6-1.6a1 1 0 00-1.4 0l-4 4a1 1 0 101.4 1.4L11 13l1.6 1.6a1 1 0 001.4 0l4-4a1 1 0 000-1.4L16.4 7.6l1.3-1.3a1 1 0 00-1.4-1.4L15 6.3a1 1 0 00-.3-.7 1 1 0 00-.7-.3 1 1 0 00-.7.3z"/></svg>
                  } @else {
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="12" r="3"/><path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83"/></svg>
                  }
                </div>
                <div class="msg-content">
                  @if (msg.role === 'TOOL') {
                    <div class="msg-role tool-role">
                      {{ 'CHAT.TOOL_RESULT' | translate }}
                      @if (msg.metadata?.['toolName']) {
                        <span class="tool-name-badge">{{ msg.metadata?.['toolName'] }}</span>
                      }
                    </div>
                  } @else {
                    <div class="msg-role">{{ msg.role === 'USER' ? ('CHAT.ROLE_USER' | translate) : ('CHAT.ROLE_AI' | translate) }}</div>
                  }
                  <div class="msg-text" [innerHTML]="formatMessage(msg.content)"></div>
                </div>
              </div>
            }
            @if (store.isLoading() && !store.isStreaming()) {
              <div class="message assistant">
                <div class="msg-avatar">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="12" r="3"/></svg>
                </div>
                <div class="msg-content">
                  <div class="msg-role">{{ 'CHAT.ROLE_AI' | translate }}</div>
                  <div class="typing-indicator">
                    <span></span><span></span><span></span>
                  </div>
                </div>
              </div>
            }
          }
        </div>

        @if (store.error()) {
          <div class="error-bar">{{ store.error() }}</div>
        }

        <div class="input-area">
          <textarea
            [(ngModel)]="userInput"
            (keydown.enter)="onEnter($event)"
            [placeholder]="'CHAT.PLACEHOLDER' | translate"
            [disabled]="store.isLoading()"
            rows="1"
            #inputField
          ></textarea>
          <button class="send-btn" (click)="sendMessage()" [disabled]="store.isLoading() || store.isStreaming() || !userInput.trim()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .chat-with-sidebar {
      display: flex;
      height: calc(100vh - 4rem);
    }
    .chat-layout {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-width: 0;
      max-width: 900px;
      margin: 0 auto;
      padding: 0 1rem;
    }
    .chat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 1rem;
      border-bottom: 1px solid var(--color-border, #e0e0e0);
      margin-bottom: 0.5rem;
      flex-wrap: wrap;
      gap: 0.75rem;
    }
    .header-left h2 { margin: 0; font-size: 1.2rem; }
    .msg-count { font-size: 0.75rem; color: var(--color-text-muted); }
    .header-controls { display: flex; align-items: end; gap: 0.75rem; flex-wrap: wrap; }
    .mode-toggle {
      display: flex; align-items: center; gap: 0.3rem;
      padding: 0.35rem 0.6rem; border: 1px solid var(--color-border); border-radius: 6px;
      background: var(--color-card-bg); font-size: 0.75rem; color: var(--color-text-secondary);
      cursor: pointer; transition: all 0.15s; font-family: inherit;
    }
    .mode-toggle:hover { border-color: var(--color-primary); color: var(--color-primary); }
    .mode-toggle.advanced { background: var(--color-msg-user-bg); border-color: var(--color-primary); color: var(--color-primary); }
    .system-prompt-toggle {
      display: flex;
      align-items: center;
      gap: 0.3rem;
      padding: 0.35rem 0.6rem;
      border: 1px solid var(--color-border);
      border-radius: 6px;
      background: var(--color-card-bg);
      font-size: 0.75rem;
      color: var(--color-text-secondary);
      cursor: pointer;
      transition: all 0.15s;
    }
    .system-prompt-toggle:hover { border-color: var(--color-primary); color: var(--color-primary); }
    .system-prompt-toggle.active { background: var(--color-msg-user-bg); border-color: var(--color-primary); color: var(--color-primary); }
    .tool-calling-toggle {
      display: flex;
      align-items: center;
      gap: 0.3rem;
      padding: 0.35rem 0.6rem;
      border: 1px solid var(--color-border);
      border-radius: 6px;
      background: var(--color-card-bg);
      font-size: 0.75rem;
      color: var(--color-text-secondary);
      cursor: pointer;
      transition: all 0.15s;
    }
    .tool-calling-toggle:hover { border-color: var(--color-msg-tool-border); color: var(--color-msg-tool-border); }
    .tool-calling-toggle.active { background: var(--color-msg-tool-bg); border-color: var(--color-msg-tool-border); color: var(--color-msg-tool-border); }
    .rag-toggle {
      display: flex;
      align-items: center;
      gap: 0.3rem;
      padding: 0.35rem 0.6rem;
      border: 1px solid var(--color-border);
      border-radius: 6px;
      background: var(--color-card-bg);
      font-size: 0.75rem;
      color: var(--color-text-secondary);
      cursor: pointer;
      transition: all 0.15s;
    }
    .rag-toggle:hover { border-color: var(--color-success); color: var(--color-success); }
    .rag-toggle.active { background: var(--color-success-bg); border-color: var(--color-success); color: var(--color-success); }

    .context-window-control {
      display: flex;
      align-items: center;
      gap: 0.3rem;
      padding: 0.35rem 0.6rem;
      border: 1px solid var(--color-border);
      border-radius: 6px;
      background: var(--color-card-bg);
      font-size: 0.75rem;
      color: var(--color-text-secondary);
    }
    .context-window-control label {
      font-size: 0.65rem;
      color: var(--color-text-muted);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      white-space: nowrap;
    }
    .context-input {
      width: 52px;
      padding: 0.2rem 0.3rem;
      border: 1px solid var(--color-border);
      border-radius: 4px;
      font-size: 0.75rem;
      text-align: center;
      background: var(--color-card-bg);
    }
    .context-input:focus { outline: none; border-color: var(--color-primary); }
    .context-indicator {
      font-size: 0.65rem;
      color: #e67e22;
      white-space: nowrap;
    }

    .system-prompt-panel {
      background: var(--color-hover-bg);
      border: 1px solid var(--color-border-light);
      border-radius: 8px;
      padding: 0.75rem;
      margin-bottom: 0.5rem;
    }
    .sp-header { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.4rem; }
    .sp-label { font-size: 0.75rem; font-weight: 600; color: var(--color-text-secondary); text-transform: uppercase; letter-spacing: 0.3px; }
    .sp-active-badge {
      font-size: 0.65rem;
      background: var(--color-success-bg);
      color: var(--color-success-text);
      padding: 0.1rem 0.4rem;
      border-radius: 4px;
    }
    .sp-textarea {
      width: 100%;
      padding: 0.5rem;
      border: 1px solid var(--color-border);
      border-radius: 6px;
      font-size: 0.8rem;
      font-family: inherit;
      resize: vertical;
      min-height: 60px;
      max-height: 150px;
      line-height: 1.4;
      box-sizing: border-box;
    }
    .sp-textarea:focus { outline: none; border-color: var(--color-primary); }
    .sp-char-counter { font-size: 0.7rem; color: var(--color-text-muted); text-align: right; margin-top: 0.25rem; }
    .sp-char-counter.near-limit { color: var(--color-warning); }
    .sp-char-counter.at-limit { color: var(--color-error); font-weight: 600; }
    .sp-actions { display: flex; gap: 0.5rem; margin-top: 0.4rem; }
    .sp-btn {
      padding: 0.3rem 0.7rem;
      border: 1px solid var(--color-border);
      border-radius: 5px;
      font-size: 0.75rem;
      cursor: pointer;
      transition: all 0.15s;
    }
    .sp-btn:disabled { opacity: 0.4; cursor: not-allowed; }
    .sp-btn.save { background: var(--color-primary); color: white; border-color: var(--color-primary); }
    .sp-btn.save:hover:not(:disabled) { background: var(--color-primary-light); }
    .sp-btn.clear { background: var(--color-card-bg); color: var(--color-error-text); border-color: var(--color-border); }
    .sp-btn.clear:hover:not(:disabled) { border-color: var(--color-error-text); }
    .control-group { display: flex; flex-direction: column; gap: 0.15rem; }
    .control-group label { font-size: 0.65rem; color: var(--color-text-muted); text-transform: uppercase; letter-spacing: 0.5px; }
    .control-group select {
      padding: 0.35rem 0.5rem;
      border: 1px solid var(--color-border);
      border-radius: 6px;
      font-size: 0.8rem;
      background: var(--color-card-bg);
      cursor: pointer;
    }

    .messages-area {
      flex: 1;
      overflow-y: auto;
      padding: 1rem 0;
    }

    .welcome {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      text-align: center;
      color: var(--color-text-muted);
    }
    .welcome-icon { color: var(--color-border); margin-bottom: 1rem; }
    .welcome h3 { color: var(--color-text-secondary); margin-bottom: 0.35rem; font-size: 1.1rem; }
    .welcome p { font-size: 0.85rem; margin-bottom: 1.5rem; }
    .suggestions { display: flex; flex-wrap: wrap; gap: 0.5rem; justify-content: center; }
    .suggestion {
      padding: 0.5rem 1rem;
      background: var(--color-card-bg);
      border: 1px solid var(--color-border);
      border-radius: 20px;
      cursor: pointer;
      font-size: 0.8rem;
      color: var(--color-text-secondary);
      transition: all 0.15s;
    }
    .suggestion:hover { border-color: var(--color-primary); color: var(--color-primary); }

    .message {
      display: flex;
      gap: 0.75rem;
      margin-bottom: 1.25rem;
      padding: 0.75rem;
      border-radius: 10px;
    }
    .message.user { background: var(--color-msg-user-bg); }
    .message.assistant { background: var(--color-card-bg); box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
    .message.tool { background: var(--color-msg-tool-bg); border-left: 3px solid var(--color-msg-tool-border); }
    .msg-avatar {
      width: 30px;
      height: 30px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .message.user .msg-avatar { background: var(--color-primary); color: white; }
    .message.assistant .msg-avatar { background: var(--color-success-bg); color: var(--color-success-text); }
    .message.tool .msg-avatar { background: var(--color-msg-tool-bg); color: var(--color-msg-tool-border); }
    .tool-role { color: var(--color-msg-tool-border) !important; }
    .tool-name-badge { font-family: monospace; background: var(--color-msg-tool-bg); color: var(--color-msg-tool-border); padding: 0.05rem 0.35rem; border-radius: 3px; font-size: 0.65rem; margin-left: 0.3rem; }
    .msg-content { flex: 1; min-width: 0; }
    .msg-role { font-size: 0.7rem; font-weight: 600; color: var(--color-text-muted); text-transform: uppercase; margin-bottom: 0.25rem; }
    .msg-text {
      font-size: 0.9rem;
      line-height: 1.6;
      color: var(--color-text);
      word-wrap: break-word;
    }
    .msg-text pre {
      background: var(--color-code-bg);
      color: var(--color-code-text);
      padding: 0.75rem;
      border-radius: 6px;
      overflow-x: auto;
      font-size: 0.8rem;
      margin: 0.5rem 0;
      font-family: 'Consolas', 'Fira Code', monospace;
    }
    .msg-text code {
      background: var(--color-skeleton-base);
      padding: 0.1rem 0.3rem;
      border-radius: 3px;
      font-size: 0.85em;
      font-family: 'Consolas', 'Fira Code', monospace;
    }
    .msg-text pre code { background: none; padding: 0; }

    .typing-indicator {
      display: flex;
      gap: 4px;
      padding: 0.5rem 0;
    }
    .typing-indicator span {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: var(--color-text-muted);
      animation: typing 1.4s infinite;
    }
    .typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
    .typing-indicator span:nth-child(3) { animation-delay: 0.4s; }
    @keyframes typing {
      0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
      30% { opacity: 1; transform: scale(1); }
    }

    .error-bar {
      background: var(--color-error-bg);
      color: var(--color-error-text);
      padding: 0.5rem 1rem;
      border-radius: 6px;
      font-size: 0.8rem;
      margin-bottom: 0.5rem;
    }

    .input-area {
      display: flex;
      gap: 0.5rem;
      padding: 0.75rem 0 0;
      border-top: 1px solid var(--color-border-light);
    }
    .input-area textarea {
      flex: 1;
      padding: 0.75rem 1rem;
      border: 1px solid var(--color-border);
      border-radius: 10px;
      font-size: 0.9rem;
      font-family: inherit;
      resize: none;
      min-height: 44px;
      max-height: 120px;
      line-height: 1.4;
    }
    .input-area textarea:focus { outline: none; border-color: var(--color-primary); }
    .send-btn {
      width: 44px;
      height: 44px;
      background: var(--color-primary);
      color: white;
      border: none;
      border-radius: 10px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: background 0.15s;
    }
    .send-btn:hover:not(:disabled) { background: var(--color-primary-light); }
    .send-btn:disabled { opacity: 0.4; cursor: not-allowed; }
  `]
})
export class ChatPageComponent implements AfterViewChecked {
  store = inject(ChatStore);
  private chatService = inject(ChatService);
  private chatStreamService = inject(ChatStreamService);
  private conversationService = inject(ConversationService);
  i18n = inject(TranslationService);
  userInput = '';
  systemPromptInput = '';

  @ViewChild('messagesArea') messagesArea!: ElementRef;
  @ViewChild('sidebar') sidebar!: ChatSidebarComponent;
  private shouldScroll = false;

  showSystemPrompt = signal(false);
  availableModels = signal<string[]>(['llama3.2', 'llama3.1', 'mistral', 'codellama', 'phi3', 'gemma2']);

  ngAfterViewChecked() {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  onEnter(event: Event) {
    const ke = event as KeyboardEvent;
    if (!ke.shiftKey) {
      ke.preventDefault();
      this.sendMessage();
    }
  }

  toggleSystemPrompt() {
    const newVal = !this.showSystemPrompt();
    this.showSystemPrompt.set(newVal);
    if (newVal) {
      this.systemPromptInput = this.store.currentSystemPrompt() || '';
    }
  }

  systemPromptDirty(): boolean {
    return this.systemPromptInput.trim() !== (this.store.currentSystemPrompt() || '');
  }

  saveSystemPrompt() {
    const prompt = this.systemPromptInput.trim() || null;
    this.store.setSystemPrompt(prompt);
    const convId = this.store.currentConversationId();
    if (convId) {
      this.conversationService.updateSystemPrompt(convId, prompt).subscribe();
    }
  }

  onContextWindowChange(event: Event) {
    const input = event.target as HTMLInputElement;
    const value = input.value ? parseInt(input.value, 10) : null;
    if (value !== null && (isNaN(value) || value < 1)) return;
    this.store.setMaxContextMessages(value);
    const convId = this.store.currentConversationId();
    if (convId) {
      this.conversationService.updateContextWindow(convId, value).subscribe();
    }
  }

  clearSystemPrompt() {
    this.systemPromptInput = '';
    this.store.setSystemPrompt(null);
    const convId = this.store.currentConversationId();
    if (convId) {
      this.conversationService.updateSystemPrompt(convId, null).subscribe();
    }
  }

  useSuggestion(text: string) {
    this.userInput = text;
    this.sendMessage();
  }

  sendMessage() {
    const content = this.userInput.trim();
    if (!content) return;

    this.store.addMessage({ role: 'USER', content });
    this.userInput = '';
    this.store.setError(null);
    this.shouldScroll = true;

    // Usa streaming se tool calling NON è abilitato
    if (!this.store.toolCallingEnabled()) {
      this.sendStreamingMessage(content);
    } else {
      this.sendSyncMessage(content);
    }
  }

  private async sendStreamingMessage(content: string) {
    this.store.setStreaming(true);
    this.store.setLoading(true);
    this.store.addStreamingAssistantMessage();
    this.shouldScroll = true;

    try {
      await this.chatStreamService.streamChat({
        message: content,
        provider: this.store.selectedProvider(),
        model: this.store.selectedModel(),
        conversationId: this.store.currentConversationId() ?? undefined,
        systemPrompt: this.store.currentSystemPrompt() ?? undefined,
        enableRag: this.store.ragEnabled() || undefined
      }, (event) => {
        switch (event.type) {
          case 'conversation':
            if (event.data.conversationId && !this.store.currentConversationId()) {
              this.store.setCurrentConversation(
                event.data.conversationId,
                content.length > 100 ? content.substring(0, 100) + '...' : content
              );
              this.sidebar?.loadConversations();
            }
            break;
          case 'token':
            this.store.appendTokenToLastMessage(event.data.content);
            this.shouldScroll = true;
            break;
          case 'done':
            this.store.setStreaming(false);
            this.store.setLoading(false);
            this.shouldScroll = true;
            break;
          case 'error':
            this.store.setError(event.data.message || this.i18n.instant('CHAT.ERROR_SERVER'));
            this.store.setStreaming(false);
            this.store.setLoading(false);
            break;
        }
      });
    } catch (e: any) {
      // Fallback a sync se streaming fallisce (es. AbortError non è errore)
      if (e?.name !== 'AbortError') {
        this.store.setError(this.i18n.instant('CHAT.ERROR_SERVER'));
      }
      this.store.setStreaming(false);
      this.store.setLoading(false);
      this.shouldScroll = true;
    }
  }

  private sendSyncMessage(content: string) {
    this.store.setLoading(true);

    this.chatService.chat({
      message: content,
      provider: this.store.selectedProvider(),
      model: this.store.selectedModel(),
      conversationId: this.store.currentConversationId() ?? undefined,
      systemPrompt: this.store.currentSystemPrompt() ?? undefined,
      enableToolCalling: this.store.toolCallingEnabled() || undefined,
      enableRag: this.store.ragEnabled() || undefined
    }).subscribe({
      next: (response) => {
        this.store.addMessage({ role: 'ASSISTANT', content: response.content });
        if (response.conversationId && !this.store.currentConversationId()) {
          this.store.setCurrentConversation(
            response.conversationId,
            content.length > 100 ? content.substring(0, 100) + '...' : content
          );
          this.sidebar?.loadConversations();
        }
        this.store.setLoading(false);
        this.shouldScroll = true;
      },
      error: () => {
        this.store.setError(this.i18n.instant('CHAT.ERROR_SERVER'));
        this.store.setLoading(false);
        this.shouldScroll = true;
      }
    });
  }

  formatMessage(text: string): string {
    let html = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');

    // Code blocks
    html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre><code>$2</code></pre>');
    // Inline code
    html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
    // Bold
    html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    // Line breaks
    html = html.replace(/\n/g, '<br>');

    return html;
  }

  private scrollToBottom() {
    if (this.messagesArea?.nativeElement) {
      const el = this.messagesArea.nativeElement;
      el.scrollTop = el.scrollHeight;
    }
  }
}
