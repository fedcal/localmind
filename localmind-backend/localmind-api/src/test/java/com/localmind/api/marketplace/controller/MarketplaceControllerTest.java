package com.localmind.api.marketplace.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.marketplace.dto.AddReviewRequest;
import com.localmind.api.marketplace.dto.PublishAgentRequest;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.marketplace.model.AgentCategory;
import com.localmind.domain.marketplace.model.MarketplaceAgent;
import com.localmind.domain.marketplace.model.MarketplaceReview;
import com.localmind.domain.marketplace.port.in.MarketplaceUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

class MarketplaceControllerTest {

    private MockMvc mockMvc;
    private MarketplaceUseCase marketplaceUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        marketplaceUseCase = mock(MarketplaceUseCase.class);
        MarketplaceController controller = new MarketplaceController(marketplaceUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testListAgents_noFilters() throws Exception {
        List<MarketplaceAgent> agents = List.of(
                MarketplaceAgent.builder()
                        .id("a1").name("Agent A").description("Desc A").author("Author A")
                        .version("1.0.0").category(AgentCategory.CHAT).downloadCount(10).avgRating(4.5)
                        .createdAt(Instant.parse("2026-01-15T10:00:00Z")).build(),
                MarketplaceAgent.builder()
                        .id("a2").name("Agent B").description("Desc B").author("Author B")
                        .version("2.0.0").category(AgentCategory.AUTOMATION).downloadCount(5).avgRating(3.0)
                        .createdAt(Instant.parse("2026-01-16T10:00:00Z")).build()
        );
        when(marketplaceUseCase.listAgents(null, null)).thenReturn(agents);

        mockMvc.perform(get("/api/v1/marketplace/agents")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("a1"))
                .andExpect(jsonPath("$[0].name").value("Agent A"))
                .andExpect(jsonPath("$[0].category").value("CHAT"))
                .andExpect(jsonPath("$[0].downloadCount").value(10))
                .andExpect(jsonPath("$[0].avgRating").value(4.5))
                .andExpect(jsonPath("$[1].id").value("a2"))
                .andExpect(jsonPath("$[1].name").value("Agent B"))
                .andExpect(jsonPath("$[1].category").value("AUTOMATION"));

        verify(marketplaceUseCase).listAgents(null, null);
    }

    @Test
    void testListAgents_withCategory() throws Exception {
        List<MarketplaceAgent> agents = List.of(
                MarketplaceAgent.builder()
                        .id("a1").name("Chat Agent").category(AgentCategory.CHAT)
                        .createdAt(Instant.parse("2026-01-15T10:00:00Z")).build()
        );
        when(marketplaceUseCase.listAgents(AgentCategory.CHAT, null)).thenReturn(agents);

        mockMvc.perform(get("/api/v1/marketplace/agents")
                        .param("category", "CHAT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("CHAT"));

        verify(marketplaceUseCase).listAgents(AgentCategory.CHAT, null);
    }

    @Test
    void testGetAgent_found() throws Exception {
        MarketplaceAgent agent = MarketplaceAgent.builder()
                .id("a1").name("Agent A").description("Description").author("Author")
                .version("1.0.0").category(AgentCategory.CODE_ASSISTANT)
                .downloadCount(100).avgRating(4.8)
                .createdAt(Instant.parse("2026-01-15T10:00:00Z")).build();
        when(marketplaceUseCase.getAgent("a1")).thenReturn(Optional.of(agent));

        mockMvc.perform(get("/api/v1/marketplace/agents/a1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("a1"))
                .andExpect(jsonPath("$.name").value("Agent A"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.author").value("Author"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.category").value("CODE_ASSISTANT"))
                .andExpect(jsonPath("$.downloadCount").value(100))
                .andExpect(jsonPath("$.avgRating").value(4.8));

        verify(marketplaceUseCase).getAgent("a1");
    }

    @Test
    void testGetAgent_notFound() throws Exception {
        when(marketplaceUseCase.getAgent("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/marketplace/agents/nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(marketplaceUseCase).getAgent("nonexistent");
    }

    @Test
    void testPublishAgent_success() throws Exception {
        MarketplaceAgent saved = MarketplaceAgent.builder()
                .id("a-new").name("New Agent").description("New desc").author("Author")
                .version("1.0.0").category(AgentCategory.CUSTOM)
                .downloadUrl("https://example.com/agent.jar")
                .createdAt(Instant.parse("2026-01-15T10:00:00Z"))
                .publishedAt(Instant.parse("2026-01-15T10:00:00Z"))
                .build();
        when(marketplaceUseCase.publishAgent(any(MarketplaceAgent.class))).thenReturn(saved);

        PublishAgentRequest request = new PublishAgentRequest(
                "New Agent", "New desc", "Author", "1.0.0", "CUSTOM", "https://example.com/agent.jar"
        );

        mockMvc.perform(post("/api/v1/marketplace/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("a-new"))
                .andExpect(jsonPath("$.name").value("New Agent"))
                .andExpect(jsonPath("$.category").value("CUSTOM"))
                .andExpect(jsonPath("$.downloadUrl").value("https://example.com/agent.jar"));

        verify(marketplaceUseCase).publishAgent(any(MarketplaceAgent.class));
    }

    @Test
    void testInstallAgent_success() throws Exception {
        doNothing().when(marketplaceUseCase).installAgent("a1");

        mockMvc.perform(post("/api/v1/marketplace/agents/a1/install"))
                .andExpect(status().isOk());

        verify(marketplaceUseCase).installAgent("a1");
    }

    @Test
    void testInstallAgent_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Agent not found: missing"))
                .when(marketplaceUseCase).installAgent("missing");

        mockMvc.perform(post("/api/v1/marketplace/agents/missing/install"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Agent not found: missing"));
    }

    @Test
    void testAddReview_success() throws Exception {
        MarketplaceReview review = MarketplaceReview.builder()
                .id("r1").agentId("a1").rating(5).comment("Excellent!")
                .createdAt(Instant.parse("2026-01-15T12:00:00Z")).build();
        when(marketplaceUseCase.addReview("a1", 5, "Excellent!")).thenReturn(review);

        AddReviewRequest request = new AddReviewRequest(5, "Excellent!");

        mockMvc.perform(post("/api/v1/marketplace/agents/a1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("r1"))
                .andExpect(jsonPath("$.agentId").value("a1"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent!"));

        verify(marketplaceUseCase).addReview("a1", 5, "Excellent!");
    }

    @Test
    void testGetReviews_success() throws Exception {
        List<MarketplaceReview> reviews = List.of(
                MarketplaceReview.builder()
                        .id("r1").agentId("a1").rating(4).comment("Good")
                        .createdAt(Instant.parse("2026-01-15T12:00:00Z")).build(),
                MarketplaceReview.builder()
                        .id("r2").agentId("a1").rating(5).comment("Great")
                        .createdAt(Instant.parse("2026-01-16T12:00:00Z")).build()
        );
        when(marketplaceUseCase.getReviews("a1")).thenReturn(reviews);

        mockMvc.perform(get("/api/v1/marketplace/agents/a1/reviews")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("r1"))
                .andExpect(jsonPath("$[0].rating").value(4))
                .andExpect(jsonPath("$[1].id").value("r2"))
                .andExpect(jsonPath("$[1].rating").value(5));

        verify(marketplaceUseCase).getReviews("a1");
    }

    @Test
    void testDeleteAgent_success() throws Exception {
        doNothing().when(marketplaceUseCase).deleteAgent("a1");

        mockMvc.perform(delete("/api/v1/marketplace/agents/a1"))
                .andExpect(status().isNoContent());

        verify(marketplaceUseCase).deleteAgent("a1");
    }

    @Test
    void testDeleteAgent_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Agent not found: missing"))
                .when(marketplaceUseCase).deleteAgent("missing");

        mockMvc.perform(delete("/api/v1/marketplace/agents/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Agent not found: missing"));
    }
}
