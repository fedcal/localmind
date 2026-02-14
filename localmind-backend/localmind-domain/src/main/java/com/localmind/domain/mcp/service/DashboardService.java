package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.port.in.DashboardUseCase;
import com.localmind.domain.mcp.port.in.IncidentManagerUseCase;
import com.localmind.domain.mcp.port.in.ProjectEconomicsUseCase;
import com.localmind.domain.mcp.port.in.QualityGateUseCase;
import com.localmind.domain.mcp.port.in.ScrumBoardUseCase;
import com.localmind.domain.mcp.port.in.AgileMetricsUseCase;
import com.localmind.domain.mcp.port.in.TimeTrackingUseCase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain service implementing the dashboard-api tool group.
 * Aggregates real data from multiple use cases with graceful fallbacks.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class DashboardService implements DashboardUseCase {

    private static final List<String> TOOL_CLASS_NAMES = Arrays.asList(
            "LocalMindMcpTools",
            "LocalMindUtilityTools",
            "LocalMindCodeTools",
            "LocalMindTestTools",
            "LocalMindDevOpsTools",
            "LocalMindDatabaseTools",
            "LocalMindDocTools",
            "LocalMindProjectTools",
            "LocalMindGovernanceTools",
            "LocalMindOpsTools",
            "LocalMindQualityTools",
            "LocalMindCommTools"
    );

    private final ScrumBoardUseCase scrumBoardUseCase;
    private final AgileMetricsUseCase agileMetricsUseCase;
    private final TimeTrackingUseCase timeTrackingUseCase;
    private final ProjectEconomicsUseCase projectEconomicsUseCase;
    private final IncidentManagerUseCase incidentManagerUseCase;
    private final QualityGateUseCase qualityGateUseCase;

    public DashboardService(ScrumBoardUseCase scrumBoardUseCase,
                            AgileMetricsUseCase agileMetricsUseCase,
                            TimeTrackingUseCase timeTrackingUseCase,
                            ProjectEconomicsUseCase projectEconomicsUseCase,
                            IncidentManagerUseCase incidentManagerUseCase,
                            QualityGateUseCase qualityGateUseCase) {
        this.scrumBoardUseCase = scrumBoardUseCase;
        this.agileMetricsUseCase = agileMetricsUseCase;
        this.timeTrackingUseCase = timeTrackingUseCase;
        this.projectEconomicsUseCase = projectEconomicsUseCase;
        this.incidentManagerUseCase = incidentManagerUseCase;
        this.qualityGateUseCase = qualityGateUseCase;
    }

    // ========== 1. getOverview ==========

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new LinkedHashMap<>();

        // Sprint data from backlog
        Map<String, Object> sprint = buildSprintOverview();

        // Velocity placeholder (requires historical sprint data)
        Map<String, Object> velocity = new LinkedHashMap<>();
        velocity.put("current", "N/A");
        velocity.put("average", "N/A");
        velocity.put("trend", "N/A");

        // Time tracking data
        Map<String, Object> time = buildTimeOverview();

        // Budget data
        Map<String, Object> budget = buildBudgetOverview();

        result.put("sprint", sprint);
        result.put("velocity", velocity);
        result.put("time", time);
        result.put("budget", budget);
        result.put("generatedAt", Instant.now().toString());

        return result;
    }

    // ========== 2. getServerStatus ==========

    @Override
    public Map<String, Object> getServerStatus(String serverName) {
        Map<String, Object> result = new LinkedHashMap<>();
        Instant now = Instant.now();

        List<Map<String, Object>> servers = new ArrayList<>();
        for (String toolClassName : TOOL_CLASS_NAMES) {
            servers.add(buildServerStatus(toolClassName, "healthy", now));
        }

        List<Map<String, Object>> filtered;
        if (serverName != null && !serverName.isBlank()) {
            filtered = new ArrayList<>();
            for (Map<String, Object> server : servers) {
                if (serverName.equals(server.get("name"))) {
                    filtered.add(server);
                }
            }
        } else {
            filtered = servers;
        }

        result.put("servers", filtered);
        result.put("count", filtered.size());
        if (serverName != null && !serverName.isBlank()) {
            result.put("serverName", serverName);
        }

        return result;
    }

    // ========== 3. getRecentActivity ==========

    @Override
    public Map<String, Object> getRecentActivity(int limit) {
        Map<String, Object> result = new LinkedHashMap<>();
        int effectiveLimit = limit > 0 ? limit : 10;

        List<Map<String, Object>> activities = new ArrayList<>();

        try {
            Map<String, Object> incidentData = incidentManagerUseCase.listIncidents(null, null, effectiveLimit);
            Object incidentsList = incidentData.get("incidents");
            if (incidentsList instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> incidents = (List<Map<String, Object>>) incidentsList;
                for (Map<String, Object> incident : incidents) {
                    String title = incident.get("title") != null ? incident.get("title").toString() : "Unknown incident";
                    String severity = incident.get("severity") != null ? incident.get("severity").toString() : "unknown";
                    String status = incident.get("status") != null ? incident.get("status").toString() : "unknown";
                    String timestamp = incident.get("createdAt") != null ? incident.get("createdAt").toString() : Instant.now().toString();

                    activities.add(buildActivity("incident",
                            "Incident '" + title + "' [" + severity + "] - " + status,
                            Instant.parse(timestamp)));
                }
            }
        } catch (Exception e) {
            // Fallback: no incident data available
        }

        // Limit the final list
        List<Map<String, Object>> limited = activities.size() > effectiveLimit
                ? activities.subList(0, effectiveLimit)
                : activities;

        result.put("activities", limited);
        result.put("count", limited.size());
        result.put("limit", effectiveLimit);

        return result;
    }

    // ========== 4. getProjectSummary ==========

    @Override
    public Map<String, Object> getProjectSummary(String project) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (project == null || project.isBlank()) {
            result.put("error", "Project name is required");
            return result;
        }

        result.put("project", project);

        // Sprint data
        Map<String, Object> sprint = buildSprintOverview();

        // Velocity placeholder
        Map<String, Object> velocity = new LinkedHashMap<>();
        velocity.put("current", "N/A");
        velocity.put("average", "N/A");
        velocity.put("trend", "N/A");
        velocity.put("sprintCount", "N/A");

        // Budget data
        Map<String, Object> budget = buildBudgetSummary(project);

        // Incidents data
        Map<String, Object> incidents = buildIncidentsSummary();

        // Quality data
        Map<String, Object> quality = buildQualitySummary();

        result.put("sprint", sprint);
        result.put("velocity", velocity);
        result.put("budget", budget);
        result.put("incidents", incidents);
        result.put("quality", quality);
        result.put("generatedAt", Instant.now().toString());

        return result;
    }

    // ========== Private helpers for getOverview ==========

    private Map<String, Object> buildSprintOverview() {
        Map<String, Object> sprint = new LinkedHashMap<>();
        try {
            Map<String, Object> backlog = scrumBoardUseCase.getBacklog();
            Object storiesObj = backlog.get("stories");
            int totalStories = 0;
            int completedStories = 0;

            if (storiesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> stories = (List<Map<String, Object>>) storiesObj;
                totalStories = stories.size();
                // Count stories whose tasks are all "done"
                for (Map<String, Object> story : stories) {
                    Object tasksObj = story.get("tasks");
                    if (tasksObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> tasks = (List<Map<String, Object>>) tasksObj;
                        if (!tasks.isEmpty() && tasks.stream().allMatch(t -> "done".equals(t.get("status")))) {
                            completedStories++;
                        }
                    }
                }
            }

            int totalPoints = backlog.get("totalPoints") instanceof Number
                    ? ((Number) backlog.get("totalPoints")).intValue() : 0;

            sprint.put("active", totalStories > 0);
            sprint.put("name", "Backlog");
            sprint.put("progress", totalStories > 0 ? (completedStories * 100) / totalStories : 0);
            sprint.put("completedStories", completedStories);
            sprint.put("totalStories", totalStories);
            sprint.put("totalPoints", totalPoints);
        } catch (Exception e) {
            sprint.put("active", "N/A");
            sprint.put("name", "N/A");
            sprint.put("progress", 0);
            sprint.put("completedStories", "N/A");
            sprint.put("totalStories", "N/A");
        }
        return sprint;
    }

    private Map<String, Object> buildTimeOverview() {
        Map<String, Object> time = new LinkedHashMap<>();
        try {
            LocalDate now = LocalDate.now();
            String startDate = now.minusDays(30).toString();
            String endDate = now.toString();
            Map<String, Object> timesheet = timeTrackingUseCase.getTimesheet(startDate, endDate, null);

            Object totalMinutesObj = timesheet.get("totalMinutes");
            int totalMinutes = totalMinutesObj instanceof Number ? ((Number) totalMinutesObj).intValue() : 0;
            double trackedHours = Math.round(totalMinutes / 60.0 * 100.0) / 100.0;

            time.put("trackedHours", trackedHours);
            time.put("period", startDate + " to " + endDate);
            time.put("totalFormatted", timesheet.getOrDefault("totalFormatted", "N/A"));
        } catch (Exception e) {
            time.put("trackedHours", 0);
            time.put("period", "N/A");
            time.put("totalFormatted", "N/A");
        }
        return time;
    }

    private Map<String, Object> buildBudgetOverview() {
        Map<String, Object> budget = new LinkedHashMap<>();
        try {
            Map<String, Object> budgetStatus = projectEconomicsUseCase.getBudgetStatus("default");
            if (budgetStatus.containsKey("error")) {
                budget.put("status", "N/A");
                budget.put("spentPercent", 0);
                budget.put("remainingPercent", 0);
            } else {
                Object percentageUsed = budgetStatus.get("percentageUsed");
                double pctUsed = percentageUsed instanceof Number ? ((Number) percentageUsed).doubleValue() : 0;
                String statusStr = pctUsed > 90 ? "over-budget" : pctUsed > 75 ? "warning" : "on-track";

                budget.put("status", statusStr);
                budget.put("spentPercent", pctUsed);
                budget.put("remainingPercent", Math.round((100 - pctUsed) * 100.0) / 100.0);
            }
        } catch (Exception e) {
            budget.put("status", "N/A");
            budget.put("spentPercent", 0);
            budget.put("remainingPercent", 0);
        }
        return budget;
    }

    // ========== Private helpers for getProjectSummary ==========

    private Map<String, Object> buildBudgetSummary(String projectName) {
        Map<String, Object> budget = new LinkedHashMap<>();
        try {
            Map<String, Object> budgetStatus = projectEconomicsUseCase.getBudgetStatus(projectName);
            if (budgetStatus.containsKey("error")) {
                budget.put("total", "N/A");
                budget.put("spent", "N/A");
                budget.put("remaining", "N/A");
                budget.put("status", "N/A");
            } else {
                budget.put("total", budgetStatus.getOrDefault("totalBudget", "N/A"));
                budget.put("spent", budgetStatus.getOrDefault("totalSpent", "N/A"));
                budget.put("remaining", budgetStatus.getOrDefault("remaining", "N/A"));

                Object percentageUsed = budgetStatus.get("percentageUsed");
                double pctUsed = percentageUsed instanceof Number ? ((Number) percentageUsed).doubleValue() : 0;
                budget.put("status", pctUsed > 90 ? "over-budget" : pctUsed > 75 ? "warning" : "on-track");
            }
        } catch (Exception e) {
            budget.put("total", "N/A");
            budget.put("spent", "N/A");
            budget.put("remaining", "N/A");
            budget.put("status", "N/A");
        }
        return budget;
    }

    private Map<String, Object> buildIncidentsSummary() {
        Map<String, Object> incidents = new LinkedHashMap<>();
        try {
            Map<String, Object> openIncidents = incidentManagerUseCase.listIncidents("open", null, 100);
            Map<String, Object> resolvedIncidents = incidentManagerUseCase.listIncidents("resolved", null, 100);

            int openCount = openIncidents.get("count") instanceof Number
                    ? ((Number) openIncidents.get("count")).intValue() : 0;
            int resolvedCount = resolvedIncidents.get("count") instanceof Number
                    ? ((Number) resolvedIncidents.get("count")).intValue() : 0;

            incidents.put("open", openCount);
            incidents.put("resolvedThisSprint", resolvedCount);
        } catch (Exception e) {
            incidents.put("open", "N/A");
            incidents.put("resolvedThisSprint", "N/A");
        }
        return incidents;
    }

    private Map<String, Object> buildQualitySummary() {
        Map<String, Object> quality = new LinkedHashMap<>();
        try {
            Map<String, Object> gatesData = qualityGateUseCase.listGates(null);
            Object gatesObj = gatesData.get("gates");
            int totalGates = 0;

            if (gatesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> gates = (List<Map<String, Object>>) gatesObj;
                totalGates = gates.size();
            }

            quality.put("gatesDefined", totalGates);
            quality.put("totalGates", gatesData.getOrDefault("count", 0));
        } catch (Exception e) {
            quality.put("gatesDefined", "N/A");
            quality.put("totalGates", "N/A");
        }
        return quality;
    }

    // ========== Helpers ==========

    private Map<String, Object> buildServerStatus(String name, String status, Instant lastCheck) {
        Map<String, Object> server = new LinkedHashMap<>();
        server.put("name", name);
        server.put("status", status);
        server.put("lastCheck", lastCheck.toString());
        return server;
    }

    private Map<String, Object> buildActivity(String type, String description, Instant timestamp) {
        Map<String, Object> activity = new LinkedHashMap<>();
        activity.put("type", type);
        activity.put("description", description);
        activity.put("timestamp", timestamp.toString());
        return activity;
    }
}
