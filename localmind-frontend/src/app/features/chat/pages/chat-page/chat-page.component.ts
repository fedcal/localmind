import { Component, inject, signal, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChatStore } from '../../state/chat.store';
import { ChatService } from '../../services/chat.service';

@Component({
  selector: 'app-chat-page',
  imports: [FormsModule],
  template: `
    <div class="chat-layout">
      <div class="chat-header">
        <div class="header-left">
          <h2>Chat AI</h2>
          <span class="msg-count">{{ store.messageCount() }} messaggi</span>
        </div>
        <div class="header-controls">
          <div class="control-group">
            <label>Provider</label>
            <select [ngModel]="store.selectedProvider()" (ngModelChange)="store.setProvider($event)">
              <option value="OLLAMA">Ollama</option>
              <option value="OPENAI">OpenAI</option>
              <option value="ANTHROPIC">Anthropic</option>
            </select>
          </div>
          <div class="control-group">
            <label>Modello</label>
            <select [ngModel]="store.selectedModel()" (ngModelChange)="store.setModel($event)">
              @for (model of availableModels(); track model) {
                <option [value]="model">{{ model }}</option>
              }
            </select>
          </div>
          <button class="btn-icon" (click)="store.clearMessages()" title="Nuova chat">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 5v14M5 12h14"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="messages-area" #messagesArea>
        @if (store.messages().length === 0 && !store.isLoading()) {
          <div class="welcome">
            <div class="welcome-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
              </svg>
            </div>
            <h3>Benvenuto in LocalMind</h3>
            <p>Inizia una conversazione con il tuo assistente AI locale.</p>
            <div class="suggestions">
              <button class="suggestion" (click)="useSuggestion('Spiega come funziona il RAG')">
                Spiega come funziona il RAG
              </button>
              <button class="suggestion" (click)="useSuggestion('Riassumi i documenti caricati')">
                Riassumi i documenti caricati
              </button>
              <button class="suggestion" (click)="useSuggestion('Quali modelli LLM sono disponibili?')">
                Quali modelli sono disponibili?
              </button>
            </div>
          </div>
        } @else {
          @for (msg of store.messages(); track $index) {
            <div class="message" [class]="msg.role.toLowerCase()">
              <div class="msg-avatar">
                @if (msg.role === 'USER') {
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
                } @else {
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="12" r="3"/><path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83"/></svg>
                }
              </div>
              <div class="msg-content">
                <div class="msg-role">{{ msg.role === 'USER' ? 'Tu' : 'AI' }}</div>
                <div class="msg-text" [innerHTML]="formatMessage(msg.content)"></div>
              </div>
            </div>
          }
          @if (store.isLoading()) {
            <div class="message assistant">
              <div class="msg-avatar">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="12" r="3"/></svg>
              </div>
              <div class="msg-content">
                <div class="msg-role">AI</div>
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
          placeholder="Scrivi un messaggio..."
          [disabled]="store.isLoading()"
          rows="1"
          #inputField
        ></textarea>
        <button class="send-btn" (click)="sendMessage()" [disabled]="store.isLoading() || !userInput.trim()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
            <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
          </svg>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .chat-layout {
      display: flex;
      flex-direction: column;
      height: calc(100vh - 4rem);
      max-width: 900px;
      margin: 0 auto;
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
    .msg-count { font-size: 0.75rem; color: #999; }
    .header-controls { display: flex; align-items: end; gap: 0.75rem; }
    .control-group { display: flex; flex-direction: column; gap: 0.15rem; }
    .control-group label { font-size: 0.65rem; color: #999; text-transform: uppercase; letter-spacing: 0.5px; }
    .control-group select {
      padding: 0.35rem 0.5rem;
      border: 1px solid #ddd;
      border-radius: 6px;
      font-size: 0.8rem;
      background: white;
      cursor: pointer;
    }
    .btn-icon {
      padding: 0.4rem;
      background: none;
      border: 1px solid #ddd;
      border-radius: 6px;
      cursor: pointer;
      color: #666;
      transition: all 0.15s;
      display: flex;
      align-items: center;
    }
    .btn-icon:hover { background: #f5f5f5; color: #333; }

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
      color: #999;
    }
    .welcome-icon { color: #ccc; margin-bottom: 1rem; }
    .welcome h3 { color: #555; margin-bottom: 0.35rem; font-size: 1.1rem; }
    .welcome p { font-size: 0.85rem; margin-bottom: 1.5rem; }
    .suggestions { display: flex; flex-wrap: wrap; gap: 0.5rem; justify-content: center; }
    .suggestion {
      padding: 0.5rem 1rem;
      background: white;
      border: 1px solid #ddd;
      border-radius: 20px;
      cursor: pointer;
      font-size: 0.8rem;
      color: #555;
      transition: all 0.15s;
    }
    .suggestion:hover { border-color: #0f3460; color: #0f3460; }

    .message {
      display: flex;
      gap: 0.75rem;
      margin-bottom: 1.25rem;
      padding: 0.75rem;
      border-radius: 10px;
    }
    .message.user { background: #f0f4ff; }
    .message.assistant { background: white; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
    .msg-avatar {
      width: 30px;
      height: 30px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .message.user .msg-avatar { background: #0f3460; color: white; }
    .message.assistant .msg-avatar { background: #e8f5e9; color: #2e7d32; }
    .msg-content { flex: 1; min-width: 0; }
    .msg-role { font-size: 0.7rem; font-weight: 600; color: #999; text-transform: uppercase; margin-bottom: 0.25rem; }
    .msg-text {
      font-size: 0.9rem;
      line-height: 1.6;
      color: #333;
      word-wrap: break-word;
    }
    .msg-text pre {
      background: #1e1e2e;
      color: #cdd6f4;
      padding: 0.75rem;
      border-radius: 6px;
      overflow-x: auto;
      font-size: 0.8rem;
      margin: 0.5rem 0;
      font-family: 'Consolas', 'Fira Code', monospace;
    }
    .msg-text code {
      background: #f0f0f0;
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
      background: #999;
      animation: typing 1.4s infinite;
    }
    .typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
    .typing-indicator span:nth-child(3) { animation-delay: 0.4s; }
    @keyframes typing {
      0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
      30% { opacity: 1; transform: scale(1); }
    }

    .error-bar {
      background: #fdf2f2;
      color: #c62828;
      padding: 0.5rem 1rem;
      border-radius: 6px;
      font-size: 0.8rem;
      margin-bottom: 0.5rem;
    }

    .input-area {
      display: flex;
      gap: 0.5rem;
      padding: 0.75rem 0 0;
      border-top: 1px solid #eee;
    }
    .input-area textarea {
      flex: 1;
      padding: 0.75rem 1rem;
      border: 1px solid #ddd;
      border-radius: 10px;
      font-size: 0.9rem;
      font-family: inherit;
      resize: none;
      min-height: 44px;
      max-height: 120px;
      line-height: 1.4;
    }
    .input-area textarea:focus { outline: none; border-color: #0f3460; }
    .send-btn {
      width: 44px;
      height: 44px;
      background: #0f3460;
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
    .send-btn:hover:not(:disabled) { background: #1a4a7a; }
    .send-btn:disabled { opacity: 0.4; cursor: not-allowed; }
  `]
})
export class ChatPageComponent implements AfterViewChecked {
  store = inject(ChatStore);
  private chatService = inject(ChatService);
  userInput = '';

  @ViewChild('messagesArea') messagesArea!: ElementRef;
  private shouldScroll = false;

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

  useSuggestion(text: string) {
    this.userInput = text;
    this.sendMessage();
  }

  sendMessage() {
    const content = this.userInput.trim();
    if (!content) return;

    this.store.addMessage({ role: 'USER', content });
    this.userInput = '';
    this.store.setLoading(true);
    this.store.setError(null);
    this.shouldScroll = true;

    this.chatService.chat({
      message: content,
      provider: this.store.selectedProvider(),
      model: this.store.selectedModel()
    }).subscribe({
      next: (response) => {
        this.store.addMessage({ role: 'ASSISTANT', content: response.content });
        this.store.setLoading(false);
        this.shouldScroll = true;
      },
      error: (err) => {
        this.store.setError('Errore nella comunicazione con il server. Verifica che il backend sia attivo.');
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
