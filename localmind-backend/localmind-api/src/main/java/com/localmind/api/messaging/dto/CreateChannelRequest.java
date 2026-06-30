package com.localmind.api.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChannelRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String platform;
    @NotBlank
    private String botToken;
    private String webhookSecret;
    private String defaultSystemPrompt;
    private String defaultModel;
    private String defaultProvider;
    private boolean enableRag;
    private boolean enableToolCalling;
}
