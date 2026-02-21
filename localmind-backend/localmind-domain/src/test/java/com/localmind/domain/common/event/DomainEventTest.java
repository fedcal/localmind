package com.localmind.domain.common.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventTest {

    // ========================================================================
    // DocumentIndexedEvent
    // ========================================================================

    @Test
    void testDocumentIndexedEvent_hasCorrectFields() {
        DocumentIndexedEvent event = new DocumentIndexedEvent("doc-1", "test.pdf", 5, "/docs");

        assertThat(event.getDocumentId()).isEqualTo("doc-1");
        assertThat(event.getDocumentTitle()).isEqualTo("test.pdf");
        assertThat(event.getChunkCount()).isEqualTo(5);
        assertThat(event.getSourceFolder()).isEqualTo("/docs");
        assertThat(event.getEventType()).isEqualTo("DOCUMENT_INDEXED");
    }

    @Test
    void testDocumentIndexedEvent_withNullSourceFolder() {
        DocumentIndexedEvent event = new DocumentIndexedEvent("doc-1", "file.txt", 3, null);

        assertThat(event.getSourceFolder()).isNull();
        assertThat(event.getEventType()).isEqualTo("DOCUMENT_INDEXED");
    }

    // ========================================================================
    // ConversationCompletedEvent
    // ========================================================================

    @Test
    void testConversationCompletedEvent_hasCorrectFields() {
        ConversationCompletedEvent event = new ConversationCompletedEvent(
                "conv-1", 4, "OLLAMA", "llama3", 150L);

        assertThat(event.getConversationId()).isEqualTo("conv-1");
        assertThat(event.getMessageCount()).isEqualTo(4);
        assertThat(event.getProvider()).isEqualTo("OLLAMA");
        assertThat(event.getModelName()).isEqualTo("llama3");
        assertThat(event.getTokensUsed()).isEqualTo(150L);
        assertThat(event.getEventType()).isEqualTo("CONVERSATION_COMPLETED");
    }

    @Test
    void testConversationCompletedEvent_withZeroTokens() {
        ConversationCompletedEvent event = new ConversationCompletedEvent(
                "conv-2", 1, "OPENAI", "gpt-4", 0L);

        assertThat(event.getTokensUsed()).isZero();
        assertThat(event.getEventType()).isEqualTo("CONVERSATION_COMPLETED");
    }

    // ========================================================================
    // EmbeddingGeneratedEvent
    // ========================================================================

    @Test
    void testEmbeddingGeneratedEvent_hasCorrectFields() {
        EmbeddingGeneratedEvent event = new EmbeddingGeneratedEvent("doc-1", "chunk-1", 768);

        assertThat(event.getDocumentId()).isEqualTo("doc-1");
        assertThat(event.getChunkId()).isEqualTo("chunk-1");
        assertThat(event.getVectorDimension()).isEqualTo(768);
        assertThat(event.getEventType()).isEqualTo("EMBEDDING_GENERATED");
    }

    @Test
    void testEmbeddingGeneratedEvent_withDifferentDimension() {
        EmbeddingGeneratedEvent event = new EmbeddingGeneratedEvent("doc-2", "chunk-5", 1536);

        assertThat(event.getVectorDimension()).isEqualTo(1536);
    }

    // ========================================================================
    // DocumentDeletedEvent
    // ========================================================================

    @Test
    void testDocumentDeletedEvent_hasCorrectFields() {
        DocumentDeletedEvent event = new DocumentDeletedEvent("doc-1", "report.pdf");

        assertThat(event.getDocumentId()).isEqualTo("doc-1");
        assertThat(event.getDocumentTitle()).isEqualTo("report.pdf");
        assertThat(event.getEventType()).isEqualTo("DOCUMENT_DELETED");
    }

    @Test
    void testDocumentDeletedEvent_withNullTitle() {
        DocumentDeletedEvent event = new DocumentDeletedEvent("doc-2", null);

        assertThat(event.getDocumentTitle()).isNull();
        assertThat(event.getEventType()).isEqualTo("DOCUMENT_DELETED");
    }

    // ========================================================================
    // PluginStateChangedEvent
    // ========================================================================

    @Test
    void testPluginStateChangedEvent_hasCorrectFields() {
        PluginStateChangedEvent event = new PluginStateChangedEvent("plugin-1", "DISABLED", "ACTIVE");

        assertThat(event.getPluginId()).isEqualTo("plugin-1");
        assertThat(event.getPreviousState()).isEqualTo("DISABLED");
        assertThat(event.getNewState()).isEqualTo("ACTIVE");
        assertThat(event.getEventType()).isEqualTo("PLUGIN_STATE_CHANGED");
    }

    @Test
    void testPluginStateChangedEvent_sameState() {
        PluginStateChangedEvent event = new PluginStateChangedEvent("plugin-2", "ACTIVE", "ACTIVE");

        assertThat(event.getPreviousState()).isEqualTo("ACTIVE");
        assertThat(event.getNewState()).isEqualTo("ACTIVE");
    }

    // ========================================================================
    // DomainEvent base class behavior / Comportamento classe base DomainEvent
    // ========================================================================

    @Test
    void testDomainEvent_generatesUniqueIds() {
        DocumentIndexedEvent event1 = new DocumentIndexedEvent("doc-1", "a.pdf", 1, null);
        DocumentIndexedEvent event2 = new DocumentIndexedEvent("doc-2", "b.pdf", 2, null);

        assertThat(event1.getEventId()).isNotNull();
        assertThat(event2.getEventId()).isNotNull();
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    void testDomainEvent_setsOccurredAt() {
        Instant before = Instant.now();
        DocumentDeletedEvent event = new DocumentDeletedEvent("doc-1", "test.pdf");
        Instant after = Instant.now();

        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getOccurredAt()).isAfterOrEqualTo(before);
        assertThat(event.getOccurredAt()).isBeforeOrEqualTo(after);
    }

    @Test
    void testDomainEvent_eventIdIsUuidFormat() {
        ConversationCompletedEvent event = new ConversationCompletedEvent(
                "conv-1", 1, "OLLAMA", "llama3", 50L);

        // UUID format: 8-4-4-4-12 hex characters
        assertThat(event.getEventId()).matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void testDomainEvent_differentTypesHaveDifferentEventTypes() {
        DocumentIndexedEvent indexed = new DocumentIndexedEvent("d1", "a.pdf", 1, null);
        DocumentDeletedEvent deleted = new DocumentDeletedEvent("d2", "b.pdf");
        ConversationCompletedEvent conv = new ConversationCompletedEvent("c1", 1, "OLLAMA", "m", 0);
        EmbeddingGeneratedEvent emb = new EmbeddingGeneratedEvent("d1", "ch1", 768);
        PluginStateChangedEvent plugin = new PluginStateChangedEvent("p1", "OFF", "ON");

        assertThat(indexed.getEventType()).isEqualTo("DOCUMENT_INDEXED");
        assertThat(deleted.getEventType()).isEqualTo("DOCUMENT_DELETED");
        assertThat(conv.getEventType()).isEqualTo("CONVERSATION_COMPLETED");
        assertThat(emb.getEventType()).isEqualTo("EMBEDDING_GENERATED");
        assertThat(plugin.getEventType()).isEqualTo("PLUGIN_STATE_CHANGED");
    }
}
