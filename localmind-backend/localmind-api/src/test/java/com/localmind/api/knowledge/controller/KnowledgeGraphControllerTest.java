package com.localmind.api.knowledge.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.knowledge.dto.IndexTextRequest;
import com.localmind.domain.knowledge.model.*;
import com.localmind.domain.knowledge.port.in.KnowledgeGraphUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class KnowledgeGraphControllerTest {

    private MockMvc mockMvc;
    private KnowledgeGraphUseCase knowledgeGraphUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        knowledgeGraphUseCase = mock(KnowledgeGraphUseCase.class);
        KnowledgeGraphController controller = new KnowledgeGraphController(
                Optional.of(knowledgeGraphUseCase));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSearchEntities_returnsResults() throws Exception {
        KnowledgeEntity entity = KnowledgeEntity.builder()
                .id("e1").name("Google").type(EntityType.ORGANIZATION)
                .properties(Map.of("sector", "Technology"))
                .build();

        when(knowledgeGraphUseCase.searchEntities("Google")).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/v1/knowledge/entities")
                        .param("query", "Google")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("e1"))
                .andExpect(jsonPath("$[0].name").value("Google"))
                .andExpect(jsonPath("$[0].type").value("ORGANIZATION"))
                .andExpect(jsonPath("$[0].properties.sector").value("Technology"));

        verify(knowledgeGraphUseCase).searchEntities("Google");
    }

    @Test
    void testSearchEntities_returnsEmptyList() throws Exception {
        when(knowledgeGraphUseCase.searchEntities("nonexistent")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/knowledge/entities")
                        .param("query", "nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetSubgraph_returnsSubgraph() throws Exception {
        KnowledgeEntity entity1 = KnowledgeEntity.builder()
                .id("e1").name("John").type(EntityType.PERSON).properties(Map.of()).build();
        KnowledgeEntity entity2 = KnowledgeEntity.builder()
                .id("e2").name("Google").type(EntityType.ORGANIZATION).properties(Map.of()).build();
        KnowledgeRelation relation = KnowledgeRelation.builder()
                .id("r1").fromEntityId("e1").toEntityId("e2")
                .type(RelationType.WORKS_AT).properties(Map.of()).build();

        KnowledgeSubgraph subgraph = KnowledgeSubgraph.builder()
                .entities(List.of(entity1, entity2))
                .relations(List.of(relation))
                .build();

        when(knowledgeGraphUseCase.getEntitySubgraph("e1", 2)).thenReturn(subgraph);

        mockMvc.perform(get("/api/v1/knowledge/entities/e1/subgraph")
                        .param("depth", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entities.length()").value(2))
                .andExpect(jsonPath("$.relations.length()").value(1))
                .andExpect(jsonPath("$.entities[0].name").value("John"))
                .andExpect(jsonPath("$.relations[0].type").value("WORKS_AT"));

        verify(knowledgeGraphUseCase).getEntitySubgraph("e1", 2);
    }

    @Test
    void testGetSubgraph_usesDefaultDepth() throws Exception {
        KnowledgeSubgraph subgraph = KnowledgeSubgraph.builder()
                .entities(List.of()).relations(List.of()).build();

        when(knowledgeGraphUseCase.getEntitySubgraph("e1", 2)).thenReturn(subgraph);

        mockMvc.perform(get("/api/v1/knowledge/entities/e1/subgraph")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(knowledgeGraphUseCase).getEntitySubgraph("e1", 2);
    }

    @Test
    void testIndexText_success() throws Exception {
        IndexTextRequest request = new IndexTextRequest("John works at Google", "doc-1");

        doNothing().when(knowledgeGraphUseCase).indexText("John works at Google", "doc-1");

        mockMvc.perform(post("/api/v1/knowledge/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(knowledgeGraphUseCase).indexText("John works at Google", "doc-1");
    }

    @Test
    void testIndexText_withBlankText_returns400() throws Exception {
        IndexTextRequest request = new IndexTextRequest("", null);

        mockMvc.perform(post("/api/v1/knowledge/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(knowledgeGraphUseCase, never()).indexText(anyString(), anyString());
    }

    @Test
    void testGetStats_returnsStats() throws Exception {
        KnowledgeGraphStats stats = KnowledgeGraphStats.builder()
                .totalEntities(25)
                .totalRelations(12)
                .entitiesByType(Map.of(EntityType.PERSON, 10L, EntityType.ORGANIZATION, 15L))
                .build();

        when(knowledgeGraphUseCase.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/knowledge/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEntities").value(25))
                .andExpect(jsonPath("$.totalRelations").value(12))
                .andExpect(jsonPath("$.entitiesByType.PERSON").value(10))
                .andExpect(jsonPath("$.entitiesByType.ORGANIZATION").value(15));

        verify(knowledgeGraphUseCase).getStats();
    }

    @Test
    void testDeleteEntity_success() throws Exception {
        doNothing().when(knowledgeGraphUseCase).deleteEntity("e1");

        mockMvc.perform(delete("/api/v1/knowledge/entities/e1"))
                .andExpect(status().isOk());

        verify(knowledgeGraphUseCase).deleteEntity("e1");
    }
}
