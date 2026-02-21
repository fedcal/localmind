package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.FlakyTest;
import com.localmind.domain.mcp.model.PipelineRun;
import com.localmind.infrastructure.persistence.entity.mcp.FlakyTestEntity;
import com.localmind.infrastructure.persistence.entity.mcp.PipelineRunEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaFlakyTestRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaPipelineRunRepository;
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
class CicdMonitorRepositoryAdapterTest {

    @Mock
    private JpaPipelineRunRepository pipelineRunJpaRepository;

    @Mock
    private JpaFlakyTestRepository flakyTestJpaRepository;

    private CicdMonitorRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new CicdMonitorRepositoryAdapter(pipelineRunJpaRepository, flakyTestJpaRepository);
    }

    @Test
    void savePipelineRun_mapsCorrectlyFromDomainToEntityAndBack() {
        PipelineRun domain = PipelineRun.builder()
                .id(TEST_UUID.toString())
                .owner("owner1")
                .repo("repo1")
                .runId("run-123")
                .title("Build CI")
                .branch("main")
                .status("completed")
                .conclusion("success")
                .workflow("ci.yml")
                .url("https://github.com/owner1/repo1/actions/runs/123")
                .createdAt(NOW)
                .build();

        PipelineRunEntity savedEntity = PipelineRunEntity.builder()
                .id(TEST_UUID)
                .owner("owner1")
                .repo("repo1")
                .runId("run-123")
                .title("Build CI")
                .branch("main")
                .status("completed")
                .conclusion("success")
                .workflow("ci.yml")
                .url("https://github.com/owner1/repo1/actions/runs/123")
                .createdAt(NOW)
                .build();

        when(pipelineRunJpaRepository.save(any(PipelineRunEntity.class))).thenReturn(savedEntity);

        PipelineRun result = adapter.savePipelineRun(domain);

        ArgumentCaptor<PipelineRunEntity> captor = ArgumentCaptor.forClass(PipelineRunEntity.class);
        verify(pipelineRunJpaRepository).save(captor.capture());

        PipelineRunEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getOwner()).isEqualTo("owner1");
        assertThat(captured.getRepo()).isEqualTo("repo1");
        assertThat(captured.getRunId()).isEqualTo("run-123");
        assertThat(captured.getTitle()).isEqualTo("Build CI");
        assertThat(captured.getBranch()).isEqualTo("main");
        assertThat(captured.getStatus()).isEqualTo("completed");
        assertThat(captured.getConclusion()).isEqualTo("success");
        assertThat(captured.getWorkflow()).isEqualTo("ci.yml");
        assertThat(captured.getUrl()).isEqualTo("https://github.com/owner1/repo1/actions/runs/123");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getOwner()).isEqualTo("owner1");
        assertThat(result.getRepo()).isEqualTo("repo1");
        assertThat(result.getRunId()).isEqualTo("run-123");
        assertThat(result.getTitle()).isEqualTo("Build CI");
    }

    @Test
    void findByRepo_mapsEntitiesCorrectly() {
        PipelineRunEntity entity1 = PipelineRunEntity.builder()
                .id(TEST_UUID)
                .owner("owner1").repo("repo1").runId("run-1")
                .title("Build 1").branch("main").status("completed").conclusion("success")
                .workflow("ci.yml").url("https://github.com/1")
                .createdAt(NOW)
                .build();

        UUID uuid2 = UUID.fromString("b1b2c3d4-e5f6-7890-abcd-ef1234567890");
        PipelineRunEntity entity2 = PipelineRunEntity.builder()
                .id(uuid2)
                .owner("owner1").repo("repo1").runId("run-2")
                .title("Build 2").branch("develop").status("in_progress").conclusion(null)
                .workflow("cd.yml").url("https://github.com/2")
                .createdAt(NOW)
                .build();

        when(pipelineRunJpaRepository.findByOwnerAndRepoOrderByCreatedAtDesc("owner1", "repo1"))
                .thenReturn(List.of(entity1, entity2));

        List<PipelineRun> result = adapter.findByRepo("owner1", "repo1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getTitle()).isEqualTo("Build 1");
        assertThat(result.get(0).getBranch()).isEqualTo("main");

        assertThat(result.get(1).getId()).isEqualTo(uuid2.toString());
        assertThat(result.get(1).getTitle()).isEqualTo("Build 2");
        assertThat(result.get(1).getStatus()).isEqualTo("in_progress");
        assertThat(result.get(1).getConclusion()).isNull();
    }

    @Test
    void findByRunId_whenFound_returnsMappedDomain() {
        PipelineRunEntity entity = PipelineRunEntity.builder()
                .id(TEST_UUID)
                .owner("owner1").repo("repo1").runId("run-999")
                .title("Deploy").branch("release").status("completed").conclusion("success")
                .workflow("deploy.yml").url("https://github.com/999")
                .createdAt(NOW)
                .build();

        when(pipelineRunJpaRepository.findByRunId("run-999")).thenReturn(Optional.of(entity));

        Optional<PipelineRun> result = adapter.findByRunId("run-999");

        assertThat(result).isPresent();
        assertThat(result.get().getRunId()).isEqualTo("run-999");
        assertThat(result.get().getTitle()).isEqualTo("Deploy");
        assertThat(result.get().getWorkflow()).isEqualTo("deploy.yml");
    }

    @Test
    void saveFlakyTest_mapsCorrectlyFromDomainToEntityAndBack() {
        FlakyTest domain = FlakyTest.builder()
                .id(TEST_UUID.toString())
                .repo("repo1")
                .testName("com.example.TestFlakyClass#testMethod")
                .workflow("ci.yml")
                .passCount(5)
                .failCount(3)
                .flakinessRate(37.5)
                .detectedAt(NOW)
                .build();

        FlakyTestEntity savedEntity = FlakyTestEntity.builder()
                .id(TEST_UUID)
                .repo("repo1")
                .testName("com.example.TestFlakyClass#testMethod")
                .workflow("ci.yml")
                .passCount(5)
                .failCount(3)
                .flakinessRate(37.5)
                .detectedAt(NOW)
                .build();

        when(flakyTestJpaRepository.save(any(FlakyTestEntity.class))).thenReturn(savedEntity);

        FlakyTest result = adapter.saveFlakyTest(domain);

        ArgumentCaptor<FlakyTestEntity> captor = ArgumentCaptor.forClass(FlakyTestEntity.class);
        verify(flakyTestJpaRepository).save(captor.capture());

        FlakyTestEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getRepo()).isEqualTo("repo1");
        assertThat(captured.getTestName()).isEqualTo("com.example.TestFlakyClass#testMethod");
        assertThat(captured.getWorkflow()).isEqualTo("ci.yml");
        assertThat(captured.getPassCount()).isEqualTo(5);
        assertThat(captured.getFailCount()).isEqualTo(3);
        assertThat(captured.getFlakinessRate()).isEqualTo(37.5);
        assertThat(captured.getDetectedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getTestName()).isEqualTo("com.example.TestFlakyClass#testMethod");
        assertThat(result.getPassCount()).isEqualTo(5);
        assertThat(result.getFailCount()).isEqualTo(3);
        assertThat(result.getFlakinessRate()).isEqualTo(37.5);
    }
}
