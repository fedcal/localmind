package com.localmind.api.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private String id;
    private String title;
    private String systemPrompt;
    private Integer maxContextMessages;
    private Set<String> tags;
    private Map<String, Object> metadata;
    private List<ChatMessageDto> messages;
    private Instant createdAt;
    private Instant updatedAt;
}
