# LocalMind SDK for JavaScript/TypeScript

TypeScript SDK for integrating with the [LocalMind](https://github.com/localmind) API. Provides a fully typed async client for chat, document management, semantic search, and conversation operations. Works in both Node.js (18+) and browser environments.

## Requirements

- Node.js 18+ (uses native `fetch`)
- No runtime dependencies

## Installation

```bash
npm install @localmind/sdk
```

### Build from source

```bash
cd localmind-sdk-js
npm install
npm run build
```

## Quick Start

```typescript
import { LocalMindClient } from '@localmind/sdk';

// Create client (defaults to http://localhost:8080/api/v1)
const client = new LocalMindClient();

// Authenticate (if auth is configured on the server)
await client.login('your-password');

// Simple chat
const response = await client.chat('Riassumi il documento...');
console.log(response.content);

// Chat with provider/model selection
const response = await client.chat('Explain this concept', {
  provider: 'OLLAMA',
  model: 'llama3',
});

// Chat with RAG enabled
const response = await client.chat('What does the deployment guide say?', {
  enableRag: true,
  temperature: 0.3,
});
console.log(response.content);
if (response.ragSources) {
  for (const source of response.ragSources) {
    console.log(`  Source: ${source.filename} (score: ${source.score.toFixed(2)})`);
  }
}
```

## Document Management

```typescript
// List all documents
const documents = await client.documents.list();

// Upload a document (Node.js)
const doc = await client.documents.upload('/path/to/report.pdf');
console.log(`Uploaded: ${doc.filename} (status: ${doc.status})`);

// Upload a document (browser)
const fileInput = document.querySelector<HTMLInputElement>('#fileInput');
const file = fileInput.files[0];
const doc = await client.documents.uploadBlob(file, file.name);

// Get document details
const doc = await client.documents.get('document-id');

// Delete a document
await client.documents.delete('document-id');
```

## Semantic Search

```typescript
// Search with default topK (5)
const results = await client.search('deployment procedure');

// Search with custom topK
const results = await client.search('security policy', 10);
for (const result of results) {
  console.log(`${result.score.toFixed(2)} - ${result.filename}: ${result.content.slice(0, 100)}`);
}
```

## Conversation Management

```typescript
// List all conversations
const conversations = await client.conversations.list();

// List by tag
const tagged = await client.conversations.list('project-x');

// Get full conversation with messages
const convo = await client.conversations.getById('conversation-id');
for (const msg of convo.messages) {
  console.log(`${msg.role}: ${msg.content}`);
}

// Continue a conversation
const reply = await client.chat('Follow-up question', {
  conversationId: convo.id,
});

// Rename a conversation
await client.conversations.rename('conversation-id', 'New Title');

// Update system prompt
await client.conversations.updateSystemPrompt('conversation-id', 'You are a helpful assistant.');

// Tag management
await client.conversations.addTag('conversation-id', 'important');
await client.conversations.removeTag('conversation-id', 'old-tag');

// Export to PDF
const pdfBuffer = await client.conversations.export('conversation-id', 'pdf');

// Delete a conversation
await client.conversations.delete('conversation-id');
```

## Health Check

```typescript
const health = await client.health();
console.log(`Status: ${health.status}`);       // UP or DEGRADED
console.log(`Services:`, health.services);     // { api: "UP", ollama: "UP" }
```

## Configuration

```typescript
const client = new LocalMindClient({
  baseUrl: 'http://my-server:8080/api/v1',     // Custom server URL
  authToken: 'pre-existing-jwt-token',          // Pre-set auth token
  timeoutMs: 300_000,                            // Request timeout in ms (default: 120000)
});
```

## Error Handling

All API errors throw `LocalMindException` with the HTTP status code and response body:

```typescript
import { LocalMindClient, LocalMindException } from '@localmind/sdk';

const client = new LocalMindClient();
try {
  await client.chat('Hello');
} catch (error) {
  if (error instanceof LocalMindException) {
    console.error(`Status: ${error.statusCode}`);         // e.g. 401, 500
    console.error(`Body: ${error.responseBody}`);         // Raw server response
    console.error(`Message: ${error.message}`);
  }
}
```

## TypeScript Types

All response types are fully typed and exported:

```typescript
import type {
  ChatResponse,
  ChatOptions,
  Document,
  SearchResult,
  Conversation,
  ConversationSummary,
  ChatMessage,
  TokenUsage,
  RagSource,
  HealthStatus,
} from '@localmind/sdk';
```

## License

MIT
