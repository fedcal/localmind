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
public class KnowledgeEntity {
    private String id;
    private String name;
    private EntityType type;
    private Map<String, String> properties;
}
