package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.InsightResult;
import com.localmind.infrastructure.persistence.entity.mcp.InsightResultEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaInsightResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class InsightRepositoryAdapterTest {

    @Mock
    private JpaInsightResultRepository jpaRepository;

    private InsightRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-13T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new InsightRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsDomainToEntityCorrectly() {
        InsightResult domain = InsightResult.builder()
                .id(TEST_UUID.toString())
                .question("How is velocity?")
                .resultJson("{\"insight\":\"...\"}")
                .createdAt(NOW)
                .build();

        InsightResultEntity savedEntity = InsightResultEntity.builder()
                .id(TEST_UUID)
                .question("How is velocity?")
                .resultJson("{\"insight\":\"...\"}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(InsightResultEntity.class))).thenReturn(savedEntity);

        InsightResult result = adapter.save(domain);

        ArgumentCaptor<InsightResultEntity> captor = ArgumentCaptor.forClass(InsightResultEntity.class);
        verify(jpaRepository).save(captor.capture());

        InsightResultEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getQuestion()).isEqualTo("How is velocity?");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getQuestion()).isEqualTo("How is velocity?");
    }

    @Test
    void findAll_returnsMappedList() {
        InsightResultEntity entity = InsightResultEntity.builder()
                .id(TEST_UUID)
                .question("Test question")
                .resultJson("{}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.findAll()).thenReturn(List.of(entity));

        List<InsightResult> results = adapter.findAll();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getQuestion()).isEqualTo("Test question");
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        InsightResult domain = InsightResult.builder()
                .question("Question")
                .resultJson("{}")
                .createdAt(NOW)
                .build();

        InsightResultEntity savedEntity = InsightResultEntity.builder()
                .id(TEST_UUID)
                .question("Question")
                .resultJson("{}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(InsightResultEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<InsightResultEntity> captor = ArgumentCaptor.forClass(InsightResultEntity.class);
        verify(jpaRepository).save(captor.capture());

        InsightResultEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();
    }
}
