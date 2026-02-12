package com.localmind.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.common.model.PageRequest;
import com.localmind.domain.common.model.PageResponse;
import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.port.out.ConversationRepository;
import com.localmind.infrastructure.persistence.entity.ChatMessageEntity;
import com.localmind.infrastructure.persistence.entity.ConversationEntity;
import com.localmind.infrastructure.persistence.entity.ConversationTagEntity;
import com.localmind.infrastructure.persistence.repository.JpaConversationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final JpaConversationRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public ConversationRepositoryAdapter(JpaConversationRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ConversationContext save(ConversationContext conversation) {
        ConversationEntity entity;

        if (conversation.getId() != null) {
            Optional<ConversationEntity> existing = jpaRepository.findById(
                    UUID.fromString(conversation.getId()));

            if (existing.isPresent()) {
                entity = existing.get();
                entity.setTitle(conversation.getTitle());
                entity.setSystemPrompt(conversation.getSystemPrompt());
                entity.setMaxContextMessages(conversation.getMaxContextMessages());
                entity.setMetadata(serializeMetadata(conversation.getMetadata()));
                entity.setUpdatedAt(conversation.getUpdatedAt());

                // Sync tags
                syncTags(entity, conversation.getTags());

                int existingCount = entity.getMessages().size();
                List<ChatMessage> domainMessages = conversation.getMessages();

                for (int i = existingCount; i < domainMessages.size(); i++) {
                    ChatMessage msg = domainMessages.get(i);
                    ChatMessageEntity msgEntity = ChatMessageEntity.builder()
                            .role(msg.getRole().name())
                            .content(msg.getContent())
                            .metadata(serializeMetadata(msg.getMetadata()))
                            .conversation(entity)
                            .build();
                    entity.getMessages().add(msgEntity);
                }
            } else {
                entity = toEntity(conversation);
            }
        } else {
            entity = toEntity(conversation);
        }

        ConversationEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ConversationContext> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<ConversationContext> findAllOrderByUpdatedAtDesc() {
        return jpaRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<ConversationContext> findAllPaginated(PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize());
        Page<ConversationEntity> page = jpaRepository.findAllByOrderByUpdatedAtDesc(pageable);
        return toPageResponse(page);
    }

    @Override
    public PageResponse<ConversationContext> searchByContent(String query, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize());
        Page<ConversationEntity> page = jpaRepository.searchByContent(query, pageable);
        return toPageResponse(page);
    }

    @Override
    public List<ConversationContext> findByTag(String tag) {
        return jpaRepository.findByTag(tag).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    private ConversationEntity toEntity(ConversationContext domain) {
        ConversationEntity entity = ConversationEntity.builder()
                .title(domain.getTitle())
                .systemPrompt(domain.getSystemPrompt())
                .maxContextMessages(domain.getMaxContextMessages())
                .metadata(serializeMetadata(domain.getMetadata()))
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        if (domain.getMessages() != null) {
            List<ChatMessageEntity> messageEntities = domain.getMessages().stream()
                    .map(msg -> ChatMessageEntity.builder()
                            .role(msg.getRole().name())
                            .content(msg.getContent())
                            .metadata(serializeMetadata(msg.getMetadata()))
                            .conversation(entity)
                            .build())
                    .collect(Collectors.toList());
            entity.setMessages(messageEntities);
        }

        if (domain.getTags() != null) {
            List<ConversationTagEntity> tagEntities = domain.getTags().stream()
                    .map(tag -> ConversationTagEntity.builder()
                            .tag(tag)
                            .conversation(entity)
                            .build())
                    .collect(Collectors.toList());
            entity.setTags(tagEntities);
        }

        return entity;
    }

    private ConversationContext toDomain(ConversationEntity entity) {
        List<ChatMessage> messages = entity.getMessages() != null
                ? entity.getMessages().stream()
                    .map(me -> ChatMessage.builder()
                            .role(ChatMessage.Role.valueOf(me.getRole()))
                            .content(me.getContent())
                            .metadata(deserializeMetadata(me.getMetadata()))
                            .build())
                    .collect(Collectors.toList())
                : new ArrayList<>();

        Set<String> tags = entity.getTags() != null
                ? entity.getTags().stream()
                    .map(ConversationTagEntity::getTag)
                    .collect(Collectors.toSet())
                : new HashSet<>();

        return ConversationContext.builder()
                .id(entity.getId().toString())
                .title(entity.getTitle())
                .systemPrompt(entity.getSystemPrompt())
                .maxContextMessages(entity.getMaxContextMessages())
                .tags(tags)
                .metadata(deserializeMetadata(entity.getMetadata()))
                .messages(messages)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PageResponse<ConversationContext> toPageResponse(Page<ConversationEntity> page) {
        List<ConversationContext> content = page.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        return PageResponse.<ConversationContext>builder()
                .content(content)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .hasMore(page.hasNext())
                .build();
    }

    private void syncTags(ConversationEntity entity, Set<String> domainTags) {
        Set<String> tags = domainTags != null ? domainTags : new HashSet<>();

        Set<String> existingTags = entity.getTags().stream()
                .map(ConversationTagEntity::getTag)
                .collect(Collectors.toSet());

        // Remove tags not in domain
        entity.getTags().removeIf(te -> !tags.contains(te.getTag()));

        // Add new tags
        for (String tag : tags) {
            if (!existingTags.contains(tag)) {
                entity.getTags().add(ConversationTagEntity.builder()
                        .tag(tag)
                        .conversation(entity)
                        .build());
            }
        }
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Map<String, Object> deserializeMetadata(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
