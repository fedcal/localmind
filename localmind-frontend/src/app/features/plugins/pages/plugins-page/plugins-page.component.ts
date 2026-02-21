import { Component, OnInit, inject, signal } from '@angular/core';
import { PluginService } from '../../services/plugin.service';
import { PluginInfo } from '../../models/plugin.model';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { LoadingSkeletonComponent } from '../../../../shared/components/loading-skeleton/loading-skeleton.component';

@Component({
  selector: 'app-plugins-page',
  standalone: true,
  imports: [TranslatePipe, LoadingSkeletonComponent],
  template: `
    <div class="plugins-page">
      <div class="header">
        <div>
          <h2>{{ 'PLUGINS.TITLE' | translate }}</h2>
          <p class="subtitle">{{ 'PLUGINS.SUBTITLE' | translate }}</p>
        </div>
        <button class="btn btn-primary" (click)="fileInput.click()">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
          {{ 'PLUGINS.UPLOAD' | translate }}
        </button>
        <input #fileInput type="file" accept=".jar" hidden (change)="onFileSelected($event)">
      </div>

      @if (notification()) {
        <div class="notification" [class]="notification()!.type">
          {{ notification()!.message }}
        </div>
      }

      @if (loading()) {
        <app-loading-skeleton [count]="3" />
      } @else if (plugins().length === 0) {
        <div class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <path d="M14.7 6.3a1 1 0 000 1.4l1.6 1.6a1 1 0 001.4 0l3.77-3.77a6 6 0 01-7.94 7.94l-6.91 6.91a2.12 2.12 0 01-3-3l6.91-6.91a6 6 0 017.94-7.94l-3.76 3.76z"/>
          </svg>
          <h3>{{ 'PLUGINS.EMPTY' | translate }}</h3>
          <p>{{ 'PLUGINS.EMPTY_HINT' | translate }}</p>
        </div>
      } @else {
        <div class="plugin-list">
          @for (plugin of plugins(); track plugin.id) {
            <div class="plugin-card" [class]="'state-' + plugin.state.toLowerCase()">
              <div class="plugin-info">
                <div class="plugin-header">
                  <h3>{{ plugin.name }}</h3>
                  <span class="state-badge" [class]="plugin.state.toLowerCase()">
                    {{ 'PLUGINS.STATUS_' + plugin.state | translate }}
                  </span>
                </div>
                @if (plugin.description) {
                  <p class="description">{{ plugin.description }}</p>
                }
                <div class="meta">
                  <span class="meta-item">
                    <strong>{{ 'PLUGINS.VERSION' | translate }}:</strong> {{ plugin.version }}
                  </span>
                  @if (plugin.author) {
                    <span class="meta-item">
                      <strong>{{ 'PLUGINS.AUTHOR' | translate }}:</strong> {{ plugin.author }}
                    </span>
                  }
                  @if (plugin.license) {
                    <span class="meta-item">
                      <strong>{{ 'PLUGINS.LICENSE' | translate }}:</strong> {{ plugin.license }}
                    </span>
                  }
                </div>
              </div>
              <div class="plugin-actions">
                @if (plugin.state === 'STARTED' || plugin.state === 'RESOLVED') {
                  <button class="btn-sm" (click)="disablePlugin(plugin.id)"
                          [disabled]="actionId() === plugin.id">
                    @if (actionId() === plugin.id && actionType() === 'disable') { <span class="spinner-xs"></span> }
                    {{ 'PLUGINS.DISABLE' | translate }}
                  </button>
                } @else {
                  <button class="btn-sm btn-enable" (click)="enablePlugin(plugin.id)"
                          [disabled]="actionId() === plugin.id">
                    @if (actionId() === plugin.id && actionType() === 'enable') { <span class="spinner-xs"></span> }
                    {{ 'PLUGINS.ENABLE' | translate }}
                  </button>
                }
                <button class="btn-sm btn-danger" (click)="deletePlugin(plugin.id)"
                        [disabled]="actionId() === plugin.id">
                  {{ 'PLUGINS.DELETE' | translate }}
                </button>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
    .header h2 { margin: 0; color: var(--color-text); }
    .subtitle { margin: 0.25rem 0 0; font-size: 0.85rem; color: var(--color-text-secondary); }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-light); }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: var(--color-success-bg); color: var(--color-success-text); }
    .notification.error { background: var(--color-error-bg); color: var(--color-error-text); }

    .plugin-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .plugin-card {
      background: var(--color-card-bg); padding: 1rem 1.25rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); display: flex;
      justify-content: space-between; align-items: center;
      border-left: 4px solid var(--color-border);
    }
    .plugin-card.state-started { border-left-color: var(--color-success); }
    .plugin-card.state-resolved { border-left-color: var(--color-info); }
    .plugin-card.state-stopped { border-left-color: var(--color-text-muted); }
    .plugin-card.state-disabled { border-left-color: var(--color-warning); }
    .plugin-card.state-created { border-left-color: var(--color-border); }
    .plugin-card.state-failed { border-left-color: var(--color-error); }

    .plugin-header { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.25rem; }
    .plugin-header h3 { margin: 0; font-size: 1rem; }
    .state-badge {
      font-size: 0.65rem; padding: 0.15rem 0.5rem; border-radius: 10px;
      text-transform: uppercase; font-weight: 600;
    }
    .state-badge.started { background: var(--color-success-bg); color: var(--color-success-text); }
    .state-badge.resolved { background: var(--color-info-bg); color: var(--color-info-text); }
    .state-badge.stopped { background: var(--color-border); color: var(--color-text-secondary); }
    .state-badge.disabled { background: var(--color-warning-bg); color: var(--color-warning-text); }
    .state-badge.created { background: var(--color-border); color: var(--color-text-secondary); }
    .state-badge.failed { background: var(--color-error-bg); color: var(--color-error-text); }

    .description { color: var(--color-text-secondary); font-size: 0.85rem; margin: 0.25rem 0; }
    .meta { display: flex; align-items: center; gap: 1rem; font-size: 0.8rem; color: var(--color-text-muted); flex-wrap: wrap; }
    .meta-item strong { font-weight: 600; color: var(--color-text-secondary); }

    .plugin-actions { display: flex; gap: 0.5rem; flex-shrink: 0; }
    .btn-sm {
      padding: 0.35rem 0.75rem; border: 1px solid var(--color-border); background: var(--color-card-bg);
      border-radius: 4px; cursor: pointer; font-size: 0.8rem; font-family: inherit;
      display: inline-flex; align-items: center; gap: 0.3rem; color: var(--color-text);
    }
    .btn-sm:hover:not(:disabled) { background: var(--color-bg); }
    .btn-sm:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-enable { color: var(--color-success); border-color: var(--color-success); }
    .btn-enable:hover:not(:disabled) { background: var(--color-success-bg); }
    .btn-danger { color: var(--color-error); border-color: var(--color-error); }
    .btn-danger:hover:not(:disabled) { background: var(--color-error-bg); }

    .empty-state {
      text-align: center; padding: 3rem; color: var(--color-text-secondary);
      background: var(--color-card-bg); border-radius: 8px;
    }
    .empty-state svg { margin-bottom: 1rem; }
    .empty-state h3 { margin-bottom: 0.25rem; }
    .empty-state p { font-size: 0.85rem; color: var(--color-text-muted); }

    .spinner-xs { display: inline-block; width: 10px; height: 10px; border: 2px solid var(--color-border); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class PluginsPageComponent implements OnInit {
  private pluginService = inject(PluginService);
  private i18n = inject(TranslationService);

  plugins = signal<PluginInfo[]>([]);
  loading = signal(false);
  uploading = signal(false);
  actionId = signal<string | null>(null);
  actionType = signal<string | null>(null);
  notification = signal<{ type: string; message: string } | null>(null);

  ngOnInit(): void {
    this.loadPlugins();
  }

  loadPlugins(): void {
    this.loading.set(true);
    this.pluginService.getPlugins().subscribe({
      next: (plugins) => { this.plugins.set(plugins); this.loading.set(false); },
      error: () => { this.loading.set(false); }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploading.set(true);
    this.pluginService.uploadPlugin(file).subscribe({
      next: () => {
        this.uploading.set(false);
        this.showNotify('success', this.i18n.instant('PLUGINS.UPLOAD_SUCCESS'));
        this.loadPlugins();
      },
      error: () => {
        this.uploading.set(false);
        this.showNotify('error', this.i18n.instant('PLUGINS.UPLOAD_ERROR'));
      }
    });
    input.value = '';
  }

  enablePlugin(id: string): void {
    this.actionId.set(id);
    this.actionType.set('enable');
    this.pluginService.enablePlugin(id).subscribe({
      next: () => {
        this.actionId.set(null);
        this.showNotify('success', this.i18n.instant('PLUGINS.ENABLE_SUCCESS'));
        this.loadPlugins();
      },
      error: () => {
        this.actionId.set(null);
        this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
      }
    });
  }

  disablePlugin(id: string): void {
    this.actionId.set(id);
    this.actionType.set('disable');
    this.pluginService.disablePlugin(id).subscribe({
      next: () => {
        this.actionId.set(null);
        this.showNotify('success', this.i18n.instant('PLUGINS.DISABLE_SUCCESS'));
        this.loadPlugins();
      },
      error: () => {
        this.actionId.set(null);
        this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
      }
    });
  }

  deletePlugin(id: string): void {
    if (!confirm(this.i18n.instant('PLUGINS.DELETE_CONFIRM'))) return;
    this.actionId.set(id);
    this.actionType.set('delete');
    this.pluginService.deletePlugin(id).subscribe({
      next: () => {
        this.actionId.set(null);
        this.showNotify('success', this.i18n.instant('PLUGINS.DELETE_SUCCESS'));
        this.loadPlugins();
      },
      error: () => {
        this.actionId.set(null);
        this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
      }
    });
  }

  private showNotify(type: string, message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 4000);
  }
}
