package com.localmind.api.messaging.controller;

import com.localmind.api.messaging.dto.CreateChannelRequest;
import com.localmind.api.messaging.dto.MessagingChannelDto;
import com.localmind.domain.messaging.model.MessagingChannel;
import com.localmind.domain.messaging.model.MessagingPlatform;
import com.localmind.domain.messaging.port.in.MessagingChannelUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/channels")
public class MessagingChannelController {

    private final MessagingChannelUseCase channelUseCase;

    public MessagingChannelController(MessagingChannelUseCase channelUseCase) {
        this.channelUseCase = channelUseCase;
    }

    @GetMapping
    public ResponseEntity<List<MessagingChannelDto>> listAll() {
        List<MessagingChannelDto> channels = channelUseCase.listAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(channels);
    }

    @PostMapping
    public ResponseEntity<MessagingChannelDto> create(@Valid @RequestBody CreateChannelRequest request) {
        MessagingChannel channel = MessagingChannel.builder()
                .name(request.getName())
                .platform(MessagingPlatform.valueOf(request.getPlatform()))
                .botToken(request.getBotToken())
                .webhookSecret(request.getWebhookSecret())
                .active(true)
                .defaultSystemPrompt(request.getDefaultSystemPrompt())
                .defaultModel(request.getDefaultModel())
                .defaultProvider(request.getDefaultProvider())
                .enableRag(request.isEnableRag())
                .enableToolCalling(request.isEnableToolCalling())
                .build();

        MessagingChannel saved = channelUseCase.save(channel);
        return ResponseEntity.ok(toDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessagingChannelDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(toDto(channelUseCase.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessagingChannelDto> update(@PathVariable String id,
                                                       @Valid @RequestBody CreateChannelRequest request) {
        MessagingChannel existing = channelUseCase.getById(id);
        existing.setName(request.getName());
        existing.setPlatform(MessagingPlatform.valueOf(request.getPlatform()));
        existing.setBotToken(request.getBotToken());
        existing.setWebhookSecret(request.getWebhookSecret());
        existing.setDefaultSystemPrompt(request.getDefaultSystemPrompt());
        existing.setDefaultModel(request.getDefaultModel());
        existing.setDefaultProvider(request.getDefaultProvider());
        existing.setEnableRag(request.isEnableRag());
        existing.setEnableToolCalling(request.isEnableToolCalling());

        MessagingChannel saved = channelUseCase.save(existing);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        channelUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, String>> test(@PathVariable String id) {
        MessagingChannelUseCase.TestResult result = channelUseCase.testConnection(id);
        return ResponseEntity.ok(Map.of("status", result.status(), "message", result.message()));
    }

    private MessagingChannelDto toDto(MessagingChannel channel) {
        return MessagingChannelDto.builder()
                .id(channel.getId())
                .name(channel.getName())
                .platform(channel.getPlatform().name())
                .botToken(maskToken(channel.getBotToken()))
                .webhookSecret(channel.getWebhookSecret() != null ? "****" : null)
                .active(channel.isActive())
                .defaultSystemPrompt(channel.getDefaultSystemPrompt())
                .defaultModel(channel.getDefaultModel())
                .defaultProvider(channel.getDefaultProvider())
                .enableRag(channel.isEnableRag())
                .enableToolCalling(channel.isEnableToolCalling())
                .createdAt(channel.getCreatedAt())
                .updatedAt(channel.getUpdatedAt())
                .build();
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) return "****";
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
