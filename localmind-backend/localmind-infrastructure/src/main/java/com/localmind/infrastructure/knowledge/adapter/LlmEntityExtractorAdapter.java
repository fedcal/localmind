package com.localmind.infrastructure.knowledge.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.knowledge.model.EntityType;
import com.localmind.domain.knowledge.model.KnowledgeEntity;
import com.localmind.domain.knowledge.model.KnowledgeRelation;
import com.localmind.domain.knowledge.model.RelationType;
import com.localmind.domain.knowledge.port.out.EntityExtractorPort;
import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.LlmRequest;
import com.localmind.domain.llm.model.LlmResponse;
import com.localmind.domain.llm.port.out.LlmClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@ConditionalOnProperty(name = "localmind.knowledge-graph.enabled", havingValue = "true")
public class LlmEntityExtractorAdapter implements EntityExtractorPort {

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public LlmEntityExtractorAdapter(List<LlmClient> clients) {
        this.llmClient = clients.isEmpty() ? null : clients.get(0);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<KnowledgeEntity> extractEntities(String text) {
        if (llmClient == null || text == null || text.isBlank()) {
            return List.of();
        }

        String prompt = buildEntityExtractionPrompt(text);

        try {
            LlmRequest request = LlmRequest.builder()
                    .messages(List.of(ChatMessage.builder()
                            .role(ChatMessage.Role.USER)
                            .content(prompt)
                            .build()))
                    .temperature(0.1)
                    .build();

            LlmResponse response = llmClient.call(request);
            String content = response.getContent();

            return parseEntitiesFromJson(content);
        } catch (Exception e) {
            log.warn("Failed to extract entities from text: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<KnowledgeRelation> extractRelations(String text, List<KnowledgeEntity> entities) {
        if (llmClient == null || text == null || text.isBlank() || entities == null || entities.isEmpty()) {
            return List.of();
        }

        String prompt = buildRelationExtractionPrompt(text, entities);

        try {
            LlmRequest request = LlmRequest.builder()
                    .messages(List.of(ChatMessage.builder()
                            .role(ChatMessage.Role.USER)
                            .content(prompt)
                            .build()))
                    .temperature(0.1)
                    .build();

            LlmResponse response = llmClient.call(request);
            String content = response.getContent();

            return parseRelationsFromJson(content);
        } catch (Exception e) {
            log.warn("Failed to extract relations from text: {}", e.getMessage());
            return List.of();
        }
    }

    private String buildEntityExtractionPrompt(String text) {
        return "Extract named entities from the following text and return them as a JSON array. " +
                "Each entity should have: \"id\" (UUID string), \"name\" (entity name), " +
                "\"type\" (one of: PERSON, ORGANIZATION, PLACE, CONCEPT, EVENT, TECHNOLOGY, DOCUMENT), " +
                "\"properties\" (object with additional key-value pairs, can be empty {}). " +
                "Return ONLY the JSON array, no other text.\n\n" +
                "Text:\n" + text;
    }

    private String buildRelationExtractionPrompt(String text, List<KnowledgeEntity> entities) {
        StringBuilder entitiesInfo = new StringBuilder();
        for (KnowledgeEntity entity : entities) {
            entitiesInfo.append("- ").append(entity.getName())
                    .append(" (id: ").append(entity.getId())
                    .append(", type: ").append(entity.getType()).append(")\n");
        }

        return "Given the following entities:\n" + entitiesInfo +
                "\nExtract relationships between these entities from the text below. " +
                "Return a JSON array of relationships. Each relationship should have: " +
                "\"id\" (UUID string), \"fromEntityId\" (source entity id), " +
                "\"toEntityId\" (target entity id), " +
                "\"type\" (one of: WORKS_AT, LOCATED_IN, RELATED_TO, PART_OF, CREATED_BY, DEPENDS_ON, REFERENCES, MENTIONED_IN), " +
                "\"properties\" (object with additional key-value pairs, can be empty {}). " +
                "Return ONLY the JSON array, no other text.\n\n" +
                "Text:\n" + text;
    }

    List<KnowledgeEntity> parseEntitiesFromJson(String content) {
        try {
            String json = extractJsonArray(content);
            List<Map<String, Object>> rawEntities = objectMapper.readValue(json,
                    new TypeReference<>() {});

            List<KnowledgeEntity> entities = new ArrayList<>();
            for (Map<String, Object> raw : rawEntities) {
                try {
                    String id = raw.getOrDefault("id", UUID.randomUUID().toString()).toString();
                    String name = (String) raw.get("name");
                    EntityType type = EntityType.valueOf((String) raw.get("type"));

                    @SuppressWarnings("unchecked")
                    Map<String, String> properties = raw.containsKey("properties")
                            ? convertToStringMap((Map<String, Object>) raw.get("properties"))
                            : Map.of();

                    entities.add(KnowledgeEntity.builder()
                            .id(id)
                            .name(name)
                            .type(type)
                            .properties(properties)
                            .build());
                } catch (Exception e) {
                    log.debug("Skipping invalid entity entry: {}", e.getMessage());
                }
            }
            return entities;
        } catch (Exception e) {
            log.warn("Failed to parse entities JSON: {}", e.getMessage());
            return List.of();
        }
    }

    List<KnowledgeRelation> parseRelationsFromJson(String content) {
        try {
            String json = extractJsonArray(content);
            List<Map<String, Object>> rawRelations = objectMapper.readValue(json,
                    new TypeReference<>() {});

            List<KnowledgeRelation> relations = new ArrayList<>();
            for (Map<String, Object> raw : rawRelations) {
                try {
                    String id = raw.getOrDefault("id", UUID.randomUUID().toString()).toString();
                    String fromEntityId = (String) raw.get("fromEntityId");
                    String toEntityId = (String) raw.get("toEntityId");
                    RelationType type = RelationType.valueOf((String) raw.get("type"));

                    @SuppressWarnings("unchecked")
                    Map<String, String> properties = raw.containsKey("properties")
                            ? convertToStringMap((Map<String, Object>) raw.get("properties"))
                            : Map.of();

                    relations.add(KnowledgeRelation.builder()
                            .id(id)
                            .fromEntityId(fromEntityId)
                            .toEntityId(toEntityId)
                            .type(type)
                            .properties(properties)
                            .build());
                } catch (Exception e) {
                    log.debug("Skipping invalid relation entry: {}", e.getMessage());
                }
            }
            return relations;
        } catch (Exception e) {
            log.warn("Failed to parse relations JSON: {}", e.getMessage());
            return List.of();
        }
    }

    private String extractJsonArray(String content) {
        int start = content.indexOf('[');
        int end = content.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }

    private Map<String, String> convertToStringMap(Map<String, Object> map) {
        if (map == null) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return result;
    }
}
