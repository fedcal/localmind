package com.localmind.domain.llm.port.out;

import java.util.List;

public interface OllamaModelPort {
    List<String> listAvailableModels(String baseUrl);
}
