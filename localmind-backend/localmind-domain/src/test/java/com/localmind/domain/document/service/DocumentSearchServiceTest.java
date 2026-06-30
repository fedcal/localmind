package com.localmind.domain.document.service;

import com.localmind.domain.document.model.SearchResult;
import com.localmind.domain.document.port.out.VectorStorePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentSearchServiceTest {

    @Mock
    private VectorStorePort vectorStorePort;

    @InjectMocks
    private DocumentSearchService documentSearchService;

    @Test
    void search_shouldDelegateToVectorStore() {
        when(vectorStorePort.search("query", 5)).thenReturn(List.of());

        documentSearchService.search("query", 5);

        verify(vectorStorePort).search("query", 5);
    }

    @Test
    void search_shouldPassQueryAndTopK() {
        String query = "machine learning concepts";
        int topK = 10;
        when(vectorStorePort.search(query, topK)).thenReturn(List.of());

        documentSearchService.search(query, topK);

        verify(vectorStorePort).search(query, topK);
    }

    @Test
    void search_shouldReturnResults() {
        List<SearchResult> expected = List.of(
                SearchResult.builder()
                        .documentId("doc-1")
                        .filename("paper.pdf")
                        .content("relevant chunk content")
                        .score(0.95)
                        .chunkIndex(0)
                        .build(),
                SearchResult.builder()
                        .documentId("doc-2")
                        .filename("notes.txt")
                        .content("another relevant chunk")
                        .score(0.87)
                        .chunkIndex(2)
                        .build()
        );
        when(vectorStorePort.search("AI", 5)).thenReturn(expected);

        List<SearchResult> result = documentSearchService.search("AI", 5);

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expected);
        assertThat(result.get(0).getScore()).isGreaterThan(result.get(1).getScore());
    }
}
