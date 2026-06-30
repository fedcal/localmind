package com.localmind.api.knowledge.dto;

import java.util.Map;

public record KnowledgeRelationDto(
        String id,
        String fromEntityId,
        String toEntityId,
        String type,
        Map<String, String> properties
) {}
