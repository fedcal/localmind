package com.localmind.shared.contract;

/**
 * Contratto API per il futuro Chat Microservice.
 * Definisce le operazioni che il servizio chat esporra' come microservizio.
 *
 * API contract for the future Chat Microservice.
 * Defines the operations that the chat service will expose as a microservice.
 */
public interface ChatServiceContract {
    // POST /api/v1/chat
    // POST /api/v1/chat/stream
    // GET /api/v1/conversations
    // GET /api/v1/conversations/{id}
    // DELETE /api/v1/conversations/{id}
    // GET /api/v1/conversations/{id}/export
    // POST /api/v1/conversations/import
}
