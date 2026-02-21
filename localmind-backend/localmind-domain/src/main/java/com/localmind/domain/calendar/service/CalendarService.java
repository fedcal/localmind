package com.localmind.domain.calendar.service;

import com.localmind.domain.calendar.model.CalendarEvent;
import com.localmind.domain.calendar.port.in.CalendarUseCase;
import com.localmind.domain.calendar.port.out.CalendarPort;

import java.time.Instant;
import java.util.List;

public class CalendarService implements CalendarUseCase {

    private final CalendarPort calendarPort;

    public CalendarService(CalendarPort calendarPort) {
        this.calendarPort = calendarPort;
    }

    @Override
    public List<CalendarEvent> listEvents(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both 'from' and 'to' parameters are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must be before 'to'");
        }
        return calendarPort.listEvents(from, to);
    }

    @Override
    public CalendarEvent createEvent(CalendarEvent event) {
        if (event.getTitle() == null || event.getTitle().isBlank()) {
            throw new IllegalArgumentException("Event title is required");
        }
        if (event.getStartTime() == null || event.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        if (event.getStartTime().isAfter(event.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        return calendarPort.createEvent(event);
    }

    @Override
    public void deleteEvent(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID is required");
        }
        calendarPort.deleteEvent(eventId);
    }

    @Override
    public boolean isAvailable() {
        return calendarPort.isAvailable();
    }
}
