package com.localmind.infrastructure.event.listener;

import com.localmind.domain.common.event.ConversationCompletedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class AnalyticsEventListenerTest {

    private final AnalyticsEventListener listener = new AnalyticsEventListener();

    @Test
    void testOnConversationCompleted_logsAnalytics() {
        ConversationCompletedEvent event = new ConversationCompletedEvent(
                "conv-1", 4, "OLLAMA", "llama3", 150L);

        assertThatCode(() -> listener.onConversationCompleted(event))
                .doesNotThrowAnyException();
    }

    @Test
    void testOnConversationCompleted_withNullConversationId() {
        ConversationCompletedEvent event = new ConversationCompletedEvent(
                null, 0, "OPENAI", "gpt-4", 0L);

        assertThatCode(() -> listener.onConversationCompleted(event))
                .doesNotThrowAnyException();
    }
}
