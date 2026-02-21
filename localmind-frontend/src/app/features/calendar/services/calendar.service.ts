import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { CalendarEvent, CreateCalendarEventRequest } from '../models/calendar-event.model';

@Injectable({ providedIn: 'root' })
export class CalendarService {
  private api = inject(ApiService);

  listEvents(from: string, to: string): Observable<CalendarEvent[]> {
    return this.api.get<CalendarEvent[]>(`/calendar/events?from=${from}&to=${to}`);
  }

  createEvent(request: CreateCalendarEventRequest): Observable<CalendarEvent> {
    return this.api.post<CalendarEvent>('/calendar/events', request);
  }

  deleteEvent(id: string): Observable<void> {
    return this.api.delete<void>(`/calendar/events/${id}`);
  }
}
