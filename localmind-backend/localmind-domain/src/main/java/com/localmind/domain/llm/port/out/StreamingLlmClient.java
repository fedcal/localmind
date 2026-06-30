package com.localmind.domain.llm.port.out;

import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmRequest;
import com.localmind.domain.llm.model.TokenUsage;

import java.util.function.Consumer;

public interface StreamingLlmClient {
    void streamResponse(LlmRequest request,
                        Consumer<String> onToken,
                        Consumer<TokenUsage> onComplete,
                        Consumer<Exception> onError);

    LlmProvider getProvider();

    boolean isAvailable();
}
