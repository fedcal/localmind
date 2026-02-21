package com.localmind.domain.llm.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationSummaryTest {

    @Test
    void testBuilder_setsAllFields() {
        Instant now = Instant.parse("2026-02-21T10:00:00Z");

        ConversationSummary summary = ConversationSummary.builder()
                .id("cs-1")
                .title("Test conversation")
                .messageCount(5)
                .lastMessagePreview("This is the last message")
                .tags(List.of("important", "work"))
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(summary.getId()).isEqualTo("cs-1");
        assertThat(summary.getTitle()).isEqualTo("Test conversation");
        assertThat(summary.getMessageCount()).isEqualTo(5);
        assertThat(summary.getLastMessagePreview()).isEqualTo("This is the last message");
        assertThat(summary.getTags()).containsExactly("important", "work");
        assertThat(summary.getCreatedAt()).isEqualTo(now);
        assertThat(summary.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testDefaultValues() {
        ConversationSummary summary = new ConversationSummary();

        assertThat(summary.getId()).isNull();
        assertThat(summary.getTitle()).isNull();
        assertThat(summary.getMessageCount()).isEqualTo(0);
        assertThat(summary.getLastMessagePreview()).isNull();
        assertThat(summary.getTags()).isEmpty();
        assertThat(summary.getCreatedAt()).isNull();
        assertThat(summary.getUpdatedAt()).isNull();
    }

    @Test
    void testBuilder_withDefaultTags() {
        ConversationSummary summary = ConversationSummary.builder()
                .id("cs-2")
                .title("No tags")
                .messageCount(1)
                .build();

        assertThat(summary.getTags()).isNotNull();
        assertThat(summary.getTags()).isEmpty();
    }

    @Test
    void testSettersAndGetters() {
        Instant now = Instant.now();
        ConversationSummary summary = new ConversationSummary();
        summary.setId("cs-3");
        summary.setTitle("Updated title");
        summary.setMessageCount(10);
        summary.setLastMessagePreview("Preview text");
        summary.setTags(List.of("tag1"));
        summary.setCreatedAt(now);
        summary.setUpdatedAt(now);

        assertThat(summary.getId()).isEqualTo("cs-3");
        assertThat(summary.getTitle()).isEqualTo("Updated title");
        assertThat(summary.getMessageCount()).isEqualTo(10);
        assertThat(summary.getLastMessagePreview()).isEqualTo("Preview text");
        assertThat(summary.getTags()).containsExactly("tag1");
        assertThat(summary.getCreatedAt()).isEqualTo(now);
        assertThat(summary.getUpdatedAt()).isEqualTo(now);
    }
}
