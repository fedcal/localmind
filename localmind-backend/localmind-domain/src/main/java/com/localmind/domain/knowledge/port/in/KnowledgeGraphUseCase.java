package com.localmind.domain.knowledge.port.in;

import com.localmind.domain.knowledge.model.KnowledgeEntity;
import com.localmind.domain.knowledge.model.KnowledgeGraphStats;
import com.localmind.domain.knowledge.model.KnowledgeSubgraph;

import java.util.List;

public interface KnowledgeGraphUseCase {

    void indexText(String text, String sourceDocumentId);

    List<KnowledgeEntity> searchEntities(String query);

    KnowledgeSubgraph getEntitySubgraph(String entityId, int depth);

    KnowledgeGraphStats getStats();

    void deleteEntity(String entityId);
}
