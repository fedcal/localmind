package com.localmind.domain.finetuning.port.out;

import com.localmind.domain.finetuning.model.TrainingDataset;

import java.util.List;
import java.util.Optional;

public interface TrainingDatasetRepository {

    TrainingDataset save(TrainingDataset dataset);

    Optional<TrainingDataset> findById(String id);

    List<TrainingDataset> findAll();
}
