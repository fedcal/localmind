package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.mcp.port.in.DashboardUseCase;
import com.localmind.domain.mcp.port.in.InsightEngineUseCase;
import com.localmind.domain.mcp.port.in.McpInternalRegistryUseCase;
import com.localmind.domain.mcp.port.in.QualityGateUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindQualityTools {

    private final QualityGateUseCase qualityGateUseCase;
    private final InsightEngineUseCase insightEngineUseCase;
    private final DashboardUseCase dashboardUseCase;
    private final McpInternalRegistryUseCase mcpInternalRegistryUseCase;

    public LocalMindQualityTools(QualityGateUseCase qualityGateUseCase,
                                  InsightEngineUseCase insightEngineUseCase,
                                  DashboardUseCase dashboardUseCase,
                                  McpInternalRegistryUseCase mcpInternalRegistryUseCase) {
        this.qualityGateUseCase = qualityGateUseCase;
        this.insightEngineUseCase = insightEngineUseCase;
        this.dashboardUseCase = dashboardUseCase;
        this.mcpInternalRegistryUseCase = mcpInternalRegistryUseCase;
    }

    // ==================== QUALITY GATE ====================

    @Tool(description = "Define a new quality gate with a set of metric checks. Each check specifies "
            + "a metric name, comparison operator (>=, <=, >, <, ==, !=), and threshold value. "
            + "Returns the gate ID, name, projectName, checks and createdAt.")
    public Map<String, Object> defineGate(
            @ToolParam(description = "Quality gate name (e.g. 'Production Readiness')") String name,
            @ToolParam(description = "Project name (optional, can be null)") String projectName,
            @ToolParam(description = "JSON array of checks, e.g. [{\"metric\":\"coverage\",\"operator\":\">=\",\"threshold\":80}]") String checksJson) {
        return qualityGateUseCase.defineGate(name, projectName, checksJson);
    }

    @Tool(description = "Evaluate a quality gate against provided metrics. Checks each metric against "
            + "its threshold using the defined operator. All checks must pass for the gate to pass. "
            + "Returns gateId, gateName, passed (boolean), per-check results, and evaluatedAt.")
    public Map<String, Object> evaluateGate(
            @ToolParam(description = "The quality gate ID to evaluate") String gateId,
            @ToolParam(description = "JSON object mapping metric names to values, e.g. {\"coverage\":85,\"bugs\":3}") String metricsJson) {
        return qualityGateUseCase.evaluateGate(gateId, metricsJson);
    }

    @Tool(description = "List all defined quality gates, optionally filtered by project name. "
            + "Returns gates array with id, name, projectName, checks, createdAt, and total count.")
    public Map<String, Object> listGates(
            @ToolParam(description = "Optional project name filter (null for all gates)") String projectName) {
        return qualityGateUseCase.listGates(projectName);
    }

    @Tool(description = "Get the evaluation history of a quality gate, ordered by most recent first. "
            + "Returns gateId, evaluations array with pass/fail status and metrics, and count.")
    public Map<String, Object> getGateHistory(
            @ToolParam(description = "The quality gate ID") String gateId,
            @ToolParam(description = "Maximum number of evaluations to return") int limit) {
        return qualityGateUseCase.getGateHistory(gateId, limit);
    }

    // ==================== INSIGHT ENGINE ====================

    @Tool(description = "Query an insight using natural language. Analyzes the question and generates "
            + "contextual insights about velocity, budget, sprint, incidents, or quality. "
            + "Returns question, insight text, confidence score, and suggested actions.")
    public Map<String, Object> queryInsight(
            @ToolParam(description = "Natural language question about project metrics or health") String question) {
        return insightEngineUseCase.queryInsight(question);
    }

    @Tool(description = "Correlate multiple metrics to find relationships between them. "
            + "Generates correlation analysis (strong/moderate/weak/negligible) for each metric pair. "
            + "Returns metrics list, correlations with strength/direction/interpretation, and period.")
    public Map<String, Object> correlateMetrics(
            @ToolParam(description = "JSON array of metric names, e.g. [\"velocity\",\"coverage\",\"bugs\"]") String metricsJson,
            @ToolParam(description = "Time period for analysis, e.g. 'last-sprint', 'last-month'") String period) {
        return insightEngineUseCase.correlateMetrics(metricsJson, period);
    }

    @Tool(description = "Explain an observed trend in a specific metric. Provides analysis of why "
            + "a metric is increasing, decreasing, or stable. Returns metric, direction, explanation, "
            + "contributing factors, and confidence score.")
    public Map<String, Object> explainTrend(
            @ToolParam(description = "The metric name to explain") String metric,
            @ToolParam(description = "Trend direction: 'increasing', 'decreasing', or 'stable'") String direction,
            @ToolParam(description = "Time period for analysis") String period) {
        return insightEngineUseCase.explainTrend(metric, direction, period);
    }

    @Tool(description = "Generate an aggregated health dashboard showing overall project health. "
            + "Returns overallHealth (good/warning/critical), overallScore, and area-level breakdowns "
            + "for sprint, code quality, CI/CD, budget, incidents, and velocity.")
    public Map<String, Object> healthDashboard() {
        return insightEngineUseCase.healthDashboard();
    }

    // ==================== DASHBOARD API ====================

    @Tool(description = "Get a dashboard overview with aggregated sprint progress, velocity trend, "
            + "time tracking utilization, and budget status. Returns sprint, velocity, time, "
            + "budget sections and generatedAt timestamp.")
    public Map<String, Object> getOverview() {
        return dashboardUseCase.getOverview();
    }

    @Tool(description = "Get the status of MCP servers. Optionally filter by server name. "
            + "Returns servers array with name, status, lastCheck, and count.")
    public Map<String, Object> getServerStatus(
            @ToolParam(description = "Optional server name to filter (null for all servers)") String serverName) {
        return dashboardUseCase.getServerStatus(serverName);
    }

    @Tool(description = "Get recent project activity entries. Returns a list of activities "
            + "with type, description, timestamp, and the total count.")
    public Map<String, Object> getRecentActivity(
            @ToolParam(description = "Maximum number of activities to return (default 10)") int limit) {
        return dashboardUseCase.getRecentActivity(limit);
    }

    @Tool(description = "Get a comprehensive project summary including sprint status, velocity, "
            + "budget, incidents, and quality metrics. Returns all sections for the specified project.")
    public Map<String, Object> getProjectSummary(
            @ToolParam(description = "The project name") String project) {
        return dashboardUseCase.getProjectSummary(project);
    }

    // ==================== MCP REGISTRY ====================

    @Tool(description = "Register an internal MCP server in the in-memory registry. "
            + "Returns registration details with generated ID, name, URL, transport, "
            + "capabilities, and registeredAt timestamp.")
    public Map<String, Object> registerInternalServer(
            @ToolParam(description = "Server name") String name,
            @ToolParam(description = "Server URL (e.g. 'http://localhost:8080')") String url,
            @ToolParam(description = "Transport type: 'sse' or 'stdio'") String transport,
            @ToolParam(description = "JSON array of capabilities, e.g. [\"tool-execution\",\"resource-reading\"]") String capabilitiesJson) {
        return mcpInternalRegistryUseCase.registerInternalServer(name, url, transport, capabilitiesJson);
    }

    @Tool(description = "Discover registered MCP servers, optionally filtered by status and/or transport. "
            + "Returns servers array and count.")
    public Map<String, Object> discoverServers(
            @ToolParam(description = "Optional status filter (e.g. 'healthy'), null for all") String status,
            @ToolParam(description = "Optional transport filter (e.g. 'sse'), null for all") String transport) {
        return mcpInternalRegistryUseCase.discoverServers(status, transport);
    }

    @Tool(description = "Perform a health check on a registered MCP server. "
            + "Returns serverId, name, status, response time in ms, and checkedAt timestamp.")
    public Map<String, Object> mcpHealthCheck(
            @ToolParam(description = "The server ID to health check") String serverId) {
        return mcpInternalRegistryUseCase.healthCheck(serverId);
    }

    @Tool(description = "Get the capabilities of a registered MCP server. "
            + "Returns serverId, name, and capabilities list.")
    public Map<String, Object> getServerCapabilities(
            @ToolParam(description = "The server ID") String serverId) {
        return mcpInternalRegistryUseCase.getCapabilities(serverId);
    }
}
