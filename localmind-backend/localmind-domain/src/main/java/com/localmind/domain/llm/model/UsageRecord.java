package com.localmind.domain.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRecord {
    private String id;
    private LlmProvider provider;
    private String model;
    private TokenUsage tokenUsage;
    private double cost;
    private long latencyMs;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
