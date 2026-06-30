package com.localmind.domain.llm.port.in;

import com.localmind.domain.llm.model.LlmModel;
import com.localmind.domain.llm.model.LlmProvider;

import java.util.List;

public interface ModelManagementUseCase {
    List<LlmModel> listModels();
    List<LlmModel> listModelsByProvider(LlmProvider provider);
    LlmModel getModel(String modelId);
}
