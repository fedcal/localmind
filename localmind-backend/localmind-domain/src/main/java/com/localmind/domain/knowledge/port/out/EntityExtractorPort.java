package com.localmind.domain.knowledge.port.out;

import com.localmind.domain.knowledge.model.KnowledgeEntity;
import com.localmind.domain.knowledge.model.KnowledgeRelation;

import java.util.List;

public interface EntityExtractorPort {

    List<KnowledgeEntity> extractEntities(String text);

    List<KnowledgeRelation> extractRelations(String text, List<KnowledgeEntity> entities);
}
