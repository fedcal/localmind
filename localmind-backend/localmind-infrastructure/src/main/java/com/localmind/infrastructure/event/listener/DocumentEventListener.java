package com.localmind.infrastructure.event.listener;

import com.localmind.domain.common.event.DocumentDeletedEvent;
import com.localmind.domain.common.event.DocumentIndexedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener per gli eventi relativi ai documenti.
 * Gestisce il logging degli eventi di indicizzazione e cancellazione.
 *
 * Listener for document-related events.
 * Handles logging for indexing and deletion events.
 */
@Component
public class DocumentEventListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentEventListener.class);

    @EventListener
    public void onDocumentIndexed(DocumentIndexedEvent event) {
        log.info("Document indexed: {} ({} chunks) [eventId={}]",
                event.getDocumentTitle(), event.getChunkCount(), event.getEventId());
    }

    @EventListener
    public void onDocumentDeleted(DocumentDeletedEvent event) {
        log.info("Document deleted: {} [eventId={}]",
                event.getDocumentTitle(), event.getEventId());
    }
}
