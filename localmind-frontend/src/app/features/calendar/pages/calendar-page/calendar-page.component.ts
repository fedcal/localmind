import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { CalendarService } from '../../services/calendar.service';
import { CalendarEvent, CreateCalendarEventRequest } from '../../models/calendar-event.model';

@Component({
  selector: 'app-calendar-page',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  template: `
    <div class="calendar-page">
      <div class="page-header">
        <div>
          <h1>{{ 'CALENDAR.TITLE' | translate }}</h1>
          <p class="subtitle">{{ 'CALENDAR.SUBTITLE' | translate }}</p>
        </div>
        <div class="header-actions">
          <button class="btn btn-secondary" (click)="loadEvents()">{{ 'COMMON.REFRESH' | translate }}</button>
          <button class="btn btn-primary" (click)="showForm.set(!showForm())">{{ 'CALENDAR.ADD_EVENT' | translate }}</button>
        </div>
      </div>

      @if (showForm()) {
        <div class="form-card">
          <h3>{{ 'CALENDAR.FORM_TITLE' | translate }}</h3>
          <div class="form-group">
            <label>{{ 'CALENDAR.EVENT_TITLE' | translate }} *</label>
            <input type="text" [(ngModel)]="newEvent.title"
                   [placeholder]="'CALENDAR.EVENT_TITLE_PLACEHOLDER' | translate" />
          </div>
          <div class="form-group">
            <label>{{ 'CALENDAR.DESCRIPTION' | translate }}</label>
            <textarea [(ngModel)]="newEvent.description"
                      [placeholder]="'CALENDAR.DESCRIPTION_PLACEHOLDER' | translate"></textarea>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>{{ 'CALENDAR.START_TIME' | translate }} *</label>
              <input type="datetime-local" [(ngModel)]="startTimeLocal" />
            </div>
            <div class="form-group">
              <label>{{ 'CALENDAR.END_TIME' | translate }} *</label>
              <input type="datetime-local" [(ngModel)]="endTimeLocal" />
            </div>
          </div>
          <div class="form-group">
            <label>{{ 'CALENDAR.LOCATION' | translate }}</label>
            <input type="text" [(ngModel)]="newEvent.location"
                   [placeholder]="'CALENDAR.LOCATION_PLACEHOLDER' | translate" />
          </div>
          <div class="form-actions">
            <button class="btn btn-secondary" (click)="showForm.set(false)">{{ 'COMMON.CANCEL' | translate }}</button>
            <button class="btn btn-primary" (click)="createEvent()"
                    [disabled]="!newEvent.title || !startTimeLocal || !endTimeLocal">
              {{ 'COMMON.SAVE' | translate }}
            </button>
          </div>
        </div>
      }

      @if (loading()) {
        <p class="loading">{{ 'COMMON.LOADING' | translate }}</p>
      } @else if (events().length === 0) {
        <div class="empty-state">
          <h3>{{ 'CALENDAR.EMPTY_TITLE' | translate }}</h3>
          <p>{{ 'CALENDAR.EMPTY_DESC' | translate }}</p>
        </div>
      } @else {
        <div class="events-list">
          @for (event of events(); track event.id) {
            <div class="event-card">
              <div class="event-info">
                <h3>{{ event.title }}</h3>
                @if (event.description) {
                  <p class="event-desc">{{ event.description }}</p>
                }
                <div class="event-meta">
                  <span class="event-time">{{ event.startTime | date:'short' }} - {{ event.endTime | date:'short' }}</span>
                  @if (event.location) {
                    <span class="event-location">{{ event.location }}</span>
                  }
                  @if (event.attendees && event.attendees.length > 0) {
                    <span class="event-attendees">{{ event.attendees.join(', ') }}</span>
                  }
                </div>
              </div>
              <button class="btn btn-danger btn-sm" (click)="deleteEvent(event.id)"
                      [title]="'COMMON.DELETE' | translate">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                </svg>
              </button>
            </div>
          }
        </div>
      }

      @if (notification()) {
        <div class="notification" [class.error]="notificationError()">{{ notification() }}</div>
      }
    </div>
  `,
  styles: [`
    .calendar-page { max-width: 900px; }
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
    .page-header h1 { margin: 0; font-size: 1.5rem; }
    .subtitle { color: var(--color-text-muted); margin: 0.25rem 0 0; font-size: 0.875rem; }
    .header-actions { display: flex; gap: 0.5rem; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; cursor: pointer; font-size: 0.875rem; font-weight: 500; transition: all 0.2s; }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn-primary:hover { opacity: 0.9; }
    .btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-secondary { background: var(--color-surface); color: var(--color-text); border: 1px solid var(--color-border); }
    .btn-secondary:hover { background: var(--color-border); }
    .btn-danger { background: none; color: var(--color-error); border: 1px solid var(--color-error); }
    .btn-danger:hover { background: var(--color-error); color: white; }
    .btn-sm { padding: 0.35rem 0.6rem; }
    .form-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: 8px; padding: 1.5rem; margin-bottom: 1.5rem; }
    .form-card h3 { margin: 0 0 1rem; }
    .form-group { margin-bottom: 1rem; }
    .form-group label { display: block; margin-bottom: 0.25rem; font-weight: 500; font-size: 0.875rem; }
    .form-group input, .form-group textarea { width: 100%; padding: 0.5rem; border: 1px solid var(--color-border); border-radius: 6px; font-size: 0.875rem; background: var(--color-bg); color: var(--color-text); box-sizing: border-box; }
    .form-group textarea { min-height: 80px; resize: vertical; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .form-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
    .events-list { display: flex; flex-direction: column; gap: 0.5rem; }
    .event-card { display: flex; justify-content: space-between; align-items: center; background: var(--color-surface); border: 1px solid var(--color-border); border-radius: 8px; padding: 1rem 1.25rem; }
    .event-info h3 { margin: 0; font-size: 1rem; }
    .event-desc { margin: 0.25rem 0; color: var(--color-text-muted); font-size: 0.85rem; }
    .event-meta { display: flex; gap: 1rem; font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.25rem; }
    .event-time { font-weight: 500; }
    .empty-state { text-align: center; padding: 3rem 1rem; color: var(--color-text-muted); }
    .loading { text-align: center; color: var(--color-text-muted); padding: 2rem; }
    .notification { position: fixed; top: 1rem; right: 1rem; padding: 0.75rem 1.25rem; border-radius: 8px; background: var(--color-success-bg); color: var(--color-success-text); font-size: 0.85rem; z-index: 1000; animation: slideIn 0.3s ease; }
    .notification.error { background: var(--color-error-bg); color: var(--color-error-text); }
    @keyframes slideIn { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }
  `]
})
export class CalendarPageComponent implements OnInit {
  private calendarService = inject(CalendarService);

  events = signal<CalendarEvent[]>([]);
  loading = signal(false);
  showForm = signal(false);
  notification = signal('');
  notificationError = signal(false);

  newEvent: CreateCalendarEventRequest = { title: '', startTime: '', endTime: '' };
  startTimeLocal = '';
  endTimeLocal = '';

  ngOnInit() {
    this.loadEvents();
  }

  loadEvents() {
    this.loading.set(true);
    const now = new Date();
    const from = new Date(now.getFullYear(), now.getMonth(), 1).toISOString();
    const to = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59).toISOString();

    this.calendarService.listEvents(from, to).subscribe({
      next: (events) => {
        this.events.set(events);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.showNotification('CALENDAR.LOAD_ERROR', true);
      }
    });
  }

  createEvent() {
    const request: CreateCalendarEventRequest = {
      ...this.newEvent,
      startTime: new Date(this.startTimeLocal).toISOString(),
      endTime: new Date(this.endTimeLocal).toISOString()
    };

    this.calendarService.createEvent(request).subscribe({
      next: () => {
        this.showForm.set(false);
        this.resetForm();
        this.loadEvents();
        this.showNotification('CALENDAR.CREATE_SUCCESS', false);
      },
      error: () => this.showNotification('CALENDAR.CREATE_ERROR', true)
    });
  }

  deleteEvent(id: string) {
    this.calendarService.deleteEvent(id).subscribe({
      next: () => {
        this.events.set(this.events().filter(e => e.id !== id));
        this.showNotification('CALENDAR.DELETE_SUCCESS', false);
      },
      error: () => this.showNotification('CALENDAR.DELETE_ERROR', true)
    });
  }

  private resetForm() {
    this.newEvent = { title: '', startTime: '', endTime: '' };
    this.startTimeLocal = '';
    this.endTimeLocal = '';
  }

  private showNotification(msg: string, isError: boolean) {
    this.notification.set(msg);
    this.notificationError.set(isError);
    setTimeout(() => this.notification.set(''), 4000);
  }
}
