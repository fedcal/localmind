package com.localmind.domain.llm.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.common.model.PageRequest;
import com.localmind.domain.common.model.PageResponse;
import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.port.in.ConversationUseCase;
import com.localmind.domain.llm.port.out.ConversationRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConversationService implements ConversationUseCase {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    public List<ConversationContext> listAll() {
        return conversationRepository.findAllOrderByUpdatedAtDesc();
    }

    @Override
    public PageResponse<ConversationContext> listPaginated(PageRequest pageRequest) {
        return conversationRepository.findAllPaginated(pageRequest);
    }

    @Override
    public PageResponse<ConversationContext> search(String query, PageRequest pageRequest) {
        return conversationRepository.searchByContent(query, pageRequest);
    }

    @Override
    public ConversationContext getById(String id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + id));
    }

    @Override
    public ConversationContext rename(String id, String newTitle) {
        ConversationContext conversation = getById(id);
        conversation.setTitle(newTitle);
        conversation.setUpdatedAt(Instant.now());
        return conversationRepository.save(conversation);
    }

    @Override
    public void deleteById(String id) {
        getById(id);
        conversationRepository.deleteById(id);
    }

    @Override
    public ConversationContext updateMaxContextMessages(String id, Integer maxContextMessages) {
        ConversationContext conversation = getById(id);
        conversation.setMaxContextMessages(maxContextMessages);
        conversation.setUpdatedAt(Instant.now());
        return conversationRepository.save(conversation);
    }

    @Override
    public ConversationContext addTag(String id, String tag) {
        ConversationContext conversation = getById(id);
        conversation.getTags().add(tag);
        conversation.setUpdatedAt(Instant.now());
        return conversationRepository.save(conversation);
    }

    @Override
    public ConversationContext removeTag(String id, String tag) {
        ConversationContext conversation = getById(id);
        conversation.getTags().remove(tag);
        conversation.setUpdatedAt(Instant.now());
        return conversationRepository.save(conversation);
    }

    @Override
    public List<ConversationContext> findByTag(String tag) {
        return conversationRepository.findByTag(tag);
    }

    @Override
    public ConversationContext updateMetadata(String id, Map<String, Object> metadata) {
        ConversationContext conversation = getById(id);
        conversation.setMetadata(metadata);
        conversation.setUpdatedAt(Instant.now());
        return conversationRepository.save(conversation);
    }

    @Override
    public ConversationContext updateSystemPrompt(String id, String systemPrompt) {
        ConversationContext conversation = getById(id);
        conversation.setSystemPrompt(systemPrompt);
        conversation.setUpdatedAt(Instant.now());
        return conversationRepository.save(conversation);
    }

    /**
     * Trova o crea una conversazione per il flusso chat.
     * Se conversationId e' presente, carica la conversazione esistente.
     * Altrimenti crea una nuova conversazione con titolo auto-generato dal primo messaggio.
     * In entrambi i casi aggiunge il messaggio utente e salva.
     */
    public ConversationContext getOrCreateForChat(String conversationId, String userMessage, String systemPrompt) {
        ConversationContext conversation;

        if (conversationId != null && !conversationId.isBlank()) {
            conversation = getById(conversationId);
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                conversation.setSystemPrompt(systemPrompt);
            }
        } else {
            String autoTitle = userMessage.length() > 100
                    ? userMessage.substring(0, 100) + "..."
                    : userMessage;

            conversation = ConversationContext.builder()
                    .id(UUID.randomUUID().toString())
                    .title(autoTitle)
                    .systemPrompt(systemPrompt != null && !systemPrompt.isBlank() ? systemPrompt : null)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }

        conversation.addMessage(ChatMessage.builder()
                .role(ChatMessage.Role.USER)
                .content(userMessage)
                .build());

        return conversationRepository.save(conversation);
    }

    /**
     * Aggiunge la risposta dell'assistente alla conversazione e salva.
     */
    public ConversationContext addAssistantMessage(String conversationId, String content) {
        ConversationContext conversation = getById(conversationId);
        conversation.addMessage(ChatMessage.builder()
                .role(ChatMessage.Role.ASSISTANT)
                .content(content)
                .build());
        return conversationRepository.save(conversation);
    }

    /**
     * Aggiunge il risultato di un tool MCP alla conversazione come messaggio TOOL.
     */
    public ConversationContext addToolMessage(String conversationId, String toolName, String content, Map<String, Object> metadata) {
        ConversationContext conversation = getById(conversationId);
        Map<String, Object> toolMetadata = new java.util.HashMap<>();
        toolMetadata.put("toolName", toolName);
        if (metadata != null) {
            toolMetadata.putAll(metadata);
        }
        conversation.addMessage(ChatMessage.builder()
                .role(ChatMessage.Role.TOOL)
                .content(content)
                .metadata(toolMetadata)
                .build());
        return conversationRepository.save(conversation);
    }
}
