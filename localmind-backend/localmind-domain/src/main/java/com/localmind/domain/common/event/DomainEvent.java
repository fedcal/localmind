package com.localmind.domain.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Classe base astratta per tutti gli eventi di dominio.
 * Pura Java, senza dipendenze da framework.
 *
 * Abstract base class for all domain events.
 * Pure Java, no framework dependencies.
 */
public abstract class DomainEvent {
    private final String eventId;
    private final Instant occurredAt;
    private final String eventType;

    protected DomainEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
        this.eventType = eventType;
    }

    public String getEventId() { return eventId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getEventType() { return eventType; }
}
