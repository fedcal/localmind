package com.localmind.domain.common.event;

/**
 * Evento emesso quando una conversazione LLM viene completata.
 * Event emitted when an LLM conversation is completed.
 */
public class ConversationCompletedEvent extends DomainEvent {

    private final String conversationId;
    private final int messageCount;
    private final String provider;
    private final String modelName;
    private final long tokensUsed;

    public ConversationCompletedEvent(String conversationId, int messageCount,
                                       String provider, String modelName, long tokensUsed) {
        super("CONVERSATION_COMPLETED");
        this.conversationId = conversationId;
        this.messageCount = messageCount;
        this.provider = provider;
        this.modelName = modelName;
        this.tokensUsed = tokensUsed;
    }

    public String getConversationId() { return conversationId; }
    public int getMessageCount() { return messageCount; }
    public String getProvider() { return provider; }
    public String getModelName() { return modelName; }
    public long getTokensUsed() { return tokensUsed; }
}
