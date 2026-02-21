package com.localmind.sdk;

/**
 * Token usage statistics from an LLM response.
 */
public class TokenUsage {

    private int promptTokens;
    private int completionTokens;
    private int totalTokens;

    public TokenUsage() {
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }

    @Override
    public String toString() {
        return "TokenUsage{prompt=" + promptTokens
                + ", completion=" + completionTokens
                + ", total=" + totalTokens + "}";
    }
}
