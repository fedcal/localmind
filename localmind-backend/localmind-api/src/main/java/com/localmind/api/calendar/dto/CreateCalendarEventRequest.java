package com.localmind.api.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCalendarEventRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    @NotNull(message = "Start time is required")
    private Instant startTime;
    @NotNull(message = "End time is required")
    private Instant endTime;
    private String location;
    private List<String> attendees;
}
