package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.CodeSnippet;
import com.localmind.domain.mcp.port.out.SnippetRepository;
import com.localmind.infrastructure.persistence.entity.mcp.SnippetEntity;
import com.localmind.infrastructure.persistence.entity.mcp.SnippetTagEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaSnippetRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SnippetRepositoryAdapter implements SnippetRepository {

    private final JpaSnippetRepository jpaRepository;

    public SnippetRepositoryAdapter(JpaSnippetRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CodeSnippet save(CodeSnippet snippet) {
        SnippetEntity entity = toEntity(snippet);
        SnippetEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<CodeSnippet> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<CodeSnippet> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CodeSnippet> searchByKeyword(String keyword) {
        return jpaRepository.searchByKeyword(keyword).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CodeSnippet> findByTag(String tag) {
        return jpaRepository.findByTag(tag).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CodeSnippet> findByLanguage(String language) {
        return jpaRepository.findByLanguage(language).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        UUID uuid = UUID.fromString(id);
        if (jpaRepository.existsById(uuid)) {
            jpaRepository.deleteById(uuid);
            return true;
        }
        return false;
    }

    private SnippetEntity toEntity(CodeSnippet domain) {
        SnippetEntity entity = SnippetEntity.builder()
                .title(domain.getTitle())
                .code(domain.getCode())
                .language(domain.getLanguage())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        if (domain.getTags() != null) {
            List<SnippetTagEntity> tagEntities = domain.getTags().stream()
                    .map(tag -> SnippetTagEntity.builder()
                            .tag(tag)
                            .snippet(entity)
                            .build())
                    .collect(Collectors.toList());
            entity.setTags(tagEntities);
        }

        return entity;
    }

    private CodeSnippet toDomain(SnippetEntity entity) {
        Set<String> tags = entity.getTags() != null
                ? entity.getTags().stream()
                    .map(SnippetTagEntity::getTag)
                    .collect(Collectors.toSet())
                : new HashSet<>();

        return CodeSnippet.builder()
                .id(entity.getId().toString())
                .title(entity.getTitle())
                .code(entity.getCode())
                .language(entity.getLanguage())
                .description(entity.getDescription())
                .tags(tags)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
