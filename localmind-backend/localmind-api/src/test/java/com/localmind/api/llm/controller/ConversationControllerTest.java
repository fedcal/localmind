package com.localmind.api.llm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.llm.dto.AddTagRequestDto;
import com.localmind.api.llm.dto.AddToolResultRequestDto;
import com.localmind.api.llm.dto.RenameConversationRequestDto;
import com.localmind.api.llm.dto.UpdateContextWindowRequestDto;
import com.localmind.api.llm.dto.UpdateSystemPromptRequestDto;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.common.model.PageRequest;
import com.localmind.domain.common.model.PageResponse;
import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.model.ConversationSummary;
import com.localmind.domain.llm.port.in.ConversationExportUseCase;
import com.localmind.domain.llm.port.in.ConversationImportUseCase;
import com.localmind.domain.llm.port.in.ConversationUseCase;
import com.localmind.domain.llm.port.out.ConversationReadRepository;
import com.localmind.domain.llm.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class ConversationControllerTest {

    private MockMvc mockMvc;
    private ConversationUseCase conversationUseCase;
    private ConversationService conversationService;
    private ConversationExportUseCase conversationExportUseCase;
    private ConversationImportUseCase conversationImportUseCase;
    private ConversationReadRepository conversationReadRepository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        conversationUseCase = mock(ConversationUseCase.class);
        conversationService = mock(ConversationService.class);
        conversationExportUseCase = mock(ConversationExportUseCase.class);
        conversationImportUseCase = mock(ConversationImportUseCase.class);
        conversationReadRepository = mock(ConversationReadRepository.class);
        ConversationController controller = new ConversationController(
                conversationUseCase, conversationService,
                conversationExportUseCase, conversationImportUseCase,
                Optional.of(conversationReadRepository));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void listAll_returnsConversations() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext conv1 = ConversationContext.builder()
                .id("conv-1")
                .title("First conversation")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Hi!").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        ConversationContext conv2 = ConversationContext.builder()
                .id("conv-2")
                .title("Second conversation")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Question").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.listAll()).thenReturn(List.of(conv1, conv2));

        mockMvc.perform(get("/api/v1/conversations")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("conv-1"))
                .andExpect(jsonPath("$[0].title").value("First conversation"))
                .andExpect(jsonPath("$[0].messageCount").value(2))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists())
                .andExpect(jsonPath("$[1].id").value("conv-2"))
                .andExpect(jsonPath("$[1].title").value("Second conversation"))
                .andExpect(jsonPath("$[1].messageCount").value(1));

        verify(conversationUseCase).listAll();
    }

    @Test
    void getById_returnsConversation() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("My conversation")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("What is AI?").build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("AI is...").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.getById("conv-1")).thenReturn(conversation);

        mockMvc.perform(get("/api/v1/conversations/conv-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.title").value("My conversation"))
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[0].role").value("USER"))
                .andExpect(jsonPath("$.messages[0].content").value("What is AI?"))
                .andExpect(jsonPath("$.messages[1].role").value("ASSISTANT"))
                .andExpect(jsonPath("$.messages[1].content").value("AI is..."))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(conversationUseCase).getById("conv-1");
    }

    @Test
    void getById_returnsConversationWithSystemPrompt() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("My conversation")
                .systemPrompt("You are a helpful assistant")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.getById("conv-1")).thenReturn(conversation);

        mockMvc.perform(get("/api/v1/conversations/conv-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.title").value("My conversation"))
                .andExpect(jsonPath("$.systemPrompt").value("You are a helpful assistant"))
                .andExpect(jsonPath("$.messages.length()").value(1));

        verify(conversationUseCase).getById("conv-1");
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(conversationUseCase.getById("nonexistent"))
                .thenThrow(new ResourceNotFoundException("Conversation not found: nonexistent"));

        mockMvc.perform(get("/api/v1/conversations/nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Conversation not found: nonexistent"));
    }

    @Test
    void rename_updatesTitle() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext updated = ConversationContext.builder()
                .id("conv-1")
                .title("New title")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.rename("conv-1", "New title")).thenReturn(updated);

        RenameConversationRequestDto request = RenameConversationRequestDto.builder()
                .title("New title")
                .build();

        mockMvc.perform(patch("/api/v1/conversations/conv-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.messages.length()").value(1))
                .andExpect(jsonPath("$.messages[0].role").value("USER"))
                .andExpect(jsonPath("$.messages[0].content").value("Hello"));

        verify(conversationUseCase).rename("conv-1", "New title");
    }

    @Test
    void rename_blankTitle_returns400() throws Exception {
        RenameConversationRequestDto request = RenameConversationRequestDto.builder()
                .title("")
                .build();

        mockMvc.perform(patch("/api/v1/conversations/conv-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(conversationUseCase);
    }

    @Test
    void rename_nullTitle_returns400() throws Exception {
        RenameConversationRequestDto request = RenameConversationRequestDto.builder()
                .build();

        mockMvc.perform(patch("/api/v1/conversations/conv-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(conversationUseCase);
    }

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(conversationUseCase).deleteById("conv-1");

        mockMvc.perform(delete("/api/v1/conversations/conv-1"))
                .andExpect(status().isNoContent());

        verify(conversationUseCase).deleteById("conv-1");
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Conversation not found: nonexistent"))
                .when(conversationUseCase).deleteById("nonexistent");

        mockMvc.perform(delete("/api/v1/conversations/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Conversation not found: nonexistent"));
    }

    @Test
    void updateSystemPrompt_updatesPrompt() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext updated = ConversationContext.builder()
                .id("conv-1")
                .title("My conversation")
                .systemPrompt("You are helpful")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.updateSystemPrompt("conv-1", "You are helpful")).thenReturn(updated);

        UpdateSystemPromptRequestDto request = UpdateSystemPromptRequestDto.builder()
                .systemPrompt("You are helpful")
                .build();

        mockMvc.perform(patch("/api/v1/conversations/conv-1/system-prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.systemPrompt").value("You are helpful"))
                .andExpect(jsonPath("$.messages.length()").value(1));

        verify(conversationUseCase).updateSystemPrompt("conv-1", "You are helpful");
    }

    @Test
    void updateSystemPrompt_clearPrompt() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext updated = ConversationContext.builder()
                .id("conv-1")
                .title("My conversation")
                .systemPrompt(null)
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.updateSystemPrompt("conv-1", null)).thenReturn(updated);

        UpdateSystemPromptRequestDto request = UpdateSystemPromptRequestDto.builder()
                .systemPrompt(null)
                .build();

        mockMvc.perform(patch("/api/v1/conversations/conv-1/system-prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.systemPrompt").isEmpty())
                .andExpect(jsonPath("$.messages.length()").value(1));

        verify(conversationUseCase).updateSystemPrompt("conv-1", null);
    }

    @Test
    void addToolResult_addsToolMessageToConversation() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");
        Map<String, Object> metadata = Map.of("toolName", "calculator", "executionTime", 150);

        ConversationContext updated = ConversationContext.builder()
                .id("conv-1")
                .title("Tool conversation")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Calculate 2+2").build(),
                        ChatMessage.builder().role(ChatMessage.Role.TOOL).content("4")
                                .metadata(metadata).build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationService.addToolMessage(
                eq("conv-1"), eq("calculator"), eq("4"), eq(Map.of("executionTime", 150))))
                .thenReturn(updated);

        AddToolResultRequestDto request = AddToolResultRequestDto.builder()
                .toolName("calculator")
                .content("4")
                .metadata(Map.of("executionTime", 150))
                .build();

        mockMvc.perform(post("/api/v1/conversations/conv-1/tool-result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[1].role").value("TOOL"))
                .andExpect(jsonPath("$.messages[1].content").value("4"))
                .andExpect(jsonPath("$.messages[1].metadata.toolName").value("calculator"));

        verify(conversationService).addToolMessage(
                eq("conv-1"), eq("calculator"), eq("4"), eq(Map.of("executionTime", 150)));
        verifyNoInteractions(conversationUseCase);
    }

    @Test
    void addToolResult_withoutMetadata_succeeds() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext updated = ConversationContext.builder()
                .id("conv-1")
                .title("Tool conversation")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Search something").build(),
                        ChatMessage.builder().role(ChatMessage.Role.TOOL).content("result data")
                                .metadata(Map.of("toolName", "search")).build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationService.addToolMessage(
                eq("conv-1"), eq("search"), eq("result data"), isNull()))
                .thenReturn(updated);

        AddToolResultRequestDto request = AddToolResultRequestDto.builder()
                .toolName("search")
                .content("result data")
                .build();

        mockMvc.perform(post("/api/v1/conversations/conv-1/tool-result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[1].role").value("TOOL"))
                .andExpect(jsonPath("$.messages[1].content").value("result data"));

        verify(conversationService).addToolMessage(
                eq("conv-1"), eq("search"), eq("result data"), isNull());
    }

    @Test
    void addToolResult_missingToolName_returns400() throws Exception {
        AddToolResultRequestDto request = AddToolResultRequestDto.builder()
                .content("some content")
                .build();

        mockMvc.perform(post("/api/v1/conversations/conv-1/tool-result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(conversationService);
        verifyNoInteractions(conversationUseCase);
    }

    @Test
    void getById_returnsMessagesWithMetadata() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");
        Map<String, Object> toolMetadata = Map.of("toolName", "web_search", "resultCount", 5);

        ConversationContext conversation = ConversationContext.builder()
                .id("conv-1")
                .title("Conversation with tools")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Search for AI").build(),
                        ChatMessage.builder().role(ChatMessage.Role.TOOL).content("Found 5 results")
                                .metadata(toolMetadata).build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Here are the results").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.getById("conv-1")).thenReturn(conversation);

        mockMvc.perform(get("/api/v1/conversations/conv-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.messages.length()").value(3))
                .andExpect(jsonPath("$.messages[0].role").value("USER"))
                .andExpect(jsonPath("$.messages[0].metadata").doesNotExist())
                .andExpect(jsonPath("$.messages[1].role").value("TOOL"))
                .andExpect(jsonPath("$.messages[1].content").value("Found 5 results"))
                .andExpect(jsonPath("$.messages[1].metadata.toolName").value("web_search"))
                .andExpect(jsonPath("$.messages[1].metadata.resultCount").value(5))
                .andExpect(jsonPath("$.messages[2].role").value("ASSISTANT"))
                .andExpect(jsonPath("$.messages[2].metadata").doesNotExist());

        verify(conversationUseCase).getById("conv-1");
    }

    // ===== P5 - Memory Window / Context Limit =====

    @Test
    void updateContextWindow_shouldReturn200() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext updated = ConversationContext.builder()
                .id("conv-1")
                .title("My conversation")
                .maxContextMessages(20)
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.updateMaxContextMessages("conv-1", 20)).thenReturn(updated);

        UpdateContextWindowRequestDto request = UpdateContextWindowRequestDto.builder()
                .maxContextMessages(20)
                .build();

        mockMvc.perform(patch("/api/v1/conversations/conv-1/context-window")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.maxContextMessages").value(20))
                .andExpect(jsonPath("$.messages.length()").value(1));

        verify(conversationUseCase).updateMaxContextMessages("conv-1", 20);
    }

    @Test
    void updateContextWindow_withNull_shouldReturn200() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext updated = ConversationContext.builder()
                .id("conv-1")
                .title("My conversation")
                .maxContextMessages(null)
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.updateMaxContextMessages("conv-1", null)).thenReturn(updated);

        UpdateContextWindowRequestDto request = UpdateContextWindowRequestDto.builder()
                .maxContextMessages(null)
                .build();

        mockMvc.perform(patch("/api/v1/conversations/conv-1/context-window")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.maxContextMessages").isEmpty())
                .andExpect(jsonPath("$.messages.length()").value(1));

        verify(conversationUseCase).updateMaxContextMessages("conv-1", null);
    }

    // ===== P6 - Metadata e Tag sulle Conversazioni =====

    @Test
    void addTag_shouldReturn200() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext updated = ConversationContext.builder()
                .id("conv-1")
                .title("My conversation")
                .tags(new HashSet<>(Set.of("important")))
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.addTag("conv-1", "important")).thenReturn(updated);

        AddTagRequestDto request = AddTagRequestDto.builder()
                .tag("important")
                .build();

        mockMvc.perform(post("/api/v1/conversations/conv-1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags[0]").value("important"));

        verify(conversationUseCase).addTag("conv-1", "important");
    }

    @Test
    void addTag_withBlankTag_shouldReturn400() throws Exception {
        AddTagRequestDto request = AddTagRequestDto.builder()
                .tag("")
                .build();

        mockMvc.perform(post("/api/v1/conversations/conv-1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(conversationUseCase);
    }

    @Test
    void removeTag_shouldReturn200() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext updated = ConversationContext.builder()
                .id("conv-1")
                .title("My conversation")
                .tags(new HashSet<>())
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.removeTag("conv-1", "important")).thenReturn(updated);

        mockMvc.perform(delete("/api/v1/conversations/conv-1/tags/important"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-1"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags").isEmpty());

        verify(conversationUseCase).removeTag("conv-1", "important");
    }

    @Test
    void listAll_withTagFilter_shouldFilterByTag() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext conv1 = ConversationContext.builder()
                .id("conv-1")
                .title("Tagged conversation")
                .tags(new HashSet<>(Set.of("important")))
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.findByTag("important")).thenReturn(List.of(conv1));

        mockMvc.perform(get("/api/v1/conversations")
                        .param("tag", "important")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("conv-1"))
                .andExpect(jsonPath("$[0].title").value("Tagged conversation"));

        verify(conversationUseCase).findByTag("important");
        verify(conversationUseCase, never()).listAll();
    }

    @Test
    void listAll_withTagFilter_returnsTagsAndMetadata() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationContext conv1 = ConversationContext.builder()
                .id("conv-1")
                .title("Tagged conversation")
                .tags(new HashSet<>(Set.of("important", "work")))
                .metadata(Map.of("category", "project", "priority", 1))
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationUseCase.findByTag("important")).thenReturn(List.of(conv1));

        mockMvc.perform(get("/api/v1/conversations")
                        .param("tag", "important")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("conv-1"))
                .andExpect(jsonPath("$[0].tags").isArray())
                .andExpect(jsonPath("$[0].tags.length()").value(2))
                .andExpect(jsonPath("$[0].metadata").isMap())
                .andExpect(jsonPath("$[0].metadata.category").value("project"))
                .andExpect(jsonPath("$[0].metadata.priority").value(1));

        verify(conversationUseCase).findByTag("important");
    }

    // ===== P7 - Paginazione e Ricerca =====

    @Test
    void listPaginated_shouldReturnPagedSummaries() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        PageResponse<ConversationContext> response = PageResponse.<ConversationContext>builder()
                .content(List.of(
                        ConversationContext.builder()
                                .id("conv-1")
                                .title("First conversation")
                                .messages(List.of(
                                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()
                                ))
                                .createdAt(now)
                                .updatedAt(now)
                                .build(),
                        ConversationContext.builder()
                                .id("conv-2")
                                .title("Second conversation")
                                .messages(List.of(
                                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hi").build(),
                                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Hello!").build()
                                ))
                                .createdAt(now)
                                .updatedAt(now)
                                .build()
                ))
                .totalElements(50)
                .totalPages(3)
                .page(0)
                .size(20)
                .hasMore(true)
                .build();

        when(conversationUseCase.listPaginated(any(PageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/conversations/paginated")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value("conv-1"))
                .andExpect(jsonPath("$.content[0].title").value("First conversation"))
                .andExpect(jsonPath("$.content[0].messageCount").value(1))
                .andExpect(jsonPath("$.content[1].id").value("conv-2"))
                .andExpect(jsonPath("$.content[1].messageCount").value(2))
                .andExpect(jsonPath("$.totalElements").value(50))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.hasMore").value(true));

        verify(conversationUseCase).listPaginated(any(PageRequest.class));
    }

    @Test
    void listPaginated_withDefaultParams_shouldUseDefaults() throws Exception {
        PageResponse<ConversationContext> response = PageResponse.<ConversationContext>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .page(0)
                .size(20)
                .hasMore(false)
                .build();

        when(conversationUseCase.listPaginated(any(PageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/conversations/paginated")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(conversationUseCase).listPaginated(any(PageRequest.class));
    }

    @Test
    void search_shouldReturnMatchingConversations() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        PageResponse<ConversationContext> response = PageResponse.<ConversationContext>builder()
                .content(List.of(
                        ConversationContext.builder()
                                .id("conv-1")
                                .title("Java tutorial")
                                .messages(List.of(
                                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Tell me about Java").build()
                                ))
                                .createdAt(now)
                                .updatedAt(now)
                                .build()
                ))
                .totalElements(1)
                .totalPages(1)
                .page(0)
                .size(20)
                .hasMore(false)
                .build();

        when(conversationUseCase.search(eq("Java"), any(PageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/conversations/search")
                        .param("query", "Java")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value("conv-1"))
                .andExpect(jsonPath("$.content[0].title").value("Java tutorial"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.hasMore").value(false));

        verify(conversationUseCase).search(eq("Java"), any(PageRequest.class));
    }

    @Test
    void search_noResults_shouldReturnEmptyPage() throws Exception {
        PageResponse<ConversationContext> response = PageResponse.<ConversationContext>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .page(0)
                .size(20)
                .hasMore(false)
                .build();

        when(conversationUseCase.search(eq("nonexistent"), any(PageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/conversations/search")
                        .param("query", "nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(conversationUseCase).search(eq("nonexistent"), any(PageRequest.class));
    }

    // ===== CQRS Read Model Tests =====

    @Test
    void getSummaries_returnsList() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationSummary summary1 = ConversationSummary.builder()
                .id("cs-1")
                .title("First summary")
                .messageCount(5)
                .lastMessagePreview("Last message preview")
                .tags(List.of("important"))
                .createdAt(now)
                .updatedAt(now)
                .build();

        ConversationSummary summary2 = ConversationSummary.builder()
                .id("cs-2")
                .title("Second summary")
                .messageCount(3)
                .tags(List.of())
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationReadRepository.findRecentSummaries(20))
                .thenReturn(List.of(summary1, summary2));

        mockMvc.perform(get("/api/v1/conversations/summaries")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("cs-1"))
                .andExpect(jsonPath("$[0].title").value("First summary"))
                .andExpect(jsonPath("$[0].messageCount").value(5))
                .andExpect(jsonPath("$[0].lastMessagePreview").value("Last message preview"))
                .andExpect(jsonPath("$[0].tags[0]").value("important"))
                .andExpect(jsonPath("$[1].id").value("cs-2"))
                .andExpect(jsonPath("$[1].title").value("Second summary"))
                .andExpect(jsonPath("$[1].messageCount").value(3));

        verify(conversationReadRepository).findRecentSummaries(20);
    }

    @Test
    void searchSummaries_returnsResults() throws Exception {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");

        ConversationSummary summary = ConversationSummary.builder()
                .id("cs-1")
                .title("Java tutorial")
                .messageCount(10)
                .lastMessagePreview("About Java streams")
                .tags(List.of("programming"))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(conversationReadRepository.searchSummaries("Java"))
                .thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/conversations/summaries/search")
                        .param("query", "Java")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("cs-1"))
                .andExpect(jsonPath("$[0].title").value("Java tutorial"))
                .andExpect(jsonPath("$[0].messageCount").value(10))
                .andExpect(jsonPath("$[0].lastMessagePreview").value("About Java streams"));

        verify(conversationReadRepository).searchSummaries("Java");
    }
}
