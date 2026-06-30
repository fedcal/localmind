package com.localmind.domain.knowledge.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class KnowledgeGraphStats {
    private long totalEntities;
    private long totalRelations;
    private Map<EntityType, Long> entitiesByType;
}
