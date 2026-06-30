package com.localmind.infrastructure.knowledge.adapter;

import com.localmind.domain.knowledge.model.EntityType;
import com.localmind.domain.knowledge.model.KnowledgeEntity;
import com.localmind.domain.knowledge.model.KnowledgeRelation;
import com.localmind.domain.knowledge.model.RelationType;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmRequest;
import com.localmind.domain.llm.model.LlmResponse;
import com.localmind.domain.llm.port.out.LlmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LlmEntityExtractorAdapterTest {

    private LlmClient llmClient;
    private LlmEntityExtractorAdapter adapter;

    @BeforeEach
    void setUp() {
        llmClient = mock(LlmClient.class);
        when(llmClient.getProvider()).thenReturn(LlmProvider.OLLAMA);
        adapter = new LlmEntityExtractorAdapter(List.of(llmClient));
    }

    @Test
    void testExtractEntities_parsesJsonResponse() {
        String jsonResponse = "[" +
                "{\"id\": \"e1\", \"name\": \"John\", \"type\": \"PERSON\", \"properties\": {\"role\": \"developer\"}}," +
                "{\"id\": \"e2\", \"name\": \"Google\", \"type\": \"ORGANIZATION\", \"properties\": {}}" +
                "]";

        when(llmClient.call(any(LlmRequest.class)))
                .thenReturn(LlmResponse.builder().content(jsonResponse).build());

        List<KnowledgeEntity> entities = adapter.extractEntities("John works at Google");

        assertThat(entities).hasSize(2);
        assertThat(entities.get(0).getName()).isEqualTo("John");
        assertThat(entities.get(0).getType()).isEqualTo(EntityType.PERSON);
        assertThat(entities.get(0).getProperties()).containsEntry("role", "developer");
        assertThat(entities.get(1).getName()).isEqualTo("Google");
        assertThat(entities.get(1).getType()).isEqualTo(EntityType.ORGANIZATION);
    }

    @Test
    void testExtractEntities_parsesJsonWithSurroundingText() {
        String jsonResponse = "Here are the entities:\n" +
                "[{\"id\": \"e1\", \"name\": \"Python\", \"type\": \"TECHNOLOGY\", \"properties\": {}}]\n" +
                "Found 1 entity.";

        when(llmClient.call(any(LlmRequest.class)))
                .thenReturn(LlmResponse.builder().content(jsonResponse).build());

        List<KnowledgeEntity> entities = adapter.extractEntities("Python is a programming language");

        assertThat(entities).hasSize(1);
        assertThat(entities.get(0).getName()).isEqualTo("Python");
        assertThat(entities.get(0).getType()).isEqualTo(EntityType.TECHNOLOGY);
    }

    @Test
    void testExtractEntities_invalidJson_returnsEmptyList() {
        when(llmClient.call(any(LlmRequest.class)))
                .thenReturn(LlmResponse.builder().content("This is not JSON").build());

        List<KnowledgeEntity> entities = adapter.extractEntities("Some text");

        assertThat(entities).isEmpty();
    }

    @Test
    void testExtractEntities_emptyText_returnsEmptyList() {
        List<KnowledgeEntity> entities = adapter.extractEntities("");

        assertThat(entities).isEmpty();
    }

    @Test
    void testExtractEntities_nullText_returnsEmptyList() {
        List<KnowledgeEntity> entities = adapter.extractEntities(null);

        assertThat(entities).isEmpty();
    }

    @Test
    void testExtractEntities_llmThrowsException_returnsEmptyList() {
        when(llmClient.call(any(LlmRequest.class)))
                .thenThrow(new RuntimeException("LLM error"));

        List<KnowledgeEntity> entities = adapter.extractEntities("Some text");

        assertThat(entities).isEmpty();
    }

    @Test
    void testExtractRelations_parsesJsonResponse() {
        String jsonResponse = "[" +
                "{\"id\": \"r1\", \"fromEntityId\": \"e1\", \"toEntityId\": \"e2\", " +
                "\"type\": \"WORKS_AT\", \"properties\": {\"since\": \"2020\"}}" +
                "]";

        when(llmClient.call(any(LlmRequest.class)))
                .thenReturn(LlmResponse.builder().content(jsonResponse).build());

        List<KnowledgeEntity> entities = List.of(
                KnowledgeEntity.builder().id("e1").name("John").type(EntityType.PERSON).properties(Map.of()).build(),
                KnowledgeEntity.builder().id("e2").name("Google").type(EntityType.ORGANIZATION).properties(Map.of()).build()
        );

        List<KnowledgeRelation> relations = adapter.extractRelations("John works at Google", entities);

        assertThat(relations).hasSize(1);
        assertThat(relations.get(0).getFromEntityId()).isEqualTo("e1");
        assertThat(relations.get(0).getToEntityId()).isEqualTo("e2");
        assertThat(relations.get(0).getType()).isEqualTo(RelationType.WORKS_AT);
        assertThat(relations.get(0).getProperties()).containsEntry("since", "2020");
    }

    @Test
    void testExtractRelations_noEntities_returnsEmptyList() {
        List<KnowledgeRelation> relations = adapter.extractRelations("Some text", List.of());

        assertThat(relations).isEmpty();
    }

    @Test
    void testExtractRelations_nullEntities_returnsEmptyList() {
        List<KnowledgeRelation> relations = adapter.extractRelations("Some text", null);

        assertThat(relations).isEmpty();
    }

    @Test
    void testExtractRelations_invalidJson_returnsEmptyList() {
        when(llmClient.call(any(LlmRequest.class)))
                .thenReturn(LlmResponse.builder().content("not json").build());

        List<KnowledgeEntity> entities = List.of(
                KnowledgeEntity.builder().id("e1").name("John").type(EntityType.PERSON).properties(Map.of()).build()
        );

        List<KnowledgeRelation> relations = adapter.extractRelations("Some text", entities);

        assertThat(relations).isEmpty();
    }

    @Test
    void testExtractRelations_llmThrowsException_returnsEmptyList() {
        when(llmClient.call(any(LlmRequest.class)))
                .thenThrow(new RuntimeException("LLM error"));

        List<KnowledgeEntity> entities = List.of(
                KnowledgeEntity.builder().id("e1").name("John").type(EntityType.PERSON).properties(Map.of()).build()
        );

        List<KnowledgeRelation> relations = adapter.extractRelations("Some text", entities);

        assertThat(relations).isEmpty();
    }

    @Test
    void testNoLlmClient_extractEntities_returnsEmptyList() {
        LlmEntityExtractorAdapter adapterWithoutClient = new LlmEntityExtractorAdapter(List.of());

        List<KnowledgeEntity> entities = adapterWithoutClient.extractEntities("Some text");

        assertThat(entities).isEmpty();
    }
}
