import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SettingsService } from '../../services/settings.service';
import { LlmProviderConfig, CreateProviderRequest } from '../../models/settings.model';

@Component({
  selector: 'app-settings-page',
  imports: [FormsModule],
  template: `
    <div class="settings-page">
      <div class="page-header">
        <div>
          <h1>Impostazioni</h1>
          <p class="subtitle">Configurazione provider LLM e preferenze</p>
        </div>
        <button class="btn btn-primary" (click)="toggleForm()">
          {{ showForm ? 'Annulla' : '+ Aggiungi Provider' }}
        </button>
      </div>

      @if (showForm) {
        <div class="add-form card">
          <h3>Nuovo Provider LLM</h3>
          <div class="form-grid">
            <div class="form-group">
              <label>Nome *</label>
              <input type="text" [(ngModel)]="newProvider.name" placeholder="Es: Ollama Locale">
            </div>
            <div class="form-group">
              <label>Tipo *</label>
              <select [(ngModel)]="newProvider.type" (ngModelChange)="onTypeChange($event)">
                <option value="OLLAMA">Ollama</option>
                <option value="OPENAI">OpenAI</option>
                <option value="ANTHROPIC">Anthropic</option>
                <option value="GOOGLE">Google Gemini</option>
              </select>
            </div>
            <div class="form-group">
              <label>URL Base *</label>
              <input type="text" [(ngModel)]="newProvider.baseUrl"
                     (blur)="onBaseUrlBlur()"
                     [placeholder]="getUrlPlaceholder()">
            </div>
            @if (newProvider.type !== 'OLLAMA') {
              <div class="form-group">
                <label>API Key *</label>
                <input type="password" [(ngModel)]="newProvider.apiKey"
                       [placeholder]="getApiKeyPlaceholder()">
              </div>
            }
            <div class="form-group">
              <label>Modello Predefinito</label>
              @if (newProvider.type === 'OLLAMA') {
                <div class="select-with-refresh">
                  <select [(ngModel)]="newProvider.defaultModel" [disabled]="loadingModels()">
                    <option value="">-- Seleziona modello --</option>
                    @for (model of ollamaModels(); track model) {
                      <option [value]="model">{{ model }}</option>
                    }
                  </select>
                  <button class="btn-icon" (click)="fetchOllamaModels()" [disabled]="loadingModels()"
                          title="Aggiorna lista modelli">
                    @if (loadingModels()) {
                      <span class="spinner-sm"></span>
                    } @else {
                      &#x21bb;
                    }
                  </button>
                </div>
                @if (ollamaModels().length === 0 && !loadingModels() && ollamaModelsFetched()) {
                  <span class="hint error">Nessun modello trovato. Verifica URL e che Ollama sia in esecuzione.</span>
                }
              } @else {
                <input type="text" [(ngModel)]="newProvider.defaultModel"
                       [placeholder]="getModelPlaceholder()">
              }
            </div>
          </div>
          <button class="btn btn-primary" (click)="addProvider()"
                  [disabled]="!isFormValid()">
            Salva Provider
          </button>
        </div>
      }

      @if (notification()) {
        <div class="notification" [class]="notification()!.type">
          {{ notification()!.message }}
        </div>
      }

      @if (loading()) {
        <div class="loading-state">
          <span class="spinner"></span>
          Caricamento provider...
        </div>
      } @else if (providers().length === 0) {
        <div class="empty-state card">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4"/>
          </svg>
          <h3>Nessun provider configurato</h3>
          <p>Aggiungi un provider LLM per iniziare a usare la chat.</p>
        </div>
      } @else {
        <div class="provider-list">
          @for (provider of providers(); track provider.id) {
            <div class="provider-card" [class.disabled]="!provider.enabled">
              <div class="provider-header">
                <div class="provider-name-row">
                  <span class="provider-type-icon" [class]="provider.type.toLowerCase()">
                    {{ provider.type.charAt(0) }}
                  </span>
                  <div>
                    <div class="provider-name">{{ provider.name }}</div>
                    <div class="provider-url">{{ provider.baseUrl }}</div>
                  </div>
                </div>
                <span class="badge" [class]="provider.enabled ? 'badge-success' : 'badge-neutral'">
                  {{ provider.enabled ? 'Attivo' : 'Disattivato' }}
                </span>
              </div>
              <div class="provider-details">
                <div class="detail-row">
                  <span class="detail-label">Tipo:</span>
                  <span class="badge badge-primary">{{ provider.type }}</span>
                </div>
                @if (provider.defaultModel) {
                  <div class="detail-row">
                    <span class="detail-label">Modello default:</span>
                    <span class="mono">{{ provider.defaultModel }}</span>
                  </div>
                }
                @if (provider.models.length > 0) {
                  <div class="detail-row">
                    <span class="detail-label">Modelli:</span>
                    <span class="models-list">
                      @for (model of provider.models; track model) {
                        <span class="model-tag">{{ model }}</span>
                      }
                    </span>
                  </div>
                }
              </div>
              <div class="provider-actions">
                <button class="btn btn-sm btn-secondary" (click)="testProvider(provider)"
                        [disabled]="testingId() === provider.id">
                  @if (testingId() === provider.id) {
                    <span class="spinner-sm"></span>
                  }
                  Test Connessione
                </button>
                <button class="btn btn-sm btn-danger" (click)="deleteProvider(provider.id)">
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
    .settings-page { max-width: 900px; }
    .page-header { display: flex; justify-content: space-between; align-items: start; margin-bottom: 1.5rem; }
    .page-header h1 { margin: 0; font-size: 1.5rem; }
    .subtitle { color: #888; font-size: 0.85rem; margin: 0.15rem 0 0 0; }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: #0f3460; color: white; }
    .btn-primary:hover:not(:disabled) { background: #1a4a7a; }
    .btn-secondary { background: white; color: #333; border: 1px solid #ddd; }
    .btn-secondary:hover:not(:disabled) { background: #f5f5f5; }
    .btn-danger { background: transparent; color: #e74c3c; border: 1px solid #e74c3c; }
    .btn-danger:hover:not(:disabled) { background: #fef2f2; }
    .btn-sm { padding: 0.35rem 0.7rem; font-size: 0.8rem; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-icon { background: none; border: 1px solid #ddd; border-radius: 4px; padding: 0.4rem 0.6rem; cursor: pointer; font-size: 1rem; line-height: 1; display: flex; align-items: center; }
    .btn-icon:hover:not(:disabled) { background: #f5f5f5; }
    .btn-icon:disabled { opacity: 0.5; cursor: not-allowed; }
    .card { background: white; border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); padding: 1.25rem; }
    .add-form { margin-bottom: 1.5rem; }
    .add-form h3 { margin: 0 0 1rem 0; font-size: 1rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8rem; font-weight: 500; color: #333; }
    .form-group input, .form-group select {
      padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; width: 100%; box-sizing: border-box;
    }
    .select-with-refresh { display: flex; gap: 0.35rem; }
    .select-with-refresh select { flex: 1; }
    .hint { font-size: 0.7rem; margin-top: 0.15rem; }
    .hint.error { color: #e74c3c; }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: #d4edda; color: #155724; }
    .notification.error { background: #f8d7da; color: #721c24; }

    .loading-state { display: flex; align-items: center; gap: 0.5rem; justify-content: center; padding: 3rem; color: #999; }
    .spinner { display: inline-block; width: 16px; height: 16px; border: 2px solid #ddd; border-top-color: #0f3460; border-radius: 50%; animation: spin 0.6s linear infinite; }
    .spinner-sm { display: inline-block; width: 12px; height: 12px; border: 2px solid #ddd; border-top-color: #0f3460; border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }

    .empty-state { text-align: center; padding: 3rem; }
    .empty-state svg { margin-bottom: 1rem; }
    .empty-state h3 { color: #666; margin-bottom: 0.25rem; }
    .empty-state p { font-size: 0.85rem; color: #999; }

    .provider-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .provider-card {
      background: white; padding: 1.25rem; border-radius: 10px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06); border-left: 4px solid #2ecc71;
    }
    .provider-card.disabled { border-left-color: #ddd; opacity: 0.7; }
    .provider-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
    .provider-name-row { display: flex; align-items: center; gap: 0.75rem; }
    .provider-type-icon {
      width: 36px; height: 36px; border-radius: 8px;
      display: flex; align-items: center; justify-content: center;
      font-weight: 700; font-size: 0.85rem; color: white; flex-shrink: 0;
    }
    .provider-type-icon.ollama { background: #2ecc71; }
    .provider-type-icon.openai { background: #10a37f; }
    .provider-type-icon.anthropic { background: #d4a574; }
    .provider-type-icon.google { background: #4285f4; }
    .provider-name { font-weight: 600; font-size: 0.95rem; }
    .provider-url { font-size: 0.75rem; color: #999; font-family: monospace; }
    .badge { font-size: 0.65rem; padding: 0.1rem 0.45rem; border-radius: 10px; font-weight: 600; text-transform: uppercase; }
    .badge-success { background: #d4edda; color: #155724; }
    .badge-neutral { background: #e2e3e5; color: #383d41; }
    .badge-primary { background: #e3f2fd; color: #0f3460; }
    .provider-details { margin-bottom: 0.75rem; }
    .detail-row { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.35rem; font-size: 0.8rem; }
    .detail-label { color: #888; min-width: 100px; }
    .mono { font-family: monospace; font-size: 0.8rem; }
    .models-list { display: flex; flex-wrap: wrap; gap: 0.25rem; }
    .model-tag { background: #f0f0f0; padding: 0.1rem 0.4rem; border-radius: 4px; font-size: 0.7rem; font-family: monospace; }
    .provider-actions { display: flex; gap: 0.5rem; }

  `]
})
export class SettingsPageComponent implements OnInit {
  private settingsService = inject(SettingsService);

  providers = signal<LlmProviderConfig[]>([]);
  loading = signal(false);
  showForm = false;
  testingId = signal<string | null>(null);
  notification = signal<{ type: string; message: string } | null>(null);
  ollamaModels = signal<string[]>([]);
  loadingModels = signal(false);
  ollamaModelsFetched = signal(false);
  newProvider: CreateProviderRequest = { name: '', type: 'OLLAMA', baseUrl: 'http://localhost:11434', defaultModel: '' };

  ngOnInit() { this.loadProviders(); }

  toggleForm() {
    this.showForm = !this.showForm;
    if (this.showForm) {
      this.resetForm();
    }
  }

  loadProviders() {
    this.loading.set(true);
    this.settingsService.listProviders().subscribe({
      next: (p) => { this.providers.set(p); this.loading.set(false); },
      error: () => { this.loading.set(false); }
    });
  }

  onTypeChange(type: string) {
    this.newProvider.defaultModel = '';
    this.ollamaModels.set([]);
    this.ollamaModelsFetched.set(false);
    switch (type) {
      case 'OLLAMA':
        this.newProvider.baseUrl = 'http://localhost:11434';
        this.newProvider.apiKey = undefined;
        this.fetchOllamaModels();
        break;
      case 'OPENAI':
        this.newProvider.baseUrl = 'https://api.openai.com';
        break;
      case 'ANTHROPIC':
        this.newProvider.baseUrl = 'https://api.anthropic.com';
        break;
      case 'GOOGLE':
        this.newProvider.baseUrl = 'https://generativelanguage.googleapis.com';
        break;
    }
  }

  onBaseUrlBlur() {
    if (this.newProvider.type === 'OLLAMA' && this.newProvider.baseUrl?.trim()) {
      this.fetchOllamaModels();
    }
  }

  fetchOllamaModels() {
    const url = this.newProvider.baseUrl?.trim();
    if (!url) return;
    this.loadingModels.set(true);
    this.ollamaModelsFetched.set(false);
    this.settingsService.listOllamaModels(url).subscribe({
      next: (models) => {
        this.ollamaModels.set(models);
        this.loadingModels.set(false);
        this.ollamaModelsFetched.set(true);
        if (models.length > 0 && !this.newProvider.defaultModel) {
          this.newProvider.defaultModel = models[0];
        }
      },
      error: () => {
        this.ollamaModels.set([]);
        this.loadingModels.set(false);
        this.ollamaModelsFetched.set(true);
      }
    });
  }

  getUrlPlaceholder(): string {
    switch (this.newProvider.type) {
      case 'OLLAMA': return 'http://localhost:11434';
      case 'OPENAI': return 'https://api.openai.com';
      case 'ANTHROPIC': return 'https://api.anthropic.com';
      case 'GOOGLE': return 'https://generativelanguage.googleapis.com';
      default: return '';
    }
  }

  getApiKeyPlaceholder(): string {
    switch (this.newProvider.type) {
      case 'OPENAI': return 'sk-...';
      case 'ANTHROPIC': return 'sk-ant-...';
      case 'GOOGLE': return 'AIza...';
      default: return '';
    }
  }

  getModelPlaceholder(): string {
    switch (this.newProvider.type) {
      case 'OPENAI': return 'gpt-4o-mini';
      case 'ANTHROPIC': return 'claude-sonnet-4-20250514';
      case 'GOOGLE': return 'gemini-2.0-flash';
      default: return '';
    }
  }

  isFormValid(): boolean {
    if (!this.newProvider.name?.trim() || !this.newProvider.baseUrl?.trim()) return false;
    if (this.newProvider.type !== 'OLLAMA' && !this.newProvider.apiKey?.trim()) return false;
    return true;
  }

  addProvider() {
    if (!this.isFormValid()) return;
    this.settingsService.saveProvider(this.newProvider).subscribe({
      next: () => {
        this.showForm = false;
        this.resetForm();
        this.showNotify('success', 'Provider aggiunto');
        this.loadProviders();
      },
      error: () => this.showNotify('error', 'Errore nel salvataggio del provider')
    });
  }

  testProvider(provider: LlmProviderConfig) {
    this.testingId.set(provider.id);
    this.settingsService.testProvider(provider.id).subscribe({
      next: (res) => {
        this.testingId.set(null);
        this.showNotify(res.status === 'OK' ? 'success' : 'error',
          res.status === 'OK' ? `${provider.name}: Connessione riuscita` : `${provider.name}: ${res.message}`);
      },
      error: () => {
        this.testingId.set(null);
        this.showNotify('error', `${provider.name}: Connessione fallita`);
      }
    });
  }

  deleteProvider(id: string) {
    this.settingsService.deleteProvider(id).subscribe({
      next: () => { this.showNotify('success', 'Provider rimosso'); this.loadProviders(); },
      error: () => this.showNotify('error', 'Errore nella rimozione')
    });
  }

  private resetForm() {
    this.newProvider = { name: '', type: 'OLLAMA', baseUrl: 'http://localhost:11434', defaultModel: '' };
    this.ollamaModels.set([]);
    this.ollamaModelsFetched.set(false);
  }

  private showNotify(type: string, message: string) {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 4000);
  }
}
