package com.localmind.domain.finetuning.port.out;

import com.localmind.domain.finetuning.model.FineTuningJob;

import java.util.List;
import java.util.Optional;

public interface FineTuningJobRepository {

    FineTuningJob save(FineTuningJob job);

    Optional<FineTuningJob> findById(String id);

    List<FineTuningJob> findAll();

    void deleteById(String id);
}
