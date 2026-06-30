package com.localmind.domain.automation.port.in;

import com.localmind.domain.automation.model.AutomationEvent;
import com.localmind.domain.automation.model.Webhook;

import java.util.List;

public interface AutomationUseCase {
    void trigger(AutomationEvent event);
    List<Webhook> listWebhooks();
    Webhook findById(String id);
    Webhook save(Webhook webhook);
    void delete(String id);
    void testWebhook(String id);
}
