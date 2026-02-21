package com.localmind.infrastructure.finetuning.persistence.adapter;

import com.localmind.domain.finetuning.model.TrainingDataset;
import com.localmind.domain.finetuning.port.out.TrainingDatasetRepository;
import com.localmind.infrastructure.finetuning.persistence.entity.TrainingDatasetEntity;
import com.localmind.infrastructure.finetuning.persistence.repository.TrainingDatasetJpaRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TrainingDatasetRepositoryAdapter implements TrainingDatasetRepository {

    private final TrainingDatasetJpaRepository jpaRepository;

    public TrainingDatasetRepositoryAdapter(TrainingDatasetJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TrainingDataset save(TrainingDataset dataset) {
        TrainingDatasetEntity entity = toEntity(dataset);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<TrainingDataset> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<TrainingDataset> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private TrainingDatasetEntity toEntity(TrainingDataset dataset) {
        String documentIdsStr = dataset.getDocumentIds() != null
                ? String.join(",", dataset.getDocumentIds())
                : null;

        return TrainingDatasetEntity.builder()
                .id(UUID.fromString(dataset.getId()))
                .name(dataset.getName())
                .documentIds(documentIdsStr)
                .sampleCount(dataset.getSampleCount())
                .format(dataset.getFormat())
                .createdAt(dataset.getCreatedAt())
                .build();
    }

    private TrainingDataset toDomain(TrainingDatasetEntity entity) {
        List<String> documentIds = entity.getDocumentIds() != null && !entity.getDocumentIds().isEmpty()
                ? Arrays.asList(entity.getDocumentIds().split(","))
                : new ArrayList<>();

        return TrainingDataset.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .documentIds(documentIds)
                .sampleCount(entity.getSampleCount())
                .format(entity.getFormat())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
