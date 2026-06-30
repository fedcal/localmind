package com.localmind.domain.finetuning.port.in;

import com.localmind.domain.finetuning.model.FineTuningJob;
import com.localmind.domain.finetuning.model.TrainingDataset;
import com.localmind.domain.finetuning.model.TrainingTechnique;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FineTuningUseCase {

    TrainingDataset createDataset(String name, List<String> documentIds);

    FineTuningJob startTraining(String datasetId, String baseModel, TrainingTechnique technique, Map<String, String> hyperparameters);

    List<FineTuningJob> listJobs();

    Optional<FineTuningJob> getJob(String jobId);

    void deployModel(String jobId);

    List<TrainingDataset> listDatasets();
}
