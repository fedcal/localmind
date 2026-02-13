package com.localmind.domain.mcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardServiceTest {

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService();
    }

    // ========== getOverview Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getOverview_returnsAllSections() {
        Map<String, Object> result = service.getOverview();

        assertThat(result).containsKeys("sprint", "velocity", "time", "budget", "generatedAt");

        Map<String, Object> sprint = (Map<String, Object>) result.get("sprint");
        assertThat(sprint.get("active")).isEqualTo(true);
        assertThat(sprint.get("name")).isEqualTo("Current Sprint");

        Map<String, Object> velocity = (Map<String, Object>) result.get("velocity");
        assertThat(velocity.get("trend")).isEqualTo("stable");

        Map<String, Object> budget = (Map<String, Object>) result.get("budget");
        assertThat(budget.get("status")).isEqualTo("on-track");
    }

    @Test
    void getOverview_hasGeneratedAt() {
        Map<String, Object> result = service.getOverview();

        assertThat(result.get("generatedAt")).isNotNull();
    }

    // ========== getServerStatus Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getServerStatus_noFilter_returnsAllServers() {
        Map<String, Object> result = service.getServerStatus(null);

        List<Map<String, Object>> servers = (List<Map<String, Object>>) result.get("servers");
        assertThat(servers).isNotEmpty();
        assertThat((int) result.get("count")).isEqualTo(servers.size());

        for (Map<String, Object> server : servers) {
            assertThat(server).containsKeys("name", "status", "lastCheck");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getServerStatus_withFilter_filtersCorrectly() {
        Map<String, Object> result = service.getServerStatus("localmind-core");

        List<Map<String, Object>> servers = (List<Map<String, Object>>) result.get("servers");
        assertThat(servers).hasSize(1);
        assertThat(servers.get(0).get("name")).isEqualTo("localmind-core");
        assertThat(result.get("serverName")).isEqualTo("localmind-core");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getServerStatus_unknownServer_returnsEmptyList() {
        Map<String, Object> result = service.getServerStatus("unknown-server");

        List<Map<String, Object>> servers = (List<Map<String, Object>>) result.get("servers");
        assertThat(servers).isEmpty();
        assertThat(result.get("count")).isEqualTo(0);
    }

    // ========== getRecentActivity Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getRecentActivity_defaultLimit_returnsActivities() {
        Map<String, Object> result = service.getRecentActivity(0);

        List<Map<String, Object>> activities = (List<Map<String, Object>>) result.get("activities");
        assertThat(activities).hasSize(10); // default limit
        assertThat(result.get("limit")).isEqualTo(10);

        for (Map<String, Object> activity : activities) {
            assertThat(activity).containsKeys("type", "description", "timestamp");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRecentActivity_withLimit_respectsLimit() {
        Map<String, Object> result = service.getRecentActivity(3);

        List<Map<String, Object>> activities = (List<Map<String, Object>>) result.get("activities");
        assertThat(activities).hasSize(3);
        assertThat(result.get("count")).isEqualTo(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRecentActivity_largeLimitReturnsAll() {
        Map<String, Object> result = service.getRecentActivity(100);

        List<Map<String, Object>> activities = (List<Map<String, Object>>) result.get("activities");
        assertThat(activities.size()).isGreaterThanOrEqualTo(10);
    }

    // ========== getProjectSummary Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getProjectSummary_validProject_returnsSummary() {
        Map<String, Object> result = service.getProjectSummary("localmind");

        assertThat(result.get("project")).isEqualTo("localmind");
        assertThat(result).containsKeys("sprint", "velocity", "budget", "incidents", "quality", "generatedAt");

        Map<String, Object> sprint = (Map<String, Object>) result.get("sprint");
        assertThat(sprint).containsKeys("name", "status", "completedPoints", "totalPoints");

        Map<String, Object> quality = (Map<String, Object>) result.get("quality");
        assertThat(quality).containsKeys("coverage", "technicalDebt");
    }

    @Test
    void getProjectSummary_nullProject_returnsError() {
        Map<String, Object> result = service.getProjectSummary(null);

        assertThat(result.get("error")).isEqualTo("Project name is required");
    }

    @Test
    void getProjectSummary_blankProject_returnsError() {
        Map<String, Object> result = service.getProjectSummary("");

        assertThat(result.get("error")).isEqualTo("Project name is required");
    }
}
