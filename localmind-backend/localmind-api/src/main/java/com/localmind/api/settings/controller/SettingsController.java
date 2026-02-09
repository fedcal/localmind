package com.localmind.api.settings.controller;

import com.localmind.api.settings.dto.CreateProviderRequestDto;
import com.localmind.api.settings.dto.ProviderConfigDto;
import com.localmind.api.settings.dto.TestResultDto;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmProviderConfig;
import com.localmind.domain.llm.port.in.ProviderConfigUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/settings/providers")
public class SettingsController {

    private final ProviderConfigUseCase useCase;

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

    @GetMapping("/ollama/models")
    public ResponseEntity<List<String>> listOllamaModels(@RequestParam String baseUrl) {
        List<String> models = useCase.listOllamaModels(baseUrl);
        return ResponseEntity.ok(models);
    }

    private ProviderConfigDto toDto(LlmProviderConfig config) {
        return ProviderConfigDto.builder()
                .id(config.getId())
                .name(config.getName())
                .type(config.getType().name())
                .baseUrl(config.getBaseUrl())
                .enabled(config.isEnabled())
                .defaultModel(config.getDefaultModel())
                .models(Collections.emptyList())
                .build();
    }
}
