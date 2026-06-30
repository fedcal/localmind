# LocalMind SDK for Java

Java SDK for integrating with the [LocalMind](https://github.com/localmind) API. Provides a type-safe client for chat, document management, semantic search, and conversation operations.

## Requirements

- Java 17+
- No external HTTP library required (uses `java.net.http.HttpClient`)
- Jackson Databind for JSON serialization

## Installation

### Maven

```xml
<dependency>
    <groupId>com.localmind</groupId>
    <artifactId>localmind-sdk-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Build from source

```bash
cd localmind-sdk-java
mvn install
```

## Quick Start

```java
import com.localmind.sdk.*;

// Create client (defaults to http://localhost:8080/api/v1)
var client = LocalMindClient.builder().build();

// Authenticate (if auth is configured on the server)
client.login("your-password");

// Simple chat
ChatResponse response = client.chat("Riassumi il documento...");
System.out.println(response.getContent());

// Chat with provider/model selection
ChatResponse response = client.chat("Explain this concept", "OLLAMA", "llama3");

// Chat with full options
ChatResponse response = client.chat("Analyze this document", ChatOptions.builder()
    .provider("OPENAI")
    .model("gpt-4o")
    .enableRag(true)
    .temperature(0.3)
    .build());
System.out.println("Response: " + response.getContent());
System.out.println("Conversation ID: " + response.getConversationId());
```

## Document Management

```java
// List all documents
List<Document> documents = client.documents().list();

// Upload a document
Document doc = client.documents().upload(new File("report.pdf"));
System.out.println("Uploaded: " + doc.getFilename() + " (status: " + doc.getStatus() + ")");

// Get document details
Document doc = client.documents().get("document-id");

// Delete a document
client.documents().delete("document-id");
```

## Semantic Search

```java
// Search with default topK (5)
List<SearchResult> results = client.search().query("deployment procedure");

// Search with custom topK
List<SearchResult> results = client.search().query("security policy", 10);
for (SearchResult result : results) {
    System.out.printf("%.2f - %s: %s%n", result.getScore(), result.getFilename(), result.getContent());
}
```

## Conversation Management

```java
// List all conversations
List<ConversationSummary> conversations = client.conversations().list();

// List by tag
List<ConversationSummary> tagged = client.conversations().listByTag("project-x");

// Get full conversation with messages
Conversation convo = client.conversations().getById("conversation-id");
for (ChatMessage msg : convo.getMessages()) {
    System.out.println(msg.getRole() + ": " + msg.getContent());
}

// Continue a conversation
ChatResponse reply = client.chat("Follow-up question", ChatOptions.builder()
    .conversationId(convo.getId())
    .build());

// Rename a conversation
client.conversations().rename("conversation-id", "New Title");

// Export to PDF
byte[] pdf = client.conversations().export("conversation-id", "pdf");
Files.write(Path.of("conversation.pdf"), pdf);

// Delete a conversation
client.conversations().delete("conversation-id");
```

## Health Check

```java
HealthStatus health = client.health();
System.out.println("Status: " + health.getStatus());       // UP or DEGRADED
System.out.println("Healthy: " + health.isHealthy());
System.out.println("Services: " + health.getServices());   // {api=UP, ollama=UP}
```

## Configuration

```java
var client = LocalMindClient.builder()
    .baseUrl("http://my-server:8080/api/v1")    // Custom server URL
    .authToken("pre-existing-jwt-token")         // Pre-set auth token
    .connectTimeout(Duration.ofSeconds(10))       // Connection timeout (default: 30s)
    .requestTimeout(Duration.ofMinutes(5))        // Request timeout (default: 120s)
    .build();
```

## Error Handling

All API errors throw `LocalMindException` with the HTTP status code and response body:

```java
try {
    client.chat("Hello");
} catch (LocalMindException e) {
    System.err.println("Status: " + e.getStatusCode());      // e.g. 401, 500
    System.err.println("Body: " + e.getResponseBody());       // Raw server response
    System.err.println("Message: " + e.getMessage());
}
```

## License

MIT
