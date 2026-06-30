package com.localmind.infrastructure.finetuning.persistence.adapter;

import com.localmind.domain.finetuning.model.TrainingDataset;
import com.localmind.infrastructure.finetuning.persistence.entity.TrainingDatasetEntity;
import com.localmind.infrastructure.finetuning.persistence.repository.TrainingDatasetJpaRepository;
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
class TrainingDatasetRepositoryAdapterTest {

    @Mock
    private TrainingDatasetJpaRepository jpaRepository;

    @InjectMocks
    private TrainingDatasetRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-01-15T10:30:00Z");

    @Test
    void save_shouldMapDomainToEntityAndBack() {
        // given
        TrainingDataset dataset = TrainingDataset.builder()
                .id(TEST_UUID.toString())
                .name("test-dataset")
                .documentIds(List.of("doc-1", "doc-2"))
                .sampleCount(10)
                .format("JSONL")
                .createdAt(NOW)
                .build();

        TrainingDatasetEntity savedEntity = TrainingDatasetEntity.builder()
                .id(TEST_UUID)
                .name("test-dataset")
                .documentIds("doc-1,doc-2")
                .sampleCount(10)
                .format("JSONL")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(TrainingDatasetEntity.class))).thenReturn(savedEntity);

        // when
        TrainingDataset result = adapter.save(dataset);

        // then
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("test-dataset");
        assertThat(result.getDocumentIds()).containsExactly("doc-1", "doc-2");
        assertThat(result.getSampleCount()).isEqualTo(10);
        assertThat(result.getFormat()).isEqualTo("JSONL");

        ArgumentCaptor<TrainingDatasetEntity> captor = ArgumentCaptor.forClass(TrainingDatasetEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getDocumentIds()).isEqualTo("doc-1,doc-2");
    }

    @Test
    void findById_found_shouldReturnMappedDomain() {
        // given
        TrainingDatasetEntity entity = TrainingDatasetEntity.builder()
                .id(TEST_UUID)
                .name("test-ds")
                .documentIds("doc-a,doc-b,doc-c")
                .sampleCount(3)
                .format("JSONL")
                .createdAt(NOW)
                .build();
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        // when
        Optional<TrainingDataset> result = adapter.findById(TEST_UUID.toString());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("test-ds");
        assertThat(result.get().getDocumentIds()).hasSize(3);
    }

    @Test
    void findById_notFound_shouldReturnEmpty() {
        // given
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        // when
        Optional<TrainingDataset> result = adapter.findById(TEST_UUID.toString());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnMappedList() {
        // given
        TrainingDatasetEntity entity = TrainingDatasetEntity.builder()
                .id(TEST_UUID)
                .name("ds-1")
                .documentIds("doc-1")
                .sampleCount(1)
                .format("JSONL")
                .createdAt(NOW)
                .build();
        when(jpaRepository.findAll()).thenReturn(List.of(entity));

        // when
        List<TrainingDataset> result = adapter.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ds-1");
    }

    @Test
    void save_nullDocumentIds_shouldMapCorrectly() {
        // given
        TrainingDataset dataset = TrainingDataset.builder()
                .id(TEST_UUID.toString())
                .name("empty-ds")
                .documentIds(null)
                .sampleCount(0)
                .format("JSONL")
                .createdAt(NOW)
                .build();

        TrainingDatasetEntity savedEntity = TrainingDatasetEntity.builder()
                .id(TEST_UUID)
                .name("empty-ds")
                .documentIds(null)
                .sampleCount(0)
                .format("JSONL")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(TrainingDatasetEntity.class))).thenReturn(savedEntity);

        // when
        TrainingDataset result = adapter.save(dataset);

        // then
        assertThat(result.getDocumentIds()).isEmpty();
    }

    @Test
    void toDomain_emptyDocumentIds_shouldReturnEmptyList() {
        // given
        TrainingDatasetEntity entity = TrainingDatasetEntity.builder()
                .id(TEST_UUID)
                .name("empty")
                .documentIds("")
                .sampleCount(0)
                .format("JSONL")
                .createdAt(NOW)
                .build();
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        // when
        Optional<TrainingDataset> result = adapter.findById(TEST_UUID.toString());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getDocumentIds()).isEmpty();
    }
}
