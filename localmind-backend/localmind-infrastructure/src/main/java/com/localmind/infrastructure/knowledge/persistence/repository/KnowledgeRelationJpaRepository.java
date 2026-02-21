package com.localmind.infrastructure.knowledge.persistence.repository;

import com.localmind.infrastructure.knowledge.persistence.entity.KnowledgeRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeRelationJpaRepository extends JpaRepository<KnowledgeRelationEntity, UUID> {

    List<KnowledgeRelationEntity> findByFromEntityIdOrToEntityId(String fromId, String toId);

    void deleteByFromEntityIdOrToEntityId(String fromId, String toId);
}
