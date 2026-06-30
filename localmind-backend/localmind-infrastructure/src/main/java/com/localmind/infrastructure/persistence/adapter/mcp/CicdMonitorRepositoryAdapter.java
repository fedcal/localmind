package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.FlakyTest;
import com.localmind.domain.mcp.model.PipelineRun;
import com.localmind.domain.mcp.port.out.CicdMonitorRepository;
import com.localmind.infrastructure.persistence.entity.mcp.FlakyTestEntity;
import com.localmind.infrastructure.persistence.entity.mcp.PipelineRunEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaFlakyTestRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaPipelineRunRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain CicdMonitorRepository port to JPA persistence.
 */
@Component
public class CicdMonitorRepositoryAdapter implements CicdMonitorRepository {

    private final JpaPipelineRunRepository pipelineRunJpaRepository;
    private final JpaFlakyTestRepository flakyTestJpaRepository;

    public CicdMonitorRepositoryAdapter(JpaPipelineRunRepository pipelineRunJpaRepository,
                                         JpaFlakyTestRepository flakyTestJpaRepository) {
        this.pipelineRunJpaRepository = pipelineRunJpaRepository;
        this.flakyTestJpaRepository = flakyTestJpaRepository;
    }

    @Override
    public PipelineRun savePipelineRun(PipelineRun pipelineRun) {
        PipelineRunEntity entity = toEntity(pipelineRun);
        PipelineRunEntity saved = pipelineRunJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<PipelineRun> findByRepo(String owner, String repo) {
        return pipelineRunJpaRepository.findByOwnerAndRepoOrderByCreatedAtDesc(owner, repo)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PipelineRun> findByRunId(String runId) {
        return pipelineRunJpaRepository.findByRunId(runId)
                .map(this::toDomain);
    }

    @Override
    public FlakyTest saveFlakyTest(FlakyTest flakyTest) {
        FlakyTestEntity entity = toFlakyTestEntity(flakyTest);
        FlakyTestEntity saved = flakyTestJpaRepository.save(entity);
        return toFlakyTestDomain(saved);
    }

    // --- PipelineRun mapping ---

    private PipelineRunEntity toEntity(PipelineRun domain) {
        PipelineRunEntity entity = PipelineRunEntity.builder()
                .owner(domain.getOwner())
                .repo(domain.getRepo())
                .runId(domain.getRunId())
                .title(domain.getTitle())
                .branch(domain.getBranch())
                .status(domain.getStatus())
                .conclusion(domain.getConclusion())
                .workflow(domain.getWorkflow())
                .url(domain.getUrl())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private PipelineRun toDomain(PipelineRunEntity entity) {
        return PipelineRun.builder()
                .id(entity.getId().toString())
                .owner(entity.getOwner())
                .repo(entity.getRepo())
                .runId(entity.getRunId())
                .title(entity.getTitle())
                .branch(entity.getBranch())
                .status(entity.getStatus())
                .conclusion(entity.getConclusion())
                .workflow(entity.getWorkflow())
                .url(entity.getUrl())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // --- FlakyTest mapping ---

    private FlakyTestEntity toFlakyTestEntity(FlakyTest domain) {
        FlakyTestEntity entity = FlakyTestEntity.builder()
                .repo(domain.getRepo())
                .testName(domain.getTestName())
                .workflow(domain.getWorkflow())
                .passCount(domain.getPassCount())
                .failCount(domain.getFailCount())
                .flakinessRate(domain.getFlakinessRate())
                .detectedAt(domain.getDetectedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private FlakyTest toFlakyTestDomain(FlakyTestEntity entity) {
        return FlakyTest.builder()
                .id(entity.getId().toString())
                .repo(entity.getRepo())
                .testName(entity.getTestName())
                .workflow(entity.getWorkflow())
                .passCount(entity.getPassCount())
                .failCount(entity.getFailCount())
                .flakinessRate(entity.getFlakinessRate())
                .detectedAt(entity.getDetectedAt())
                .build();
    }
}
