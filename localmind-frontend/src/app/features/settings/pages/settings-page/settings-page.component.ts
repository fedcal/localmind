import { Component, inject, OnInit, signal, effect, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { SettingsService } from '../../services/settings.service';
import { LlmProviderConfig, CreateProviderRequest, OllamaStatus, OllamaModelDetail } from '../../models/settings.model';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { OllamaDownloadService } from '../../../../core/services/ollama-download.service';
import { LoadingSkeletonComponent } from '../../../../shared/components/loading-skeleton/loading-skeleton.component';
import { UrlValidatorDirective } from '../../../../shared/validators/url-validator.directive';
import { FieldErrorComponent } from '../../../../shared/components/field-error/field-error.component';

@Component({
  selector: 'app-settings-page',
  imports: [FormsModule, TranslatePipe, SlicePipe, LoadingSkeletonComponent, UrlValidatorDirective, FieldErrorComponent],
  template: `
    <div class="settings-page">
      <div class="page-header">
        <div>
          <h1>{{ 'SETTINGS.TITLE' | translate }}</h1>
          <p class="subtitle">{{ 'SETTINGS.SUBTITLE' | translate }}</p>
        </div>
        <button class="btn btn-primary" (click)="toggleForm()">
          {{ showForm ? ('COMMON.CANCEL' | translate) : ('SETTINGS.ADD_PROVIDER' | translate) }}
        </button>
      </div>

      <!-- Sezione Stato Ollama -->
      <div class="ollama-status-card" [class.online]="ollamaOnline() === true" [class.offline]="ollamaOnline() === false">
        <div class="ollama-status-header">
          <div class="ollama-title-row">
            <h3>{{ 'SETTINGS.OLLAMA_STATUS_TITLE' | translate }}</h3>
            @if (ollamaOnline() === null) {
              <span class="badge badge-neutral">
                <span class="spinner-sm"></span>
                {{ 'SETTINGS.OLLAMA_CHECKING' | translate }}
              </span>
            } @else if (ollamaOnline()) {
              <span class="badge badge-success">&#x2713; {{ 'SETTINGS.OLLAMA_ONLINE' | translate }}</span>
            } @else {
              <span class="badge badge-danger">&#x2717; {{ 'SETTINGS.OLLAMA_OFFLINE' | translate }}</span>
            }
          </div>
          <button class="btn-icon" (click)="checkOllamaStatus()" [disabled]="ollamaCheckLoading()"
                  [title]="'COMMON.REFRESH' | translate">
            @if (ollamaCheckLoading()) {
              <span class="spinner-sm"></span>
            } @else {
              &#x21bb;
            }
          </button>
        </div>

        @if (ollamaOnline() === true) {
          <div class="ollama-info">
            <span class="ollama-version">{{ 'SETTINGS.OLLAMA_VERSION' | translate }}: <strong>{{ ollamaVersion() }}</strong></span>
            <span class="ollama-count">{{ ollamaInstalledModels().length }} {{ 'SETTINGS.OLLAMA_MODELS_INSTALLED' | translate }}</span>
          </div>

          @if (ollamaInstalledModels().length > 0) {
            <div class="ollama-models-table">
              <div class="table-header">
                <span>{{ 'SETTINGS.OLLAMA_MODEL_NAME' | translate }}</span>
                <span>{{ 'SETTINGS.OLLAMA_MODEL_SIZE' | translate }}</span>
                <span>{{ 'SETTINGS.OLLAMA_MODEL_MODIFIED' | translate }}</span>
                <span></span>
              </div>
              @for (model of ollamaInstalledModels(); track model.name) {
                <div class="table-row">
                  <span class="mono">{{ model.name }}</span>
                  <span>{{ model.size }}</span>
                  <span>{{ model.modifiedAt | slice:0:10 }}</span>
                  <span>
                    <button class="model-action delete-model" (click)="deleteInstalledModel(model.name)"
                            [title]="'SETTINGS.DELETE_MODEL' | translate">&times;</button>
                  </span>
                </div>
              }
            </div>
          } @else {
            <p class="hint">{{ 'SETTINGS.OLLAMA_NO_MODELS_INSTALLED' | translate }}</p>
          }

          <div class="ollama-install-row">
            <input type="text" [(ngModel)]="installModelName"
                   [placeholder]="'SETTINGS.OLLAMA_INSTALL_PLACEHOLDER' | translate"
                   class="pull-input"
                   (keydown.enter)="installModel()">
            <button class="btn btn-sm btn-primary" (click)="installModel()"
                    [disabled]="installingModel() || !installModelName.trim()">
              @if (installingModel()) {
                <span class="spinner-sm"></span>
              }
              {{ 'SETTINGS.OLLAMA_INSTALL_MODEL' | translate }}
            </button>
          </div>
        }

        @if (ollamaOnline() === false) {
          <div class="ollama-offline-content">
            <div class="ollama-alert">
              <strong>{{ 'SETTINGS.OLLAMA_UNREACHABLE' | translate }}</strong>
              <p>{{ 'SETTINGS.OLLAMA_UNREACHABLE_DESC' | translate }}</p>
            </div>
            <div class="ollama-actions">
              <button class="btn btn-primary" (click)="startOllama()" [disabled]="startingOllama()">
                @if (startingOllama()) {
                  <span class="spinner-sm"></span>
                  {{ 'SETTINGS.OLLAMA_STARTING' | translate }}
                } @else {
                  &#x25B6; {{ 'SETTINGS.OLLAMA_START' | translate }}
                }
              </button>
              <button class="btn btn-secondary" (click)="checkOllamaStatus()">
                {{ 'SETTINGS.OLLAMA_RETRY' | translate }}
              </button>
            </div>
            <details class="ollama-instructions">
              <summary>{{ 'SETTINGS.OLLAMA_MANUAL_INSTRUCTIONS' | translate }}</summary>
              <ul>
                <li>{{ 'SETTINGS.OLLAMA_CMD_LINUX' | translate }}</li>
                <li>{{ 'SETTINGS.OLLAMA_CMD_DOCKER' | translate }}</li>
              </ul>
            </details>
          </div>
        }
      </div>

      @if (showForm) {
        <div class="add-form card">
          <h3>{{ 'SETTINGS.FORM_TITLE' | translate }}</h3>
          <div class="form-grid">
            <div class="form-group">
              <label>{{ 'SETTINGS.FORM_NAME' | translate }}</label>
              <input type="text" [(ngModel)]="newProvider.name" name="providerName" #providerName="ngModel"
                     [placeholder]="'SETTINGS.FORM_NAME_PLACEHOLDER' | translate" required minlength="2">
              @if (providerName.invalid && providerName.touched) {
                <span class="form-error">
                  @if (providerName.errors?.['required']) { {{ 'VALIDATION.REQUIRED' | translate }} }
                  @else if (providerName.errors?.['minlength']) { {{ 'VALIDATION.MIN_LENGTH' | translate:{ min: 2 } }} }
                </span>
              }
            </div>
            <div class="form-group">
              <label>{{ 'SETTINGS.FORM_TYPE' | translate }}</label>
              <select [(ngModel)]="newProvider.type" (ngModelChange)="onTypeChange($event)">
                <option value="OLLAMA">Ollama</option>
                <option value="OPENAI">OpenAI</option>
                <option value="ANTHROPIC">Anthropic</option>
                <option value="GOOGLE">Google Gemini</option>
                <option value="DEEPSEEK">DeepSeek</option>
                <option value="MISTRAL">Mistral</option>
                <option value="XAI">xAI Grok</option>
              </select>
            </div>
            <div class="form-group">
              <label>{{ 'SETTINGS.FORM_URL' | translate }}</label>
              <input type="text" [(ngModel)]="newProvider.baseUrl" name="baseUrl" #baseUrl="ngModel"
                     (blur)="onBaseUrlBlur()"
                     [placeholder]="getUrlPlaceholder()" required appUrlValidator>
              <app-field-error [control]="baseUrl" />
            </div>
            @if (newProvider.type !== 'OLLAMA') {
              <div class="form-group">
                <label>{{ 'SETTINGS.FORM_APIKEY' | translate }}</label>
                <input type="password" [(ngModel)]="newProvider.apiKey" name="apiKey" #apiKey="ngModel"
                       [placeholder]="getApiKeyPlaceholder()" required minlength="10">
                @if (apiKey.invalid && apiKey.touched) {
                  <span class="form-error">
                    @if (apiKey.errors?.['required']) { {{ 'VALIDATION.REQUIRED' | translate }} }
                    @else if (apiKey.errors?.['minlength']) { {{ 'VALIDATION.MIN_LENGTH' | translate:{ min: 10 } }} }
                  </span>
                }
              </div>
            }
            <div class="form-group">
              <label>{{ 'SETTINGS.FORM_MODEL' | translate }}</label>
              @if (newProvider.type === 'OLLAMA') {
                <div class="select-with-refresh">
                  <select [(ngModel)]="newProvider.defaultModel" [disabled]="loadingModels()">
                    <option value="">{{ 'SETTINGS.FORM_MODEL_SELECT' | translate }}</option>
                    @for (model of ollamaModels(); track model) {
                      <option [value]="model">{{ model }}</option>
                    }
                  </select>
                  <button class="btn-icon" (click)="fetchOllamaModels()" [disabled]="loadingModels()"
                          [title]="'SETTINGS.FORM_MODEL_REFRESH' | translate">
                    @if (loadingModels()) {
                      <span class="spinner-sm"></span>
                    } @else {
                      &#x21bb;
                    }
                  </button>
                </div>
                @if (ollamaModels().length === 0 && !loadingModels() && ollamaModelsFetched()) {
                  <span class="hint error">{{ 'SETTINGS.FORM_OLLAMA_NO_MODELS' | translate }}</span>
                }
              } @else {
                <input type="text" [(ngModel)]="newProvider.defaultModel"
                       [placeholder]="getModelPlaceholder()">
              }
            </div>
          </div>
          <button class="btn btn-primary" (click)="addProvider()"
                  [disabled]="!isFormValid()">
            {{ 'SETTINGS.SAVE_PROVIDER' | translate }}
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
      } @else if (providers().length === 0) {
        <div class="empty-state card">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4"/>
          </svg>
          <h3>{{ 'SETTINGS.EMPTY_TITLE' | translate }}</h3>
          <p>{{ 'SETTINGS.EMPTY_DESC' | translate }}</p>
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
                  {{ provider.enabled ? ('SETTINGS.PROVIDER_ACTIVE' | translate) : ('SETTINGS.PROVIDER_DISABLED' | translate) }}
                </span>
              </div>
              <div class="provider-details">
                <div class="detail-row">
                  <span class="detail-label">{{ 'SETTINGS.PROVIDER_TYPE' | translate }}</span>
                  <span class="badge badge-primary">{{ provider.type }}</span>
                </div>
                @if (provider.defaultModel) {
                  <div class="detail-row">
                    <span class="detail-label">{{ 'SETTINGS.PROVIDER_DEFAULT_MODEL' | translate }}</span>
                    <span class="mono">{{ provider.defaultModel }}</span>
                  </div>
                }
                @if (provider.models.length > 0) {
                  <div class="detail-row models-row">
                    <span class="detail-label">{{ 'SETTINGS.PROVIDER_MODELS' | translate }}</span>
                    <span class="models-list">
                      @for (model of provider.models; track model) {
                        <span class="model-tag" [class.active-model]="model === provider.defaultModel">
                          {{ model }}
                          @if (model !== provider.defaultModel) {
                            <button class="model-action set-default" (click)="setDefaultModel(provider, model)"
                                    [title]="'SETTINGS.SET_DEFAULT' | translate">&#x2713;</button>
                          }
                          @if (provider.type === 'OLLAMA') {
                            <button class="model-action delete-model" (click)="deleteOllamaModel(provider, model)"
                                    [title]="'SETTINGS.DELETE_MODEL' | translate">&times;</button>
                          }
                        </span>
                      }
                    </span>
                  </div>
                }
              </div>
              @if (provider.type === 'OLLAMA') {
                <div class="pull-model-row">
                  <input type="text" [(ngModel)]="pullModelName"
                         [placeholder]="'SETTINGS.PULL_MODEL_PLACEHOLDER' | translate"
                         class="pull-input">
                  <button class="btn btn-sm btn-secondary" (click)="pullOllamaModel(provider)"
                          [disabled]="pullingModel() || !pullModelName.trim()">
                    @if (pullingModel()) {
                      <span class="spinner-sm"></span>
                    }
                    {{ 'SETTINGS.PULL_MODEL' | translate }}
                  </button>
                </div>
              }
              <div class="provider-actions">
                <button class="btn btn-sm btn-secondary" (click)="testProvider(provider)"
                        [disabled]="testingId() === provider.id">
                  @if (testingId() === provider.id) {
                    <span class="spinner-sm"></span>
                  }
                  {{ 'SETTINGS.TEST_CONNECTION' | translate }}
                </button>
                <button class="btn btn-sm btn-danger" (click)="deleteProvider(provider.id)">
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
    .settings-page { max-width: 900px; }
    .page-header { display: flex; justify-content: space-between; align-items: start; margin-bottom: 1.5rem; }
    .page-header h1 { margin: 0; font-size: 1.5rem; }
    .subtitle { color: var(--color-text-muted); font-size: 0.85rem; margin: 0.15rem 0 0 0; }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-light); }
    .btn-secondary { background: var(--color-card-bg); color: var(--color-text); border: 1px solid var(--color-border); }
    .btn-secondary:hover:not(:disabled) { background: var(--color-bg); }
    .btn-danger { background: transparent; color: var(--color-error); border: 1px solid var(--color-error); }
    .btn-danger:hover:not(:disabled) { background: var(--color-error-bg); }
    .btn-sm { padding: 0.35rem 0.7rem; font-size: 0.8rem; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-icon { background: none; border: 1px solid var(--color-border); border-radius: 4px; padding: 0.4rem 0.6rem; cursor: pointer; font-size: 1rem; line-height: 1; display: flex; align-items: center; }
    .btn-icon:hover:not(:disabled) { background: var(--color-bg); }
    .btn-icon:disabled { opacity: 0.5; cursor: not-allowed; }
    .card { background: var(--color-card-bg); border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); padding: 1.25rem; }
    .add-form { margin-bottom: 1.5rem; }
    .add-form h3 { margin: 0 0 1rem 0; font-size: 1rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8rem; font-weight: 500; color: var(--color-text); }
    .form-group input, .form-group select {
      padding: 0.5rem; border: 1px solid var(--color-border); border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; width: 100%; box-sizing: border-box;
      background: var(--color-input-bg); color: var(--color-text);
    }
    .form-error { font-size: 0.75rem; color: var(--color-error); margin-top: 0.2rem; display: block; }
    .select-with-refresh { display: flex; gap: 0.35rem; }
    .select-with-refresh select { flex: 1; }
    .hint { font-size: 0.7rem; margin-top: 0.15rem; }
    .hint.error { color: var(--color-error); }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: var(--color-success-bg); color: var(--color-success-text); }
    .notification.error { background: var(--color-error-bg); color: var(--color-error-text); }

    .loading-state { display: flex; align-items: center; gap: 0.5rem; justify-content: center; padding: 3rem; color: var(--color-text-muted); }
    .spinner { display: inline-block; width: 16px; height: 16px; border: 2px solid var(--color-border); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.6s linear infinite; }
    .spinner-sm { display: inline-block; width: 12px; height: 12px; border: 2px solid var(--color-border); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }

    .empty-state { text-align: center; padding: 3rem; }
    .empty-state svg { margin-bottom: 1rem; }
    .empty-state h3 { color: var(--color-text-secondary); margin-bottom: 0.25rem; }
    .empty-state p { font-size: 0.85rem; color: var(--color-text-muted); }

    .provider-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .provider-card {
      background: var(--color-card-bg); padding: 1.25rem; border-radius: 10px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06); border-left: 4px solid var(--color-success);
    }
    .provider-card.disabled { border-left-color: var(--color-border); opacity: 0.7; }
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
    .provider-type-icon.deepseek { background: #0066ff; }
    .provider-type-icon.mistral { background: #ff7000; }
    .provider-type-icon.xai { background: #1d1d1f; }
    .provider-name { font-weight: 600; font-size: 0.95rem; }
    .provider-url { font-size: 0.75rem; color: var(--color-text-muted); font-family: monospace; }
    .badge { font-size: 0.65rem; padding: 0.1rem 0.45rem; border-radius: 10px; font-weight: 600; text-transform: uppercase; }
    .badge-success { background: var(--color-success-bg); color: var(--color-success-text); }
    .badge-neutral { background: var(--color-border); color: var(--color-text-secondary); }
    .badge-primary { background: var(--color-info-bg); color: var(--color-primary); }
    .provider-details { margin-bottom: 0.75rem; }
    .detail-row { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.35rem; font-size: 0.8rem; }
    .detail-label { color: var(--color-text-muted); min-width: 100px; }
    .mono { font-family: monospace; font-size: 0.8rem; }
    .models-list { display: flex; flex-wrap: wrap; gap: 0.25rem; }
    .model-tag { background: var(--color-skeleton-base); padding: 0.1rem 0.4rem; border-radius: 4px; font-size: 0.7rem; font-family: monospace; }
    .provider-actions { display: flex; gap: 0.5rem; }
    .models-row { align-items: start !important; }
    .model-tag { display: inline-flex; align-items: center; gap: 0.2rem; position: relative; }
    .model-tag.active-model { background: var(--color-success-bg); border: 1px solid var(--color-success); }
    .model-action { background: none; border: none; cursor: pointer; font-size: 0.75rem; padding: 0 0.15rem; line-height: 1; border-radius: 2px; }
    .model-action.set-default { color: var(--color-success); }
    .model-action.set-default:hover { background: var(--color-success-bg); }
    .model-action.delete-model { color: var(--color-error); }
    .model-action.delete-model:hover { background: var(--color-error-bg); }
    .pull-model-row { display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.75rem; }
    .pull-input { padding: 0.35rem 0.5rem; border: 1px solid var(--color-border); border-radius: 4px; font-size: 0.8rem; font-family: monospace; flex: 1; max-width: 300px; }

    .ollama-status-card {
      background: var(--color-card-bg); padding: 1.25rem; border-radius: 10px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08); margin-bottom: 1.5rem;
      border-left: 4px solid var(--color-border);
    }
    .ollama-status-card.online { border-left-color: var(--color-success); }
    .ollama-status-card.offline { border-left-color: var(--color-error); }
    .ollama-status-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
    .ollama-title-row { display: flex; align-items: center; gap: 0.75rem; }
    .ollama-title-row h3 { margin: 0; font-size: 1rem; }
    .badge-danger { background: var(--color-error-bg); color: var(--color-error-text); }
    .ollama-info { display: flex; gap: 1.5rem; font-size: 0.85rem; color: var(--color-text-secondary); margin-bottom: 0.75rem; }
    .ollama-version, .ollama-count { display: flex; align-items: center; gap: 0.25rem; }
    .ollama-models-table { margin-bottom: 0.75rem; font-size: 0.8rem; }
    .table-header {
      display: grid; grid-template-columns: 2fr 1fr 1fr 40px; gap: 0.5rem;
      padding: 0.4rem 0.5rem; font-weight: 600; color: var(--color-text-muted); border-bottom: 1px solid var(--color-border-light);
    }
    .table-row {
      display: grid; grid-template-columns: 2fr 1fr 1fr 40px; gap: 0.5rem;
      padding: 0.4rem 0.5rem; border-bottom: 1px solid var(--color-bg); align-items: center;
    }
    .table-row:hover { background: var(--color-hover-bg); }
    .ollama-install-row { display: flex; gap: 0.5rem; align-items: center; }
    .ollama-offline-content { display: flex; flex-direction: column; gap: 0.75rem; }
    .ollama-alert { background: var(--color-warning-bg); border-radius: 6px; padding: 0.75rem 1rem; font-size: 0.85rem; }
    .ollama-alert strong { display: block; margin-bottom: 0.25rem; }
    .ollama-alert p { margin: 0; color: var(--color-warning-text); }
    .ollama-actions { display: flex; gap: 0.5rem; }
    .ollama-instructions { font-size: 0.8rem; color: var(--color-text-secondary); }
    .ollama-instructions summary { cursor: pointer; font-weight: 500; margin-bottom: 0.5rem; }
    .ollama-instructions ul { margin: 0; padding-left: 1.25rem; }
    .ollama-instructions li { margin-bottom: 0.35rem; font-family: monospace; font-size: 0.75rem; }

  `]
})
export class SettingsPageComponent implements OnInit {
  private settingsService = inject(SettingsService);
  private i18n = inject(TranslationService);
  private downloadService = inject(OllamaDownloadService);

  providers = signal<LlmProviderConfig[]>([]);
  loading = signal(false);
  showForm = false;
  testingId = signal<string | null>(null);
  notification = signal<{ type: string; message: string } | null>(null);
  ollamaModels = signal<string[]>([]);
  loadingModels = signal(false);
  ollamaModelsFetched = signal(false);
  pullingModel = computed(() => this.downloadService.isDownloading());
  pullModelName = '';
  newProvider: CreateProviderRequest = { name: '', type: 'OLLAMA', baseUrl: 'http://localhost:11434', defaultModel: '' };

  // Ollama Status
  ollamaOnline = signal<boolean | null>(null);
  ollamaVersion = signal('');
  ollamaInstalledModels = signal<OllamaModelDetail[]>([]);
  ollamaCheckLoading = signal(false);
  startingOllama = signal(false);
  installingModel = computed(() => this.downloadService.isDownloading());
  installModelName = '';
  private ollamaBaseUrl = 'http://localhost:11434';

  constructor() {
    effect(() => {
      const progress = this.downloadService.downloadProgress();
      if (progress?.done && !progress.error) {
        this.showNotify('success', this.i18n.instant('SETTINGS.PULL_SUCCESS', { model: this.downloadService.downloadModelName() }));
        this.loadProviders();
        this.checkOllamaStatus();
      } else if (progress?.error) {
        this.showNotify('error', this.i18n.instant('SETTINGS.PULL_ERROR', { model: this.downloadService.downloadModelName() }));
      }
    });
  }

  ngOnInit() {
    this.loadProviders();
    this.checkOllamaStatus();
  }

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
      case 'DEEPSEEK':
        this.newProvider.baseUrl = 'https://api.deepseek.com';
        break;
      case 'MISTRAL':
        this.newProvider.baseUrl = 'https://api.mistral.ai';
        break;
      case 'XAI':
        this.newProvider.baseUrl = 'https://api.x.ai/v1';
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
      case 'DEEPSEEK': return 'https://api.deepseek.com';
      case 'MISTRAL': return 'https://api.mistral.ai';
      case 'XAI': return 'https://api.x.ai/v1';
      default: return '';
    }
  }

  getApiKeyPlaceholder(): string {
    switch (this.newProvider.type) {
      case 'OPENAI': return 'sk-...';
      case 'ANTHROPIC': return 'sk-ant-...';
      case 'GOOGLE': return 'AIza...';
      case 'DEEPSEEK': return 'sk-...';
      case 'MISTRAL': return 'api-key-...';
      case 'XAI': return 'xai-...';
      default: return '';
    }
  }

  getModelPlaceholder(): string {
    switch (this.newProvider.type) {
      case 'OPENAI': return 'gpt-4o-mini';
      case 'ANTHROPIC': return 'claude-sonnet-4-20250514';
      case 'GOOGLE': return 'gemini-2.0-flash';
      case 'DEEPSEEK': return 'deepseek-chat';
      case 'MISTRAL': return 'mistral-large-latest';
      case 'XAI': return 'grok-2';
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
        this.showNotify('success', this.i18n.instant('SETTINGS.ADD_SUCCESS'));
        this.loadProviders();
      },
      error: () => this.showNotify('error', this.i18n.instant('SETTINGS.ADD_ERROR'))
    });
  }

  testProvider(provider: LlmProviderConfig) {
    this.testingId.set(provider.id);
    this.settingsService.testProvider(provider.id).subscribe({
      next: (res) => {
        this.testingId.set(null);
        this.showNotify(res.status === 'OK' ? 'success' : 'error',
          res.status === 'OK' ? this.i18n.instant('SETTINGS.TEST_SUCCESS', { name: provider.name }) : `${provider.name}: ${res.message}`);
      },
      error: () => {
        this.testingId.set(null);
        this.showNotify('error', this.i18n.instant('SETTINGS.TEST_FAILURE', { name: provider.name }));
      }
    });
  }

  deleteProvider(id: string) {
    this.settingsService.deleteProvider(id).subscribe({
      next: () => { this.showNotify('success', this.i18n.instant('SETTINGS.DELETE_SUCCESS')); this.loadProviders(); },
      error: () => this.showNotify('error', this.i18n.instant('SETTINGS.DELETE_ERROR'))
    });
  }

  pullOllamaModel(provider: LlmProviderConfig) {
    const name = this.pullModelName.trim();
    if (!name || !provider.baseUrl) return;
    this.downloadService.startPull(provider.baseUrl, name);
    this.pullModelName = '';
  }

  deleteOllamaModel(provider: LlmProviderConfig, modelName: string) {
    if (!provider.baseUrl) return;
    this.settingsService.deleteOllamaModel(provider.baseUrl, modelName).subscribe({
      next: () => {
        this.showNotify('success', this.i18n.instant('SETTINGS.MODEL_DELETE_SUCCESS', { model: modelName }));
        this.loadProviders();
      },
      error: () => this.showNotify('error', this.i18n.instant('SETTINGS.MODEL_DELETE_ERROR', { model: modelName }))
    });
  }

  setDefaultModel(provider: LlmProviderConfig, modelName: string) {
    this.settingsService.updateDefaultModel(provider.id, modelName).subscribe({
      next: () => {
        this.showNotify('success', this.i18n.instant('SETTINGS.DEFAULT_MODEL_SUCCESS', { model: modelName }));
        this.loadProviders();
      },
      error: () => this.showNotify('error', this.i18n.instant('SETTINGS.DEFAULT_MODEL_ERROR'))
    });
  }

  checkOllamaStatus() {
    this.ollamaCheckLoading.set(true);
    // Usa la baseUrl dal primo provider Ollama configurato, altrimenti default
    const ollamaProvider = this.providers().find(p => p.type === 'OLLAMA' && p.enabled);
    const baseUrl = ollamaProvider?.baseUrl || this.ollamaBaseUrl;
    this.settingsService.getOllamaStatus(baseUrl).subscribe({
      next: (status) => {
        this.ollamaOnline.set(status.online);
        this.ollamaVersion.set(status.version || '');
        this.ollamaInstalledModels.set(status.models || []);
        this.ollamaCheckLoading.set(false);
      },
      error: () => {
        this.ollamaOnline.set(false);
        this.ollamaCheckLoading.set(false);
      }
    });
  }

  startOllama() {
    this.startingOllama.set(true);
    this.settingsService.startOllama().subscribe({
      next: () => {
        this.showNotify('success', this.i18n.instant('SETTINGS.OLLAMA_START_SUCCESS'));
        // Attendi 3 secondi e ricontrolla lo stato
        setTimeout(() => {
          this.startingOllama.set(false);
          this.checkOllamaStatus();
        }, 3000);
      },
      error: () => {
        this.startingOllama.set(false);
        this.showNotify('error', this.i18n.instant('SETTINGS.OLLAMA_START_ERROR'));
      }
    });
  }

  installModel() {
    const name = this.installModelName.trim();
    if (!name) return;
    const ollamaProvider = this.providers().find(p => p.type === 'OLLAMA' && p.enabled);
    const baseUrl = ollamaProvider?.baseUrl || this.ollamaBaseUrl;
    this.downloadService.startPull(baseUrl, name);
    this.installModelName = '';
  }

  deleteInstalledModel(modelName: string) {
    const ollamaProvider = this.providers().find(p => p.type === 'OLLAMA' && p.enabled);
    const baseUrl = ollamaProvider?.baseUrl || this.ollamaBaseUrl;
    this.settingsService.deleteOllamaModel(baseUrl, modelName).subscribe({
      next: () => {
        this.showNotify('success', this.i18n.instant('SETTINGS.MODEL_DELETE_SUCCESS', { model: modelName }));
        this.checkOllamaStatus();
        this.loadProviders();
      },
      error: () => this.showNotify('error', this.i18n.instant('SETTINGS.MODEL_DELETE_ERROR', { model: modelName }))
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
