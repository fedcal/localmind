package com.localmind.infrastructure.persistence.repository;

import com.localmind.infrastructure.persistence.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaDocumentRepository extends JpaRepository<DocumentEntity, UUID> {
    List<DocumentEntity> findByStatus(String status);
    Optional<DocumentEntity> findByFileHash(String fileHash);
}
