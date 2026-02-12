package com.localmind.api.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
    private String content;
    private String model;
    private String provider;
    private String conversationId;
    private TokenUsageDto tokenUsage;
    private long latencyMs;
}
