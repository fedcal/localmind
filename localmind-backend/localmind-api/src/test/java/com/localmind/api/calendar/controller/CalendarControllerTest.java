package com.localmind.api.calendar.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.calendar.dto.CreateCalendarEventRequest;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.domain.calendar.model.CalendarEvent;
import com.localmind.domain.calendar.port.in.CalendarUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

class CalendarControllerTest {

    private MockMvc mockMvc;
    private CalendarUseCase calendarUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        calendarUseCase = mock(CalendarUseCase.class);
        CalendarController controller = new CalendarController(calendarUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void listEvents_shouldReturnEvents() throws Exception {
        Instant from = Instant.parse("2026-02-01T00:00:00Z");
        Instant to = Instant.parse("2026-02-28T23:59:59Z");

        CalendarEvent event = CalendarEvent.builder()
                .id("evt-1")
                .title("Team Meeting")
                .description("Weekly sync")
                .startTime(Instant.parse("2026-02-15T10:00:00Z"))
                .endTime(Instant.parse("2026-02-15T11:00:00Z"))
                .location("Room A")
                .attendees(List.of("alice@test.com"))
                .build();

        when(calendarUseCase.listEvents(from, to)).thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/calendar/events")
                        .param("from", "2026-02-01T00:00:00Z")
                        .param("to", "2026-02-28T23:59:59Z")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("evt-1"))
                .andExpect(jsonPath("$[0].title").value("Team Meeting"))
                .andExpect(jsonPath("$[0].location").value("Room A"))
                .andExpect(jsonPath("$[0].attendees[0]").value("alice@test.com"));

        verify(calendarUseCase).listEvents(from, to);
    }

    @Test
    void listEvents_shouldReturnEmptyList() throws Exception {
        Instant from = Instant.parse("2026-03-01T00:00:00Z");
        Instant to = Instant.parse("2026-03-31T23:59:59Z");

        when(calendarUseCase.listEvents(from, to)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/calendar/events")
                        .param("from", "2026-03-01T00:00:00Z")
                        .param("to", "2026-03-31T23:59:59Z")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createEvent_shouldReturnCreatedEvent() throws Exception {
        CreateCalendarEventRequest request = CreateCalendarEventRequest.builder()
                .title("New Meeting")
                .description("Project review")
                .startTime(Instant.parse("2026-02-20T14:00:00Z"))
                .endTime(Instant.parse("2026-02-20T15:00:00Z"))
                .location("Room B")
                .attendees(List.of("bob@test.com"))
                .build();

        CalendarEvent created = CalendarEvent.builder()
                .id("evt-new")
                .title("New Meeting")
                .description("Project review")
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location("Room B")
                .attendees(List.of("bob@test.com"))
                .build();

        when(calendarUseCase.createEvent(any(CalendarEvent.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/calendar/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("evt-new"))
                .andExpect(jsonPath("$.title").value("New Meeting"))
                .andExpect(jsonPath("$.description").value("Project review"))
                .andExpect(jsonPath("$.location").value("Room B"));

        verify(calendarUseCase).createEvent(any(CalendarEvent.class));
    }

    @Test
    void createEvent_shouldReturn400WhenTitleIsBlank() throws Exception {
        CreateCalendarEventRequest request = CreateCalendarEventRequest.builder()
                .title("")
                .startTime(Instant.parse("2026-02-20T14:00:00Z"))
                .endTime(Instant.parse("2026-02-20T15:00:00Z"))
                .build();

        mockMvc.perform(post("/api/v1/calendar/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calendarUseCase, never()).createEvent(any());
    }

    @Test
    void deleteEvent_shouldReturn204() throws Exception {
        doNothing().when(calendarUseCase).deleteEvent("evt-1");

        mockMvc.perform(delete("/api/v1/calendar/events/evt-1"))
                .andExpect(status().isNoContent());

        verify(calendarUseCase).deleteEvent("evt-1");
    }

    @Test
    void deleteEvent_shouldCallUseCaseWithCorrectId() throws Exception {
        doNothing().when(calendarUseCase).deleteEvent("evt-special-123");

        mockMvc.perform(delete("/api/v1/calendar/events/evt-special-123"))
                .andExpect(status().isNoContent());

        verify(calendarUseCase).deleteEvent("evt-special-123");
    }
}
