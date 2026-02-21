package com.localmind.infrastructure.event.listener;

import com.localmind.domain.automation.model.AutomationEvent;
import com.localmind.domain.automation.model.AutomationEventType;
import com.localmind.domain.automation.port.in.AutomationUseCase;
import com.localmind.domain.common.event.DocumentIndexedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Listener che inoltra eventi di dominio ai webhook configurati.
 * Usa @Async per non bloccare il thread principale.
 *
 * Listener that forwards domain events to configured webhooks.
 * Uses @Async to avoid blocking the main thread.
 */
@Component
public class WebhookEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebhookEventListener.class);

    private final Optional<AutomationUseCase> automationUseCase;

    public WebhookEventListener(Optional<AutomationUseCase> automationUseCase) {
        this.automationUseCase = automationUseCase;
    }

    @Async("eventExecutor")
    @EventListener
    public void onDocumentIndexed(DocumentIndexedEvent event) {
        if (automationUseCase.isEmpty()) {
            log.debug("AutomationUseCase not available, skipping webhook for event [{}]", event.getEventId());
            return;
        }

        try {
            AutomationEvent automationEvent = AutomationEvent.builder()
                    .type(AutomationEventType.DOCUMENT_INDEXED)
                    .payload(Map.of(
                            "documentId", event.getDocumentId(),
                            "documentTitle", event.getDocumentTitle(),
                            "chunkCount", event.getChunkCount(),
                            "sourceFolder", event.getSourceFolder() != null ? event.getSourceFolder() : ""
                    ))
                    .build();

            automationUseCase.get().trigger(automationEvent);
            log.debug("Webhook triggered for document indexed event [{}]", event.getEventId());
        } catch (Exception e) {
            log.warn("Failed to trigger webhook for document indexed event [{}]: {}",
                    event.getEventId(), e.getMessage());
        }
    }
}
