package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.ComposeAnalysis;
import com.localmind.infrastructure.persistence.entity.mcp.ComposeAnalysisEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaComposeAnalysisRepository;
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
class DockerAnalysisRepositoryAdapterTest {

    @Mock
    private JpaComposeAnalysisRepository jpaRepository;

    private DockerAnalysisRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new DockerAnalysisRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsDomainToEntityCorrectly() {
        ComposeAnalysis domain = ComposeAnalysis.builder()
                .id(TEST_UUID.toString())
                .contentType("docker-compose")
                .serviceCount(3)
                .resultJson("{\"serviceCount\":3}")
                .createdAt(NOW)
                .build();

        ComposeAnalysisEntity savedEntity = ComposeAnalysisEntity.builder()
                .id(TEST_UUID)
                .contentType("docker-compose")
                .serviceCount(3)
                .result("{\"serviceCount\":3}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(ComposeAnalysisEntity.class))).thenReturn(savedEntity);

        ComposeAnalysis result = adapter.save(domain);

        ArgumentCaptor<ComposeAnalysisEntity> captor = ArgumentCaptor.forClass(ComposeAnalysisEntity.class);
        verify(jpaRepository).save(captor.capture());

        ComposeAnalysisEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getContentType()).isEqualTo("docker-compose");
        assertThat(captured.getServiceCount()).isEqualTo(3);
        assertThat(captured.getResult()).isEqualTo("{\"serviceCount\":3}");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getContentType()).isEqualTo("docker-compose");
        assertThat(result.getServiceCount()).isEqualTo(3);
        assertThat(result.getResultJson()).isEqualTo("{\"serviceCount\":3}");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_mapsEntityToDomainCorrectly() {
        ComposeAnalysis domain = ComposeAnalysis.builder()
                .id(TEST_UUID.toString())
                .contentType("dockerfile")
                .serviceCount(2)
                .resultJson("{\"stages\":2}")
                .createdAt(NOW)
                .build();

        ComposeAnalysisEntity savedEntity = ComposeAnalysisEntity.builder()
                .id(TEST_UUID)
                .contentType("dockerfile")
                .serviceCount(2)
                .result("{\"stages\":2}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(ComposeAnalysisEntity.class))).thenReturn(savedEntity);

        ComposeAnalysis result = adapter.save(domain);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getContentType()).isEqualTo("dockerfile");
        assertThat(result.getServiceCount()).isEqualTo(2);
        assertThat(result.getResultJson()).isEqualTo("{\"stages\":2}");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        ComposeAnalysis domain = ComposeAnalysis.builder()
                .contentType("generated-compose")
                .serviceCount(1)
                .resultJson("{}")
                .createdAt(NOW)
                .build();

        ComposeAnalysisEntity savedEntity = ComposeAnalysisEntity.builder()
                .id(TEST_UUID)
                .contentType("generated-compose")
                .serviceCount(1)
                .result("{}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(ComposeAnalysisEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<ComposeAnalysisEntity> captor = ArgumentCaptor.forClass(ComposeAnalysisEntity.class);
        verify(jpaRepository).save(captor.capture());

        ComposeAnalysisEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();
        assertThat(captured.getContentType()).isEqualTo("generated-compose");
    }
}
