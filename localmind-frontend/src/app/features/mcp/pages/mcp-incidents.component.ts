import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { McpIncidentService } from '../services/mcp-incident.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../core/i18n/translation.service';

@Component({
  selector: 'app-mcp-incidents',
  standalone: true,
  imports: [FormsModule, TranslatePipe],
  template: `
    <div class="incidents-page">
      <div class="header">
        <h2>{{ 'MCP_INCIDENTS.TITLE' | translate }}</h2>
        <button class="btn btn-primary" (click)="showCreateForm.set(!showCreateForm())">
          {{ showCreateForm() ? ('COMMON.CANCEL' | translate) : ('MCP_INCIDENTS.OPEN' | translate) }}
        </button>
      </div>

      @if (showCreateForm()) {
        <div class="form-panel">
          <h3>{{ 'MCP_INCIDENTS.OPEN' | translate }}</h3>
          <div class="form-grid">
            <div class="form-group">
              <label>{{ 'MCP_INCIDENTS.TITLE_FIELD' | translate }}</label>
              <input type="text" [(ngModel)]="newIncident.title" [placeholder]="'MCP_INCIDENTS.TITLE_FIELD' | translate">
            </div>
            <div class="form-group">
              <label>{{ 'MCP_INCIDENTS.SEVERITY' | translate }}</label>
              <select [(ngModel)]="newIncident.severity">
                <option value="critical">{{ 'MCP_INCIDENTS.SEV_CRITICAL' | translate }}</option>
                <option value="high">{{ 'MCP_INCIDENTS.SEV_HIGH' | translate }}</option>
                <option value="medium">{{ 'MCP_INCIDENTS.SEV_MEDIUM' | translate }}</option>
                <option value="low">{{ 'MCP_INCIDENTS.SEV_LOW' | translate }}</option>
              </select>
            </div>
            <div class="form-group full-width">
              <label>{{ 'MCP_INCIDENTS.DESCRIPTION' | translate }}</label>
              <textarea [(ngModel)]="newIncident.description" rows="3" [placeholder]="'MCP_INCIDENTS.DESCRIPTION' | translate"></textarea>
            </div>
          </div>
          <button class="btn btn-primary" (click)="openIncident()" [disabled]="!newIncident.title">
            {{ 'MCP_INCIDENTS.OPEN' | translate }}
          </button>
        </div>
      }

      @if (notification()) {
        <div class="notification" [class]="notification()!.type">
          {{ notification()!.message }}
        </div>
      }

      <!-- Filters -->
      <div class="filter-bar">
        <div class="filter-group">
          <label>{{ 'MCP_INCIDENTS.FILTER_STATUS' | translate }}</label>
          <select [(ngModel)]="statusFilter" (ngModelChange)="loadIncidents()">
            <option value="">{{ 'MCP_INCIDENTS.FILTER_ALL' | translate }}</option>
            <option value="open">{{ 'MCP_INCIDENTS.STATUS_OPEN' | translate }}</option>
            <option value="investigating">{{ 'MCP_INCIDENTS.STATUS_INVESTIGATING' | translate }}</option>
            <option value="mitigating">{{ 'MCP_INCIDENTS.STATUS_MITIGATING' | translate }}</option>
            <option value="resolved">{{ 'MCP_INCIDENTS.STATUS_RESOLVED' | translate }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label>{{ 'MCP_INCIDENTS.FILTER_SEVERITY' | translate }}</label>
          <select [(ngModel)]="severityFilter" (ngModelChange)="loadIncidents()">
            <option value="">{{ 'MCP_INCIDENTS.FILTER_ALL' | translate }}</option>
            <option value="critical">{{ 'MCP_INCIDENTS.SEV_CRITICAL' | translate }}</option>
            <option value="high">{{ 'MCP_INCIDENTS.SEV_HIGH' | translate }}</option>
            <option value="medium">{{ 'MCP_INCIDENTS.SEV_MEDIUM' | translate }}</option>
            <option value="low">{{ 'MCP_INCIDENTS.SEV_LOW' | translate }}</option>
          </select>
        </div>
      </div>

      <!-- Incident List -->
      @if (loading()) {
        <div class="loading-state">
          <span class="spinner"></span>
          {{ 'COMMON.LOADING' | translate }}
        </div>
      } @else if (incidents().length === 0) {
        <p class="empty">{{ 'MCP_INCIDENTS.EMPTY' | translate }}</p>
      } @else {
        <div class="incident-list">
          @for (incident of incidents(); track incident.id || $index) {
            <div class="incident-card" [class.selected]="selectedIncident()?.id === incident.id"
                 (click)="selectIncident(incident)">
              <div class="incident-header">
                <span class="incident-title">{{ incident.title }}</span>
                <div class="incident-badges">
                  <span class="severity-badge" [class]="'sev-' + (incident.severity || 'medium')">
                    {{ getSeverityLabel(incident.severity) }}
                  </span>
                  <span class="status-badge" [class]="'status-' + (incident.status || 'open')">
                    {{ getStatusLabel(incident.status) }}
                  </span>
                </div>
              </div>
              @if (incident.description) {
                <p class="incident-desc">{{ incident.description }}</p>
              }
              @if (incident.createdAt) {
                <span class="incident-date">{{ incident.createdAt }}</span>
              }
            </div>
          }
        </div>
      }

      <!-- Detail Panel -->
      @if (selectedIncident()) {
        <div class="detail-panel">
          <div class="detail-header">
            <h3>{{ selectedIncident()!.title }}</h3>
            <button class="btn btn-secondary btn-sm" (click)="selectedIncident.set(null)">
              {{ 'COMMON.CLOSE' | translate }}
            </button>
          </div>

          <div class="detail-meta">
            <span class="severity-badge" [class]="'sev-' + (selectedIncident()!.severity || 'medium')">
              {{ getSeverityLabel(selectedIncident()!.severity) }}
            </span>
            <span class="status-badge" [class]="'status-' + (selectedIncident()!.status || 'open')">
              {{ getStatusLabel(selectedIncident()!.status) }}
            </span>
          </div>

          @if (selectedIncident()!.description) {
            <p class="detail-desc">{{ selectedIncident()!.description }}</p>
          }

          <!-- Timeline -->
          @if (selectedIncident()!.timeline && selectedIncident()!.timeline.length > 0) {
            <div class="timeline-section">
              <h4>{{ 'MCP_INCIDENTS.TIMELINE' | translate }}</h4>
              <div class="timeline">
                @for (entry of selectedIncident()!.timeline; track $index) {
                  <div class="timeline-entry">
                    <span class="timeline-time">{{ entry.timestamp || entry.time }}</span>
                    <span class="timeline-text">{{ entry.message || entry.description }}</span>
                  </div>
                }
              </div>
            </div>
          }

          <!-- Add Timeline -->
          <div class="add-timeline">
            <h4>{{ 'MCP_INCIDENTS.ADD_TIMELINE' | translate }}</h4>
            <div class="timeline-form">
              <textarea [(ngModel)]="timelineMessage" rows="2"
                        [placeholder]="'MCP_INCIDENTS.DESCRIPTION' | translate"></textarea>
              <button class="btn btn-secondary" (click)="addTimelineEntry()"
                      [disabled]="!timelineMessage">
                {{ 'MCP_INCIDENTS.ADD_TIMELINE' | translate }}
              </button>
            </div>
          </div>

          <!-- Resolve -->
          @if (selectedIncident()!.status !== 'resolved') {
            <div class="resolve-section">
              <h4>{{ 'MCP_INCIDENTS.RESOLVE' | translate }}</h4>
              <div class="resolve-form">
                <div class="form-group">
                  <label>{{ 'MCP_INCIDENTS.RESOLUTION' | translate }}</label>
                  <textarea [(ngModel)]="resolveData.resolution" rows="2"></textarea>
                </div>
                <div class="form-group">
                  <label>{{ 'MCP_INCIDENTS.ROOT_CAUSE' | translate }}</label>
                  <textarea [(ngModel)]="resolveData.rootCause" rows="2"></textarea>
                </div>
                <button class="btn btn-primary" (click)="resolveIncident()"
                        [disabled]="!resolveData.resolution">
                  {{ 'MCP_INCIDENTS.RESOLVE' | translate }}
                </button>
              </div>
            </div>
          }

          <!-- Postmortem -->
          <button class="btn btn-secondary" (click)="loadPostmortem()" style="margin-top: 0.75rem;">
            {{ 'MCP_INCIDENTS.POSTMORTEM' | translate }}
          </button>
          @if (postmortemData()) {
            <div class="postmortem-panel">
              <pre class="postmortem-content">{{ formatJson(postmortemData()) }}</pre>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h2 { margin: 0; color: #1a1a2e; }
    h3 { color: #333; font-size: 1.05rem; margin: 0 0 1rem 0; }
    h4 { color: #444; font-size: 0.9rem; margin: 0 0 0.5rem 0; }

    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: #0f3460; color: white; }
    .btn-primary:hover:not(:disabled) { background: #1a4a7a; }
    .btn-secondary { background: white; color: #333; border: 1px solid #ddd; }
    .btn-secondary:hover:not(:disabled) { background: #f5f5f5; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-sm { padding: 0.35rem 0.75rem; font-size: 0.8rem; }

    .form-panel {
      background: white; padding: 1.5rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 1.5rem;
    }
    .form-panel h3 { margin: 0 0 1rem 0; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8rem; font-weight: 500; color: #333; }
    .form-group input, .form-group select, .form-group textarea {
      padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; width: 100%; box-sizing: border-box;
    }
    .form-group textarea { resize: vertical; }
    .full-width { grid-column: 1 / -1; }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: #d4edda; color: #155724; }
    .notification.error { background: #f8d7da; color: #721c24; }

    .filter-bar { display: flex; gap: 1rem; margin-bottom: 1.5rem; flex-wrap: wrap; }
    .filter-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .filter-group label { font-size: 0.75rem; font-weight: 500; color: #666; }
    .filter-group select {
      padding: 0.4rem 0.5rem; border: 1px solid #ddd; border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; min-width: 150px;
    }

    .empty { color: #888; font-size: 0.85rem; }
    .loading-state { display: flex; align-items: center; gap: 0.5rem; color: #999; font-size: 0.85rem; }
    .spinner { display: inline-block; width: 14px; height: 14px; border: 2px solid #ddd; border-top-color: #0f3460; border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }

    .incident-list { display: flex; flex-direction: column; gap: 0.5rem; margin-bottom: 1.5rem; }
    .incident-card {
      background: white; padding: 0.75rem 1rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08); cursor: pointer;
      border-left: 3px solid #ddd; transition: all 0.15s;
    }
    .incident-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.12); }
    .incident-card.selected { border-left-color: #0f3460; box-shadow: 0 0 0 2px rgba(15,52,96,0.2); }
    .incident-header { display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; }
    .incident-title { font-weight: 600; font-size: 0.9rem; }
    .incident-badges { display: flex; gap: 0.3rem; }
    .incident-desc { font-size: 0.8rem; color: #666; margin: 0.25rem 0 0 0; }
    .incident-date { font-size: 0.7rem; color: #999; }

    .severity-badge {
      font-size: 0.65rem; padding: 0.1rem 0.4rem; border-radius: 4px;
      text-transform: uppercase; font-weight: 600;
    }
    .sev-critical { background: #f8d7da; color: #721c24; }
    .sev-high { background: #fff3cd; color: #856404; }
    .sev-medium { background: #d4edda; color: #155724; }
    .sev-low { background: #e2e3e5; color: #383d41; }

    .status-badge {
      font-size: 0.65rem; padding: 0.1rem 0.4rem; border-radius: 4px;
      text-transform: uppercase; font-weight: 600;
    }
    .status-open { background: #f8d7da; color: #721c24; }
    .status-investigating { background: #fff3cd; color: #856404; }
    .status-mitigating { background: #cce5ff; color: #004085; }
    .status-resolved { background: #d4edda; color: #155724; }

    .detail-panel {
      background: white; padding: 1.5rem; border-radius: 10px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1); border-top: 3px solid #0f3460;
    }
    .detail-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
    .detail-header h3 { margin: 0; }
    .detail-meta { display: flex; gap: 0.5rem; margin-bottom: 0.75rem; }
    .detail-desc { font-size: 0.85rem; color: #666; margin: 0 0 1rem 0; }

    .timeline-section { margin-bottom: 1rem; }
    .timeline { display: flex; flex-direction: column; gap: 0.4rem; }
    .timeline-entry {
      display: flex; gap: 0.75rem; font-size: 0.8rem;
      padding: 0.4rem 0; border-bottom: 1px solid #f0f0f0;
    }
    .timeline-time { color: #999; font-size: 0.75rem; white-space: nowrap; min-width: 120px; }
    .timeline-text { color: #333; }

    .add-timeline { margin-bottom: 1rem; }
    .timeline-form { display: flex; gap: 0.5rem; align-items: flex-end; }
    .timeline-form textarea {
      flex: 1; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; resize: vertical; box-sizing: border-box;
    }

    .resolve-section { margin-bottom: 1rem; padding-top: 0.75rem; border-top: 1px solid #eee; }
    .resolve-form .form-group { margin-bottom: 0.5rem; }
    .resolve-form textarea {
      width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; resize: vertical; box-sizing: border-box;
    }

    .postmortem-panel { margin-top: 0.75rem; }
    .postmortem-content {
      background: #1e1e2e; color: #cdd6f4; padding: 0.75rem;
      border-radius: 6px; font-size: 0.8rem; font-family: monospace;
      overflow-x: auto; max-height: 300px; overflow-y: auto;
    }
  `]
})
export class McpIncidentsComponent implements OnInit {
  private incidentService = inject(McpIncidentService);
  private i18n = inject(TranslationService);

  incidents = signal<any[]>([]);
  loading = signal(false);
  selectedIncident = signal<any>(null);
  showCreateForm = signal(false);
  notification = signal<{ type: string; message: string } | null>(null);
  postmortemData = signal<any>(null);

  statusFilter = '';
  severityFilter = '';
  timelineMessage = '';
  resolveData: any = { resolution: '', rootCause: '' };

  newIncident: any = { title: '', severity: 'medium', description: '' };

  ngOnInit(): void {
    this.loadIncidents();
  }

  loadIncidents(): void {
    this.loading.set(true);
    const status = this.statusFilter || undefined;
    const severity = this.severityFilter || undefined;
    this.incidentService.listIncidents(status, severity).subscribe({
      next: (data) => {
        this.incidents.set(Array.isArray(data) ? data : (data?.incidents || []));
        this.loading.set(false);
      },
      error: () => {
        this.incidents.set([]);
        this.loading.set(false);
      }
    });
  }

  selectIncident(incident: any): void {
    this.selectedIncident.set(incident);
    this.postmortemData.set(null);
    this.timelineMessage = '';
    this.resolveData = { resolution: '', rootCause: '' };
  }

  openIncident(): void {
    this.incidentService.openIncident(this.newIncident).subscribe({
      next: () => {
        this.showCreateForm.set(false);
        this.newIncident = { title: '', severity: 'medium', description: '' };
        this.loadIncidents();
        this.showNotify('success', 'Incident created');
      },
      error: () => this.showNotify('error', 'Error creating incident')
    });
  }

  addTimelineEntry(): void {
    const incident = this.selectedIncident();
    if (!incident || !this.timelineMessage) return;
    this.incidentService.addTimeline(incident.id, { message: this.timelineMessage }).subscribe({
      next: (updated) => {
        if (updated) this.selectedIncident.set(updated);
        this.timelineMessage = '';
        this.loadIncidents();
        this.showNotify('success', 'Timeline entry added');
      },
      error: () => this.showNotify('error', 'Error adding timeline entry')
    });
  }

  resolveIncident(): void {
    const incident = this.selectedIncident();
    if (!incident) return;
    this.incidentService.resolveIncident(incident.id, this.resolveData).subscribe({
      next: (updated) => {
        if (updated) this.selectedIncident.set(updated);
        this.resolveData = { resolution: '', rootCause: '' };
        this.loadIncidents();
        this.showNotify('success', 'Incident resolved');
      },
      error: () => this.showNotify('error', 'Error resolving incident')
    });
  }

  loadPostmortem(): void {
    const incident = this.selectedIncident();
    if (!incident) return;
    this.incidentService.getPostmortem(incident.id).subscribe({
      next: (data) => this.postmortemData.set(data),
      error: () => this.showNotify('error', 'Error loading postmortem')
    });
  }

  getSeverityLabel(severity: string): string {
    return this.i18n.instant(`MCP_INCIDENTS.SEV_${(severity || 'medium').toUpperCase()}`);
  }

  getStatusLabel(status: string): string {
    return this.i18n.instant(`MCP_INCIDENTS.STATUS_${(status || 'open').toUpperCase()}`);
  }

  formatJson(data: any): string {
    try {
      return JSON.stringify(data, null, 2);
    } catch {
      return String(data);
    }
  }

  private showNotify(type: string, message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 4000);
  }
}
