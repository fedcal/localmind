package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.DocumentMetadata;
import com.localmind.domain.document.model.DocumentStatus;
import com.localmind.infrastructure.persistence.entity.DocumentEntity;
import com.localmind.infrastructure.persistence.repository.JpaDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentRepositoryAdapterTest {

    @Mock
    private JpaDocumentRepository jpaRepository;

    @InjectMocks
    private DocumentRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-01-15T10:30:00Z");

    @Test
    void save_shouldMapAndDelegate() {
        // given
        Document document = Document.builder()
                .id(TEST_UUID.toString())
                .filename("test.pdf")
                .filePath("/docs/test.pdf")
                .mimeType("application/pdf")
                .fileSize(1024L)
                .fileHash("abc123hash")
                .status(DocumentStatus.PENDING)
                .metadata(DocumentMetadata.builder()
                        .author("Author")
                        .pageCount(10)
                        .language("it")
                        .createdDate(NOW)
                        .build())
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        DocumentEntity savedEntity = DocumentEntity.builder()
                .id(TEST_UUID)
                .filename("test.pdf")
                .filePath("/docs/test.pdf")
                .mimeType("application/pdf")
                .fileSize(1024L)
                .fileHash("abc123hash")
                .status("PENDING")
                .metadata(Map.of(
                        "author", "Author",
                        "pageCount", 10,
                        "language", "it",
                        "createdDate", NOW.toString()
                ))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(jpaRepository.save(any(DocumentEntity.class))).thenReturn(savedEntity);

        // when
        Document result = adapter.save(document);

        // then
        ArgumentCaptor<DocumentEntity> captor = ArgumentCaptor.forClass(DocumentEntity.class);
        verify(jpaRepository).save(captor.capture());

        DocumentEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getId()).isEqualTo(TEST_UUID);
        assertThat(capturedEntity.getFilename()).isEqualTo("test.pdf");
        assertThat(capturedEntity.getFilePath()).isEqualTo("/docs/test.pdf");
        assertThat(capturedEntity.getMimeType()).isEqualTo("application/pdf");
        assertThat(capturedEntity.getFileSize()).isEqualTo(1024L);
        assertThat(capturedEntity.getFileHash()).isEqualTo("abc123hash");
        assertThat(capturedEntity.getStatus()).isEqualTo("PENDING");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getFilename()).isEqualTo("test.pdf");
        assertThat(result.getStatus()).isEqualTo(DocumentStatus.PENDING);
        assertThat(result.getMetadata()).isNotNull();
        assertThat(result.getMetadata().getAuthor()).isEqualTo("Author");
        assertThat(result.getMetadata().getPageCount()).isEqualTo(10);
        assertThat(result.getMetadata().getLanguage()).isEqualTo("it");
    }

    @Test
    void findById_shouldReturnMappedDocument() {
        // given
        DocumentEntity entity = DocumentEntity.builder()
                .id(TEST_UUID)
                .filename("report.docx")
                .filePath("/docs/report.docx")
                .mimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .fileSize(2048L)
                .fileHash("def456hash")
                .status("INDEXED")
                .createdAt(NOW)
                .build();

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        // when
        Optional<Document> result = adapter.findById(TEST_UUID.toString());

        // then
        assertThat(result).isPresent();
        Document doc = result.get();
        assertThat(doc.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(doc.getFilename()).isEqualTo("report.docx");
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.INDEXED);
        assertThat(doc.getFileSize()).isEqualTo(2048L);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        // given
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        // when
        Optional<Document> result = adapter.findById(TEST_UUID.toString());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnMappedDocuments() {
        // given
        DocumentEntity entity1 = DocumentEntity.builder()
                .id(TEST_UUID)
                .filename("file1.pdf")
                .filePath("/docs/file1.pdf")
                .mimeType("application/pdf")
                .fileSize(512L)
                .fileHash("hash1")
                .status("PENDING")
                .createdAt(NOW)
                .build();

        UUID secondUuid = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
        DocumentEntity entity2 = DocumentEntity.builder()
                .id(secondUuid)
                .filename("file2.txt")
                .filePath("/docs/file2.txt")
                .mimeType("text/plain")
                .fileSize(256L)
                .fileHash("hash2")
                .status("INDEXED")
                .createdAt(NOW)
                .build();

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        // when
        List<Document> result = adapter.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFilename()).isEqualTo("file1.pdf");
        assertThat(result.get(0).getStatus()).isEqualTo(DocumentStatus.PENDING);
        assertThat(result.get(1).getFilename()).isEqualTo("file2.txt");
        assertThat(result.get(1).getStatus()).isEqualTo(DocumentStatus.INDEXED);
    }

    @Test
    void deleteById_shouldDelegateWithUuid() {
        // when
        adapter.deleteById(TEST_UUID.toString());

        // then
        verify(jpaRepository).deleteById(TEST_UUID);
    }
}
