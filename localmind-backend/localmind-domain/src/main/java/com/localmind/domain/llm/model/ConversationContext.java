package com.localmind.domain.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContext {
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String title;
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();
    @Builder.Default
    private Instant createdAt = Instant.now();
    private Instant updatedAt;

    public void addMessage(ChatMessage message) {
        messages.add(message);
        updatedAt = Instant.now();
    }
}
