package com.localmind.infrastructure.persistence.repository.automation;

import com.localmind.infrastructure.persistence.entity.automation.WebhookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaWebhookRepository extends JpaRepository<WebhookEntity, UUID> {
    List<WebhookEntity> findByEventType(String eventType);
    List<WebhookEntity> findByActive(boolean active);
}
