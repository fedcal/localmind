package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.GateEvaluation;
import com.localmind.domain.mcp.model.QualityGate;
import com.localmind.infrastructure.persistence.entity.mcp.GateEvaluationEntity;
import com.localmind.infrastructure.persistence.entity.mcp.QualityGateEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaGateEvaluationRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaQualityGateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualityGateRepositoryAdapterTest {

    @Mock
    private JpaQualityGateRepository jpaGateRepository;

    @Mock
    private JpaGateEvaluationRepository jpaEvaluationRepository;

    private QualityGateRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-13T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new QualityGateRepositoryAdapter(jpaGateRepository, jpaEvaluationRepository);
    }

    @Test
    void saveGate_mapsDomainToEntityCorrectly() {
        QualityGate domain = QualityGate.builder()
                .id(TEST_UUID.toString())
                .name("Production Gate")
                .projectName("my-project")
                .checksJson("[{\"metric\":\"coverage\",\"operator\":\">=\",\"threshold\":80}]")
                .createdAt(NOW)
                .build();

        QualityGateEntity savedEntity = QualityGateEntity.builder()
                .id(TEST_UUID)
                .name("Production Gate")
                .projectName("my-project")
                .checksJson("[{\"metric\":\"coverage\",\"operator\":\">=\",\"threshold\":80}]")
                .createdAt(NOW)
                .build();

        when(jpaGateRepository.save(any(QualityGateEntity.class))).thenReturn(savedEntity);

        QualityGate result = adapter.saveGate(domain);

        ArgumentCaptor<QualityGateEntity> captor = ArgumentCaptor.forClass(QualityGateEntity.class);
        verify(jpaGateRepository).save(captor.capture());

        QualityGateEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getName()).isEqualTo("Production Gate");
        assertThat(captured.getProjectName()).isEqualTo("my-project");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("Production Gate");
    }

    @Test
    void findGateById_found_returnsDomain() {
        QualityGateEntity entity = QualityGateEntity.builder()
                .id(TEST_UUID)
                .name("Test Gate")
                .checksJson("[]")
                .createdAt(NOW)
                .build();

        when(jpaGateRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<QualityGate> result = adapter.findGateById(TEST_UUID.toString());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Gate");
    }

    @Test
    void findGateById_notFound_returnsEmpty() {
        when(jpaGateRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        Optional<QualityGate> result = adapter.findGateById(TEST_UUID.toString());

        assertThat(result).isEmpty();
    }

    @Test
    void saveEvaluation_mapsDomainToEntityCorrectly() {
        GateEvaluation domain = GateEvaluation.builder()
                .id(TEST_UUID.toString())
                .gateId("gate-1")
                .passed(true)
                .metricsJson("{\"coverage\":90}")
                .resultsJson("[{\"passed\":true}]")
                .createdAt(NOW)
                .build();

        GateEvaluationEntity savedEntity = GateEvaluationEntity.builder()
                .id(TEST_UUID)
                .gateId("gate-1")
                .passed(true)
                .metricsJson("{\"coverage\":90}")
                .resultsJson("[{\"passed\":true}]")
                .createdAt(NOW)
                .build();

        when(jpaEvaluationRepository.save(any(GateEvaluationEntity.class))).thenReturn(savedEntity);

        GateEvaluation result = adapter.saveEvaluation(domain);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getGateId()).isEqualTo("gate-1");
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void findEvaluationsByGateId_returnsMappedList() {
        GateEvaluationEntity entity = GateEvaluationEntity.builder()
                .id(TEST_UUID)
                .gateId("gate-1")
                .passed(false)
                .metricsJson("{}")
                .resultsJson("[]")
                .createdAt(NOW)
                .build();

        when(jpaEvaluationRepository.findByGateId("gate-1")).thenReturn(List.of(entity));

        List<GateEvaluation> results = adapter.findEvaluationsByGateId("gate-1");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGateId()).isEqualTo("gate-1");
        assertThat(results.get(0).isPassed()).isFalse();
    }
}
