package com.localmind.api.settings.controller;

import com.localmind.api.settings.dto.CreateProviderRequestDto;
import com.localmind.api.settings.dto.OllamaStatusDto;
import com.localmind.api.settings.dto.ProviderConfigDto;
import com.localmind.api.settings.dto.PullModelRequestDto;
import com.localmind.api.settings.dto.TestResultDto;
import com.localmind.api.settings.dto.UpdateDefaultModelDto;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmProviderConfig;
import com.localmind.domain.llm.model.OllamaStatus;
import com.localmind.domain.llm.port.in.ProviderConfigUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/v1/settings/providers")
public class SettingsController {

    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);
    private final ProviderConfigUseCase useCase;
    private final ExecutorService pullExecutor = Executors.newCachedThreadPool();

    public SettingsController(ProviderConfigUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<List<ProviderConfigDto>> listAll() {
        List<ProviderConfigDto> result = useCase.listAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ProviderConfigDto> create(@Valid @RequestBody CreateProviderRequestDto request) {
        LlmProviderConfig config = LlmProviderConfig.builder()
                .name(request.getName())
                .type(LlmProvider.valueOf(request.getType()))
                .baseUrl(request.getBaseUrl())
                .apiKey(request.getApiKey())
                .defaultModel(request.getDefaultModel())
                .enabled(true)
                .build();

        LlmProviderConfig saved = useCase.save(config);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        useCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<TestResultDto> test(@PathVariable String id) {
        ProviderConfigUseCase.TestResult result = useCase.testConnection(id);
        return ResponseEntity.ok(TestResultDto.builder()
                .status(result.status())
                .message(result.message())
                .build());
    }

    @GetMapping("/ollama/status")
    public ResponseEntity<OllamaStatusDto> getOllamaStatus(
            @RequestParam(defaultValue = "http://localhost:11434") String baseUrl) {
        OllamaStatus status = useCase.checkOllamaStatus(baseUrl);
        return ResponseEntity.ok(toOllamaStatusDto(status));
    }

    @PostMapping("/ollama/start")
    public ResponseEntity<Void> startOllama() {
        log.info("Avvio Ollama richiesto");
        useCase.startOllama();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ollama/models")
    public ResponseEntity<List<String>> listOllamaModels(@RequestParam String baseUrl) {
        List<String> models = useCase.listOllamaModels(baseUrl);
        return ResponseEntity.ok(models);
    }

    @PostMapping("/ollama/models/pull")
    public ResponseEntity<Void> pullOllamaModel(@Valid @RequestBody PullModelRequestDto request) {
        log.info("Pulling Ollama model: {} from {}", request.getModelName(), request.getBaseUrl());
        useCase.pullOllamaModel(request.getBaseUrl(), request.getModelName());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/ollama/models/pull/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter pullOllamaModelStream(@RequestParam String baseUrl, @RequestParam String modelName) {
        log.info("Streaming pull Ollama model: {} from {}", modelName, baseUrl);
        SseEmitter emitter = new SseEmitter(600_000L);

        pullExecutor.execute(() -> {
            try {
                useCase.pullOllamaModelStreaming(baseUrl, modelName, progress -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("progress")
                                .data(progress, MediaType.APPLICATION_JSON));
                        if (progress.isDone() || progress.isError()) {
                            emitter.complete();
                        }
                    } catch (IOException e) {
                        log.warn("SSE send failed: {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                });
            } catch (Exception e) {
                log.error("Pull streaming failed: {}", e.getMessage());
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> log.warn("SSE error: {}", e.getMessage()));

        return emitter;
    }

    @DeleteMapping("/ollama/models/{modelName}")
    public ResponseEntity<Void> deleteOllamaModel(@PathVariable String modelName,
                                                    @RequestParam String baseUrl) {
        log.info("Deleting Ollama model: {} from {}", modelName, baseUrl);
        useCase.deleteOllamaModel(baseUrl, modelName);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/default-model")
    public ResponseEntity<ProviderConfigDto> updateDefaultModel(@PathVariable String id,
                                                                 @Valid @RequestBody UpdateDefaultModelDto request) {
        LlmProviderConfig updated = useCase.updateDefaultModel(id, request.getModel());
        return ResponseEntity.ok(toDto(updated));
    }

    private OllamaStatusDto toOllamaStatusDto(OllamaStatus status) {
        List<OllamaStatusDto.OllamaModelDto> models = Collections.emptyList();
        if (status.getModels() != null) {
            models = status.getModels().stream()
                    .map(m -> OllamaStatusDto.OllamaModelDto.builder()
                            .name(m.getName())
                            .size(formatSize(m.getSizeBytes()))
                            .modifiedAt(m.getModifiedAt())
                            .build())
                    .toList();
        }
        return OllamaStatusDto.builder()
                .online(status.isOnline())
                .version(status.getVersion())
                .models(models)
                .errorMessage(status.getErrorMessage())
                .build();
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        double gb = mb / 1024.0;
        return String.format("%.1f GB", gb);
    }

    private ProviderConfigDto toDto(LlmProviderConfig config) {
        List<String> models = Collections.emptyList();
        if (config.getType() == LlmProvider.OLLAMA && config.getBaseUrl() != null) {
            try {
                models = useCase.listOllamaModels(config.getBaseUrl());
            } catch (Exception e) {
                log.warn("Could not fetch Ollama models from {}: {}", config.getBaseUrl(), e.getMessage());
            }
        }

        return ProviderConfigDto.builder()
                .id(config.getId())
                .name(config.getName())
                .type(config.getType().name())
                .baseUrl(config.getBaseUrl())
                .enabled(config.isEnabled())
                .defaultModel(config.getDefaultModel())
                .models(models)
                .build();
    }
}
