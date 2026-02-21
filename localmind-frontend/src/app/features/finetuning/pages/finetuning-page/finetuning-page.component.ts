import { Component, OnInit, inject, signal } from '@angular/core';
import { FineTuningService } from '../../services/finetuning.service';
import { FineTuningJob, TrainingDataset } from '../../models/finetuning.model';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { LoadingSkeletonComponent } from '../../../../shared/components/loading-skeleton/loading-skeleton.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-finetuning-page',
  standalone: true,
  imports: [TranslatePipe, LoadingSkeletonComponent, FormsModule],
  template: `
    <div class="finetuning-page">
      <div class="header">
        <div>
          <h2>{{ 'FINETUNING.TITLE' | translate }}</h2>
          <p class="subtitle">{{ 'FINETUNING.SUBTITLE' | translate }}</p>
        </div>
      </div>

      @if (notification()) {
        <div class="notification" [class]="notification()!.type">
          {{ notification()!.message }}
        </div>
      }

      <div class="tabs">
        <button class="tab" [class.active]="activeTab() === 'datasets'" (click)="activeTab.set('datasets')">
          {{ 'FINETUNING.TAB_DATASETS' | translate }}
        </button>
        <button class="tab" [class.active]="activeTab() === 'jobs'" (click)="activeTab.set('jobs')">
          {{ 'FINETUNING.TAB_JOBS' | translate }}
        </button>
      </div>

      @if (activeTab() === 'datasets') {
        <div class="section">
          <div class="section-header">
            <h3>{{ 'FINETUNING.DATASETS_TITLE' | translate }}</h3>
            <button class="btn btn-primary" (click)="showDatasetForm.set(!showDatasetForm())">
              {{ 'FINETUNING.CREATE_DATASET' | translate }}
            </button>
          </div>

          @if (showDatasetForm()) {
            <div class="form-card">
              <div class="form-group">
                <label>{{ 'FINETUNING.DATASET_NAME' | translate }} *</label>
                <input type="text" [(ngModel)]="datasetName" [placeholder]="'FINETUNING.DATASET_NAME_PLACEHOLDER' | translate">
              </div>
              <div class="form-group">
                <label>{{ 'FINETUNING.DOCUMENT_IDS' | translate }} *</label>
                <textarea [(ngModel)]="documentIdsText" rows="3"
                          [placeholder]="'FINETUNING.DOCUMENT_IDS_PLACEHOLDER' | translate"></textarea>
              </div>
              <div class="form-actions">
                <button class="btn btn-primary" (click)="createDataset()"
                        [disabled]="!datasetName || !documentIdsText || creatingDataset()">
                  @if (creatingDataset()) { <span class="spinner-xs"></span> }
                  {{ 'COMMON.SAVE' | translate }}
                </button>
                <button class="btn" (click)="showDatasetForm.set(false)">
                  {{ 'COMMON.CANCEL' | translate }}
                </button>
              </div>
            </div>
          }

          @if (loadingDatasets()) {
            <app-loading-skeleton [count]="3" />
          } @else if (datasets().length === 0) {
            <div class="empty-state">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
                <path d="M4 7V4a2 2 0 012-2h8.5L20 7.5V20a2 2 0 01-2 2H6a2 2 0 01-2-2v-3"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              <h3>{{ 'FINETUNING.DATASETS_EMPTY' | translate }}</h3>
              <p>{{ 'FINETUNING.DATASETS_EMPTY_HINT' | translate }}</p>
            </div>
          } @else {
            <div class="card-list">
              @for (dataset of datasets(); track dataset.id) {
                <div class="card">
                  <div class="card-info">
                    <div class="card-header">
                      <h4>{{ dataset.name }}</h4>
                      <span class="badge format">{{ dataset.format }}</span>
                    </div>
                    <div class="meta">
                      <span class="meta-item">
                        <strong>{{ 'FINETUNING.SAMPLES' | translate }}:</strong> {{ dataset.sampleCount }}
                      </span>
                      <span class="meta-item">
                        <strong>{{ 'FINETUNING.DOCUMENTS' | translate }}:</strong> {{ dataset.documentIds.length }}
                      </span>
                      <span class="meta-item">
                        <strong>{{ 'FINETUNING.CREATED' | translate }}:</strong> {{ dataset.createdAt | date:'short' }}
                      </span>
                    </div>
                  </div>
                </div>
              }
            </div>
          }
        </div>
      }

      @if (activeTab() === 'jobs') {
        <div class="section">
          <div class="section-header">
            <h3>{{ 'FINETUNING.JOBS_TITLE' | translate }}</h3>
            <button class="btn btn-primary" (click)="showJobForm.set(!showJobForm())">
              {{ 'FINETUNING.START_TRAINING' | translate }}
            </button>
          </div>

          @if (showJobForm()) {
            <div class="form-card">
              <div class="form-group">
                <label>{{ 'FINETUNING.DATASET' | translate }} *</label>
                <select [(ngModel)]="selectedDatasetId">
                  <option value="">{{ 'FINETUNING.SELECT_DATASET' | translate }}</option>
                  @for (ds of datasets(); track ds.id) {
                    <option [value]="ds.id">{{ ds.name }}</option>
                  }
                </select>
              </div>
              <div class="form-group">
                <label>{{ 'FINETUNING.BASE_MODEL' | translate }} *</label>
                <input type="text" [(ngModel)]="baseModel" placeholder="llama3.2">
              </div>
              <div class="form-group">
                <label>{{ 'FINETUNING.TECHNIQUE' | translate }} *</label>
                <select [(ngModel)]="technique">
                  <option value="LORA">LoRA</option>
                  <option value="QLORA">QLoRA</option>
                  <option value="FULL">Full</option>
                </select>
              </div>
              <div class="form-actions">
                <button class="btn btn-primary" (click)="startTraining()"
                        [disabled]="!selectedDatasetId || !baseModel || startingTraining()">
                  @if (startingTraining()) { <span class="spinner-xs"></span> }
                  {{ 'FINETUNING.START_TRAINING' | translate }}
                </button>
                <button class="btn" (click)="showJobForm.set(false)">
                  {{ 'COMMON.CANCEL' | translate }}
                </button>
              </div>
            </div>
          }

          @if (loadingJobs()) {
            <app-loading-skeleton [count]="3" />
          } @else if (jobs().length === 0) {
            <div class="empty-state">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
                <path d="M12 2a10 10 0 100 20 10 10 0 000-20z"/>
                <path d="M12 6v6l4 2"/>
              </svg>
              <h3>{{ 'FINETUNING.JOBS_EMPTY' | translate }}</h3>
              <p>{{ 'FINETUNING.JOBS_EMPTY_HINT' | translate }}</p>
            </div>
          } @else {
            <div class="card-list">
              @for (job of jobs(); track job.id) {
                <div class="card" [class]="'status-' + job.status.toLowerCase()">
                  <div class="card-info">
                    <div class="card-header">
                      <h4>{{ job.baseModel }}</h4>
                      <span class="badge" [class]="job.status.toLowerCase()">
                        {{ 'FINETUNING.STATUS_' + job.status | translate }}
                      </span>
                    </div>
                    <div class="meta">
                      <span class="meta-item">
                        <strong>{{ 'FINETUNING.TECHNIQUE' | translate }}:</strong> {{ job.technique }}
                      </span>
                      <span class="meta-item">
                        <strong>{{ 'FINETUNING.PROGRESS' | translate }}:</strong> {{ job.progress }}%
                      </span>
                      @if (job.outputModelName) {
                        <span class="meta-item">
                          <strong>{{ 'FINETUNING.OUTPUT_MODEL' | translate }}:</strong> {{ job.outputModelName }}
                        </span>
                      }
                      <span class="meta-item">
                        <strong>{{ 'FINETUNING.CREATED' | translate }}:</strong> {{ job.createdAt | date:'short' }}
                      </span>
                    </div>
                    @if (job.status === 'TRAINING' || job.status === 'PREPARING') {
                      <div class="progress-bar">
                        <div class="progress-fill" [style.width.%]="job.progress"></div>
                      </div>
                    }
                    @if (job.error) {
                      <div class="error-text">{{ job.error }}</div>
                    }
                  </div>
                  <div class="card-actions">
                    @if (job.status === 'TRAINING' || job.status === 'PREPARING') {
                      <button class="btn-sm" (click)="refreshJob(job.id)" [disabled]="actionId() === job.id">
                        @if (actionId() === job.id) { <span class="spinner-xs"></span> }
                        {{ 'COMMON.REFRESH' | translate }}
                      </button>
                    }
                    @if (job.status === 'COMPLETED') {
                      <button class="btn-sm btn-deploy" (click)="deployModel(job.id)" [disabled]="actionId() === job.id">
                        @if (actionId() === job.id) { <span class="spinner-xs"></span> }
                        {{ 'FINETUNING.DEPLOY' | translate }}
                      </button>
                    }
                  </div>
                </div>
              }
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

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: var(--color-success-bg); color: var(--color-success-text); }
    .notification.error { background: var(--color-error-bg); color: var(--color-error-text); }

    .tabs { display: flex; gap: 0; margin-bottom: 1.5rem; border-bottom: 2px solid var(--color-border); }
    .tab {
      padding: 0.6rem 1.25rem; border: none; background: none; cursor: pointer;
      font-size: 0.9rem; font-weight: 500; color: var(--color-text-secondary);
      border-bottom: 2px solid transparent; margin-bottom: -2px; font-family: inherit;
    }
    .tab:hover { color: var(--color-text); }
    .tab.active { color: var(--color-primary); border-bottom-color: var(--color-primary); }

    .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .section-header h3 { margin: 0; color: var(--color-text); }

    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: 1px solid var(--color-border); border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; background: var(--color-card-bg); color: var(--color-text); }
    .btn-primary { background: var(--color-primary); color: white; border-color: var(--color-primary); }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-light); }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }

    .form-card {
      background: var(--color-card-bg); padding: 1.25rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 1rem;
    }
    .form-group { margin-bottom: 0.75rem; }
    .form-group label { display: block; font-size: 0.8rem; font-weight: 600; margin-bottom: 0.3rem; color: var(--color-text-secondary); }
    .form-group input, .form-group select, .form-group textarea {
      width: 100%; padding: 0.5rem 0.75rem; border: 1px solid var(--color-border); border-radius: 6px;
      font-size: 0.85rem; font-family: inherit; background: var(--color-bg); color: var(--color-text);
      box-sizing: border-box;
    }
    .form-group textarea { resize: vertical; }
    .form-actions { display: flex; gap: 0.5rem; margin-top: 1rem; }

    .card-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .card {
      background: var(--color-card-bg); padding: 1rem 1.25rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); display: flex;
      justify-content: space-between; align-items: center;
      border-left: 4px solid var(--color-border);
    }
    .card.status-completed { border-left-color: var(--color-success); }
    .card.status-training, .card.status-preparing { border-left-color: var(--color-info); }
    .card.status-failed { border-left-color: var(--color-error); }
    .card.status-deployed { border-left-color: #9b59b6; }
    .card.status-pending { border-left-color: var(--color-warning); }

    .card-header { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.25rem; }
    .card-header h4 { margin: 0; font-size: 1rem; }
    .badge {
      font-size: 0.65rem; padding: 0.15rem 0.5rem; border-radius: 10px;
      text-transform: uppercase; font-weight: 600;
    }
    .badge.completed { background: var(--color-success-bg); color: var(--color-success-text); }
    .badge.training, .badge.preparing { background: var(--color-info-bg); color: var(--color-info-text); }
    .badge.failed { background: var(--color-error-bg); color: var(--color-error-text); }
    .badge.deployed { background: #f0e6f6; color: #9b59b6; }
    .badge.pending { background: var(--color-warning-bg); color: var(--color-warning-text); }
    .badge.format { background: var(--color-border); color: var(--color-text-secondary); }

    .meta { display: flex; align-items: center; gap: 1rem; font-size: 0.8rem; color: var(--color-text-muted); flex-wrap: wrap; }
    .meta-item strong { font-weight: 600; color: var(--color-text-secondary); }

    .progress-bar { width: 100%; height: 6px; background: var(--color-border); border-radius: 3px; margin-top: 0.5rem; overflow: hidden; }
    .progress-fill { height: 100%; background: linear-gradient(90deg, var(--color-primary), var(--color-info)); border-radius: 3px; transition: width 0.3s ease; }

    .error-text { color: var(--color-error); font-size: 0.8rem; margin-top: 0.5rem; }

    .card-actions { display: flex; gap: 0.5rem; flex-shrink: 0; }
    .btn-sm {
      padding: 0.35rem 0.75rem; border: 1px solid var(--color-border); background: var(--color-card-bg);
      border-radius: 4px; cursor: pointer; font-size: 0.8rem; font-family: inherit;
      display: inline-flex; align-items: center; gap: 0.3rem; color: var(--color-text);
    }
    .btn-sm:hover:not(:disabled) { background: var(--color-bg); }
    .btn-sm:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-deploy { color: #9b59b6; border-color: #9b59b6; }
    .btn-deploy:hover:not(:disabled) { background: #f0e6f6; }

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
export class FineTuningPageComponent implements OnInit {
  private ftService = inject(FineTuningService);
  private i18n = inject(TranslationService);

  activeTab = signal<'datasets' | 'jobs'>('datasets');
  datasets = signal<TrainingDataset[]>([]);
  jobs = signal<FineTuningJob[]>([]);
  loadingDatasets = signal(false);
  loadingJobs = signal(false);
  showDatasetForm = signal(false);
  showJobForm = signal(false);
  creatingDataset = signal(false);
  startingTraining = signal(false);
  actionId = signal<string | null>(null);
  notification = signal<{ type: string; message: string } | null>(null);

  datasetName = '';
  documentIdsText = '';
  selectedDatasetId = '';
  baseModel = '';
  technique = 'LORA';

  ngOnInit(): void {
    this.loadDatasets();
    this.loadJobs();
  }

  loadDatasets(): void {
    this.loadingDatasets.set(true);
    this.ftService.listDatasets().subscribe({
      next: (datasets) => { this.datasets.set(datasets); this.loadingDatasets.set(false); },
      error: () => { this.loadingDatasets.set(false); this.showNotify('error', this.i18n.instant('COMMON.ERROR')); }
    });
  }

  loadJobs(): void {
    this.loadingJobs.set(true);
    this.ftService.listJobs().subscribe({
      next: (jobs) => { this.jobs.set(jobs); this.loadingJobs.set(false); },
      error: () => { this.loadingJobs.set(false); this.showNotify('error', this.i18n.instant('COMMON.ERROR')); }
    });
  }

  createDataset(): void {
    const docIds = this.documentIdsText.split(',').map(id => id.trim()).filter(id => id);
    if (!this.datasetName || docIds.length === 0) return;

    this.creatingDataset.set(true);
    this.ftService.createDataset({ name: this.datasetName, documentIds: docIds }).subscribe({
      next: () => {
        this.creatingDataset.set(false);
        this.showDatasetForm.set(false);
        this.datasetName = '';
        this.documentIdsText = '';
        this.showNotify('success', this.i18n.instant('FINETUNING.DATASET_CREATED'));
        this.loadDatasets();
      },
      error: () => {
        this.creatingDataset.set(false);
        this.showNotify('error', this.i18n.instant('FINETUNING.DATASET_ERROR'));
      }
    });
  }

  startTraining(): void {
    if (!this.selectedDatasetId || !this.baseModel) return;

    this.startingTraining.set(true);
    this.ftService.startTraining({
      datasetId: this.selectedDatasetId,
      baseModel: this.baseModel,
      technique: this.technique
    }).subscribe({
      next: () => {
        this.startingTraining.set(false);
        this.showJobForm.set(false);
        this.selectedDatasetId = '';
        this.baseModel = '';
        this.technique = 'LORA';
        this.showNotify('success', this.i18n.instant('FINETUNING.TRAINING_STARTED'));
        this.loadJobs();
      },
      error: () => {
        this.startingTraining.set(false);
        this.showNotify('error', this.i18n.instant('FINETUNING.TRAINING_ERROR'));
      }
    });
  }

  refreshJob(jobId: string): void {
    this.actionId.set(jobId);
    this.ftService.getJob(jobId).subscribe({
      next: (job) => {
        this.actionId.set(null);
        const current = this.jobs();
        this.jobs.set(current.map(j => j.id === jobId ? job : j));
      },
      error: () => {
        this.actionId.set(null);
        this.showNotify('error', this.i18n.instant('COMMON.ERROR'));
      }
    });
  }

  deployModel(jobId: string): void {
    this.actionId.set(jobId);
    this.ftService.deployModel(jobId).subscribe({
      next: () => {
        this.actionId.set(null);
        this.showNotify('success', this.i18n.instant('FINETUNING.DEPLOY_SUCCESS'));
        this.loadJobs();
      },
      error: () => {
        this.actionId.set(null);
        this.showNotify('error', this.i18n.instant('FINETUNING.DEPLOY_ERROR'));
      }
    });
  }

  private showNotify(type: string, message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 4000);
  }
}
