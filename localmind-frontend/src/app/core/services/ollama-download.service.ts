import { Injectable, signal, computed } from '@angular/core';
import { environment } from '../../../environments/environment';

export interface PullProgress {
  status: string;
  digest: string | null;
  total: number;
  completed: number;
  done: boolean;
  error: boolean;
  errorMessage: string | null;
  percentage: number;
}

@Injectable({ providedIn: 'root' })
export class OllamaDownloadService {
  private _isActive = signal(false);
  private _modelName = signal('');
  private _progress = signal<PullProgress | null>(null);
  private _completed = signal(false);
  private eventSource: EventSource | null = null;

  readonly isDownloading = this._isActive.asReadonly();
  readonly downloadModelName = this._modelName.asReadonly();
  readonly downloadProgress = this._progress.asReadonly();
  readonly downloadCompleted = this._completed.asReadonly();

  readonly downloadPercentage = computed(() => {
    const p = this._progress();
    if (!p || p.total <= 0) return -1;
    return Math.round((p.completed * 100) / p.total);
  });

  readonly showBar = computed(() => this._isActive() || this._completed());

  startPull(baseUrl: string, modelName: string): void {
    this.close();
    this._modelName.set(modelName);
    this._isActive.set(true);
    this._completed.set(false);
    this._progress.set(null);

    const params = new URLSearchParams({ baseUrl, modelName });
    const url = `${environment.apiUrl}/settings/providers/ollama/models/pull/stream?${params}`;

    this.eventSource = new EventSource(url);

    this.eventSource.addEventListener('progress', (event: MessageEvent) => {
      const progress: PullProgress = JSON.parse(event.data);
      this._progress.set(progress);

      if (progress.done || progress.error) {
        this._isActive.set(false);
        this._completed.set(true);
        this.close();
      }
    });

    this.eventSource.onerror = () => {
      if (this.eventSource?.readyState === EventSource.CLOSED) {
        if (!this._completed()) {
          this._progress.set({
            status: 'error', digest: null, total: 0, completed: 0,
            done: true, error: true, errorMessage: 'Connection lost', percentage: -1
          });
          this._isActive.set(false);
          this._completed.set(true);
        }
        this.close();
      }
    };
  }

  dismiss(): void {
    this.close();
    this._isActive.set(false);
    this._completed.set(false);
    this._progress.set(null);
    this._modelName.set('');
  }

  private close(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }
}
