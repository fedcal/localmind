package com.localmind.infrastructure.persistence.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.infrastructure.persistence.entity.ChatMessageEntity;
import com.localmind.infrastructure.persistence.entity.ConversationEntity;
import com.localmind.infrastructure.persistence.entity.ConversationTagEntity;
import com.localmind.infrastructure.persistence.repository.JpaConversationRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationRepositoryAdapterTest {

    @Mock
    private JpaConversationRepository jpaRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ConversationRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ConversationRepositoryAdapter(jpaRepository, objectMapper);
    }

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-11T10:00:00Z");
    private final Instant LATER = Instant.parse("2026-02-11T11:00:00Z");

    @Test
    void save_newConversation_shouldMapAllFieldsAndMessages() {
        ConversationContext domain = ConversationContext.builder()
                .id(null)
                .title("Nuova conversazione")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Ciao").build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Buongiorno!").build()
                ))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        ConversationEntity savedEntity = buildConversationEntity(TEST_UUID, "Nuova conversazione", NOW, NOW,
                List.of(
                        buildMessageEntity("USER", "Ciao"),
                        buildMessageEntity("ASSISTANT", "Buongiorno!")
                ));

        when(jpaRepository.save(any(ConversationEntity.class))).thenReturn(savedEntity);

        ConversationContext result = adapter.save(domain);

        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(jpaRepository).save(captor.capture());

        ConversationEntity captured = captor.getValue();
        assertThat(captured.getTitle()).isEqualTo("Nuova conversazione");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);
        assertThat(captured.getUpdatedAt()).isEqualTo(NOW);
        assertThat(captured.getMessages()).hasSize(2);
        assertThat(captured.getMessages().get(0).getRole()).isEqualTo("USER");
        assertThat(captured.getMessages().get(0).getContent()).isEqualTo("Ciao");
        assertThat(captured.getMessages().get(0).getConversation()).isSameAs(captured);
        assertThat(captured.getMessages().get(1).getRole()).isEqualTo("ASSISTANT");
        assertThat(captured.getMessages().get(1).getContent()).isEqualTo("Buongiorno!");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getTitle()).isEqualTo("Nuova conversazione");
        assertThat(result.getMessages()).hasSize(2);
        assertThat(result.getMessages().get(0).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(result.getMessages().get(0).getContent()).isEqualTo("Ciao");
        assertThat(result.getMessages().get(1).getRole()).isEqualTo(ChatMessage.Role.ASSISTANT);
        assertThat(result.getMessages().get(1).getContent()).isEqualTo("Buongiorno!");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
        assertThat(result.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_existingConversation_addsNewMessages() {
        ConversationEntity existingEntity = buildConversationEntity(TEST_UUID, "Titolo originale", NOW, NOW,
                new ArrayList<>(List.of(
                        buildMessageEntity("USER", "Primo messaggio"),
                        buildMessageEntity("ASSISTANT", "Prima risposta")
                )));
        existingEntity.getMessages().forEach(m -> m.setConversation(existingEntity));

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(existingEntity));

        ConversationContext domain = ConversationContext.builder()
                .id(TEST_UUID.toString())
                .title("Titolo aggiornato")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Primo messaggio").build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Prima risposta").build(),
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Secondo messaggio").build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Seconda risposta").build()
                ))
                .createdAt(NOW)
                .updatedAt(LATER)
                .build();

        ConversationEntity savedEntity = buildConversationEntity(TEST_UUID, "Titolo aggiornato", NOW, LATER,
                List.of(
                        buildMessageEntity("USER", "Primo messaggio"),
                        buildMessageEntity("ASSISTANT", "Prima risposta"),
                        buildMessageEntity("USER", "Secondo messaggio"),
                        buildMessageEntity("ASSISTANT", "Seconda risposta")
                ));

        when(jpaRepository.save(any(ConversationEntity.class))).thenReturn(savedEntity);

        ConversationContext result = adapter.save(domain);

        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(jpaRepository).save(captor.capture());

        ConversationEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getTitle()).isEqualTo("Titolo aggiornato");
        assertThat(captured.getUpdatedAt()).isEqualTo(LATER);
        assertThat(captured.getMessages()).hasSize(4);
        assertThat(captured.getMessages().get(2).getRole()).isEqualTo("USER");
        assertThat(captured.getMessages().get(2).getContent()).isEqualTo("Secondo messaggio");
        assertThat(captured.getMessages().get(2).getConversation()).isSameAs(captured);
        assertThat(captured.getMessages().get(3).getRole()).isEqualTo("ASSISTANT");
        assertThat(captured.getMessages().get(3).getContent()).isEqualTo("Seconda risposta");

        assertThat(result.getMessages()).hasSize(4);
        assertThat(result.getTitle()).isEqualTo("Titolo aggiornato");
    }

    @Test
    void save_existingId_notFoundInDb_shouldCreateNewEntity() {
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        ConversationContext domain = ConversationContext.builder()
                .id(TEST_UUID.toString())
                .title("Conversazione orfana")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Ciao").build()
                ))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        ConversationEntity savedEntity = buildConversationEntity(TEST_UUID, "Conversazione orfana", NOW, NOW,
                List.of(buildMessageEntity("USER", "Ciao")));

        when(jpaRepository.save(any(ConversationEntity.class))).thenReturn(savedEntity);

        ConversationContext result = adapter.save(domain);

        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(jpaRepository).save(captor.capture());

        ConversationEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getTitle()).isEqualTo("Conversazione orfana");
        assertThat(captured.getMessages()).hasSize(1);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
    }

    @Test
    void findById_found_shouldReturnMappedDomain() {
        ConversationEntity entity = buildConversationEntity(TEST_UUID, "Test conversazione", NOW, LATER,
                List.of(
                        buildMessageEntity("USER", "Domanda"),
                        buildMessageEntity("ASSISTANT", "Risposta")
                ));

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<ConversationContext> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        ConversationContext conversation = result.get();
        assertThat(conversation.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(conversation.getTitle()).isEqualTo("Test conversazione");
        assertThat(conversation.getCreatedAt()).isEqualTo(NOW);
        assertThat(conversation.getUpdatedAt()).isEqualTo(LATER);
        assertThat(conversation.getMessages()).hasSize(2);
        assertThat(conversation.getMessages().get(0).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(conversation.getMessages().get(0).getContent()).isEqualTo("Domanda");
        assertThat(conversation.getMessages().get(1).getRole()).isEqualTo(ChatMessage.Role.ASSISTANT);
        assertThat(conversation.getMessages().get(1).getContent()).isEqualTo("Risposta");

        verify(jpaRepository).findById(TEST_UUID);
    }

    @Test
    void findById_notFound_shouldReturnEmpty() {
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        Optional<ConversationContext> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isEmpty();
        verify(jpaRepository).findById(TEST_UUID);
    }

    @Test
    void findAllOrderByUpdatedAtDesc_shouldReturnMappedList() {
        UUID uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        ConversationEntity entity1 = buildConversationEntity(uuid1, "Conversazione recente", LATER, LATER,
                List.of(buildMessageEntity("USER", "Msg 1")));
        ConversationEntity entity2 = buildConversationEntity(uuid2, "Conversazione vecchia", NOW, NOW,
                List.of(buildMessageEntity("SYSTEM", "System prompt")));

        when(jpaRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of(entity1, entity2));

        List<ConversationContext> result = adapter.findAllOrderByUpdatedAtDesc();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(uuid1.toString());
        assertThat(result.get(0).getTitle()).isEqualTo("Conversazione recente");
        assertThat(result.get(0).getMessages()).hasSize(1);
        assertThat(result.get(0).getMessages().get(0).getRole()).isEqualTo(ChatMessage.Role.USER);

        assertThat(result.get(1).getId()).isEqualTo(uuid2.toString());
        assertThat(result.get(1).getTitle()).isEqualTo("Conversazione vecchia");
        assertThat(result.get(1).getMessages()).hasSize(1);
        assertThat(result.get(1).getMessages().get(0).getRole()).isEqualTo(ChatMessage.Role.SYSTEM);

        verify(jpaRepository).findAllByOrderByUpdatedAtDesc();
    }

    @Test
    void findAllOrderByUpdatedAtDesc_emptyList_shouldReturnEmptyList() {
        when(jpaRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of());

        List<ConversationContext> result = adapter.findAllOrderByUpdatedAtDesc();

        assertThat(result).isEmpty();
        verify(jpaRepository).findAllByOrderByUpdatedAtDesc();
    }

    @Test
    void deleteById_shouldDelegateToJpaRepository() {
        adapter.deleteById(TEST_UUID.toString());

        verify(jpaRepository).deleteById(TEST_UUID);
    }

    @Test
    void save_newConversation_withSystemPrompt_shouldMapSystemPrompt() {
        ConversationContext domain = ConversationContext.builder()
                .id(null)
                .title("Conversazione con system prompt")
                .systemPrompt("Custom system prompt")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Ciao").build()
                ))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        ConversationEntity savedEntity = buildConversationEntity(TEST_UUID, "Conversazione con system prompt", NOW, NOW,
                List.of(buildMessageEntity("USER", "Ciao")), "Custom system prompt");

        when(jpaRepository.save(any(ConversationEntity.class))).thenReturn(savedEntity);

        ConversationContext result = adapter.save(domain);

        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(jpaRepository).save(captor.capture());

        ConversationEntity captured = captor.getValue();
        assertThat(captured.getSystemPrompt()).isEqualTo("Custom system prompt");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getSystemPrompt()).isEqualTo("Custom system prompt");
    }

    @Test
    void save_existingConversation_shouldUpdateSystemPrompt() {
        ConversationEntity existingEntity = buildConversationEntity(TEST_UUID, "Titolo originale", NOW, NOW,
                new ArrayList<>(List.of(buildMessageEntity("USER", "Primo messaggio"))), null);
        existingEntity.getMessages().forEach(m -> m.setConversation(existingEntity));

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(existingEntity));

        ConversationContext domain = ConversationContext.builder()
                .id(TEST_UUID.toString())
                .title("Titolo originale")
                .systemPrompt("Updated prompt")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Primo messaggio").build()
                ))
                .createdAt(NOW)
                .updatedAt(LATER)
                .build();

        ConversationEntity savedEntity = buildConversationEntity(TEST_UUID, "Titolo originale", NOW, LATER,
                List.of(buildMessageEntity("USER", "Primo messaggio")), "Updated prompt");

        when(jpaRepository.save(any(ConversationEntity.class))).thenReturn(savedEntity);

        ConversationContext result = adapter.save(domain);

        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(jpaRepository).save(captor.capture());

        ConversationEntity captured = captor.getValue();
        assertThat(captured.getSystemPrompt()).isEqualTo("Updated prompt");

        assertThat(result.getSystemPrompt()).isEqualTo("Updated prompt");
    }

    @Test
    void findById_withSystemPrompt_shouldReturnMappedDomain() {
        ConversationEntity entity = buildConversationEntity(TEST_UUID, "Test conversazione", NOW, LATER,
                List.of(buildMessageEntity("USER", "Domanda")), "System prompt personalizzato");

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<ConversationContext> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        ConversationContext conversation = result.get();
        assertThat(conversation.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(conversation.getSystemPrompt()).isEqualTo("System prompt personalizzato");
        assertThat(conversation.getTitle()).isEqualTo("Test conversazione");
        assertThat(conversation.getMessages()).hasSize(1);

        verify(jpaRepository).findById(TEST_UUID);
    }

    // --- P2: Context Sharing - Metadata Tests ---

    @Test
    void save_withMetadata_serializesMetadataToJson() {
        Map<String, Object> metadata = Map.of(
                "toolCallId", "call_123",
                "toolName", "get_weather",
                "resultStatus", "success"
        );

        ConversationContext domain = ConversationContext.builder()
                .id(null)
                .title("Conversazione con tool")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Che tempo fa?").build(),
                        ChatMessage.builder().role(ChatMessage.Role.TOOL).content("{\"temp\":20}").metadata(metadata).build()
                ))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        ConversationEntity savedEntity = buildConversationEntity(TEST_UUID, "Conversazione con tool", NOW, NOW,
                List.of(
                        buildMessageEntity("USER", "Che tempo fa?"),
                        buildMessageEntity("TOOL", "{\"temp\":20}")
                ));

        when(jpaRepository.save(any(ConversationEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(jpaRepository).save(captor.capture());

        ConversationEntity captured = captor.getValue();
        assertThat(captured.getMessages()).hasSize(2);

        ChatMessageEntity toolMessage = captured.getMessages().get(1);
        assertThat(toolMessage.getRole()).isEqualTo("TOOL");
        assertThat(toolMessage.getMetadata()).isNotNull();

        // Verify the metadata is valid JSON containing the expected keys
        String metadataJson = toolMessage.getMetadata();
        assertThat(metadataJson).contains("\"toolCallId\"");
        assertThat(metadataJson).contains("\"call_123\"");
        assertThat(metadataJson).contains("\"toolName\"");
        assertThat(metadataJson).contains("\"get_weather\"");
        assertThat(metadataJson).contains("\"resultStatus\"");
        assertThat(metadataJson).contains("\"success\"");
    }

    @Test
    void findById_withMetadata_deserializesJsonToMap() {
        ChatMessageEntity toolMessageEntity = ChatMessageEntity.builder()
                .id(UUID.randomUUID())
                .role("TOOL")
                .content("{\"temp\":20}")
                .metadata("{\"toolCallId\":\"call_456\",\"toolName\":\"get_weather\"}")
                .createdAt(NOW)
                .build();

        ConversationEntity entity = buildConversationEntity(TEST_UUID, "Con metadata", NOW, LATER,
                List.of(
                        buildMessageEntity("USER", "Domanda"),
                        toolMessageEntity
                ));

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<ConversationContext> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        ConversationContext conversation = result.get();
        assertThat(conversation.getMessages()).hasSize(2);

        ChatMessage toolMsg = conversation.getMessages().get(1);
        assertThat(toolMsg.getRole()).isEqualTo(ChatMessage.Role.TOOL);
        assertThat(toolMsg.getMetadata()).isNotNull();
        assertThat(toolMsg.getMetadata()).containsEntry("toolCallId", "call_456");
        assertThat(toolMsg.getMetadata()).containsEntry("toolName", "get_weather");
    }

    @Test
    void save_withNullMetadata_setsMetadataToNull() {
        ConversationContext domain = ConversationContext.builder()
                .id(null)
                .title("Senza metadata")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Ciao").metadata(null).build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Buongiorno!").metadata(null).build()
                ))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        ConversationEntity savedEntity = buildConversationEntity(TEST_UUID, "Senza metadata", NOW, NOW,
                List.of(
                        buildMessageEntity("USER", "Ciao"),
                        buildMessageEntity("ASSISTANT", "Buongiorno!")
                ));

        when(jpaRepository.save(any(ConversationEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(jpaRepository).save(captor.capture());

        ConversationEntity captured = captor.getValue();
        assertThat(captured.getMessages()).hasSize(2);
        assertThat(captured.getMessages().get(0).getMetadata()).isNull();
        assertThat(captured.getMessages().get(1).getMetadata()).isNull();
    }

    // --- P5: Memory Window / Context Limit ---

    @Test
    void save_shouldMapMaxContextMessages() {
        ConversationContext domain = ConversationContext.builder()
                .id(null)
                .title("Conversazione con context limit")
                .maxContextMessages(25)
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Ciao").build()
                ))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        ConversationEntity savedEntity = buildConversationEntity(TEST_UUID, "Conversazione con context limit", NOW, NOW,
                List.of(buildMessageEntity("USER", "Ciao")), null, 25);

        when(jpaRepository.save(any(ConversationEntity.class))).thenReturn(savedEntity);

        ConversationContext result = adapter.save(domain);

        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(jpaRepository).save(captor.capture());

        ConversationEntity captured = captor.getValue();
        assertThat(captured.getMaxContextMessages()).isEqualTo(25);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getMaxContextMessages()).isEqualTo(25);
    }

    @Test
    void toDomain_shouldMapMaxContextMessages() {
        ConversationEntity entity = buildConversationEntity(TEST_UUID, "Test con limit", NOW, LATER,
                List.of(buildMessageEntity("USER", "Domanda")), null, 15);

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<ConversationContext> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        ConversationContext conversation = result.get();
        assertThat(conversation.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(conversation.getMaxContextMessages()).isEqualTo(15);
        assertThat(conversation.getTitle()).isEqualTo("Test con limit");
        assertThat(conversation.getMessages()).hasSize(1);

        verify(jpaRepository).findById(TEST_UUID);
    }

    // --- P6: Metadata e Tag sulle Conversazioni ---

    @Test
    void save_shouldMapTags() {
        ConversationContext domain = ConversationContext.builder()
                .id(null)
                .title("Conversazione con tag")
                .tags(new HashSet<>(Set.of("important", "work")))
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Ciao").build()
                ))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        ConversationEntity savedEntity = buildConversationEntity(TEST_UUID, "Conversazione con tag", NOW, NOW,
                List.of(buildMessageEntity("USER", "Ciao")));

        when(jpaRepository.save(any(ConversationEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(jpaRepository).save(captor.capture());

        ConversationEntity captured = captor.getValue();
        assertThat(captured.getTags()).isNotNull();
        assertThat(captured.getTags()).hasSize(2);

        Set<String> tagValues = captured.getTags().stream()
                .map(ConversationTagEntity::getTag)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(tagValues).containsExactlyInAnyOrder("important", "work");

        captured.getTags().forEach(tagEntity ->
                assertThat(tagEntity.getConversation()).isSameAs(captured));
    }

    @Test
    void toDomain_shouldMapTags() {
        ConversationEntity entity = buildConversationEntity(TEST_UUID, "Con tag", NOW, LATER,
                List.of(buildMessageEntity("USER", "Domanda")));

        ConversationTagEntity tagEntity1 = ConversationTagEntity.builder()
                .id(UUID.randomUUID())
                .tag("important")
                .conversation(entity)
                .build();
        ConversationTagEntity tagEntity2 = ConversationTagEntity.builder()
                .id(UUID.randomUUID())
                .tag("work")
                .conversation(entity)
                .build();
        entity.setTags(new ArrayList<>(List.of(tagEntity1, tagEntity2)));

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<ConversationContext> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        ConversationContext conversation = result.get();
        assertThat(conversation.getTags()).isNotNull();
        assertThat(conversation.getTags()).hasSize(2);
        assertThat(conversation.getTags()).containsExactlyInAnyOrder("important", "work");

        verify(jpaRepository).findById(TEST_UUID);
    }

    @Test
    void save_shouldMapConversationMetadata() {
        Map<String, Object> metadata = Map.of("category", "project", "priority", 1);

        ConversationContext domain = ConversationContext.builder()
                .id(null)
                .title("Conversazione con metadata")
                .metadata(metadata)
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Ciao").build()
                ))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        ConversationEntity savedEntity = buildConversationEntity(TEST_UUID, "Conversazione con metadata", NOW, NOW,
                List.of(buildMessageEntity("USER", "Ciao")));

        when(jpaRepository.save(any(ConversationEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(jpaRepository).save(captor.capture());

        ConversationEntity captured = captor.getValue();
        assertThat(captured.getMetadata()).isNotNull();
        assertThat(captured.getMetadata()).contains("\"category\"");
        assertThat(captured.getMetadata()).contains("\"project\"");
        assertThat(captured.getMetadata()).contains("\"priority\"");
    }

    @Test
    void toDomain_shouldMapConversationMetadata() {
        ConversationEntity entity = buildConversationEntity(TEST_UUID, "Con metadata", NOW, LATER,
                List.of(buildMessageEntity("USER", "Domanda")));
        entity.setMetadata("{\"category\":\"project\",\"priority\":1}");

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<ConversationContext> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        ConversationContext conversation = result.get();
        assertThat(conversation.getMetadata()).isNotNull();
        assertThat(conversation.getMetadata()).containsEntry("category", "project");
        assertThat(conversation.getMetadata()).containsEntry("priority", 1);

        verify(jpaRepository).findById(TEST_UUID);
    }

    @Test
    void findByTag_shouldDelegateToJpaRepository() {
        ConversationEntity entity = buildConversationEntity(TEST_UUID, "Tagged conv", NOW, LATER,
                List.of(buildMessageEntity("USER", "Domanda")));
        ConversationTagEntity tagEntity = ConversationTagEntity.builder()
                .id(UUID.randomUUID())
                .tag("important")
                .conversation(entity)
                .build();
        entity.setTags(new ArrayList<>(List.of(tagEntity)));

        when(jpaRepository.findByTag("important")).thenReturn(List.of(entity));

        List<ConversationContext> result = adapter.findByTag("important");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getTitle()).isEqualTo("Tagged conv");
        verify(jpaRepository).findByTag("important");
    }

    // --- P7: Paginazione e Ricerca ---

    @Test
    void findAllPaginated_shouldConvertSpringPageToPageResponse() {
        UUID uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        ConversationEntity entity1 = buildConversationEntity(uuid1, "Conv 1", NOW, LATER,
                List.of(buildMessageEntity("USER", "Msg 1")));
        ConversationEntity entity2 = buildConversationEntity(uuid2, "Conv 2", NOW, NOW,
                List.of(buildMessageEntity("USER", "Msg 2")));

        org.springframework.data.domain.Page<ConversationEntity> springPage =
                new org.springframework.data.domain.PageImpl<>(
                        List.of(entity1, entity2),
                        org.springframework.data.domain.PageRequest.of(0, 10),
                        25
                );

        when(jpaRepository.findAllByOrderByUpdatedAtDesc(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(springPage);

        com.localmind.domain.common.model.PageRequest pageRequest = new com.localmind.domain.common.model.PageRequest(0, 10);
        com.localmind.domain.common.model.PageResponse<ConversationContext> result = adapter.findAllPaginated(pageRequest);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(uuid1.toString());
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Conv 1");
        assertThat(result.getContent().get(1).getId()).isEqualTo(uuid2.toString());
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.isHasMore()).isTrue();

        verify(jpaRepository).findAllByOrderByUpdatedAtDesc(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void findAllPaginated_emptyPage_shouldReturnEmptyResponse() {
        org.springframework.data.domain.Page<ConversationEntity> emptyPage =
                new org.springframework.data.domain.PageImpl<>(
                        List.of(),
                        org.springframework.data.domain.PageRequest.of(0, 10),
                        0
                );

        when(jpaRepository.findAllByOrderByUpdatedAtDesc(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(emptyPage);

        com.localmind.domain.common.model.PageRequest pageRequest = new com.localmind.domain.common.model.PageRequest(0, 10);
        com.localmind.domain.common.model.PageResponse<ConversationContext> result = adapter.findAllPaginated(pageRequest);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.isHasMore()).isFalse();
    }

    @Test
    void searchByContent_shouldConvertSpringPageToPageResponse() {
        UUID uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");

        ConversationEntity entity1 = buildConversationEntity(uuid1, "Java tutorial", NOW, LATER,
                List.of(buildMessageEntity("USER", "Tell me about Java")));

        org.springframework.data.domain.Page<ConversationEntity> springPage =
                new org.springframework.data.domain.PageImpl<>(
                        List.of(entity1),
                        org.springframework.data.domain.PageRequest.of(0, 20),
                        1
                );

        when(jpaRepository.searchByContent(eq("Java"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(springPage);

        com.localmind.domain.common.model.PageRequest pageRequest = new com.localmind.domain.common.model.PageRequest(0, 20);
        com.localmind.domain.common.model.PageResponse<ConversationContext> result = adapter.searchByContent("Java", pageRequest);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java tutorial");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isHasMore()).isFalse();

        verify(jpaRepository).searchByContent(eq("Java"), any(org.springframework.data.domain.Pageable.class));
    }

    // --- Helper methods ---

    private ConversationEntity buildConversationEntity(UUID id, String title, Instant createdAt, Instant updatedAt,
                                                        List<ChatMessageEntity> messages) {
        return buildConversationEntity(id, title, createdAt, updatedAt, messages, null, null);
    }

    private ConversationEntity buildConversationEntity(UUID id, String title, Instant createdAt, Instant updatedAt,
                                                        List<ChatMessageEntity> messages, String systemPrompt) {
        return buildConversationEntity(id, title, createdAt, updatedAt, messages, systemPrompt, null);
    }

    private ConversationEntity buildConversationEntity(UUID id, String title, Instant createdAt, Instant updatedAt,
                                                        List<ChatMessageEntity> messages, String systemPrompt,
                                                        Integer maxContextMessages) {
        return ConversationEntity.builder()
                .id(id)
                .title(title)
                .systemPrompt(systemPrompt)
                .maxContextMessages(maxContextMessages)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .messages(new ArrayList<>(messages))
                .build();
    }

    private ChatMessageEntity buildMessageEntity(String role, String content) {
        return ChatMessageEntity.builder()
                .id(UUID.randomUUID())
                .role(role)
                .content(content)
                .createdAt(NOW)
                .build();
    }
}
