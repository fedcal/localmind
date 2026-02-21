package com.localmind.infrastructure.persistence.repository;

import com.localmind.infrastructure.persistence.entity.DocumentChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaDocumentChunkRepository extends JpaRepository<DocumentChunkEntity, UUID> {
    List<DocumentChunkEntity> findByDocumentId(String documentId);
    void deleteByDocumentId(String documentId);
}
