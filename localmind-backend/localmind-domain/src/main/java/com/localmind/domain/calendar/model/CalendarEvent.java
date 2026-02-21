package com.localmind.domain.calendar.model;

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
public class CalendarEvent {
    private String id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private String location;
    private List<String> attendees;
}
