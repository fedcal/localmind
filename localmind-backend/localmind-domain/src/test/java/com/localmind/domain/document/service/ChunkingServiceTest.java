package com.localmind.domain.document.service;

import com.localmind.domain.document.model.DocumentChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChunkingServiceTest {

    private static final String DOC_ID = "doc-123";

    private ChunkingService chunkingService;

    @BeforeEach
    void setUp() {
        chunkingService = new ChunkingService(10, 3);
    }

    @Test
    void chunk_withNullText_returnsEmptyList() {
        List<DocumentChunk> result = chunkingService.chunk(DOC_ID, null);

        assertThat(result).isEmpty();
    }

    @Test
    void chunk_withEmptyText_returnsEmptyList() {
        List<DocumentChunk> result = chunkingService.chunk(DOC_ID, "");

        assertThat(result).isEmpty();
    }

    @Test
    void chunk_withTextShorterThanChunkSize_returnsSingleChunk() {
        String text = "short";

        List<DocumentChunk> result = chunkingService.chunk(DOC_ID, text);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDocumentId()).isEqualTo(DOC_ID);
        assertThat(result.get(0).getContent()).isEqualTo("short");
        assertThat(result.get(0).getChunkIndex()).isZero();
    }

    @Test
    void chunk_withTextExactlyChunkSize_returnsSingleChunk() {
        // With overlap=0, a text of exactly chunkSize should produce a single chunk
        ChunkingService noOverlapService = new ChunkingService(10, 0);
        String text = "0123456789"; // exactly 10 chars

        List<DocumentChunk> result = noOverlapService.chunk(DOC_ID, text);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("0123456789");
        assertThat(result.get(0).getChunkIndex()).isZero();
    }

    @Test
    void chunk_withLongText_returnsMultipleChunks() {
        // chunkSize=10, overlap=3 => step=7
        // 25 chars: positions 0-9, 7-16, 14-23, 21-24
        String text = "abcdefghijklmnopqrstuvwxy"; // 25 chars

        List<DocumentChunk> result = chunkingService.chunk(DOC_ID, text);

        assertThat(result).hasSizeGreaterThan(1);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getDocumentId()).isEqualTo(DOC_ID);
            assertThat(result.get(i).getChunkIndex()).isEqualTo(i);
            assertThat(result.get(i).getContent()).isNotEmpty();
        }
    }

    @Test
    void chunk_withOverlap_chunksOverlapCorrectly() {
        // chunkSize=10, overlap=3 => step=7
        // First chunk: positions 0..9
        // Second chunk: positions 7..16
        // The overlap region is positions 7..9 ("hijklmn" -> "hij" actually)
        String text = "abcdefghijklmnopqrstuvwxyz"; // 26 chars

        List<DocumentChunk> result = chunkingService.chunk(DOC_ID, text);

        assertThat(result.size()).isGreaterThanOrEqualTo(2);

        // First chunk starts at 0, length 10: "abcdefghij"
        String firstChunk = result.get(0).getContent();
        assertThat(firstChunk).isEqualTo("abcdefghij");

        // Second chunk starts at 7, length 10: "hijklmnopq"
        String secondChunk = result.get(1).getContent();
        assertThat(secondChunk).isEqualTo("hijklmnopq");

        // Verify overlap: last 3 chars of first chunk == first 3 chars of second chunk
        String overlapFromFirst = firstChunk.substring(firstChunk.length() - 3);
        String overlapFromSecond = secondChunk.substring(0, 3);
        assertThat(overlapFromFirst).isEqualTo(overlapFromSecond);
    }
}
