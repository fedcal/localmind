package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.FlakyTest;
import com.localmind.domain.mcp.model.PipelineRun;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting CI/CD monitor data.
 */
public interface CicdMonitorRepository {

    /**
     * Saves a pipeline run.
     *
     * @param pipelineRun the pipeline run to persist
     * @return the saved pipeline run (with generated id if new)
     */
    PipelineRun savePipelineRun(PipelineRun pipelineRun);

    /**
     * Finds all pipeline runs for a given repository.
     *
     * @param owner the repository owner
     * @param repo  the repository name
     * @return list of pipeline runs ordered by creation date descending
     */
    List<PipelineRun> findByRepo(String owner, String repo);

    /**
     * Finds a specific pipeline run by its run ID.
     *
     * @param runId the pipeline run identifier
     * @return optional containing the pipeline run if found
     */
    Optional<PipelineRun> findByRunId(String runId);

    /**
     * Saves a flaky test detection result.
     *
     * @param flakyTest the flaky test to persist
     * @return the saved flaky test (with generated id if new)
     */
    FlakyTest saveFlakyTest(FlakyTest flakyTest);
}
