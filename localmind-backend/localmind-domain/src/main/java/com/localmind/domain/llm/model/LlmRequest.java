package com.localmind.domain.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {
    private List<ChatMessage> messages;
    private LlmProvider provider;
    private String model;
    @Builder.Default
    private double temperature = 0.7;
    private int maxTokens;
    private String conversationId;
    private boolean stream;
}
