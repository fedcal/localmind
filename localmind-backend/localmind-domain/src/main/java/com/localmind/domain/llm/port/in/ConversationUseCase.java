package com.localmind.domain.llm.port.in;

import com.localmind.domain.common.model.PageRequest;
import com.localmind.domain.common.model.PageResponse;
import com.localmind.domain.llm.model.ConversationContext;

import java.util.List;
import java.util.Map;

public interface ConversationUseCase {

    List<ConversationContext> listAll();

    PageResponse<ConversationContext> listPaginated(PageRequest pageRequest);

    PageResponse<ConversationContext> search(String query, PageRequest pageRequest);

    ConversationContext getById(String id);

    ConversationContext rename(String id, String newTitle);

    void deleteById(String id);

    ConversationContext updateSystemPrompt(String id, String systemPrompt);

    ConversationContext updateMaxContextMessages(String id, Integer maxContextMessages);

    ConversationContext addTag(String id, String tag);

    ConversationContext removeTag(String id, String tag);

    List<ConversationContext> findByTag(String tag);

    ConversationContext updateMetadata(String id, Map<String, Object> metadata);
}
