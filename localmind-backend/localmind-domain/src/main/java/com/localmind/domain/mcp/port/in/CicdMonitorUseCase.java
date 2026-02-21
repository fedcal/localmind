package com.localmind.domain.mcp.port.in;

import java.util.List;
import java.util.Map;

/**
 * Input port for the cicd-monitor tool group.
 * Provides pipeline listing, status retrieval, pipeline saving, and flaky test detection.
 */
public interface CicdMonitorUseCase {

    /**
     * Lists all pipeline runs saved for the given repository.
     *
     * @param owner the repository owner
     * @param repo  the repository name
     * @return list of pipeline runs as maps, or empty list with message if none found
     */
    List<Map<String, Object>> listPipelines(String owner, String repo);

    /**
     * Retrieves a specific pipeline run by its run ID.
     *
     * @param runId the pipeline run identifier
     * @return pipeline run details including jobs information
     */
    Map<String, Object> getPipelineStatus(String runId);

    /**
     * Saves a pipeline run to the repository.
     *
     * @param owner      the repository owner
     * @param repo       the repository name
     * @param runId      the pipeline run identifier
     * @param title      the pipeline run title
     * @param branch     the branch name
     * @param status     the pipeline status (e.g. completed, in_progress)
     * @param conclusion the pipeline conclusion (e.g. success, failure)
     * @param workflow   the workflow name
     * @param url        the pipeline run URL
     * @return save confirmation with id
     */
    Map<String, Object> savePipelineRun(String owner, String repo, String runId,
                                         String title, String branch, String status,
                                         String conclusion, String workflow, String url);

    /**
     * Analyzes a list of test run results to detect flaky tests.
     * Groups by testName, calculates flakiness rate as min(pass,fail)/total * 100 for tests
     * with both pass and fail results, and persists the results.
     *
     * @param runs list of run results [{testName, passed: boolean, workflow, branch}]
     * @return analysis result with analyzedRuns, flakyTestsFound, and flakyTests details
     */
    Map<String, Object> detectFlakyTests(List<Map<String, Object>> runs);
}
