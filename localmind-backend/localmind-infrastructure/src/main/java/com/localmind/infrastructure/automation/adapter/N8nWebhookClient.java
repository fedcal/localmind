package com.localmind.infrastructure.automation.adapter;

import com.localmind.domain.automation.port.out.WebhookClientPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
public class N8nWebhookClient implements WebhookClientPort {

    private final WebClient webClient;

    public N8nWebhookClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public void callWebhook(String url, Map<String, Object> payload) {
        try {
            webClient.post()
                    .uri(url)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Webhook called successfully: {}", url);
        } catch (Exception e) {
            log.error("Failed to call webhook {}: {}", url, e.getMessage());
        }
    }
}
