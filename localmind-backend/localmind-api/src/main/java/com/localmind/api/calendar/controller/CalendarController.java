package com.localmind.api.calendar.controller;

import com.localmind.api.calendar.dto.CalendarEventDto;
import com.localmind.api.calendar.dto.CreateCalendarEventRequest;
import com.localmind.domain.calendar.model.CalendarEvent;
import com.localmind.domain.calendar.port.in.CalendarUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar")
@Tag(name = "Calendar", description = "Calendar event management via CalDAV")
@ConditionalOnProperty(name = "localmind.calendar.enabled", havingValue = "true")
public class CalendarController {

    private final CalendarUseCase calendarUseCase;

    public CalendarController(CalendarUseCase calendarUseCase) {
        this.calendarUseCase = calendarUseCase;
    }

    @GetMapping("/events")
    @Operation(summary = "Lista eventi calendario / List calendar events")
    @ApiResponse(responseCode = "200", description = "Lista eventi / Event list")
    public ResponseEntity<List<CalendarEventDto>> listEvents(
            @RequestParam Instant from,
            @RequestParam Instant to) {
        List<CalendarEventDto> events = calendarUseCase.listEvents(from, to).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(events);
    }

    @PostMapping("/events")
    @Operation(summary = "Crea evento calendario / Create calendar event")
    @ApiResponse(responseCode = "200", description = "Evento creato / Event created")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<CalendarEventDto> createEvent(
            @Valid @RequestBody CreateCalendarEventRequest request) {
        CalendarEvent event = CalendarEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .attendees(request.getAttendees())
                .build();

        CalendarEvent created = calendarUseCase.createEvent(event);
        return ResponseEntity.ok(toDto(created));
    }

    @DeleteMapping("/events/{id}")
    @Operation(summary = "Elimina evento calendario / Delete calendar event")
    @ApiResponse(responseCode = "204", description = "Evento eliminato / Event deleted")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        calendarUseCase.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    private CalendarEventDto toDto(CalendarEvent event) {
        return CalendarEventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .attendees(event.getAttendees())
                .build();
    }
}
