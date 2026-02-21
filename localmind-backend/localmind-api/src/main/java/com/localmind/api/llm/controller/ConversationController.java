package com.localmind.api.llm.controller;

import com.localmind.api.llm.dto.*;
import com.localmind.domain.common.model.PageRequest;
import com.localmind.domain.common.model.PageResponse;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.model.ConversationSummary;
import com.localmind.domain.llm.port.in.ConversationExportUseCase;
import com.localmind.domain.llm.port.in.ConversationImportUseCase;
import com.localmind.domain.llm.port.in.ConversationUseCase;
import com.localmind.domain.llm.port.out.ConversationReadRepository;
import com.localmind.domain.llm.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/conversations")
@Tag(name = "Conversations", description = "Gestione conversazioni / Conversation management")
public class ConversationController {

    private final ConversationUseCase conversationUseCase;
    private final ConversationService conversationService;
    private final ConversationExportUseCase conversationExportUseCase;
    private final ConversationImportUseCase conversationImportUseCase;
    private final Optional<ConversationReadRepository> conversationReadRepository;

    public ConversationController(ConversationUseCase conversationUseCase,
                                  ConversationService conversationService,
                                  ConversationExportUseCase conversationExportUseCase,
                                  ConversationImportUseCase conversationImportUseCase,
                                  Optional<ConversationReadRepository> conversationReadRepository) {
        this.conversationUseCase = conversationUseCase;
        this.conversationService = conversationService;
        this.conversationExportUseCase = conversationExportUseCase;
        this.conversationImportUseCase = conversationImportUseCase;
        this.conversationReadRepository = conversationReadRepository;
    }

    @GetMapping
    @Operation(summary = "Lista conversazioni / List conversations")
    @ApiResponse(responseCode = "200", description = "Lista conversazioni / Conversation list")
    public ResponseEntity<List<ConversationSummaryDto>> listAll(@RequestParam(required = false) String tag) {
        List<ConversationContext> conversations = (tag != null && !tag.isBlank())
                ? conversationUseCase.findByTag(tag)
                : conversationUseCase.listAll();
        List<ConversationSummaryDto> summaries = conversations.stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/paginated")
    @Operation(summary = "Lista conversazioni paginata / Paginated conversation list")
    @ApiResponse(responseCode = "200", description = "Pagina di conversazioni / Page of conversations")
    public ResponseEntity<PageResponse<ConversationSummaryDto>> listPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ConversationContext> result = conversationUseCase.listPaginated(new PageRequest(page, size));
        PageResponse<ConversationSummaryDto> response = PageResponse.<ConversationSummaryDto>builder()
                .content(result.getContent().stream().map(this::toSummaryDto).collect(Collectors.toList()))
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(result.getPage())
                .size(result.getSize())
                .hasMore(result.isHasMore())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Cerca conversazioni / Search conversations")
    @ApiResponse(responseCode = "200", description = "Risultati ricerca / Search results")
    public ResponseEntity<PageResponse<ConversationSummaryDto>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ConversationContext> result = conversationUseCase.search(query, new PageRequest(page, size));
        PageResponse<ConversationSummaryDto> response = PageResponse.<ConversationSummaryDto>builder()
                .content(result.getContent().stream().map(this::toSummaryDto).collect(Collectors.toList()))
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(result.getPage())
                .size(result.getSize())
                .hasMore(result.isHasMore())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio conversazione / Get conversation detail")
    @ApiResponse(responseCode = "200", description = "Conversazione trovata / Conversation found")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<ConversationDto> getById(@PathVariable String id) {
        ConversationContext conversation = conversationUseCase.getById(id);
        return ResponseEntity.ok(toDto(conversation));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Rinomina conversazione / Rename conversation")
    @ApiResponse(responseCode = "200", description = "Conversazione rinominata / Conversation renamed")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<ConversationDto> rename(
            @PathVariable String id,
            @Valid @RequestBody RenameConversationRequestDto request) {
        ConversationContext updated = conversationUseCase.rename(id, request.getTitle());
        return ResponseEntity.ok(toDto(updated));
    }

    @PatchMapping("/{id}/system-prompt")
    @Operation(summary = "Aggiorna system prompt / Update system prompt")
    @ApiResponse(responseCode = "200", description = "System prompt aggiornato / System prompt updated")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<ConversationDto> updateSystemPrompt(
            @PathVariable String id,
            @RequestBody UpdateSystemPromptRequestDto request) {
        ConversationContext updated = conversationUseCase.updateSystemPrompt(id, request.getSystemPrompt());
        return ResponseEntity.ok(toDto(updated));
    }

    @PatchMapping("/{id}/context-window")
    @Operation(summary = "Aggiorna finestra di contesto / Update context window")
    @ApiResponse(responseCode = "200", description = "Finestra di contesto aggiornata / Context window updated")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<ConversationDto> updateContextWindow(
            @PathVariable String id,
            @RequestBody UpdateContextWindowRequestDto request) {
        ConversationContext updated = conversationUseCase.updateMaxContextMessages(id, request.getMaxContextMessages());
        return ResponseEntity.ok(toDto(updated));
    }

    @PostMapping("/{id}/tool-result")
    @Operation(summary = "Aggiungi risultato tool / Add tool result")
    @ApiResponse(responseCode = "200", description = "Risultato tool aggiunto / Tool result added")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<ConversationDto> addToolResult(
            @PathVariable String id,
            @Valid @RequestBody AddToolResultRequestDto request) {
        ConversationContext updated = conversationService.addToolMessage(
                id, request.getToolName(), request.getContent(), request.getMetadata());
        return ResponseEntity.ok(toDto(updated));
    }

    @PostMapping("/{id}/tags")
    @Operation(summary = "Aggiungi tag alla conversazione / Add tag to conversation")
    @ApiResponse(responseCode = "200", description = "Tag aggiunto / Tag added")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<ConversationDto> addTag(
            @PathVariable String id,
            @Valid @RequestBody AddTagRequestDto request) {
        ConversationContext updated = conversationUseCase.addTag(id, request.getTag());
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}/tags/{tag}")
    @Operation(summary = "Rimuovi tag dalla conversazione / Remove tag from conversation")
    @ApiResponse(responseCode = "200", description = "Tag rimosso / Tag removed")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<ConversationDto> removeTag(
            @PathVariable String id,
            @PathVariable String tag) {
        ConversationContext updated = conversationUseCase.removeTag(id, tag);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina conversazione / Delete conversation")
    @ApiResponse(responseCode = "204", description = "Conversazione eliminata / Conversation deleted")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        conversationUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/export")
    @Operation(summary = "Esporta conversazione / Export conversation")
    @ApiResponse(responseCode = "200", description = "File esportato / File exported")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<byte[]> exportConversation(
            @PathVariable String id,
            @RequestParam(defaultValue = "json") String format) {
        byte[] data;
        String contentType;
        String extension;
        switch (format.toLowerCase()) {
            case "md", "markdown" -> {
                data = conversationExportUseCase.exportToMarkdown(id);
                contentType = "text/markdown";
                extension = "md";
            }
            case "pdf" -> {
                data = conversationExportUseCase.exportToPdf(id);
                contentType = "application/pdf";
                extension = "pdf";
            }
            default -> {
                data = conversationExportUseCase.exportToJson(id);
                contentType = "application/json";
                extension = "json";
            }
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=conversation." + extension)
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    @GetMapping("/export")
    @Operation(summary = "Esporta conversazioni multiple / Export multiple conversations")
    @ApiResponse(responseCode = "200", description = "File esportato / File exported")
    public ResponseEntity<byte[]> exportMultiple(
            @RequestParam List<String> ids,
            @RequestParam(defaultValue = "json") String format) {
        byte[] data = conversationExportUseCase.exportMultipleToJson(ids);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=conversations.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @PostMapping("/import")
    @Operation(summary = "Importa conversazioni da file / Import conversations from file")
    @ApiResponse(responseCode = "200", description = "Conversazioni importate / Conversations imported")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<List<ConversationDto>> importConversations(@RequestParam("file") MultipartFile file) {
        try {
            List<ConversationContext> imported = conversationImportUseCase.importFromJson(file.getInputStream());
            List<ConversationDto> dtos = imported.stream().map(this::toDto).toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to import conversations: " + e.getMessage(), e);
        }
    }

    // ===== CQRS Read Model Endpoints =====

    @GetMapping("/summaries")
    @Operation(summary = "Riepiloghi conversazioni recenti (CQRS) / Recent conversation summaries (CQRS)")
    @ApiResponse(responseCode = "200", description = "Riepiloghi conversazioni / Conversation summaries")
    public ResponseEntity<List<ConversationSummaryReadDto>> getSummaries(
            @RequestParam(defaultValue = "20") int limit) {
        if (conversationReadRepository.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<ConversationSummaryReadDto> summaries = conversationReadRepository.get()
                .findRecentSummaries(limit).stream()
                .map(this::toSummaryReadDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/summaries/search")
    @Operation(summary = "Cerca riepiloghi conversazioni (CQRS) / Search conversation summaries (CQRS)")
    @ApiResponse(responseCode = "200", description = "Risultati ricerca / Search results")
    public ResponseEntity<List<ConversationSummaryReadDto>> searchSummaries(
            @RequestParam String query) {
        if (conversationReadRepository.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<ConversationSummaryReadDto> summaries = conversationReadRepository.get()
                .searchSummaries(query).stream()
                .map(this::toSummaryReadDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    private ConversationSummaryReadDto toSummaryReadDto(ConversationSummary s) {
        return ConversationSummaryReadDto.builder()
                .id(s.getId())
                .title(s.getTitle())
                .messageCount(s.getMessageCount())
                .lastMessagePreview(s.getLastMessagePreview())
                .tags(s.getTags())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private ConversationDto toDto(ConversationContext c) {
        return ConversationDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .systemPrompt(c.getSystemPrompt())
                .maxContextMessages(c.getMaxContextMessages())
                .tags(c.getTags())
                .metadata(c.getMetadata())
                .messages(c.getMessages() != null
                        ? c.getMessages().stream()
                            .map(m -> ChatMessageDto.builder()
                                    .role(m.getRole().name())
                                    .content(m.getContent())
                                    .metadata(m.getMetadata())
                                    .build())
                            .collect(Collectors.toList())
                        : List.<ChatMessageDto>of())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ConversationSummaryDto toSummaryDto(ConversationContext c) {
        return ConversationSummaryDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .messageCount(c.getMessages() != null ? c.getMessages().size() : 0)
                .tags(c.getTags())
                .metadata(c.getMetadata())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
