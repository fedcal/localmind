package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.document.model.DocumentChunk;
import com.localmind.infrastructure.persistence.entity.DocumentChunkEntity;
import com.localmind.infrastructure.persistence.repository.JpaDocumentChunkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentChunkRepositoryAdapterTest {

    @Mock
    private JpaDocumentChunkRepository jpaRepository;

    @InjectMocks
    private DocumentChunkRepositoryAdapter adapter;

    private static final UUID CHUNK_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String DOCUMENT_ID = "doc-001";

    @Test
    void save_shouldConvertToEntityAndSave() {
        // given
        DocumentChunk chunk = DocumentChunk.builder()
                .id(CHUNK_UUID.toString())
                .documentId(DOCUMENT_ID)
                .content("This is a test chunk content.")
                .chunkIndex(0)
                .build();

        DocumentChunkEntity savedEntity = DocumentChunkEntity.builder()
                .id(CHUNK_UUID)
                .documentId(DOCUMENT_ID)
                .content("This is a test chunk content.")
                .chunkIndex(0)
                .build();

        when(jpaRepository.save(any(DocumentChunkEntity.class))).thenReturn(savedEntity);

        // when
        adapter.save(chunk);

        // then
        ArgumentCaptor<DocumentChunkEntity> captor = ArgumentCaptor.forClass(DocumentChunkEntity.class);
        verify(jpaRepository).save(captor.capture());

        DocumentChunkEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getId()).isEqualTo(CHUNK_UUID);
        assertThat(capturedEntity.getDocumentId()).isEqualTo(DOCUMENT_ID);
        assertThat(capturedEntity.getContent()).isEqualTo("This is a test chunk content.");
        assertThat(capturedEntity.getChunkIndex()).isEqualTo(0);
    }

    @Test
    void save_shouldConvertResultBackToDomain() {
        // given
        DocumentChunk chunk = DocumentChunk.builder()
                .id(CHUNK_UUID.toString())
                .documentId(DOCUMENT_ID)
                .content("Chunk content for domain conversion.")
                .chunkIndex(2)
                .build();

        DocumentChunkEntity savedEntity = DocumentChunkEntity.builder()
                .id(CHUNK_UUID)
                .documentId(DOCUMENT_ID)
                .content("Chunk content for domain conversion.")
                .chunkIndex(2)
                .build();

        when(jpaRepository.save(any(DocumentChunkEntity.class))).thenReturn(savedEntity);

        // when
        DocumentChunk result = adapter.save(chunk);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(CHUNK_UUID.toString());
        assertThat(result.getDocumentId()).isEqualTo(DOCUMENT_ID);
        assertThat(result.getContent()).isEqualTo("Chunk content for domain conversion.");
        assertThat(result.getChunkIndex()).isEqualTo(2);
    }

    @Test
    void saveAll_shouldConvertAllChunks() {
        // given
        UUID secondUuid = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

        DocumentChunk chunk1 = DocumentChunk.builder()
                .id(CHUNK_UUID.toString())
                .documentId(DOCUMENT_ID)
                .content("First chunk content.")
                .chunkIndex(0)
                .build();

        DocumentChunk chunk2 = DocumentChunk.builder()
                .id(secondUuid.toString())
                .documentId(DOCUMENT_ID)
                .content("Second chunk content.")
                .chunkIndex(1)
                .build();

        DocumentChunkEntity savedEntity1 = DocumentChunkEntity.builder()
                .id(CHUNK_UUID)
                .documentId(DOCUMENT_ID)
                .content("First chunk content.")
                .chunkIndex(0)
                .build();

        DocumentChunkEntity savedEntity2 = DocumentChunkEntity.builder()
                .id(secondUuid)
                .documentId(DOCUMENT_ID)
                .content("Second chunk content.")
                .chunkIndex(1)
                .build();

        when(jpaRepository.saveAll(anyList())).thenReturn(List.of(savedEntity1, savedEntity2));

        // when
        List<DocumentChunk> results = adapter.saveAll(List.of(chunk1, chunk2));

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(CHUNK_UUID.toString());
        assertThat(results.get(0).getContent()).isEqualTo("First chunk content.");
        assertThat(results.get(0).getChunkIndex()).isEqualTo(0);
        assertThat(results.get(1).getId()).isEqualTo(secondUuid.toString());
        assertThat(results.get(1).getContent()).isEqualTo("Second chunk content.");
        assertThat(results.get(1).getChunkIndex()).isEqualTo(1);

        verify(jpaRepository).saveAll(anyList());
    }

    @Test
    void findByDocumentId_shouldReturnDomainChunks() {
        // given
        UUID secondUuid = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

        DocumentChunkEntity entity1 = DocumentChunkEntity.builder()
                .id(CHUNK_UUID)
                .documentId(DOCUMENT_ID)
                .content("First chunk.")
                .chunkIndex(0)
                .build();

        DocumentChunkEntity entity2 = DocumentChunkEntity.builder()
                .id(secondUuid)
                .documentId(DOCUMENT_ID)
                .content("Second chunk.")
                .chunkIndex(1)
                .build();

        when(jpaRepository.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of(entity1, entity2));

        // when
        List<DocumentChunk> results = adapter.findByDocumentId(DOCUMENT_ID);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(CHUNK_UUID.toString());
        assertThat(results.get(0).getDocumentId()).isEqualTo(DOCUMENT_ID);
        assertThat(results.get(0).getContent()).isEqualTo("First chunk.");
        assertThat(results.get(0).getChunkIndex()).isEqualTo(0);
        assertThat(results.get(1).getId()).isEqualTo(secondUuid.toString());
        assertThat(results.get(1).getDocumentId()).isEqualTo(DOCUMENT_ID);
        assertThat(results.get(1).getContent()).isEqualTo("Second chunk.");
        assertThat(results.get(1).getChunkIndex()).isEqualTo(1);

        verify(jpaRepository).findByDocumentId(DOCUMENT_ID);
    }

    @Test
    void findByDocumentId_shouldReturnEmptyList() {
        // given
        when(jpaRepository.findByDocumentId("doc-nonexistent")).thenReturn(Collections.emptyList());

        // when
        List<DocumentChunk> results = adapter.findByDocumentId("doc-nonexistent");

        // then
        assertThat(results).isEmpty();
        verify(jpaRepository).findByDocumentId("doc-nonexistent");
    }

    @Test
    void deleteByDocumentId_shouldCallRepository() {
        // when
        adapter.deleteByDocumentId(DOCUMENT_ID);

        // then
        verify(jpaRepository).deleteByDocumentId(DOCUMENT_ID);
    }
}
