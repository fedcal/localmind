package com.localmind.domain.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeRelation {
    private String id;
    private String fromEntityId;
    private String toEntityId;
    private RelationType type;
    private Map<String, String> properties;
}
