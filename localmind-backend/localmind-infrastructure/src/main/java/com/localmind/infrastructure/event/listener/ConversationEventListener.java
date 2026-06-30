package com.localmind.infrastructure.event.listener;

import com.localmind.domain.common.event.ConversationCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener per gli eventi relativi alle conversazioni LLM.
 * Gestisce il logging delle conversazioni completate.
 *
 * Listener for LLM conversation-related events.
 * Handles logging for completed conversations.
 */
@Component
public class ConversationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ConversationEventListener.class);

    @EventListener
    public void onConversationCompleted(ConversationCompletedEvent event) {
        log.info("Conversation completed: provider={}, model={}, tokens={} [eventId={}]",
                event.getProvider(), event.getModelName(), event.getTokensUsed(), event.getEventId());
    }
}
