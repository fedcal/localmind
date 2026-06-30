package com.localmind.domain.llm.port.in;

import java.util.List;

public interface ConversationExportUseCase {
    byte[] exportToJson(String conversationId);
    byte[] exportToMarkdown(String conversationId);
    byte[] exportToPdf(String conversationId);
    byte[] exportMultipleToJson(List<String> conversationIds);
}
