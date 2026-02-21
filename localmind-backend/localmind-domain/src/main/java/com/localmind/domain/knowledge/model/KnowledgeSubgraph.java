package com.localmind.domain.knowledge.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KnowledgeSubgraph {
    private List<KnowledgeEntity> entities;
    private List<KnowledgeRelation> relations;
}
