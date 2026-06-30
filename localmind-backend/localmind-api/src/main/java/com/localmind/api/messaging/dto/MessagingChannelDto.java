package com.localmind.api.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagingChannelDto {
    private String id;
    private String name;
    private String platform;
    private String botToken;
    private String webhookSecret;
    private boolean active;
    private String defaultSystemPrompt;
    private String defaultModel;
    private String defaultProvider;
    private boolean enableRag;
    private boolean enableToolCalling;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
