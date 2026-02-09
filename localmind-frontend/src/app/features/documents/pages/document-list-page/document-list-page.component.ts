import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DocumentService } from '../../services/document.service';
import { Document } from '../../models/document.model';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../../core/i18n/translation.service';

@Component({
  selector: 'app-document-list-page',
  imports: [FormsModule, TranslatePipe],
  template: `
    <div class="documents-page">
      <div class="page-header">
        <div>
          <h1>{{ 'DOCUMENTS.TITLE' | translate }}</h1>
          <span class="subtitle">{{ 'DOCUMENTS.COUNT' | translate:{ count: filteredDocuments().length } }}</span>
        </div>
        <div class="header-actions">
          <button class="btn btn-secondary btn-sm" (click)="loadDocuments()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M23 4v6h-6M1 20v-6h6"/>
              <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
            </svg>
            {{ 'COMMON.REFRESH' | translate }}
          </button>
          <button class="btn btn-primary" (click)="triggerUpload()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
            </svg>
            {{ 'DOCUMENTS.UPLOAD' | translate }}
          </button>
          <input type="file" #fileInput (change)="onFileSelected($event)" style="display:none"
                 accept=".pdf,.txt,.md,.doc,.docx,.csv,.json">
        </div>
      </div>

      @if (notification()) {
        <div class="notification" [class]="notification()!.type">
          {{ notification()!.message }}
          <button class="close-btn" (click)="notification.set(null)">&times;</button>
        </div>
      }

      @if (uploading()) {
        <div class="upload-bar">
          <div class="upload-info">
            <span class="loading-spinner"></span>
            {{ 'DOCUMENTS.UPLOADING' | translate:{ filename: uploadFileName() } }}
          </div>
        </div>
      }

      <div class="filter-tabs">
        <button [class.active]="activeFilter() === 'ALL'" (click)="activeFilter.set('ALL')">
          {{ 'DOCUMENTS.FILTER_ALL' | translate }} ({{ documents().length }})
        </button>
        <button [class.active]="activeFilter() === 'INDEXED'" (click)="activeFilter.set('INDEXED')">
          {{ 'DOCUMENTS.FILTER_INDEXED' | translate }} ({{ countByStatus('INDEXED') }})
        </button>
        <button [class.active]="activeFilter() === 'PENDING'" (click)="activeFilter.set('PENDING')">
          {{ 'DOCUMENTS.FILTER_PENDING' | translate }} ({{ countByStatus('PENDING') }})
        </button>
        <button [class.active]="activeFilter() === 'PROCESSING'" (click)="activeFilter.set('PROCESSING')">
          {{ 'DOCUMENTS.FILTER_PROCESSING' | translate }} ({{ countByStatus('PROCESSING') }})
        </button>
        <button [class.active]="activeFilter() === 'FAILED'" (click)="activeFilter.set('FAILED')">
          {{ 'DOCUMENTS.FILTER_FAILED' | translate }} ({{ countByStatus('FAILED') }})
        </button>
      </div>

      @if (loading()) {
        <div class="loading-state">
          <span class="loading-spinner"></span>
          <span>{{ 'DOCUMENTS.LOADING' | translate }}</span>
        </div>
      } @else if (filteredDocuments().length === 0) {
        <div class="empty-state card">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
          <h3>{{ 'DOCUMENTS.EMPTY_TITLE' | translate }}</h3>
          <p>{{ 'DOCUMENTS.EMPTY_DESC' | translate }}</p>
        </div>
      } @else {
        <div class="doc-grid">
          @for (doc of filteredDocuments(); track doc.id) {
            <div class="doc-card">
              <div class="doc-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                </svg>
              </div>
              <div class="doc-info">
                <div class="doc-name">{{ doc.filename }}</div>
                <div class="doc-meta">
                  <span class="badge" [class]="getStatusClass(doc.status)">{{ 'ENUM.DOCUMENT_STATUS.' + doc.status | translate }}</span>
                  <span class="doc-size">{{ formatSize(doc.fileSize) }}</span>
                  <span class="doc-type">{{ doc.mimeType }}</span>
                </div>
                <div class="doc-date">{{ 'DOCUMENTS.UPLOADED_LABEL' | translate }} {{ formatDate(doc.createdAt) }}</div>
              </div>
              <div class="doc-actions">
                <button class="btn-icon" title="Elimina" (click)="deleteDoc(doc)">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                  </svg>
                </button>
              </div>
            </div>
          }
        </div>
      }

      @if (confirmDelete()) {
        <div class="modal-overlay" (click)="confirmDelete.set(null)">
          <div class="modal" (click)="$event.stopPropagation()">
            <h3>{{ 'DOCUMENTS.CONFIRM_DELETE_TITLE' | translate }}</h3>
            <p [innerHTML]="'DOCUMENTS.CONFIRM_DELETE_MSG' | translate:{ filename: confirmDelete()!.filename }"></p>
            <div class="modal-actions">
              <button class="btn btn-secondary" (click)="confirmDelete.set(null)">{{ 'COMMON.CANCEL' | translate }}</button>
              <button class="btn btn-danger" (click)="confirmDeleteAction()">{{ 'COMMON.DELETE' | translate }}</button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .documents-page { max-width: 1100px; }
    .page-header { display: flex; justify-content: space-between; align-items: start; margin-bottom: 1.25rem; }
    .page-header h1 { margin: 0; font-size: 1.5rem; }
    .subtitle { font-size: 0.8rem; color: #999; }
    .header-actions { display: flex; gap: 0.5rem; align-items: center; }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.45rem 0.9rem; border: none; border-radius: 6px; font-size: 0.8rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: #0f3460; color: white; }
    .btn-primary:hover { background: #1a4a7a; }
    .btn-secondary { background: white; color: #333; border: 1px solid #ddd; }
    .btn-secondary:hover { background: #f5f5f5; }
    .btn-danger { background: #e74c3c; color: white; }
    .btn-sm { padding: 0.35rem 0.7rem; }
    .btn-icon { padding: 0.35rem; background: none; border: none; cursor: pointer; color: #999; border-radius: 4px; }
    .btn-icon:hover { background: #fee; color: #e74c3c; }

    .notification {
      padding: 0.6rem 1rem;
      border-radius: 6px;
      font-size: 0.8rem;
      margin-bottom: 1rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .notification.success { background: #d4edda; color: #155724; }
    .notification.error { background: #f8d7da; color: #721c24; }
    .close-btn { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: inherit; }

    .upload-bar {
      background: #e3f2fd;
      padding: 0.6rem 1rem;
      border-radius: 6px;
      margin-bottom: 1rem;
    }
    .upload-info { display: flex; align-items: center; gap: 0.5rem; font-size: 0.85rem; color: #1565c0; }
    .loading-spinner {
      display: inline-block; width: 16px; height: 16px;
      border: 2px solid #ddd; border-top-color: #0f3460;
      border-radius: 50%; animation: spin 0.6s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .filter-tabs {
      display: flex; gap: 0; border-bottom: 2px solid #e0e0e0;
      margin-bottom: 1.25rem; overflow-x: auto;
    }
    .filter-tabs button {
      padding: 0.6rem 1rem; background: none; border: none;
      border-bottom: 2px solid transparent; margin-bottom: -2px;
      cursor: pointer; font-size: 0.8rem; color: #888;
      white-space: nowrap; font-family: inherit;
    }
    .filter-tabs button:hover { color: #333; }
    .filter-tabs button.active { color: #0f3460; border-bottom-color: #0f3460; font-weight: 600; }

    .loading-state {
      display: flex; align-items: center; gap: 0.5rem;
      justify-content: center; padding: 3rem; color: #999;
    }

    .empty-state {
      text-align: center; padding: 3rem; color: #999;
    }
    .empty-state svg { margin-bottom: 1rem; }
    .empty-state h3 { color: #666; margin-bottom: 0.25rem; }
    .empty-state p { font-size: 0.85rem; }

    .doc-grid { display: flex; flex-direction: column; gap: 0.5rem; }
    .doc-card {
      background: white; padding: 1rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06);
      display: flex; align-items: center; gap: 1rem;
    }
    .doc-icon { color: #0f3460; flex-shrink: 0; }
    .doc-info { flex: 1; min-width: 0; }
    .doc-name { font-weight: 600; font-size: 0.9rem; margin-bottom: 0.3rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .doc-meta { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 0.15rem; }
    .badge {
      font-size: 0.65rem; padding: 0.1rem 0.45rem; border-radius: 10px;
      font-weight: 600; text-transform: uppercase;
    }
    .badge-success { background: #d4edda; color: #155724; }
    .badge-warning { background: #fff3cd; color: #856404; }
    .badge-error { background: #f8d7da; color: #721c24; }
    .badge-info { background: #d1ecf1; color: #0c5460; }
    .badge-neutral { background: #e2e3e5; color: #383d41; }
    .doc-size, .doc-type { font-size: 0.75rem; color: #999; }
    .doc-date { font-size: 0.7rem; color: #bbb; }

    .modal-overlay {
      position: fixed; top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.4); display: flex;
      align-items: center; justify-content: center; z-index: 1000;
    }
    .modal {
      background: white; padding: 1.5rem; border-radius: 10px;
      box-shadow: 0 10px 40px rgba(0,0,0,0.2); max-width: 400px; width: 90%;
    }
    .modal h3 { margin: 0 0 0.5rem 0; }
    .modal p { font-size: 0.9rem; color: #666; margin-bottom: 1.25rem; }
    .modal-actions { display: flex; gap: 0.5rem; justify-content: flex-end; }
  `]
})
export class DocumentListPageComponent implements OnInit {
  private documentService = inject(DocumentService);
  private i18n = inject(TranslationService);

  documents = signal<Document[]>([]);
  loading = signal(false);
  uploading = signal(false);
  uploadFileName = signal('');
  activeFilter = signal<string>('ALL');
  confirmDelete = signal<Document | null>(null);
  notification = signal<{ type: string; message: string } | null>(null);

  filteredDocuments = computed(() => {
    const filter = this.activeFilter();
    const docs = this.documents();
    if (filter === 'ALL') return docs;
    return docs.filter(d => d.status === filter);
  });

  ngOnInit() {
    this.loadDocuments();
  }

  loadDocuments() {
    this.loading.set(true);
    this.documentService.listDocuments().subscribe({
      next: (docs) => { this.documents.set(docs); this.loading.set(false); },
      error: () => { this.loading.set(false); this.showNotification('error', this.i18n.instant('DOCUMENTS.LOAD_ERROR')); }
    });
  }

  triggerUpload() {
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    input?.click();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.uploading.set(true);
    this.uploadFileName.set(file.name);
    this.documentService.uploadDocument(file).subscribe({
      next: () => {
        this.uploading.set(false);
        this.showNotification('success', this.i18n.instant('DOCUMENTS.UPLOAD_SUCCESS', { filename: file.name }));
        this.loadDocuments();
        input.value = '';
      },
      error: () => {
        this.uploading.set(false);
        this.showNotification('error', this.i18n.instant('DOCUMENTS.UPLOAD_ERROR', { filename: file.name }));
        input.value = '';
      }
    });
  }

  deleteDoc(doc: Document) {
    this.confirmDelete.set(doc);
  }

  confirmDeleteAction() {
    const doc = this.confirmDelete();
    if (!doc) return;
    this.documentService.deleteDocument(doc.id).subscribe({
      next: () => {
        this.confirmDelete.set(null);
        this.showNotification('success', this.i18n.instant('DOCUMENTS.DELETE_SUCCESS', { filename: doc.filename }));
        this.loadDocuments();
      },
      error: () => {
        this.confirmDelete.set(null);
        this.showNotification('error', this.i18n.instant('DOCUMENTS.DELETE_ERROR'));
      }
    });
  }

  countByStatus(status: string): number {
    return this.documents().filter(d => d.status === status).length;
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'INDEXED': return 'badge-success';
      case 'PENDING': return 'badge-warning';
      case 'PROCESSING': return 'badge-info';
      case 'FAILED': return 'badge-error';
      case 'ARCHIVED': return 'badge-neutral';
      default: return 'badge-neutral';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'INDEXED': return 'Indicizzato';
      case 'PENDING': return 'In attesa';
      case 'PROCESSING': return 'Elaborazione';
      case 'FAILED': return 'Fallito';
      case 'ARCHIVED': return 'Archiviato';
      default: return status;
    }
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const locale = this.i18n.currentLang() === 'it' ? 'it-IT' : 'en-US';
    return d.toLocaleDateString(locale, { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  private showNotification(type: string, message: string) {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 4000);
  }
}
