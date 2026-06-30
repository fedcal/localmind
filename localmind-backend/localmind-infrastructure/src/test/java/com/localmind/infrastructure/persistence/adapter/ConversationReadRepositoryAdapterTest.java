package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.llm.model.ConversationSummary;
import com.localmind.infrastructure.persistence.entity.ConversationSummaryEntity;
import com.localmind.infrastructure.persistence.repository.ConversationSummaryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationReadRepositoryAdapterTest {

    @Mock
    private ConversationSummaryJpaRepository jpaRepository;

    private ConversationReadRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-21T10:00:00Z");
    private final Instant LATER = Instant.parse("2026-02-21T11:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new ConversationReadRepositoryAdapter(jpaRepository);
    }

    @Test
    void testFindRecentSummaries_delegatesToJpa() {
        ConversationSummaryEntity entity = buildEntity(TEST_UUID, "Test conv", 5,
                "Last message", "important,work", NOW, LATER);

        when(jpaRepository.findAllByOrderByUpdatedAtDesc(any(Pageable.class)))
                .thenReturn(List.of(entity));

        List<ConversationSummary> result = adapter.findRecentSummaries(10);

        assertThat(result).hasSize(1);
        verify(jpaRepository).findAllByOrderByUpdatedAtDesc(any(Pageable.class));
    }

    @Test
    void testFindRecentSummaries_mapsCorrectly() {
        ConversationSummaryEntity entity = buildEntity(TEST_UUID, "Test conv", 5,
                "Last message preview", "important,work", NOW, LATER);

        when(jpaRepository.findAllByOrderByUpdatedAtDesc(any(Pageable.class)))
                .thenReturn(List.of(entity));

        List<ConversationSummary> result = adapter.findRecentSummaries(10);

        assertThat(result).hasSize(1);
        ConversationSummary summary = result.get(0);
        assertThat(summary.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(summary.getTitle()).isEqualTo("Test conv");
        assertThat(summary.getMessageCount()).isEqualTo(5);
        assertThat(summary.getLastMessagePreview()).isEqualTo("Last message preview");
        assertThat(summary.getTags()).containsExactly("important", "work");
        assertThat(summary.getCreatedAt()).isEqualTo(NOW);
        assertThat(summary.getUpdatedAt()).isEqualTo(LATER);
    }

    @Test
    void testSearchSummaries_delegatesToJpa() {
        ConversationSummaryEntity entity = buildEntity(TEST_UUID, "Java tutorial", 10,
                "About Java streams", "programming", NOW, LATER);

        when(jpaRepository.findByTitleContainingIgnoreCase("Java"))
                .thenReturn(List.of(entity));

        List<ConversationSummary> result = adapter.searchSummaries("Java");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java tutorial");
        verify(jpaRepository).findByTitleContainingIgnoreCase("Java");
    }

    @Test
    void testFindSummaryById_found() {
        ConversationSummaryEntity entity = buildEntity(TEST_UUID, "Found conv", 3,
                "Preview", "tag1", NOW, LATER);

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<ConversationSummary> result = adapter.findSummaryById(TEST_UUID.toString());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get().getTitle()).isEqualTo("Found conv");
        assertThat(result.get().getMessageCount()).isEqualTo(3);
        verify(jpaRepository).findById(TEST_UUID);
    }

    @Test
    void testFindSummaryById_notFound() {
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        Optional<ConversationSummary> result = adapter.findSummaryById(TEST_UUID.toString());

        assertThat(result).isEmpty();
        verify(jpaRepository).findById(TEST_UUID);
    }

    @Test
    void testCountAll() {
        when(jpaRepository.count()).thenReturn(42L);

        long count = adapter.countAll();

        assertThat(count).isEqualTo(42L);
        verify(jpaRepository).count();
    }

    private ConversationSummaryEntity buildEntity(UUID id, String title, int messageCount,
                                                    String lastMessagePreview, String tags,
                                                    Instant createdAt, Instant updatedAt) {
        return ConversationSummaryEntity.builder()
                .id(id)
                .title(title)
                .messageCount(messageCount)
                .lastMessagePreview(lastMessagePreview)
                .tags(tags)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
