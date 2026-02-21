package com.localmind.infrastructure.event.adapter;

import com.localmind.domain.common.event.DomainEvent;
import com.localmind.domain.common.port.out.DomainEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Implementazione Spring dell'interfaccia DomainEventPublisherPort.
 * Delega la pubblicazione degli eventi al sistema ApplicationEvent di Spring.
 *
 * Spring implementation of the DomainEventPublisherPort interface.
 * Delegates event publishing to Spring's ApplicationEvent system.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {} [{}]", event.getEventType(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }
}
