package com.localmind.infrastructure.persistence.repository;

import com.localmind.infrastructure.persistence.entity.DocumentStatsCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository JPA per il modello di lettura delle statistiche documenti (CQRS).
 * JPA repository for the document statistics read model (CQRS).
 */
@Repository
public interface DocumentStatsCacheJpaRepository extends JpaRepository<DocumentStatsCacheEntity, UUID> {

    @Query("SELECT d.status, COUNT(d) FROM DocumentStatsCacheEntity d GROUP BY d.status")
    List<Object[]> getStatusDistribution();

    @Query("SELECT d.mimeType, COUNT(d) FROM DocumentStatsCacheEntity d GROUP BY d.mimeType")
    List<Object[]> getTypeDistribution();
}
