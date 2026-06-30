import { Component, inject, signal, OnInit } from '@angular/core';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { BackupService, BackupInfo } from '../../services/backup.service';
import { DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-backup-page',
  standalone: true,
  imports: [TranslatePipe, DecimalPipe, DatePipe, FormsModule],
  template: `
    <div class="page-container">
      <header class="page-header">
        <div class="header-text">
          <h1>{{ 'BACKUP.TITLE' | translate }}</h1>
          <p class="subtitle">{{ 'BACKUP.SUBTITLE' | translate }}</p>
        </div>
        <div class="header-stats">
          <div class="stat">
            <span class="stat-value">{{ backups().length }}</span>
            <span class="stat-label">{{ 'BACKUP.TOTAL' | translate }}</span>
          </div>
        </div>
      </header>

      @if (message()) {
        <div class="alert" [class.alert-success]="!isError()" [class.alert-error]="isError()" role="status">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            @if (isError()) {
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            } @else {
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
            }
          </svg>
          <span>{{ message() }}</span>
        </div>
      }

      <section class="actions-grid">
        <article class="card">
          <div class="card-head">
            <div class="card-icon primary">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
            </div>
            <div>
              <h2>{{ 'BACKUP.CREATE_TITLE' | translate }}</h2>
              <p class="card-sub">{{ 'BACKUP.CREATE_DESC' | translate }}</p>
            </div>
          </div>

          <div class="options">
            <label class="option" [class.checked]="includeDatabase">
              <input type="checkbox" [(ngModel)]="includeDatabase" />
              <div class="option-body">
                <span class="option-title">{{ 'BACKUP.COMPONENT_DATABASE' | translate }}</span>
                <span class="option-desc">{{ 'BACKUP.COMPONENT_DATABASE_DESC' | translate }}</span>
              </div>
            </label>
            <label class="option" [class.checked]="includeConfig">
              <input type="checkbox" [(ngModel)]="includeConfig" />
              <div class="option-body">
                <span class="option-title">{{ 'BACKUP.COMPONENT_CONFIG' | translate }}</span>
                <span class="option-desc">{{ 'BACKUP.COMPONENT_CONFIG_DESC' | translate }}</span>
              </div>
            </label>
            <label class="option" [class.checked]="includeDocuments">
              <input type="checkbox" [(ngModel)]="includeDocuments" />
              <div class="option-body">
                <span class="option-title">{{ 'BACKUP.COMPONENT_DOCUMENTS' | translate }}</span>
                <span class="option-desc">{{ 'BACKUP.COMPONENT_DOCUMENTS_DESC' | translate }}</span>
              </div>
            </label>
          </div>

          <button class="btn btn-primary btn-block" (click)="createBackup()"
                  [disabled]="creating() || !hasSelection()">
            @if (creating()) { <span class="spinner-sm"></span> }
            {{ creating() ? ('COMMON.LOADING' | translate) : ('BACKUP.CREATE' | translate) }}
          </button>
        </article>

        <article class="card">
          <div class="card-head">
            <div class="card-icon accent">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 12a9 9 0 1 0 9-9"/><polyline points="3 4 3 12 11 12"/>
              </svg>
            </div>
            <div>
              <h2>{{ 'BACKUP.RESTORE_TITLE' | translate }}</h2>
              <p class="card-sub">{{ 'BACKUP.RESTORE_DESC' | translate }}</p>
            </div>
          </div>

          <label class="dropzone" [class.dragover]="dragover()"
                 (dragover)="$event.preventDefault(); dragover.set(true)"
                 (dragleave)="dragover.set(false)"
                 (drop)="onDrop($event)">
            <input #fileInput type="file" accept=".zip" (change)="onRestoreFile($event)" hidden />
            <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            <span class="dropzone-title">{{ 'BACKUP.RESTORE_HINT' | translate }}</span>
            <span class="dropzone-sub">{{ 'BACKUP.RESTORE_FORMAT' | translate }}</span>
          </label>

          <button class="btn btn-secondary btn-block" (click)="fileInput.click()">
            {{ 'BACKUP.RESTORE' | translate }}
          </button>
        </article>
      </section>

      <section class="list-section">
        <div class="list-head">
          <h2>{{ 'BACKUP.HISTORY' | translate }}</h2>
          <button class="btn-ghost" (click)="loadBackups()" [title]="'COMMON.REFRESH' | translate">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
          </button>
        </div>

        <div class="backup-list">
          @for (backup of backups(); track backup.id) {
            <article class="backup-card">
              <div class="file-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                </svg>
              </div>
              <div class="backup-info">
                <span class="filename">{{ backup.filename }}</span>
                <div class="meta">
                  <span class="meta-item">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                    </svg>
                    {{ backup.createdAt | date:'medium' }}
                  </span>
                  <span class="meta-item">{{ backup.sizeBytes | number }} B</span>
                </div>
                <div class="components">
                  @for (c of backup.components; track c) {
                    <span class="badge">{{ c }}</span>
                  }
                </div>
              </div>
              <div class="backup-actions">
                <button class="btn-icon" (click)="downloadBackup(backup.filename)"
                        [title]="'BACKUP.DOWNLOAD' | translate">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
                  </svg>
                </button>
                <button class="btn-icon btn-icon-danger" (click)="deleteBackup(backup.filename)"
                        [title]="'COMMON.DELETE' | translate">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6l-2 14a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2L5 6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                </button>
              </div>
            </article>
          } @empty {
            <div class="empty-state">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
                <ellipse cx="12" cy="5" rx="9" ry="3"/>
                <path d="M3 5v14c0 1.66 4.03 3 9 3s9-1.34 9-3V5"/>
                <path d="M3 12c0 1.66 4.03 3 9 3s9-1.34 9-3"/>
              </svg>
              <h3>{{ 'BACKUP.EMPTY_TITLE' | translate }}</h3>
              <p>{{ 'BACKUP.EMPTY' | translate }}</p>
            </div>
          }
        </div>
      </section>
    </div>
  `,
  styles: [`
    .page-container { padding: 1.75rem 2rem; max-width: 1200px; margin: 0 auto; }

    .page-header {
      display: flex; justify-content: space-between; align-items: flex-start;
      gap: 1.5rem; margin-bottom: 1.75rem;
    }
    .header-text h1 { margin: 0 0 0.35rem; font-size: 1.75rem; color: var(--color-text); }
    .subtitle { margin: 0; color: var(--color-text-secondary); font-size: 0.95rem; }
    .header-stats { display: flex; gap: 1rem; }
    .stat {
      display: flex; flex-direction: column; align-items: center;
      padding: 0.65rem 1.1rem; background: var(--color-card-bg);
      border: 1px solid var(--color-border); border-radius: 10px; min-width: 90px;
    }
    .stat-value { font-size: 1.5rem; font-weight: 700; color: var(--color-primary); line-height: 1; }
    .stat-label {
      font-size: 0.7rem; color: var(--color-text-muted);
      text-transform: uppercase; letter-spacing: 0.05em; margin-top: 0.25rem;
    }

    .alert {
      display: flex; align-items: center; gap: 0.6rem;
      padding: 0.75rem 1rem; border-radius: 8px; margin-bottom: 1.25rem;
      border-left: 4px solid; font-size: 0.9rem;
    }
    .alert-success {
      background: var(--color-success-bg, #e7f7ee); color: var(--color-success-text, #0f5132);
      border-color: var(--color-success, #2ea05a);
    }
    .alert-error {
      background: var(--color-error-bg, #fdecea); color: var(--color-error-text, #842029);
      border-color: var(--color-error, #dc3545);
    }

    .actions-grid {
      display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 2rem;
    }
    .card {
      background: var(--color-card-bg);
      border: 1px solid var(--color-border);
      border-radius: 12px; padding: 1.5rem;
      display: flex; flex-direction: column; gap: 1.25rem;
    }
    .card-head { display: flex; align-items: center; gap: 0.85rem; }
    .card-head h2 { margin: 0; font-size: 1.05rem; color: var(--color-text); }
    .card-sub { margin: 0.15rem 0 0; color: var(--color-text-muted); font-size: 0.82rem; }
    .card-icon {
      width: 40px; height: 40px; border-radius: 10px;
      display: flex; align-items: center; justify-content: center; flex-shrink: 0;
    }
    .card-icon.primary { background: color-mix(in srgb, var(--color-primary) 12%, transparent); color: var(--color-primary); }
    .card-icon.accent  { background: color-mix(in srgb, #7c5cff 12%, transparent); color: #7c5cff; }

    .options { display: flex; flex-direction: column; gap: 0.5rem; }
    .option {
      display: flex; align-items: flex-start; gap: 0.75rem;
      padding: 0.75rem 0.9rem; border: 1px solid var(--color-border);
      border-radius: 8px; cursor: pointer; transition: border-color 0.15s, background 0.15s;
      background: var(--color-bg);
    }
    .option:hover { border-color: var(--color-primary); }
    .option.checked {
      border-color: var(--color-primary);
      background: color-mix(in srgb, var(--color-primary) 6%, transparent);
    }
    .option input[type=checkbox] { margin-top: 0.15rem; cursor: pointer; accent-color: var(--color-primary); }
    .option-body { display: flex; flex-direction: column; gap: 0.15rem; }
    .option-title { font-weight: 600; font-size: 0.88rem; color: var(--color-text); }
    .option-desc { font-size: 0.78rem; color: var(--color-text-muted); }

    .dropzone {
      display: flex; flex-direction: column; align-items: center; gap: 0.4rem;
      padding: 1.75rem 1rem; border: 2px dashed var(--color-border);
      border-radius: 10px; color: var(--color-text-muted); cursor: pointer;
      transition: border-color 0.15s, background 0.15s, color 0.15s;
      background: var(--color-bg);
    }
    .dropzone:hover, .dropzone.dragover {
      border-color: var(--color-primary); color: var(--color-primary);
      background: color-mix(in srgb, var(--color-primary) 6%, transparent);
    }
    .dropzone-title { font-weight: 600; font-size: 0.9rem; }
    .dropzone-sub { font-size: 0.75rem; }

    .btn {
      display: inline-flex; align-items: center; justify-content: center; gap: 0.5rem;
      padding: 0.6rem 1.1rem; border: none; border-radius: 8px; cursor: pointer;
      font-size: 0.9rem; font-family: inherit; font-weight: 600;
    }
    .btn-block { width: 100%; }
    .btn-primary { background: var(--color-primary); color: #fff; }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-light, var(--color-primary)); }
    .btn-secondary {
      background: var(--color-card-bg); color: var(--color-text);
      border: 1px solid var(--color-border);
    }
    .btn-secondary:hover:not(:disabled) { background: var(--color-bg); }
    .btn:disabled { opacity: 0.55; cursor: not-allowed; }

    .list-section { margin-top: 1rem; }
    .list-head {
      display: flex; justify-content: space-between; align-items: center;
      margin-bottom: 1rem;
    }
    .list-head h2 { margin: 0; font-size: 1.1rem; color: var(--color-text); }
    .btn-ghost {
      background: transparent; border: 1px solid var(--color-border);
      color: var(--color-text-muted); width: 32px; height: 32px;
      border-radius: 8px; cursor: pointer; display: inline-flex;
      align-items: center; justify-content: center; transition: all 0.15s;
    }
    .btn-ghost:hover { color: var(--color-primary); border-color: var(--color-primary); }

    .backup-list { display: flex; flex-direction: column; gap: 0.6rem; }
    .backup-card {
      display: flex; align-items: center; gap: 1rem;
      padding: 0.9rem 1.1rem; background: var(--color-card-bg);
      border: 1px solid var(--color-border); border-radius: 10px;
      transition: border-color 0.15s, transform 0.15s;
    }
    .backup-card:hover { border-color: var(--color-primary); transform: translateY(-1px); }

    .file-icon {
      width: 40px; height: 40px; border-radius: 10px; flex-shrink: 0;
      display: flex; align-items: center; justify-content: center;
      background: color-mix(in srgb, var(--color-primary) 10%, transparent);
      color: var(--color-primary);
    }
    .backup-info { flex: 1; display: flex; flex-direction: column; gap: 0.35rem; min-width: 0; }
    .filename {
      font-weight: 600; font-size: 0.92rem; color: var(--color-text);
      overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
    }
    .meta { display: flex; gap: 0.9rem; flex-wrap: wrap; }
    .meta-item {
      display: inline-flex; align-items: center; gap: 0.3rem;
      font-size: 0.78rem; color: var(--color-text-muted);
    }
    .components { display: flex; gap: 0.35rem; flex-wrap: wrap; }
    .badge {
      font-size: 0.68rem; font-weight: 600; padding: 0.15rem 0.5rem;
      border-radius: 10px; text-transform: uppercase; letter-spacing: 0.03em;
      background: color-mix(in srgb, var(--color-primary) 12%, transparent);
      color: var(--color-primary);
    }

    .backup-actions { display: flex; gap: 0.4rem; flex-shrink: 0; }
    .btn-icon {
      width: 34px; height: 34px; border-radius: 8px; cursor: pointer;
      background: transparent; border: 1px solid var(--color-border);
      color: var(--color-text-muted); display: inline-flex;
      align-items: center; justify-content: center; transition: all 0.15s;
    }
    .btn-icon:hover { color: var(--color-primary); border-color: var(--color-primary); }
    .btn-icon-danger:hover {
      color: var(--color-error, #dc3545);
      border-color: var(--color-error, #dc3545);
      background: color-mix(in srgb, var(--color-error, #dc3545) 8%, transparent);
    }

    .empty-state {
      display: flex; flex-direction: column; align-items: center;
      padding: 3rem 1.5rem; text-align: center;
      background: var(--color-card-bg); border: 1px dashed var(--color-border);
      border-radius: 12px; color: var(--color-text-muted);
    }
    .empty-state svg { color: var(--color-text-muted); opacity: 0.5; margin-bottom: 1rem; }
    .empty-state h3 { margin: 0 0 0.35rem; color: var(--color-text); font-size: 1rem; }
    .empty-state p { margin: 0; font-size: 0.85rem; }

    .spinner-sm {
      display: inline-block; width: 14px; height: 14px;
      border: 2px solid rgba(255,255,255,0.35); border-top-color: #fff;
      border-radius: 50%; animation: spin 0.6s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    @media (max-width: 900px) {
      .page-container { padding: 1.25rem; }
      .actions-grid { grid-template-columns: 1fr; }
      .page-header { flex-direction: column; align-items: stretch; }
      .header-stats { justify-content: flex-start; }
      .backup-card { flex-wrap: wrap; }
    }
  `]
})
export class BackupPageComponent implements OnInit {
  private backupService = inject(BackupService);

  backups = signal<BackupInfo[]>([]);
  creating = signal(false);
  message = signal('');
  isError = signal(false);
  dragover = signal(false);

  includeDatabase = true;
  includeConfig = true;
  includeDocuments = false;

  ngOnInit() { this.loadBackups(); }

  hasSelection(): boolean {
    return this.includeDatabase || this.includeConfig || this.includeDocuments;
  }

  loadBackups() {
    this.backupService.list().subscribe({
      next: (list) => this.backups.set(list),
      error: () => {}
    });
  }

  createBackup() {
    const components: string[] = [];
    if (this.includeDatabase) components.push('DATABASE');
    if (this.includeConfig) components.push('CONFIGURATION');
    if (this.includeDocuments) components.push('DOCUMENTS_METADATA');
    if (components.length === 0) return;

    this.creating.set(true);
    this.backupService.create(components).subscribe({
      next: () => {
        this.creating.set(false);
        this.message.set('Backup created successfully');
        this.isError.set(false);
        this.loadBackups();
        setTimeout(() => this.message.set(''), 4000);
      },
      error: () => {
        this.creating.set(false);
        this.message.set('Backup creation failed');
        this.isError.set(true);
      }
    });
  }

  downloadBackup(filename: string) {
    this.backupService.download(filename).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
        URL.revokeObjectURL(url);
      }
    });
  }

  deleteBackup(filename: string) {
    this.backupService.delete(filename).subscribe({
      next: () => this.loadBackups()
    });
  }

  onRestoreFile(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input?.files?.[0];
    if (!file) return;
    this.restoreFile(file);
    input.value = '';
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.dragover.set(false);
    const file = event.dataTransfer?.files?.[0];
    if (file) this.restoreFile(file);
  }

  private restoreFile(file: File) {
    this.backupService.restore(file).subscribe({
      next: () => {
        this.message.set('Restore completed successfully');
        this.isError.set(false);
        setTimeout(() => this.message.set(''), 4000);
      },
      error: () => {
        this.message.set('Restore failed');
        this.isError.set(true);
      }
    });
  }
}
