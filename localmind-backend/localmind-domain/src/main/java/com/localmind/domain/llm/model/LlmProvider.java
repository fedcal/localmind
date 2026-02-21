package com.localmind.domain.llm.model;

import lombok.Getter;

@Getter
public enum LlmProvider {
    OLLAMA("Ollama", true),
    OPENAI("OpenAI", false),
    ANTHROPIC("Anthropic", false),
    GOOGLE("Google", false);

    private final String displayName;
    private final boolean local;

    LlmProvider(String displayName, boolean local) {
        this.displayName = displayName;
        this.local = local;
    }
}
