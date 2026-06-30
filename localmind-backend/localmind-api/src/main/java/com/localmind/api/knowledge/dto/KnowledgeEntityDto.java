package com.localmind.api.knowledge.dto;

import java.util.Map;

public record KnowledgeEntityDto(
        String id,
        String name,
        String type,
        Map<String, String> properties
) {}
