package com.localmind.domain.llm.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.port.in.ConversationExportUseCase;
import com.localmind.domain.llm.port.in.ConversationImportUseCase;
import com.localmind.domain.llm.port.out.ConversationExportPort;
import com.localmind.domain.llm.port.out.ConversationImportPort;
import com.localmind.domain.llm.port.out.ConversationRepository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConversationExportService implements ConversationExportUseCase, ConversationImportUseCase {

    private final ConversationRepository conversationRepository;
    private final ConversationExportPort conversationExportPort;
    private final ConversationImportPort conversationImportPort;

    public ConversationExportService(ConversationRepository conversationRepository,
                                     ConversationExportPort conversationExportPort,
                                     ConversationImportPort conversationImportPort) {
        this.conversationRepository = conversationRepository;
        this.conversationExportPort = conversationExportPort;
        this.conversationImportPort = conversationImportPort;
    }

    @Override
    public byte[] exportToJson(String conversationId) {
        ConversationContext conversation = findConversationOrThrow(conversationId);
        return conversationExportPort.toJson(conversation);
    }

    @Override
    public byte[] exportToMarkdown(String conversationId) {
        ConversationContext conversation = findConversationOrThrow(conversationId);
        return conversationExportPort.toMarkdown(conversation);
    }

    @Override
    public byte[] exportToPdf(String conversationId) {
        ConversationContext conversation = findConversationOrThrow(conversationId);
        return conversationExportPort.toPdf(conversation);
    }

    @Override
    public byte[] exportMultipleToJson(List<String> conversationIds) {
        List<ConversationContext> conversations = new ArrayList<>();
        for (String id : conversationIds) {
            conversations.add(findConversationOrThrow(id));
        }
        return conversationExportPort.multipleToJson(conversations);
    }

    @Override
    public List<ConversationContext> importFromJson(InputStream data) {
        List<ConversationContext> imported = conversationImportPort.fromJson(data);
        List<ConversationContext> saved = new ArrayList<>();
        for (ConversationContext conversation : imported) {
            saved.add(conversationRepository.save(conversation));
        }
        return saved;
    }

    private ConversationContext findConversationOrThrow(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
    }
}
