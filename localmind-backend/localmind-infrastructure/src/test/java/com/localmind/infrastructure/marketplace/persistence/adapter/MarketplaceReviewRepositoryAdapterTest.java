package com.localmind.infrastructure.marketplace.persistence.adapter;

import com.localmind.domain.marketplace.model.MarketplaceReview;
import com.localmind.infrastructure.marketplace.persistence.entity.MarketplaceReviewEntity;
import com.localmind.infrastructure.marketplace.persistence.repository.MarketplaceReviewJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketplaceReviewRepositoryAdapterTest {

    @Mock
    private MarketplaceReviewJpaRepository jpaRepository;

    @InjectMocks
    private MarketplaceReviewRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-01-15T10:30:00Z");

    @Test
    void testSave_mapsFieldsCorrectly() {
        MarketplaceReview review = MarketplaceReview.builder()
                .id(TEST_UUID.toString())
                .agentId("agent-1")
                .rating(5)
                .comment("Excellent!")
                .createdAt(NOW)
                .build();

        MarketplaceReviewEntity savedEntity = MarketplaceReviewEntity.builder()
                .id(TEST_UUID)
                .agentId("agent-1")
                .rating(5)
                .comment("Excellent!")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(MarketplaceReviewEntity.class))).thenReturn(savedEntity);

        MarketplaceReview result = adapter.save(review);

        ArgumentCaptor<MarketplaceReviewEntity> captor = ArgumentCaptor.forClass(MarketplaceReviewEntity.class);
        verify(jpaRepository).save(captor.capture());

        MarketplaceReviewEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getAgentId()).isEqualTo("agent-1");
        assertThat(captured.getRating()).isEqualTo(5);
        assertThat(captured.getComment()).isEqualTo("Excellent!");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getRating()).isEqualTo(5);
    }

    @Test
    void testFindByAgentId_returnsReviews() {
        UUID review1Id = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-234567890123");
        UUID review2Id = UUID.fromString("d4e5f6a7-b8c9-0123-defa-345678901234");

        List<MarketplaceReviewEntity> entities = List.of(
                MarketplaceReviewEntity.builder()
                        .id(review1Id).agentId("agent-1").rating(4).comment("Good").createdAt(NOW).build(),
                MarketplaceReviewEntity.builder()
                        .id(review2Id).agentId("agent-1").rating(5).comment("Great").createdAt(NOW).build()
        );

        when(jpaRepository.findByAgentId("agent-1")).thenReturn(entities);

        List<MarketplaceReview> result = adapter.findByAgentId("agent-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRating()).isEqualTo(4);
        assertThat(result.get(0).getComment()).isEqualTo("Good");
        assertThat(result.get(1).getRating()).isEqualTo(5);
        assertThat(result.get(1).getComment()).isEqualTo("Great");
        verify(jpaRepository).findByAgentId("agent-1");
    }

    @Test
    void testFindByAgentId_emptyList() {
        when(jpaRepository.findByAgentId("agent-2")).thenReturn(List.of());

        List<MarketplaceReview> result = adapter.findByAgentId("agent-2");

        assertThat(result).isEmpty();
    }

    @Test
    void testCalculateAverageRating_withReviews() {
        List<MarketplaceReviewEntity> entities = List.of(
                MarketplaceReviewEntity.builder()
                        .id(UUID.randomUUID()).agentId("agent-1").rating(4).createdAt(NOW).build(),
                MarketplaceReviewEntity.builder()
                        .id(UUID.randomUUID()).agentId("agent-1").rating(5).createdAt(NOW).build(),
                MarketplaceReviewEntity.builder()
                        .id(UUID.randomUUID()).agentId("agent-1").rating(3).createdAt(NOW).build()
        );

        when(jpaRepository.findByAgentId("agent-1")).thenReturn(entities);

        double avg = adapter.calculateAverageRating("agent-1");

        assertThat(avg).isEqualTo(4.0);
    }

    @Test
    void testCalculateAverageRating_noReviews() {
        when(jpaRepository.findByAgentId("agent-2")).thenReturn(List.of());

        double avg = adapter.calculateAverageRating("agent-2");

        assertThat(avg).isEqualTo(0.0);
    }
}
