package com.localmind.infrastructure.persistence.sync;

import com.localmind.domain.common.event.ConversationCompletedEvent;
import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.port.out.ConversationRepository;
import com.localmind.infrastructure.persistence.entity.ConversationSummaryEntity;
import com.localmind.infrastructure.persistence.repository.ConversationSummaryJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servizio di sincronizzazione per il modello di lettura delle conversazioni (CQRS).
 * Ascolta gli eventi di conversazione completata e sincronizza periodicamente.
 *
 * Sync service for the conversation read model (CQRS).
 * Listens to conversation completed events and synchronizes periodically.
 */
@Component
public class ConversationSummarySyncService {

    private static final Logger log = LoggerFactory.getLogger(ConversationSummarySyncService.class);

    private final ConversationRepository conversationRepository;
    private final ConversationSummaryJpaRepository summaryJpaRepository;

    public ConversationSummarySyncService(ConversationRepository conversationRepository,
                                           ConversationSummaryJpaRepository summaryJpaRepository) {
        this.conversationRepository = conversationRepository;
        this.summaryJpaRepository = summaryJpaRepository;
    }

    /**
     * Ascolta l'evento ConversationCompletedEvent e aggiorna il riepilogo.
     * Listens to ConversationCompletedEvent and updates the summary.
     */
    @EventListener
    public void onConversationCompleted(ConversationCompletedEvent event) {
        try {
            syncSingleConversation(event.getConversationId());
            log.debug("Synced conversation summary for conversationId={}", event.getConversationId());
        } catch (Exception e) {
            log.warn("Failed to sync conversation summary for conversationId={}: {}",
                    event.getConversationId(), e.getMessage());
        }
    }

    /**
     * Sincronizzazione periodica di tutte le conversazioni (ogni ora).
     * Periodic sync of all conversations (every hour).
     */
    @Scheduled(fixedRate = 3600000)
    public void syncAll() {
        try {
            List<ConversationContext> conversations = conversationRepository.findAllOrderByUpdatedAtDesc();
            int synced = 0;
            for (ConversationContext conversation : conversations) {
                upsertSummary(conversation);
                synced++;
            }
            log.info("CQRS conversation summary sync completed: {} conversations synced", synced);
        } catch (Exception e) {
            log.error("Failed to sync conversation summaries: {}", e.getMessage(), e);
        }
    }

    /**
     * Sincronizza una singola conversazione per ID.
     * Syncs a single conversation by ID.
     */
    public void syncSingleConversation(String conversationId) {
        Optional<ConversationContext> conversation = conversationRepository.findById(conversationId);
        conversation.ifPresent(this::upsertSummary);
    }

    private void upsertSummary(ConversationContext conversation) {
        UUID uuid = UUID.fromString(conversation.getId());

        List<ChatMessage> messages = conversation.getMessages();
        int messageCount = messages != null ? messages.size() : 0;
        String lastMessagePreview = extractLastMessagePreview(messages);
        String tagsString = conversation.getTags() != null
                ? String.join(",", conversation.getTags())
                : "";

        ConversationSummaryEntity entity = summaryJpaRepository.findById(uuid)
                .orElse(ConversationSummaryEntity.builder()
                        .id(uuid)
                        .createdAt(conversation.getCreatedAt() != null ? conversation.getCreatedAt() : Instant.now())
                        .build());

        entity.setTitle(conversation.getTitle());
        entity.setMessageCount(messageCount);
        entity.setLastMessagePreview(lastMessagePreview);
        entity.setTags(tagsString);
        entity.setUpdatedAt(conversation.getUpdatedAt() != null ? conversation.getUpdatedAt() : Instant.now());

        summaryJpaRepository.save(entity);
    }

    private String extractLastMessagePreview(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        ChatMessage lastMessage = messages.get(messages.size() - 1);
        String content = lastMessage.getContent();
        if (content == null) {
            return null;
        }
        return content.length() > 500 ? content.substring(0, 500) : content;
    }
}
