package com.localmind.domain.common.port.out;

import com.localmind.domain.common.event.DomainEvent;

/**
 * Porta per la pubblicazione di eventi di dominio.
 * L'implementazione infrastrutturale si occupa del dispatching.
 *
 * Port for publishing domain events.
 * The infrastructure implementation handles the dispatching.
 */
public interface DomainEventPublisherPort {
    void publish(DomainEvent event);
}
