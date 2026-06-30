import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FolderService } from '../../services/folder.service';
import { MonitoredFolder, CreateFolderRequest } from '../../models/folder.model';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { LoadingSkeletonComponent } from '../../../../shared/components/loading-skeleton/loading-skeleton.component';

@Component({
  selector: 'app-folder-config-page',
  imports: [FormsModule, TranslatePipe, LoadingSkeletonComponent],
  template: `
    <div class="folders-page">
      <div class="page-header">
        <div>
          <h1>{{ 'FOLDERS.TITLE' | translate }}</h1>
          <p class="subtitle">{{ 'FOLDERS.SUBTITLE' | translate }}</p>
        </div>
        <button class="btn btn-primary" (click)="showForm = !showForm">
          {{ showForm ? ('COMMON.CANCEL' | translate) : ('FOLDERS.ADD' | translate) }}
        </button>
      </div>

      @if (showForm) {
        <div class="add-form card">
          <h3>{{ 'FOLDERS.FORM_TITLE' | translate }}</h3>
          <div class="form-grid">
            <div class="form-group full-width">
              <label>{{ 'FOLDERS.FORM_PATH' | translate }}</label>
              <input type="text" [(ngModel)]="newFolder.path" name="folderPath" #folderPath="ngModel"
                     [placeholder]="'FOLDERS.FORM_PATH_PLACEHOLDER' | translate" required minlength="2">
              @if (folderPath.invalid && folderPath.touched) {
                <span class="form-error">
                  @if (folderPath.errors?.['required']) { {{ 'VALIDATION.REQUIRED' | translate }} }
                  @else if (folderPath.errors?.['minlength']) { {{ 'VALIDATION.MIN_LENGTH' | translate:{ min: 2 } }} }
                </span>
              }
            </div>
            <div class="form-group">
              <label>{{ 'FOLDERS.FORM_PATTERN' | translate }}</label>
              <input type="text" [(ngModel)]="newFolder.pattern" [placeholder]="'FOLDERS.FORM_PATTERN_PLACEHOLDER' | translate">
            </div>
            <div class="form-group checkbox-wrap">
              <label>
                <input type="checkbox" [(ngModel)]="newFolder.recursive">
                {{ 'FOLDERS.FORM_RECURSIVE' | translate }}
              </label>
            </div>
          </div>
          <button class="btn btn-primary" (click)="addFolder()" [disabled]="!newFolder.path.trim()">
            {{ 'COMMON.ADD' | translate }}
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
      } @else if (folders().length === 0) {
        <div class="empty-state card">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/>
          </svg>
          <h3>{{ 'FOLDERS.EMPTY_TITLE' | translate }}</h3>
          <p>{{ 'FOLDERS.EMPTY_DESC' | translate }}</p>
        </div>
      } @else {
        <div class="folder-list">
          @for (folder of folders(); track folder.id) {
            <div class="folder-card">
              <div class="folder-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/>
                </svg>
              </div>
              <div class="folder-info">
                <div class="folder-path">{{ folder.path }}</div>
                <div class="folder-meta">
                  <span class="badge" [class]="getStatusClass(folder.status)">{{ folder.status }}</span>
                  <span class="meta-item">{{ 'FOLDERS.DOCUMENTS_COUNT' | translate:{ count: folder.documentCount } }}</span>
                  @if (folder.pattern) {
                    <span class="meta-item mono">{{ folder.pattern }}</span>
                  }
                  <span class="meta-item">{{ folder.recursive ? ('FOLDERS.RECURSIVE' | translate) : ('FOLDERS.ROOT_ONLY' | translate) }}</span>
                </div>
                @if (folder.lastSyncAt) {
                  <div class="folder-sync">{{ 'FOLDERS.LAST_SYNC' | translate }} {{ formatDate(folder.lastSyncAt) }}</div>
                }
              </div>
              <div class="folder-actions">
                <button class="btn-action" [title]="'FOLDERS.SYNC_TITLE' | translate" (click)="syncFolder(folder.id)"
                        [disabled]="folder.status === 'SYNCING'">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M23 4v6h-6M1 20v-6h6"/>
                    <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
                  </svg>
                </button>
                <button class="btn-action danger" [title]="'FOLDERS.REMOVE_TITLE' | translate" (click)="removeFolder(folder.id)">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                  </svg>
                </button>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .folders-page { max-width: 900px; }
    .page-header { display: flex; justify-content: space-between; align-items: start; margin-bottom: 1.5rem; }
    .page-header h1 { margin: 0; font-size: 1.5rem; }
    .subtitle { color: var(--color-text-muted); font-size: 0.85rem; margin: 0.15rem 0 0 0; }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-light); }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .card { background: var(--color-card-bg); border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); padding: 1.25rem; }
    .add-form { margin-bottom: 1.5rem; }
    .add-form h3 { margin: 0 0 1rem 0; font-size: 1rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8rem; font-weight: 500; color: var(--color-text); }
    .form-group input[type="text"] { padding: 0.5rem; border: 1px solid var(--color-border); border-radius: 4px; font-size: 0.85rem; background: var(--color-input-bg); color: var(--color-text); }
    .form-error { font-size: 0.75rem; color: var(--color-error); margin-top: 0.15rem; }
    .full-width { grid-column: 1 / -1; }
    .checkbox-wrap { justify-content: end; }
    .checkbox-wrap label { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-size: 0.85rem; }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: var(--color-success-bg); color: var(--color-success-text); }
    .notification.error { background: var(--color-error-bg); color: var(--color-error-text); }

    .loading-state { display: flex; align-items: center; gap: 0.5rem; justify-content: center; padding: 3rem; color: var(--color-text-muted); }
    .spinner {
      display: inline-block; width: 16px; height: 16px;
      border: 2px solid var(--color-border); border-top-color: var(--color-primary); border-radius: 50%;
      animation: spin 0.6s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .empty-state { text-align: center; padding: 3rem; }
    .empty-state svg { margin-bottom: 1rem; }
    .empty-state h3 { color: var(--color-text-secondary); margin-bottom: 0.25rem; }
    .empty-state p { font-size: 0.85rem; color: var(--color-text-muted); }

    .folder-list { display: flex; flex-direction: column; gap: 0.5rem; }
    .folder-card {
      background: var(--color-card-bg); padding: 1rem 1.25rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06);
      display: flex; align-items: center; gap: 1rem;
    }
    .folder-icon { color: var(--color-warning); flex-shrink: 0; }
    .folder-info { flex: 1; min-width: 0; }
    .folder-path { font-weight: 600; font-size: 0.9rem; margin-bottom: 0.3rem; font-family: monospace; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .folder-meta { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 0.15rem; }
    .badge { font-size: 0.65rem; padding: 0.1rem 0.45rem; border-radius: 10px; font-weight: 600; text-transform: uppercase; }
    .badge-success { background: var(--color-success-bg); color: var(--color-success-text); }
    .badge-warning { background: var(--color-warning-bg); color: var(--color-warning-text); }
    .badge-error { background: var(--color-error-bg); color: var(--color-error-text); }
    .badge-info { background: #d1ecf1; color: #0c5460; }
    .meta-item { font-size: 0.75rem; color: var(--color-text-muted); }
    .mono { font-family: monospace; }
    .folder-sync { font-size: 0.7rem; color: var(--color-text-muted); }
    .folder-actions { display: flex; gap: 0.35rem; }
    .btn-action {
      padding: 0.4rem; background: none; border: 1px solid var(--color-border);
      border-radius: 6px; cursor: pointer; color: var(--color-text-secondary); transition: all 0.15s;
    }
    .btn-action:hover { background: var(--color-bg); color: var(--color-text); }
    .btn-action:disabled { opacity: 0.4; cursor: not-allowed; }
    .btn-action.danger { border-color: var(--color-error-bg); color: var(--color-error); }
    .btn-action.danger:hover { background: var(--color-error-bg); }
  `]
})
export class FolderConfigPageComponent implements OnInit {
  private folderService = inject(FolderService);
  private i18n = inject(TranslationService);

  folders = signal<MonitoredFolder[]>([]);
  loading = signal(false);
  showForm = false;
  notification = signal<{ type: string; message: string } | null>(null);

  newFolder: CreateFolderRequest = { path: '', pattern: '', recursive: true };

  ngOnInit() { this.loadFolders(); }

  loadFolders() {
    this.loading.set(true);
    this.folderService.listFolders().subscribe({
      next: (f) => { this.folders.set(f); this.loading.set(false); },
      error: () => { this.loading.set(false); }
    });
  }

  addFolder() {
    if (!this.newFolder.path?.trim()) return;
    this.folderService.addFolder(this.newFolder).subscribe({
      next: () => {
        this.showForm = false;
        this.newFolder = { path: '', pattern: '', recursive: true };
        this.showNotify('success', this.i18n.instant('FOLDERS.ADD_SUCCESS'));
        this.loadFolders();
      },
      error: () => this.showNotify('error', this.i18n.instant('FOLDERS.ADD_ERROR'))
    });
  }

  removeFolder(id: string) {
    this.folderService.removeFolder(id).subscribe({
      next: () => { this.showNotify('success', this.i18n.instant('FOLDERS.REMOVE_SUCCESS')); this.loadFolders(); },
      error: () => this.showNotify('error', this.i18n.instant('FOLDERS.REMOVE_ERROR'))
    });
  }

  syncFolder(id: string) {
    this.folderService.syncFolder(id).subscribe({
      next: () => { this.showNotify('success', this.i18n.instant('FOLDERS.SYNC_SUCCESS')); this.loadFolders(); },
      error: () => this.showNotify('error', this.i18n.instant('FOLDERS.SYNC_ERROR'))
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'badge-success';
      case 'SYNCING': return 'badge-info';
      case 'ERROR': return 'badge-error';
      case 'PAUSED': return 'badge-warning';
      default: return 'badge-info';
    }
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const locale = this.i18n.currentLang() === 'it' ? 'it-IT' : 'en-US';
    return new Date(dateStr).toLocaleDateString(locale, { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  private showNotify(type: string, message: string) {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 3000);
  }
}
