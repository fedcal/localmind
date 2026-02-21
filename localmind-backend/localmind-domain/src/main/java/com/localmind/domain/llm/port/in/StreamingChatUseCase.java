package com.localmind.domain.llm.port.in;

import com.localmind.domain.llm.model.LlmRequest;
import com.localmind.domain.llm.model.LlmResponse;

import java.util.function.Consumer;

public interface StreamingChatUseCase {
    void streamChat(LlmRequest request,
                    Consumer<String> onToken,
                    Consumer<LlmResponse> onComplete,
                    Consumer<Exception> onError);
}
