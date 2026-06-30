package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.mcp.port.in.CicdMonitorUseCase;
import com.localmind.domain.mcp.port.in.DockerAnalysisUseCase;
import com.localmind.domain.mcp.port.in.LogAnalyzerUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindDevOpsTools {

    private final DockerAnalysisUseCase dockerAnalysisUseCase;
    private final LogAnalyzerUseCase logAnalyzerUseCase;
    private final CicdMonitorUseCase cicdMonitorUseCase;

    public LocalMindDevOpsTools(DockerAnalysisUseCase dockerAnalysisUseCase,
                                LogAnalyzerUseCase logAnalyzerUseCase,
                                CicdMonitorUseCase cicdMonitorUseCase) {
        this.dockerAnalysisUseCase = dockerAnalysisUseCase;
        this.logAnalyzerUseCase = logAnalyzerUseCase;
        this.cicdMonitorUseCase = cicdMonitorUseCase;
    }

    // ==================== DOCKER ANALYSIS ====================

    @Tool(description = "Parse docker-compose YAML content and extract services, networks, volumes. "
            + "Performs validation checks for missing image/build directives, :latest tags, "
            + "privileged mode, and host network mode. Returns serviceCount, services, networks, "
            + "volumes, and validationIssues.")
    public Map<String, Object> parseCompose(
            @ToolParam(description = "The docker-compose YAML content as a string") String content) {
        return dockerAnalysisUseCase.parseCompose(content);
    }

    @Tool(description = "Analyze Dockerfile content for best-practice violations. Parses instructions "
            + "and checks for :latest tags, missing tags, large base images, consecutive RUN instructions, "
            + "ADD vs COPY usage, missing HEALTHCHECK, and running as root. Returns baseImage, stages, "
            + "totalInstructions, issues with severity/rule/suggestion, and a summary.")
    public Map<String, Object> analyzeDockerfile(
            @ToolParam(description = "The Dockerfile content as a string") String content) {
        return dockerAnalysisUseCase.analyzeDockerfile(content);
    }

    @Tool(description = "List common Docker services with their metadata including name, image, "
            + "default ports, and description. Covers databases (MySQL, PostgreSQL, MongoDB), "
            + "caches (Redis), message brokers (RabbitMQ, Kafka), web servers (Nginx, Apache), "
            + "monitoring (Grafana, Prometheus), and more.")
    public Map<String, Object> listDockerServices() {
        return dockerAnalysisUseCase.listDockerServices();
    }

    @Tool(description = "Generate docker-compose YAML from a list of service definitions. Each service "
            + "can specify name, image, ports, environment variables, and volumes. Automatically adds "
            + "restart policy and a named volumes section. Returns the generated YAML string and serviceCount.")
    public Map<String, Object> generateCompose(
            @ToolParam(description = "List of service definitions. Each map should contain: "
                    + "'name' (string, required), 'image' (string), 'ports' (list of strings like '3306:3306'), "
                    + "'environment' (map of key-value pairs), 'volumes' (list of strings like 'data:/var/lib/mysql')")
            List<Map<String, Object>> services) {
        return dockerAnalysisUseCase.generateCompose(services);
    }

    // ==================== LOG ANALYZER ====================

    @Tool(description = "Analyze log file content: count log levels (INFO, WARN, ERROR, DEBUG), "
            + "extract top error messages, detect time range, and identify the log format. "
            + "Returns totalLines, levels breakdown, topErrors, timeRange, and detected format.")
    public Map<String, Object> analyzeLogFile(
            @ToolParam(description = "The log file content as a string") String content,
            @ToolParam(description = "The log format: 'auto' (detect automatically), 'json', or 'plain'. Defaults to 'auto'")
            String format) {
        return logAnalyzerUseCase.analyzeLogFile(content, format);
    }

    @Tool(description = "Find recurring error patterns in log content by normalizing error messages "
            + "and grouping by pattern. Returns patterns with occurrence count, sorted by frequency. "
            + "Useful for identifying systematic issues in application logs.")
    public Map<String, Object> findErrorPatterns(
            @ToolParam(description = "The log file content as a string") String content,
            @ToolParam(description = "Minimum number of occurrences to include a pattern (default 2)")
            int minCount) {
        return logAnalyzerUseCase.findErrorPatterns(content, minCount);
    }

    @Tool(description = "Return the last N lines of log content, optionally filtered by a case-insensitive "
            + "keyword. Simulates the Unix 'tail' command with optional grep-like filtering. "
            + "Returns totalLines, returnedLines count, applied filter, and the matching content.")
    public Map<String, Object> tailLog(
            @ToolParam(description = "The log file content as a string") String content,
            @ToolParam(description = "Number of lines to return (default 50)") int lines,
            @ToolParam(description = "Optional case-insensitive keyword filter. Pass null or empty for no filter")
            String filter) {
        return logAnalyzerUseCase.tailLog(content, lines, filter);
    }

    @Tool(description = "Generate a human-readable summary of log content including total lines, "
            + "log level distribution, error rate percentage, and warning rate percentage. "
            + "Provides a quick overview of log health.")
    public Map<String, Object> generateLogSummary(
            @ToolParam(description = "The log file content as a string") String content) {
        return logAnalyzerUseCase.generateLogSummary(content);
    }

    // ==================== CI/CD MONITOR ====================

    @Tool(description = "List all saved pipeline runs for a given GitHub repository. "
            + "Returns a list of pipeline run summaries including runId, title, branch, status, "
            + "conclusion, workflow, and URL. Returns empty list with message if none found.")
    public List<Map<String, Object>> listPipelines(
            @ToolParam(description = "The repository owner (e.g. 'octocat')") String owner,
            @ToolParam(description = "The repository name (e.g. 'hello-world')") String repo) {
        return cicdMonitorUseCase.listPipelines(owner, repo);
    }

    @Tool(description = "Retrieve the status and details of a specific pipeline run by its run ID. "
            + "Returns pipeline run details including jobs information, status, and conclusion.")
    public Map<String, Object> getPipelineStatus(
            @ToolParam(description = "The pipeline run identifier") String runId) {
        return cicdMonitorUseCase.getPipelineStatus(runId);
    }

    @Tool(description = "Save a pipeline run record to the local database for tracking. "
            + "Stores pipeline metadata including owner, repo, runId, title, branch, status, "
            + "conclusion, workflow name, and URL. Returns save confirmation with generated ID.")
    public Map<String, Object> savePipelineRun(
            @ToolParam(description = "The repository owner") String owner,
            @ToolParam(description = "The repository name") String repo,
            @ToolParam(description = "The pipeline run identifier") String runId,
            @ToolParam(description = "The pipeline run title or commit message") String title,
            @ToolParam(description = "The branch name") String branch,
            @ToolParam(description = "The pipeline status: completed, in_progress, queued") String status,
            @ToolParam(description = "The pipeline conclusion: success, failure, cancelled, skipped") String conclusion,
            @ToolParam(description = "The workflow name (e.g. 'CI', 'Deploy')") String workflow,
            @ToolParam(description = "The pipeline run URL") String url) {
        return cicdMonitorUseCase.savePipelineRun(owner, repo, runId, title, branch, status, conclusion, workflow, url);
    }

    @Tool(description = "Analyze a list of test run results to detect flaky tests. Groups results by "
            + "testName, calculates flakiness rate as min(pass,fail)/total * 100 for tests with both "
            + "pass and fail outcomes, and persists the results. Returns analyzedRuns count, "
            + "flakyTestsFound count, and detailed flakyTests list with flakiness percentages.")
    public Map<String, Object> detectFlakyTests(
            @ToolParam(description = "List of test run results. Each map should contain: "
                    + "'testName' (string), 'passed' (boolean), 'workflow' (string), 'branch' (string)")
            List<Map<String, Object>> runs) {
        return cicdMonitorUseCase.detectFlakyTests(runs);
    }
}
