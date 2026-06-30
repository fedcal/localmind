package com.localmind.domain.calendar.port.in;

import com.localmind.domain.calendar.model.CalendarEvent;

import java.time.Instant;
import java.util.List;

public interface CalendarUseCase {
    List<CalendarEvent> listEvents(Instant from, Instant to);
    CalendarEvent createEvent(CalendarEvent event);
    void deleteEvent(String eventId);
    boolean isAvailable();
}
