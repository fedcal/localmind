package com.localmind.infrastructure.vectorstore.adapter;

import com.localmind.domain.document.model.DocumentChunk;
import com.localmind.domain.document.model.SearchResult;
import com.localmind.domain.document.port.out.VectorStorePort;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class QdrantVectorStoreAdapter implements VectorStorePort {

    private final VectorStore vectorStore;

    public QdrantVectorStoreAdapter(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void store(List<DocumentChunk> chunks) {
        List<Document> documents = chunks.stream()
                .map(chunk -> new Document(
                        chunk.getId(),
                        chunk.getContent(),
                        Map.of(
                                "documentId", chunk.getDocumentId(),
                                "chunkIndex", chunk.getChunkIndex()
                        )))
                .toList();
        vectorStore.add(documents);
    }

    @Override
    public List<SearchResult> search(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();

        List<Document> results = vectorStore.similaritySearch(request);

        return results.stream()
                .map(doc -> SearchResult.builder()
                        .documentId(getMetadataString(doc.getMetadata(), "documentId"))
                        .filename(getMetadataString(doc.getMetadata(), "filename"))
                        .content(doc.getText())
                        .score(doc.getScore() != null ? doc.getScore() : 0.0)
                        .chunkIndex(getMetadataInt(doc.getMetadata(), "chunkIndex"))
                        .build())
                .toList();
    }

    @Override
    public void deleteByDocumentId(String documentId) {
        vectorStore.delete(List.of(documentId));
    }

    private String getMetadataString(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value != null ? value.toString() : null;
    }

    private int getMetadataInt(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
}
