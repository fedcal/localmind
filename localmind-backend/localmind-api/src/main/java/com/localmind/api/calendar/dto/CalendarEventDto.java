package com.localmind.api.calendar.dto;

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
public class CalendarEventDto {
    private String id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private String location;
    private List<String> attendees;
}
