package com.localmind.domain.calendar.service;

import com.localmind.domain.calendar.model.CalendarEvent;
import com.localmind.domain.calendar.port.out.CalendarPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private CalendarPort calendarPort;

    @InjectMocks
    private CalendarService calendarService;

    @Test
    void listEvents_shouldReturnEvents() {
        Instant from = Instant.parse("2026-02-01T00:00:00Z");
        Instant to = Instant.parse("2026-02-28T23:59:59Z");

        CalendarEvent event = CalendarEvent.builder()
                .id("evt-1")
                .title("Meeting")
                .startTime(Instant.parse("2026-02-15T10:00:00Z"))
                .endTime(Instant.parse("2026-02-15T11:00:00Z"))
                .build();

        when(calendarPort.listEvents(from, to)).thenReturn(List.of(event));

        List<CalendarEvent> result = calendarService.listEvents(from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Meeting");
        verify(calendarPort).listEvents(from, to);
    }

    @Test
    void listEvents_shouldThrowWhenFromIsNull() {
        Instant to = Instant.parse("2026-02-28T23:59:59Z");

        assertThatThrownBy(() -> calendarService.listEvents(null, to))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");

        verify(calendarPort, never()).listEvents(any(), any());
    }

    @Test
    void listEvents_shouldThrowWhenToIsNull() {
        Instant from = Instant.parse("2026-02-01T00:00:00Z");

        assertThatThrownBy(() -> calendarService.listEvents(from, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");

        verify(calendarPort, never()).listEvents(any(), any());
    }

    @Test
    void listEvents_shouldThrowWhenFromIsAfterTo() {
        Instant from = Instant.parse("2026-03-01T00:00:00Z");
        Instant to = Instant.parse("2026-02-01T00:00:00Z");

        assertThatThrownBy(() -> calendarService.listEvents(from, to))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before");

        verify(calendarPort, never()).listEvents(any(), any());
    }

    @Test
    void createEvent_shouldCreateAndReturnEvent() {
        CalendarEvent event = CalendarEvent.builder()
                .title("Team Standup")
                .startTime(Instant.parse("2026-02-15T09:00:00Z"))
                .endTime(Instant.parse("2026-02-15T09:15:00Z"))
                .location("Room A")
                .attendees(List.of("alice@test.com"))
                .build();

        CalendarEvent created = CalendarEvent.builder()
                .id("evt-new")
                .title("Team Standup")
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location("Room A")
                .attendees(List.of("alice@test.com"))
                .build();

        when(calendarPort.createEvent(event)).thenReturn(created);

        CalendarEvent result = calendarService.createEvent(event);

        assertThat(result.getId()).isEqualTo("evt-new");
        assertThat(result.getTitle()).isEqualTo("Team Standup");
        verify(calendarPort).createEvent(event);
    }

    @Test
    void createEvent_shouldThrowWhenTitleIsBlank() {
        CalendarEvent event = CalendarEvent.builder()
                .title("")
                .startTime(Instant.parse("2026-02-15T09:00:00Z"))
                .endTime(Instant.parse("2026-02-15T10:00:00Z"))
                .build();

        assertThatThrownBy(() -> calendarService.createEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");

        verify(calendarPort, never()).createEvent(any());
    }

    @Test
    void createEvent_shouldThrowWhenStartTimeIsNull() {
        CalendarEvent event = CalendarEvent.builder()
                .title("Meeting")
                .startTime(null)
                .endTime(Instant.parse("2026-02-15T10:00:00Z"))
                .build();

        assertThatThrownBy(() -> calendarService.createEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");

        verify(calendarPort, never()).createEvent(any());
    }

    @Test
    void createEvent_shouldThrowWhenStartAfterEnd() {
        CalendarEvent event = CalendarEvent.builder()
                .title("Meeting")
                .startTime(Instant.parse("2026-02-15T12:00:00Z"))
                .endTime(Instant.parse("2026-02-15T10:00:00Z"))
                .build();

        assertThatThrownBy(() -> calendarService.createEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before");

        verify(calendarPort, never()).createEvent(any());
    }

    @Test
    void deleteEvent_shouldDelegateToPort() {
        calendarService.deleteEvent("evt-1");

        verify(calendarPort).deleteEvent("evt-1");
    }

    @Test
    void deleteEvent_shouldThrowWhenIdIsBlank() {
        assertThatThrownBy(() -> calendarService.deleteEvent(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");

        verify(calendarPort, never()).deleteEvent(any());
    }

    @Test
    void deleteEvent_shouldThrowWhenIdIsNull() {
        assertThatThrownBy(() -> calendarService.deleteEvent(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");

        verify(calendarPort, never()).deleteEvent(any());
    }

    @Test
    void isAvailable_shouldDelegateToPort() {
        when(calendarPort.isAvailable()).thenReturn(true);

        boolean result = calendarService.isAvailable();

        assertThat(result).isTrue();
        verify(calendarPort).isAvailable();
    }

    @Test
    void isAvailable_shouldReturnFalseWhenPortUnavailable() {
        when(calendarPort.isAvailable()).thenReturn(false);

        boolean result = calendarService.isAvailable();

        assertThat(result).isFalse();
    }
}
