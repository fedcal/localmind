package com.localmind.domain.llm.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.common.model.PageRequest;
import com.localmind.domain.common.model.PageResponse;
import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.port.out.ConversationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private ConversationService service;

    @Test
    void listAll_shouldDelegateToRepository() {
        List<ConversationContext> conversations = List.of(
                ConversationContext.builder().id("1").title("Prima conversazione").build(),
                ConversationContext.builder().id("2").title("Seconda conversazione").build()
        );

        when(conversationRepository.findAllOrderByUpdatedAtDesc()).thenReturn(conversations);

        List<ConversationContext> result = service.listAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ConversationContext::getTitle)
                .containsExactly("Prima conversazione", "Seconda conversazione");
        verify(conversationRepository).findAllOrderByUpdatedAtDesc();
    }

    @Test
    void getById_shouldReturnConversation_whenFound() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Test conversazione")
                .build();

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));

        ConversationContext result = service.getById("conv-1");

        assertThat(result.getId()).isEqualTo("conv-1");
        assertThat(result.getTitle()).isEqualTo("Test conversazione");
        verify(conversationRepository).findById("conv-1");
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenNotFound() {
        when(conversationRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("non-existent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("non-existent");

        verify(conversationRepository).findById("non-existent");
    }

    @Test
    void rename_shouldUpdateTitleAndTimestamp() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Vecchio titolo")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.rename("conv-1", "Nuovo titolo");

        assertThat(result.getTitle()).isEqualTo("Nuovo titolo");
        assertThat(result.getUpdatedAt()).isAfter(Instant.parse("2025-01-01T00:00:00Z"));
        verify(conversationRepository).findById("conv-1");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void deleteById_shouldDeleteExistingConversation() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Da eliminare")
                .build();

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));

        service.deleteById("conv-1");

        verify(conversationRepository).findById("conv-1");
        verify(conversationRepository).deleteById("conv-1");
    }

    @Test
    void deleteById_shouldThrowResourceNotFoundException_whenNotFound() {
        when(conversationRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteById("non-existent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("non-existent");

        verify(conversationRepository).findById("non-existent");
        verify(conversationRepository, never()).deleteById(any());
    }

    @Test
    void getOrCreateForChat_shouldCreateNewConversation_whenIdIsNull() {
        String userMessage = "Ciao, come stai?";

        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.getOrCreateForChat(null, userMessage, null);

        assertThat(result.getId()).isNotNull().isNotBlank();
        assertThat(result.getTitle()).isEqualTo(userMessage);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.getMessages().get(0).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(result.getMessages().get(0).getContent()).isEqualTo(userMessage);

        ArgumentCaptor<ConversationContext> captor = ArgumentCaptor.forClass(ConversationContext.class);
        verify(conversationRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo(userMessage);
        verify(conversationRepository, never()).findById(any());
    }

    @Test
    void getOrCreateForChat_shouldLoadExistingConversation_whenIdIsProvided() {
        ConversationContext existing = ConversationContext.builder()
                .id("conv-existing")
                .title("Conversazione esistente")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        when(conversationRepository.findById("conv-existing")).thenReturn(Optional.of(existing));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.getOrCreateForChat("conv-existing", "Nuovo messaggio", null);

        assertThat(result.getId()).isEqualTo("conv-existing");
        assertThat(result.getTitle()).isEqualTo("Conversazione esistente");
        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.getMessages().get(0).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(result.getMessages().get(0).getContent()).isEqualTo("Nuovo messaggio");
        verify(conversationRepository).findById("conv-existing");
        verify(conversationRepository).save(existing);
    }

    @Test
    void getOrCreateForChat_shouldTruncateTitle_whenMessageExceeds100Chars() {
        String longMessage = "A".repeat(150);

        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.getOrCreateForChat(null, longMessage, null);

        assertThat(result.getTitle()).hasSize(103); // 100 chars + "..."
        assertThat(result.getTitle()).endsWith("...");
        assertThat(result.getTitle()).startsWith("A".repeat(100));
        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.getMessages().get(0).getContent()).isEqualTo(longMessage);

        verify(conversationRepository).save(any(ConversationContext.class));
        verify(conversationRepository, never()).findById(any());
    }

    @Test
    void getOrCreateForChat_shouldCreateNewConversation_whenIdIsBlank() {
        String userMessage = "Messaggio di test";

        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.getOrCreateForChat("   ", userMessage, null);

        assertThat(result.getId()).isNotNull().isNotBlank();
        assertThat(result.getTitle()).isEqualTo(userMessage);
        assertThat(result.getMessages()).hasSize(1);
        verify(conversationRepository, never()).findById(any());
        verify(conversationRepository).save(any(ConversationContext.class));
    }

    @Test
    void getOrCreateForChat_withSystemPrompt_shouldSetItOnNewConversation() {
        String userMessage = "Messaggio utente";
        String systemPrompt = "Sei un assistente esperto in programmazione Java";

        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.getOrCreateForChat(null, userMessage, systemPrompt);

        assertThat(result.getSystemPrompt()).isEqualTo(systemPrompt);
        assertThat(result.getId()).isNotNull().isNotBlank();
        assertThat(result.getTitle()).isEqualTo(userMessage);
        assertThat(result.getMessages()).hasSize(1);

        ArgumentCaptor<ConversationContext> captor = ArgumentCaptor.forClass(ConversationContext.class);
        verify(conversationRepository).save(captor.capture());
        assertThat(captor.getValue().getSystemPrompt()).isEqualTo(systemPrompt);
    }

    @Test
    void getOrCreateForChat_withSystemPrompt_shouldSetItOnExistingConversation() {
        ConversationContext existing = ConversationContext.builder()
                .id("conv-existing")
                .title("Conversazione esistente")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        String systemPrompt = "Sei un assistente esperto in Spring Boot";

        when(conversationRepository.findById("conv-existing")).thenReturn(Optional.of(existing));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.getOrCreateForChat("conv-existing", "Nuovo messaggio", systemPrompt);

        assertThat(result.getSystemPrompt()).isEqualTo(systemPrompt);
        assertThat(result.getId()).isEqualTo("conv-existing");
        assertThat(result.getMessages()).hasSize(1);
        verify(conversationRepository).findById("conv-existing");
        verify(conversationRepository).save(existing);
    }

    @Test
    void getOrCreateForChat_withBlankSystemPrompt_shouldNotSetIt() {
        String userMessage = "Messaggio di test";

        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.getOrCreateForChat(null, userMessage, "   ");

        assertThat(result.getSystemPrompt()).isNull();
        assertThat(result.getId()).isNotNull().isNotBlank();
        assertThat(result.getTitle()).isEqualTo(userMessage);

        ArgumentCaptor<ConversationContext> captor = ArgumentCaptor.forClass(ConversationContext.class);
        verify(conversationRepository).save(captor.capture());
        assertThat(captor.getValue().getSystemPrompt()).isNull();
    }

    @Test
    void addAssistantMessage_shouldAddMessageAndSave() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Conversazione")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.addAssistantMessage("conv-1", "Ecco la risposta");

        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.getMessages().get(0).getRole()).isEqualTo(ChatMessage.Role.ASSISTANT);
        assertThat(result.getMessages().get(0).getContent()).isEqualTo("Ecco la risposta");
        assertThat(result.getUpdatedAt()).isAfter(Instant.parse("2025-01-01T00:00:00Z"));
        verify(conversationRepository).findById("conv-1");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void updateSystemPrompt_shouldUpdateAndSave() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Conversazione")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        String newSystemPrompt = "Sei un assistente esperto in Machine Learning";

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.updateSystemPrompt("conv-1", newSystemPrompt);

        assertThat(result.getSystemPrompt()).isEqualTo(newSystemPrompt);
        assertThat(result.getUpdatedAt()).isAfter(Instant.parse("2025-01-01T00:00:00Z"));
        verify(conversationRepository).findById("conv-1");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void updateSystemPrompt_notFound_shouldThrow() {
        when(conversationRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSystemPrompt("non-existent", "Nuovo prompt"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("non-existent");

        verify(conversationRepository).findById("non-existent");
        verify(conversationRepository, never()).save(any());
    }

    // ===== P2 - Context Sharing: Tool Results =====

    @Test
    void addToolMessage_withMetadata_addsToolMessageToConversation() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Conversazione con tool")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        Map<String, Object> additionalMetadata = new HashMap<>();
        additionalMetadata.put("executionTime", 150L);
        additionalMetadata.put("serverId", "mcp-server-1");

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.addToolMessage("conv-1", "search_tool", "Risultato ricerca", additionalMetadata);

        assertThat(result.getMessages()).hasSize(1);
        ChatMessage toolMessage = result.getMessages().get(0);
        assertThat(toolMessage.getRole()).isEqualTo(ChatMessage.Role.TOOL);
        assertThat(toolMessage.getContent()).isEqualTo("Risultato ricerca");
        assertThat(toolMessage.getMetadata()).containsEntry("toolName", "search_tool");
        assertThat(toolMessage.getMetadata()).containsEntry("executionTime", 150L);
        assertThat(toolMessage.getMetadata()).containsEntry("serverId", "mcp-server-1");
        verify(conversationRepository).findById("conv-1");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void addToolMessage_withoutMetadata_addsToolMessageWithOnlyToolName() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-2")
                .title("Conversazione senza metadata extra")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        when(conversationRepository.findById("conv-2")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.addToolMessage("conv-2", "file_reader", "Contenuto file letto", null);

        assertThat(result.getMessages()).hasSize(1);
        ChatMessage toolMessage = result.getMessages().get(0);
        assertThat(toolMessage.getRole()).isEqualTo(ChatMessage.Role.TOOL);
        assertThat(toolMessage.getContent()).isEqualTo("Contenuto file letto");
        assertThat(toolMessage.getMetadata()).hasSize(1);
        assertThat(toolMessage.getMetadata()).containsEntry("toolName", "file_reader");
        verify(conversationRepository).findById("conv-2");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void addToolMessage_conversationNotFound_throwsException() {
        when(conversationRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addToolMessage("non-existent", "any_tool", "Contenuto", null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("non-existent");

        verify(conversationRepository).findById("non-existent");
        verify(conversationRepository, never()).save(any());
    }

    // ===== P5 - Memory Window / Context Limit =====

    @Test
    void updateMaxContextMessages_shouldSetValueAndSave() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Conversazione")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.updateMaxContextMessages("conv-1", 20);

        assertThat(result.getMaxContextMessages()).isEqualTo(20);
        assertThat(result.getUpdatedAt()).isAfter(Instant.parse("2025-01-01T00:00:00Z"));
        verify(conversationRepository).findById("conv-1");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void updateMaxContextMessages_withNull_shouldClearValue() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Conversazione")
                .maxContextMessages(20)
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.updateMaxContextMessages("conv-1", null);

        assertThat(result.getMaxContextMessages()).isNull();
        assertThat(result.getUpdatedAt()).isAfter(Instant.parse("2025-01-01T00:00:00Z"));
        verify(conversationRepository).findById("conv-1");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void updateMaxContextMessages_notFound_shouldThrow() {
        when(conversationRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateMaxContextMessages("non-existent", 10))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("non-existent");

        verify(conversationRepository).findById("non-existent");
        verify(conversationRepository, never()).save(any());
    }

    // ===== P5 - ConversationContext.getContextWindow() =====

    @Test
    void getContextWindow_belowLimit_returnsAllMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            messages.add(ChatMessage.builder().role(ChatMessage.Role.USER).content("msg-" + i).build());
        }

        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Test")
                .messages(messages)
                .build();

        List<ChatMessage> result = conversation.getContextWindow(10);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getContent()).isEqualTo("msg-0");
        assertThat(result.get(1).getContent()).isEqualTo("msg-1");
        assertThat(result.get(2).getContent()).isEqualTo("msg-2");
    }

    @Test
    void getContextWindow_aboveLimit_returnsLastN() {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            messages.add(ChatMessage.builder().role(ChatMessage.Role.USER).content("msg-" + i).build());
        }

        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Test")
                .messages(messages)
                .build();

        List<ChatMessage> result = conversation.getContextWindow(5);

        assertThat(result).hasSize(5);
        assertThat(result.get(0).getContent()).isEqualTo("msg-5");
        assertThat(result.get(1).getContent()).isEqualTo("msg-6");
        assertThat(result.get(2).getContent()).isEqualTo("msg-7");
        assertThat(result.get(3).getContent()).isEqualTo("msg-8");
        assertThat(result.get(4).getContent()).isEqualTo("msg-9");
    }

    @Test
    void getContextWindow_usesConversationMax_overDefault() {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            messages.add(ChatMessage.builder().role(ChatMessage.Role.USER).content("msg-" + i).build());
        }

        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Test")
                .maxContextMessages(3)
                .messages(messages)
                .build();

        List<ChatMessage> result = conversation.getContextWindow(50);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getContent()).isEqualTo("msg-7");
        assertThat(result.get(1).getContent()).isEqualTo("msg-8");
        assertThat(result.get(2).getContent()).isEqualTo("msg-9");
    }

    @Test
    void getContextWindow_nullMaxUsesDefault() {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            messages.add(ChatMessage.builder().role(ChatMessage.Role.USER).content("msg-" + i).build());
        }

        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Test")
                .maxContextMessages(null)
                .messages(messages)
                .build();

        List<ChatMessage> result = conversation.getContextWindow(5);

        assertThat(result).hasSize(5);
        assertThat(result.get(0).getContent()).isEqualTo("msg-5");
        assertThat(result.get(4).getContent()).isEqualTo("msg-9");
    }

    @Test
    void getContextWindow_emptyMessages_returnsEmpty() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Test")
                .messages(new ArrayList<>())
                .build();

        List<ChatMessage> result = conversation.getContextWindow(10);

        assertThat(result).isEmpty();
    }

    // ===== P6 - Metadata e Tag sulle Conversazioni =====

    @Test
    void addTag_shouldAddTagAndSave() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Conversazione")
                .tags(new HashSet<>())
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.addTag("conv-1", "important");

        assertThat(result.getTags()).contains("important");
        assertThat(result.getUpdatedAt()).isAfter(Instant.parse("2025-01-01T00:00:00Z"));
        verify(conversationRepository).findById("conv-1");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void addTag_notFound_shouldThrow() {
        when(conversationRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addTag("non-existent", "important"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("non-existent");

        verify(conversationRepository).findById("non-existent");
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void removeTag_shouldRemoveTagAndSave() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Conversazione")
                .tags(new HashSet<>(Set.of("tag1", "tag2")))
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.removeTag("conv-1", "tag1");

        assertThat(result.getTags()).hasSize(1);
        assertThat(result.getTags()).contains("tag2");
        assertThat(result.getTags()).doesNotContain("tag1");
        assertThat(result.getUpdatedAt()).isAfter(Instant.parse("2025-01-01T00:00:00Z"));
        verify(conversationRepository).findById("conv-1");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void removeTag_notFound_shouldThrow() {
        when(conversationRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeTag("non-existent", "tag1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("non-existent");

        verify(conversationRepository).findById("non-existent");
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void findByTag_shouldDelegateToRepository() {
        List<ConversationContext> conversations = List.of(
                ConversationContext.builder().id("1").title("Conv 1")
                        .tags(new HashSet<>(Set.of("important"))).build(),
                ConversationContext.builder().id("2").title("Conv 2")
                        .tags(new HashSet<>(Set.of("important"))).build()
        );

        when(conversationRepository.findByTag("important")).thenReturn(conversations);

        List<ConversationContext> result = service.findByTag("important");

        assertThat(result).hasSize(2);
        verify(conversationRepository).findByTag("important");
    }

    @Test
    void updateMetadata_shouldSetMetadataAndSave() {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Conversazione")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        Map<String, Object> metadata = Map.of("category", "work", "priority", 1);

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationContext result = service.updateMetadata("conv-1", metadata);

        assertThat(result.getMetadata()).containsEntry("category", "work");
        assertThat(result.getMetadata()).containsEntry("priority", 1);
        assertThat(result.getUpdatedAt()).isAfter(Instant.parse("2025-01-01T00:00:00Z"));
        verify(conversationRepository).findById("conv-1");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void updateMetadata_notFound_shouldThrow() {
        when(conversationRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateMetadata("non-existent", Map.of("key", "value")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("non-existent");

        verify(conversationRepository).findById("non-existent");
        verify(conversationRepository, never()).save(any());
    }

    // ===== P7 - Paginazione e Ricerca =====

    @Test
    void listPaginated_shouldDelegateToRepository() {
        PageRequest pageRequest = new PageRequest(0, 10);
        PageResponse<ConversationContext> response = PageResponse.<ConversationContext>builder()
                .content(List.of(
                        ConversationContext.builder().id("1").title("Conv 1").build(),
                        ConversationContext.builder().id("2").title("Conv 2").build()
                ))
                .totalElements(2)
                .totalPages(1)
                .page(0)
                .size(10)
                .hasMore(false)
                .build();

        when(conversationRepository.findAllPaginated(any(PageRequest.class))).thenReturn(response);

        PageResponse<ConversationContext> result = service.listPaginated(pageRequest);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.isHasMore()).isFalse();
        verify(conversationRepository).findAllPaginated(pageRequest);
    }

    @Test
    void search_shouldDelegateToRepository() {
        PageRequest pageRequest = new PageRequest(0, 10);
        PageResponse<ConversationContext> response = PageResponse.<ConversationContext>builder()
                .content(List.of(
                        ConversationContext.builder().id("1").title("Java tutorial").build()
                ))
                .totalElements(1)
                .totalPages(1)
                .page(0)
                .size(10)
                .hasMore(false)
                .build();

        when(conversationRepository.searchByContent(eq("Java"), any(PageRequest.class))).thenReturn(response);

        PageResponse<ConversationContext> result = service.search("Java", pageRequest);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java tutorial");
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(conversationRepository).searchByContent("Java", pageRequest);
    }

    @Test
    void search_emptyResults_shouldReturnEmptyPage() {
        PageRequest pageRequest = new PageRequest(0, 10);
        PageResponse<ConversationContext> response = PageResponse.<ConversationContext>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .page(0)
                .size(10)
                .hasMore(false)
                .build();

        when(conversationRepository.searchByContent(eq("nonexistent"), any(PageRequest.class))).thenReturn(response);

        PageResponse<ConversationContext> result = service.search("nonexistent", pageRequest);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(conversationRepository).searchByContent("nonexistent", pageRequest);
    }
}
