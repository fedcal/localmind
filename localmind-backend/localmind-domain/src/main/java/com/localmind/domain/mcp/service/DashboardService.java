package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.port.in.DashboardUseCase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain service implementing the dashboard-api tool group.
 * Standalone service with no persistence - generates simulated aggregated data.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class DashboardService implements DashboardUseCase {

    // ========== 1. getOverview ==========

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> sprint = new LinkedHashMap<>();
        sprint.put("active", true);
        sprint.put("name", "Current Sprint");
        sprint.put("progress", 65);
        sprint.put("daysRemaining", 5);
        sprint.put("completedStories", 8);
        sprint.put("totalStories", 12);

        Map<String, Object> velocity = new LinkedHashMap<>();
        velocity.put("current", 42);
        velocity.put("average", 40);
        velocity.put("trend", "stable");

        Map<String, Object> time = new LinkedHashMap<>();
        time.put("trackedHours", 120);
        time.put("estimatedHours", 160);
        time.put("utilizationPercent", 75);

        Map<String, Object> budget = new LinkedHashMap<>();
        budget.put("status", "on-track");
        budget.put("spentPercent", 60);
        budget.put("remainingPercent", 40);

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

        List<Map<String, Object>> servers = new ArrayList<>();
        Instant now = Instant.now();

        servers.add(buildServerStatus("localmind-core", "healthy", now));
        servers.add(buildServerStatus("localmind-devops", "healthy", now));
        servers.add(buildServerStatus("localmind-database", "healthy", now));
        servers.add(buildServerStatus("localmind-project", "healthy", now));
        servers.add(buildServerStatus("localmind-docs", "healthy", now));

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

        List<Map<String, Object>> allActivities = new ArrayList<>();
        Instant now = Instant.now();

        allActivities.add(buildActivity("sprint", "Sprint 'Current Sprint' started", now.minusSeconds(86400)));
        allActivities.add(buildActivity("story", "User story 'Login Feature' completed", now.minusSeconds(72000)));
        allActivities.add(buildActivity("deployment", "Deployment to staging environment successful", now.minusSeconds(54000)));
        allActivities.add(buildActivity("quality-gate", "Quality gate 'Production Readiness' passed", now.minusSeconds(43200)));
        allActivities.add(buildActivity("incident", "Incident 'Database slowdown' resolved", now.minusSeconds(36000)));
        allActivities.add(buildActivity("review", "Code review for PR #42 approved", now.minusSeconds(28800)));
        allActivities.add(buildActivity("budget", "Budget checkpoint: 60% utilized", now.minusSeconds(21600)));
        allActivities.add(buildActivity("test", "Test suite execution: 98% pass rate", now.minusSeconds(14400)));
        allActivities.add(buildActivity("task", "Task 'Implement API endpoint' moved to Done", now.minusSeconds(7200)));
        allActivities.add(buildActivity("metric", "Velocity metric updated: 42 story points", now.minusSeconds(3600)));
        allActivities.add(buildActivity("retrospective", "Sprint retrospective completed", now.minusSeconds(1800)));
        allActivities.add(buildActivity("documentation", "API documentation updated for v2", now.minusSeconds(900)));

        List<Map<String, Object>> limited = allActivities.size() > effectiveLimit
                ? allActivities.subList(0, effectiveLimit)
                : allActivities;

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

        Map<String, Object> sprint = new LinkedHashMap<>();
        sprint.put("name", "Sprint 12");
        sprint.put("status", "in-progress");
        sprint.put("completedPoints", 28);
        sprint.put("totalPoints", 42);

        Map<String, Object> velocity = new LinkedHashMap<>();
        velocity.put("current", 42);
        velocity.put("average", 38);
        velocity.put("trend", "improving");
        velocity.put("sprintCount", 12);

        Map<String, Object> budget = new LinkedHashMap<>();
        budget.put("total", 100000);
        budget.put("spent", 60000);
        budget.put("remaining", 40000);
        budget.put("status", "on-track");

        Map<String, Object> incidents = new LinkedHashMap<>();
        incidents.put("open", 2);
        incidents.put("resolvedThisSprint", 5);
        incidents.put("meanResolutionHours", 4.5);

        Map<String, Object> quality = new LinkedHashMap<>();
        quality.put("coverage", 82);
        quality.put("technicalDebt", "low");
        quality.put("gatesPassed", 3);
        quality.put("gatesFailed", 0);

        result.put("sprint", sprint);
        result.put("velocity", velocity);
        result.put("budget", budget);
        result.put("incidents", incidents);
        result.put("quality", quality);
        result.put("generatedAt", Instant.now().toString());

        return result;
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
