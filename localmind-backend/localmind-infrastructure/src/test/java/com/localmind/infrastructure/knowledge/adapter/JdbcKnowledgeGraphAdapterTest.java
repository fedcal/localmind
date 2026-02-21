package com.localmind.infrastructure.knowledge.adapter;

import com.localmind.domain.knowledge.model.*;
import com.localmind.infrastructure.knowledge.persistence.entity.KnowledgeEntityEntity;
import com.localmind.infrastructure.knowledge.persistence.entity.KnowledgeRelationEntity;
import com.localmind.infrastructure.knowledge.persistence.repository.KnowledgeEntityJpaRepository;
import com.localmind.infrastructure.knowledge.persistence.repository.KnowledgeRelationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class JdbcKnowledgeGraphAdapterTest {

    private KnowledgeEntityJpaRepository entityRepository;
    private KnowledgeRelationJpaRepository relationRepository;
    private JdbcKnowledgeGraphAdapter adapter;

    @BeforeEach
    void setUp() {
        entityRepository = mock(KnowledgeEntityJpaRepository.class);
        relationRepository = mock(KnowledgeRelationJpaRepository.class);
        adapter = new JdbcKnowledgeGraphAdapter(entityRepository, relationRepository);
    }

    @Test
    void testStoreEntities_savesAll() {
        KnowledgeEntity entity1 = KnowledgeEntity.builder()
                .id(UUID.randomUUID().toString()).name("John").type(EntityType.PERSON)
                .properties(Map.of("role", "developer")).build();
        KnowledgeEntity entity2 = KnowledgeEntity.builder()
                .id(UUID.randomUUID().toString()).name("Google").type(EntityType.ORGANIZATION)
                .properties(Map.of()).build();

        when(entityRepository.saveAll(anyList())).thenReturn(List.of());

        adapter.storeEntities(List.of(entity1, entity2));

        verify(entityRepository).saveAll(argThat(list -> {
            @SuppressWarnings("unchecked")
            List<KnowledgeEntityEntity> entities = (List<KnowledgeEntityEntity>) list;
            return entities.size() == 2;
        }));
    }

    @Test
    void testStoreRelations_savesAll() {
        KnowledgeRelation relation = KnowledgeRelation.builder()
                .id(UUID.randomUUID().toString())
                .fromEntityId("e1").toEntityId("e2")
                .type(RelationType.WORKS_AT).properties(Map.of()).build();

        when(relationRepository.saveAll(anyList())).thenReturn(List.of());

        adapter.storeRelations(List.of(relation));

        verify(relationRepository).saveAll(argThat(list -> {
            @SuppressWarnings("unchecked")
            List<KnowledgeRelationEntity> relations = (List<KnowledgeRelationEntity>) list;
            return relations.size() == 1;
        }));
    }

    @Test
    void testFindEntities_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        KnowledgeEntityEntity jpaEntity = KnowledgeEntityEntity.builder()
                .id(id).name("Google").entityType("ORGANIZATION")
                .properties("{\"sector\":\"tech\"}").createdAt(Instant.now()).build();

        when(entityRepository.findByNameContainingIgnoreCase("Google"))
                .thenReturn(List.of(jpaEntity));

        List<KnowledgeEntity> result = adapter.findEntities("Google");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Google");
        assertThat(result.get(0).getType()).isEqualTo(EntityType.ORGANIZATION);
        assertThat(result.get(0).getProperties()).containsEntry("sector", "tech");
        verify(entityRepository).findByNameContainingIgnoreCase("Google");
    }

    @Test
    void testFindEntities_emptyResult() {
        when(entityRepository.findByNameContainingIgnoreCase("unknown"))
                .thenReturn(List.of());

        List<KnowledgeEntity> result = adapter.findEntities("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void testGetStats_countsCorrectly() {
        when(entityRepository.count()).thenReturn(15L);
        when(relationRepository.count()).thenReturn(8L);

        List<Object[]> typeCounts = new ArrayList<>();
        typeCounts.add(new Object[]{"PERSON", 5L});
        typeCounts.add(new Object[]{"ORGANIZATION", 10L});
        when(entityRepository.countByEntityType()).thenReturn(typeCounts);

        KnowledgeGraphStats stats = adapter.getStats();

        assertThat(stats.getTotalEntities()).isEqualTo(15);
        assertThat(stats.getTotalRelations()).isEqualTo(8);
        assertThat(stats.getEntitiesByType()).containsEntry(EntityType.PERSON, 5L);
        assertThat(stats.getEntitiesByType()).containsEntry(EntityType.ORGANIZATION, 10L);
    }

    @Test
    void testGetStats_handlesUnknownEntityType() {
        when(entityRepository.count()).thenReturn(1L);
        when(relationRepository.count()).thenReturn(0L);

        List<Object[]> typeCounts = new ArrayList<>();
        typeCounts.add(new Object[]{"UNKNOWN_TYPE", 1L});
        when(entityRepository.countByEntityType()).thenReturn(typeCounts);

        KnowledgeGraphStats stats = adapter.getStats();

        assertThat(stats.getTotalEntities()).isEqualTo(1);
        assertThat(stats.getEntitiesByType()).isEmpty();
    }

    @Test
    void testGetSubgraph_withDepth1() {
        UUID entityId = UUID.randomUUID();
        UUID relatedId = UUID.randomUUID();
        String entityIdStr = entityId.toString();
        String relatedIdStr = relatedId.toString();

        KnowledgeEntityEntity rootEntity = KnowledgeEntityEntity.builder()
                .id(entityId).name("John").entityType("PERSON")
                .properties(null).createdAt(Instant.now()).build();

        KnowledgeRelationEntity relation = KnowledgeRelationEntity.builder()
                .id(UUID.randomUUID()).fromEntityId(entityIdStr).toEntityId(relatedIdStr)
                .relationType("WORKS_AT").properties(null).createdAt(Instant.now()).build();

        when(entityRepository.findById(entityId)).thenReturn(Optional.of(rootEntity));
        when(relationRepository.findByFromEntityIdOrToEntityId(entityIdStr, entityIdStr))
                .thenReturn(List.of(relation));

        // For depth=1 the related entity is at depth 0 (no more traversal)
        when(relationRepository.findByFromEntityIdOrToEntityId(relatedIdStr, relatedIdStr))
                .thenReturn(List.of());
        when(entityRepository.findById(relatedId)).thenReturn(Optional.empty());

        KnowledgeSubgraph subgraph = adapter.getSubgraph(entityIdStr, 1);

        assertThat(subgraph.getEntities()).hasSize(1);
        assertThat(subgraph.getEntities().get(0).getName()).isEqualTo("John");
        assertThat(subgraph.getRelations()).hasSize(1);
        assertThat(subgraph.getRelations().get(0).getType()).isEqualTo(RelationType.WORKS_AT);
    }

    @Test
    void testDeleteEntity_deletesEntityAndRelations() {
        String entityId = UUID.randomUUID().toString();

        adapter.deleteEntity(entityId);

        verify(relationRepository).deleteByFromEntityIdOrToEntityId(entityId, entityId);
        verify(entityRepository).deleteById(UUID.fromString(entityId));
    }

    @Test
    void testStoreEntities_withNullProperties() {
        KnowledgeEntity entity = KnowledgeEntity.builder()
                .id(UUID.randomUUID().toString()).name("Test").type(EntityType.CONCEPT)
                .properties(null).build();

        when(entityRepository.saveAll(anyList())).thenReturn(List.of());

        adapter.storeEntities(List.of(entity));

        verify(entityRepository).saveAll(anyList());
    }

    @Test
    void testFindEntities_withNullProperties() {
        UUID id = UUID.randomUUID();
        KnowledgeEntityEntity jpaEntity = KnowledgeEntityEntity.builder()
                .id(id).name("Test").entityType("CONCEPT")
                .properties(null).createdAt(Instant.now()).build();

        when(entityRepository.findByNameContainingIgnoreCase("Test"))
                .thenReturn(List.of(jpaEntity));

        List<KnowledgeEntity> result = adapter.findEntities("Test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProperties()).isEmpty();
    }
}
