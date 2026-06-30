package com.localmind.domain.automation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationEvent {
    private AutomationEventType type;
    private Map<String, Object> payload;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
