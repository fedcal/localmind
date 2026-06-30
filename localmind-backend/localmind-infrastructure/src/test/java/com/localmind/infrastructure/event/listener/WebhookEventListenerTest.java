package com.localmind.infrastructure.event.listener;

import com.localmind.domain.automation.model.AutomationEvent;
import com.localmind.domain.automation.port.in.AutomationUseCase;
import com.localmind.domain.common.event.DocumentIndexedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookEventListenerTest {

    @Mock
    private AutomationUseCase automationUseCase;

    @Test
    void testOnDocumentIndexed_triggersAutomation() {
        // given
        WebhookEventListener listener = new WebhookEventListener(Optional.of(automationUseCase));
        DocumentIndexedEvent event = new DocumentIndexedEvent("doc-1", "test.pdf", 3, "/docs");

        // when
        listener.onDocumentIndexed(event);

        // then
        ArgumentCaptor<AutomationEvent> captor = ArgumentCaptor.forClass(AutomationEvent.class);
        verify(automationUseCase).trigger(captor.capture());

        AutomationEvent automationEvent = captor.getValue();
        assertThat(automationEvent.getPayload()).containsEntry("documentId", "doc-1");
        assertThat(automationEvent.getPayload()).containsEntry("documentTitle", "test.pdf");
        assertThat(automationEvent.getPayload()).containsEntry("chunkCount", 3);
    }

    @Test
    void testOnDocumentIndexed_withNullSourceFolder_usesEmptyString() {
        // given
        WebhookEventListener listener = new WebhookEventListener(Optional.of(automationUseCase));
        DocumentIndexedEvent event = new DocumentIndexedEvent("doc-1", "test.pdf", 3, null);

        // when
        listener.onDocumentIndexed(event);

        // then
        ArgumentCaptor<AutomationEvent> captor = ArgumentCaptor.forClass(AutomationEvent.class);
        verify(automationUseCase).trigger(captor.capture());
        assertThat(captor.getValue().getPayload()).containsEntry("sourceFolder", "");
    }

    @Test
    void testOnDocumentIndexed_withoutAutomationUseCase_doesNotThrow() {
        // given - AutomationUseCase non disponibile / not available
        WebhookEventListener listener = new WebhookEventListener(Optional.empty());
        DocumentIndexedEvent event = new DocumentIndexedEvent("doc-1", "test.pdf", 3, "/docs");

        // when / then
        assertThatCode(() -> listener.onDocumentIndexed(event))
                .doesNotThrowAnyException();
    }

    @Test
    void testOnDocumentIndexed_automationThrowsException_doesNotPropagate() {
        // given
        WebhookEventListener listener = new WebhookEventListener(Optional.of(automationUseCase));
        DocumentIndexedEvent event = new DocumentIndexedEvent("doc-1", "test.pdf", 3, "/docs");
        doThrow(new RuntimeException("Webhook call failed")).when(automationUseCase).trigger(any());

        // when / then - L'eccezione non deve propagarsi / Exception must not propagate
        assertThatCode(() -> listener.onDocumentIndexed(event))
                .doesNotThrowAnyException();
    }
}
