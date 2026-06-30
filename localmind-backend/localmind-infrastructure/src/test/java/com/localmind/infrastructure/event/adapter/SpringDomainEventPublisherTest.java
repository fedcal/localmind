package com.localmind.infrastructure.event.adapter;

import com.localmind.domain.common.event.DocumentDeletedEvent;
import com.localmind.domain.common.event.DocumentIndexedEvent;
import com.localmind.domain.common.event.DomainEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpringDomainEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private SpringDomainEventPublisher springDomainEventPublisher;

    @Test
    void testPublish_delegatesToApplicationEventPublisher() {
        // given
        DocumentIndexedEvent event = new DocumentIndexedEvent("doc-1", "test.pdf", 3, "/docs");

        // when
        springDomainEventPublisher.publish(event);

        // then
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        Object published = captor.getValue();
        assertThat(published).isInstanceOf(DocumentIndexedEvent.class);
        DocumentIndexedEvent publishedEvent = (DocumentIndexedEvent) published;
        assertThat(publishedEvent.getDocumentId()).isEqualTo("doc-1");
        assertThat(publishedEvent.getDocumentTitle()).isEqualTo("test.pdf");
        assertThat(publishedEvent.getChunkCount()).isEqualTo(3);
    }

    @Test
    void testPublish_preservesEventIdentity() {
        // given
        DocumentDeletedEvent event = new DocumentDeletedEvent("doc-2", "report.pdf");

        // when
        springDomainEventPublisher.publish(event);

        // then
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        // L'evento pubblicato deve essere esattamente lo stesso oggetto
        // The published event must be exactly the same object
        assertThat(captor.getValue()).isSameAs(event);
    }

    @Test
    void testPublish_worksWithAnyDomainEvent() {
        // given - Usa un evento diverso per verificare il polimorfismo
        // given - Use a different event to verify polymorphism
        DomainEvent event = new DocumentDeletedEvent("doc-3", "notes.txt");

        // when
        springDomainEventPublisher.publish(event);

        // then
        verify(applicationEventPublisher).publishEvent(event);
    }
}
