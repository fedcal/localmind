package com.localmind.domain.document.port.out;

import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.DocumentStatus;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository {
    Document save(Document document);
    Optional<Document> findById(String id);
    List<Document> findAll();
    List<Document> findByStatus(DocumentStatus status);
    Optional<Document> findByFileHash(String hash);
    void deleteById(String id);
}
