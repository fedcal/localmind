package com.localmind.domain.llm.port.out;

import com.localmind.domain.llm.model.ConversationContext;

import java.io.InputStream;
import java.util.List;

public interface ConversationImportPort {
    List<ConversationContext> fromJson(InputStream data);
}
