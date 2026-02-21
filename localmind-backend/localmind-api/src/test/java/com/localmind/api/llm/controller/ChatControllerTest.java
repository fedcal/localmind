package com.localmind.api.llm.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.llm.dto.ChatRequestDto;
import com.localmind.domain.common.exception.LlmProviderException;
import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.in.ChatUseCase;
import com.localmind.domain.llm.service.ConversationService;
import com.localmind.domain.document.model.SearchResult;
import com.localmind.domain.document.port.in.DocumentSearchUseCase;
import com.localmind.domain.mcp.model.ToolCallRequest;
import com.localmind.domain.mcp.model.ToolCallResponse;
import com.localmind.domain.mcp.port.in.ToolCallingPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ChatControllerTest {

    private MockMvc mockMvc;
    private ChatUseCase chatUseCase;
    private ConversationService conversationService;
    private ToolCallingPort toolCallingPort;
    private DocumentSearchUseCase documentSearchUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        chatUseCase = mock(ChatUseCase.class);
        conversationService = mock(ConversationService.class);
        toolCallingPort = mock(ToolCallingPort.class);
        documentSearchUseCase = mock(DocumentSearchUseCase.class);
        ChatController controller = new ChatController(chatUseCase, conversationService, toolCallingPort, documentSearchUseCase, 50);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void chat_shouldReturnOkWithResponse() throws Exception {
        String convId = "conv-123";
        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("Hello")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("Hello"), any())).thenReturn(conversation);

        TokenUsage tokenUsage = TokenUsage.builder()
                .promptTokens(10)
                .completionTokens(20)
                .totalTokens(30)
                .build();

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Hello from AI")
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .tokenUsage(tokenUsage)
                .latencyMs(150L)
                .build();

        when(chatUseCase.chat(any())).thenReturn(llmResponse);

        ConversationContext updatedConv = ConversationContext.builder()
                .id(convId)
                .title("Hello")
                .build();
        when(conversationService.addAssistantMessage(eq(convId), eq("Hello from AI"))).thenReturn(updatedConv);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Hello")
                .provider("OLLAMA")
                .model("llama3")
                .temperature(0.7)
                .maxTokens(512)
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello from AI"))
                .andExpect(jsonPath("$.model").value("llama3"))
                .andExpect(jsonPath("$.provider").value("OLLAMA"))
                .andExpect(jsonPath("$.conversationId").value(convId))
                .andExpect(jsonPath("$.latencyMs").value(150))
                .andExpect(jsonPath("$.tokenUsage.promptTokens").value(10))
                .andExpect(jsonPath("$.tokenUsage.completionTokens").value(20))
                .andExpect(jsonPath("$.tokenUsage.totalTokens").value(30));

        verify(conversationService).getOrCreateForChat(isNull(), eq("Hello"), any());
        verify(chatUseCase).chat(any());
        verify(conversationService).addAssistantMessage(eq(convId), eq("Hello from AI"));
    }

    @Test
    void chat_withExistingConversationId_shouldPassItThrough() throws Exception {
        String convId = "existing-conv-id";
        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("Previous chat")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hi").build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Hello!").build(),
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("How are you?").build()
                ))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(eq(convId), eq("How are you?"), any())).thenReturn(conversation);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("I'm doing well!")
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(100L)
                .build();

        when(chatUseCase.chat(any())).thenReturn(llmResponse);
        when(conversationService.addAssistantMessage(eq(convId), eq("I'm doing well!")))
                .thenReturn(conversation);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("How are you?")
                .conversationId(convId)
                .provider("OLLAMA")
                .model("llama3")
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("I'm doing well!"))
                .andExpect(jsonPath("$.conversationId").value(convId));

        verify(conversationService).getOrCreateForChat(eq(convId), eq("How are you?"), any());
    }

    @Test
    void chat_shouldReturn502OnLlmProviderException() throws Exception {
        ConversationContext conversation = ConversationContext.builder()
                .id("conv-err")
                .title("Hello")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("Hello"), any())).thenReturn(conversation);
        when(chatUseCase.chat(any())).thenThrow(new LlmProviderException("Connection refused"));

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Hello")
                .provider("OLLAMA")
                .model("llama3")
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("LLM provider error: Connection refused"));
    }

    @Test
    void chat_withSystemPrompt_shouldPrependSystemMessage() throws Exception {
        String convId = "conv-system";
        String systemPrompt = "You are a helpful assistant";

        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("Hello")
                .systemPrompt(systemPrompt)
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("Hello"), eq(systemPrompt))).thenReturn(conversation);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Hello! How can I help you?")
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(120L)
                .build();

        ArgumentCaptor<LlmRequest> requestCaptor = ArgumentCaptor.forClass(LlmRequest.class);
        when(chatUseCase.chat(requestCaptor.capture())).thenReturn(llmResponse);

        ConversationContext updatedConv = ConversationContext.builder()
                .id(convId)
                .title("Hello")
                .systemPrompt(systemPrompt)
                .build();
        when(conversationService.addAssistantMessage(eq(convId), eq("Hello! How can I help you?"))).thenReturn(updatedConv);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Hello")
                .systemPrompt(systemPrompt)
                .provider("OLLAMA")
                .model("llama3")
                .temperature(0.7)
                .maxTokens(512)
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello! How can I help you?"))
                .andExpect(jsonPath("$.conversationId").value(convId));

        verify(conversationService).getOrCreateForChat(isNull(), eq("Hello"), eq(systemPrompt));
        verify(chatUseCase).chat(any());
        verify(conversationService).addAssistantMessage(eq(convId), eq("Hello! How can I help you?"));

        // Verify that the LlmRequest has a SYSTEM message prepended
        LlmRequest capturedRequest = requestCaptor.getValue();
        List<ChatMessage> messages = capturedRequest.getMessages();

        assertThat(messages).hasSizeGreaterThanOrEqualTo(2);
        assertThat(messages.get(0).getRole()).isEqualTo(ChatMessage.Role.SYSTEM);
        assertThat(messages.get(0).getContent()).isEqualTo(systemPrompt);
        assertThat(messages.get(1).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(messages.get(1).getContent()).isEqualTo("Hello");
    }

    @Test
    void chat_withoutSystemPrompt_shouldNotPrependSystemMessage() throws Exception {
        String convId = "conv-no-system";

        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("Hi there")
                .systemPrompt(null)
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("Hi there").build()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("Hi there"), isNull())).thenReturn(conversation);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Hi! What can I do for you?")
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(110L)
                .build();

        ArgumentCaptor<LlmRequest> requestCaptor = ArgumentCaptor.forClass(LlmRequest.class);
        when(chatUseCase.chat(requestCaptor.capture())).thenReturn(llmResponse);

        ConversationContext updatedConv = ConversationContext.builder()
                .id(convId)
                .title("Hi there")
                .build();
        when(conversationService.addAssistantMessage(eq(convId), eq("Hi! What can I do for you?"))).thenReturn(updatedConv);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Hi there")
                .provider("OLLAMA")
                .model("llama3")
                .temperature(0.7)
                .maxTokens(512)
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hi! What can I do for you?"))
                .andExpect(jsonPath("$.conversationId").value(convId));

        verify(conversationService).getOrCreateForChat(isNull(), eq("Hi there"), isNull());
        verify(chatUseCase).chat(any());
        verify(conversationService).addAssistantMessage(eq(convId), eq("Hi! What can I do for you?"));

        // Verify that the LlmRequest does NOT have a SYSTEM message
        LlmRequest capturedRequest = requestCaptor.getValue();
        List<ChatMessage> messages = capturedRequest.getMessages();

        assertThat(messages).isNotEmpty();
        assertThat(messages.get(0).getRole()).isNotEqualTo(ChatMessage.Role.SYSTEM);
        assertThat(messages.get(0).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(messages.get(0).getContent()).isEqualTo("Hi there");

        // Verify no SYSTEM messages exist in the entire list
        boolean hasSystemMessage = messages.stream()
                .anyMatch(msg -> msg.getRole() == ChatMessage.Role.SYSTEM);
        assertThat(hasSystemMessage).isFalse();
    }

    @Test
    void chat_withToolMessages_shouldConvertToolToSystemForLlm() throws Exception {
        String convId = "conv-tool";

        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("Tool test")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Search for cats").build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Let me search...").build(),
                        ChatMessage.builder().role(ChatMessage.Role.TOOL).content("Found 5 results about cats")
                                .metadata(Map.of("toolName", "web_search")).build(),
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Tell me more").build()
                ))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(eq(convId), eq("Tell me more"), isNull())).thenReturn(conversation);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Here are more details about cats")
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(200L)
                .build();

        ArgumentCaptor<LlmRequest> requestCaptor = ArgumentCaptor.forClass(LlmRequest.class);
        when(chatUseCase.chat(requestCaptor.capture())).thenReturn(llmResponse);

        ConversationContext updatedConv = ConversationContext.builder()
                .id(convId)
                .title("Tool test")
                .build();
        when(conversationService.addAssistantMessage(eq(convId), eq("Here are more details about cats"))).thenReturn(updatedConv);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Tell me more")
                .conversationId(convId)
                .provider("OLLAMA")
                .model("llama3")
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Here are more details about cats"))
                .andExpect(jsonPath("$.conversationId").value(convId));

        // Verify the TOOL message was converted to SYSTEM with [Tool Result: toolName] prefix
        LlmRequest capturedRequest = requestCaptor.getValue();
        List<ChatMessage> messages = capturedRequest.getMessages();

        assertThat(messages).hasSize(4);
        assertThat(messages.get(0).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(messages.get(0).getContent()).isEqualTo("Search for cats");
        assertThat(messages.get(1).getRole()).isEqualTo(ChatMessage.Role.ASSISTANT);
        assertThat(messages.get(1).getContent()).isEqualTo("Let me search...");
        // TOOL message should now be SYSTEM with prefix
        assertThat(messages.get(2).getRole()).isEqualTo(ChatMessage.Role.SYSTEM);
        assertThat(messages.get(2).getContent()).isEqualTo("[Tool Result: web_search]\nFound 5 results about cats");
        assertThat(messages.get(3).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(messages.get(3).getContent()).isEqualTo("Tell me more");

        // Verify no TOOL role messages remain in the LlmRequest
        boolean hasToolMessage = messages.stream()
                .anyMatch(msg -> msg.getRole() == ChatMessage.Role.TOOL);
        assertThat(hasToolMessage).isFalse();
    }

    @Test
    void chat_withToolMessagesNoMetadata_shouldUseUnknownToolName() throws Exception {
        String convId = "conv-tool-no-meta";

        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("Tool no metadata")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Do something").build(),
                        ChatMessage.builder().role(ChatMessage.Role.TOOL).content("Some tool output")
                                .metadata(null).build(),
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Continue").build()
                ))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(eq(convId), eq("Continue"), isNull())).thenReturn(conversation);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Continuing...")
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(100L)
                .build();

        ArgumentCaptor<LlmRequest> requestCaptor = ArgumentCaptor.forClass(LlmRequest.class);
        when(chatUseCase.chat(requestCaptor.capture())).thenReturn(llmResponse);

        ConversationContext updatedConv = ConversationContext.builder()
                .id(convId)
                .title("Tool no metadata")
                .build();
        when(conversationService.addAssistantMessage(eq(convId), eq("Continuing..."))).thenReturn(updatedConv);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Continue")
                .conversationId(convId)
                .provider("OLLAMA")
                .model("llama3")
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Continuing..."))
                .andExpect(jsonPath("$.conversationId").value(convId));

        // Verify the TOOL message with null metadata uses "unknown" as toolName
        LlmRequest capturedRequest = requestCaptor.getValue();
        List<ChatMessage> messages = capturedRequest.getMessages();

        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(messages.get(0).getContent()).isEqualTo("Do something");
        // TOOL message with null metadata should use "unknown"
        assertThat(messages.get(1).getRole()).isEqualTo(ChatMessage.Role.SYSTEM);
        assertThat(messages.get(1).getContent()).isEqualTo("[Tool Result: unknown]\nSome tool output");
        assertThat(messages.get(2).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(messages.get(2).getContent()).isEqualTo("Continue");
    }

    @Test
    void chat_withToolCallingEnabled_shouldExecuteToolAndReturnFinalResponse() throws Exception {
        String convId = "conv-agentic";

        // Conversazione iniziale con messaggio USER
        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("Use a tool")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("What's the weather?").build()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("What's the weather?"), any())).thenReturn(conversation);

        // Tool calling abilitato e tool disponibili
        when(toolCallingPort.hasAvailableTools()).thenReturn(true);
        when(toolCallingPort.buildToolsSystemPrompt()).thenReturn("Available tools:\n- weather_check");

        // Prima chiamata LLM: risponde con tool_call
        String toolCallJson = "Let me check. {\"tool_call\": {\"name\": \"weather_check\", \"serverId\": \"srv1\", \"arguments\": {\"city\": \"Rome\"}}}";
        LlmResponse firstResponse = LlmResponse.builder()
                .content(toolCallJson)
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(200L)
                .build();

        // Seconda chiamata LLM: risposta finale
        LlmResponse finalResponse = LlmResponse.builder()
                .content("The weather in Rome is sunny, 22°C")
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(150L)
                .build();

        when(chatUseCase.chat(any())).thenReturn(firstResponse, finalResponse);

        // Parsing tool call dalla prima risposta
        ToolCallRequest toolCallReq = ToolCallRequest.builder()
                .toolName("weather_check")
                .serverId("srv1")
                .arguments(Map.of("city", "Rome"))
                .build();
        when(toolCallingPort.parseToolCallFromResponse(toolCallJson)).thenReturn(Optional.of(toolCallReq));
        when(toolCallingPort.parseToolCallFromResponse("The weather in Rome is sunny, 22°C")).thenReturn(Optional.empty());

        // Esecuzione tool
        ToolCallResponse toolResp = ToolCallResponse.builder()
                .toolName("weather_check")
                .result("{\"temp\": 22, \"condition\": \"sunny\"}")
                .success(true)
                .executionTimeMs(500)
                .build();
        when(toolCallingPort.executeToolCall(toolCallReq)).thenReturn(toolResp);

        // Conversazione aggiornata dopo tool message
        ConversationContext updatedConv = ConversationContext.builder()
                .id(convId)
                .title("Use a tool")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("What's the weather?").build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content(toolCallJson).build(),
                        ChatMessage.builder().role(ChatMessage.Role.TOOL).content("{\"temp\": 22, \"condition\": \"sunny\"}")
                                .metadata(Map.of("toolName", "weather_check")).build()
                ))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(conversationService.getById(convId)).thenReturn(updatedConv);
        when(conversationService.addAssistantMessage(eq(convId), anyString())).thenReturn(updatedConv);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("What's the weather?")
                .provider("OLLAMA")
                .model("llama3")
                .enableToolCalling(true)
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("The weather in Rome is sunny, 22°C"))
                .andExpect(jsonPath("$.conversationId").value(convId));

        // Verifica: 2 chiamate LLM, 1 esecuzione tool, salvataggio assistant intermedio e tool message
        verify(chatUseCase, times(2)).chat(any());
        verify(toolCallingPort).executeToolCall(toolCallReq);
        verify(conversationService).addAssistantMessage(eq(convId), eq(toolCallJson));
        verify(conversationService).addToolMessage(eq(convId), eq("weather_check"),
                eq("{\"temp\": 22, \"condition\": \"sunny\"}"), anyMap());
        verify(conversationService).getById(convId);
        // Risposta finale salvata
        verify(conversationService).addAssistantMessage(eq(convId), eq("The weather in Rome is sunny, 22°C"));
    }

    @Test
    void chat_withToolCallingDisabled_shouldNotCheckForTools() throws Exception {
        String convId = "conv-no-tool";

        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("No tools")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("Hello"), any())).thenReturn(conversation);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Hi there!")
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(100L)
                .build();

        when(chatUseCase.chat(any())).thenReturn(llmResponse);
        when(conversationService.addAssistantMessage(eq(convId), eq("Hi there!"))).thenReturn(conversation);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Hello")
                .provider("OLLAMA")
                .model("llama3")
                .enableToolCalling(false)
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hi there!"));

        // Tool calling non dovrebbe essere invocato
        verify(toolCallingPort, never()).hasAvailableTools();
        verify(toolCallingPort, never()).buildToolsSystemPrompt();
        verify(toolCallingPort, never()).parseToolCallFromResponse(anyString());
        verify(toolCallingPort, never()).executeToolCall(any());
    }

    @Test
    void chat_withToolCallingEnabled_butNoToolsAvailable_shouldSkipToolCalling() throws Exception {
        String convId = "conv-no-tools-avail";

        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("No tools available")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("Help me").build()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("Help me"), any())).thenReturn(conversation);
        when(toolCallingPort.hasAvailableTools()).thenReturn(false);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("How can I help?")
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(100L)
                .build();

        when(chatUseCase.chat(any())).thenReturn(llmResponse);
        when(conversationService.addAssistantMessage(eq(convId), eq("How can I help?"))).thenReturn(conversation);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Help me")
                .provider("OLLAMA")
                .model("llama3")
                .enableToolCalling(true)
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("How can I help?"));

        // Tool prompt non dovrebbe essere generato
        verify(toolCallingPort, never()).buildToolsSystemPrompt();
        verify(toolCallingPort, never()).parseToolCallFromResponse(anyString());
    }

    // ===== RAG Tests =====

    @Test
    void chat_withRagEnabled_shouldSearchAndReturnSources() throws Exception {
        String convId = "conv-rag";
        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("RAG test")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER)
                        .content("Tell me about the report").build()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("Tell me about the report"), any()))
                .thenReturn(conversation);

        SearchResult sr = SearchResult.builder()
                .documentId("doc-1").filename("report.pdf")
                .content("The quarterly report shows growth").score(0.95).chunkIndex(0).build();
        when(documentSearchUseCase.search("Tell me about the report", 5))
                .thenReturn(List.of(sr));

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Based on your documents, the report shows growth")
                .model("llama3").provider(LlmProvider.OLLAMA).latencyMs(100L).build();

        ArgumentCaptor<LlmRequest> requestCaptor = ArgumentCaptor.forClass(LlmRequest.class);
        when(chatUseCase.chat(requestCaptor.capture())).thenReturn(llmResponse);
        when(conversationService.addAssistantMessage(eq(convId), anyString())).thenReturn(conversation);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Tell me about the report")
                .provider("OLLAMA").model("llama3").enableRag(true).build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ragSources").isArray())
                .andExpect(jsonPath("$.ragSources.length()").value(1))
                .andExpect(jsonPath("$.ragSources[0].documentId").value("doc-1"))
                .andExpect(jsonPath("$.ragSources[0].filename").value("report.pdf"))
                .andExpect(jsonPath("$.ragSources[0].score").value(0.95))
                .andExpect(jsonPath("$.ragSources[0].chunkIndex").value(0));

        // Verify RAG context was injected into system messages
        LlmRequest captured = requestCaptor.getValue();
        boolean hasRagContext = captured.getMessages().stream()
                .anyMatch(m -> m.getRole() == ChatMessage.Role.SYSTEM
                        && m.getContent().contains("[Contesto dai tuoi documenti]")
                        && m.getContent().contains("report.pdf"));
        assertThat(hasRagContext).isTrue();

        verify(documentSearchUseCase).search("Tell me about the report", 5);
    }

    @Test
    void chat_withRagDisabled_shouldNotSearchDocuments() throws Exception {
        String convId = "conv-no-rag";
        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("No RAG")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello").build()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("Hello"), any())).thenReturn(conversation);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Hi there!").model("llama3").provider(LlmProvider.OLLAMA).latencyMs(100L).build();
        when(chatUseCase.chat(any())).thenReturn(llmResponse);
        when(conversationService.addAssistantMessage(eq(convId), eq("Hi there!"))).thenReturn(conversation);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Hello").provider("OLLAMA").model("llama3").enableRag(false).build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ragSources").doesNotExist());

        verify(documentSearchUseCase, never()).search(any(), anyInt());
    }

    @Test
    void chat_withRagEnabled_noResults_shouldReturnNullRagSources() throws Exception {
        String convId = "conv-rag-empty";
        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("RAG empty")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER)
                        .content("Something obscure").build()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("Something obscure"), any()))
                .thenReturn(conversation);
        when(documentSearchUseCase.search("Something obscure", 5)).thenReturn(List.of());

        LlmResponse llmResponse = LlmResponse.builder()
                .content("I don't have relevant documents").model("llama3")
                .provider(LlmProvider.OLLAMA).latencyMs(100L).build();
        when(chatUseCase.chat(any())).thenReturn(llmResponse);
        when(conversationService.addAssistantMessage(eq(convId), anyString())).thenReturn(conversation);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Something obscure").provider("OLLAMA").model("llama3").enableRag(true).build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ragSources").doesNotExist());

        verify(documentSearchUseCase).search("Something obscure", 5);
    }

    // ===== P5 - Memory Window / Context Limit =====

    @Test
    void chat_shouldUseContextWindowForLongConversation() throws Exception {
        // Setup a controller instance with defaultMaxContextMessages=5
        ChatUseCase localChatUseCase = mock(ChatUseCase.class);
        ConversationService localConversationService = mock(ConversationService.class);
        ToolCallingPort localToolCallingPort = mock(ToolCallingPort.class);
        DocumentSearchUseCase localDocSearch = mock(DocumentSearchUseCase.class);
        ChatController limitedController = new ChatController(localChatUseCase, localConversationService, localToolCallingPort, localDocSearch, 5);
        MockMvc limitedMockMvc = MockMvcBuilders.standaloneSetup(limitedController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String convId = "conv-context-window";

        // Build a conversation with 10 messages
        List<ChatMessage> tenMessages = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ChatMessage.Role role = (i % 2 == 0) ? ChatMessage.Role.USER : ChatMessage.Role.ASSISTANT;
            tenMessages.add(ChatMessage.builder().role(role).content("msg-" + i).build());
        }

        ConversationContext conversation = ConversationContext.builder()
                .id(convId)
                .title("Long conversation")
                .messages(tenMessages)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(localConversationService.getOrCreateForChat(eq(convId), eq("msg-latest"), isNull())).thenReturn(conversation);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Response to context window")
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(100L)
                .build();

        ArgumentCaptor<LlmRequest> requestCaptor = ArgumentCaptor.forClass(LlmRequest.class);
        when(localChatUseCase.chat(requestCaptor.capture())).thenReturn(llmResponse);
        when(localConversationService.addAssistantMessage(eq(convId), anyString())).thenReturn(conversation);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("msg-latest")
                .conversationId(convId)
                .provider("OLLAMA")
                .model("llama3")
                .build();

        limitedMockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Response to context window"));

        // Verify only the last 5 messages were sent to the LLM
        LlmRequest capturedRequest = requestCaptor.getValue();
        List<ChatMessage> sentMessages = capturedRequest.getMessages();

        assertThat(sentMessages).hasSize(5);
        assertThat(sentMessages.get(0).getContent()).isEqualTo("msg-5");
        assertThat(sentMessages.get(1).getContent()).isEqualTo("msg-6");
        assertThat(sentMessages.get(2).getContent()).isEqualTo("msg-7");
        assertThat(sentMessages.get(3).getContent()).isEqualTo("msg-8");
        assertThat(sentMessages.get(4).getContent()).isEqualTo("msg-9");
    }
}
