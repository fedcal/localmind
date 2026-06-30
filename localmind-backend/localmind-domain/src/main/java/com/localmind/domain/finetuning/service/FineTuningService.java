package com.localmind.domain.finetuning.service;

import com.localmind.domain.finetuning.model.FineTuningJob;
import com.localmind.domain.finetuning.model.FineTuningStatus;
import com.localmind.domain.finetuning.model.TrainingDataset;
import com.localmind.domain.finetuning.model.TrainingTechnique;
import com.localmind.domain.finetuning.port.in.FineTuningUseCase;
import com.localmind.domain.finetuning.port.out.FineTuningJobRepository;
import com.localmind.domain.finetuning.port.out.FineTuningPort;
import com.localmind.domain.finetuning.port.out.TrainingDatasetRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FineTuningService implements FineTuningUseCase {

    private final FineTuningPort fineTuningPort;
    private final FineTuningJobRepository jobRepository;
    private final TrainingDatasetRepository datasetRepository;

    public FineTuningService(FineTuningPort fineTuningPort,
                             FineTuningJobRepository jobRepository,
                             TrainingDatasetRepository datasetRepository) {
        this.fineTuningPort = fineTuningPort;
        this.jobRepository = jobRepository;
        this.datasetRepository = datasetRepository;
    }

    @Override
    public TrainingDataset createDataset(String name, List<String> documentIds) {
        TrainingDataset dataset = TrainingDataset.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .documentIds(documentIds)
                .sampleCount(0)
                .format("JSONL")
                .createdAt(Instant.now())
                .build();

        dataset = datasetRepository.save(dataset);

        String path = fineTuningPort.prepareDataset(dataset.getId(), documentIds);

        dataset.setSampleCount(documentIds.size());
        dataset = datasetRepository.save(dataset);

        return dataset;
    }

    @Override
    public FineTuningJob startTraining(String datasetId, String baseModel,
                                        TrainingTechnique technique,
                                        Map<String, String> hyperparameters) {
        FineTuningJob job = FineTuningJob.builder()
                .id(UUID.randomUUID().toString())
                .baseModel(baseModel)
                .technique(technique)
                .status(FineTuningStatus.PENDING)
                .progress(0)
                .datasetId(datasetId)
                .hyperparameters(hyperparameters)
                .createdAt(Instant.now())
                .build();

        job = jobRepository.save(job);

        fineTuningPort.startTraining(job);

        return job;
    }

    @Override
    public List<FineTuningJob> listJobs() {
        return jobRepository.findAll();
    }

    @Override
    public Optional<FineTuningJob> getJob(String jobId) {
        Optional<FineTuningJob> optionalJob = jobRepository.findById(jobId);

        if (optionalJob.isPresent()) {
            FineTuningJob job = optionalJob.get();
            if (job.getStatus() == FineTuningStatus.TRAINING || job.getStatus() == FineTuningStatus.PREPARING) {
                FineTuningStatus currentStatus = fineTuningPort.getTrainingStatus(jobId);
                int currentProgress = fineTuningPort.getTrainingProgress(jobId);
                job.setStatus(currentStatus);
                job.setProgress(currentProgress);
                if (currentStatus == FineTuningStatus.COMPLETED) {
                    job.setCompletedAt(Instant.now());
                }
                job = jobRepository.save(job);
            }
            return Optional.of(job);
        }

        return Optional.empty();
    }

    @Override
    public void deployModel(String jobId) {
        FineTuningJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        if (job.getStatus() != FineTuningStatus.COMPLETED) {
            throw new IllegalStateException("Cannot deploy model: job status is " + job.getStatus() + ", expected COMPLETED");
        }

        String modelName = job.getOutputModelName() != null
                ? job.getOutputModelName()
                : job.getBaseModel() + "-ft-" + jobId.substring(0, 8);

        fineTuningPort.deployModel(jobId, modelName);

        job.setStatus(FineTuningStatus.DEPLOYED);
        job.setOutputModelName(modelName);
        jobRepository.save(job);
    }

    @Override
    public List<TrainingDataset> listDatasets() {
        return datasetRepository.findAll();
    }
}
