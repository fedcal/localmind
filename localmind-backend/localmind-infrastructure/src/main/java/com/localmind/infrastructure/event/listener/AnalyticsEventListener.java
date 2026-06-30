package com.localmind.infrastructure.event.listener;

import com.localmind.domain.common.event.ConversationCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener per la raccolta di dati analitici dagli eventi di dominio.
 * Attualmente registra le metriche nel log; in futuro potrebbe scrivere
 * su un archivio di metriche dedicato.
 *
 * Listener for collecting analytics data from domain events.
 * Currently logs metrics; in future it could write to a dedicated metrics store.
 */
@Component
public class AnalyticsEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventListener.class);

    @EventListener
    public void onConversationCompleted(ConversationCompletedEvent event) {
        log.info("Analytics: conversation={}, provider={}, model={}, tokens={}, messageCount={} [eventId={}]",
                event.getConversationId(),
                event.getProvider(),
                event.getModelName(),
                event.getTokensUsed(),
                event.getMessageCount(),
                event.getEventId());
    }
}
