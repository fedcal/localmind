package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.CodebaseKnowledgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaCodebaseKnowledgeRepository extends JpaRepository<CodebaseKnowledgeEntity, UUID> {

    List<CodebaseKnowledgeEntity> findByTargetPathOrderByCreatedAtDesc(String targetPath);
}
