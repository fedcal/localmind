package com.localmind.domain.knowledge.port.out;

import com.localmind.domain.knowledge.model.KnowledgeEntity;
import com.localmind.domain.knowledge.model.KnowledgeGraphStats;
import com.localmind.domain.knowledge.model.KnowledgeRelation;
import com.localmind.domain.knowledge.model.KnowledgeSubgraph;

import java.util.List;

public interface KnowledgeGraphPort {

    void storeEntities(List<KnowledgeEntity> entities);

    void storeRelations(List<KnowledgeRelation> relations);

    List<KnowledgeEntity> findEntities(String query);

    List<KnowledgeRelation> findRelations(String entityId);

    KnowledgeSubgraph getSubgraph(String entityId, int depth);

    KnowledgeGraphStats getStats();

    void deleteEntity(String entityId);
}
