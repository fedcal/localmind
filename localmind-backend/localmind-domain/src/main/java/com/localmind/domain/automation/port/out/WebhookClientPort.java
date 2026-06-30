package com.localmind.domain.automation.port.out;

import java.util.Map;

public interface WebhookClientPort {
    void callWebhook(String url, Map<String, Object> payload);
}
