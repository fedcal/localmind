package com.localmind.api.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryDto {
    private String id;
    private String title;
    private int messageCount;
    private Set<String> tags;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;
}
