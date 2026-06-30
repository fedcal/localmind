package com.localmind.infrastructure.mcp.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindMcpPrompts {

    public String getRagQueryPrompt(String query, String context) {
        return String.format("""
                You are a helpful AI assistant with access to a knowledge base.

                Context from knowledge base:
                %s

                User query: %s

                Please provide a comprehensive answer based on the context provided.
                If the context doesn't contain relevant information, say so clearly.
                """, context, query);
    }

    public String getSummarizeDocumentPrompt(String content) {
        return String.format("""
                Please provide a concise summary of the following document:

                %s

                The summary should be 2-3 paragraphs highlighting the main points,
                key findings, and actionable insights.
                """, content);
    }
}
