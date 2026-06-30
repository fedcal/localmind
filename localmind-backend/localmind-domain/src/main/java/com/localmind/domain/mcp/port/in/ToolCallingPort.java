package com.localmind.domain.mcp.port.in;

import com.localmind.domain.mcp.model.ToolCallRequest;
import com.localmind.domain.mcp.model.ToolCallResponse;

import java.util.Optional;

/**
 * Port per il tool calling agentico dal chat.
 * Permette al layer API di interagire con i tool MCP senza dipendere dall'infrastruttura.
 */
public interface ToolCallingPort {

    /**
     * Genera il system prompt che descrive i tool disponibili al LLM.
     */
    String buildToolsSystemPrompt();

    /**
     * Analizza la risposta del LLM per cercare un tool_call JSON.
     */
    Optional<ToolCallRequest> parseToolCallFromResponse(String response);

    /**
     * Esegue un tool call e restituisce il risultato.
     */
    ToolCallResponse executeToolCall(ToolCallRequest request);

    /**
     * Verifica se ci sono tool disponibili.
     */
    boolean hasAvailableTools();
}
