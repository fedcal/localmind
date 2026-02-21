package com.localmind.domain.finetuning.service;

import com.localmind.domain.finetuning.model.FineTuningJob;
import com.localmind.domain.finetuning.model.FineTuningStatus;
import com.localmind.domain.finetuning.model.TrainingDataset;
import com.localmind.domain.finetuning.model.TrainingTechnique;
import com.localmind.domain.finetuning.port.out.FineTuningJobRepository;
import com.localmind.domain.finetuning.port.out.FineTuningPort;
import com.localmind.domain.finetuning.port.out.TrainingDatasetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineTuningServiceTest {

    @Mock
    private FineTuningPort fineTuningPort;

    @Mock
    private FineTuningJobRepository jobRepository;

    @Mock
    private TrainingDatasetRepository datasetRepository;

    @InjectMocks
    private FineTuningService fineTuningService;

    @Test
    void createDataset_shouldSaveAndPrepare() {
        // given
        String name = "test-dataset";
        List<String> docIds = List.of("doc-1", "doc-2");

        when(datasetRepository.save(any(TrainingDataset.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(fineTuningPort.prepareDataset(anyString(), anyList())).thenReturn("/output/path");

        // when
        TrainingDataset result = fineTuningService.createDataset(name, docIds);

        // then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getDocumentIds()).containsExactly("doc-1", "doc-2");
        assertThat(result.getFormat()).isEqualTo("JSONL");
        assertThat(result.getId()).isNotNull();

        verify(datasetRepository, times(2)).save(any(TrainingDataset.class));
        verify(fineTuningPort).prepareDataset(anyString(), eq(docIds));
    }

    @Test
    void startTraining_shouldCreateJobAndCallPort() {
        // given
        String datasetId = "ds-1";
        String baseModel = "llama3.2";
        TrainingTechnique technique = TrainingTechnique.LORA;
        Map<String, String> hyperparameters = Map.of("lr", "1e-4");

        when(jobRepository.save(any(FineTuningJob.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        FineTuningJob result = fineTuningService.startTraining(datasetId, baseModel, technique, hyperparameters);

        // then
        assertThat(result.getBaseModel()).isEqualTo(baseModel);
        assertThat(result.getTechnique()).isEqualTo(TrainingTechnique.LORA);
        assertThat(result.getStatus()).isEqualTo(FineTuningStatus.PENDING);
        assertThat(result.getProgress()).isZero();
        assertThat(result.getDatasetId()).isEqualTo(datasetId);
        assertThat(result.getHyperparameters()).containsEntry("lr", "1e-4");

        verify(jobRepository).save(any(FineTuningJob.class));
        verify(fineTuningPort).startTraining(any(FineTuningJob.class));
    }

    @Test
    void listJobs_shouldReturnAll() {
        // given
        FineTuningJob job = FineTuningJob.builder()
                .id("job-1")
                .baseModel("llama3.2")
                .technique(TrainingTechnique.LORA)
                .status(FineTuningStatus.COMPLETED)
                .progress(100)
                .createdAt(Instant.now())
                .build();
        when(jobRepository.findAll()).thenReturn(List.of(job));

        // when
        List<FineTuningJob> result = fineTuningService.listJobs();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("job-1");
        verify(jobRepository).findAll();
    }

    @Test
    void getJob_notFound_shouldReturnEmpty() {
        // given
        when(jobRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // when
        Optional<FineTuningJob> result = fineTuningService.getJob("nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getJob_completed_shouldReturnWithoutRefresh() {
        // given
        FineTuningJob job = FineTuningJob.builder()
                .id("job-1")
                .baseModel("llama3.2")
                .technique(TrainingTechnique.LORA)
                .status(FineTuningStatus.COMPLETED)
                .progress(100)
                .createdAt(Instant.now())
                .build();
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(job));

        // when
        Optional<FineTuningJob> result = fineTuningService.getJob("job-1");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(FineTuningStatus.COMPLETED);
        verify(fineTuningPort, never()).getTrainingStatus(anyString());
        verify(fineTuningPort, never()).getTrainingProgress(anyString());
    }

    @Test
    void getJob_training_shouldRefreshStatus() {
        // given
        FineTuningJob job = FineTuningJob.builder()
                .id("job-1")
                .baseModel("llama3.2")
                .technique(TrainingTechnique.LORA)
                .status(FineTuningStatus.TRAINING)
                .progress(50)
                .createdAt(Instant.now())
                .build();
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(job));
        when(fineTuningPort.getTrainingStatus("job-1")).thenReturn(FineTuningStatus.TRAINING);
        when(fineTuningPort.getTrainingProgress("job-1")).thenReturn(75);
        when(jobRepository.save(any(FineTuningJob.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Optional<FineTuningJob> result = fineTuningService.getJob("job-1");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getProgress()).isEqualTo(75);
        verify(fineTuningPort).getTrainingStatus("job-1");
        verify(fineTuningPort).getTrainingProgress("job-1");
        verify(jobRepository).save(any(FineTuningJob.class));
    }

    @Test
    void getJob_trainingCompleted_shouldSetCompletedAt() {
        // given
        FineTuningJob job = FineTuningJob.builder()
                .id("job-1")
                .baseModel("llama3.2")
                .technique(TrainingTechnique.LORA)
                .status(FineTuningStatus.TRAINING)
                .progress(90)
                .createdAt(Instant.now())
                .build();
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(job));
        when(fineTuningPort.getTrainingStatus("job-1")).thenReturn(FineTuningStatus.COMPLETED);
        when(fineTuningPort.getTrainingProgress("job-1")).thenReturn(100);
        when(jobRepository.save(any(FineTuningJob.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Optional<FineTuningJob> result = fineTuningService.getJob("job-1");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(FineTuningStatus.COMPLETED);
        assertThat(result.get().getCompletedAt()).isNotNull();
    }

    @Test
    void deployModel_shouldDeployCompletedJob() {
        // given
        String jobId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
        FineTuningJob job = FineTuningJob.builder()
                .id(jobId)
                .baseModel("llama3.2")
                .technique(TrainingTechnique.LORA)
                .status(FineTuningStatus.COMPLETED)
                .progress(100)
                .createdAt(Instant.now())
                .build();
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(FineTuningJob.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        fineTuningService.deployModel(jobId);

        // then
        ArgumentCaptor<String> modelNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(fineTuningPort).deployModel(eq(jobId), modelNameCaptor.capture());
        assertThat(modelNameCaptor.getValue()).startsWith("llama3.2-ft-");

        ArgumentCaptor<FineTuningJob> jobCaptor = ArgumentCaptor.forClass(FineTuningJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        assertThat(jobCaptor.getValue().getStatus()).isEqualTo(FineTuningStatus.DEPLOYED);
    }

    @Test
    void deployModel_notCompleted_shouldThrow() {
        // given
        FineTuningJob job = FineTuningJob.builder()
                .id("job-1")
                .baseModel("llama3.2")
                .technique(TrainingTechnique.LORA)
                .status(FineTuningStatus.TRAINING)
                .progress(50)
                .createdAt(Instant.now())
                .build();
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(job));

        // when/then
        assertThatThrownBy(() -> fineTuningService.deployModel("job-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot deploy model");
    }

    @Test
    void deployModel_notFound_shouldThrow() {
        // given
        when(jobRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> fineTuningService.deployModel("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Job not found");
    }

    @Test
    void listDatasets_shouldReturnAll() {
        // given
        TrainingDataset ds = TrainingDataset.builder()
                .id("ds-1")
                .name("test")
                .documentIds(List.of("doc-1"))
                .sampleCount(1)
                .format("JSONL")
                .createdAt(Instant.now())
                .build();
        when(datasetRepository.findAll()).thenReturn(List.of(ds));

        // when
        List<TrainingDataset> result = fineTuningService.listDatasets();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test");
    }

    @Test
    void deployModel_withExistingOutputModelName_shouldUseIt() {
        // given
        FineTuningJob job = FineTuningJob.builder()
                .id("job-1")
                .baseModel("llama3.2")
                .technique(TrainingTechnique.LORA)
                .status(FineTuningStatus.COMPLETED)
                .progress(100)
                .outputModelName("my-custom-model")
                .createdAt(Instant.now())
                .build();
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(job));
        when(jobRepository.save(any(FineTuningJob.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        fineTuningService.deployModel("job-1");

        // then
        verify(fineTuningPort).deployModel("job-1", "my-custom-model");
    }
}
