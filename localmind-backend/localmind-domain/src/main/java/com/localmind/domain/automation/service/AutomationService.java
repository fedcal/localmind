package com.localmind.domain.automation.service;

import com.localmind.domain.automation.model.AutomationEvent;
import com.localmind.domain.automation.model.Webhook;
import com.localmind.domain.automation.port.in.AutomationUseCase;
import com.localmind.domain.automation.port.out.WebhookClientPort;
import com.localmind.domain.automation.port.out.WebhookRepository;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AutomationService implements AutomationUseCase {

    private static final Logger log = Logger.getLogger(AutomationService.class.getName());

    private final WebhookRepository webhookRepository;
    private final WebhookClientPort webhookClientPort;

    public AutomationService(WebhookRepository webhookRepository, WebhookClientPort webhookClientPort) {
        this.webhookRepository = webhookRepository;
        this.webhookClientPort = webhookClientPort;
    }

    @Override
    public void trigger(AutomationEvent event) {
        log.info(() -> "Triggering automation event: " + event.getType());
        List<Webhook> webhooks = webhookRepository.findByEventType(event.getType());
        for (Webhook webhook : webhooks) {
            if (!webhook.isActive()) {
                log.fine(() -> "Skipping inactive webhook: " + webhook.getName() + " (" + webhook.getId() + ")");
                continue;
            }
            try {
                log.info(() -> "Calling webhook '" + webhook.getName() + "' at URL: " + webhook.getUrl());
                webhookClientPort.callWebhook(webhook.getUrl(), event.getPayload());
            } catch (Exception e) {
                log.log(Level.SEVERE, "Failed to call webhook '" + webhook.getName() + "' (id=" + webhook.getId() + "): " + e.getMessage(), e);
            }
        }
    }

    @Override
    public List<Webhook> listWebhooks() {
        return webhookRepository.findAll();
    }

    @Override
    public Webhook findById(String id) {
        return webhookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Webhook not found with id: " + id));
    }

    @Override
    public Webhook save(Webhook webhook) {
        log.info(() -> "Saving webhook: name=" + webhook.getName() + ", url=" + webhook.getUrl() + ", eventType=" + webhook.getEventType());
        return webhookRepository.save(webhook);
    }

    @Override
    public void delete(String id) {
        log.info(() -> "Deleting webhook with id: " + id);
        webhookRepository.deleteById(id);
    }

    @Override
    public void testWebhook(String id) {
        Webhook webhook = findById(id);
        log.info(() -> "Testing webhook '" + webhook.getName() + "' at URL: " + webhook.getUrl());
        Map<String, Object> testPayload = Map.of(
                "event", "test",
                "webhookId", id
        );
        webhookClientPort.callWebhook(webhook.getUrl(), testPayload);
    }
}
