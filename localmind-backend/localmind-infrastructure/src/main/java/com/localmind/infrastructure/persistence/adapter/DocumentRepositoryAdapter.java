package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.DocumentMetadata;
import com.localmind.domain.document.model.DocumentStatus;
import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.infrastructure.persistence.entity.DocumentEntity;
import com.localmind.infrastructure.persistence.repository.JpaDocumentRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class DocumentRepositoryAdapter implements DocumentRepository {

    private final JpaDocumentRepository jpaRepository;

    public DocumentRepositoryAdapter(JpaDocumentRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Document save(Document document) {
        DocumentEntity entity = toEntity(document);
        DocumentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Document> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<Document> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Document> findByStatus(DocumentStatus status) {
        return jpaRepository.findByStatus(status.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Document> findByFileHash(String hash) {
        return jpaRepository.findByFileHash(hash).map(this::toDomain);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    private DocumentEntity toEntity(Document doc) {
        DocumentEntity.DocumentEntityBuilder builder = DocumentEntity.builder()
                .filename(doc.getFilename())
                .filePath(doc.getFilePath())
                .mimeType(doc.getMimeType())
                .fileSize(doc.getFileSize())
                .fileHash(doc.getFileHash())
                .status(doc.getStatus().name())
                .createdAt(doc.getCreatedAt() != null ? doc.getCreatedAt() : Instant.now())
                .updatedAt(doc.getUpdatedAt())
                .indexedAt(doc.getIndexedAt())
                .ocrConfidence(doc.getOcrConfidence());

        if (doc.getId() != null) {
            builder.id(UUID.fromString(doc.getId()));
        }

        if (doc.getMetadata() != null) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("author", doc.getMetadata().getAuthor());
            meta.put("pageCount", doc.getMetadata().getPageCount());
            meta.put("language", doc.getMetadata().getLanguage());
            if (doc.getMetadata().getCreatedDate() != null) {
                meta.put("createdDate", doc.getMetadata().getCreatedDate().toString());
            }
            builder.metadata(meta);
        }

        return builder.build();
    }

    private Document toDomain(DocumentEntity entity) {
        Document.DocumentBuilder builder = Document.builder()
                .id(entity.getId().toString())
                .filename(entity.getFilename())
                .filePath(entity.getFilePath())
                .mimeType(entity.getMimeType())
                .fileSize(entity.getFileSize() != null ? entity.getFileSize() : 0L)
                .fileHash(entity.getFileHash())
                .status(DocumentStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .indexedAt(entity.getIndexedAt())
                .ocrConfidence(entity.getOcrConfidence());

        if (entity.getMetadata() != null) {
            Map<String, Object> meta = entity.getMetadata();
            builder.metadata(DocumentMetadata.builder()
                    .author((String) meta.get("author"))
                    .pageCount(meta.get("pageCount") != null ? ((Number) meta.get("pageCount")).intValue() : 0)
                    .language((String) meta.get("language"))
                    .createdDate(meta.get("createdDate") != null ? Instant.parse((String) meta.get("createdDate")) : null)
                    .build());
        }

        return builder.build();
    }
}
