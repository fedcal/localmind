package com.localmind.domain.document.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.DocumentStatus;
import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.domain.document.port.out.FolderConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FolderConfigRepository folderConfigRepository;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void ingest_shouldSaveDocumentWithPendingStatus() {
        InputStream content = new ByteArrayInputStream("test content".getBytes());
        Document savedDoc = Document.builder()
                .id("generated-id")
                .filename("test.pdf")
                .mimeType("application/pdf")
                .status(DocumentStatus.PENDING)
                .build();
        when(documentRepository.save(any(Document.class))).thenReturn(savedDoc);

        Document result = documentService.ingest("test.pdf", content, "application/pdf");

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(DocumentStatus.PENDING);
        assertThat(result.getStatus()).isEqualTo(DocumentStatus.PENDING);
    }

    @Test
    void ingest_shouldSetFilenameAndMimeType() {
        InputStream content = new ByteArrayInputStream("data".getBytes());
        Document savedDoc = Document.builder()
                .id("id-1")
                .filename("report.docx")
                .mimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .status(DocumentStatus.PENDING)
                .build();
        when(documentRepository.save(any(Document.class))).thenReturn(savedDoc);

        documentService.ingest("report.docx",
                content,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());
        Document captured = captor.getValue();
        assertThat(captured.getFilename()).isEqualTo("report.docx");
        assertThat(captured.getMimeType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

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
    void deleteById_shouldCallRepository() {
        documentService.deleteById("doc-to-delete");

        verify(documentRepository).deleteById("doc-to-delete");
    }

    @Test
    void ingestFromFolder_shouldThrowWhenFolderNotFound() {
        when(folderConfigRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.ingestFromFolder("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }
}
