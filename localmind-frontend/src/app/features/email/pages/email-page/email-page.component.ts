import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { EmailService } from '../../services/email.service';
import { EmailMessage, SendEmailRequest } from '../../models/email.model';

@Component({
  selector: 'app-email-page',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  template: `
    <div class="email-page">
      <div class="page-header">
        <div>
          <h1>{{ 'EMAIL.TITLE' | translate }}</h1>
          <p class="subtitle">{{ 'EMAIL.SUBTITLE' | translate }}</p>
        </div>
        <div class="header-actions">
          <button class="btn btn-secondary" (click)="loadInbox()">{{ 'COMMON.REFRESH' | translate }}</button>
          <button class="btn btn-primary" (click)="showCompose.set(!showCompose())">{{ 'EMAIL.COMPOSE' | translate }}</button>
        </div>
      </div>

      @if (showCompose()) {
        <div class="form-card">
          <h3>{{ 'EMAIL.COMPOSE_TITLE' | translate }}</h3>
          <div class="form-group">
            <label>{{ 'EMAIL.TO' | translate }} *</label>
            <input type="text" [(ngModel)]="composeRecipients"
                   [placeholder]="'EMAIL.TO_PLACEHOLDER' | translate" />
          </div>
          <div class="form-group">
            <label>{{ 'EMAIL.SUBJECT' | translate }} *</label>
            <input type="text" [(ngModel)]="composeSubject"
                   [placeholder]="'EMAIL.SUBJECT_PLACEHOLDER' | translate" />
          </div>
          <div class="form-group">
            <label>{{ 'EMAIL.BODY' | translate }}</label>
            <textarea [(ngModel)]="composeBody"
                      [placeholder]="'EMAIL.BODY_PLACEHOLDER' | translate" rows="6"></textarea>
          </div>
          <div class="form-actions">
            <button class="btn btn-secondary" (click)="showCompose.set(false)">{{ 'COMMON.CANCEL' | translate }}</button>
            <button class="btn btn-primary" (click)="sendEmail()"
                    [disabled]="!composeRecipients || !composeSubject">
              {{ 'EMAIL.SEND' | translate }}
            </button>
          </div>
        </div>
      }

      @if (loading()) {
        <p class="loading">{{ 'COMMON.LOADING' | translate }}</p>
      } @else if (messages().length === 0) {
        <div class="empty-state">
          <h3>{{ 'EMAIL.EMPTY_TITLE' | translate }}</h3>
          <p>{{ 'EMAIL.EMPTY_DESC' | translate }}</p>
        </div>
      } @else {
        <div class="inbox-list">
          @for (msg of messages(); track msg.id) {
            <div class="email-card" [class.unread]="!msg.read"
                 (click)="selectMessage(msg)">
              <div class="email-header">
                <span class="email-from">{{ msg.from }}</span>
                <span class="email-date">{{ msg.receivedAt | date:'short' }}</span>
              </div>
              <div class="email-subject">{{ msg.subject }}</div>
              <div class="email-preview">{{ msg.body | slice:0:120 }}</div>
            </div>
          }
        </div>
      }

      @if (selectedMessage()) {
        <div class="detail-overlay" (click)="selectedMessage.set(null)">
          <div class="detail-card" (click)="$event.stopPropagation()">
            <div class="detail-header">
              <h3>{{ selectedMessage()!.subject }}</h3>
              <button class="btn-close" (click)="selectedMessage.set(null)">&times;</button>
            </div>
            <div class="detail-meta">
              <span><strong>{{ 'EMAIL.FROM' | translate }}:</strong> {{ selectedMessage()!.from }}</span>
              <span><strong>{{ 'EMAIL.TO' | translate }}:</strong> {{ selectedMessage()!.to.join(', ') }}</span>
              <span><strong>{{ 'EMAIL.DATE' | translate }}:</strong> {{ selectedMessage()!.receivedAt | date:'medium' }}</span>
            </div>
            <div class="detail-body">{{ selectedMessage()!.body }}</div>
            <div class="detail-actions">
              <button class="btn btn-secondary" (click)="generateDraftReply(selectedMessage()!.id)"
                      [disabled]="generatingReply()">
                @if (generatingReply()) {
                  {{ 'EMAIL.GENERATING_REPLY' | translate }}
                } @else {
                  {{ 'EMAIL.DRAFT_REPLY' | translate }}
                }
              </button>
              @if (!selectedMessage()!.read) {
                <button class="btn btn-secondary" (click)="markAsRead(selectedMessage()!.id)">
                  {{ 'EMAIL.MARK_READ' | translate }}
                </button>
              }
            </div>
            @if (draftReply()) {
              <div class="draft-reply">
                <h4>{{ 'EMAIL.DRAFT_REPLY_TITLE' | translate }}</h4>
                <div class="draft-body">{{ draftReply() }}</div>
              </div>
            }
          </div>
        </div>
      }

      @if (notification()) {
        <div class="notification" [class.error]="notificationError()">{{ notification() }}</div>
      }
    </div>
  `,
  styles: [`
    .email-page { max-width: 900px; }
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
    .page-header h1 { margin: 0; font-size: 1.5rem; }
    .subtitle { color: var(--color-text-muted); margin: 0.25rem 0 0; font-size: 0.875rem; }
    .header-actions { display: flex; gap: 0.5rem; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; cursor: pointer; font-size: 0.875rem; font-weight: 500; transition: all 0.2s; }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn-primary:hover { opacity: 0.9; }
    .btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-secondary { background: var(--color-surface); color: var(--color-text); border: 1px solid var(--color-border); }
    .btn-secondary:hover { background: var(--color-border); }
    .btn-secondary:disabled { opacity: 0.5; cursor: not-allowed; }
    .form-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: 8px; padding: 1.5rem; margin-bottom: 1.5rem; }
    .form-card h3 { margin: 0 0 1rem; }
    .form-group { margin-bottom: 1rem; }
    .form-group label { display: block; margin-bottom: 0.25rem; font-weight: 500; font-size: 0.875rem; }
    .form-group input, .form-group textarea { width: 100%; padding: 0.5rem; border: 1px solid var(--color-border); border-radius: 6px; font-size: 0.875rem; background: var(--color-bg); color: var(--color-text); box-sizing: border-box; }
    .form-group textarea { resize: vertical; }
    .form-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
    .inbox-list { display: flex; flex-direction: column; gap: 4px; }
    .email-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: 8px; padding: 0.75rem 1rem; cursor: pointer; transition: all 0.2s; }
    .email-card:hover { border-color: var(--color-primary); box-shadow: 0 1px 4px rgba(0,0,0,0.05); }
    .email-card.unread { border-left: 3px solid var(--color-primary); }
    .email-header { display: flex; justify-content: space-between; margin-bottom: 0.25rem; }
    .email-from { font-weight: 600; font-size: 0.875rem; }
    .email-date { font-size: 0.75rem; color: var(--color-text-muted); }
    .email-subject { font-size: 0.9rem; font-weight: 500; margin-bottom: 0.25rem; }
    .email-preview { font-size: 0.8rem; color: var(--color-text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .detail-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 100; }
    .detail-card { background: var(--color-surface); border-radius: 12px; padding: 1.5rem; max-width: 700px; width: 90%; max-height: 80vh; overflow-y: auto; }
    .detail-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem; }
    .detail-header h3 { margin: 0; }
    .btn-close { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: var(--color-text-muted); padding: 0 0.25rem; }
    .detail-meta { display: flex; flex-direction: column; gap: 0.25rem; font-size: 0.8rem; color: var(--color-text-muted); margin-bottom: 1rem; padding-bottom: 1rem; border-bottom: 1px solid var(--color-border); }
    .detail-body { white-space: pre-wrap; font-size: 0.9rem; line-height: 1.6; margin-bottom: 1rem; }
    .detail-actions { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
    .draft-reply { background: var(--color-bg); border: 1px solid var(--color-border); border-radius: 8px; padding: 1rem; }
    .draft-reply h4 { margin: 0 0 0.5rem; font-size: 0.9rem; }
    .draft-body { white-space: pre-wrap; font-size: 0.85rem; color: var(--color-text-muted); }
    .empty-state { text-align: center; padding: 3rem 1rem; color: var(--color-text-muted); }
    .loading { text-align: center; color: var(--color-text-muted); padding: 2rem; }
    .notification { position: fixed; top: 1rem; right: 1rem; padding: 0.75rem 1.25rem; border-radius: 8px; background: var(--color-success-bg); color: var(--color-success-text); font-size: 0.85rem; z-index: 1000; animation: slideIn 0.3s ease; }
    .notification.error { background: var(--color-error-bg); color: var(--color-error-text); }
    @keyframes slideIn { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }
  `]
})
export class EmailPageComponent implements OnInit {
  private emailService = inject(EmailService);

  messages = signal<EmailMessage[]>([]);
  loading = signal(false);
  showCompose = signal(false);
  selectedMessage = signal<EmailMessage | null>(null);
  draftReply = signal('');
  generatingReply = signal(false);
  notification = signal('');
  notificationError = signal(false);

  composeRecipients = '';
  composeSubject = '';
  composeBody = '';

  ngOnInit() {
    this.loadInbox();
  }

  loadInbox() {
    this.loading.set(true);
    this.emailService.listInbox(20).subscribe({
      next: (msgs) => {
        this.messages.set(msgs);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.showNotification('EMAIL.LOAD_ERROR', true);
      }
    });
  }

  sendEmail() {
    const request: SendEmailRequest = {
      to: this.composeRecipients.split(',').map(s => s.trim()),
      subject: this.composeSubject,
      body: this.composeBody
    };

    this.emailService.sendEmail(request).subscribe({
      next: () => {
        this.showCompose.set(false);
        this.resetComposeForm();
        this.showNotification('EMAIL.SEND_SUCCESS', false);
      },
      error: () => this.showNotification('EMAIL.SEND_ERROR', true)
    });
  }

  selectMessage(msg: EmailMessage) {
    this.selectedMessage.set(msg);
    this.draftReply.set('');
  }

  markAsRead(id: string) {
    this.emailService.markAsRead(id).subscribe({
      next: () => {
        this.messages.set(this.messages().map(m =>
          m.id === id ? { ...m, read: true } : m
        ));
        if (this.selectedMessage()?.id === id) {
          this.selectedMessage.set({ ...this.selectedMessage()!, read: true });
        }
        this.showNotification('EMAIL.MARK_READ_SUCCESS', false);
      },
      error: () => this.showNotification('EMAIL.MARK_READ_ERROR', true)
    });
  }

  generateDraftReply(id: string) {
    this.generatingReply.set(true);
    this.draftReply.set('');
    this.emailService.generateDraftReply(id).subscribe({
      next: (resp) => {
        this.draftReply.set(resp.draftBody);
        this.generatingReply.set(false);
      },
      error: () => {
        this.generatingReply.set(false);
        this.showNotification('EMAIL.DRAFT_ERROR', true);
      }
    });
  }

  private resetComposeForm() {
    this.composeRecipients = '';
    this.composeSubject = '';
    this.composeBody = '';
  }

  private showNotification(msg: string, isError: boolean) {
    this.notification.set(msg);
    this.notificationError.set(isError);
    setTimeout(() => this.notification.set(''), 4000);
  }
}
