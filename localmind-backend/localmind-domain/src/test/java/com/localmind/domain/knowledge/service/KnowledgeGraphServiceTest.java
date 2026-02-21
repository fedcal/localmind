package com.localmind.domain.knowledge.service;

import com.localmind.domain.knowledge.model.*;
import com.localmind.domain.knowledge.port.out.EntityExtractorPort;
import com.localmind.domain.knowledge.port.out.KnowledgeGraphPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class KnowledgeGraphServiceTest {

    private KnowledgeGraphPort knowledgeGraphPort;
    private EntityExtractorPort entityExtractorPort;
    private KnowledgeGraphService service;

    @BeforeEach
    void setUp() {
        knowledgeGraphPort = mock(KnowledgeGraphPort.class);
        entityExtractorPort = mock(EntityExtractorPort.class);
        service = new KnowledgeGraphService(knowledgeGraphPort, entityExtractorPort);
    }

    @Test
    void testIndexText_extractsAndStoresEntitiesAndRelations() {
        String text = "John works at Google in Mountain View";
        String sourceDocId = "doc-1";

        KnowledgeEntity entity1 = KnowledgeEntity.builder()
                .id("e1").name("John").type(EntityType.PERSON).properties(Map.of()).build();
        KnowledgeEntity entity2 = KnowledgeEntity.builder()
                .id("e2").name("Google").type(EntityType.ORGANIZATION).properties(Map.of()).build();

        KnowledgeRelation relation = KnowledgeRelation.builder()
                .id("r1").fromEntityId("e1").toEntityId("e2")
                .type(RelationType.WORKS_AT).properties(Map.of()).build();

        when(entityExtractorPort.extractEntities(text)).thenReturn(List.of(entity1, entity2));
        when(entityExtractorPort.extractRelations(text, List.of(entity1, entity2)))
                .thenReturn(List.of(relation));

        service.indexText(text, sourceDocId);

        verify(knowledgeGraphPort).storeEntities(List.of(entity1, entity2));
        verify(knowledgeGraphPort).storeRelations(List.of(relation));
    }

    @Test
    void testIndexText_noEntitiesExtracted_doesNotStoreRelations() {
        String text = "Some generic text without entities";

        when(entityExtractorPort.extractEntities(text)).thenReturn(List.of());

        service.indexText(text, "doc-1");

        verify(knowledgeGraphPort, never()).storeEntities(anyList());
        verify(entityExtractorPort, never()).extractRelations(anyString(), anyList());
        verify(knowledgeGraphPort, never()).storeRelations(anyList());
    }

    @Test
    void testIndexText_withEmptyText_throwsException() {
        assertThatThrownBy(() -> service.indexText("", "doc-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Text must not be empty");
    }

    @Test
    void testIndexText_withNullText_throwsException() {
        assertThatThrownBy(() -> service.indexText(null, "doc-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Text must not be empty");
    }

    @Test
    void testIndexText_entitiesExtractedButNoRelations() {
        String text = "John is a developer";
        KnowledgeEntity entity = KnowledgeEntity.builder()
                .id("e1").name("John").type(EntityType.PERSON).properties(Map.of()).build();

        when(entityExtractorPort.extractEntities(text)).thenReturn(List.of(entity));
        when(entityExtractorPort.extractRelations(text, List.of(entity))).thenReturn(List.of());

        service.indexText(text, "doc-1");

        verify(knowledgeGraphPort).storeEntities(List.of(entity));
        verify(knowledgeGraphPort, never()).storeRelations(anyList());
    }

    @Test
    void testSearchEntities_delegatesToPort() {
        KnowledgeEntity entity = KnowledgeEntity.builder()
                .id("e1").name("Google").type(EntityType.ORGANIZATION).properties(Map.of()).build();

        when(knowledgeGraphPort.findEntities("Google")).thenReturn(List.of(entity));

        List<KnowledgeEntity> result = service.searchEntities("Google");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Google");
        verify(knowledgeGraphPort).findEntities("Google");
    }

    @Test
    void testSearchEntities_withEmptyQuery_throwsException() {
        assertThatThrownBy(() -> service.searchEntities(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Query must not be empty");
    }

    @Test
    void testSearchEntities_withNullQuery_throwsException() {
        assertThatThrownBy(() -> service.searchEntities(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Query must not be empty");
    }

    @Test
    void testGetEntitySubgraph_delegatesToPort() {
        KnowledgeSubgraph subgraph = KnowledgeSubgraph.builder()
                .entities(List.of(KnowledgeEntity.builder()
                        .id("e1").name("John").type(EntityType.PERSON).properties(Map.of()).build()))
                .relations(List.of())
                .build();

        when(knowledgeGraphPort.getSubgraph("e1", 2)).thenReturn(subgraph);

        KnowledgeSubgraph result = service.getEntitySubgraph("e1", 2);

        assertThat(result.getEntities()).hasSize(1);
        assertThat(result.getRelations()).isEmpty();
        verify(knowledgeGraphPort).getSubgraph("e1", 2);
    }

    @Test
    void testGetEntitySubgraph_withEmptyId_throwsException() {
        assertThatThrownBy(() -> service.getEntitySubgraph("", 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entity ID must not be empty");
    }

    @Test
    void testGetEntitySubgraph_withInvalidDepth_throwsException() {
        assertThatThrownBy(() -> service.getEntitySubgraph("e1", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Depth must be at least 1");
    }

    @Test
    void testGetStats_delegatesToPort() {
        KnowledgeGraphStats stats = KnowledgeGraphStats.builder()
                .totalEntities(10)
                .totalRelations(5)
                .entitiesByType(Map.of(EntityType.PERSON, 3L, EntityType.ORGANIZATION, 7L))
                .build();

        when(knowledgeGraphPort.getStats()).thenReturn(stats);

        KnowledgeGraphStats result = service.getStats();

        assertThat(result.getTotalEntities()).isEqualTo(10);
        assertThat(result.getTotalRelations()).isEqualTo(5);
        assertThat(result.getEntitiesByType()).containsEntry(EntityType.PERSON, 3L);
        verify(knowledgeGraphPort).getStats();
    }

    @Test
    void testDeleteEntity_delegatesToPort() {
        service.deleteEntity("e1");

        verify(knowledgeGraphPort).deleteEntity("e1");
    }

    @Test
    void testDeleteEntity_withEmptyId_throwsException() {
        assertThatThrownBy(() -> service.deleteEntity(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entity ID must not be empty");
    }

    @Test
    void testDeleteEntity_withNullId_throwsException() {
        assertThatThrownBy(() -> service.deleteEntity(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entity ID must not be empty");
    }
}
