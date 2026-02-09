package com.localmind.domain.document.service;

import com.localmind.domain.document.model.DocumentChunk;

import java.util.ArrayList;
import java.util.List;

public class ChunkingService {

    private final int chunkSize;
    private final int chunkOverlap;

    public ChunkingService(int chunkSize, int chunkOverlap) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public List<DocumentChunk> chunk(String documentId, String text) {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int start = 0;
        int index = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String chunkContent = text.substring(start, end);

            chunks.add(DocumentChunk.builder()
                    .documentId(documentId)
                    .content(chunkContent)
                    .chunkIndex(index++)
                    .build());

            start += chunkSize - chunkOverlap;
        }
        return chunks;
    }
}
