package com.localmind.domain.llm.port.in;

import com.localmind.domain.llm.model.ConversationContext;

import java.io.InputStream;
import java.util.List;

public interface ConversationImportUseCase {
    List<ConversationContext> importFromJson(InputStream data);
}
