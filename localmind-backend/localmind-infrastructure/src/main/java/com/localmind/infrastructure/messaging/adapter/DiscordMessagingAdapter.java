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

import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.messaging.discord.enabled", havingValue = "true")
public class DiscordMessagingAdapter implements MessagingClientPort {

    private static final Logger log = LoggerFactory.getLogger(DiscordMessagingAdapter.class);
    private static final String DISCORD_API_URL = "https://discord.com/api/v10";
    private final WebClient webClient;

    public DiscordMessagingAdapter() {
        this.webClient = WebClient.builder()
                .baseUrl(DISCORD_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public void sendMessage(OutboundMessage message) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", message.getText());
        if (message.getReplyToMessageId() != null) {
            body.put("message_reference", Map.of("message_id", message.getReplyToMessageId()));
        }

        webClient.post()
                .uri("/channels/{channelId}/messages", message.getChannelId())
                .header(HttpHeaders.AUTHORIZATION, "Bot " + message.getBotToken())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> log.error("Errore invio messaggio Discord: {}", e.getMessage()))
                .subscribe();
    }

    @Override
    public boolean isAvailable(String botToken) {
        try {
            String response = webClient.get()
                    .uri("/users/@me")
                    .header(HttpHeaders.AUTHORIZATION, "Bot " + botToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return response != null && response.contains("\"id\"");
        } catch (Exception e) {
            log.warn("Discord bot non raggiungibile: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public MessagingPlatform getPlatform() {
        return MessagingPlatform.DISCORD;
    }
}
