package com.localmind.infrastructure.persistence.repository;

import com.localmind.infrastructure.persistence.entity.LlmUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaLlmUsageRepository extends JpaRepository<LlmUsageEntity, UUID> {
    List<LlmUsageEntity> findByTimestampBetween(Instant from, Instant to);
}
