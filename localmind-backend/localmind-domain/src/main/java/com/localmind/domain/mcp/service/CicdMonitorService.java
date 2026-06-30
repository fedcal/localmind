package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.FlakyTest;
import com.localmind.domain.mcp.model.PipelineRun;
import com.localmind.domain.mcp.port.in.CicdMonitorUseCase;
import com.localmind.domain.mcp.port.out.CicdMonitorRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain service implementing CI/CD monitoring tools.
 * Pure Java, zero Spring dependencies.
 */
public class CicdMonitorService implements CicdMonitorUseCase {

    private final CicdMonitorRepository repository;

    public CicdMonitorService(CicdMonitorRepository repository) {
        this.repository = repository;
    }

    // ========================================================================
    // 1. listPipelines
    // ========================================================================

    @Override
    public List<Map<String, Object>> listPipelines(String owner, String repo) {
        List<PipelineRun> runs = repository.findByRepo(owner, repo);

        if (runs.isEmpty()) {
            Map<String, Object> emptyResult = new LinkedHashMap<>();
            emptyResult.put("message", "No pipeline runs found for " + owner + "/" + repo);
            List<Map<String, Object>> result = new ArrayList<>();
            result.add(emptyResult);
            return result;
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (PipelineRun run : runs) {
            result.add(pipelineRunToMap(run));
        }
        return result;
    }

    // ========================================================================
    // 2. getPipelineStatus
    // ========================================================================

    @Override
    public Map<String, Object> getPipelineStatus(String runId) {
        Optional<PipelineRun> optionalRun = repository.findByRunId(runId);

        if (optionalRun.isEmpty()) {
            Map<String, Object> notFound = new LinkedHashMap<>();
            notFound.put("error", "Pipeline run not found");
            notFound.put("runId", runId);
            return notFound;
        }

        PipelineRun run = optionalRun.get();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", run.getId());
        result.put("title", run.getTitle());
        result.put("branch", run.getBranch());
        result.put("status", run.getStatus());
        result.put("conclusion", run.getConclusion());
        result.put("workflow", run.getWorkflow());
        result.put("createdAt", run.getCreatedAt() != null ? run.getCreatedAt().toString() : null);
        result.put("updatedAt", run.getCreatedAt() != null ? run.getCreatedAt().toString() : null);
        result.put("duration", null);
        result.put("jobs", List.of());
        return result;
    }

    // ========================================================================
    // 3. savePipelineRun
    // ========================================================================

    @Override
    public Map<String, Object> savePipelineRun(String owner, String repo, String runId,
                                                String title, String branch, String status,
                                                String conclusion, String workflow, String url) {
        PipelineRun pipelineRun = PipelineRun.builder()
                .id(UUID.randomUUID().toString())
                .owner(owner)
                .repo(repo)
                .runId(runId)
                .title(title)
                .branch(branch)
                .status(status)
                .conclusion(conclusion)
                .workflow(workflow)
                .url(url)
                .createdAt(Instant.now())
                .build();

        PipelineRun saved = repository.savePipelineRun(pipelineRun);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", saved.getId());
        result.put("saved", true);
        result.put("message", "Pipeline run " + runId + " saved successfully for " + owner + "/" + repo);
        return result;
    }

    // ========================================================================
    // 4. detectFlakyTests
    // ========================================================================

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> detectFlakyTests(List<Map<String, Object>> runs) {
        if (runs == null || runs.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("analyzedRuns", 0);
            empty.put("flakyTestsFound", 0);
            empty.put("flakyTests", List.of());
            return empty;
        }

        // Group by testName
        Map<String, TestAggregation> aggregations = new HashMap<>();
        for (Map<String, Object> run : runs) {
            String testName = (String) run.get("testName");
            if (testName == null || testName.isBlank()) {
                continue;
            }

            boolean passed = toBoolean(run.get("passed"));
            String workflow = run.get("workflow") != null ? run.get("workflow").toString() : "";
            String branch = run.get("branch") != null ? run.get("branch").toString() : "";

            TestAggregation agg = aggregations.computeIfAbsent(testName, k -> new TestAggregation());
            agg.workflow = workflow;
            agg.branch = branch;
            if (passed) {
                agg.passCount++;
            } else {
                agg.failCount++;
            }
        }

        // Detect flaky tests: those with both pass and fail
        List<Map<String, Object>> flakyTests = new ArrayList<>();
        for (Map.Entry<String, TestAggregation> entry : aggregations.entrySet()) {
            TestAggregation agg = entry.getValue();
            if (agg.passCount > 0 && agg.failCount > 0) {
                int total = agg.passCount + agg.failCount;
                double flakinessRate = (double) Math.min(agg.passCount, agg.failCount) / total * 100;

                Map<String, Object> flakyTest = new LinkedHashMap<>();
                flakyTest.put("testName", entry.getKey());
                flakyTest.put("workflow", agg.workflow);
                flakyTest.put("branch", agg.branch);
                flakyTest.put("passCount", agg.passCount);
                flakyTest.put("failCount", agg.failCount);
                flakyTest.put("totalRuns", total);
                flakyTest.put("flakinessRate", Math.round(flakinessRate * 100.0) / 100.0);
                flakyTests.add(flakyTest);

                // Persist flaky test
                FlakyTest domainFlakyTest = FlakyTest.builder()
                        .id(UUID.randomUUID().toString())
                        .repo(agg.workflow)
                        .testName(entry.getKey())
                        .workflow(agg.workflow)
                        .passCount(agg.passCount)
                        .failCount(agg.failCount)
                        .flakinessRate(Math.round(flakinessRate * 100.0) / 100.0)
                        .detectedAt(Instant.now())
                        .build();
                repository.saveFlakyTest(domainFlakyTest);
            }
        }

        // Sort by flakinessRate descending
        flakyTests.sort(Comparator.comparingDouble(
                (Map<String, Object> m) -> (double) m.get("flakinessRate")).reversed());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("analyzedRuns", runs.size());
        result.put("flakyTestsFound", flakyTests.size());
        result.put("flakyTests", flakyTests);
        return result;
    }

    // ========================================================================
    // Helper classes and methods
    // ========================================================================

    private static class TestAggregation {
        int passCount = 0;
        int failCount = 0;
        String workflow = "";
        String branch = "";
    }

    private Map<String, Object> pipelineRunToMap(PipelineRun run) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", run.getId());
        map.put("title", run.getTitle());
        map.put("branch", run.getBranch());
        map.put("status", run.getStatus());
        map.put("conclusion", run.getConclusion());
        map.put("workflow", run.getWorkflow());
        map.put("createdAt", run.getCreatedAt() != null ? run.getCreatedAt().toString() : null);
        map.put("url", run.getUrl());
        return map;
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return "true".equalsIgnoreCase((String) value);
        }
        return false;
    }
}
