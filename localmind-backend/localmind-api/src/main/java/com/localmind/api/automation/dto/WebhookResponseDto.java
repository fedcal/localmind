package com.localmind.api.automation.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponseDto {
    private String id;
    private String name;
    private String url;
    private String eventType;
    private boolean active;
}
