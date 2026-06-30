package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.port.in.AgileMetricsUseCase;
import com.localmind.domain.mcp.port.in.IncidentManagerUseCase;
import com.localmind.domain.mcp.port.in.ProjectEconomicsUseCase;
import com.localmind.domain.mcp.port.in.QualityGateUseCase;
import com.localmind.domain.mcp.port.in.ScrumBoardUseCase;
import com.localmind.domain.mcp.port.in.TimeTrackingUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {

    @Mock
    private ScrumBoardUseCase scrumBoardUseCase;
    @Mock
    private AgileMetricsUseCase agileMetricsUseCase;
    @Mock
    private TimeTrackingUseCase timeTrackingUseCase;
    @Mock
    private ProjectEconomicsUseCase projectEconomicsUseCase;
    @Mock
    private IncidentManagerUseCase incidentManagerUseCase;
    @Mock
    private QualityGateUseCase qualityGateUseCase;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(scrumBoardUseCase, agileMetricsUseCase,
                timeTrackingUseCase, projectEconomicsUseCase,
                incidentManagerUseCase, qualityGateUseCase);

        // Default mocks for backlog
        Map<String, Object> backlog = new LinkedHashMap<>();
        List<Map<String, Object>> stories = new ArrayList<>();
        Map<String, Object> story1 = new LinkedHashMap<>();
        story1.put("id", "s1");
        story1.put("title", "Story 1");
        story1.put("storyPoints", 5);
        List<Map<String, Object>> tasks1 = new ArrayList<>();
        Map<String, Object> task1 = new LinkedHashMap<>();
        task1.put("status", "done");
        tasks1.add(task1);
        story1.put("tasks", tasks1);
        stories.add(story1);

        Map<String, Object> story2 = new LinkedHashMap<>();
        story2.put("id", "s2");
        story2.put("title", "Story 2");
        story2.put("storyPoints", 3);
        List<Map<String, Object>> tasks2 = new ArrayList<>();
        Map<String, Object> task2 = new LinkedHashMap<>();
        task2.put("status", "in_progress");
        tasks2.add(task2);
        story2.put("tasks", tasks2);
        stories.add(story2);

        backlog.put("stories", stories);
        backlog.put("totalPoints", 8);
        backlog.put("count", 2);
        when(scrumBoardUseCase.getBacklog()).thenReturn(backlog);

        // Default mocks for timesheet
        Map<String, Object> timesheet = new LinkedHashMap<>();
        timesheet.put("totalMinutes", 7200);
        timesheet.put("totalFormatted", "120:00");
        when(timeTrackingUseCase.getTimesheet(anyString(), anyString(), isNull())).thenReturn(timesheet);

        // Default mocks for budget
        Map<String, Object> budgetStatus = new LinkedHashMap<>();
        budgetStatus.put("totalBudget", 100000.0);
        budgetStatus.put("totalSpent", 60000.0);
        budgetStatus.put("remaining", 40000.0);
        budgetStatus.put("percentageUsed", 60.0);
        when(projectEconomicsUseCase.getBudgetStatus(anyString())).thenReturn(budgetStatus);

        // Default mocks for incidents
        Map<String, Object> openIncidents = new LinkedHashMap<>();
        openIncidents.put("incidents", new ArrayList<>());
        openIncidents.put("count", 2);
        when(incidentManagerUseCase.listIncidents("open", null, 100)).thenReturn(openIncidents);

        Map<String, Object> resolvedIncidents = new LinkedHashMap<>();
        resolvedIncidents.put("incidents", new ArrayList<>());
        resolvedIncidents.put("count", 5);
        when(incidentManagerUseCase.listIncidents("resolved", null, 100)).thenReturn(resolvedIncidents);

        Map<String, Object> allIncidents = new LinkedHashMap<>();
        allIncidents.put("incidents", new ArrayList<>());
        allIncidents.put("count", 0);
        when(incidentManagerUseCase.listIncidents(isNull(), isNull(), anyInt())).thenReturn(allIncidents);

        // Default mocks for quality gates
        Map<String, Object> gatesData = new LinkedHashMap<>();
        gatesData.put("gates", new ArrayList<>());
        gatesData.put("count", 3);
        when(qualityGateUseCase.listGates(isNull())).thenReturn(gatesData);
    }

    // ========== getOverview Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getOverview_returnsAllSections() {
        Map<String, Object> result = service.getOverview();

        assertThat(result).containsKeys("sprint", "velocity", "time", "budget", "generatedAt");

        Map<String, Object> sprint = (Map<String, Object>) result.get("sprint");
        assertThat(sprint.get("active")).isEqualTo(true);
        assertThat(sprint.get("completedStories")).isEqualTo(1);
        assertThat(sprint.get("totalStories")).isEqualTo(2);

        Map<String, Object> budget = (Map<String, Object>) result.get("budget");
        assertThat(budget.get("status")).isEqualTo("on-track");
    }

    @Test
    void getOverview_hasGeneratedAt() {
        Map<String, Object> result = service.getOverview();

        assertThat(result.get("generatedAt")).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getOverview_fallsBackOnException() {
        when(scrumBoardUseCase.getBacklog()).thenThrow(new RuntimeException("DB error"));
        when(timeTrackingUseCase.getTimesheet(anyString(), anyString(), isNull())).thenThrow(new RuntimeException("DB error"));
        when(projectEconomicsUseCase.getBudgetStatus(anyString())).thenThrow(new RuntimeException("DB error"));

        Map<String, Object> result = service.getOverview();

        assertThat(result).containsKeys("sprint", "velocity", "time", "budget", "generatedAt");
        Map<String, Object> sprint = (Map<String, Object>) result.get("sprint");
        assertThat(sprint.get("active")).isEqualTo("N/A");
    }

    // ========== getServerStatus Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getServerStatus_noFilter_returnsAllServers() {
        Map<String, Object> result = service.getServerStatus(null);

        List<Map<String, Object>> servers = (List<Map<String, Object>>) result.get("servers");
        assertThat(servers).hasSize(12);
        assertThat((int) result.get("count")).isEqualTo(12);

        for (Map<String, Object> server : servers) {
            assertThat(server).containsKeys("name", "status", "lastCheck");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getServerStatus_withFilter_filtersCorrectly() {
        Map<String, Object> result = service.getServerStatus("LocalMindMcpTools");

        List<Map<String, Object>> servers = (List<Map<String, Object>>) result.get("servers");
        assertThat(servers).hasSize(1);
        assertThat(servers.get(0).get("name")).isEqualTo("LocalMindMcpTools");
        assertThat(result.get("serverName")).isEqualTo("LocalMindMcpTools");
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
        assertThat(activities).isNotNull();
        assertThat(result.get("limit")).isEqualTo(10);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRecentActivity_withLimit_respectsLimit() {
        // Setup: mock incidents with 5 items
        Map<String, Object> incidentData = new LinkedHashMap<>();
        List<Map<String, Object>> incidentList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> incident = new LinkedHashMap<>();
            incident.put("title", "Incident " + i);
            incident.put("severity", "medium");
            incident.put("status", "open");
            incident.put("createdAt", "2026-01-01T00:00:00Z");
            incidentList.add(incident);
        }
        incidentData.put("incidents", incidentList);
        incidentData.put("count", 5);
        when(incidentManagerUseCase.listIncidents(isNull(), isNull(), anyInt())).thenReturn(incidentData);

        Map<String, Object> result = service.getRecentActivity(3);

        List<Map<String, Object>> activities = (List<Map<String, Object>>) result.get("activities");
        assertThat(activities).hasSize(3);
        assertThat(result.get("count")).isEqualTo(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRecentActivity_exceptionFallback_returnsEmptyList() {
        when(incidentManagerUseCase.listIncidents(isNull(), isNull(), anyInt()))
                .thenThrow(new RuntimeException("DB error"));

        Map<String, Object> result = service.getRecentActivity(10);

        List<Map<String, Object>> activities = (List<Map<String, Object>>) result.get("activities");
        assertThat(activities).isEmpty();
        assertThat(result.get("count")).isEqualTo(0);
    }

    // ========== getProjectSummary Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getProjectSummary_validProject_returnsSummary() {
        Map<String, Object> result = service.getProjectSummary("localmind");

        assertThat(result.get("project")).isEqualTo("localmind");
        assertThat(result).containsKeys("sprint", "velocity", "budget", "incidents", "quality", "generatedAt");

        Map<String, Object> sprint = (Map<String, Object>) result.get("sprint");
        assertThat(sprint).containsKeys("active", "completedStories", "totalStories");

        Map<String, Object> budget = (Map<String, Object>) result.get("budget");
        assertThat(budget).containsKeys("total", "spent", "remaining", "status");
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
