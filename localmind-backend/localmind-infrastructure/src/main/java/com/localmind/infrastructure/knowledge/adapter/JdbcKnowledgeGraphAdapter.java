package com.localmind.infrastructure.knowledge.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.knowledge.model.*;
import com.localmind.domain.knowledge.port.out.KnowledgeGraphPort;
import com.localmind.infrastructure.knowledge.persistence.entity.KnowledgeEntityEntity;
import com.localmind.infrastructure.knowledge.persistence.entity.KnowledgeRelationEntity;
import com.localmind.infrastructure.knowledge.persistence.repository.KnowledgeEntityJpaRepository;
import com.localmind.infrastructure.knowledge.persistence.repository.KnowledgeRelationJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "localmind.knowledge-graph.enabled", havingValue = "true")
public class JdbcKnowledgeGraphAdapter implements KnowledgeGraphPort {

    private final KnowledgeEntityJpaRepository entityRepository;
    private final KnowledgeRelationJpaRepository relationRepository;
    private final ObjectMapper objectMapper;

    public JdbcKnowledgeGraphAdapter(KnowledgeEntityJpaRepository entityRepository,
                                     KnowledgeRelationJpaRepository relationRepository) {
        this.entityRepository = entityRepository;
        this.relationRepository = relationRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Transactional
    public void storeEntities(List<KnowledgeEntity> entities) {
        List<KnowledgeEntityEntity> jpaEntities = entities.stream()
                .map(this::toJpaEntity)
                .toList();
        entityRepository.saveAll(jpaEntities);
    }

    @Override
    @Transactional
    public void storeRelations(List<KnowledgeRelation> relations) {
        List<KnowledgeRelationEntity> jpaRelations = relations.stream()
                .map(this::toJpaRelation)
                .toList();
        relationRepository.saveAll(jpaRelations);
    }

    @Override
    public List<KnowledgeEntity> findEntities(String query) {
        return entityRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::toDomainEntity)
                .toList();
    }

    @Override
    public List<KnowledgeRelation> findRelations(String entityId) {
        return relationRepository.findByFromEntityIdOrToEntityId(entityId, entityId).stream()
                .map(this::toDomainRelation)
                .toList();
    }

    @Override
    public KnowledgeSubgraph getSubgraph(String entityId, int depth) {
        Set<String> visitedEntityIds = new HashSet<>();
        List<KnowledgeEntity> allEntities = new ArrayList<>();
        List<KnowledgeRelation> allRelations = new ArrayList<>();

        collectSubgraph(entityId, depth, visitedEntityIds, allEntities, allRelations);

        return KnowledgeSubgraph.builder()
                .entities(allEntities)
                .relations(allRelations)
                .build();
    }

    private void collectSubgraph(String entityId, int remainingDepth,
                                  Set<String> visitedEntityIds,
                                  List<KnowledgeEntity> allEntities,
                                  List<KnowledgeRelation> allRelations) {
        if (remainingDepth <= 0 || visitedEntityIds.contains(entityId)) {
            return;
        }
        visitedEntityIds.add(entityId);

        entityRepository.findById(UUID.fromString(entityId))
                .ifPresent(entity -> allEntities.add(toDomainEntity(entity)));

        List<KnowledgeRelation> relations = findRelations(entityId);
        allRelations.addAll(relations);

        for (KnowledgeRelation relation : relations) {
            String nextEntityId = relation.getFromEntityId().equals(entityId)
                    ? relation.getToEntityId()
                    : relation.getFromEntityId();
            collectSubgraph(nextEntityId, remainingDepth - 1, visitedEntityIds, allEntities, allRelations);
        }
    }

    @Override
    public KnowledgeGraphStats getStats() {
        long totalEntities = entityRepository.count();
        long totalRelations = relationRepository.count();

        Map<EntityType, Long> entitiesByType = new EnumMap<>(EntityType.class);
        List<Object[]> counts = entityRepository.countByEntityType();
        for (Object[] row : counts) {
            try {
                EntityType type = EntityType.valueOf((String) row[0]);
                entitiesByType.put(type, (Long) row[1]);
            } catch (IllegalArgumentException ignored) {
                // Skip unknown entity types
            }
        }

        return KnowledgeGraphStats.builder()
                .totalEntities(totalEntities)
                .totalRelations(totalRelations)
                .entitiesByType(entitiesByType)
                .build();
    }

    @Override
    @Transactional
    public void deleteEntity(String entityId) {
        relationRepository.deleteByFromEntityIdOrToEntityId(entityId, entityId);
        entityRepository.deleteById(UUID.fromString(entityId));
    }

    private KnowledgeEntityEntity toJpaEntity(KnowledgeEntity entity) {
        UUID id = entity.getId() != null ? UUID.fromString(entity.getId()) : UUID.randomUUID();
        return KnowledgeEntityEntity.builder()
                .id(id)
                .name(entity.getName())
                .entityType(entity.getType().name())
                .properties(serializeProperties(entity.getProperties()))
                .build();
    }

    private KnowledgeRelationEntity toJpaRelation(KnowledgeRelation relation) {
        UUID id = relation.getId() != null ? UUID.fromString(relation.getId()) : UUID.randomUUID();
        return KnowledgeRelationEntity.builder()
                .id(id)
                .fromEntityId(relation.getFromEntityId())
                .toEntityId(relation.getToEntityId())
                .relationType(relation.getType().name())
                .properties(serializeProperties(relation.getProperties()))
                .build();
    }

    private KnowledgeEntity toDomainEntity(KnowledgeEntityEntity entity) {
        return KnowledgeEntity.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .type(EntityType.valueOf(entity.getEntityType()))
                .properties(deserializeProperties(entity.getProperties()))
                .build();
    }

    private KnowledgeRelation toDomainRelation(KnowledgeRelationEntity relation) {
        return KnowledgeRelation.builder()
                .id(relation.getId().toString())
                .fromEntityId(relation.getFromEntityId())
                .toEntityId(relation.getToEntityId())
                .type(RelationType.valueOf(relation.getRelationType()))
                .properties(deserializeProperties(relation.getProperties()))
                .build();
    }

    private String serializeProperties(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Map<String, String> deserializeProperties(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }
}
