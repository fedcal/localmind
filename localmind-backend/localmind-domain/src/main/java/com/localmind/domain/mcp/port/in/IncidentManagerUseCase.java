package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the incident management tool group.
 * Provides incident lifecycle management: open, update, add timeline, resolve, postmortem, list.
 */
public interface IncidentManagerUseCase {

    /**
     * Opens a new incident with the given details.
     *
     * @param title       the incident title
     * @param severity    the severity level (critical, high, medium, low)
     * @param description a description of the incident
     * @param affectedSystemsJson optional JSON string listing affected systems
     * @return result with id, title, severity, status, description, affectedSystems, createdAt
     */
    Map<String, Object> openIncident(String title, String severity, String description, String affectedSystemsJson);

    /**
     * Updates an existing incident's status and/or adds a note.
     *
     * @param id     the incident id
     * @param status the new status (nullable)
     * @param note   a note to add to the timeline (nullable)
     * @return result with id, status, updated flag, timelineEntries count
     */
    Map<String, Object> updateIncident(String id, String status, String note);

    /**
     * Adds a timeline entry to an existing incident.
     *
     * @param incidentId  the incident id
     * @param description the timeline entry description
     * @param source      the source of the timeline entry
     * @return result with incidentId, entry details, totalEntries
     */
    Map<String, Object> addTimelineEntry(String incidentId, String description, String source);

    /**
     * Resolves an incident, recording the resolution and root cause.
     *
     * @param id         the incident id
     * @param resolution the resolution description
     * @param rootCause  the root cause analysis
     * @return result with id, status, resolution, rootCause, resolvedAt, duration
     */
    Map<String, Object> resolveIncident(String id, String resolution, String rootCause);

    /**
     * Generates a post-mortem report in Markdown format for the given incident.
     *
     * @param id the incident id
     * @return result with incidentId, postmortem markdown, generatedAt
     */
    Map<String, Object> generatePostmortem(String id);

    /**
     * Lists incidents with optional filters for status and severity.
     *
     * @param status   optional status filter
     * @param severity optional severity filter
     * @param limit    maximum number of results
     * @return result with incidents list, count, filters
     */
    Map<String, Object> listIncidents(String status, String severity, int limit);
}
