package com.localmind.domain.document.service;

import com.localmind.domain.common.exception.DocumentProcessingException;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.DocumentChunk;
import com.localmind.domain.document.model.DocumentStatus;
import com.localmind.domain.document.model.FolderConfig;
import com.localmind.domain.document.model.OcrResult;
import com.localmind.domain.document.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionPipelineServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentChunkRepository documentChunkRepository;

    @Mock
    private TextExtractorPort textExtractorPort;

    @Mock
    private ChunkingService chunkingService;

    @Mock
    private VectorStorePort vectorStorePort;

    @Mock
    private FileSystemScannerPort fileSystemScannerPort;

    @Mock
    private FolderConfigRepository folderConfigRepository;

    private DocumentIngestionPipelineService service;

    private static final String FILENAME = "test.pdf";
    private static final String MIME_TYPE = "application/pdf";
    private static final long FILE_SIZE = 1024L;
    private static final String FILE_HASH = "abc123hash";

    @BeforeEach
    void setUp() {
        // Costruisce il servizio senza OCR (Optional.empty) per i test base
        // Build service without OCR (Optional.empty) for base tests
        service = new DocumentIngestionPipelineService(
                documentRepository, documentChunkRepository, textExtractorPort,
                chunkingService, vectorStorePort, fileSystemScannerPort,
                folderConfigRepository, Optional.empty());
    }

    private InputStream createTestStream() {
        return new ByteArrayInputStream("test content".getBytes());
    }

    @Test
    void ingestFile_shouldDeduplicateByHash_returnsExistingIndexed() {
        Document existing = Document.builder()
                .id("doc-1")
                .filename(FILENAME)
                .fileHash(FILE_HASH)
                .status(DocumentStatus.INDEXED)
                .build();
        when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.of(existing));

        Document result = service.ingestFile(FILENAME, createTestStream(), MIME_TYPE, FILE_SIZE, FILE_HASH);

        assertThat(result).isEqualTo(existing);
        assertThat(result.getStatus()).isEqualTo(DocumentStatus.INDEXED);
        verify(documentRepository).findByFileHash(FILE_HASH);
        verify(documentRepository, never()).save(any());
        verify(textExtractorPort, never()).extract(any(), any());
    }

    @Test
    void ingestFile_shouldSaveDocumentAsPending() {
        when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());

        // Track statuses at time of each save (ArgumentCaptor captures by reference)
        List<DocumentStatus> savedStatuses = new ArrayList<>();
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            savedStatuses.add(doc.getStatus());
            if (doc.getId() == null) {
                doc.setId("generated-id");
            }
            return doc;
        });
        when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn("extracted text");
        when(chunkingService.chunk(any(), eq("extracted text"))).thenReturn(List.of());

        Document result = service.ingestFile(FILENAME, createTestStream(), MIME_TYPE, FILE_SIZE, FILE_HASH);

        // First save should be PENDING, then PROCESSING, then INDEXED
        assertThat(savedStatuses.get(0)).isEqualTo(DocumentStatus.PENDING);
        assertThat(savedStatuses.get(1)).isEqualTo(DocumentStatus.PROCESSING);
        assertThat(savedStatuses.get(2)).isEqualTo(DocumentStatus.INDEXED);
        assertThat(result.getFilename()).isEqualTo(FILENAME);
        assertThat(result.getMimeType()).isEqualTo(MIME_TYPE);
        assertThat(result.getFileSize()).isEqualTo(FILE_SIZE);
        assertThat(result.getFileHash()).isEqualTo(FILE_HASH);
    }

    @Test
    void ingestFile_shouldExtractTextAndChunk() {
        when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            if (doc.getId() == null) doc.setId("doc-1");
            return doc;
        });
        when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn("extracted text");
        when(chunkingService.chunk("doc-1", "extracted text")).thenReturn(List.of());

        service.ingestFile(FILENAME, createTestStream(), MIME_TYPE, FILE_SIZE, FILE_HASH);

        verify(textExtractorPort).extract(any(InputStream.class), eq(MIME_TYPE));
        verify(chunkingService).chunk("doc-1", "extracted text");
    }

    @Test
    void ingestFile_shouldSaveChunksToRepository() {
        when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            if (doc.getId() == null) doc.setId("doc-1");
            return doc;
        });
        when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn("some text");

        DocumentChunk chunk1 = DocumentChunk.builder().documentId("doc-1").content("chunk1").chunkIndex(0).build();
        DocumentChunk chunk2 = DocumentChunk.builder().documentId("doc-1").content("chunk2").chunkIndex(1).build();
        when(chunkingService.chunk("doc-1", "some text")).thenReturn(List.of(chunk1, chunk2));

        service.ingestFile(FILENAME, createTestStream(), MIME_TYPE, FILE_SIZE, FILE_HASH);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentChunk>> captor = ArgumentCaptor.forClass(List.class);
        verify(documentChunkRepository).saveAll(captor.capture());
        List<DocumentChunk> savedChunks = captor.getValue();
        assertThat(savedChunks).hasSize(2);
        assertThat(savedChunks.get(0).getId()).isNotNull();
        assertThat(savedChunks.get(1).getId()).isNotNull();
        assertThat(savedChunks).allMatch(c -> FILENAME.equals(c.getFilename()));
    }

    @Test
    void ingestFile_shouldStoreChunksInVectorStore() {
        when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            if (doc.getId() == null) doc.setId("doc-1");
            return doc;
        });
        when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn("text");

        DocumentChunk chunk = DocumentChunk.builder().documentId("doc-1").content("text").chunkIndex(0).build();
        when(chunkingService.chunk("doc-1", "text")).thenReturn(List.of(chunk));

        service.ingestFile(FILENAME, createTestStream(), MIME_TYPE, FILE_SIZE, FILE_HASH);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentChunk>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStorePort).store(captor.capture());
        List<DocumentChunk> storedChunks = captor.getValue();
        assertThat(storedChunks).hasSize(1);
        assertThat(storedChunks.get(0).getFilename()).isEqualTo(FILENAME);
    }

    @Test
    void ingestFile_shouldPropagateFilenameToAllChunks() {
        when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            if (doc.getId() == null) doc.setId("doc-1");
            return doc;
        });
        when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn("text content here");

        DocumentChunk chunk1 = DocumentChunk.builder().documentId("doc-1").content("text").chunkIndex(0).build();
        DocumentChunk chunk2 = DocumentChunk.builder().documentId("doc-1").content("content").chunkIndex(1).build();
        DocumentChunk chunk3 = DocumentChunk.builder().documentId("doc-1").content("here").chunkIndex(2).build();
        when(chunkingService.chunk("doc-1", "text content here")).thenReturn(List.of(chunk1, chunk2, chunk3));

        service.ingestFile(FILENAME, createTestStream(), MIME_TYPE, FILE_SIZE, FILE_HASH);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentChunk>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStorePort).store(captor.capture());
        List<DocumentChunk> storedChunks = captor.getValue();

        assertThat(storedChunks).hasSize(3);
        assertThat(storedChunks).allMatch(c -> FILENAME.equals(c.getFilename()));
        assertThat(storedChunks).allMatch(c -> c.getId() != null);
    }

    @Test
    void ingestFile_shouldSetStatusToIndexedOnSuccess() {
        when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            if (doc.getId() == null) doc.setId("doc-1");
            return doc;
        });
        when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn("text");
        when(chunkingService.chunk("doc-1", "text")).thenReturn(List.of());

        Document result = service.ingestFile(FILENAME, createTestStream(), MIME_TYPE, FILE_SIZE, FILE_HASH);

        assertThat(result.getStatus()).isEqualTo(DocumentStatus.INDEXED);
        assertThat(result.getIndexedAt()).isNotNull();
    }

    @Test
    void ingestFile_shouldSetStatusToFailedOnExtractError() {
        when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            if (doc.getId() == null) doc.setId("doc-1");
            return doc;
        });
        when(textExtractorPort.extract(any(), eq(MIME_TYPE)))
                .thenThrow(new RuntimeException("Extraction failed"));

        assertThatThrownBy(() -> service.ingestFile(FILENAME, createTestStream(), MIME_TYPE, FILE_SIZE, FILE_HASH))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("Failed to ingest file")
                .hasMessageContaining(FILENAME);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, atLeastOnce()).save(captor.capture());
        Document lastSaved = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(lastSaved.getStatus()).isEqualTo(DocumentStatus.FAILED);
    }

    @Test
    void ingestFile_shouldSetStatusToFailedOnVectorStoreError() {
        when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            if (doc.getId() == null) doc.setId("doc-1");
            return doc;
        });
        when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn("text");
        when(chunkingService.chunk("doc-1", "text")).thenReturn(List.of(
                DocumentChunk.builder().documentId("doc-1").content("text").chunkIndex(0).build()
        ));
        doThrow(new RuntimeException("Vector store unavailable")).when(vectorStorePort).store(anyList());

        assertThatThrownBy(() -> service.ingestFile(FILENAME, createTestStream(), MIME_TYPE, FILE_SIZE, FILE_HASH))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("Failed to ingest file");

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, atLeastOnce()).save(captor.capture());
        Document lastSaved = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(lastSaved.getStatus()).isEqualTo(DocumentStatus.FAILED);
    }

    @Test
    void ingestFromFolder_shouldScanAndIngestFiles() throws IOException {
        String folderId = "folder-1";
        FolderConfig config = FolderConfig.builder()
                .id(folderId)
                .path("/test/path")
                .recursive(true)
                .build();
        when(folderConfigRepository.findById(folderId)).thenReturn(Optional.of(config));

        FileSystemScannerPort.FileInfo fileInfo = new FileSystemScannerPort.FileInfo(
                Path.of("/test/path/doc.pdf"), "application/pdf", 500L, "hash1");
        when(fileSystemScannerPort.scan("/test/path", true)).thenReturn(List.of(fileInfo));
        when(fileSystemScannerPort.openFile(fileInfo.path())).thenReturn(createTestStream());

        when(documentRepository.findByFileHash("hash1")).thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            if (doc.getId() == null) doc.setId("doc-new");
            return doc;
        });
        when(textExtractorPort.extract(any(), eq("application/pdf"))).thenReturn("pdf text");
        when(chunkingService.chunk("doc-new", "pdf text")).thenReturn(List.of());

        List<Document> result = service.ingestFromFolder(folderId);

        assertThat(result).hasSize(1);
        verify(fileSystemScannerPort).scan("/test/path", true);
    }

    @Test
    void ingestFromFolder_shouldDeduplicateByHash() throws IOException {
        String folderId = "folder-1";
        FolderConfig config = FolderConfig.builder()
                .id(folderId)
                .path("/test/path")
                .recursive(false)
                .build();
        when(folderConfigRepository.findById(folderId)).thenReturn(Optional.of(config));

        FileSystemScannerPort.FileInfo fileInfo = new FileSystemScannerPort.FileInfo(
                Path.of("/test/path/doc.pdf"), "application/pdf", 500L, "existing-hash");
        when(fileSystemScannerPort.scan("/test/path", false)).thenReturn(List.of(fileInfo));

        Document existingDoc = Document.builder()
                .id("doc-existing")
                .fileHash("existing-hash")
                .status(DocumentStatus.INDEXED)
                .build();
        when(documentRepository.findByFileHash("existing-hash")).thenReturn(Optional.of(existingDoc));

        List<Document> result = service.ingestFromFolder(folderId);

        assertThat(result).isEmpty();
        verify(fileSystemScannerPort, never()).openFile(any());
    }

    @Test
    void ingestFromFolder_shouldContinueOnFileError() throws IOException {
        String folderId = "folder-1";
        FolderConfig config = FolderConfig.builder()
                .id(folderId)
                .path("/test/path")
                .recursive(true)
                .build();
        when(folderConfigRepository.findById(folderId)).thenReturn(Optional.of(config));

        FileSystemScannerPort.FileInfo failFile = new FileSystemScannerPort.FileInfo(
                Path.of("/test/path/bad.pdf"), "application/pdf", 100L, "hash-fail");
        FileSystemScannerPort.FileInfo goodFile = new FileSystemScannerPort.FileInfo(
                Path.of("/test/path/good.pdf"), "application/pdf", 200L, "hash-good");
        when(fileSystemScannerPort.scan("/test/path", true)).thenReturn(List.of(failFile, goodFile));

        // First file: IO error on open
        when(documentRepository.findByFileHash("hash-fail")).thenReturn(Optional.empty());
        when(fileSystemScannerPort.openFile(failFile.path())).thenThrow(new IOException("Cannot read"));

        // Second file: succeeds
        when(documentRepository.findByFileHash("hash-good")).thenReturn(Optional.empty());
        when(fileSystemScannerPort.openFile(goodFile.path())).thenReturn(createTestStream());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            if (doc.getId() == null) doc.setId("doc-good");
            return doc;
        });
        when(textExtractorPort.extract(any(), eq("application/pdf"))).thenReturn("good text");
        when(chunkingService.chunk("doc-good", "good text")).thenReturn(List.of());

        List<Document> result = service.ingestFromFolder(folderId);

        assertThat(result).hasSize(1);
    }

    @Test
    void ingestFromFolder_shouldThrowWhenFolderConfigNotFound() {
        when(folderConfigRepository.findById("missing-folder")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ingestFromFolder("missing-folder"))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("Folder config not found");
    }

    // ========================================================================
    // Test OCR routing / Test di routing OCR
    // ========================================================================

    @Nested
    class OcrRoutingTests {

        @Mock
        private OcrExtractorPort ocrExtractorPort;

        private DocumentIngestionPipelineService serviceWithOcr;

        @BeforeEach
        void setUpOcr() {
            serviceWithOcr = new DocumentIngestionPipelineService(
                    documentRepository, documentChunkRepository, textExtractorPort,
                    chunkingService, vectorStorePort, fileSystemScannerPort,
                    folderConfigRepository, Optional.of(ocrExtractorPort));
        }

        @Test
        void ingestFile_image_shouldUseOcrDirectly() {
            // given
            String imageFilename = "scan.png";
            String imageMimeType = "image/png";
            when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
            when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
                Document doc = invocation.getArgument(0);
                if (doc.getId() == null) doc.setId("doc-ocr");
                return doc;
            });
            when(ocrExtractorPort.isAvailable()).thenReturn(true);
            when(ocrExtractorPort.extract(any(), eq(imageMimeType)))
                    .thenReturn(OcrResult.builder()
                            .text("OCR extracted text from image")
                            .confidence(85.5)
                            .language("ita+eng")
                            .pageCount(1)
                            .build());
            when(chunkingService.chunk("doc-ocr", "OCR extracted text from image")).thenReturn(List.of());

            // when
            Document result = serviceWithOcr.ingestFile(imageFilename, createTestStream(),
                    imageMimeType, FILE_SIZE, FILE_HASH);

            // then
            assertThat(result.getStatus()).isEqualTo(DocumentStatus.INDEXED);
            assertThat(result.getOcrConfidence()).isEqualTo(85.5);
            verify(ocrExtractorPort).extract(any(), eq(imageMimeType));
            verify(textExtractorPort, never()).extract(any(), any());
        }

        @Test
        void ingestFile_image_shouldThrowWhenOcrNotAvailable() {
            // given
            String imageFilename = "scan.png";
            String imageMimeType = "image/png";
            when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
            when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
                Document doc = invocation.getArgument(0);
                if (doc.getId() == null) doc.setId("doc-ocr");
                return doc;
            });
            when(ocrExtractorPort.isAvailable()).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> serviceWithOcr.ingestFile(imageFilename, createTestStream(),
                    imageMimeType, FILE_SIZE, FILE_HASH))
                    .isInstanceOf(DocumentProcessingException.class)
                    .hasMessageContaining("Failed to ingest file");
        }

        @Test
        void ingestFile_pdf_shouldUseTikaWhenTextIsSufficient() {
            // given
            String longText = "A".repeat(100); // >= 50 caratteri / >= 50 characters
            when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
            when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
                Document doc = invocation.getArgument(0);
                if (doc.getId() == null) doc.setId("doc-pdf");
                return doc;
            });
            when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn(longText);
            when(chunkingService.chunk("doc-pdf", longText)).thenReturn(List.of());

            // when
            Document result = serviceWithOcr.ingestFile(FILENAME, createTestStream(),
                    MIME_TYPE, FILE_SIZE, FILE_HASH);

            // then
            assertThat(result.getStatus()).isEqualTo(DocumentStatus.INDEXED);
            assertThat(result.getOcrConfidence()).isNull();
            verify(textExtractorPort).extract(any(), eq(MIME_TYPE));
            verify(ocrExtractorPort, never()).extract(any(), any());
        }

        @Test
        void ingestFile_pdf_shouldFallbackToOcrWhenTikaTextTooShort() {
            // given - Tika restituisce testo troppo corto / Tika returns too short text
            String shortText = "ab";
            when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
            when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
                Document doc = invocation.getArgument(0);
                if (doc.getId() == null) doc.setId("doc-pdf");
                return doc;
            });
            when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn(shortText);
            when(ocrExtractorPort.isAvailable()).thenReturn(true);
            when(ocrExtractorPort.extract(any(), eq(MIME_TYPE)))
                    .thenReturn(OcrResult.builder()
                            .text("OCR text from scanned PDF")
                            .confidence(72.0)
                            .language("ita+eng")
                            .pageCount(3)
                            .build());
            when(chunkingService.chunk("doc-pdf", "OCR text from scanned PDF")).thenReturn(List.of());

            // when
            Document result = serviceWithOcr.ingestFile(FILENAME, createTestStream(),
                    MIME_TYPE, FILE_SIZE, FILE_HASH);

            // then
            assertThat(result.getStatus()).isEqualTo(DocumentStatus.INDEXED);
            assertThat(result.getOcrConfidence()).isEqualTo(72.0);
            verify(textExtractorPort).extract(any(), eq(MIME_TYPE));
            verify(ocrExtractorPort).extract(any(), eq(MIME_TYPE));
        }

        @Test
        void ingestFile_pdf_shouldReturnTikaTextWhenOcrNotAvailableAndTextShort() {
            // given
            String shortText = "xy";
            when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
            when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
                Document doc = invocation.getArgument(0);
                if (doc.getId() == null) doc.setId("doc-pdf");
                return doc;
            });
            when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn(shortText);
            when(ocrExtractorPort.isAvailable()).thenReturn(false);
            when(chunkingService.chunk("doc-pdf", shortText)).thenReturn(List.of());

            // when
            Document result = serviceWithOcr.ingestFile(FILENAME, createTestStream(),
                    MIME_TYPE, FILE_SIZE, FILE_HASH);

            // then
            assertThat(result.getStatus()).isEqualTo(DocumentStatus.INDEXED);
            assertThat(result.getOcrConfidence()).isNull();
            verify(textExtractorPort).extract(any(), eq(MIME_TYPE));
            verify(ocrExtractorPort, never()).extract(any(), any());
        }

        @Test
        void ingestFile_pdf_shouldReturnEmptyWhenTikaReturnsNullAndOcrNotAvailable() {
            // given
            when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
            when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
                Document doc = invocation.getArgument(0);
                if (doc.getId() == null) doc.setId("doc-pdf");
                return doc;
            });
            when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn(null);
            when(ocrExtractorPort.isAvailable()).thenReturn(false);
            when(chunkingService.chunk("doc-pdf", "")).thenReturn(List.of());

            // when
            Document result = serviceWithOcr.ingestFile(FILENAME, createTestStream(),
                    MIME_TYPE, FILE_SIZE, FILE_HASH);

            // then
            assertThat(result.getStatus()).isEqualTo(DocumentStatus.INDEXED);
        }

        @Test
        void ingestFile_textPlain_shouldUseTikaRegardlessOfOcr() {
            // given
            String textMimeType = "text/plain";
            String textFilename = "readme.txt";
            when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
            when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
                Document doc = invocation.getArgument(0);
                if (doc.getId() == null) doc.setId("doc-txt");
                return doc;
            });
            when(textExtractorPort.extract(any(), eq(textMimeType))).thenReturn("plain text content");
            when(chunkingService.chunk("doc-txt", "plain text content")).thenReturn(List.of());

            // when
            Document result = serviceWithOcr.ingestFile(textFilename, createTestStream(),
                    textMimeType, FILE_SIZE, FILE_HASH);

            // then
            assertThat(result.getStatus()).isEqualTo(DocumentStatus.INDEXED);
            assertThat(result.getOcrConfidence()).isNull();
            verify(textExtractorPort).extract(any(), eq(textMimeType));
            verify(ocrExtractorPort, never()).extract(any(), any());
        }
    }

    @Nested
    class WithoutOcrTests {

        @Test
        void ingestFile_image_shouldThrowWhenNoOcrConfigured() {
            // given - service senza OCR / service without OCR
            String imageFilename = "photo.jpg";
            String imageMimeType = "image/jpeg";
            when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
            when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
                Document doc = invocation.getArgument(0);
                if (doc.getId() == null) doc.setId("doc-img");
                return doc;
            });

            // when / then
            assertThatThrownBy(() -> service.ingestFile(imageFilename, createTestStream(),
                    imageMimeType, FILE_SIZE, FILE_HASH))
                    .isInstanceOf(DocumentProcessingException.class)
                    .hasMessageContaining("Failed to ingest file");
        }

        @Test
        void ingestFile_pdf_shouldUseTikaOnlyWhenNoOcrConfigured() {
            // given
            String text = "A".repeat(100);
            when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
            when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
                Document doc = invocation.getArgument(0);
                if (doc.getId() == null) doc.setId("doc-pdf");
                return doc;
            });
            when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn(text);
            when(chunkingService.chunk("doc-pdf", text)).thenReturn(List.of());

            // when
            Document result = service.ingestFile(FILENAME, createTestStream(),
                    MIME_TYPE, FILE_SIZE, FILE_HASH);

            // then
            assertThat(result.getStatus()).isEqualTo(DocumentStatus.INDEXED);
            assertThat(result.getOcrConfidence()).isNull();
            verify(textExtractorPort).extract(any(), eq(MIME_TYPE));
        }

        @Test
        void ingestFile_pdf_shouldReturnShortTikaTextWhenNoOcrConfigured() {
            // given - Tika testo corto, nessun OCR disponibile
            // Tika short text, no OCR available
            String shortText = "ab";
            when(documentRepository.findByFileHash(FILE_HASH)).thenReturn(Optional.empty());
            when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
                Document doc = invocation.getArgument(0);
                if (doc.getId() == null) doc.setId("doc-pdf");
                return doc;
            });
            when(textExtractorPort.extract(any(), eq(MIME_TYPE))).thenReturn(shortText);
            when(chunkingService.chunk("doc-pdf", shortText)).thenReturn(List.of());

            // when
            Document result = service.ingestFile(FILENAME, createTestStream(),
                    MIME_TYPE, FILE_SIZE, FILE_HASH);

            // then
            assertThat(result.getStatus()).isEqualTo(DocumentStatus.INDEXED);
            assertThat(result.getOcrConfidence()).isNull();
        }
    }
}
