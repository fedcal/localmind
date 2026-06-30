package com.localmind.infrastructure.event.listener;

import com.localmind.domain.common.event.DocumentDeletedEvent;
import com.localmind.domain.common.event.DocumentIndexedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class DocumentEventListenerTest {

    private final DocumentEventListener listener = new DocumentEventListener();

    @Test
    void testOnDocumentIndexed_logsEvent() {
        // given
        DocumentIndexedEvent event = new DocumentIndexedEvent("doc-1", "test.pdf", 5, "/docs");

        // when / then - Verifica che non venga lanciata nessuna eccezione
        // when / then - Verify no exception is thrown
        assertThatCode(() -> listener.onDocumentIndexed(event))
                .doesNotThrowAnyException();
    }

    @Test
    void testOnDocumentIndexed_withZeroChunks() {
        DocumentIndexedEvent event = new DocumentIndexedEvent("doc-2", "empty.txt", 0, null);

        assertThatCode(() -> listener.onDocumentIndexed(event))
                .doesNotThrowAnyException();
    }

    @Test
    void testOnDocumentDeleted_logsEvent() {
        // given
        DocumentDeletedEvent event = new DocumentDeletedEvent("doc-1", "report.pdf");

        // when / then
        assertThatCode(() -> listener.onDocumentDeleted(event))
                .doesNotThrowAnyException();
    }

    @Test
    void testOnDocumentDeleted_withNullTitle() {
        DocumentDeletedEvent event = new DocumentDeletedEvent("doc-2", null);

        assertThatCode(() -> listener.onDocumentDeleted(event))
                .doesNotThrowAnyException();
    }
}
