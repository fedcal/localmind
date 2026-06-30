package com.localmind.domain.document.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.port.out.DocumentChunkRepository;
import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.domain.document.port.out.VectorStorePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private VectorStorePort vectorStorePort;

    @Mock
    private DocumentChunkRepository documentChunkRepository;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void findById_shouldReturnDocument() {
        Document doc = Document.builder().id("doc-1").filename("file.txt").build();
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));

        Document result = documentService.findById("doc-1");

        assertThat(result).isEqualTo(doc);
        verify(documentRepository).findById("doc-1");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(documentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.findById("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void findAll_shouldReturnAllDocuments() {
        List<Document> docs = List.of(
                Document.builder().id("1").filename("a.txt").build(),
                Document.builder().id("2").filename("b.txt").build()
        );
        when(documentRepository.findAll()).thenReturn(docs);

        List<Document> result = documentService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(docs);
    }

    @Test
    void deleteById_shouldCascadeDeleteInOrder() {
        Document doc = Document.builder().id("doc-1").filename("file.txt").build();
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));

        documentService.deleteById("doc-1");

        InOrder inOrder = inOrder(vectorStorePort, documentChunkRepository, documentRepository);
        inOrder.verify(vectorStorePort).deleteByDocumentId("doc-1");
        inOrder.verify(documentChunkRepository).deleteByDocumentId("doc-1");
        inOrder.verify(documentRepository).deleteById("doc-1");
    }

    @Test
    void deleteById_shouldThrowWhenNotFound() {
        when(documentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.deleteById("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");

        verify(vectorStorePort, never()).deleteByDocumentId(any());
        verify(documentChunkRepository, never()).deleteByDocumentId(any());
        verify(documentRepository, never()).deleteById(any());
    }
}
