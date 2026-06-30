package com.localmind.domain.llm.port.in;

import com.localmind.domain.llm.model.LlmRequest;
import com.localmind.domain.llm.model.LlmResponse;

public interface ChatUseCase {
    LlmResponse chat(LlmRequest request);
}
