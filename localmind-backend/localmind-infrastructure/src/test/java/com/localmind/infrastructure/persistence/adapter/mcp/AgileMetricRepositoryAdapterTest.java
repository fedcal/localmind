package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.AgileMetric;
import com.localmind.infrastructure.persistence.entity.mcp.AgileMetricEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaAgileMetricRepository;
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
class AgileMetricRepositoryAdapterTest {

    @Mock
    private JpaAgileMetricRepository jpaRepository;

    private AgileMetricRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new AgileMetricRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsDomainToEntityCorrectly() {
        AgileMetric domain = AgileMetric.builder()
                .id(TEST_UUID.toString())
                .metricType("velocity")
                .sprintRef("Sprint 10")
                .resultJson("{\"averageVelocity\":25.0}")
                .createdAt(NOW)
                .build();

        AgileMetricEntity savedEntity = AgileMetricEntity.builder()
                .id(TEST_UUID)
                .metricType("velocity")
                .sprintRef("Sprint 10")
                .result("{\"averageVelocity\":25.0}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(AgileMetricEntity.class))).thenReturn(savedEntity);

        AgileMetric result = adapter.save(domain);

        ArgumentCaptor<AgileMetricEntity> captor = ArgumentCaptor.forClass(AgileMetricEntity.class);
        verify(jpaRepository).save(captor.capture());

        AgileMetricEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getMetricType()).isEqualTo("velocity");
        assertThat(captured.getSprintRef()).isEqualTo("Sprint 10");
        assertThat(captured.getResult()).isEqualTo("{\"averageVelocity\":25.0}");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getMetricType()).isEqualTo("velocity");
        assertThat(result.getSprintRef()).isEqualTo("Sprint 10");
        assertThat(result.getResultJson()).isEqualTo("{\"averageVelocity\":25.0}");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        AgileMetric domain = AgileMetric.builder()
                .metricType("burndown")
                .sprintRef(null)
                .resultJson("{}")
                .createdAt(NOW)
                .build();

        AgileMetricEntity savedEntity = AgileMetricEntity.builder()
                .id(TEST_UUID)
                .metricType("burndown")
                .result("{}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(AgileMetricEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<AgileMetricEntity> captor = ArgumentCaptor.forClass(AgileMetricEntity.class);
        verify(jpaRepository).save(captor.capture());

        AgileMetricEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();
        assertThat(captured.getMetricType()).isEqualTo("burndown");
    }

    @Test
    void findByMetricType_mapsEntitiesToDomain() {
        AgileMetricEntity entity1 = AgileMetricEntity.builder()
                .id(TEST_UUID)
                .metricType("velocity")
                .sprintRef("Sprint 1")
                .result("{\"avg\":20}")
                .createdAt(NOW)
                .build();

        UUID secondUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        AgileMetricEntity entity2 = AgileMetricEntity.builder()
                .id(secondUuid)
                .metricType("velocity")
                .sprintRef("Sprint 2")
                .result("{\"avg\":25}")
                .createdAt(NOW.plusSeconds(60))
                .build();

        when(jpaRepository.findByMetricTypeOrderByCreatedAtDesc("velocity"))
                .thenReturn(List.of(entity2, entity1));

        List<AgileMetric> results = adapter.findByMetricType("velocity");

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(secondUuid.toString());
        assertThat(results.get(0).getSprintRef()).isEqualTo("Sprint 2");
        assertThat(results.get(1).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(results.get(1).getSprintRef()).isEqualTo("Sprint 1");
    }
}
