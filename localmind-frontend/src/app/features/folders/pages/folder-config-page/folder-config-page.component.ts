import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FolderService } from '../../services/folder.service';
import { MonitoredFolder, CreateFolderRequest } from '../../models/folder.model';

@Component({
  selector: 'app-folder-config-page',
  imports: [FormsModule],
  template: `
    <div class="folders-page">
      <div class="page-header">
        <div>
          <h1>Cartelle Monitorate</h1>
          <p class="subtitle">Configura le cartelle locali da indicizzare per la RAG</p>
        </div>
        <button class="btn btn-primary" (click)="showForm = !showForm">
          {{ showForm ? 'Annulla' : '+ Aggiungi Cartella' }}
        </button>
      </div>

      @if (showForm) {
        <div class="add-form card">
          <h3>Nuova Cartella</h3>
          <div class="form-grid">
            <div class="form-group full-width">
              <label>Percorso *</label>
              <input type="text" [(ngModel)]="newFolder.path" placeholder="Es: /home/user/documenti">
            </div>
            <div class="form-group">
              <label>Pattern File (glob)</label>
              <input type="text" [(ngModel)]="newFolder.pattern" placeholder="Es: *.pdf,*.md,*.txt">
            </div>
            <div class="form-group checkbox-wrap">
              <label>
                <input type="checkbox" [(ngModel)]="newFolder.recursive">
                Includi sottocartelle
              </label>
            </div>
          </div>
          <button class="btn btn-primary" (click)="addFolder()" [disabled]="!newFolder.path.trim()">
            Aggiungi
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
          Caricamento cartelle...
        </div>
      } @else if (folders().length === 0) {
        <div class="empty-state card">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/>
          </svg>
          <h3>Nessuna cartella configurata</h3>
          <p>Aggiungi una cartella locale per iniziare l'indicizzazione automatica.</p>
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
                  <span class="meta-item">{{ folder.documentCount }} documenti</span>
                  @if (folder.pattern) {
                    <span class="meta-item mono">{{ folder.pattern }}</span>
                  }
                  <span class="meta-item">{{ folder.recursive ? 'Ricorsivo' : 'Solo root' }}</span>
                </div>
                @if (folder.lastSyncAt) {
                  <div class="folder-sync">Ultimo sync: {{ formatDate(folder.lastSyncAt) }}</div>
                }
              </div>
              <div class="folder-actions">
                <button class="btn-action" title="Sincronizza" (click)="syncFolder(folder.id)"
                        [disabled]="folder.status === 'SYNCING'">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M23 4v6h-6M1 20v-6h6"/>
                    <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
                  </svg>
                </button>
                <button class="btn-action danger" title="Rimuovi" (click)="removeFolder(folder.id)">
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
    .subtitle { color: #888; font-size: 0.85rem; margin: 0.15rem 0 0 0; }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: #0f3460; color: white; }
    .btn-primary:hover:not(:disabled) { background: #1a4a7a; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .card { background: white; border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); padding: 1.25rem; }
    .add-form { margin-bottom: 1.5rem; }
    .add-form h3 { margin: 0 0 1rem 0; font-size: 1rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8rem; font-weight: 500; color: #333; }
    .form-group input[type="text"] { padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; font-size: 0.85rem; }
    .full-width { grid-column: 1 / -1; }
    .checkbox-wrap { justify-content: end; }
    .checkbox-wrap label { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-size: 0.85rem; }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: #d4edda; color: #155724; }
    .notification.error { background: #f8d7da; color: #721c24; }

    .loading-state { display: flex; align-items: center; gap: 0.5rem; justify-content: center; padding: 3rem; color: #999; }
    .spinner {
      display: inline-block; width: 16px; height: 16px;
      border: 2px solid #ddd; border-top-color: #0f3460; border-radius: 50%;
      animation: spin 0.6s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .empty-state { text-align: center; padding: 3rem; }
    .empty-state svg { margin-bottom: 1rem; }
    .empty-state h3 { color: #666; margin-bottom: 0.25rem; }
    .empty-state p { font-size: 0.85rem; color: #999; }

    .folder-list { display: flex; flex-direction: column; gap: 0.5rem; }
    .folder-card {
      background: white; padding: 1rem 1.25rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06);
      display: flex; align-items: center; gap: 1rem;
    }
    .folder-icon { color: #f39c12; flex-shrink: 0; }
    .folder-info { flex: 1; min-width: 0; }
    .folder-path { font-weight: 600; font-size: 0.9rem; margin-bottom: 0.3rem; font-family: monospace; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .folder-meta { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 0.15rem; }
    .badge { font-size: 0.65rem; padding: 0.1rem 0.45rem; border-radius: 10px; font-weight: 600; text-transform: uppercase; }
    .badge-success { background: #d4edda; color: #155724; }
    .badge-warning { background: #fff3cd; color: #856404; }
    .badge-error { background: #f8d7da; color: #721c24; }
    .badge-info { background: #d1ecf1; color: #0c5460; }
    .meta-item { font-size: 0.75rem; color: #999; }
    .mono { font-family: monospace; }
    .folder-sync { font-size: 0.7rem; color: #bbb; }
    .folder-actions { display: flex; gap: 0.35rem; }
    .btn-action {
      padding: 0.4rem; background: none; border: 1px solid #ddd;
      border-radius: 6px; cursor: pointer; color: #666; transition: all 0.15s;
    }
    .btn-action:hover { background: #f5f5f5; color: #333; }
    .btn-action:disabled { opacity: 0.4; cursor: not-allowed; }
    .btn-action.danger { border-color: #fcc; color: #e74c3c; }
    .btn-action.danger:hover { background: #fef2f2; }
  `]
})
export class FolderConfigPageComponent implements OnInit {
  private folderService = inject(FolderService);

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
        this.showNotify('success', 'Cartella aggiunta con successo');
        this.loadFolders();
      },
      error: () => this.showNotify('error', 'Errore nell\'aggiunta della cartella')
    });
  }

  removeFolder(id: string) {
    this.folderService.removeFolder(id).subscribe({
      next: () => { this.showNotify('success', 'Cartella rimossa'); this.loadFolders(); },
      error: () => this.showNotify('error', 'Errore nella rimozione')
    });
  }

  syncFolder(id: string) {
    this.folderService.syncFolder(id).subscribe({
      next: () => { this.showNotify('success', 'Sincronizzazione avviata'); this.loadFolders(); },
      error: () => this.showNotify('error', 'Errore nella sincronizzazione')
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
    return new Date(dateStr).toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  private showNotify(type: string, message: string) {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 3000);
  }
}
