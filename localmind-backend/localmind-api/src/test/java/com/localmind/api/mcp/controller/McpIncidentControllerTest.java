package com.localmind.api.mcp.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.mcp.dto.OpenIncidentRequest;
import com.localmind.api.mcp.dto.ResolveIncidentRequest;
import com.localmind.api.mcp.dto.TimelineEntryRequest;
import com.localmind.api.mcp.dto.UpdateIncidentRequest;
import com.localmind.domain.mcp.port.in.IncidentManagerUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

class McpIncidentControllerTest {

    private MockMvc mockMvc;
    private IncidentManagerUseCase incidentManagerUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        incidentManagerUseCase = mock(IncidentManagerUseCase.class);
        McpIncidentController controller = new McpIncidentController(incidentManagerUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listIncidents_withNoFilters_shouldReturnAll() throws Exception {
        Map<String, Object> result = Map.of(
                "incidents", List.of(
                        Map.of("id", "inc-1", "title", "DB down", "severity", "critical", "status", "open"),
                        Map.of("id", "inc-2", "title", "Slow API", "severity", "medium", "status", "resolved")
                ),
                "count", 2,
                "filters", Map.of()
        );

        when(incidentManagerUseCase.listIncidents(null, null, 50)).thenReturn(result);

        mockMvc.perform(get("/api/v1/mcp/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.incidents[0].id").value("inc-1"))
                .andExpect(jsonPath("$.incidents[0].title").value("DB down"))
                .andExpect(jsonPath("$.incidents[1].id").value("inc-2"));

        verify(incidentManagerUseCase).listIncidents(null, null, 50);
    }

    @Test
    void listIncidents_withFilters_shouldPassParams() throws Exception {
        Map<String, Object> result = Map.of(
                "incidents", List.of(
                        Map.of("id", "inc-1", "title", "DB down", "severity", "critical", "status", "open")
                ),
                "count", 1,
                "filters", Map.of("status", "open", "severity", "critical")
        );

        when(incidentManagerUseCase.listIncidents("open", "critical", 10)).thenReturn(result);

        mockMvc.perform(get("/api/v1/mcp/incidents")
                        .param("status", "open")
                        .param("severity", "critical")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.incidents[0].severity").value("critical"))
                .andExpect(jsonPath("$.incidents[0].status").value("open"));

        verify(incidentManagerUseCase).listIncidents("open", "critical", 10);
    }

    @Test
    void openIncident_shouldCreateAndReturnIncident() throws Exception {
        OpenIncidentRequest request = OpenIncidentRequest.builder()
                .title("Database connection pool exhausted")
                .severity("critical")
                .description("All DB connections are in use, new requests are timing out")
                .affectedSystemsJson("[\"api-gateway\",\"user-service\"]")
                .build();

        Map<String, Object> result = Map.of(
                "id", "inc-100",
                "title", "Database connection pool exhausted",
                "severity", "critical",
                "status", "open",
                "description", "All DB connections are in use, new requests are timing out",
                "createdAt", "2026-02-13T10:00:00Z"
        );

        when(incidentManagerUseCase.openIncident(
                "Database connection pool exhausted",
                "critical",
                "All DB connections are in use, new requests are timing out",
                "[\"api-gateway\",\"user-service\"]"
        )).thenReturn(result);

        mockMvc.perform(post("/api/v1/mcp/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("inc-100"))
                .andExpect(jsonPath("$.title").value("Database connection pool exhausted"))
                .andExpect(jsonPath("$.severity").value("critical"))
                .andExpect(jsonPath("$.status").value("open"));

        verify(incidentManagerUseCase).openIncident(
                "Database connection pool exhausted",
                "critical",
                "All DB connections are in use, new requests are timing out",
                "[\"api-gateway\",\"user-service\"]"
        );
    }

    @Test
    void updateIncident_shouldUpdateStatusAndNote() throws Exception {
        UpdateIncidentRequest request = UpdateIncidentRequest.builder()
                .status("investigating")
                .note("DBA team is analyzing slow queries")
                .build();

        Map<String, Object> result = Map.of(
                "id", "inc-100",
                "status", "investigating",
                "updated", true,
                "timelineEntries", 3
        );

        when(incidentManagerUseCase.updateIncident("inc-100", "investigating", "DBA team is analyzing slow queries"))
                .thenReturn(result);

        mockMvc.perform(put("/api/v1/mcp/incidents/inc-100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("inc-100"))
                .andExpect(jsonPath("$.status").value("investigating"))
                .andExpect(jsonPath("$.updated").value(true))
                .andExpect(jsonPath("$.timelineEntries").value(3));

        verify(incidentManagerUseCase).updateIncident("inc-100", "investigating", "DBA team is analyzing slow queries");
    }

    @Test
    void addTimelineEntry_shouldAddEntryAndReturnResult() throws Exception {
        TimelineEntryRequest request = TimelineEntryRequest.builder()
                .description("Restarted connection pool with increased max size")
                .source("on-call-engineer")
                .build();

        Map<String, Object> result = Map.of(
                "incidentId", "inc-100",
                "entryId", "entry-5",
                "description", "Restarted connection pool with increased max size",
                "source", "on-call-engineer",
                "totalEntries", 4
        );

        when(incidentManagerUseCase.addTimelineEntry(
                "inc-100",
                "Restarted connection pool with increased max size",
                "on-call-engineer"
        )).thenReturn(result);

        mockMvc.perform(post("/api/v1/mcp/incidents/inc-100/timeline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentId").value("inc-100"))
                .andExpect(jsonPath("$.entryId").value("entry-5"))
                .andExpect(jsonPath("$.source").value("on-call-engineer"))
                .andExpect(jsonPath("$.totalEntries").value(4));

        verify(incidentManagerUseCase).addTimelineEntry(
                "inc-100",
                "Restarted connection pool with increased max size",
                "on-call-engineer"
        );
    }

    @Test
    void resolveIncident_shouldResolveAndReturnResult() throws Exception {
        ResolveIncidentRequest request = ResolveIncidentRequest.builder()
                .resolution("Increased pool size from 10 to 50 and added connection timeout")
                .rootCause("Connection leak in batch processing module")
                .build();

        Map<String, Object> result = Map.of(
                "id", "inc-100",
                "status", "resolved",
                "resolution", "Increased pool size from 10 to 50 and added connection timeout",
                "rootCause", "Connection leak in batch processing module",
                "resolvedAt", "2026-02-13T12:30:00Z",
                "duration", "2h 30m"
        );

        when(incidentManagerUseCase.resolveIncident(
                "inc-100",
                "Increased pool size from 10 to 50 and added connection timeout",
                "Connection leak in batch processing module"
        )).thenReturn(result);

        mockMvc.perform(post("/api/v1/mcp/incidents/inc-100/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("inc-100"))
                .andExpect(jsonPath("$.status").value("resolved"))
                .andExpect(jsonPath("$.resolution").value("Increased pool size from 10 to 50 and added connection timeout"))
                .andExpect(jsonPath("$.rootCause").value("Connection leak in batch processing module"))
                .andExpect(jsonPath("$.duration").value("2h 30m"));

        verify(incidentManagerUseCase).resolveIncident(
                "inc-100",
                "Increased pool size from 10 to 50 and added connection timeout",
                "Connection leak in batch processing module"
        );
    }

    @Test
    void generatePostmortem_shouldReturnPostmortemReport() throws Exception {
        Map<String, Object> result = Map.of(
                "incidentId", "inc-100",
                "postmortem", "# Post-Mortem: Database connection pool exhausted\n\n## Summary\n...",
                "generatedAt", "2026-02-13T14:00:00Z"
        );

        when(incidentManagerUseCase.generatePostmortem("inc-100")).thenReturn(result);

        mockMvc.perform(get("/api/v1/mcp/incidents/inc-100/postmortem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentId").value("inc-100"))
                .andExpect(jsonPath("$.postmortem").exists())
                .andExpect(jsonPath("$.generatedAt").value("2026-02-13T14:00:00Z"));

        verify(incidentManagerUseCase).generatePostmortem("inc-100");
    }

    @Test
    void openIncident_whenUseCaseThrows_shouldReturn500() throws Exception {
        OpenIncidentRequest request = OpenIncidentRequest.builder()
                .title("Service crash")
                .severity("high")
                .description("Unexpected shutdown")
                .build();

        when(incidentManagerUseCase.openIncident(anyString(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Database unavailable"));

        mockMvc.perform(post("/api/v1/mcp/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));

        verify(incidentManagerUseCase).openIncident("Service crash", "high", "Unexpected shutdown", null);
    }
}
