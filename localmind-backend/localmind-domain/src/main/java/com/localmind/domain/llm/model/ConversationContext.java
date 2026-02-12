package com.localmind.domain.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.*;

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
    private String systemPrompt;
    private Instant updatedAt;
    private Integer maxContextMessages;
    @Builder.Default
    private Set<String> tags = new HashSet<>();
    private Map<String, Object> metadata;

    public void addMessage(ChatMessage message) {
        messages.add(message);
        updatedAt = Instant.now();
    }

    /**
     * Restituisce una finestra di contesto limitata dei messaggi.
     * Prende gli ultimi N messaggi dove N = maxContextMessages (o defaultMax se null).
     * Il primo messaggio SYSTEM viene sempre preservato se presente.
     */
    public List<ChatMessage> getContextWindow(int defaultMax) {
        int max = maxContextMessages != null ? maxContextMessages : defaultMax;
        if (messages == null || messages.isEmpty() || messages.size() <= max) {
            return messages != null ? messages : new ArrayList<>();
        }

        List<ChatMessage> window = new ArrayList<>();
        int start = messages.size() - max;
        if (start < 0) start = 0;

        window.addAll(messages.subList(start, messages.size()));
        return window;
    }
}
