import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { ChannelsService } from '../../services/channels.service';
import {
  CreateChannelRequest,
  MessagingChannel,
  MessagingPlatform,
} from '../../models/channel.model';

@Component({
  selector: 'app-channels-page',
  standalone: true,
  imports: [FormsModule, TranslatePipe],
  template: `
    <div class="channels-page">
      <div class="header">
        <h2>{{ 'CHANNELS.TITLE' | translate }}</h2>
        <button class="btn btn-primary" (click)="openForm()">
          {{ 'CHANNELS.ADD' | translate }}
        </button>
      </div>

      @if (showForm()) {
        <div class="add-form">
          <h3>{{ editingId() ? ('CHANNELS.EDIT' | translate) : ('CHANNELS.ADD' | translate) }}</h3>
          <div class="form-grid">
            <div class="form-group">
              <label>{{ 'CHANNELS.NAME' | translate }} *</label>
              <input type="text" [(ngModel)]="formName" name="channelName"
                     placeholder="Es: Slack #general" minlength="2" required>
            </div>
            <div class="form-group">
              <label>{{ 'CHANNELS.PLATFORM' | translate }} *</label>
              <select [(ngModel)]="formPlatform" name="channelPlatform">
                @for (p of platforms; track p) {
                  <option [value]="p">{{ 'CHANNELS.PLATFORM.' + p | translate }}</option>
                }
              </select>
            </div>
            <div class="form-group full">
              <label>{{ 'CHANNELS.BOT_TOKEN' | translate }} *</label>
              <input type="password" [(ngModel)]="formBotToken" name="channelToken"
                     placeholder="xoxb-... / bot... / ..." required>
            </div>
            <div class="form-group">
              <label>{{ 'CHANNELS.WEBHOOK_SECRET' | translate }}</label>
              <input type="password" [(ngModel)]="formWebhookSecret" name="channelSecret">
            </div>
            <div class="form-group">
              <label>{{ 'CHANNELS.PROVIDER' | translate }}</label>
              <input type="text" [(ngModel)]="formProvider" name="channelProvider"
                     placeholder="OLLAMA">
            </div>
            <div class="form-group">
              <label>{{ 'CHANNELS.MODEL' | translate }}</label>
              <input type="text" [(ngModel)]="formModel" name="channelModel"
                     placeholder="llama3.2">
            </div>
            <div class="form-group full">
              <label>{{ 'CHANNELS.SYSTEM_PROMPT' | translate }}</label>
              <textarea [(ngModel)]="formSystemPrompt" name="channelSystemPrompt"
                        rows="3" placeholder="Sei un assistente utile..."></textarea>
            </div>
            <div class="form-group checkbox-group">
              <label>
                <input type="checkbox" [(ngModel)]="formEnableRag" name="channelRag">
                {{ 'CHANNELS.ENABLE_RAG' | translate }}
              </label>
            </div>
            <div class="form-group checkbox-group">
              <label>
                <input type="checkbox" [(ngModel)]="formEnableToolCalling" name="channelTools">
                {{ 'CHANNELS.ENABLE_TOOLS' | translate }}
              </label>
            </div>
          </div>
          <div class="form-actions">
            <button class="btn btn-primary" (click)="saveChannel()"
                    [disabled]="!isFormValid() || saving()">
              @if (saving()) { <span class="spinner-sm"></span> }
              {{ 'CHANNELS.SAVE' | translate }}
            </button>
            <button class="btn btn-secondary" (click)="closeForm()">
              {{ 'CHANNELS.CANCEL' | translate }}
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
      } @else if (channels().length === 0) {
        <div class="empty-state">
          <h3>{{ 'CHANNELS.NO_CHANNELS' | translate }}</h3>
          <p>{{ 'CHANNELS.NO_CHANNELS_DESC' | translate }}</p>
        </div>
      } @else {
        <div class="channel-list">
          @for (channel of channels(); track channel.id) {
            <div class="channel-card" [class.inactive]="!channel.active">
              <div class="channel-info">
                <div class="channel-header">
                  <h3>{{ channel.name }}</h3>
                  <span class="platform-badge">{{ channel.platform }}</span>
                  <span class="status-badge" [class.active]="channel.active" [class.inactive]="!channel.active">
                    {{ channel.active ? ('CHANNELS.ACTIVE' | translate) : ('CHANNELS.INACTIVE' | translate) }}
                  </span>
                </div>
                <div class="meta">
                  <span>{{ channel.defaultProvider || '—' }} · {{ channel.defaultModel || '—' }}</span>
                  @if (channel.enableRag) { <span class="flag">RAG</span> }
                  @if (channel.enableToolCalling) { <span class="flag">Tools</span> }
                </div>
              </div>
              <div class="channel-actions">
                <button class="btn-sm" (click)="openForm(channel)"
                        [disabled]="actionId() === channel.id">
                  {{ 'CHANNELS.EDIT' | translate }}
                </button>
                <button class="btn-sm btn-test" (click)="testChannel(channel.id)"
                        [disabled]="actionId() === channel.id || !channel.active">
                  @if (actionId() === channel.id && actionType() === 'test') { <span class="spinner-xs"></span> }
                  {{ 'CHANNELS.TEST' | translate }}
                </button>
                <button class="btn-sm btn-danger" (click)="deleteChannel(channel.id)"
                        [disabled]="actionId() === channel.id">
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
    .form-group.full { grid-column: span 2; }
    .form-group label { font-size: 0.8rem; font-weight: 500; color: var(--color-text); }
    .form-group input, .form-group select, .form-group textarea {
      padding: 0.5rem; border: 1px solid var(--color-border); border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; width: 100%; box-sizing: border-box;
      background: var(--color-input-bg); color: var(--color-text);
    }
    .checkbox-group { display: flex; align-items: end; }
    .checkbox-group label {
      display: flex; align-items: center; gap: 0.5rem;
      cursor: pointer; font-size: 0.85rem; color: var(--color-text);
    }
    .form-actions { display: flex; gap: 0.75rem; }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: var(--color-success-bg); color: var(--color-success-text); }
    .notification.error { background: var(--color-error-bg); color: var(--color-error-text); }

    .loading, .empty-state {
      text-align: center; padding: 3rem; color: var(--color-text-secondary);
      background: var(--color-card-bg); border-radius: 8px;
    }
    .empty-state h3 { margin-bottom: 0.25rem; color: var(--color-text); }
    .empty-state p { font-size: 0.85rem; color: var(--color-text-muted); }

    .channel-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .channel-card {
      background: var(--color-card-bg); padding: 1rem 1.25rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); display: flex;
      justify-content: space-between; align-items: center;
      border-left: 4px solid var(--color-success);
    }
    .channel-card.inactive { border-left-color: var(--color-text-muted); }

    .channel-header { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.25rem; flex-wrap: wrap; }
    .channel-header h3 { margin: 0; font-size: 1rem; color: var(--color-text); }
    .platform-badge {
      background: var(--color-info-bg); color: var(--color-info-text);
      padding: 0.1rem 0.5rem; border-radius: 4px; font-size: 0.7rem; font-weight: 600;
    }
    .status-badge {
      font-size: 0.65rem; padding: 0.15rem 0.5rem; border-radius: 10px;
      text-transform: uppercase; font-weight: 600;
    }
    .status-badge.active { background: var(--color-success-bg); color: var(--color-success-text); }
    .status-badge.inactive { background: var(--color-border); color: var(--color-text-secondary); }
    .meta { display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; color: var(--color-text-muted); }
    .flag {
      background: var(--color-info-bg); color: var(--color-info-text);
      padding: 0.1rem 0.4rem; border-radius: 4px; font-size: 0.7rem; font-weight: 600;
    }

    .channel-actions { display: flex; gap: 0.5rem; flex-shrink: 0; }
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

    .spinner, .spinner-sm, .spinner-xs {
      display: inline-block; border-radius: 50%; animation: spin 0.6s linear infinite;
    }
    .spinner { width: 16px; height: 16px; border: 2px solid var(--color-border); border-top-color: var(--color-primary); }
    .spinner-sm { width: 12px; height: 12px; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; }
    .spinner-xs { width: 10px; height: 10px; border: 2px solid var(--color-border); border-top-color: var(--color-primary); }
    @keyframes spin { to { transform: rotate(360deg); } }

    @media (max-width: 768px) {
      .form-grid { grid-template-columns: 1fr; }
      .form-group.full { grid-column: span 1; }
      .channel-card { flex-direction: column; align-items: flex-start; gap: 0.75rem; }
      .channel-actions { width: 100%; flex-wrap: wrap; }
    }
  `],
})
export class ChannelsPageComponent implements OnInit {
  private channelsService = inject(ChannelsService);
  private i18n = inject(TranslationService);

  platforms: MessagingPlatform[] = ['SLACK', 'DISCORD', 'TELEGRAM'];

  channels = signal<MessagingChannel[]>([]);
  loading = signal(false);
  saving = signal(false);
  showForm = signal(false);
  editingId = signal<string | null>(null);
  actionId = signal<string | null>(null);
  actionType = signal<string | null>(null);
  notification = signal<{ type: string; message: string } | null>(null);

  formName = '';
  formPlatform: MessagingPlatform = 'SLACK';
  formBotToken = '';
  formWebhookSecret = '';
  formProvider = '';
  formModel = '';
  formSystemPrompt = '';
  formEnableRag = true;
  formEnableToolCalling = false;

  ngOnInit(): void {
    this.loadChannels();
  }

  loadChannels(): void {
    this.loading.set(true);
    this.channelsService.listChannels().subscribe({
      next: (channels) => {
        this.channels.set(channels);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  openForm(channel?: MessagingChannel): void {
    if (channel) {
      this.editingId.set(channel.id);
      this.formName = channel.name;
      this.formPlatform = channel.platform;
      this.formBotToken = channel.botToken ?? '';
      this.formWebhookSecret = channel.webhookSecret ?? '';
      this.formProvider = channel.defaultProvider ?? '';
      this.formModel = channel.defaultModel ?? '';
      this.formSystemPrompt = channel.defaultSystemPrompt ?? '';
      this.formEnableRag = channel.enableRag;
      this.formEnableToolCalling = channel.enableToolCalling;
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
    return (
      this.formName.trim().length >= 2 &&
      this.formBotToken.trim().length > 0 &&
      !!this.formPlatform
    );
  }

  saveChannel(): void {
    if (!this.isFormValid()) return;

    const request: CreateChannelRequest = {
      name: this.formName.trim(),
      platform: this.formPlatform,
      botToken: this.formBotToken.trim(),
      webhookSecret: this.formWebhookSecret.trim() || undefined,
      defaultSystemPrompt: this.formSystemPrompt.trim() || undefined,
      defaultModel: this.formModel.trim() || undefined,
      defaultProvider: this.formProvider.trim() || undefined,
      enableRag: this.formEnableRag,
      enableToolCalling: this.formEnableToolCalling,
    };

    this.saving.set(true);
    const id = this.editingId();
    const call$ = id
      ? this.channelsService.updateChannel(id, request)
      : this.channelsService.createChannel(request);

    call$.subscribe({
      next: () => {
        this.saving.set(false);
        this.closeForm();
        this.showNotify('success', this.i18n.instant('COMMON.SUCCESS'));
        this.loadChannels();
      },
      error: () => {
        this.saving.set(false);
        this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
      },
    });
  }

  deleteChannel(id: string): void {
    if (!confirm(this.i18n.instant('CHANNELS.CONFIRM_DELETE'))) return;

    this.actionId.set(id);
    this.actionType.set('delete');
    this.channelsService.deleteChannel(id).subscribe({
      next: () => {
        this.actionId.set(null);
        this.showNotify('success', this.i18n.instant('COMMON.SUCCESS'));
        this.loadChannels();
      },
      error: () => {
        this.actionId.set(null);
        this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
      },
    });
  }

  testChannel(id: string): void {
    this.actionId.set(id);
    this.actionType.set('test');
    this.channelsService.testChannel(id).subscribe({
      next: (res) => {
        this.actionId.set(null);
        this.showNotify(res.status === 'OK' ? 'success' : 'error', res.message);
      },
      error: () => {
        this.actionId.set(null);
        this.showNotify('error', this.i18n.instant('CHANNELS.TEST_FAILED'));
      },
    });
  }

  private resetForm(): void {
    this.formName = '';
    this.formPlatform = 'SLACK';
    this.formBotToken = '';
    this.formWebhookSecret = '';
    this.formProvider = '';
    this.formModel = '';
    this.formSystemPrompt = '';
    this.formEnableRag = true;
    this.formEnableToolCalling = false;
  }

  private showNotify(type: string, message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 4000);
  }
}
