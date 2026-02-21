package com.localmind.domain.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {
    private String content;
    private String model;
    private LlmProvider provider;
    private TokenUsage tokenUsage;
    private long latencyMs;
}
