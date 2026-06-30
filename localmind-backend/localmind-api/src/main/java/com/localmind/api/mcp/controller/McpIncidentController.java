package com.localmind.api.mcp.controller;

import com.localmind.api.mcp.dto.*;
import com.localmind.domain.mcp.port.in.IncidentManagerUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(summary = "Lista incidenti / List incidents")
    @ApiResponse(responseCode = "200", description = "Lista incidenti / Incident list")
    public ResponseEntity<Map<String, Object>> listIncidents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(incidentManagerUseCase.listIncidents(status, severity, limit));
    }

    @PostMapping
    @Operation(summary = "Apri incidente / Open incident")
    @ApiResponse(responseCode = "200", description = "Incidente aperto / Incident opened")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<Map<String, Object>> openIncident(@RequestBody OpenIncidentRequest request) {
        return ResponseEntity.ok(incidentManagerUseCase.openIncident(
                request.getTitle(),
                request.getSeverity(),
                request.getDescription(),
                request.getAffectedSystemsJson()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna incidente / Update incident")
    @ApiResponse(responseCode = "200", description = "Incidente aggiornato / Incident updated")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Map<String, Object>> updateIncident(
            @PathVariable String id,
            @RequestBody UpdateIncidentRequest request) {
        return ResponseEntity.ok(incidentManagerUseCase.updateIncident(
                id,
                request.getStatus(),
                request.getNote()));
    }

    @PostMapping("/{id}/timeline")
    @Operation(summary = "Aggiungi voce alla timeline incidente / Add incident timeline entry")
    @ApiResponse(responseCode = "200", description = "Voce aggiunta / Entry added")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Map<String, Object>> addTimelineEntry(
            @PathVariable String id,
            @RequestBody TimelineEntryRequest request) {
        return ResponseEntity.ok(incidentManagerUseCase.addTimelineEntry(
                id,
                request.getDescription(),
                request.getSource()));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Risolvi incidente / Resolve incident")
    @ApiResponse(responseCode = "200", description = "Incidente risolto / Incident resolved")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Map<String, Object>> resolveIncident(
            @PathVariable String id,
            @RequestBody ResolveIncidentRequest request) {
        return ResponseEntity.ok(incidentManagerUseCase.resolveIncident(
                id,
                request.getResolution(),
                request.getRootCause()));
    }

    @GetMapping("/{id}/postmortem")
    @Operation(summary = "Genera postmortem incidente / Generate incident postmortem")
    @ApiResponse(responseCode = "200", description = "Postmortem generato / Postmortem generated")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Map<String, Object>> generatePostmortem(@PathVariable String id) {
        return ResponseEntity.ok(incidentManagerUseCase.generatePostmortem(id));
    }
}
