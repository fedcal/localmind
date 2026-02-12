package com.localmind.api.llm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDto {
    @NotBlank(message = "Message is required")
    private String message;
    private String provider;
    private String model;
    private String conversationId;
    private String systemPrompt;
    @Builder.Default
    private double temperature = 0.7;
    private int maxTokens;
    private boolean enableToolCalling;
}
