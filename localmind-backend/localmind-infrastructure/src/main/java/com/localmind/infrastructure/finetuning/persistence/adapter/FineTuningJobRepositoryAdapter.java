package com.localmind.infrastructure.finetuning.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.finetuning.model.FineTuningJob;
import com.localmind.domain.finetuning.model.FineTuningStatus;
import com.localmind.domain.finetuning.model.TrainingTechnique;
import com.localmind.domain.finetuning.port.out.FineTuningJobRepository;
import com.localmind.infrastructure.finetuning.persistence.entity.FineTuningJobEntity;
import com.localmind.infrastructure.finetuning.persistence.repository.FineTuningJobJpaRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FineTuningJobRepositoryAdapter implements FineTuningJobRepository {

    private final FineTuningJobJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public FineTuningJobRepositoryAdapter(FineTuningJobJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public FineTuningJob save(FineTuningJob job) {
        FineTuningJobEntity entity = toEntity(job);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<FineTuningJob> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<FineTuningJob> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    private FineTuningJobEntity toEntity(FineTuningJob job) {
        String hyperparametersJson = null;
        if (job.getHyperparameters() != null) {
            try {
                hyperparametersJson = objectMapper.writeValueAsString(job.getHyperparameters());
            } catch (JsonProcessingException e) {
                hyperparametersJson = "{}";
            }
        }

        return FineTuningJobEntity.builder()
                .id(UUID.fromString(job.getId()))
                .baseModel(job.getBaseModel())
                .technique(job.getTechnique().name())
                .status(job.getStatus().name())
                .progress(job.getProgress())
                .datasetId(job.getDatasetId() != null ? UUID.fromString(job.getDatasetId()) : null)
                .outputModelName(job.getOutputModelName())
                .hyperparameters(hyperparametersJson)
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .error(job.getError())
                .createdAt(job.getCreatedAt())
                .build();
    }

    private FineTuningJob toDomain(FineTuningJobEntity entity) {
        Map<String, String> hyperparameters = null;
        if (entity.getHyperparameters() != null) {
            try {
                hyperparameters = objectMapper.readValue(entity.getHyperparameters(), new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                hyperparameters = new HashMap<>();
            }
        }

        return FineTuningJob.builder()
                .id(entity.getId().toString())
                .baseModel(entity.getBaseModel())
                .technique(TrainingTechnique.valueOf(entity.getTechnique()))
                .status(FineTuningStatus.valueOf(entity.getStatus()))
                .progress(entity.getProgress())
                .datasetId(entity.getDatasetId() != null ? entity.getDatasetId().toString() : null)
                .outputModelName(entity.getOutputModelName())
                .hyperparameters(hyperparameters)
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .error(entity.getError())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
