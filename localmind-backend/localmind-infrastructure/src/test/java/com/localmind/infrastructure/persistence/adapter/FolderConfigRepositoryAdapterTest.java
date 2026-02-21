package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.document.model.FolderConfig;
import com.localmind.infrastructure.persistence.entity.FolderConfigEntity;
import com.localmind.infrastructure.persistence.repository.JpaFolderConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderConfigRepositoryAdapterTest {

    @Mock
    private JpaFolderConfigRepository jpaRepository;

    @InjectMocks
    private FolderConfigRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("e5f6a7b8-c9d0-1234-ef01-345678901234");
    private final Instant NOW = Instant.parse("2026-02-01T08:00:00Z");

    @Test
    void save_shouldMapAndDelegate() {
        // given
        FolderConfig config = FolderConfig.builder()
                .id(TEST_UUID.toString())
                .path("/home/user/documents")
                .recursive(true)
                .watchEnabled(true)
                .lastScanAt(NOW)
                .documentCount(42)
                .build();

        FolderConfigEntity savedEntity = FolderConfigEntity.builder()
                .id(TEST_UUID)
                .path("/home/user/documents")
                .recursive(true)
                .watchEnabled(true)
                .lastScanAt(NOW)
                .documentCount(42)
                .build();

        when(jpaRepository.save(any(FolderConfigEntity.class))).thenReturn(savedEntity);

        // when
        FolderConfig result = adapter.save(config);

        // then
        ArgumentCaptor<FolderConfigEntity> captor = ArgumentCaptor.forClass(FolderConfigEntity.class);
        verify(jpaRepository).save(captor.capture());

        FolderConfigEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getPath()).isEqualTo("/home/user/documents");
        assertThat(captured.isRecursive()).isTrue();
        assertThat(captured.isWatchEnabled()).isTrue();
        assertThat(captured.getDocumentCount()).isEqualTo(42);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getPath()).isEqualTo("/home/user/documents");
        assertThat(result.isRecursive()).isTrue();
        assertThat(result.isWatchEnabled()).isTrue();
        assertThat(result.getLastScanAt()).isEqualTo(NOW);
        assertThat(result.getDocumentCount()).isEqualTo(42);
    }

    @Test
    void findById_shouldReturnMappedConfig() {
        // given
        FolderConfigEntity entity = FolderConfigEntity.builder()
                .id(TEST_UUID)
                .path("/var/data/pdfs")
                .recursive(false)
                .watchEnabled(false)
                .lastScanAt(NOW)
                .documentCount(7)
                .build();

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        // when
        Optional<FolderConfig> result = adapter.findById(TEST_UUID.toString());

        // then
        assertThat(result).isPresent();
        FolderConfig config = result.get();
        assertThat(config.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(config.getPath()).isEqualTo("/var/data/pdfs");
        assertThat(config.isRecursive()).isFalse();
        assertThat(config.isWatchEnabled()).isFalse();
        assertThat(config.getDocumentCount()).isEqualTo(7);
    }

    @Test
    void findAll_shouldReturnAll() {
        // given
        FolderConfigEntity entity1 = FolderConfigEntity.builder()
                .id(TEST_UUID)
                .path("/home/user/docs")
                .recursive(true)
                .watchEnabled(true)
                .documentCount(100)
                .build();

        UUID secondUuid = UUID.fromString("f6a7b8c9-d0e1-2345-f012-456789012345");
        FolderConfigEntity entity2 = FolderConfigEntity.builder()
                .id(secondUuid)
                .path("/tmp/uploads")
                .recursive(false)
                .watchEnabled(false)
                .documentCount(3)
                .build();

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        // when
        List<FolderConfig> result = adapter.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPath()).isEqualTo("/home/user/docs");
        assertThat(result.get(0).isRecursive()).isTrue();
        assertThat(result.get(1).getPath()).isEqualTo("/tmp/uploads");
        assertThat(result.get(1).isRecursive()).isFalse();
    }

    @Test
    void deleteById_shouldDelegate() {
        // when
        adapter.deleteById(TEST_UUID.toString());

        // then
        verify(jpaRepository).deleteById(TEST_UUID);
    }
}
