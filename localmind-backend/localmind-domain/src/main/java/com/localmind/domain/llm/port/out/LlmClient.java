package com.localmind.domain.llm.port.out;

import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmRequest;
import com.localmind.domain.llm.model.LlmResponse;

public interface LlmClient {
    LlmResponse call(LlmRequest request);
    LlmProvider getProvider();
    boolean isAvailable();
}
