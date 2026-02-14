package com.localmind.api.mcp.controller;

import com.localmind.api.mcp.dto.*;
import com.localmind.domain.mcp.port.in.IncidentManagerUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mcp/incidents")
@Tag(name = "MCP Incidents", description = "Incident management")
public class McpIncidentController {

    private final IncidentManagerUseCase incidentManagerUseCase;

    public McpIncidentController(IncidentManagerUseCase incidentManagerUseCase) {
        this.incidentManagerUseCase = incidentManagerUseCase;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listIncidents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(incidentManagerUseCase.listIncidents(status, severity, limit));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> openIncident(@RequestBody OpenIncidentRequest request) {
        return ResponseEntity.ok(incidentManagerUseCase.openIncident(
                request.getTitle(),
                request.getSeverity(),
                request.getDescription(),
                request.getAffectedSystemsJson()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateIncident(
            @PathVariable String id,
            @RequestBody UpdateIncidentRequest request) {
        return ResponseEntity.ok(incidentManagerUseCase.updateIncident(
                id,
                request.getStatus(),
                request.getNote()));
    }

    @PostMapping("/{id}/timeline")
    public ResponseEntity<Map<String, Object>> addTimelineEntry(
            @PathVariable String id,
            @RequestBody TimelineEntryRequest request) {
        return ResponseEntity.ok(incidentManagerUseCase.addTimelineEntry(
                id,
                request.getDescription(),
                request.getSource()));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Map<String, Object>> resolveIncident(
            @PathVariable String id,
            @RequestBody ResolveIncidentRequest request) {
        return ResponseEntity.ok(incidentManagerUseCase.resolveIncident(
                id,
                request.getResolution(),
                request.getRootCause()));
    }

    @GetMapping("/{id}/postmortem")
    public ResponseEntity<Map<String, Object>> generatePostmortem(@PathVariable String id) {
        return ResponseEntity.ok(incidentManagerUseCase.generatePostmortem(id));
    }
}
