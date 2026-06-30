package com.localmind.infrastructure.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("localmind.knowledge-graph")
public class KnowledgeGraphProperties {
    private boolean enabled = false;
    private int maxEntitiesPerExtraction = 50;
    private int maxDepth = 3;
}
