package com.localmind.domain.llm.port.out;

import com.localmind.domain.common.model.PageRequest;
import com.localmind.domain.common.model.PageResponse;
import com.localmind.domain.llm.model.ConversationContext;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository {

    ConversationContext save(ConversationContext conversation);

    Optional<ConversationContext> findById(String id);

    List<ConversationContext> findAllOrderByUpdatedAtDesc();

    PageResponse<ConversationContext> findAllPaginated(PageRequest pageRequest);

    PageResponse<ConversationContext> searchByContent(String query, PageRequest pageRequest);

    List<ConversationContext> findByTag(String tag);

    void deleteById(String id);
}
