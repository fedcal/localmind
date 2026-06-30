package com.localmind.domain.document.port.out;

import com.localmind.domain.document.model.DocumentChunk;
import com.localmind.domain.document.model.SearchResult;

import java.util.List;

public interface VectorStorePort {
    void store(List<DocumentChunk> chunks);
    List<SearchResult> search(String query, int topK);
    void deleteByDocumentId(String documentId);
}
