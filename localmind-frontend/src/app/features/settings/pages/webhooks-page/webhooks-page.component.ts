import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { WebhookService } from '../../services/webhook.service';
import { Webhook, WebhookRequest } from '../../models/webhook.model';
import { UrlValidatorDirective } from '../../../../shared/validators/url-validator.directive';
import { FieldErrorComponent } from '../../../../shared/components/field-error/field-error.component';

@Component({
  selector: 'app-webhooks-page',
  standalone: true,
  imports: [FormsModule, TranslatePipe, UrlValidatorDirective, FieldErrorComponent],
  template: `
    <div class="webhooks-page">
      <div class="header">
        <h2>{{ 'WEBHOOKS.TITLE' | translate }}</h2>
        <button class="btn btn-primary" (click)="openForm()">
          {{ 'WEBHOOKS.ADD' | translate }}
        </button>
      </div>

      @if (showForm()) {
        <div class="add-form">
          <h3>{{ editingId() ? ('WEBHOOKS.EDIT' | translate) : ('WEBHOOKS.ADD' | translate) }}</h3>
          <div class="form-grid">
            <div class="form-group">
              <label>{{ 'WEBHOOKS.NAME' | translate }} *</label>
              <input type="text" [(ngModel)]="formName" name="webhookName" #webhookName="ngModel"
                     placeholder="Es: Notifica Slack" required minlength="2">
              @if (webhookName.invalid && webhookName.touched) {
                <span class="form-error">
                  @if (webhookName.errors?.['required']) { {{ 'VALIDATION.REQUIRED' | translate }} }
                  @else if (webhookName.errors?.['minlength']) { {{ 'VALIDATION.MIN_LENGTH' | translate:{ min: 2 } }} }
                </span>
              }
            </div>
            <div class="form-group">
              <label>{{ 'WEBHOOKS.URL' | translate }} *</label>
              <input type="url" [(ngModel)]="formUrl" name="webhookUrl" #webhookUrl="ngModel"
                     placeholder="https://example.com/webhook" required minlength="10" appUrlValidator>
              <app-field-error [control]="webhookUrl" />
            </div>
            <div class="form-group">
              <label>{{ 'WEBHOOKS.EVENT_TYPE' | translate }} *</label>
              <select [(ngModel)]="formEventType" name="webhookEventType">
                @for (type of eventTypes; track type) {
                  <option [value]="type">{{ 'WEBHOOKS.EVENT.' + type | translate }}</option>
                }
              </select>
            </div>
            <div class="form-group checkbox-group">
              <label>
                <input type="checkbox" [(ngModel)]="formActive" name="webhookActive">
                {{ 'WEBHOOKS.ACTIVE' | translate }}
              </label>
            </div>
          </div>
          <div class="form-actions">
            <button class="btn btn-primary" (click)="saveWebhook()"
                    [disabled]="!isFormValid() || saving()">
              @if (saving()) { <span class="spinner-sm"></span> }
              {{ 'WEBHOOKS.SAVE' | translate }}
            </button>
            <button class="btn btn-secondary" (click)="closeForm()">
              {{ 'WEBHOOKS.CANCEL' | translate }}
            </button>
          </div>
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
          {{ 'COMMON.LOADING' | translate }}
        </div>
      } @else if (webhooks().length === 0) {
        <div class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <path d="M10 13a5 5 0 007.54.54l3-3a5 5 0 00-7.07-7.07l-1.72 1.71"/>
            <path d="M14 11a5 5 0 00-7.54-.54l-3 3a5 5 0 007.07 7.07l1.71-1.71"/>
          </svg>
          <h3>{{ 'WEBHOOKS.NO_WEBHOOKS' | translate }}</h3>
          <p>{{ 'WEBHOOKS.NO_WEBHOOKS_DESC' | translate }}</p>
        </div>
      } @else {
        <div class="webhook-list">
          @for (webhook of webhooks(); track webhook.id) {
            <div class="webhook-card" [class.inactive]="!webhook.active">
              <div class="webhook-info">
                <div class="webhook-header">
                  <h3>{{ webhook.name }}</h3>
                  <span class="status-badge" [class.active]="webhook.active" [class.inactive]="!webhook.active">
                    {{ webhook.active ? ('WEBHOOKS.ACTIVE' | translate) : ('WEBHOOKS.INACTIVE' | translate) }}
                  </span>
                </div>
                <div class="webhook-url">{{ webhook.url }}</div>
                <div class="meta">
                  <span class="event-badge">{{ 'WEBHOOKS.EVENT.' + webhook.eventType | translate }}</span>
                </div>
              </div>
              <div class="webhook-actions">
                <button class="btn-sm" (click)="toggleActive(webhook)"
                        [disabled]="actionId() === webhook.id"
                        [title]="webhook.active ? ('WEBHOOKS.INACTIVE' | translate) : ('WEBHOOKS.ACTIVE' | translate)">
                  @if (actionId() === webhook.id && actionType() === 'toggle') { <span class="spinner-xs"></span> }
                  {{ webhook.active ? ('WEBHOOKS.INACTIVE' | translate) : ('WEBHOOKS.ACTIVE' | translate) }}
                </button>
                <button class="btn-sm" (click)="openForm(webhook)"
                        [disabled]="actionId() === webhook.id">
                  {{ 'WEBHOOKS.EDIT' | translate }}
                </button>
                <button class="btn-sm btn-test" (click)="testWebhook(webhook.id)"
                        [disabled]="actionId() === webhook.id || !webhook.active">
                  @if (actionId() === webhook.id && actionType() === 'test') { <span class="spinner-xs"></span> }
                  {{ 'WEBHOOKS.TEST' | translate }}
                </button>
                <button class="btn-sm btn-danger" (click)="deleteWebhook(webhook.id)"
                        [disabled]="actionId() === webhook.id">
                  {{ 'COMMON.DELETE' | translate }}
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

    .btn {
      display: inline-flex; align-items: center; gap: 0.4rem;
      padding: 0.5rem 1rem; border: none; border-radius: 6px;
      font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500;
    }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-light); }
    .btn-secondary {
      background: var(--color-card-bg); color: var(--color-text);
      border: 1px solid var(--color-border);
    }
    .btn-secondary:hover:not(:disabled) { background: var(--color-hover-bg); }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }

    .add-form {
      background: var(--color-card-bg); padding: 1.5rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 1.5rem;
    }
    .add-form h3 { margin: 0 0 1rem 0; color: var(--color-text); }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8rem; font-weight: 500; color: var(--color-text); }
    .form-group input, .form-group select {
      padding: 0.5rem; border: 1px solid var(--color-border); border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; width: 100%; box-sizing: border-box;
      background: var(--color-input-bg); color: var(--color-text);
    }
    .form-error { font-size: 0.75rem; color: var(--color-error); margin-top: 0.15rem; }
    .checkbox-group { display: flex; align-items: end; }
    .checkbox-group label {
      display: flex; align-items: center; gap: 0.5rem;
      cursor: pointer; font-size: 0.85rem; color: var(--color-text);
    }
    .form-actions { display: flex; gap: 0.75rem; }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: var(--color-success-bg); color: var(--color-success-text); }
    .notification.error { background: var(--color-error-bg); color: var(--color-error-text); }

    .loading {
      text-align: center; padding: 3rem; color: var(--color-text-secondary);
      background: var(--color-card-bg); border-radius: 8px;
      display: flex; align-items: center; gap: 0.5rem; justify-content: center;
    }
    .empty-state {
      text-align: center; padding: 3rem; color: var(--color-text-secondary);
      background: var(--color-card-bg); border-radius: 8px;
    }
    .empty-state svg { margin-bottom: 1rem; }
    .empty-state h3 { margin-bottom: 0.25rem; color: var(--color-text); }
    .empty-state p { font-size: 0.85rem; color: var(--color-text-muted); }

    .webhook-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .webhook-card {
      background: var(--color-card-bg); padding: 1rem 1.25rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); display: flex;
      justify-content: space-between; align-items: center;
      border-left: 4px solid var(--color-success);
    }
    .webhook-card.inactive { border-left-color: var(--color-text-muted); }

    .webhook-header { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.25rem; }
    .webhook-header h3 { margin: 0; font-size: 1rem; color: var(--color-text); }

    .status-badge {
      font-size: 0.65rem; padding: 0.15rem 0.5rem; border-radius: 10px;
      text-transform: uppercase; font-weight: 600;
    }
    .status-badge.active { background: var(--color-success-bg); color: var(--color-success-text); }
    .status-badge.inactive { background: var(--color-border); color: var(--color-text-secondary); }

    .webhook-url {
      font-family: monospace; font-size: 0.8rem; color: var(--color-text-muted);
      margin-bottom: 0.35rem; overflow: hidden; text-overflow: ellipsis;
      white-space: nowrap; max-width: 400px;
    }
    .meta { display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; color: var(--color-text-muted); }
    .event-badge {
      background: var(--color-info-bg); color: var(--color-info-text);
      padding: 0.1rem 0.4rem; border-radius: 4px; font-size: 0.7rem; font-weight: 600;
    }

    .webhook-actions { display: flex; gap: 0.5rem; flex-shrink: 0; }
    .btn-sm {
      padding: 0.35rem 0.75rem; border: 1px solid var(--color-border);
      background: var(--color-card-bg); border-radius: 4px; cursor: pointer;
      font-size: 0.8rem; font-family: inherit; color: var(--color-text);
      display: inline-flex; align-items: center; gap: 0.3rem;
    }
    .btn-sm:hover:not(:disabled) { background: var(--color-hover-bg); }
    .btn-sm:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-test { color: var(--color-primary); border-color: var(--color-primary); }
    .btn-test:hover:not(:disabled) { background: var(--color-info-bg); }
    .btn-danger { color: var(--color-error); border-color: var(--color-error); }
    .btn-danger:hover:not(:disabled) { background: var(--color-error-bg); }

    .spinner {
      display: inline-block; width: 16px; height: 16px;
      border: 2px solid var(--color-border); border-top-color: var(--color-primary);
      border-radius: 50%; animation: spin 0.6s linear infinite;
    }
    .spinner-sm {
      display: inline-block; width: 12px; height: 12px;
      border: 2px solid rgba(255,255,255,0.3); border-top-color: white;
      border-radius: 50%; animation: spin 0.6s linear infinite;
    }
    .spinner-xs {
      display: inline-block; width: 10px; height: 10px;
      border: 2px solid var(--color-border); border-top-color: var(--color-primary);
      border-radius: 50%; animation: spin 0.6s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    @media (max-width: 768px) {
      .form-grid { grid-template-columns: 1fr; }
      .webhook-card { flex-direction: column; align-items: flex-start; gap: 0.75rem; }
      .webhook-actions { width: 100%; flex-wrap: wrap; }
      .webhook-url { max-width: 100%; }
    }
  `]
})
export class WebhooksPageComponent implements OnInit {
  private webhookService = inject(WebhookService);
  private i18n = inject(TranslationService);

  webhooks = signal<Webhook[]>([]);
  loading = signal(false);
  saving = signal(false);
  showForm = signal(false);
  editingId = signal<string | null>(null);
  actionId = signal<string | null>(null);
  actionType = signal<string | null>(null);
  notification = signal<{ type: string; message: string } | null>(null);

  formName = '';
  formUrl = '';
  formEventType = 'DOCUMENT_INDEXED';
  formActive = true;

  eventTypes = ['NEW_FILE', 'DOCUMENT_INDEXED', 'DOCUMENT_FAILED', 'CHAT_COMPLETED', 'SCHEDULED'];

  ngOnInit(): void {
    this.loadWebhooks();
  }

  loadWebhooks(): void {
    this.loading.set(true);
    this.webhookService.listAll().subscribe({
      next: (webhooks) => { this.webhooks.set(webhooks); this.loading.set(false); },
      error: () => { this.loading.set(false); }
    });
  }

  openForm(webhook?: Webhook): void {
    if (webhook) {
      this.editingId.set(webhook.id);
      this.formName = webhook.name;
      this.formUrl = webhook.url;
      this.formEventType = webhook.eventType;
      this.formActive = webhook.active;
    } else {
      this.editingId.set(null);
      this.resetForm();
    }
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
    this.resetForm();
  }

  isFormValid(): boolean {
    return !!(this.formName && this.formName.length >= 2 && this.formUrl && this.formUrl.length >= 10 && this.formEventType);
  }

  saveWebhook(): void {
    if (!this.isFormValid()) return;

    const request: WebhookRequest = {
      name: this.formName,
      url: this.formUrl,
      eventType: this.formEventType,
      active: this.formActive
    };

    this.saving.set(true);
    const id = this.editingId();

    if (id) {
      this.webhookService.update(id, request).subscribe({
        next: () => {
          this.saving.set(false);
          this.closeForm();
          this.showNotify('success', this.i18n.instant('COMMON.SUCCESS'));
          this.loadWebhooks();
        },
        error: () => {
          this.saving.set(false);
          this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
        }
      });
    } else {
      this.webhookService.create(request).subscribe({
        next: () => {
          this.saving.set(false);
          this.closeForm();
          this.showNotify('success', this.i18n.instant('COMMON.SUCCESS'));
          this.loadWebhooks();
        },
        error: () => {
          this.saving.set(false);
          this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
        }
      });
    }
  }

  deleteWebhook(id: string): void {
    if (!confirm(this.i18n.instant('WEBHOOKS.CONFIRM_DELETE'))) return;

    this.actionId.set(id);
    this.actionType.set('delete');
    this.webhookService.delete(id).subscribe({
      next: () => {
        this.actionId.set(null);
        this.showNotify('success', this.i18n.instant('COMMON.SUCCESS'));
        this.loadWebhooks();
      },
      error: () => {
        this.actionId.set(null);
        this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
      }
    });
  }

  testWebhook(id: string): void {
    this.actionId.set(id);
    this.actionType.set('test');
    this.webhookService.test(id).subscribe({
      next: () => {
        this.actionId.set(null);
        this.showNotify('success', this.i18n.instant('WEBHOOKS.TEST_SUCCESS'));
      },
      error: () => {
        this.actionId.set(null);
        this.showNotify('error', this.i18n.instant('WEBHOOKS.TEST_FAILED'));
      }
    });
  }

  toggleActive(webhook: Webhook): void {
    this.actionId.set(webhook.id);
    this.actionType.set('toggle');

    const request: WebhookRequest = {
      name: webhook.name,
      url: webhook.url,
      eventType: webhook.eventType,
      active: !webhook.active
    };

    this.webhookService.update(webhook.id, request).subscribe({
      next: () => {
        this.actionId.set(null);
        this.loadWebhooks();
      },
      error: () => {
        this.actionId.set(null);
        this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
      }
    });
  }

  private resetForm(): void {
    this.formName = '';
    this.formUrl = '';
    this.formEventType = 'DOCUMENT_INDEXED';
    this.formActive = true;
  }

  private showNotify(type: string, message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 4000);
  }
}
