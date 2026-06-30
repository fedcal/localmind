package com.localmind.api.messaging.controller;

import com.localmind.domain.messaging.model.InboundMessage;
import com.localmind.domain.messaging.model.MessagingPlatform;
import com.localmind.domain.messaging.port.in.MessagingReceiveUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/channels/webhook")
public class MessagingWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MessagingWebhookController.class);
    private final MessagingReceiveUseCase receiveUseCase;

    public MessagingWebhookController(MessagingReceiveUseCase receiveUseCase) {
        this.receiveUseCase = receiveUseCase;
    }

    @PostMapping("/slack")
    public ResponseEntity<Object> slackWebhook(@RequestBody Map<String, Object> payload,
                                                @RequestHeader(value = "X-Slack-Signature", required = false) String signature) {
        // Handle Slack URL verification challenge
        if ("url_verification".equals(payload.get("type"))) {
            return ResponseEntity.ok(Map.of("challenge", payload.get("challenge")));
        }

        // Extract event data
        @SuppressWarnings("unchecked")
        Map<String, Object> event = (Map<String, Object>) payload.get("event");
        if (event == null || !"message".equals(event.get("type")) || event.containsKey("bot_id")) {
            return ResponseEntity.ok().build();
        }

        String userId = (String) event.get("user");
        String channelId = (String) event.get("channel");
        String text = (String) event.get("text");
        String messageTs = (String) event.get("ts");

        processMessageAsync(MessagingPlatform.SLACK, userId, channelId, text, messageTs);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/discord")
    public ResponseEntity<Object> discordWebhook(@RequestBody Map<String, Object> payload) {
        // Handle Discord PING verification
        Integer type = (Integer) payload.get("type");
        if (type != null && type == 1) {
            return ResponseEntity.ok(Map.of("type", 1));
        }

        // Only process MESSAGE_CREATE (type 0 in interaction, but message events have "d" data)
        @SuppressWarnings("unchecked")
        Map<String, Object> author = (Map<String, Object>) payload.get("author");
        if (author == null || Boolean.TRUE.equals(author.get("bot"))) {
            return ResponseEntity.ok().build();
        }

        String userId = (String) author.get("id");
        String channelId = (String) payload.get("channel_id");
        String text = (String) payload.get("content");
        String messageId = (String) payload.get("id");

        processMessageAsync(MessagingPlatform.DISCORD, userId, channelId, text, messageId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/telegram")
    public ResponseEntity<Void> telegramWebhook(@RequestBody Map<String, Object> payload,
                                                  @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretToken) {
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) payload.get("message");
        if (message == null) {
            return ResponseEntity.ok().build();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> from = (Map<String, Object>) message.get("from");
        @SuppressWarnings("unchecked")
        Map<String, Object> chat = (Map<String, Object>) message.get("chat");

        if (from == null || chat == null || Boolean.TRUE.equals(from.get("is_bot"))) {
            return ResponseEntity.ok().build();
        }

        String userId = String.valueOf(from.get("id"));
        String chatId = String.valueOf(chat.get("id"));
        String text = (String) message.get("text");
        String messageId = String.valueOf(message.get("message_id"));

        if (text != null && !text.isBlank()) {
            processMessageAsync(MessagingPlatform.TELEGRAM, userId, chatId, text, messageId);
        }
        return ResponseEntity.ok().build();
    }

    @Async
    protected void processMessageAsync(MessagingPlatform platform, String userId,
                                        String channelId, String text, String messageId) {
        try {
            InboundMessage inbound = InboundMessage.builder()
                    .platform(platform)
                    .platformUserId(userId)
                    .platformChannelId(channelId)
                    .text(text)
                    .messageId(messageId)
                    .build();
            receiveUseCase.receiveMessage(inbound);
        } catch (Exception e) {
            log.error("Errore processamento messaggio {} da {}: {}", platform, userId, e.getMessage(), e);
        }
    }
}
