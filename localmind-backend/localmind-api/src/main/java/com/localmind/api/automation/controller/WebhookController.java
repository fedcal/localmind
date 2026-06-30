package com.localmind.api.automation.controller;

import com.localmind.api.automation.dto.WebhookRequestDto;
import com.localmind.api.automation.dto.WebhookResponseDto;
import com.localmind.domain.automation.model.AutomationEventType;
import com.localmind.domain.automation.model.Webhook;
import com.localmind.domain.automation.port.in.AutomationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks", description = "Automazione webhook N8N / N8N webhook automation")
public class WebhookController {

    private final AutomationUseCase automationUseCase;

    public WebhookController(AutomationUseCase automationUseCase) {
        this.automationUseCase = automationUseCase;
    }

    @GetMapping
    @Operation(summary = "Lista webhook / List webhooks")
    @ApiResponse(responseCode = "200", description = "Lista webhook / Webhook list")
    public ResponseEntity<List<WebhookResponseDto>> listAll() {
        return ResponseEntity.ok(automationUseCase.listWebhooks().stream()
                .map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio webhook / Get webhook detail")
    @ApiResponse(responseCode = "200", description = "Webhook trovato / Webhook found")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<WebhookResponseDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(toDto(automationUseCase.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crea webhook / Create webhook")
    @ApiResponse(responseCode = "200", description = "Webhook creato / Webhook created")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<WebhookResponseDto> create(@Valid @RequestBody WebhookRequestDto request) {
        Webhook webhook = Webhook.builder()
                .name(request.getName())
                .url(request.getUrl())
                .eventType(AutomationEventType.valueOf(request.getEventType()))
                .active(request.isActive())
                .build();
        return ResponseEntity.ok(toDto(automationUseCase.save(webhook)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna webhook / Update webhook")
    @ApiResponse(responseCode = "200", description = "Webhook aggiornato / Webhook updated")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<WebhookResponseDto> update(@PathVariable String id,
                                                      @Valid @RequestBody WebhookRequestDto request) {
        Webhook webhook = Webhook.builder()
                .id(id)
                .name(request.getName())
                .url(request.getUrl())
                .eventType(AutomationEventType.valueOf(request.getEventType()))
                .active(request.isActive())
                .build();
        return ResponseEntity.ok(toDto(automationUseCase.save(webhook)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina webhook / Delete webhook")
    @ApiResponse(responseCode = "204", description = "Webhook eliminato / Webhook deleted")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        automationUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Testa webhook / Test webhook")
    @ApiResponse(responseCode = "200", description = "Test eseguito / Test executed")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> test(@PathVariable String id) {
        automationUseCase.testWebhook(id);
        return ResponseEntity.ok().build();
    }

    private WebhookResponseDto toDto(Webhook w) {
        return WebhookResponseDto.builder()
                .id(w.getId())
                .name(w.getName())
                .url(w.getUrl())
                .eventType(w.getEventType().name())
                .active(w.isActive())
                .build();
    }
}
