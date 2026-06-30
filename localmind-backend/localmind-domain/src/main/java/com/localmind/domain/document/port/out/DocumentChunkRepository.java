package com.localmind.domain.document.port.out;

import com.localmind.domain.document.model.DocumentChunk;

import java.util.List;

public interface DocumentChunkRepository {
    DocumentChunk save(DocumentChunk chunk);
    List<DocumentChunk> saveAll(List<DocumentChunk> chunks);
    List<DocumentChunk> findByDocumentId(String documentId);
    void deleteByDocumentId(String documentId);
}
