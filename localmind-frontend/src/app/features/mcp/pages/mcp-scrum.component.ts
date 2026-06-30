import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { McpScrumService } from '../services/mcp-scrum.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { TranslationService } from '../../../core/i18n/translation.service';

@Component({
  selector: 'app-mcp-scrum',
  standalone: true,
  imports: [FormsModule, TranslatePipe],
  template: `
    <div class="scrum-page">
      <div class="header">
        <h2>{{ 'MCP_SCRUM.TITLE' | translate }}</h2>
        <div class="header-actions">
          <button class="btn btn-primary" (click)="showCreateSprintForm.set(!showCreateSprintForm())">
            {{ showCreateSprintForm() ? ('COMMON.CANCEL' | translate) : ('MCP_SCRUM.CREATE_SPRINT' | translate) }}
          </button>
          <button class="btn btn-secondary" (click)="showCreateStoryForm.set(!showCreateStoryForm())">
            {{ showCreateStoryForm() ? ('COMMON.CANCEL' | translate) : ('MCP_SCRUM.CREATE_STORY' | translate) }}
          </button>
        </div>
      </div>

      @if (showCreateSprintForm()) {
        <div class="form-panel">
          <h3>{{ 'MCP_SCRUM.CREATE_SPRINT' | translate }}</h3>
          <div class="form-grid">
            <div class="form-group">
              <label>{{ 'MCP_SCRUM.NAME' | translate }}</label>
              <input type="text" [(ngModel)]="newSprint.name" [placeholder]="'MCP_SCRUM.NAME' | translate">
            </div>
            <div class="form-group">
              <label>{{ 'MCP_SCRUM.START_DATE' | translate }}</label>
              <input type="date" [(ngModel)]="newSprint.startDate">
            </div>
            <div class="form-group">
              <label>{{ 'MCP_SCRUM.END_DATE' | translate }}</label>
              <input type="date" [(ngModel)]="newSprint.endDate">
            </div>
            <div class="form-group full-width">
              <label>{{ 'MCP_SCRUM.GOALS' | translate }}</label>
              <textarea [(ngModel)]="newSprint.goals" rows="3" [placeholder]="'MCP_SCRUM.GOALS' | translate"></textarea>
            </div>
          </div>
          <button class="btn btn-primary" (click)="createSprint()" [disabled]="!newSprint.name">
            {{ 'MCP_SCRUM.CREATE_SPRINT' | translate }}
          </button>
        </div>
      }

      @if (showCreateStoryForm()) {
        <div class="form-panel">
          <h3>{{ 'MCP_SCRUM.CREATE_STORY' | translate }}</h3>
          <div class="form-grid">
            <div class="form-group">
              <label>{{ 'MCP_SCRUM.TITLE_FIELD' | translate }}</label>
              <input type="text" [(ngModel)]="newStory.title" [placeholder]="'MCP_SCRUM.TITLE_FIELD' | translate">
            </div>
            <div class="form-group">
              <label>{{ 'MCP_SCRUM.SPRINT_ID' | translate }}</label>
              <input type="text" [(ngModel)]="newStory.sprintId" [placeholder]="'MCP_SCRUM.SPRINT_ID' | translate">
            </div>
            <div class="form-group full-width">
              <label>{{ 'MCP_SCRUM.DESCRIPTION' | translate }}</label>
              <textarea [(ngModel)]="newStory.description" rows="3" [placeholder]="'MCP_SCRUM.DESCRIPTION' | translate"></textarea>
            </div>
            <div class="form-group">
              <label>{{ 'MCP_SCRUM.STORY_POINTS' | translate }}</label>
              <input type="number" [(ngModel)]="newStory.points" min="0">
            </div>
            <div class="form-group">
              <label>{{ 'MCP_SCRUM.PRIORITY' | translate }}</label>
              <select [(ngModel)]="newStory.priority">
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>
          </div>
          <button class="btn btn-primary" (click)="createStory()" [disabled]="!newStory.title">
            {{ 'MCP_SCRUM.CREATE_STORY' | translate }}
          </button>
        </div>
      }

      @if (notification()) {
        <div class="notification" [class]="notification()!.type">
          {{ notification()!.message }}
        </div>
      }

      <!-- Backlog Section -->
      <div class="section">
        <div class="section-header">
          <h3>{{ 'MCP_SCRUM.BACKLOG' | translate }}</h3>
          <button class="btn btn-secondary btn-sm" (click)="loadBacklog()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M23 4v6h-6M1 20v-6h6"/>
              <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
            </svg>
            {{ 'COMMON.REFRESH' | translate }}
          </button>
        </div>
        @if (backlog().length === 0) {
          <p class="empty">{{ 'MCP_SCRUM.BACKLOG_EMPTY' | translate }}</p>
        } @else {
          <div class="story-list">
            @for (story of backlog(); track story.id || $index) {
              <div class="story-card">
                <div class="story-header">
                  <span class="story-title">{{ story.title }}</span>
                  <div class="story-meta">
                    @if (story.points !== undefined && story.points !== null) {
                      <span class="points-badge">{{ story.points }} pts</span>
                    }
                    @if (story.priority) {
                      <span class="priority-badge" [class]="'priority-' + story.priority.toLowerCase()">
                        {{ story.priority }}
                      </span>
                    }
                  </div>
                </div>
                @if (story.description) {
                  <p class="story-desc">{{ story.description }}</p>
                }
              </div>
            }
          </div>
        }
      </div>

      <!-- Sprint Board Section -->
      <div class="section">
        <h3>{{ 'MCP_SCRUM.LOAD_BOARD' | translate }}</h3>
        <div class="sprint-input">
          <input type="text" [(ngModel)]="currentSprintId"
                 [placeholder]="'MCP_SCRUM.SPRINT_ID' | translate"
                 (keydown.enter)="loadBoard()">
          <button class="btn btn-primary" (click)="loadBoard()" [disabled]="!currentSprintId || loading()">
            @if (loading()) { <span class="spinner-sm"></span> }
            {{ 'MCP_SCRUM.LOAD_BOARD' | translate }}
          </button>
        </div>

        @if (!boardData() && !loading()) {
          <p class="empty board-hint">{{ 'MCP_SCRUM.BOARD_EMPTY' | translate }}</p>
        }

        @if (boardData()) {
          <div class="kanban-board">
            @for (col of columns; track col.key) {
              <div class="kanban-column" [class]="'col-' + col.key.toLowerCase()">
                <div class="column-header">
                  <span class="column-title">{{ 'MCP_SCRUM.COL_' + col.key | translate }}</span>
                  <span class="column-count">{{ getTasksByStatus(col.key).length }}</span>
                </div>
                <div class="column-body">
                  @for (task of getTasksByStatus(col.key); track task.id || $index) {
                    <div class="task-card">
                      <div class="task-title">{{ task.title }}</div>
                      @if (task.assignee) {
                        <div class="task-assignee">
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                            <circle cx="12" cy="7" r="4"/>
                          </svg>
                          {{ task.assignee }}
                        </div>
                      }
                      <div class="task-actions">
                        <select class="status-select" [ngModel]="task.status"
                                (ngModelChange)="changeTaskStatus(task.id, $event)">
                          @for (s of columns; track s.key) {
                            <option [value]="s.key">{{ 'MCP_SCRUM.COL_' + s.key | translate }}</option>
                          }
                        </select>
                      </div>
                    </div>
                  }
                </div>
              </div>
            }
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 0.5rem; }
    .header-actions { display: flex; gap: 0.5rem; }
    h2 { margin: 0; color: var(--color-text); }
    h3 { color: var(--color-text); font-size: 1.05rem; margin: 0 0 1rem 0; }
    .section { margin-bottom: 2rem; }
    .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .section-header h3 { margin: 0; }

    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-family: inherit; font-weight: 500; }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn-primary:hover:not(:disabled) { background: var(--color-primary-light); }
    .btn-secondary { background: var(--color-card-bg); color: var(--color-text); border: 1px solid var(--color-border); }
    .btn-secondary:hover:not(:disabled) { background: var(--color-bg); }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-sm { padding: 0.35rem 0.75rem; font-size: 0.8rem; }

    .form-panel {
      background: var(--color-card-bg); padding: 1.5rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 1.5rem;
    }
    .form-panel h3 { margin: 0 0 1rem 0; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8rem; font-weight: 500; color: var(--color-text); }
    .form-group input, .form-group select, .form-group textarea {
      padding: 0.5rem; border: 1px solid var(--color-border); border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; width: 100%; box-sizing: border-box;
    }
    .form-group textarea { resize: vertical; }
    .full-width { grid-column: 1 / -1; }

    .notification { padding: 0.6rem 1rem; border-radius: 6px; font-size: 0.8rem; margin-bottom: 1rem; }
    .notification.success { background: var(--color-success-bg); color: var(--color-success-text); }
    .notification.error { background: var(--color-error-bg); color: var(--color-error-text); }

    .empty { color: var(--color-text-muted); font-size: 0.85rem; }
    .board-hint { font-style: italic; }

    .story-list { display: flex; flex-direction: column; gap: 0.5rem; }
    .story-card {
      background: var(--color-card-bg); padding: 0.75rem 1rem; border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08); border-left: 3px solid var(--color-primary);
    }
    .story-header { display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; }
    .story-title { font-weight: 600; font-size: 0.9rem; }
    .story-meta { display: flex; gap: 0.4rem; align-items: center; }
    .story-desc { font-size: 0.8rem; color: var(--color-text-secondary); margin: 0.25rem 0 0 0; }
    .points-badge {
      background: var(--color-info-bg); color: var(--color-primary); font-size: 0.7rem; padding: 0.1rem 0.4rem;
      border-radius: 4px; font-weight: 600;
    }
    .priority-badge {
      font-size: 0.65rem; padding: 0.1rem 0.4rem; border-radius: 4px;
      text-transform: uppercase; font-weight: 600;
    }
    .priority-critical { background: var(--color-error-bg); color: var(--color-error-text); }
    .priority-high { background: var(--color-warning-bg); color: var(--color-warning-text); }
    .priority-medium { background: var(--color-success-bg); color: var(--color-success-text); }
    .priority-low { background: var(--color-border); color: var(--color-text-secondary); }

    .sprint-input { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
    .sprint-input input {
      padding: 0.5rem; border: 1px solid var(--color-border); border-radius: 4px;
      font-size: 0.85rem; font-family: inherit; width: 300px;
    }

    .kanban-board {
      display: grid; grid-template-columns: repeat(5, 1fr); gap: 0.75rem;
      overflow-x: auto; min-width: 0;
    }
    .kanban-column {
      background: var(--color-hover-bg); border-radius: 8px; padding: 0.75rem;
      min-height: 200px; display: flex; flex-direction: column;
    }
    .col-todo { border-top: 3px solid var(--color-text-muted); }
    .col-in_progress { border-top: 3px solid var(--color-primary); }
    .col-in_review { border-top: 3px solid var(--color-warning); }
    .col-done { border-top: 3px solid var(--color-success); }
    .col-blocked { border-top: 3px solid var(--color-error); }

    .column-header {
      display: flex; justify-content: space-between; align-items: center;
      margin-bottom: 0.75rem; padding-bottom: 0.5rem; border-bottom: 1px solid var(--color-border-light);
    }
    .column-title { font-weight: 600; font-size: 0.8rem; color: var(--color-text); }
    .column-count {
      background: var(--color-border-light); color: var(--color-text-secondary); font-size: 0.7rem; padding: 0.1rem 0.4rem;
      border-radius: 10px; min-width: 1.2rem; text-align: center;
    }
    .column-body { display: flex; flex-direction: column; gap: 0.5rem; flex: 1; }

    .task-card {
      background: var(--color-card-bg); padding: 0.6rem; border-radius: 6px;
      box-shadow: 0 1px 2px rgba(0,0,0,0.08); font-size: 0.8rem;
    }
    .task-title { font-weight: 500; margin-bottom: 0.25rem; }
    .task-assignee {
      display: flex; align-items: center; gap: 0.25rem;
      font-size: 0.7rem; color: var(--color-text-secondary); margin-bottom: 0.25rem;
    }
    .task-actions { margin-top: 0.25rem; }
    .status-select {
      width: 100%; padding: 0.2rem; border: 1px solid var(--color-border); border-radius: 4px;
      font-size: 0.7rem; font-family: inherit; background: var(--color-card-bg);
    }

    .spinner-sm { display: inline-block; width: 12px; height: 12px; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class McpScrumComponent implements OnInit {
  private scrumService = inject(McpScrumService);
  private i18n = inject(TranslationService);

  backlog = signal<any[]>([]);
  boardData = signal<any>(null);
  loading = signal(false);
  showCreateSprintForm = signal(false);
  showCreateStoryForm = signal(false);
  notification = signal<{ type: string; message: string } | null>(null);

  currentSprintId = '';

  newSprint: any = { name: '', startDate: '', endDate: '', goals: '' };
  newStory: any = { title: '', description: '', points: 0, priority: 'MEDIUM', sprintId: '' };

  readonly columns = [
    { key: 'TODO' },
    { key: 'IN_PROGRESS' },
    { key: 'IN_REVIEW' },
    { key: 'DONE' },
    { key: 'BLOCKED' }
  ];

  ngOnInit(): void {
    this.loadBacklog();
  }

  loadBacklog(): void {
    this.scrumService.getBacklog().subscribe({
      next: (data) => this.backlog.set(Array.isArray(data) ? data : (data?.stories || [])),
      error: () => this.backlog.set([])
    });
  }

  loadBoard(): void {
    if (!this.currentSprintId) return;
    this.loading.set(true);
    this.scrumService.getSprintBoard(this.currentSprintId).subscribe({
      next: (data) => {
        this.boardData.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.boardData.set(null);
        this.loading.set(false);
        this.showNotify('error', 'Error loading board');
      }
    });
  }

  getTasksByStatus(status: string): any[] {
    const board = this.boardData();
    if (!board) return [];
    if (board.columns && board.columns[status]) return board.columns[status];
    if (board.tasks) return board.tasks.filter((t: any) => t.status === status);
    if (Array.isArray(board)) return board.filter((t: any) => t.status === status);
    return [];
  }

  changeTaskStatus(taskId: string, newStatus: string): void {
    this.scrumService.updateTaskStatus(taskId, newStatus).subscribe({
      next: () => this.loadBoard(),
      error: () => this.showNotify('error', 'Error updating task status')
    });
  }

  createSprint(): void {
    const data: any = {
      name: this.newSprint.name,
      startDate: this.newSprint.startDate,
      endDate: this.newSprint.endDate
    };
    if (this.newSprint.goals) {
      data.goals = this.newSprint.goals.split('\n').filter((g: string) => g.trim());
    }
    this.scrumService.createSprint(data).subscribe({
      next: () => {
        this.showCreateSprintForm.set(false);
        this.newSprint = { name: '', startDate: '', endDate: '', goals: '' };
        this.showNotify('success', 'Sprint created');
      },
      error: () => this.showNotify('error', 'Error creating sprint')
    });
  }

  createStory(): void {
    this.scrumService.createStory(this.newStory).subscribe({
      next: () => {
        this.showCreateStoryForm.set(false);
        this.newStory = { title: '', description: '', points: 0, priority: 'MEDIUM', sprintId: '' };
        this.loadBacklog();
        this.showNotify('success', 'Story created');
      },
      error: () => this.showNotify('error', 'Error creating story')
    });
  }

  private showNotify(type: string, message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 4000);
  }
}
