package com.localmind.domain.llm.port.out;

public interface MultimodalLlmClient extends LlmClient {
    boolean supportsMultimodal();
}
