package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.DecisionLink;
import com.localmind.domain.mcp.model.DecisionRecord;
import com.localmind.domain.mcp.port.in.DecisionLogUseCase;
import com.localmind.domain.mcp.port.out.DecisionLogRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Domain service implementing the decision-log tool group.
 * Provides recording, querying, superseding and linking of architectural decision records.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class DecisionLogService implements DecisionLogUseCase {

    private final DecisionLogRepository repository;

    private static final Set<String> VALID_STATUSES = Set.of("proposed", "accepted", "deprecated", "superseded");
    private static final Set<String> VALID_LINK_TYPES = Set.of("ticket", "commit", "impact", "related");

    public DecisionLogService(DecisionLogRepository repository) {
        this.repository = repository;
    }

    // ========== 1. recordDecision ==========

    @Override
    public Map<String, Object> recordDecision(String title, String context, String decision,
                                                String alternativesJson, String consequences, String status) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (title == null || title.isBlank()) {
            result.put("error", "Title cannot be blank");
            return result;
        }

        if (context == null || context.isBlank()) {
            result.put("error", "Context cannot be blank");
            return result;
        }

        if (decision == null || decision.isBlank()) {
            result.put("error", "Decision cannot be blank");
            return result;
        }

        String effectiveStatus = (status != null && VALID_STATUSES.contains(status.toLowerCase()))
                ? status.toLowerCase()
                : "proposed";

        DecisionRecord record = DecisionRecord.builder()
                .id(UUID.randomUUID().toString())
                .title(title.trim())
                .context(context.trim())
                .decision(decision.trim())
                .alternativesJson(alternativesJson)
                .consequences(consequences)
                .status(effectiveStatus)
                .relatedTicketsJson(null)
                .createdAt(Instant.now())
                .build();

        DecisionRecord saved = repository.save(record);

        result.put("id", saved.getId());
        result.put("title", saved.getTitle());
        result.put("context", saved.getContext());
        result.put("decision", saved.getDecision());
        result.put("alternatives", saved.getAlternativesJson());
        result.put("consequences", saved.getConsequences());
        result.put("status", saved.getStatus());
        result.put("createdAt", saved.getCreatedAt().toString());

        return result;
    }

    // ========== 2. listDecisions ==========

    @Override
    public Map<String, Object> listDecisions(String status, String search, int limit) {
        Map<String, Object> result = new LinkedHashMap<>();

        int effectiveLimit = limit > 0 ? limit : 50;

        List<DecisionRecord> allRecords = repository.findAll();
        List<DecisionRecord> filtered = new ArrayList<>();

        for (DecisionRecord record : allRecords) {
            // Filter by status if provided
            if (status != null && !status.isBlank()) {
                if (!status.equalsIgnoreCase(record.getStatus())) {
                    continue;
                }
            }

            // Filter by search text if provided
            if (search != null && !search.isBlank()) {
                String searchLower = search.toLowerCase();
                boolean matchesTitle = record.getTitle() != null
                        && record.getTitle().toLowerCase().contains(searchLower);
                boolean matchesContext = record.getContext() != null
                        && record.getContext().toLowerCase().contains(searchLower);
                boolean matchesDecision = record.getDecision() != null
                        && record.getDecision().toLowerCase().contains(searchLower);

                if (!matchesTitle && !matchesContext && !matchesDecision) {
                    continue;
                }
            }

            filtered.add(record);
        }

        // Apply limit
        List<DecisionRecord> limited = filtered.size() > effectiveLimit
                ? filtered.subList(0, effectiveLimit)
                : filtered;

        List<Map<String, Object>> decisionList = new ArrayList<>();
        for (DecisionRecord record : limited) {
            decisionList.add(recordToMap(record));
        }

        result.put("decisions", decisionList);
        result.put("count", decisionList.size());

        return result;
    }

    // ========== 3. getDecision ==========

    @Override
    public Map<String, Object> getDecision(String id) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (id == null || id.isBlank()) {
            result.put("error", "Decision ID cannot be blank");
            return result;
        }

        Optional<DecisionRecord> optRecord = repository.findById(id);
        if (optRecord.isEmpty()) {
            result.put("error", "Decision not found: " + id);
            return result;
        }

        DecisionRecord record = optRecord.get();
        result.put("decision", recordToMap(record));

        List<DecisionLink> links = repository.findLinksByDecisionId(id);
        List<Map<String, Object>> linkList = new ArrayList<>();
        for (DecisionLink link : links) {
            linkList.add(linkToMap(link));
        }
        result.put("links", linkList);

        return result;
    }

    // ========== 4. supersedeDecision ==========

    @Override
    public Map<String, Object> supersedeDecision(String id, String supersededById) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (id == null || id.isBlank()) {
            result.put("error", "Decision ID cannot be blank");
            return result;
        }

        if (supersededById == null || supersededById.isBlank()) {
            result.put("error", "Superseded-by ID cannot be blank");
            return result;
        }

        Optional<DecisionRecord> optRecord = repository.findById(id);
        if (optRecord.isEmpty()) {
            result.put("error", "Decision not found: " + id);
            return result;
        }

        DecisionRecord existing = optRecord.get();

        // Create updated record with superseded status
        DecisionRecord updated = DecisionRecord.builder()
                .id(existing.getId())
                .title(existing.getTitle())
                .context(existing.getContext())
                .decision(existing.getDecision())
                .alternativesJson(existing.getAlternativesJson())
                .consequences(existing.getConsequences())
                .status("superseded")
                .relatedTicketsJson(existing.getRelatedTicketsJson())
                .createdAt(existing.getCreatedAt())
                .build();

        repository.save(updated);

        result.put("id", id);
        result.put("status", "superseded");
        result.put("supersededBy", supersededById);

        return result;
    }

    // ========== 5. linkDecision ==========

    @Override
    public Map<String, Object> linkDecision(String decisionId, String linkType, String targetId, String description) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (decisionId == null || decisionId.isBlank()) {
            result.put("error", "Decision ID cannot be blank");
            return result;
        }

        if (linkType == null || !VALID_LINK_TYPES.contains(linkType.toLowerCase())) {
            result.put("error", "Link type must be one of: ticket, commit, impact, related");
            return result;
        }

        if (targetId == null || targetId.isBlank()) {
            result.put("error", "Target ID cannot be blank");
            return result;
        }

        // Verify decision exists
        Optional<DecisionRecord> optRecord = repository.findById(decisionId);
        if (optRecord.isEmpty()) {
            result.put("error", "Decision not found: " + decisionId);
            return result;
        }

        DecisionLink link = DecisionLink.builder()
                .id(UUID.randomUUID().toString())
                .decisionId(decisionId)
                .linkType(linkType.toLowerCase())
                .targetId(targetId.trim())
                .description(description)
                .createdAt(Instant.now())
                .build();

        DecisionLink saved = repository.saveLink(link);

        result.put("id", saved.getId());
        result.put("decisionId", saved.getDecisionId());
        result.put("linkType", saved.getLinkType());
        result.put("targetId", saved.getTargetId());
        result.put("description", saved.getDescription());

        return result;
    }

    // ========== Helper Methods ==========

    private Map<String, Object> recordToMap(DecisionRecord record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", record.getId());
        map.put("title", record.getTitle());
        map.put("context", record.getContext());
        map.put("decision", record.getDecision());
        map.put("alternatives", record.getAlternativesJson());
        map.put("consequences", record.getConsequences());
        map.put("status", record.getStatus());
        map.put("createdAt", record.getCreatedAt().toString());
        return map;
    }

    private Map<String, Object> linkToMap(DecisionLink link) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", link.getId());
        map.put("decisionId", link.getDecisionId());
        map.put("linkType", link.getLinkType());
        map.put("targetId", link.getTargetId());
        map.put("description", link.getDescription());
        map.put("createdAt", link.getCreatedAt().toString());
        return map;
    }
}
