package com.localmind.infrastructure.persistence.sync;

import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.port.out.ConversationRepository;
import com.localmind.infrastructure.persistence.entity.ConversationSummaryEntity;
import com.localmind.infrastructure.persistence.repository.ConversationSummaryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationSummarySyncServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationSummaryJpaRepository summaryJpaRepository;

    private ConversationSummarySyncService syncService;

    private final Instant NOW = Instant.parse("2026-02-21T10:00:00Z");
    private final Instant LATER = Instant.parse("2026-02-21T11:00:00Z");

    @BeforeEach
    void setUp() {
        syncService = new ConversationSummarySyncService(conversationRepository, summaryJpaRepository);
    }

    @Test
    void testSyncAll_createsOrUpdatesSummaries() {
        UUID uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        ConversationContext conv1 = ConversationContext.builder()
                .id(uuid1.toString())
                .title("First conversation")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Hi!").build()
                ))
                .tags(new HashSet<>(Set.of("important")))
                .createdAt(NOW)
                .updatedAt(LATER)
                .build();

        ConversationContext conv2 = ConversationContext.builder()
                .id(uuid2.toString())
                .title("Second conversation")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Question").build()
                ))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(conversationRepository.findAllOrderByUpdatedAtDesc())
                .thenReturn(List.of(conv1, conv2));
        when(summaryJpaRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());
        when(summaryJpaRepository.save(any(ConversationSummaryEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        syncService.syncAll();

        verify(summaryJpaRepository, times(2)).save(any(ConversationSummaryEntity.class));

        ArgumentCaptor<ConversationSummaryEntity> captor = ArgumentCaptor.forClass(ConversationSummaryEntity.class);
        verify(summaryJpaRepository, times(2)).save(captor.capture());

        List<ConversationSummaryEntity> saved = captor.getAllValues();

        ConversationSummaryEntity first = saved.get(0);
        assertThat(first.getId()).isEqualTo(uuid1);
        assertThat(first.getTitle()).isEqualTo("First conversation");
        assertThat(first.getMessageCount()).isEqualTo(2);
        assertThat(first.getLastMessagePreview()).isEqualTo("Hi!");
        assertThat(first.getTags()).contains("important");

        ConversationSummaryEntity second = saved.get(1);
        assertThat(second.getId()).isEqualTo(uuid2);
        assertThat(second.getTitle()).isEqualTo("Second conversation");
        assertThat(second.getMessageCount()).isEqualTo(1);
        assertThat(second.getLastMessagePreview()).isEqualTo("Question");
    }

    @Test
    void testSyncSingle_updatesExistingSummary() {
        UUID uuid = UUID.fromString("33333333-3333-3333-3333-333333333333");

        ConversationContext conversation = ConversationContext.builder()
                .id(uuid.toString())
                .title("Updated conversation")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Updated reply").build(),
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("New question").build()
                ))
                .tags(new HashSet<>(Set.of("work", "project")))
                .createdAt(NOW)
                .updatedAt(LATER)
                .build();

        ConversationSummaryEntity existingEntity = ConversationSummaryEntity.builder()
                .id(uuid)
                .title("Old title")
                .messageCount(1)
                .lastMessagePreview("Old preview")
                .tags("old-tag")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(conversationRepository.findById(uuid.toString()))
                .thenReturn(Optional.of(conversation));
        when(summaryJpaRepository.findById(uuid))
                .thenReturn(Optional.of(existingEntity));
        when(summaryJpaRepository.save(any(ConversationSummaryEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSingleConversation(uuid.toString());

        ArgumentCaptor<ConversationSummaryEntity> captor = ArgumentCaptor.forClass(ConversationSummaryEntity.class);
        verify(summaryJpaRepository).save(captor.capture());

        ConversationSummaryEntity saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(uuid);
        assertThat(saved.getTitle()).isEqualTo("Updated conversation");
        assertThat(saved.getMessageCount()).isEqualTo(3);
        assertThat(saved.getLastMessagePreview()).isEqualTo("New question");
        assertThat(saved.getUpdatedAt()).isEqualTo(LATER);
    }

    @Test
    void testSyncAll_handlesEmptyConversations() {
        when(conversationRepository.findAllOrderByUpdatedAtDesc())
                .thenReturn(List.of());

        syncService.syncAll();

        verify(summaryJpaRepository, never()).save(any(ConversationSummaryEntity.class));
    }
}
