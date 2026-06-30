package com.localmind.infrastructure.finetuning.persistence.adapter;

import com.localmind.domain.finetuning.model.FineTuningJob;
import com.localmind.domain.finetuning.model.FineTuningStatus;
import com.localmind.domain.finetuning.model.TrainingTechnique;
import com.localmind.infrastructure.finetuning.persistence.entity.FineTuningJobEntity;
import com.localmind.infrastructure.finetuning.persistence.repository.FineTuningJobJpaRepository;
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
class FineTuningJobRepositoryAdapterTest {

    @Mock
    private FineTuningJobJpaRepository jpaRepository;

    @InjectMocks
    private FineTuningJobRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-01-15T10:30:00Z");

    @Test
    void save_shouldMapDomainToEntityAndBack() {
        // given
        FineTuningJob job = FineTuningJob.builder()
                .id(TEST_UUID.toString())
                .baseModel("llama3.2")
                .technique(TrainingTechnique.LORA)
                .status(FineTuningStatus.PENDING)
                .progress(0)
                .datasetId(TEST_UUID.toString())
                .hyperparameters(Map.of("lr", "1e-4"))
                .createdAt(NOW)
                .build();

        FineTuningJobEntity savedEntity = FineTuningJobEntity.builder()
                .id(TEST_UUID)
                .baseModel("llama3.2")
                .technique("LORA")
                .status("PENDING")
                .progress(0)
                .datasetId(TEST_UUID)
                .hyperparameters("{\"lr\":\"1e-4\"}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(FineTuningJobEntity.class))).thenReturn(savedEntity);

        // when
        FineTuningJob result = adapter.save(job);

        // then
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getBaseModel()).isEqualTo("llama3.2");
        assertThat(result.getTechnique()).isEqualTo(TrainingTechnique.LORA);
        assertThat(result.getStatus()).isEqualTo(FineTuningStatus.PENDING);

        ArgumentCaptor<FineTuningJobEntity> captor = ArgumentCaptor.forClass(FineTuningJobEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getBaseModel()).isEqualTo("llama3.2");
        assertThat(captor.getValue().getTechnique()).isEqualTo("LORA");
    }

    @Test
    void findById_found_shouldReturnMappedDomain() {
        // given
        FineTuningJobEntity entity = FineTuningJobEntity.builder()
                .id(TEST_UUID)
                .baseModel("llama3.2")
                .technique("QLORA")
                .status("COMPLETED")
                .progress(100)
                .datasetId(TEST_UUID)
                .hyperparameters("{}")
                .createdAt(NOW)
                .completedAt(NOW)
                .build();
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        // when
        Optional<FineTuningJob> result = adapter.findById(TEST_UUID.toString());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTechnique()).isEqualTo(TrainingTechnique.QLORA);
        assertThat(result.get().getStatus()).isEqualTo(FineTuningStatus.COMPLETED);
        assertThat(result.get().getProgress()).isEqualTo(100);
    }

    @Test
    void findById_notFound_shouldReturnEmpty() {
        // given
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        // when
        Optional<FineTuningJob> result = adapter.findById(TEST_UUID.toString());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnMappedList() {
        // given
        FineTuningJobEntity entity = FineTuningJobEntity.builder()
                .id(TEST_UUID)
                .baseModel("llama3.2")
                .technique("FULL")
                .status("TRAINING")
                .progress(50)
                .hyperparameters(null)
                .createdAt(NOW)
                .build();
        when(jpaRepository.findAll()).thenReturn(List.of(entity));

        // when
        List<FineTuningJob> result = adapter.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTechnique()).isEqualTo(TrainingTechnique.FULL);
        assertThat(result.get(0).getHyperparameters()).isNull();
    }

    @Test
    void deleteById_shouldDelegate() {
        // when
        adapter.deleteById(TEST_UUID.toString());

        // then
        verify(jpaRepository).deleteById(TEST_UUID);
    }

    @Test
    void save_nullHyperparameters_shouldMapCorrectly() {
        // given
        FineTuningJob job = FineTuningJob.builder()
                .id(TEST_UUID.toString())
                .baseModel("llama3.2")
                .technique(TrainingTechnique.LORA)
                .status(FineTuningStatus.PENDING)
                .progress(0)
                .hyperparameters(null)
                .createdAt(NOW)
                .build();

        FineTuningJobEntity savedEntity = FineTuningJobEntity.builder()
                .id(TEST_UUID)
                .baseModel("llama3.2")
                .technique("LORA")
                .status("PENDING")
                .progress(0)
                .hyperparameters(null)
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(FineTuningJobEntity.class))).thenReturn(savedEntity);

        // when
        FineTuningJob result = adapter.save(job);

        // then
        assertThat(result.getHyperparameters()).isNull();
    }
}
