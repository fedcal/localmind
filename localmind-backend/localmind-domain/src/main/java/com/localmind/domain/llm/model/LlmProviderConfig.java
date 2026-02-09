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
public class LlmProviderConfig {
    private String id;
    private String name;
    private LlmProvider type;
    private String baseUrl;
    private String apiKey;
    private String defaultModel;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
