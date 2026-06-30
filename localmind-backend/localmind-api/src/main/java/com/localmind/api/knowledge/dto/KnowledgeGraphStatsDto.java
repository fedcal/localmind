package com.localmind.api.knowledge.dto;

import java.util.Map;

public record KnowledgeGraphStatsDto(
        long totalEntities,
        long totalRelations,
        Map<String, Long> entitiesByType
) {}
