package com.localmind.sdk;

/**
 * Options for a chat request. Use the builder pattern for convenience.
 *
 * <pre>{@code
 * ChatOptions options = ChatOptions.builder()
 *     .provider("OLLAMA")
 *     .model("llama3")
 *     .enableRag(true)
 *     .build();
 * }</pre>
 */
public class ChatOptions {

    private String provider;
    private String model;
    private String conversationId;
    private String systemPrompt;
    private double temperature = 0.7;
    private int maxTokens;
    private boolean enableToolCalling;
    private boolean enableRag;

    private ChatOptions() {
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public boolean isEnableToolCalling() {
        return enableToolCalling;
    }

    public boolean isEnableRag() {
        return enableRag;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ChatOptions options = new ChatOptions();

        /**
         * LLM provider: OLLAMA, OPENAI, ANTHROPIC, GOOGLE.
         */
        public Builder provider(String provider) {
            options.provider = provider;
            return this;
        }

        /**
         * Specific model name (e.g. "llama3", "gpt-4o", "claude-sonnet-4-20250514").
         */
        public Builder model(String model) {
            options.model = model;
            return this;
        }

        /**
         * Existing conversation ID to continue a conversation.
         */
        public Builder conversationId(String conversationId) {
            options.conversationId = conversationId;
            return this;
        }

        /**
         * System prompt for the conversation.
         */
        public Builder systemPrompt(String systemPrompt) {
            options.systemPrompt = systemPrompt;
            return this;
        }

        /**
         * Temperature for generation (0.0 - 2.0, default 0.7).
         */
        public Builder temperature(double temperature) {
            options.temperature = temperature;
            return this;
        }

        /**
         * Maximum tokens to generate.
         */
        public Builder maxTokens(int maxTokens) {
            options.maxTokens = maxTokens;
            return this;
        }

        /**
         * Enable MCP tool calling in the response loop.
         */
        public Builder enableToolCalling(boolean enableToolCalling) {
            options.enableToolCalling = enableToolCalling;
            return this;
        }

        /**
         * Enable RAG (Retrieval-Augmented Generation) from indexed documents.
         */
        public Builder enableRag(boolean enableRag) {
            options.enableRag = enableRag;
            return this;
        }

        public ChatOptions build() {
            return options;
        }
    }
}
