import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { McpTimeService } from '../services/mcp-time.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../core/i18n/translation.service';

@Component({
  selector: 'app-mcp-time',
  standalone: true,
  imports: [FormsModule, TranslatePipe],
  template: `
    <div class="time-page">
      <h2>{{ 'MCP_TIME.TITLE' | translate }}</h2>

      @if (notification()) {
        <div class="notification" [class]="notification()!.type">
          {{ notification()!.message }}
        </div>
      }

      <!-- Timer Section -->
      <div class="section">
        <div class="section-card">
          <h3>{{ 'MCP_TIME.TIMER' | translate }}</h3>
          <div class="timer-row">
            <div class="form-group">
              <label>{{ 'MCP_TIME.TASK_ID' | translate }}</label>
              <input type="text" [(ngModel)]="timerTaskId"
                     [placeholder]="'MCP_TIME.TASK_ID' | translate"
                     [disabled]="timerActive()">
            </div>
            <div class="form-group">
              <label>{{ 'MCP_TIME.DESCRIPTION' | translate }}</label>
              <input type="text" [(ngModel)]="timerDescription"
                     [placeholder]="'MCP_TIME.DESCRIPTION' | translate"
                     [disabled]="timerActive()">
            </div>
            <div class="timer-buttons">
              @if (!timerActive()) {
                <button class="btn btn-primary" (click)="startTimer()" [disabled]="!timerTaskId">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polygon points="5 3 19 12 5 21 5 3"/>
                  </svg>
                  {{ 'MCP_TIME.START' | translate }}
                </button>
              } @else {
                <button class="btn btn-danger" (click)="stopTimer()">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/>
                  </svg>
                  {{ 'MCP_TIME.STOP' | translate }}
                </button>
              }
            </div>
          </div>
          @if (timerActive()) {
            <div class="timer-active-indicator">
              <span class="pulse"></span>
              {{ 'MCP_TIME.TIMER_ACTIVE' | translate }} <strong>{{ timerData()?.taskId || timerTaskId }}</strong>
            </div>
          }
        </div>
      </div>

      <!-- Manual Log Section -->
      <div class="section">
        <div class="section-card">
          <div class="section-header-row">
            <h3>{{ 'MCP_TIME.LOG_MANUAL' | translate }}</h3>
            <button class="btn btn-secondary btn-sm" (click)="showLogForm.set(!showLogForm())">
              {{ showLogForm() ? ('COMMON.CANCEL' | translate) : ('MCP_TIME.LOG' | translate) }}
            </button>
          </div>
          @if (showLogForm()) {
            <div class="log-form">
              <div class="form-grid">
                <div class="form-group">
                  <label>{{ 'MCP_TIME.TASK_ID' | translate }}</label>
                  <input type="text" [(ngModel)]="logEntry.taskId" [placeholder]="'MCP_TIME.TASK_ID' | translate">
                </div>
                <div class="form-group">
                  <label>{{ 'MCP_TIME.DURATION' | translate }}</label>
                  <input type="number" [(ngModel)]="logEntry.durationMinutes" min="1">
                </div>
                <div class="form-group">
                  <label>{{ 'MCP_TIME.DATE' | translate }}</label>
                  <input type="date" [(ngModel)]="logEntry.date">
                </div>
                <div class="form-group">
                  <label>{{ 'MCP_TIME.DESCRIPTION' | translate }}</label>
                  <input type="text" [(ngModel)]="logEntry.description" [placeholder]="'MCP_TIME.DESCRIPTION' | translate">
                </div>
              </div>
              <button class="btn btn-primary" (click)="logTime()"
                      [disabled]="!logEntry.taskId || !logEntry.durationMinutes">
                {{ 'MCP_TIME.LOG' | translate }}
              </button>
            </div>
          }
        </div>
      </div>

      <!-- Timesheet Section -->
      <div class="section">
        <div class="section-card">
          <h3>{{ 'MCP_TIME.TIMESHEET' | translate }}</h3>
          <div class="timesheet-controls">
            <div class="form-group">
              <label>{{ 'MCP_TIME.START_DATE' | translate }}</label>
              <input type="date" [(ngModel)]="timesheetStartDate">
            </div>
            <div class="form-group">
              <label>{{ 'MCP_TIME.END_DATE' | translate }}</label>
              <input type="date" [(ngModel)]="timesheetEndDate">
            </div>
            <div class="timesheet-btn-wrap">
              <button class="btn btn-primary" (click)="loadTimesheet()"
                      [disabled]="!timesheetStartDate || !timesheetEndDate || loading()">
                @if (loading()) { <span class="spinner-sm"></span> }
                {{ 'MCP_TIME.LOAD' | translate }}
              </button>
            </div>
          </div>

          @if (timesheet().length > 0) {
            <div class="timesheet-table-wrap">
              <table class="timesheet-table">
                <thead>
                  <tr>
                    <th>{{ 'MCP_TIME.DATE' | translate }}</th>
                    <th>{{ 'MCP_TIME.TASK_ID' | translate }}</th>
                    <th>{{ 'MCP_TIME.DURATION' | translate }}</th>
                    <th>{{ 'MCP_TIME.DESCRIPTION' | translate }}</th>
                  </tr>
                </thead>
                <tbody>
                  @for (entry of timesheet(); track $index) {
                    <tr>
                      <td>{{ entry.date || entry.startTime || '-' }}</td>
                      <td class="mono">{{ entry.taskId || entry.task || '-' }}</td>
                      <td>{{ entry.durationMinutes || entry.duration || 0 }} min</td>
                      <td>{{ entry.description || '-' }}</td>
                    </tr>
                  }
                </tbody>
                <tfoot>
                  <tr class="total-row">
                    <td colspan="2"><strong>{{ 'MCP_TIME.TOTAL' | translate }}</strong></td>
                    <td><strong>{{ getTotalMinutes() }} min</strong></td>
                    <td></td>
                  </tr>
                </tfoot>
              </table>
            </div>
          } @else if (!loading()) {
            <p class="empty">{{ 'MCP_TIME.EMPTY' | translate }}</p>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    h2 { margin: 0 0 1.5rem 0; color: #1a1a2e; }
    h3 { color: #333; font-size: 1.05rem; margin: 0 0 1rem 0; }
    .section { margin-bottom: 1.5rem; }

    .section-card {
      background: white; padding: 1.5rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08);
    }
    .section-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
    .section-header-row h3 { margin: 0; }

    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: #0f3460; color: white; }
    .btn-primary:hover:not(:disabled) { background: #1a4a7a; }
    .btn-secondary { background: white; color: #333; border: 1px solid #ddd; }
    .btn-secondary:hover:not(:disabled) { background: #f5f5f5; }
    .btn-danger { background: #dc3545; color: white; }
    .btn-danger:hover:not(:disabled) { background: #c82333; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-sm { padding: 0.35rem 0.75rem; font-size: 0.8rem; }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: #d4edda; color: #155724; }
    .notification.error { background: #f8d7da; color: #721c24; }

    .timer-row { display: flex; gap: 1rem; align-items: flex-end; flex-wrap: wrap; }
    .timer-row .form-group { flex: 1; min-width: 150px; }
    .timer-buttons { display: flex; align-items: flex-end; padding-bottom: 0.1rem; }

    .timer-active-indicator {
      display: flex; align-items: center; gap: 0.5rem;
      margin-top: 0.75rem; padding: 0.5rem 0.75rem;
      background: #e8f5e9; border-radius: 6px;
      font-size: 0.85rem; color: #2e7d32;
    }
    .pulse {
      display: inline-block; width: 8px; height: 8px;
      background: #4caf50; border-radius: 50%;
      animation: pulse-anim 1.5s ease-in-out infinite;
    }
    @keyframes pulse-anim {
      0%, 100% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.5; transform: scale(1.3); }
    }

    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8rem; font-weight: 500; color: #333; }
    .form-group input, .form-group select {
      padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; box-sizing: border-box;
    }

    .log-form { margin-top: 0.75rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }

    .timesheet-controls { display: flex; gap: 1rem; align-items: flex-end; margin-bottom: 1rem; flex-wrap: wrap; }
    .timesheet-controls .form-group { min-width: 150px; }
    .timesheet-btn-wrap { display: flex; align-items: flex-end; padding-bottom: 0.1rem; }

    .timesheet-table-wrap { overflow-x: auto; }
    .timesheet-table {
      width: 100%; border-collapse: collapse; font-size: 0.85rem;
    }
    .timesheet-table th {
      background: #f8f9fa; padding: 0.5rem 0.75rem; text-align: left;
      font-weight: 600; color: #333; border-bottom: 2px solid #e0e0e0;
      font-size: 0.8rem;
    }
    .timesheet-table td {
      padding: 0.5rem 0.75rem; border-bottom: 1px solid #f0f0f0; color: #555;
    }
    .timesheet-table tbody tr:hover { background: #f8f9fa; }
    .total-row { background: #f0f4ff; }
    .total-row td { border-top: 2px solid #0f3460; color: #0f3460; }
    .mono { font-family: monospace; font-size: 0.8rem; }

    .empty { color: #888; font-size: 0.85rem; }

    .spinner-sm { display: inline-block; width: 12px; height: 12px; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class McpTimeComponent {
  private timeService = inject(McpTimeService);
  private i18n = inject(TranslationService);

  timerActive = signal(false);
  timerData = signal<any>(null);
  timesheet = signal<any[]>([]);
  loading = signal(false);
  showLogForm = signal(false);
  notification = signal<{ type: string; message: string } | null>(null);

  timerTaskId = '';
  timerDescription = '';

  logEntry: any = { taskId: '', durationMinutes: 30, description: '', date: '' };

  timesheetStartDate = '';
  timesheetEndDate = '';

  startTimer(): void {
    if (!this.timerTaskId) return;
    this.timeService.startTimer({
      taskId: this.timerTaskId,
      description: this.timerDescription
    }).subscribe({
      next: (result) => {
        this.timerActive.set(true);
        this.timerData.set(result);
        this.showNotify('success', 'Timer started');
      },
      error: () => this.showNotify('error', 'Error starting timer')
    });
  }

  stopTimer(): void {
    this.timeService.stopTimer().subscribe({
      next: (result) => {
        this.timerActive.set(false);
        this.timerData.set(null);
        this.timerTaskId = '';
        this.timerDescription = '';
        this.showNotify('success', 'Timer stopped');
      },
      error: () => this.showNotify('error', 'Error stopping timer')
    });
  }

  logTime(): void {
    this.timeService.logTime(this.logEntry).subscribe({
      next: () => {
        this.logEntry = { taskId: '', durationMinutes: 30, description: '', date: '' };
        this.showLogForm.set(false);
        this.showNotify('success', 'Time logged');
      },
      error: () => this.showNotify('error', 'Error logging time')
    });
  }

  loadTimesheet(): void {
    if (!this.timesheetStartDate || !this.timesheetEndDate) return;
    this.loading.set(true);
    this.timeService.getTimesheet(this.timesheetStartDate, this.timesheetEndDate).subscribe({
      next: (data) => {
        this.timesheet.set(Array.isArray(data) ? data : (data?.entries || []));
        this.loading.set(false);
      },
      error: () => {
        this.timesheet.set([]);
        this.loading.set(false);
        this.showNotify('error', 'Error loading timesheet');
      }
    });
  }

  getTotalMinutes(): number {
    return this.timesheet().reduce((sum, e) => sum + (e.durationMinutes || e.duration || 0), 0);
  }

  private showNotify(type: string, message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 4000);
  }
}
