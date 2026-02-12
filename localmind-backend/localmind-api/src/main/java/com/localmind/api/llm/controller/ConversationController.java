package com.localmind.api.llm.controller;

import com.localmind.api.llm.dto.*;
import com.localmind.domain.common.model.PageRequest;
import com.localmind.domain.common.model.PageResponse;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.port.in.ConversationUseCase;
import com.localmind.domain.llm.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationUseCase conversationUseCase;
    private final ConversationService conversationService;

    public ConversationController(ConversationUseCase conversationUseCase, ConversationService conversationService) {
        this.conversationUseCase = conversationUseCase;
        this.conversationService = conversationService;
    }

    @GetMapping
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
    public ResponseEntity<ConversationDto> getById(@PathVariable String id) {
        ConversationContext conversation = conversationUseCase.getById(id);
        return ResponseEntity.ok(toDto(conversation));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ConversationDto> rename(
            @PathVariable String id,
            @Valid @RequestBody RenameConversationRequestDto request) {
        ConversationContext updated = conversationUseCase.rename(id, request.getTitle());
        return ResponseEntity.ok(toDto(updated));
    }

    @PatchMapping("/{id}/system-prompt")
    public ResponseEntity<ConversationDto> updateSystemPrompt(
            @PathVariable String id,
            @RequestBody UpdateSystemPromptRequestDto request) {
        ConversationContext updated = conversationUseCase.updateSystemPrompt(id, request.getSystemPrompt());
        return ResponseEntity.ok(toDto(updated));
    }

    @PatchMapping("/{id}/context-window")
    public ResponseEntity<ConversationDto> updateContextWindow(
            @PathVariable String id,
            @RequestBody UpdateContextWindowRequestDto request) {
        ConversationContext updated = conversationUseCase.updateMaxContextMessages(id, request.getMaxContextMessages());
        return ResponseEntity.ok(toDto(updated));
    }

    @PostMapping("/{id}/tool-result")
    public ResponseEntity<ConversationDto> addToolResult(
            @PathVariable String id,
            @Valid @RequestBody AddToolResultRequestDto request) {
        ConversationContext updated = conversationService.addToolMessage(
                id, request.getToolName(), request.getContent(), request.getMetadata());
        return ResponseEntity.ok(toDto(updated));
    }

    @PostMapping("/{id}/tags")
    public ResponseEntity<ConversationDto> addTag(
            @PathVariable String id,
            @Valid @RequestBody AddTagRequestDto request) {
        ConversationContext updated = conversationUseCase.addTag(id, request.getTag());
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}/tags/{tag}")
    public ResponseEntity<ConversationDto> removeTag(
            @PathVariable String id,
            @PathVariable String tag) {
        ConversationContext updated = conversationUseCase.removeTag(id, tag);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        conversationUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
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
