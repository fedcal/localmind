package com.localmind.domain.automation.port.out;

import com.localmind.domain.automation.model.AutomationEventType;
import com.localmind.domain.automation.model.Webhook;

import java.util.List;
import java.util.Optional;

public interface WebhookRepository {
    Webhook save(Webhook webhook);
    Optional<Webhook> findById(String id);
    List<Webhook> findAll();
    List<Webhook> findByEventType(AutomationEventType eventType);
    void deleteById(String id);
}
