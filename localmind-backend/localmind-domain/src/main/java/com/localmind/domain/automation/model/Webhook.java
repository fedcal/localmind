package com.localmind.domain.automation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {
    private String id;
    private String name;
    private String url;
    private AutomationEventType eventType;
    @Builder.Default
    private boolean active = true;
}
