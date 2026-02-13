package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.FlakyTest;
import com.localmind.domain.mcp.model.PipelineRun;
import com.localmind.domain.mcp.port.out.CicdMonitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CicdMonitorServiceTest {

    @Mock
    private CicdMonitorRepository repository;

    private CicdMonitorService service;

    @BeforeEach
    void setUp() {
        when(repository.savePipelineRun(any(PipelineRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveFlakyTest(any(FlakyTest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service = new CicdMonitorService(repository);
    }

    // ========================================================================
    // listPipelines tests
    // ========================================================================

    @Test
    @SuppressWarnings("unchecked")
    void listPipelines_withExistingRuns_returnsAll() {
        PipelineRun run1 = PipelineRun.builder()
                .id("id-1").owner("owner1").repo("repo1").runId("run-1")
                .title("Build CI").branch("main").status("completed").conclusion("success")
                .workflow("ci.yml").url("https://github.com/owner1/repo1/actions/runs/1")
                .createdAt(Instant.parse("2026-02-12T10:00:00Z"))
                .build();
        PipelineRun run2 = PipelineRun.builder()
                .id("id-2").owner("owner1").repo("repo1").runId("run-2")
                .title("Build CD").branch("develop").status("completed").conclusion("failure")
                .workflow("cd.yml").url("https://github.com/owner1/repo1/actions/runs/2")
                .createdAt(Instant.parse("2026-02-12T11:00:00Z"))
                .build();

        when(repository.findByRepo("owner1", "repo1")).thenReturn(List.of(run1, run2));

        List<Map<String, Object>> result = service.listPipelines("owner1", "repo1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("id")).isEqualTo("id-1");
        assertThat(result.get(0).get("title")).isEqualTo("Build CI");
        assertThat(result.get(0).get("branch")).isEqualTo("main");
        assertThat(result.get(0).get("status")).isEqualTo("completed");
        assertThat(result.get(0).get("conclusion")).isEqualTo("success");
        assertThat(result.get(0).get("workflow")).isEqualTo("ci.yml");
        assertThat(result.get(0).get("url")).isEqualTo("https://github.com/owner1/repo1/actions/runs/1");

        assertThat(result.get(1).get("id")).isEqualTo("id-2");
        assertThat(result.get(1).get("conclusion")).isEqualTo("failure");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listPipelines_withNoRuns_returnsEmptyListWithMessage() {
        when(repository.findByRepo("owner1", "empty-repo")).thenReturn(List.of());

        List<Map<String, Object>> result = service.listPipelines("owner1", "empty-repo");

        assertThat(result).hasSize(1);
        assertThat((String) result.get(0).get("message")).contains("No pipeline runs found");
        assertThat((String) result.get(0).get("message")).contains("owner1/empty-repo");
    }

    // ========================================================================
    // getPipelineStatus tests
    // ========================================================================

    @Test
    @SuppressWarnings("unchecked")
    void getPipelineStatus_withExistingRun_returnsDetails() {
        PipelineRun run = PipelineRun.builder()
                .id("id-1").owner("owner1").repo("repo1").runId("run-123")
                .title("Build CI").branch("main").status("completed").conclusion("success")
                .workflow("ci.yml").url("https://github.com")
                .createdAt(Instant.parse("2026-02-12T10:00:00Z"))
                .build();

        when(repository.findByRunId("run-123")).thenReturn(Optional.of(run));

        Map<String, Object> result = service.getPipelineStatus("run-123");

        assertThat(result.get("id")).isEqualTo("id-1");
        assertThat(result.get("title")).isEqualTo("Build CI");
        assertThat(result.get("branch")).isEqualTo("main");
        assertThat(result.get("status")).isEqualTo("completed");
        assertThat(result.get("conclusion")).isEqualTo("success");
        assertThat(result.get("workflow")).isEqualTo("ci.yml");
        assertThat(result.get("createdAt")).isEqualTo("2026-02-12T10:00:00Z");
        assertThat(result.get("updatedAt")).isNotNull();
        assertThat((List<?>) result.get("jobs")).isEmpty();
    }

    @Test
    void getPipelineStatus_withNonExistingRun_returnsError() {
        when(repository.findByRunId("non-existent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.getPipelineStatus("non-existent");

        assertThat(result.get("error")).isEqualTo("Pipeline run not found");
        assertThat(result.get("runId")).isEqualTo("non-existent");
    }

    // ========================================================================
    // savePipelineRun tests
    // ========================================================================

    @Test
    void savePipelineRun_savesCorrectly() {
        Map<String, Object> result = service.savePipelineRun(
                "owner1", "repo1", "run-456",
                "Deploy Pipeline", "main", "completed", "success",
                "deploy.yml", "https://github.com/owner1/repo1/actions/runs/456");

        assertThat(result.get("saved")).isEqualTo(true);
        assertThat((String) result.get("message")).contains("run-456");
        assertThat((String) result.get("message")).contains("owner1/repo1");
        assertThat(result.get("id")).isNotNull();

        ArgumentCaptor<PipelineRun> captor = ArgumentCaptor.forClass(PipelineRun.class);
        verify(repository).savePipelineRun(captor.capture());

        PipelineRun saved = captor.getValue();
        assertThat(saved.getOwner()).isEqualTo("owner1");
        assertThat(saved.getRepo()).isEqualTo("repo1");
        assertThat(saved.getRunId()).isEqualTo("run-456");
        assertThat(saved.getTitle()).isEqualTo("Deploy Pipeline");
        assertThat(saved.getBranch()).isEqualTo("main");
        assertThat(saved.getStatus()).isEqualTo("completed");
        assertThat(saved.getConclusion()).isEqualTo("success");
        assertThat(saved.getWorkflow()).isEqualTo("deploy.yml");
        assertThat(saved.getUrl()).isEqualTo("https://github.com/owner1/repo1/actions/runs/456");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void savePipelineRun_generatesUniqueId() {
        Map<String, Object> result1 = service.savePipelineRun(
                "o", "r", "run-1", "T1", "b", "s", "c", "w", "u");
        Map<String, Object> result2 = service.savePipelineRun(
                "o", "r", "run-2", "T2", "b", "s", "c", "w", "u");

        assertThat(result1.get("id")).isNotEqualTo(result2.get("id"));
    }

    // ========================================================================
    // detectFlakyTests tests
    // ========================================================================

    @Test
    @SuppressWarnings("unchecked")
    void detectFlakyTests_withFlakyTests_detectsCorrectly() {
        List<Map<String, Object>> runs = new ArrayList<>();
        runs.add(createTestRun("TestA", true, "ci.yml", "main"));
        runs.add(createTestRun("TestA", false, "ci.yml", "main"));
        runs.add(createTestRun("TestA", true, "ci.yml", "main"));
        runs.add(createTestRun("TestA", false, "ci.yml", "main"));

        Map<String, Object> result = service.detectFlakyTests(runs);

        assertThat((int) result.get("analyzedRuns")).isEqualTo(4);
        assertThat((int) result.get("flakyTestsFound")).isEqualTo(1);

        List<Map<String, Object>> flakyTests = (List<Map<String, Object>>) result.get("flakyTests");
        assertThat(flakyTests).hasSize(1);
        assertThat(flakyTests.get(0).get("testName")).isEqualTo("TestA");
        assertThat(flakyTests.get(0).get("passCount")).isEqualTo(2);
        assertThat(flakyTests.get(0).get("failCount")).isEqualTo(2);
        assertThat(flakyTests.get(0).get("totalRuns")).isEqualTo(4);
        // min(2,2)/4 * 100 = 50.0
        assertThat((double) flakyTests.get(0).get("flakinessRate")).isEqualTo(50.0);

        verify(repository, times(1)).saveFlakyTest(any(FlakyTest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectFlakyTests_withNoFlakyTests_returnsEmpty() {
        List<Map<String, Object>> runs = new ArrayList<>();
        runs.add(createTestRun("TestA", true, "ci.yml", "main"));
        runs.add(createTestRun("TestA", true, "ci.yml", "main"));
        runs.add(createTestRun("TestB", false, "ci.yml", "main"));
        runs.add(createTestRun("TestB", false, "ci.yml", "main"));

        Map<String, Object> result = service.detectFlakyTests(runs);

        assertThat((int) result.get("analyzedRuns")).isEqualTo(4);
        assertThat((int) result.get("flakyTestsFound")).isEqualTo(0);
        assertThat((List<?>) result.get("flakyTests")).isEmpty();

        verify(repository, never()).saveFlakyTest(any(FlakyTest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectFlakyTests_withEmptyList_returnsZero() {
        Map<String, Object> result = service.detectFlakyTests(List.of());

        assertThat((int) result.get("analyzedRuns")).isEqualTo(0);
        assertThat((int) result.get("flakyTestsFound")).isEqualTo(0);
        assertThat((List<?>) result.get("flakyTests")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectFlakyTests_withNull_returnsZero() {
        Map<String, Object> result = service.detectFlakyTests(null);

        assertThat((int) result.get("analyzedRuns")).isEqualTo(0);
        assertThat((int) result.get("flakyTestsFound")).isEqualTo(0);
        assertThat((List<?>) result.get("flakyTests")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectFlakyTests_sortsByFlakinessRateDescending() {
        List<Map<String, Object>> runs = new ArrayList<>();
        // TestA: 1 pass, 3 fail -> flakinessRate = min(1,3)/4*100 = 25
        runs.add(createTestRun("TestA", true, "ci.yml", "main"));
        runs.add(createTestRun("TestA", false, "ci.yml", "main"));
        runs.add(createTestRun("TestA", false, "ci.yml", "main"));
        runs.add(createTestRun("TestA", false, "ci.yml", "main"));

        // TestB: 2 pass, 2 fail -> flakinessRate = min(2,2)/4*100 = 50
        runs.add(createTestRun("TestB", true, "ci.yml", "main"));
        runs.add(createTestRun("TestB", true, "ci.yml", "main"));
        runs.add(createTestRun("TestB", false, "ci.yml", "main"));
        runs.add(createTestRun("TestB", false, "ci.yml", "main"));

        Map<String, Object> result = service.detectFlakyTests(runs);

        List<Map<String, Object>> flakyTests = (List<Map<String, Object>>) result.get("flakyTests");
        assertThat(flakyTests).hasSize(2);
        // TestB (50%) should come before TestA (25%)
        assertThat(flakyTests.get(0).get("testName")).isEqualTo("TestB");
        assertThat((double) flakyTests.get(0).get("flakinessRate")).isEqualTo(50.0);
        assertThat(flakyTests.get(1).get("testName")).isEqualTo("TestA");
        assertThat((double) flakyTests.get(1).get("flakinessRate")).isEqualTo(25.0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectFlakyTests_withStringPassedValue_handlesCorrectly() {
        List<Map<String, Object>> runs = new ArrayList<>();
        Map<String, Object> run1 = new HashMap<>();
        run1.put("testName", "TestX");
        run1.put("passed", "true");
        run1.put("workflow", "ci.yml");
        run1.put("branch", "main");
        runs.add(run1);

        Map<String, Object> run2 = new HashMap<>();
        run2.put("testName", "TestX");
        run2.put("passed", "false");
        run2.put("workflow", "ci.yml");
        run2.put("branch", "main");
        runs.add(run2);

        Map<String, Object> result = service.detectFlakyTests(runs);

        assertThat((int) result.get("flakyTestsFound")).isEqualTo(1);
        List<Map<String, Object>> flakyTests = (List<Map<String, Object>>) result.get("flakyTests");
        assertThat(flakyTests.get(0).get("testName")).isEqualTo("TestX");
    }

    @Test
    void detectFlakyTests_persistsFlakyTests() {
        List<Map<String, Object>> runs = new ArrayList<>();
        runs.add(createTestRun("TestFlaky", true, "ci.yml", "main"));
        runs.add(createTestRun("TestFlaky", false, "ci.yml", "main"));

        service.detectFlakyTests(runs);

        ArgumentCaptor<FlakyTest> captor = ArgumentCaptor.forClass(FlakyTest.class);
        verify(repository).saveFlakyTest(captor.capture());

        FlakyTest saved = captor.getValue();
        assertThat(saved.getTestName()).isEqualTo("TestFlaky");
        assertThat(saved.getPassCount()).isEqualTo(1);
        assertThat(saved.getFailCount()).isEqualTo(1);
        assertThat(saved.getFlakinessRate()).isEqualTo(50.0);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDetectedAt()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectFlakyTests_withNullTestName_skipsEntry() {
        List<Map<String, Object>> runs = new ArrayList<>();
        Map<String, Object> runWithNull = new HashMap<>();
        runWithNull.put("testName", null);
        runWithNull.put("passed", true);
        runWithNull.put("workflow", "ci.yml");
        runWithNull.put("branch", "main");
        runs.add(runWithNull);

        runs.add(createTestRun("TestValid", true, "ci.yml", "main"));
        runs.add(createTestRun("TestValid", false, "ci.yml", "main"));

        Map<String, Object> result = service.detectFlakyTests(runs);

        assertThat((int) result.get("analyzedRuns")).isEqualTo(3);
        assertThat((int) result.get("flakyTestsFound")).isEqualTo(1);
        List<Map<String, Object>> flakyTests = (List<Map<String, Object>>) result.get("flakyTests");
        assertThat(flakyTests.get(0).get("testName")).isEqualTo("TestValid");
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private Map<String, Object> createTestRun(String testName, boolean passed,
                                               String workflow, String branch) {
        Map<String, Object> run = new HashMap<>();
        run.put("testName", testName);
        run.put("passed", passed);
        run.put("workflow", workflow);
        run.put("branch", branch);
        return run;
    }
}
