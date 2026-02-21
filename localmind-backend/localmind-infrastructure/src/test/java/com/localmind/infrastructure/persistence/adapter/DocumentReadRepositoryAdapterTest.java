package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.document.model.DocumentStats;
import com.localmind.infrastructure.persistence.entity.DocumentStatsCacheEntity;
import com.localmind.infrastructure.persistence.repository.DocumentStatsCacheJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentReadRepositoryAdapterTest {

    @Mock
    private DocumentStatsCacheJpaRepository jpaRepository;

    private DocumentReadRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new DocumentReadRepositoryAdapter(jpaRepository);
    }

    @Test
    void testFindAllWithStats_mapsCorrectly() {
        DocumentStatsCacheEntity entity = buildEntity(TEST_UUID, "report.pdf", "INDEXED",
                10, 2048L, "application/pdf", 0.95, NOW);

        when(jpaRepository.findAll()).thenReturn(List.of(entity));

        List<DocumentStats> result = adapter.findAllWithStats();

        assertThat(result).hasSize(1);
        DocumentStats stats = result.get(0);
        assertThat(stats.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(stats.getTitle()).isEqualTo("report.pdf");
        assertThat(stats.getStatus()).isEqualTo("INDEXED");
        assertThat(stats.getChunkCount()).isEqualTo(10);
        assertThat(stats.getTotalSize()).isEqualTo(2048L);
        assertThat(stats.getMimeType()).isEqualTo("application/pdf");
        assertThat(stats.getOcrConfidence()).isEqualTo(0.95);
        assertThat(stats.getCreatedAt()).isEqualTo(NOW);
        verify(jpaRepository).findAll();
    }

    @Test
    void testGetTypeDistribution() {
        List<Object[]> rows = List.of(
                new Object[]{"application/pdf", 5L},
                new Object[]{"text/plain", 3L}
        );

        when(jpaRepository.getTypeDistribution()).thenReturn(rows);

        Map<String, Long> result = adapter.getTypeDistribution();

        assertThat(result).hasSize(2);
        assertThat(result).containsEntry("application/pdf", 5L);
        assertThat(result).containsEntry("text/plain", 3L);
        verify(jpaRepository).getTypeDistribution();
    }

    @Test
    void testGetStatusDistribution() {
        List<Object[]> rows = List.of(
                new Object[]{"INDEXED", 6L},
                new Object[]{"PENDING", 2L}
        );

        when(jpaRepository.getStatusDistribution()).thenReturn(rows);

        Map<String, Long> result = adapter.getStatusDistribution();

        assertThat(result).hasSize(2);
        assertThat(result).containsEntry("INDEXED", 6L);
        assertThat(result).containsEntry("PENDING", 2L);
        verify(jpaRepository).getStatusDistribution();
    }

    @Test
    void testCountAll() {
        when(jpaRepository.count()).thenReturn(15L);

        long count = adapter.countAll();

        assertThat(count).isEqualTo(15L);
        verify(jpaRepository).count();
    }

    @Test
    void testFindAllWithStats_emptyList() {
        when(jpaRepository.findAll()).thenReturn(List.of());

        List<DocumentStats> result = adapter.findAllWithStats();

        assertThat(result).isEmpty();
        verify(jpaRepository).findAll();
    }

    private DocumentStatsCacheEntity buildEntity(UUID id, String title, String status,
                                                   int chunkCount, long totalSize,
                                                   String mimeType, Double ocrConfidence,
                                                   Instant createdAt) {
        return DocumentStatsCacheEntity.builder()
                .id(id)
                .title(title)
                .status(status)
                .chunkCount(chunkCount)
                .totalSize(totalSize)
                .mimeType(mimeType)
                .ocrConfidence(ocrConfidence)
                .createdAt(createdAt)
                .build();
    }
}
