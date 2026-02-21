package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.document.model.DocumentChunk;
import com.localmind.domain.document.port.out.DocumentChunkRepository;
import com.localmind.infrastructure.persistence.entity.DocumentChunkEntity;
import com.localmind.infrastructure.persistence.repository.JpaDocumentChunkRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class DocumentChunkRepositoryAdapter implements DocumentChunkRepository {

    private final JpaDocumentChunkRepository jpaRepository;

    public DocumentChunkRepositoryAdapter(JpaDocumentChunkRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DocumentChunk save(DocumentChunk chunk) {
        DocumentChunkEntity entity = toEntity(chunk);
        DocumentChunkEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<DocumentChunk> saveAll(List<DocumentChunk> chunks) {
        List<DocumentChunkEntity> entities = chunks.stream().map(this::toEntity).toList();
        List<DocumentChunkEntity> saved = jpaRepository.saveAll(entities);
        return saved.stream().map(this::toDomain).toList();
    }

    @Override
    public List<DocumentChunk> findByDocumentId(String documentId) {
        return jpaRepository.findByDocumentId(documentId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByDocumentId(String documentId) {
        jpaRepository.deleteByDocumentId(documentId);
    }

    private DocumentChunkEntity toEntity(DocumentChunk chunk) {
        DocumentChunkEntity.DocumentChunkEntityBuilder builder = DocumentChunkEntity.builder()
                .documentId(chunk.getDocumentId())
                .content(chunk.getContent())
                .chunkIndex(chunk.getChunkIndex());

        if (chunk.getId() != null) {
            builder.id(UUID.fromString(chunk.getId()));
        }

        return builder.build();
    }

    private DocumentChunk toDomain(DocumentChunkEntity entity) {
        return DocumentChunk.builder()
                .id(entity.getId().toString())
                .documentId(entity.getDocumentId())
                .content(entity.getContent())
                .chunkIndex(entity.getChunkIndex())
                .build();
    }
}
