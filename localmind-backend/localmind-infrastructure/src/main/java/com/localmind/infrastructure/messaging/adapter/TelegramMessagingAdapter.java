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
@ConditionalOnProperty(name = "localmind.messaging.telegram.enabled", havingValue = "true")
public class TelegramMessagingAdapter implements MessagingClientPort {

    private static final Logger log = LoggerFactory.getLogger(TelegramMessagingAdapter.class);
    private static final String TELEGRAM_API_URL = "https://api.telegram.org";
    private final WebClient webClient;

    public TelegramMessagingAdapter() {
        this.webClient = WebClient.builder()
                .baseUrl(TELEGRAM_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public void sendMessage(OutboundMessage message) {
        Map<String, Object> body = Map.of(
                "chat_id", message.getChannelId(),
                "text", message.getText(),
                "reply_to_message_id", message.getReplyToMessageId() != null ? message.getReplyToMessageId() : ""
        );

        webClient.post()
                .uri("/bot{token}/sendMessage", message.getBotToken())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> log.error("Errore invio messaggio Telegram: {}", e.getMessage()))
                .subscribe();
    }

    @Override
    public boolean isAvailable(String botToken) {
        try {
            String response = webClient.get()
                    .uri("/bot{token}/getMe", botToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return response != null && response.contains("\"ok\":true");
        } catch (Exception e) {
            log.warn("Telegram bot non raggiungibile: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public MessagingPlatform getPlatform() {
        return MessagingPlatform.TELEGRAM;
    }
}
