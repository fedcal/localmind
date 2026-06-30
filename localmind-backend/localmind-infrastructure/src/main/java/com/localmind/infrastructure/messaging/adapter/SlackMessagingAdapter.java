package com.localmind.infrastructure.messaging.adapter;

import com.localmind.domain.messaging.model.MessagingPlatform;
import com.localmind.domain.messaging.model.OutboundMessage;
import com.localmind.domain.messaging.port.out.MessagingClientPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.messaging.slack.enabled", havingValue = "true")
public class SlackMessagingAdapter implements MessagingClientPort {

    private static final Logger log = LoggerFactory.getLogger(SlackMessagingAdapter.class);
    private static final String SLACK_API_URL = "https://slack.com/api";
    private final WebClient webClient;

    public SlackMessagingAdapter() {
        this.webClient = WebClient.builder()
                .baseUrl(SLACK_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public void sendMessage(OutboundMessage message) {
        Map<String, Object> body = Map.of(
                "channel", message.getChannelId(),
                "text", message.getText(),
                "thread_ts", message.getReplyToMessageId() != null ? message.getReplyToMessageId() : ""
        );

        webClient.post()
                .uri("/chat.postMessage")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + message.getBotToken())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> log.error("Errore invio messaggio Slack: {}", e.getMessage()))
                .subscribe();
    }

    @Override
    public boolean isAvailable(String botToken) {
        try {
            String response = webClient.post()
                    .uri("/auth.test")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + botToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return response != null && response.contains("\"ok\":true");
        } catch (Exception e) {
            log.warn("Slack bot non raggiungibile: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public MessagingPlatform getPlatform() {
        return MessagingPlatform.SLACK;
    }
}
