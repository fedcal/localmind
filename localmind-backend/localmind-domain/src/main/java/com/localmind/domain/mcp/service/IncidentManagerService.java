package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.Incident;
import com.localmind.domain.mcp.port.in.IncidentManagerUseCase;
import com.localmind.domain.mcp.port.out.IncidentRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain service implementing the incident management tool group.
 * Provides incident lifecycle management: open, update, add timeline, resolve, postmortem, list.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class IncidentManagerService implements IncidentManagerUseCase {

    private final IncidentRepository repository;

    private static final Set<String> VALID_SEVERITIES = Set.of("critical", "high", "medium", "low");
    private static final Set<String> VALID_STATUSES = Set.of(
            "open", "investigating", "mitigating", "resolved", "postmortem"
    );

    public IncidentManagerService(IncidentRepository repository) {
        this.repository = repository;
    }

    // ========== 1. openIncident ==========

    @Override
    public Map<String, Object> openIncident(String title, String severity, String description, String affectedSystemsJson) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (title == null || title.isBlank()) {
            result.put("error", "Title is required");
            return result;
        }
        if (severity == null || severity.isBlank()) {
            result.put("error", "Severity is required");
            return result;
        }
        if (description == null || description.isBlank()) {
            result.put("error", "Description is required");
            return result;
        }

        String normalizedSeverity = severity.toLowerCase().trim();
        if (!VALID_SEVERITIES.contains(normalizedSeverity)) {
            result.put("error", "Invalid severity: " + severity + ". Valid values: critical, high, medium, low");
            return result;
        }

        Instant now = Instant.now();
        String id = UUID.randomUUID().toString();

        // Create initial timeline entry
        String timelineJson = "[{\"timestamp\":\"" + now.toString() + "\",\"description\":\"Incident opened\",\"source\":\"system\"}]";

        Incident incident = Incident.builder()
                .id(id)
                .title(title)
                .severity(normalizedSeverity)
                .status("open")
                .description(description)
                .affectedSystemsJson(affectedSystemsJson)
                .timelineJson(timelineJson)
                .createdAt(now)
                .build();

        repository.save(incident);

        result.put("id", id);
        result.put("title", title);
        result.put("severity", normalizedSeverity);
        result.put("status", "open");
        result.put("description", description);
        result.put("affectedSystems", affectedSystemsJson);
        result.put("createdAt", now.toString());

        return result;
    }

    // ========== 2. updateIncident ==========

    @Override
    public Map<String, Object> updateIncident(String id, String status, String note) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (id == null || id.isBlank()) {
            result.put("error", "Incident id is required");
            return result;
        }

        Optional<Incident> optIncident = repository.findById(id);
        if (optIncident.isEmpty()) {
            result.put("error", "Incident not found: " + id);
            return result;
        }

        Incident incident = optIncident.get();
        String newStatus = incident.getStatus();
        String timelineJson = incident.getTimelineJson() != null ? incident.getTimelineJson() : "[]";
        boolean updated = false;

        // Update status if provided
        if (status != null && !status.isBlank()) {
            String normalizedStatus = status.toLowerCase().trim();
            if (!VALID_STATUSES.contains(normalizedStatus)) {
                result.put("error", "Invalid status: " + status + ". Valid values: open, investigating, mitigating, resolved, postmortem");
                return result;
            }
            newStatus = normalizedStatus;
            updated = true;

            // Add status change to timeline
            Instant now = Instant.now();
            String entry = "{\"timestamp\":\"" + now.toString() + "\",\"description\":\"Status changed to " + normalizedStatus + "\",\"source\":\"system\"}";
            timelineJson = appendToJsonArray(timelineJson, entry);
        }

        // Add note to timeline if provided
        if (note != null && !note.isBlank()) {
            Instant now = Instant.now();
            String entry = "{\"timestamp\":\"" + now.toString() + "\",\"description\":\"" + escapeJson(note) + "\",\"source\":\"user\"}";
            timelineJson = appendToJsonArray(timelineJson, entry);
            updated = true;
        }

        Incident updatedIncident = Incident.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .severity(incident.getSeverity())
                .status(newStatus)
                .description(incident.getDescription())
                .affectedSystemsJson(incident.getAffectedSystemsJson())
                .timelineJson(timelineJson)
                .resolution(incident.getResolution())
                .rootCause(incident.getRootCause())
                .createdAt(incident.getCreatedAt())
                .resolvedAt(incident.getResolvedAt())
                .build();

        repository.save(updatedIncident);

        result.put("id", id);
        result.put("status", newStatus);
        result.put("updated", updated);
        result.put("timelineEntries", countJsonArrayEntries(timelineJson));

        return result;
    }

    // ========== 3. addTimelineEntry ==========

    @Override
    public Map<String, Object> addTimelineEntry(String incidentId, String description, String source) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (incidentId == null || incidentId.isBlank()) {
            result.put("error", "Incident id is required");
            return result;
        }
        if (description == null || description.isBlank()) {
            result.put("error", "Description is required");
            return result;
        }

        Optional<Incident> optIncident = repository.findById(incidentId);
        if (optIncident.isEmpty()) {
            result.put("error", "Incident not found: " + incidentId);
            return result;
        }

        Incident incident = optIncident.get();
        Instant now = Instant.now();
        String effectiveSource = (source != null && !source.isBlank()) ? source : "user";

        String timelineJson = incident.getTimelineJson() != null ? incident.getTimelineJson() : "[]";
        String entry = "{\"timestamp\":\"" + now.toString() + "\",\"description\":\"" + escapeJson(description) + "\",\"source\":\"" + escapeJson(effectiveSource) + "\"}";
        timelineJson = appendToJsonArray(timelineJson, entry);

        Incident updatedIncident = Incident.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .description(incident.getDescription())
                .affectedSystemsJson(incident.getAffectedSystemsJson())
                .timelineJson(timelineJson)
                .resolution(incident.getResolution())
                .rootCause(incident.getRootCause())
                .createdAt(incident.getCreatedAt())
                .resolvedAt(incident.getResolvedAt())
                .build();

        repository.save(updatedIncident);

        Map<String, Object> entryMap = new LinkedHashMap<>();
        entryMap.put("timestamp", now.toString());
        entryMap.put("description", description);
        entryMap.put("source", effectiveSource);

        result.put("incidentId", incidentId);
        result.put("entry", entryMap);
        result.put("totalEntries", countJsonArrayEntries(timelineJson));

        return result;
    }

    // ========== 4. resolveIncident ==========

    @Override
    public Map<String, Object> resolveIncident(String id, String resolution, String rootCause) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (id == null || id.isBlank()) {
            result.put("error", "Incident id is required");
            return result;
        }

        Optional<Incident> optIncident = repository.findById(id);
        if (optIncident.isEmpty()) {
            result.put("error", "Incident not found: " + id);
            return result;
        }

        Incident incident = optIncident.get();
        Instant now = Instant.now();

        String timelineJson = incident.getTimelineJson() != null ? incident.getTimelineJson() : "[]";
        String entry = "{\"timestamp\":\"" + now.toString() + "\",\"description\":\"Incident resolved\",\"source\":\"system\"}";
        timelineJson = appendToJsonArray(timelineJson, entry);

        Incident resolvedIncident = Incident.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .severity(incident.getSeverity())
                .status("resolved")
                .description(incident.getDescription())
                .affectedSystemsJson(incident.getAffectedSystemsJson())
                .timelineJson(timelineJson)
                .resolution(resolution)
                .rootCause(rootCause)
                .createdAt(incident.getCreatedAt())
                .resolvedAt(now)
                .build();

        repository.save(resolvedIncident);

        // Calculate duration
        String duration = "unknown";
        if (incident.getCreatedAt() != null) {
            Duration d = Duration.between(incident.getCreatedAt(), now);
            long hours = d.toHours();
            long minutes = d.toMinutesPart();
            duration = hours + "h " + minutes + "m";
        }

        result.put("id", id);
        result.put("status", "resolved");
        result.put("resolution", resolution);
        result.put("rootCause", rootCause);
        result.put("resolvedAt", now.toString());
        result.put("duration", duration);

        return result;
    }

    // ========== 5. generatePostmortem ==========

    @Override
    public Map<String, Object> generatePostmortem(String id) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (id == null || id.isBlank()) {
            result.put("error", "Incident id is required");
            return result;
        }

        Optional<Incident> optIncident = repository.findById(id);
        if (optIncident.isEmpty()) {
            result.put("error", "Incident not found: " + id);
            return result;
        }

        Incident incident = optIncident.get();

        StringBuilder md = new StringBuilder();
        md.append("# Post-Mortem: ").append(incident.getTitle()).append("\n\n");
        md.append("## Incident Details\n\n");
        md.append("- **ID**: ").append(incident.getId()).append("\n");
        md.append("- **Severity**: ").append(incident.getSeverity()).append("\n");
        md.append("- **Status**: ").append(incident.getStatus()).append("\n");
        md.append("- **Created**: ").append(incident.getCreatedAt() != null ? incident.getCreatedAt().toString() : "N/A").append("\n");
        md.append("- **Resolved**: ").append(incident.getResolvedAt() != null ? incident.getResolvedAt().toString() : "N/A").append("\n");

        // Calculate duration
        if (incident.getCreatedAt() != null && incident.getResolvedAt() != null) {
            Duration d = Duration.between(incident.getCreatedAt(), incident.getResolvedAt());
            long hours = d.toHours();
            long minutes = d.toMinutesPart();
            md.append("- **Duration**: ").append(hours).append("h ").append(minutes).append("m\n");
        }

        md.append("\n## Description\n\n");
        md.append(incident.getDescription() != null ? incident.getDescription() : "N/A").append("\n");

        if (incident.getAffectedSystemsJson() != null && !incident.getAffectedSystemsJson().isBlank()) {
            md.append("\n## Affected Systems\n\n");
            md.append(incident.getAffectedSystemsJson()).append("\n");
        }

        md.append("\n## Timeline\n\n");
        if (incident.getTimelineJson() != null && !incident.getTimelineJson().isBlank()) {
            md.append(formatTimelineForMarkdown(incident.getTimelineJson()));
        } else {
            md.append("No timeline entries recorded.\n");
        }

        md.append("\n## Resolution\n\n");
        md.append(incident.getResolution() != null ? incident.getResolution() : "N/A").append("\n");

        md.append("\n## Root Cause\n\n");
        md.append(incident.getRootCause() != null ? incident.getRootCause() : "N/A").append("\n");

        md.append("\n## Action Items\n\n");
        md.append("- [ ] Review monitoring and alerting configuration\n");
        md.append("- [ ] Update runbooks and incident response procedures\n");
        md.append("- [ ] Implement preventive measures based on root cause\n");
        md.append("- [ ] Schedule follow-up review in 2 weeks\n");

        result.put("incidentId", id);
        result.put("postmortem", md.toString());
        result.put("generatedAt", Instant.now().toString());

        return result;
    }

    // ========== 6. listIncidents ==========

    @Override
    public Map<String, Object> listIncidents(String status, String severity, int limit) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Incident> all = new ArrayList<>(repository.findAll());

        // Filter by status
        if (status != null && !status.isBlank()) {
            String normalizedStatus = status.toLowerCase().trim();
            all = all.stream()
                    .filter(i -> normalizedStatus.equals(i.getStatus()))
                    .collect(Collectors.toList());
        }

        // Filter by severity
        if (severity != null && !severity.isBlank()) {
            String normalizedSeverity = severity.toLowerCase().trim();
            all = all.stream()
                    .filter(i -> normalizedSeverity.equals(i.getSeverity()))
                    .collect(Collectors.toList());
        }

        // Sort by createdAt descending
        all.sort(Comparator.comparing(
                (Incident i) -> i.getCreatedAt() != null ? i.getCreatedAt() : Instant.EPOCH
        ).reversed());

        // Limit
        int effectiveLimit = limit > 0 ? limit : 50;
        if (all.size() > effectiveLimit) {
            all = all.subList(0, effectiveLimit);
        }

        List<Map<String, Object>> incidents = new ArrayList<>();
        for (Incident i : all) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", i.getId());
            item.put("title", i.getTitle());
            item.put("severity", i.getSeverity());
            item.put("status", i.getStatus());
            item.put("createdAt", i.getCreatedAt() != null ? i.getCreatedAt().toString() : null);
            item.put("resolvedAt", i.getResolvedAt() != null ? i.getResolvedAt().toString() : null);
            incidents.add(item);
        }

        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("status", status);
        filters.put("severity", severity);

        result.put("incidents", incidents);
        result.put("count", incidents.size());
        result.put("filters", filters);

        return result;
    }

    // ========== JSON Helpers ==========

    /**
     * Appends a JSON object string to a JSON array string.
     */
    String appendToJsonArray(String jsonArray, String newEntry) {
        if (jsonArray == null || jsonArray.isBlank() || "[]".equals(jsonArray.trim())) {
            return "[" + newEntry + "]";
        }
        // Remove trailing ] and add the new entry
        String trimmed = jsonArray.trim();
        return trimmed.substring(0, trimmed.length() - 1) + "," + newEntry + "]";
    }

    /**
     * Counts entries in a JSON array by counting commas at top level + 1.
     */
    int countJsonArrayEntries(String jsonArray) {
        if (jsonArray == null || jsonArray.isBlank() || "[]".equals(jsonArray.trim())) {
            return 0;
        }
        String trimmed = jsonArray.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return 0;
        }
        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) {
            return 0;
        }
        // Count top-level objects by tracking brace depth
        int count = 0;
        int depth = 0;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    count++;
                }
                depth++;
            } else if (c == '}') {
                depth--;
            }
        }
        return count;
    }

    /**
     * Formats a timeline JSON array into Markdown bullet points.
     */
    private String formatTimelineForMarkdown(String timelineJson) {
        StringBuilder sb = new StringBuilder();
        // Simple parsing: extract each {...} block
        String trimmed = timelineJson.trim();
        if (!trimmed.startsWith("[")) {
            return trimmed + "\n";
        }

        String inner = trimmed.substring(1, trimmed.length() - 1);
        int depth = 0;
        int start = -1;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    String block = inner.substring(start, i + 1);
                    String ts = extractJsonValue(block, "timestamp");
                    String desc = extractJsonValue(block, "description");
                    String src = extractJsonValue(block, "source");
                    sb.append("- **").append(ts != null ? ts : "?").append("** [").append(src != null ? src : "?").append("]: ").append(desc != null ? desc : "?").append("\n");
                    start = -1;
                }
            }
        }

        return sb.toString();
    }

    /**
     * Extracts a simple string value from a JSON object string.
     */
    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx < 0) {
            return null;
        }
        int valStart = idx + search.length();
        int valEnd = json.indexOf("\"", valStart);
        if (valEnd < 0) {
            return null;
        }
        return json.substring(valStart, valEnd);
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
