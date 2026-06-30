package com.localmind.api.knowledge.dto;

import java.util.List;

public record KnowledgeSubgraphDto(
        List<KnowledgeEntityDto> entities,
        List<KnowledgeRelationDto> relations
) {}
