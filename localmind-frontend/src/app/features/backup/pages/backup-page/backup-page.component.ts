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
      <div class="page-header">
        <h1>{{ 'BACKUP.TITLE' | translate }}</h1>
        <p class="subtitle">{{ 'BACKUP.SUBTITLE' | translate }}</p>
      </div>

      <div class="actions-bar">
        <div class="component-selection">
          <label>
            <input type="checkbox" [(ngModel)]="includeDatabase" />
            {{ 'BACKUP.COMPONENT_DATABASE' | translate }}
          </label>
          <label>
            <input type="checkbox" [(ngModel)]="includeConfig" />
            {{ 'BACKUP.COMPONENT_CONFIG' | translate }}
          </label>
          <label>
            <input type="checkbox" [(ngModel)]="includeDocuments" />
            {{ 'BACKUP.COMPONENT_DOCUMENTS' | translate }}
          </label>
        </div>
        <button class="btn btn-primary" (click)="createBackup()" [disabled]="creating()">
          {{ creating() ? ('COMMON.LOADING' | translate) : ('BACKUP.CREATE' | translate) }}
        </button>
        <button class="btn btn-secondary" (click)="fileInput.click()">
          {{ 'BACKUP.RESTORE' | translate }}
        </button>
        <input #fileInput type="file" accept=".zip" (change)="onRestoreFile($event)" style="display:none" />
      </div>

      @if (message()) {
        <div class="alert" [class.alert-success]="!isError()" [class.alert-error]="isError()">
          {{ message() }}
        </div>
      }

      <div class="backup-list">
        @for (backup of backups(); track backup.id) {
          <div class="backup-card">
            <div class="backup-info">
              <span class="filename">{{ backup.filename }}</span>
              <span class="date">{{ backup.createdAt | date:'medium' }}</span>
              <span class="size">{{ backup.sizeBytes | number }} bytes</span>
              <span class="components">{{ backup.components.join(', ') }}</span>
            </div>
            <div class="backup-actions">
              <button class="btn btn-sm" (click)="downloadBackup(backup.filename)">
                {{ 'BACKUP.DOWNLOAD' | translate }}
              </button>
              <button class="btn btn-sm btn-danger" (click)="deleteBackup(backup.filename)">
                {{ 'COMMON.DELETE' | translate }}
              </button>
            </div>
          </div>
        } @empty {
          <div class="empty-state">
            {{ 'BACKUP.EMPTY' | translate }}
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .page-container { padding: 1.5rem; }
    .page-header h1 { margin: 0 0 0.5rem; }
    .subtitle { color: var(--text-secondary); margin: 0 0 1.5rem; }
    .actions-bar { display: flex; gap: 1rem; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; }
    .component-selection { display: flex; gap: 1rem; }
    .component-selection label { display: flex; align-items: center; gap: 0.3rem; cursor: pointer; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; cursor: pointer; font-size: 0.875rem; }
    .btn-primary { background: var(--primary); color: white; }
    .btn-secondary { background: var(--bg-secondary); color: var(--text-primary); border: 1px solid var(--border); }
    .btn-danger { background: var(--danger, #dc3545); color: white; }
    .btn-sm { padding: 0.3rem 0.6rem; font-size: 0.8rem; }
    .btn:disabled { opacity: 0.6; cursor: not-allowed; }
    .alert { padding: 0.75rem 1rem; border-radius: 6px; margin-bottom: 1rem; }
    .alert-success { background: var(--success-bg, #d4edda); color: var(--success-text, #155724); }
    .alert-error { background: var(--error-bg, #f8d7da); color: var(--error-text, #721c24); }
    .backup-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .backup-card { display: flex; justify-content: space-between; align-items: center; padding: 1rem; background: var(--bg-secondary); border-radius: 8px; border: 1px solid var(--border); }
    .backup-info { display: flex; flex-direction: column; gap: 0.25rem; }
    .filename { font-weight: 600; }
    .date, .size, .components { font-size: 0.85rem; color: var(--text-secondary); }
    .backup-actions { display: flex; gap: 0.5rem; }
    .empty-state { text-align: center; padding: 3rem; color: var(--text-secondary); }
  `]
})
export class BackupPageComponent implements OnInit {
  private backupService = inject(BackupService);

  backups = signal<BackupInfo[]>([]);
  creating = signal(false);
  message = signal('');
  isError = signal(false);

  includeDatabase = true;
  includeConfig = true;
  includeDocuments = false;

  ngOnInit() { this.loadBackups(); }

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
    this.backupService.restore(file).subscribe({
      next: () => {
        this.message.set('Restore completed successfully');
        this.isError.set(false);
      },
      error: () => {
        this.message.set('Restore failed');
        this.isError.set(true);
      }
    });
    input.value = '';
  }
}
