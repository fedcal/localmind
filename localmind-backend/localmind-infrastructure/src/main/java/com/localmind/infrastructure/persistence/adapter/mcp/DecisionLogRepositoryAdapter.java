package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.DecisionLink;
import com.localmind.domain.mcp.model.DecisionRecord;
import com.localmind.domain.mcp.port.out.DecisionLogRepository;
import com.localmind.infrastructure.persistence.entity.mcp.DecisionLinkEntity;
import com.localmind.infrastructure.persistence.entity.mcp.DecisionRecordEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaDecisionLinkRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaDecisionRecordRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain DecisionLogRepository port to JPA persistence.
 * Handles mapping between domain models and JPA entities for decision records and links.
 */
@Component
public class DecisionLogRepositoryAdapter implements DecisionLogRepository {

    private final JpaDecisionRecordRepository recordJpaRepository;
    private final JpaDecisionLinkRepository linkJpaRepository;

    public DecisionLogRepositoryAdapter(JpaDecisionRecordRepository recordJpaRepository,
                                         JpaDecisionLinkRepository linkJpaRepository) {
        this.recordJpaRepository = recordJpaRepository;
        this.linkJpaRepository = linkJpaRepository;
    }

    // --- DecisionRecord ---

    @Override
    public DecisionRecord save(DecisionRecord record) {
        DecisionRecordEntity entity = toRecordEntity(record);
        DecisionRecordEntity saved = recordJpaRepository.save(entity);
        return toRecordDomain(saved);
    }

    @Override
    public List<DecisionRecord> findAll() {
        return recordJpaRepository.findAll().stream()
                .map(this::toRecordDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DecisionRecord> findById(String id) {
        return recordJpaRepository.findById(UUID.fromString(id))
                .map(this::toRecordDomain);
    }

    // --- DecisionLink ---

    @Override
    public DecisionLink saveLink(DecisionLink link) {
        DecisionLinkEntity entity = toLinkEntity(link);
        DecisionLinkEntity saved = linkJpaRepository.save(entity);
        return toLinkDomain(saved);
    }

    @Override
    public List<DecisionLink> findLinksByDecisionId(String decisionId) {
        return linkJpaRepository.findByDecisionId(decisionId).stream()
                .map(this::toLinkDomain)
                .collect(Collectors.toList());
    }

    // --- DecisionRecord Mapping ---

    private DecisionRecordEntity toRecordEntity(DecisionRecord domain) {
        DecisionRecordEntity entity = DecisionRecordEntity.builder()
                .title(domain.getTitle())
                .context(domain.getContext())
                .decisionText(domain.getDecision())
                .alternativesJson(domain.getAlternativesJson())
                .consequences(domain.getConsequences())
                .status(domain.getStatus())
                .relatedTicketsJson(domain.getRelatedTicketsJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private DecisionRecord toRecordDomain(DecisionRecordEntity entity) {
        return DecisionRecord.builder()
                .id(entity.getId().toString())
                .title(entity.getTitle())
                .context(entity.getContext())
                .decision(entity.getDecisionText())
                .alternativesJson(entity.getAlternativesJson())
                .consequences(entity.getConsequences())
                .status(entity.getStatus())
                .relatedTicketsJson(entity.getRelatedTicketsJson())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // --- DecisionLink Mapping ---

    private DecisionLinkEntity toLinkEntity(DecisionLink domain) {
        DecisionLinkEntity entity = DecisionLinkEntity.builder()
                .decisionId(domain.getDecisionId())
                .linkType(domain.getLinkType())
                .targetId(domain.getTargetId())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private DecisionLink toLinkDomain(DecisionLinkEntity entity) {
        return DecisionLink.builder()
                .id(entity.getId().toString())
                .decisionId(entity.getDecisionId())
                .linkType(entity.getLinkType())
                .targetId(entity.getTargetId())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
