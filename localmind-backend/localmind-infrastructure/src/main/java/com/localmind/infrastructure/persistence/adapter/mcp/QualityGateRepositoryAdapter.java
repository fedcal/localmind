package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.GateEvaluation;
import com.localmind.domain.mcp.model.QualityGate;
import com.localmind.domain.mcp.port.out.QualityGateRepository;
import com.localmind.infrastructure.persistence.entity.mcp.GateEvaluationEntity;
import com.localmind.infrastructure.persistence.entity.mcp.QualityGateEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaGateEvaluationRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaQualityGateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class QualityGateRepositoryAdapter implements QualityGateRepository {

    private final JpaQualityGateRepository jpaGateRepository;
    private final JpaGateEvaluationRepository jpaEvaluationRepository;

    public QualityGateRepositoryAdapter(JpaQualityGateRepository jpaGateRepository,
                                         JpaGateEvaluationRepository jpaEvaluationRepository) {
        this.jpaGateRepository = jpaGateRepository;
        this.jpaEvaluationRepository = jpaEvaluationRepository;
    }

    @Override
    public QualityGate saveGate(QualityGate gate) {
        QualityGateEntity entity = toGateEntity(gate);
        QualityGateEntity saved = jpaGateRepository.save(entity);
        return toGateDomain(saved);
    }

    @Override
    public Optional<QualityGate> findGateById(String id) {
        return jpaGateRepository.findById(UUID.fromString(id))
                .map(this::toGateDomain);
    }

    @Override
    public List<QualityGate> findAllGates() {
        return jpaGateRepository.findAll().stream()
                .map(this::toGateDomain)
                .collect(Collectors.toList());
    }

    @Override
    public GateEvaluation saveEvaluation(GateEvaluation evaluation) {
        GateEvaluationEntity entity = toEvaluationEntity(evaluation);
        GateEvaluationEntity saved = jpaEvaluationRepository.save(entity);
        return toEvaluationDomain(saved);
    }

    @Override
    public List<GateEvaluation> findEvaluationsByGateId(String gateId) {
        return jpaEvaluationRepository.findByGateId(gateId).stream()
                .map(this::toEvaluationDomain)
                .collect(Collectors.toList());
    }

    // ========== Gate Mapping ==========

    private QualityGateEntity toGateEntity(QualityGate domain) {
        QualityGateEntity entity = QualityGateEntity.builder()
                .name(domain.getName())
                .projectName(domain.getProjectName())
                .checksJson(domain.getChecksJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private QualityGate toGateDomain(QualityGateEntity entity) {
        return QualityGate.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .projectName(entity.getProjectName())
                .checksJson(entity.getChecksJson())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // ========== Evaluation Mapping ==========

    private GateEvaluationEntity toEvaluationEntity(GateEvaluation domain) {
        GateEvaluationEntity entity = GateEvaluationEntity.builder()
                .gateId(domain.getGateId())
                .passed(domain.isPassed())
                .metricsJson(domain.getMetricsJson())
                .resultsJson(domain.getResultsJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private GateEvaluation toEvaluationDomain(GateEvaluationEntity entity) {
        return GateEvaluation.builder()
                .id(entity.getId().toString())
                .gateId(entity.getGateId())
                .passed(entity.isPassed())
                .metricsJson(entity.getMetricsJson())
                .resultsJson(entity.getResultsJson())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
