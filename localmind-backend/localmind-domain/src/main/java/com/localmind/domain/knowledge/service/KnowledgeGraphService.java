package com.localmind.domain.knowledge.service;

import com.localmind.domain.knowledge.model.KnowledgeEntity;
import com.localmind.domain.knowledge.model.KnowledgeGraphStats;
import com.localmind.domain.knowledge.model.KnowledgeRelation;
import com.localmind.domain.knowledge.model.KnowledgeSubgraph;
import com.localmind.domain.knowledge.port.in.KnowledgeGraphUseCase;
import com.localmind.domain.knowledge.port.out.EntityExtractorPort;
import com.localmind.domain.knowledge.port.out.KnowledgeGraphPort;

import java.util.List;

public class KnowledgeGraphService implements KnowledgeGraphUseCase {

    private final KnowledgeGraphPort knowledgeGraphPort;
    private final EntityExtractorPort entityExtractorPort;

    public KnowledgeGraphService(KnowledgeGraphPort knowledgeGraphPort,
                                  EntityExtractorPort entityExtractorPort) {
        this.knowledgeGraphPort = knowledgeGraphPort;
        this.entityExtractorPort = entityExtractorPort;
    }

    @Override
    public void indexText(String text, String sourceDocumentId) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text must not be empty");
        }

        List<KnowledgeEntity> entities = entityExtractorPort.extractEntities(text);
        if (entities.isEmpty()) {
            return;
        }

        knowledgeGraphPort.storeEntities(entities);

        List<KnowledgeRelation> relations = entityExtractorPort.extractRelations(text, entities);
        if (!relations.isEmpty()) {
            knowledgeGraphPort.storeRelations(relations);
        }
    }

    @Override
    public List<KnowledgeEntity> searchEntities(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query must not be empty");
        }
        return knowledgeGraphPort.findEntities(query);
    }

    @Override
    public KnowledgeSubgraph getEntitySubgraph(String entityId, int depth) {
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("Entity ID must not be empty");
        }
        if (depth < 1) {
            throw new IllegalArgumentException("Depth must be at least 1");
        }
        return knowledgeGraphPort.getSubgraph(entityId, depth);
    }

    @Override
    public KnowledgeGraphStats getStats() {
        return knowledgeGraphPort.getStats();
    }

    @Override
    public void deleteEntity(String entityId) {
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("Entity ID must not be empty");
        }
        knowledgeGraphPort.deleteEntity(entityId);
    }
}
