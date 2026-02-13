package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.LogAnalysis;
import com.localmind.infrastructure.persistence.entity.mcp.LogAnalysisEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaLogAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogAnalysisRepositoryAdapterTest {

    @Mock
    private JpaLogAnalysisRepository jpaRepository;

    private LogAnalysisRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new LogAnalysisRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsDomainToEntityCorrectly() {
        LogAnalysis domain = LogAnalysis.builder()
                .id(TEST_UUID.toString())
                .totalLines(150)
                .levelsJson("{ERROR=5, INFO=100, WARN=45}")
                .topErrorsJson("[{message=Connection failed, count=5}]")
                .createdAt(NOW)
                .build();

        LogAnalysisEntity savedEntity = LogAnalysisEntity.builder()
                .id(TEST_UUID)
                .totalLines(150)
                .levels("{ERROR=5, INFO=100, WARN=45}")
                .topErrors("[{message=Connection failed, count=5}]")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(LogAnalysisEntity.class))).thenReturn(savedEntity);

        LogAnalysis result = adapter.save(domain);

        ArgumentCaptor<LogAnalysisEntity> captor = ArgumentCaptor.forClass(LogAnalysisEntity.class);
        verify(jpaRepository).save(captor.capture());

        LogAnalysisEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getTotalLines()).isEqualTo(150);
        assertThat(captured.getLevels()).isEqualTo("{ERROR=5, INFO=100, WARN=45}");
        assertThat(captured.getTopErrors()).isEqualTo("[{message=Connection failed, count=5}]");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getTotalLines()).isEqualTo(150);
        assertThat(result.getLevelsJson()).isEqualTo("{ERROR=5, INFO=100, WARN=45}");
        assertThat(result.getTopErrorsJson()).isEqualTo("[{message=Connection failed, count=5}]");
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        LogAnalysis domain = LogAnalysis.builder()
                .id(null)
                .totalLines(10)
                .levelsJson("{INFO=10}")
                .topErrorsJson("[]")
                .createdAt(NOW)
                .build();

        UUID generatedUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        LogAnalysisEntity savedEntity = LogAnalysisEntity.builder()
                .id(generatedUuid)
                .totalLines(10)
                .levels("{INFO=10}")
                .topErrors("[]")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(LogAnalysisEntity.class))).thenReturn(savedEntity);

        LogAnalysis result = adapter.save(domain);

        ArgumentCaptor<LogAnalysisEntity> captor = ArgumentCaptor.forClass(LogAnalysisEntity.class);
        verify(jpaRepository).save(captor.capture());

        assertThat(captor.getValue().getId()).isNull();
        assertThat(result.getId()).isEqualTo(generatedUuid.toString());
    }

    @Test
    void save_delegatesToJpaRepository() {
        LogAnalysis domain = LogAnalysis.builder()
                .id(TEST_UUID.toString())
                .totalLines(1)
                .levelsJson("{}")
                .topErrorsJson("[]")
                .createdAt(NOW)
                .build();

        LogAnalysisEntity savedEntity = LogAnalysisEntity.builder()
                .id(TEST_UUID)
                .totalLines(1)
                .levels("{}")
                .topErrors("[]")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(LogAnalysisEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        verify(jpaRepository).save(any(LogAnalysisEntity.class));
    }
}
