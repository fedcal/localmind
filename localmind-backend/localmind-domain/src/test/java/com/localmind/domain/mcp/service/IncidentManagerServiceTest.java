package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.Incident;
import com.localmind.domain.mcp.port.out.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IncidentManagerServiceTest {

    @Mock
    private IncidentRepository repository;

    private IncidentManagerService service;

    @BeforeEach
    void setUp() {
        service = new IncidentManagerService(repository);
        when(repository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== openIncident Tests ==========

    @Test
    void openIncident_withValidData_createsIncident() {
        Map<String, Object> result = service.openIncident("DB outage", "critical", "Database is down", null);

        assertThat(result.get("title")).isEqualTo("DB outage");
        assertThat(result.get("severity")).isEqualTo("critical");
        assertThat(result.get("status")).isEqualTo("open");
        assertThat(result.get("description")).isEqualTo("Database is down");
        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("createdAt")).isNotNull();
    }

    @Test
    void openIncident_withAffectedSystems_includesThem() {
        String affectedSystems = "[\"api-gateway\",\"auth-service\"]";
        Map<String, Object> result = service.openIncident("Network issue", "high", "Connectivity problems", affectedSystems);

        assertThat(result.get("affectedSystems")).isEqualTo(affectedSystems);
    }

    @Test
    void openIncident_withNullTitle_returnsError() {
        Map<String, Object> result = service.openIncident(null, "high", "desc", null);

        assertThat(result.get("error")).isEqualTo("Title is required");
        verify(repository, never()).save(any());
    }

    @Test
    void openIncident_withBlankTitle_returnsError() {
        Map<String, Object> result = service.openIncident("  ", "high", "desc", null);

        assertThat(result.get("error")).isEqualTo("Title is required");
        verify(repository, never()).save(any());
    }

    @Test
    void openIncident_withNullSeverity_returnsError() {
        Map<String, Object> result = service.openIncident("title", null, "desc", null);

        assertThat(result.get("error")).isEqualTo("Severity is required");
        verify(repository, never()).save(any());
    }

    @Test
    void openIncident_withBlankSeverity_returnsError() {
        Map<String, Object> result = service.openIncident("title", "", "desc", null);

        assertThat(result.get("error")).isEqualTo("Severity is required");
        verify(repository, never()).save(any());
    }

    @Test
    void openIncident_withNullDescription_returnsError() {
        Map<String, Object> result = service.openIncident("title", "high", null, null);

        assertThat(result.get("error")).isEqualTo("Description is required");
        verify(repository, never()).save(any());
    }

    @Test
    void openIncident_withBlankDescription_returnsError() {
        Map<String, Object> result = service.openIncident("title", "high", "  ", null);

        assertThat(result.get("error")).isEqualTo("Description is required");
        verify(repository, never()).save(any());
    }

    @Test
    void openIncident_withInvalidSeverity_returnsError() {
        Map<String, Object> result = service.openIncident("title", "urgent", "desc", null);

        assertThat(result.get("error")).asString().contains("Invalid severity");
        verify(repository, never()).save(any());
    }

    @Test
    void openIncident_savesIncidentWithInitialTimeline() {
        service.openIncident("title", "medium", "desc", null);

        ArgumentCaptor<Incident> captor = ArgumentCaptor.forClass(Incident.class);
        verify(repository).save(captor.capture());
        Incident saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("open");
        assertThat(saved.getTimelineJson()).contains("Incident opened");
        assertThat(saved.getTimelineJson()).contains("system");
    }

    @Test
    void openIncident_normalizesSeverityToLowercase() {
        Map<String, Object> result = service.openIncident("title", "HIGH", "desc", null);

        assertThat(result.get("severity")).isEqualTo("high");
    }

    // ========== updateIncident Tests ==========

    @Test
    void updateIncident_withValidStatus_updatesStatus() {
        Incident existing = buildIncident("inc-1", "open",
                "[{\"timestamp\":\"2026-01-01T00:00:00Z\",\"description\":\"Incident opened\",\"source\":\"system\"}]");
        when(repository.findById("inc-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.updateIncident("inc-1", "investigating", null);

        assertThat(result.get("status")).isEqualTo("investigating");
        assertThat(result.get("updated")).isEqualTo(true);
    }

    @Test
    void updateIncident_withNote_addsToTimeline() {
        Incident existing = buildIncident("inc-1", "open",
                "[{\"timestamp\":\"2026-01-01T00:00:00Z\",\"description\":\"Incident opened\",\"source\":\"system\"}]");
        when(repository.findById("inc-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.updateIncident("inc-1", null, "Checking logs");

        assertThat(result.get("updated")).isEqualTo(true);
        assertThat((int) result.get("timelineEntries")).isGreaterThanOrEqualTo(2);
    }

    @Test
    void updateIncident_withInvalidStatus_returnsError() {
        Incident existing = buildIncident("inc-1", "open", "[]");
        when(repository.findById("inc-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.updateIncident("inc-1", "invalid-status", null);

        assertThat(result.get("error")).asString().contains("Invalid status");
    }

    @Test
    void updateIncident_withNullId_returnsError() {
        Map<String, Object> result = service.updateIncident(null, "investigating", null);

        assertThat(result.get("error")).isEqualTo("Incident id is required");
    }

    @Test
    void updateIncident_withNonExistentId_returnsError() {
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.updateIncident("non-existent", "investigating", null);

        assertThat(result.get("error")).asString().contains("Incident not found");
    }

    @Test
    void updateIncident_withStatusAndNote_updatesBoth() {
        Incident existing = buildIncident("inc-1", "open", "[]");
        when(repository.findById("inc-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.updateIncident("inc-1", "mitigating", "Applying fix");

        assertThat(result.get("status")).isEqualTo("mitigating");
        assertThat(result.get("updated")).isEqualTo(true);
        // Timeline should have status change + note = 2 entries
        assertThat((int) result.get("timelineEntries")).isGreaterThanOrEqualTo(2);
    }

    // ========== addTimelineEntry Tests ==========

    @Test
    void addTimelineEntry_withValidData_addsEntry() {
        Incident existing = buildIncident("inc-1", "investigating",
                "[{\"timestamp\":\"2026-01-01T00:00:00Z\",\"description\":\"Incident opened\",\"source\":\"system\"}]");
        when(repository.findById("inc-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.addTimelineEntry("inc-1", "Identified root cause", "oncall");

        assertThat(result.get("incidentId")).isEqualTo("inc-1");
        Map<String, Object> entry = (Map<String, Object>) result.get("entry");
        assertThat(entry.get("description")).isEqualTo("Identified root cause");
        assertThat(entry.get("source")).isEqualTo("oncall");
        assertThat(entry.get("timestamp")).isNotNull();
        assertThat((int) result.get("totalEntries")).isEqualTo(2);
    }

    @Test
    void addTimelineEntry_withNullSource_defaultsToUser() {
        Incident existing = buildIncident("inc-1", "open", "[]");
        when(repository.findById("inc-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.addTimelineEntry("inc-1", "Some observation", null);

        Map<String, Object> entry = (Map<String, Object>) result.get("entry");
        assertThat(entry.get("source")).isEqualTo("user");
    }

    @Test
    void addTimelineEntry_withNullIncidentId_returnsError() {
        Map<String, Object> result = service.addTimelineEntry(null, "desc", "system");

        assertThat(result.get("error")).isEqualTo("Incident id is required");
    }

    @Test
    void addTimelineEntry_withBlankDescription_returnsError() {
        Map<String, Object> result = service.addTimelineEntry("inc-1", "  ", "system");

        assertThat(result.get("error")).isEqualTo("Description is required");
    }

    @Test
    void addTimelineEntry_withNonExistentIncident_returnsError() {
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.addTimelineEntry("non-existent", "desc", "system");

        assertThat(result.get("error")).asString().contains("Incident not found");
    }

    // ========== resolveIncident Tests ==========

    @Test
    void resolveIncident_withValidData_resolvesIncident() {
        Incident existing = buildIncident("inc-1", "mitigating",
                "[{\"timestamp\":\"2026-01-01T00:00:00Z\",\"description\":\"Incident opened\",\"source\":\"system\"}]");
        when(repository.findById("inc-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.resolveIncident("inc-1", "Restarted service", "Memory leak in pool");

        assertThat(result.get("id")).isEqualTo("inc-1");
        assertThat(result.get("status")).isEqualTo("resolved");
        assertThat(result.get("resolution")).isEqualTo("Restarted service");
        assertThat(result.get("rootCause")).isEqualTo("Memory leak in pool");
        assertThat(result.get("resolvedAt")).isNotNull();
        assertThat(result.get("duration")).isNotNull();
    }

    @Test
    void resolveIncident_savesWithResolvedStatus() {
        Incident existing = buildIncident("inc-1", "investigating", "[]");
        when(repository.findById("inc-1")).thenReturn(Optional.of(existing));

        service.resolveIncident("inc-1", "Fixed", "Bug");

        ArgumentCaptor<Incident> captor = ArgumentCaptor.forClass(Incident.class);
        verify(repository).save(captor.capture());
        Incident saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("resolved");
        assertThat(saved.getResolution()).isEqualTo("Fixed");
        assertThat(saved.getRootCause()).isEqualTo("Bug");
        assertThat(saved.getResolvedAt()).isNotNull();
        assertThat(saved.getTimelineJson()).contains("Incident resolved");
    }

    @Test
    void resolveIncident_withNullId_returnsError() {
        Map<String, Object> result = service.resolveIncident(null, "res", "cause");

        assertThat(result.get("error")).isEqualTo("Incident id is required");
    }

    @Test
    void resolveIncident_withNonExistentId_returnsError() {
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.resolveIncident("non-existent", "res", "cause");

        assertThat(result.get("error")).asString().contains("Incident not found");
    }

    // ========== generatePostmortem Tests ==========

    @Test
    void generatePostmortem_withValidIncident_generatesMarkdown() {
        Incident existing = Incident.builder()
                .id("inc-1")
                .title("DB Outage")
                .severity("critical")
                .status("resolved")
                .description("Database went down")
                .affectedSystemsJson("[\"api\",\"auth\"]")
                .timelineJson("[{\"timestamp\":\"2026-01-01T10:00:00Z\",\"description\":\"Incident opened\",\"source\":\"system\"},{\"timestamp\":\"2026-01-01T12:00:00Z\",\"description\":\"Incident resolved\",\"source\":\"system\"}]")
                .resolution("Restarted database")
                .rootCause("Disk full")
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .resolvedAt(Instant.parse("2026-01-01T12:00:00Z"))
                .build();
        when(repository.findById("inc-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.generatePostmortem("inc-1");

        assertThat(result.get("incidentId")).isEqualTo("inc-1");
        String postmortem = (String) result.get("postmortem");
        assertThat(postmortem).contains("# Post-Mortem: DB Outage");
        assertThat(postmortem).contains("critical");
        assertThat(postmortem).contains("Database went down");
        assertThat(postmortem).contains("Restarted database");
        assertThat(postmortem).contains("Disk full");
        assertThat(postmortem).contains("Affected Systems");
        assertThat(postmortem).contains("Timeline");
        assertThat(postmortem).contains("Action Items");
        assertThat(result.get("generatedAt")).isNotNull();
    }

    @Test
    void generatePostmortem_withNullId_returnsError() {
        Map<String, Object> result = service.generatePostmortem(null);

        assertThat(result.get("error")).isEqualTo("Incident id is required");
    }

    @Test
    void generatePostmortem_withNonExistentId_returnsError() {
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.generatePostmortem("non-existent");

        assertThat(result.get("error")).asString().contains("Incident not found");
    }

    @Test
    void generatePostmortem_includesDuration() {
        Incident existing = Incident.builder()
                .id("inc-1")
                .title("Outage")
                .severity("high")
                .status("resolved")
                .description("Desc")
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .resolvedAt(Instant.parse("2026-01-01T12:30:00Z"))
                .build();
        when(repository.findById("inc-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.generatePostmortem("inc-1");

        String postmortem = (String) result.get("postmortem");
        assertThat(postmortem).contains("Duration");
        assertThat(postmortem).contains("2h 30m");
    }

    // ========== listIncidents Tests ==========

    @Test
    void listIncidents_withNoFilters_returnsAll() {
        Incident i1 = buildIncident("inc-1", "open", "[]");
        Incident i2 = buildIncident("inc-2", "resolved", "[]");
        when(repository.findAll()).thenReturn(List.of(i1, i2));

        Map<String, Object> result = service.listIncidents(null, null, 50);

        List<Map<String, Object>> incidents = (List<Map<String, Object>>) result.get("incidents");
        assertThat(incidents).hasSize(2);
        assertThat((int) result.get("count")).isEqualTo(2);
    }

    @Test
    void listIncidents_withStatusFilter_filtersCorrectly() {
        Incident i1 = Incident.builder().id("inc-1").title("T1").severity("high").status("open")
                .description("D1").createdAt(Instant.now()).build();
        Incident i2 = Incident.builder().id("inc-2").title("T2").severity("high").status("resolved")
                .description("D2").createdAt(Instant.now()).build();
        when(repository.findAll()).thenReturn(List.of(i1, i2));

        Map<String, Object> result = service.listIncidents("open", null, 50);

        List<Map<String, Object>> incidents = (List<Map<String, Object>>) result.get("incidents");
        assertThat(incidents).hasSize(1);
        assertThat(incidents.get(0).get("status")).isEqualTo("open");
    }

    @Test
    void listIncidents_withSeverityFilter_filtersCorrectly() {
        Incident i1 = Incident.builder().id("inc-1").title("T1").severity("critical").status("open")
                .description("D1").createdAt(Instant.now()).build();
        Incident i2 = Incident.builder().id("inc-2").title("T2").severity("low").status("open")
                .description("D2").createdAt(Instant.now()).build();
        when(repository.findAll()).thenReturn(List.of(i1, i2));

        Map<String, Object> result = service.listIncidents(null, "critical", 50);

        List<Map<String, Object>> incidents = (List<Map<String, Object>>) result.get("incidents");
        assertThat(incidents).hasSize(1);
        assertThat(incidents.get(0).get("severity")).isEqualTo("critical");
    }

    @Test
    void listIncidents_withLimit_limitResults() {
        Incident i1 = Incident.builder().id("inc-1").title("T1").severity("high").status("open")
                .description("D1").createdAt(Instant.parse("2026-01-01T10:00:00Z")).build();
        Incident i2 = Incident.builder().id("inc-2").title("T2").severity("high").status("open")
                .description("D2").createdAt(Instant.parse("2026-01-02T10:00:00Z")).build();
        Incident i3 = Incident.builder().id("inc-3").title("T3").severity("high").status("open")
                .description("D3").createdAt(Instant.parse("2026-01-03T10:00:00Z")).build();
        when(repository.findAll()).thenReturn(List.of(i1, i2, i3));

        Map<String, Object> result = service.listIncidents(null, null, 2);

        List<Map<String, Object>> incidents = (List<Map<String, Object>>) result.get("incidents");
        assertThat(incidents).hasSize(2);
    }

    @Test
    void listIncidents_sortsByCreatedAtDescending() {
        Incident i1 = Incident.builder().id("inc-1").title("Oldest").severity("high").status("open")
                .description("D1").createdAt(Instant.parse("2026-01-01T10:00:00Z")).build();
        Incident i2 = Incident.builder().id("inc-2").title("Newest").severity("high").status("open")
                .description("D2").createdAt(Instant.parse("2026-01-03T10:00:00Z")).build();
        when(repository.findAll()).thenReturn(List.of(i1, i2));

        Map<String, Object> result = service.listIncidents(null, null, 50);

        List<Map<String, Object>> incidents = (List<Map<String, Object>>) result.get("incidents");
        assertThat(incidents.get(0).get("title")).isEqualTo("Newest");
        assertThat(incidents.get(1).get("title")).isEqualTo("Oldest");
    }

    @Test
    void listIncidents_includesFiltersInResult() {
        when(repository.findAll()).thenReturn(List.of());

        Map<String, Object> result = service.listIncidents("open", "high", 10);

        Map<String, Object> filters = (Map<String, Object>) result.get("filters");
        assertThat(filters.get("status")).isEqualTo("open");
        assertThat(filters.get("severity")).isEqualTo("high");
    }

    // ========== Helper Methods Tests ==========

    @Test
    void appendToJsonArray_withEmptyArray_addsEntry() {
        String result = service.appendToJsonArray("[]", "{\"key\":\"val\"}");
        assertThat(result).isEqualTo("[{\"key\":\"val\"}]");
    }

    @Test
    void appendToJsonArray_withExistingEntries_appendsEntry() {
        String result = service.appendToJsonArray("[{\"a\":\"1\"}]", "{\"b\":\"2\"}");
        assertThat(result).isEqualTo("[{\"a\":\"1\"},{\"b\":\"2\"}]");
    }

    @Test
    void appendToJsonArray_withNull_createsNewArray() {
        String result = service.appendToJsonArray(null, "{\"key\":\"val\"}");
        assertThat(result).isEqualTo("[{\"key\":\"val\"}]");
    }

    @Test
    void countJsonArrayEntries_withMultipleEntries_countsCorrectly() {
        assertThat(service.countJsonArrayEntries("[]")).isEqualTo(0);
        assertThat(service.countJsonArrayEntries("[{\"a\":1}]")).isEqualTo(1);
        assertThat(service.countJsonArrayEntries("[{\"a\":1},{\"b\":2}]")).isEqualTo(2);
        assertThat(service.countJsonArrayEntries("[{\"a\":1},{\"b\":2},{\"c\":3}]")).isEqualTo(3);
    }

    @Test
    void countJsonArrayEntries_withNull_returnsZero() {
        assertThat(service.countJsonArrayEntries(null)).isEqualTo(0);
        assertThat(service.countJsonArrayEntries("")).isEqualTo(0);
    }

    // ========== Test Helpers ==========

    private Incident buildIncident(String id, String status, String timelineJson) {
        return Incident.builder()
                .id(id)
                .title("Test Incident")
                .severity("high")
                .status(status)
                .description("Test description")
                .timelineJson(timelineJson)
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .build();
    }
}
