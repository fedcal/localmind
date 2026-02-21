package com.localmind.domain.finetuning.port.out;

import com.localmind.domain.finetuning.model.FineTuningJob;
import com.localmind.domain.finetuning.model.FineTuningStatus;

import java.util.List;

public interface FineTuningPort {

    String prepareDataset(String datasetId, List<String> documentIds);

    void startTraining(FineTuningJob job);

    FineTuningStatus getTrainingStatus(String jobId);

    int getTrainingProgress(String jobId);

    void deployModel(String jobId, String modelName);

    boolean isAvailable();
}
