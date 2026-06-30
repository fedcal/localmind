export const content = `# Functional Tests - LocalMind

This document lists all functional tests for the LocalMind application, organized by module. Each test verifies an end-to-end flow from the end user's perspective.

**Total tests: 211** | **Areas covered: 17**

---

## Table of Contents

1. [Authentication](#1-authentication)
2. [LLM Chat](#2-llm-chat)
3. [Conversation Management](#3-conversation-management)
4. [Document Management](#4-document-management)
5. [Semantic Search](#5-semantic-search)
6. [Monitored Folders](#6-monitored-folders)
7. [LLM Provider Configuration](#7-llm-provider-configuration)
8. [MCP Servers](#8-mcp-servers)
9. [MCP Tools](#9-mcp-tools)
10. [LLM Models](#10-llm-models)
11. [Webhooks and Automation](#11-webhooks-and-automation)
12. [Dashboard](#12-dashboard)
13. [Specialized Agents](#13-specialized-agents)
14. [Internationalization](#14-internationalization)
15. [Navigation and Layout](#15-navigation-and-layout)
16. [Cross-Cutting Error Handling](#16-cross-cutting-error-handling)
17. [MCP Scrum, Time and Incidents](#17-mcp-scrum-time-and-incidents)

---

## 1. Authentication

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| AUTH-01 | Initial password setup | First run, no password configured | Setup form visible, after submit redirect to chat with JWT token |
| AUTH-02 | Setup - passwords don't match | \`password\` != \`confirmPassword\` | Validation error, no account created |
| AUTH-03 | Setup - empty password | Empty password field | @NotBlank validation, error 400 |
| AUTH-04 | Setup - duplicate setup | Setup attempt when password already configured | Error, setup already completed |
| AUTH-05 | Login with correct password | Valid credentials | JWT token returned, redirect to chat |
| AUTH-06 | Login with wrong password | Wrong password | Error 401, "invalid credentials" message |
| AUTH-07 | Login with empty field | Password not provided | Validation 400 |
| AUTH-08 | Check authentication status | GET /auth/status | \`configured: true/false\`, \`authenticated: true/false\` |
| AUTH-09 | Successful password change | Valid current password + new password | Password updated, 204 |
| AUTH-10 | Password change - wrong current | Wrong current password | Error 401 |
| AUTH-11 | Expired token | Request with token beyond 24h | Error 401, redirect to login |
| AUTH-12 | Invalid/malformed token | Authorization header with corrupted token | Error 401 |
| AUTH-13 | Request without token | Access to protected endpoint without Authorization | Error 401, redirect to login |
| AUTH-14 | Angular guard on protected routes | Navigation to /chat without authentication | Redirect to /login |
| AUTH-15 | Logout | Click logout | Token removed from localStorage, redirect to /login |

---

## 2. LLM Chat

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| CHAT-01 | Send basic message | Simple text message | LLM response received, new conversation created |
| CHAT-02 | Send streaming message | Tool calling disabled | Tokens received via SSE one at a time, response composed progressively |
| CHAT-03 | Send synchronous message | Tool calling enabled | Complete response received in one block |
| CHAT-04 | Empty message | Empty message field, submit | @NotBlank validation, error 400 |
| CHAT-05 | Select specific provider | Choose OPENAI from dropdown | Request routed to chosen provider |
| CHAT-06 | Select specific model | Choose llama3.2 | Response from selected model |
| CHAT-07 | Provider fallback | Primary provider unavailable | Response from next provider in chain |
| CHAT-08 | All providers unavailable | No provider reachable | Error 502 with LlmProviderException message |
| CHAT-09 | Continue conversation | Send message with existing conversationId | Message added to existing conversation |
| CHAT-10 | Custom system prompt | Set system prompt before sending | LLM follows system prompt instructions |
| CHAT-11 | System prompt - character limit | System prompt > 5000 characters | Truncation or frontend validation |
| CHAT-12 | RAG enabled | \`enableRag: true\` with indexed documents | Response contains ragSources with documentId, score |
| CHAT-13 | RAG without documents | \`enableRag: true\` but no indexed documents | Normal response without RAG sources |
| CHAT-14 | Tool calling enabled | \`enableToolCalling: true\` with available MCP tools | Agentic loop (max 3 iterations), tools executed |
| CHAT-15 | Tool calling - max iterations | Tool requiring more than 3 iterations | Stops at 3 iterations, partial response |
| CHAT-16 | Temperature parameter | \`temperature: 0.1\` vs \`temperature: 1.0\` | More deterministic vs more creative responses |
| CHAT-17 | MaxTokens parameter | \`maxTokens: 50\` | Response truncated within limit |
| CHAT-18 | Usage tracking | Successful message send | UsageRecord saved (provider, model, tokens, latency) |
| CHAT-19 | SSE - conversation events | Start streaming | \`conversation\` event with conversationId |
| CHAT-20 | SSE - done event | End streaming | \`done\` event received, UI updates state |
| CHAT-21 | SSE - error event | Error during streaming | \`error\` event with message, UI shows error |
| CHAT-22 | SSE - metadata | End streaming | \`metadata\` event with model, provider, latencyMs, tokenUsage |

---

## 3. Conversation Management

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| CONV-01 | List conversations | GET /conversations | List ordered by updatedAt DESC |
| CONV-02 | Paginated list | \`page=0, size=20\` | First page of 20 conversations with pagination metadata |
| CONV-03 | Pagination - next page | \`page=1, size=20\` with >20 conversations | Correct second page |
| CONV-04 | Search by content | \`query=keyword\` | Only conversations containing the term |
| CONV-05 | Search without results | \`query=xyznonexistent\` | Empty list |
| CONV-06 | Conversation detail | GET /conversations/{id} | Complete conversation with all messages |
| CONV-07 | Non-existent conversation | GET /conversations/{invalid-id} | Error 404 |
| CONV-08 | Rename conversation | PATCH with new title | Title updated |
| CONV-09 | Rename - empty title | Blank title | @NotBlank validation, error 400 |
| CONV-10 | Auto-title | New conversation from long message | Title = first 100 characters + "..." |
| CONV-11 | Update system prompt | PATCH /system-prompt | System prompt updated for conversation |
| CONV-12 | Update context window | PATCH /context-window with max=10 | Only last 10 messages sent to LLM |
| CONV-13 | Add tag | POST /tags with \`tag: "project-x"\` | Tag added, visible in list |
| CONV-14 | Remove tag | DELETE /tags/{tag} | Tag removed |
| CONV-15 | Filter by tag | GET /conversations?tag=project-x | Only conversations with that tag |
| CONV-16 | Add empty tag | \`tag: ""\` | @NotBlank validation, error 400 |
| CONV-17 | Delete conversation | DELETE /conversations/{id} | Conversation removed, 204 |
| CONV-18 | Delete - confirmation modal | Click delete in frontend | Confirmation modal before deletion |
| CONV-19 | Add tool result | POST /tool-result with toolName and content | TOOL message added to conversation |
| CONV-20 | Tool result - empty fields | toolName or content empty | @NotBlank validation, error 400 |
| CONV-21 | Conversation metadata | Update metadata with custom keys | Metadata saved and returned |

---

## 4. Document Management

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| DOC-01 | Upload PDF | Valid .pdf file | Document created with status PENDING -> INDEXED |
| DOC-02 | Upload TXT | .txt file | Text extracted and indexed |
| DOC-03 | Upload Markdown | .md file | Markdown content extracted |
| DOC-04 | Upload DOCX | .docx file | Text extracted from Word |
| DOC-05 | Upload CSV | .csv file | Tabular content extracted |
| DOC-06 | Upload JSON | .json file | JSON content extracted |
| DOC-07 | Upload duplicate file | Same file (same SHA-256 hash) | Existing document returned, no duplicate |
| DOC-08 | Upload empty file | 0 byte file | Error handling or empty document |
| DOC-09 | Upload without file | POST request without multipart file | Error 400 |
| DOC-10 | List documents | GET /documents | All documents with status, size, dates |
| DOC-11 | Filter by status frontend | Tab "Indexed" / "Pending" / "Failed" | Only documents with selected status |
| DOC-12 | Document detail | GET /documents/{id} | All document fields |
| DOC-13 | Non-existent document | GET /documents/{invalid-id} | Error 404 |
| DOC-14 | Delete document | DELETE /documents/{id} | Document + chunks + vectors deleted (cascade) |
| DOC-15 | Delete - confirmation modal | Click delete in frontend | Confirmation modal before deletion |
| DOC-16 | Complete ingestion pipeline | Upload -> extraction -> chunking -> embedding -> indexing | Status progression: PENDING -> PROCESSING -> INDEXED |
| DOC-17 | Pipeline - extraction error | Corrupted file | Status = FAILED, DocumentProcessingException |
| DOC-18 | Chunking with overlap | Long document | Chunks with correct overlap |
| DOC-19 | Chunking - short text | Text < chunkSize | Single chunk |
| DOC-20 | MIME type detection | Upload .pdf, .docx, .md | Correct MIME type automatically assigned |
| DOC-21 | SHA-256 hash | Upload document | Hash computed and saved for deduplication |

---

## 5. Semantic Search

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| SEARCH-01 | Basic search | Text query with indexed documents | Results ordered by similarity score |
| SEARCH-02 | Search with custom topK | \`topK: 3\` | Exactly 3 results (if available) |
| SEARCH-03 | Search without results | Query unrelated to documents | Empty list or results with low score |
| SEARCH-04 | Empty query search | \`query: ""\` | @NotBlank validation, error 400 |
| SEARCH-05 | Search without documents | No indexed documents | Empty list |
| SEARCH-06 | Similarity score | Relevant query | Score between 0 and 1, most relevant results on top |
| SEARCH-07 | Chunk content in result | Search with match | content, documentId, filename, chunkIndex present |
| SEARCH-08 | TopK values frontend | Selection 3, 5, 10, 20 | Number of results matches selection |
| SEARCH-09 | Search hints | Click on suggestion in frontend | Query auto-filled and search executed |

---

## 6. Monitored Folders

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| FOLD-01 | Add folder | Valid path | Folder created with status ACTIVE |
| FOLD-02 | Add folder - empty path | \`path: ""\` | @NotBlank validation, error 400 |
| FOLD-03 | Add folder - non-existent path | Path that doesn't exist on filesystem | Appropriate error handling |
| FOLD-04 | Recursive folder | \`recursive: true\` | Subdirectories included in scan |
| FOLD-05 | Non-recursive folder | \`recursive: false\` | Only files in root directory |
| FOLD-06 | Watch enabled | \`watchEnabled: true\` | File system monitoring active |
| FOLD-07 | List folders | GET /folders | All folders with status and document count |
| FOLD-08 | Remove folder | DELETE /folders/{id} | Folder removed, 204 |
| FOLD-09 | Manual sync | POST /folders/{id}/sync | Status SYNCING -> documents ingested -> ACTIVE |
| FOLD-10 | Sync - error | Folder no longer accessible | Status = ERROR |
| FOLD-11 | Sync - duplicate files | Already indexed files in folder | Deduplication via hash, no duplicates |
| FOLD-12 | Document count | After sync | documentCount updated correctly |
| FOLD-13 | Last sync | After completed sync | lastSyncAt updated |

---

## 7. LLM Provider Configuration

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| SETT-01 | List providers | GET /settings/providers | All configured providers |
| SETT-02 | Create Ollama provider | Name, type OLLAMA, baseUrl | Provider created successfully |
| SETT-03 | Create OpenAI provider | Name, type OPENAI, baseUrl, apiKey | Provider created |
| SETT-04 | Create provider - empty name | \`name: ""\` | @NotBlank validation, error 400 |
| SETT-05 | Create provider - null type | \`type: null\` | @NotNull validation, error 400 |
| SETT-06 | Create provider - empty baseUrl | \`baseUrl: ""\` | @NotBlank validation, error 400 |
| SETT-07 | Delete provider | DELETE /settings/providers/{id} | Provider removed, 204 |
| SETT-08 | Test Ollama connection | Configured and reachable Ollama provider | Status "success" |
| SETT-09 | Test connection - offline provider | Unreachable provider | Status "error" with message |
| SETT-10 | Test OpenAI connection | Valid API key | Status "success" (key presence check) |
| SETT-11 | Ollama status - online | Ollama running | \`online: true\`, version, model list |
| SETT-12 | Ollama status - offline | Ollama not running | \`online: false\`, errorMessage |
| SETT-13 | List Ollama models | Ollama online with models | List of downloaded model names |
| SETT-14 | Pull Ollama model | \`modelName: "llama3.2"\` | Download started, model available |
| SETT-15 | Pull model - streaming | GET /ollama/models/pull/stream | SSE progress events with download status |
| SETT-16 | Delete Ollama model | DELETE /ollama/models/{modelName} | Model removed |
| SETT-17 | Pull model - empty fields | baseUrl or modelName empty | @NotBlank validation, error 400 |
| SETT-18 | Update default model | PUT /{id}/default-model | defaultModel updated |
| SETT-19 | Start Ollama | POST /ollama/start | Service start attempt |
| SETT-20 | Frontend form validation | Incomplete form | Inline errors, submit disabled |

---

## 8. MCP Servers

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| MCP-S01 | Register STDIO server | Type STDIO with command and args | Server created, connection attempted |
| MCP-S02 | Register SSE server | Type SSE with URL | Server created, connection attempted |
| MCP-S03 | Register - empty name | \`name: ""\` | @NotBlank validation, error 400 |
| MCP-S04 | Register - null type | \`type: null\` | @NotNull validation, error 400 |
| MCP-S05 | List servers | GET /mcp/servers | All servers with status |
| MCP-S06 | Server detail | GET /mcp/servers/{id} | Full config + status + timestamp |
| MCP-S07 | Delete server | DELETE /mcp/servers/{id} | Server disconnected and removed, 204 |
| MCP-S08 | Test connection - active server | Reachable server | Status CONNECTED |
| MCP-S09 | Test connection - offline server | Unreachable server | Status ERROR |
| MCP-S10 | Reconnection | POST /reconnect on disconnected server | Status CONNECTING -> CONNECTED |
| MCP-S11 | Configurable timeout | \`timeoutSeconds: 60\` | Timeout respected in calls |
| MCP-S12 | Auto-reconnect | \`autoReconnect: true\`, server restarted | Automatic reconnection |
| MCP-S13 | Non-existent server | GET /mcp/servers/{invalid-id} | Error 404 |

---

## 9. MCP Tools

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| MCP-T01 | List external tools | Connected server with tools | List with name, description, inputSchema |
| MCP-T02 | List tools by server | GET /tools/servers/{serverId} | Only tools from specified server |
| MCP-T03 | List local tools | GET /tools/local | Built-in tools (135 native) |
| MCP-T04 | Execute tool successfully | Valid toolName, serverId, arguments | result with success=true, executionTimeMs |
| MCP-T05 | Execute tool - error | Tool that fails | success=false, errorMessage present |
| MCP-T06 | Execute tool - empty name | \`toolName: ""\` | @NotBlank validation, error 400 |
| MCP-T07 | Execute tool - empty serverId | \`serverId: ""\` | @NotBlank validation, error 400 |
| MCP-T08 | Tool from disconnected server | Server with status != CONNECTED | Error, tool unavailable |
| MCP-T09 | Tool result in conversation | Tool execution during chat | TOOL message added with metadata |

---

## 10. LLM Models

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| MOD-01 | List all models | GET /models | Models from all available providers |
| MOD-02 | Models by provider | Filter by OLLAMA | Only Ollama models |
| MOD-03 | Model detail | GET /models/{id} | name, provider, contextWindow, available |
| MOD-04 | Non-existent model | GET /models/{invalid-id} | Error 404 |
| MOD-05 | Model availability | Provider offline | \`available: false\` |

---

## 11. Webhooks and Automation

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| HOOK-01 | Create webhook | Valid name, URL, eventType | Webhook created with active=true |
| HOOK-02 | Create - empty name | \`name: ""\` | @NotBlank validation, error 400 |
| HOOK-03 | Create - name too short | \`name: "a"\` (< 2 chars) | @Size validation, error 400 |
| HOOK-04 | Create - empty URL | \`url: ""\` | @NotBlank validation, error 400 |
| HOOK-05 | Create - empty eventType | \`eventType: ""\` | @NotBlank validation, error 400 |
| HOOK-06 | List webhooks | GET /webhooks | All webhooks with status |
| HOOK-07 | Webhook detail | GET /webhooks/{id} | All fields |
| HOOK-08 | Update webhook | PUT /webhooks/{id} | Fields updated |
| HOOK-09 | Delete webhook | DELETE /webhooks/{id} | Webhook removed, 204 |
| HOOK-10 | Test webhook | POST /webhooks/{id}/test | HTTP call to configured URL |
| HOOK-11 | Non-existent webhook | GET /webhooks/{invalid-id} | Error 404 |
| HOOK-12 | Trigger DOCUMENT_INDEXED | Upload and index document | Webhook with eventType DOCUMENT_INDEXED invoked |
| HOOK-13 | Trigger CHAT_COMPLETED | Chat completion | Webhook with eventType CHAT_COMPLETED invoked |
| HOOK-14 | Disabled webhook | \`active: false\` + event trigger | Webhook NOT invoked |

---

## 12. Dashboard

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| DASH-01 | Health check - all UP | All services active | \`status: "UP"\`, all services "UP" |
| DASH-02 | Health check - degraded | Ollama offline | \`status: "DEGRADED"\`, ollama: "DOWN" |
| DASH-03 | Document statistics | Documents present | Total document count |
| DASH-04 | MCP statistics | Connected servers | Connected server count |
| DASH-05 | Quick actions | Click on action card | Navigation to correct feature |
| DASH-06 | Refresh stats | Click refresh | Data updated |

---

## 13. Specialized Agents

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| AGT-01 | TECH agent | Technical query | Response with technical analysis and citations |
| AGT-02 | BUSINESS agent | Business query | Response with business analysis |
| AGT-03 | LEGAL agent | Legal query | Response with regulatory references |
| AGT-04 | PERSONAL agent | General query | Simple and friendly response |
| AGT-05 | Agent with tool calls | Query requiring tools | Tools invoked, results integrated |

---

## 14. Internationalization

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| I18N-01 | Default language | First access | Interface in Italian |
| I18N-02 | Switch to English | Click EN flag | Entire interface translated to English |
| I18N-03 | Switch to Italian | Click IT flag | Entire interface translated to Italian |
| I18N-04 | Language persistence | Change language + page reload | Language maintained (localStorage) |
| I18N-05 | Parameter interpolation | \`{{count}} documents\` with count=5 | "5 documents" |
| I18N-06 | Translated enums | Document status in UI | "Indicizzato" (IT) / "Indexed" (EN) |
| I18N-07 | Validation translation | Required field error | "Campo obbligatorio" (IT) / "Required field" (EN) |

---

## 15. Navigation and Layout

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| NAV-01 | Sidebar navigation | Click on each link | Navigation to correct page |
| NAV-02 | Active route highlighted | Chat page open | "Chat" link highlighted in sidebar |
| NAV-03 | Collapsible sidebar | Click toggle sidebar | Sidebar reduced to icons |
| NAV-04 | 404 page | Non-existent URL | "Not Found" page |
| NAV-05 | Lazy loading | First navigation to feature | Module loaded on demand |
| NAV-06 | Dark/light theme | Toggle theme | CSS variables updated, persistent theme |
| NAV-07 | Persistent theme | Change theme + reload | Theme maintained (localStorage) |

---

## 16. Cross-Cutting Error Handling

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| ERR-01 | Error 400 - validation | Invalid body on any endpoint | ErrorResponseDto with field and message |
| ERR-02 | Error 401 - unauthenticated | Missing or expired token | ErrorResponseDto, redirect to login |
| ERR-03 | Error 404 - resource not found | Non-existent ID | ErrorResponseDto with "not found" |
| ERR-04 | Error 500 - internal error | Unhandled exception | Generic ErrorResponseDto |
| ERR-05 | Error 502 - LLM provider | Error from AI provider | ErrorResponseDto with "LLM provider error" |
| ERR-06 | Toast notifications | Any HTTP error | Toast visible for 4 seconds |
| ERR-07 | Loading skeleton | Data loading | Skeleton placeholder visible |
| ERR-08 | Disabled buttons | Async operation in progress | Button disabled, no double submit |

---

## 17. MCP Scrum, Time and Incidents

### Scrum

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| SCRUM-01 | Create sprint | Name, dates, goals | Sprint created |
| SCRUM-02 | Create user story | Title, description, points | Story created in backlog |
| SCRUM-03 | Create task | Title, storyId | Task created with status TODO |
| SCRUM-04 | Update task status | \`status: "in_progress"\` | Status updated |
| SCRUM-05 | Sprint board | GET /sprints/{id}/board | Tasks grouped by status |
| SCRUM-06 | Backlog | GET /backlog | Stories not assigned to sprint |

### Time Tracking

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| TIME-01 | Start timer | POST /time/start | Timer started |
| TIME-02 | Stop timer | POST /time/stop | Timer stopped, time recorded |
| TIME-03 | Manual time log | POST /time/log | Entry created |
| TIME-04 | Timesheet | GET /time/timesheet with date range | Entries filtered by period |

### Incident Management

| ID | Test | Scenario | Expected Result |
|----|------|----------|-----------------|
| INC-01 | Open incident | POST /incidents | Incident created with severity |
| INC-02 | Update incident | PUT /incidents/{id} | Fields updated |
| INC-03 | Add timeline | POST /incidents/{id}/timeline | Entry added |
| INC-04 | Resolve incident | POST /incidents/{id}/resolve | Status resolved |
| INC-05 | Generate postmortem | GET /incidents/{id}/postmortem | Postmortem report generated |
| INC-06 | Filter incidents | \`status=open, severity=high\` | Filtered list |

---

## Summary

| Area | Tests |
|------|-------|
| Authentication | 15 |
| LLM Chat | 22 |
| Conversations | 21 |
| Documents | 21 |
| Semantic Search | 9 |
| Monitored Folders | 13 |
| Provider Settings | 20 |
| MCP Servers | 13 |
| MCP Tools | 9 |
| LLM Models | 5 |
| Webhooks/Automation | 14 |
| Dashboard | 6 |
| Agents | 5 |
| Internationalization | 7 |
| Navigation/Layout | 7 |
| Error Handling | 8 |
| Scrum/Time/Incidents | 16 |
| **TOTAL** | **211** |
`;
