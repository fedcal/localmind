package com.localmind.infrastructure.event.listener;

import com.localmind.domain.common.event.ConversationCompletedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class ConversationEventListenerTest {

    private final ConversationEventListener listener = new ConversationEventListener();

    @Test
    void testOnConversationCompleted_logsEvent() {
        ConversationCompletedEvent event = new ConversationCompletedEvent(
                "conv-1", 4, "OLLAMA", "llama3", 150L);

        assertThatCode(() -> listener.onConversationCompleted(event))
                .doesNotThrowAnyException();
    }

    @Test
    void testOnConversationCompleted_withZeroTokens() {
        ConversationCompletedEvent event = new ConversationCompletedEvent(
                "conv-2", 1, "OPENAI", "gpt-4", 0L);

        assertThatCode(() -> listener.onConversationCompleted(event))
                .doesNotThrowAnyException();
    }
}
