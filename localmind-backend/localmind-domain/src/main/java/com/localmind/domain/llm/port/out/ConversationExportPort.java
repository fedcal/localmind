package com.localmind.domain.llm.port.out;

import com.localmind.domain.llm.model.ConversationContext;

import java.util.List;

public interface ConversationExportPort {
    byte[] toJson(ConversationContext conversation);
    byte[] toMarkdown(ConversationContext conversation);
    byte[] toPdf(ConversationContext conversation);
    byte[] multipleToJson(List<ConversationContext> conversations);
}
