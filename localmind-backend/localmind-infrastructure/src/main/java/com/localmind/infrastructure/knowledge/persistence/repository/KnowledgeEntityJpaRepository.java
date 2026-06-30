package com.localmind.infrastructure.knowledge.persistence.repository;

import com.localmind.infrastructure.knowledge.persistence.entity.KnowledgeEntityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeEntityJpaRepository extends JpaRepository<KnowledgeEntityEntity, UUID> {

    List<KnowledgeEntityEntity> findByNameContainingIgnoreCase(String query);

    @Query("SELECT e.entityType, COUNT(e) FROM KnowledgeEntityEntity e GROUP BY e.entityType")
    List<Object[]> countByEntityType();
}
