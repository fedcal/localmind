package com.localmind.infrastructure.persistence.repository;

import com.localmind.infrastructure.persistence.entity.LlmProviderConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaProviderConfigRepository extends JpaRepository<LlmProviderConfigEntity, UUID> {
    List<LlmProviderConfigEntity> findByType(String type);
}
