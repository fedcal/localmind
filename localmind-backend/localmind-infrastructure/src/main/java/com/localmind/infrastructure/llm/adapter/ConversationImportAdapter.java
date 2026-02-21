package com.localmind.infrastructure.llm.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.port.out.ConversationImportPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;

@Component
public class ConversationImportAdapter implements ConversationImportPort {

    private final ObjectMapper objectMapper;

    public ConversationImportAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ConversationContext> fromJson(InputStream data) {
        try {
            JsonNode root = objectMapper.readTree(data);
            return detectAndParse(root);
        } catch (IOException e) {
            throw new IllegalStateException("Errore durante la lettura del file JSON di importazione", e);
        }
    }

    private List<ConversationContext> detectAndParse(JsonNode root) {
        // Formato LocalMind: ha campo "source" con valore "LocalMind"
        if (root.has("source") && "LocalMind".equals(root.path("source").asText())) {
            return parseLocalMindFormat(root);
        }

        // Formato ChatGPT: array "conversations" con entries che hanno "mapping"
        if (root.isArray()) {
            // ChatGPT esporta come array di conversazioni
            for (JsonNode entry : root) {
                if (entry.has("mapping")) {
                    return parseChatGptFormat(root);
                }
            }
        }
        if (root.has("conversations")) {
            JsonNode conversations = root.get("conversations");
            if (conversations.isArray() && conversations.size() > 0) {
                JsonNode first = conversations.get(0);
                if (first.has("mapping")) {
                    return parseChatGptFormat(conversations);
                }
            }
        }

        // Formato Claude: ha campo "chat_messages"
        if (root.has("chat_messages")) {
            return parseClaudeFormat(root);
        }
        // Claude esporta anche come array di conversazioni
        if (root.isArray()) {
            for (JsonNode entry : root) {
                if (entry.has("chat_messages")) {
                    return parseClaudeArrayFormat(root);
                }
            }
        }

        throw new IllegalArgumentException("Unsupported import format");
    }

    // ===================== Formato LocalMind =====================

    private List<ConversationContext> parseLocalMindFormat(JsonNode root) {
        List<ConversationContext> result = new ArrayList<>();
        JsonNode conversations = root.path("conversations");

        if (!conversations.isArray()) {
            return result;
        }

        for (JsonNode convNode : conversations) {
            ConversationContext conversation = ConversationContext.builder()
                    .id(UUID.randomUUID().toString())
                    .title(getTextOrNull(convNode, "title"))
                    .systemPrompt(getTextOrNull(convNode, "systemPrompt"))
                    .messages(parseLocalMindMessages(convNode.path("messages")))
                    .tags(parseTagsSet(convNode.path("tags")))
                    .createdAt(parseInstantOrDefault(convNode, "createdAt"))
                    .updatedAt(parseInstantOrNull(convNode, "updatedAt"))
                    .build();
            result.add(conversation);
        }

        return result;
    }

    private List<ChatMessage> parseLocalMindMessages(JsonNode messagesNode) {
        List<ChatMessage> messages = new ArrayList<>();
        if (!messagesNode.isArray()) {
            return messages;
        }
        for (JsonNode msgNode : messagesNode) {
            ChatMessage.Role role = parseRole(msgNode.path("role").asText());
            if (role == null) {
                continue;
            }
            ChatMessage message = ChatMessage.builder()
                    .role(role)
                    .content(getTextOrNull(msgNode, "content"))
                    .metadata(parseMetadata(msgNode.path("metadata")))
                    .build();
            messages.add(message);
        }
        return messages;
    }

    // ===================== Formato ChatGPT =====================

    private List<ConversationContext> parseChatGptFormat(JsonNode conversationsArray) {
        List<ConversationContext> result = new ArrayList<>();

        for (JsonNode convNode : conversationsArray) {
            String title = getTextOrNull(convNode, "title");
            JsonNode mapping = convNode.path("mapping");

            if (!mapping.isObject()) {
                continue;
            }

            List<ChatMessage> messages = parseChatGptMapping(mapping);
            if (messages.isEmpty()) {
                continue;
            }

            Instant createdAt = convNode.has("create_time")
                    ? Instant.ofEpochSecond(convNode.path("create_time").asLong())
                    : Instant.now();

            Instant updatedAt = convNode.has("update_time")
                    ? Instant.ofEpochSecond(convNode.path("update_time").asLong())
                    : null;

            ConversationContext conversation = ConversationContext.builder()
                    .id(UUID.randomUUID().toString())
                    .title(title)
                    .messages(messages)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .tags(new HashSet<>())
                    .build();
            result.add(conversation);
        }

        return result;
    }

    private List<ChatMessage> parseChatGptMapping(JsonNode mapping) {
        // Il mapping di ChatGPT e' un oggetto dove le chiavi sono ID e i valori contengono "message"
        // Ordiniamo per create_time per mantenere l'ordine della conversazione
        List<Map.Entry<Long, ChatMessage>> sortedMessages = new ArrayList<>();

        Iterator<Map.Entry<String, JsonNode>> fields = mapping.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode node = entry.getValue();
            JsonNode messageNode = node.path("message");

            if (messageNode.isMissingNode() || messageNode.isNull()) {
                continue;
            }

            JsonNode authorNode = messageNode.path("author");
            String authorRole = authorNode.path("role").asText("");

            ChatMessage.Role role = mapChatGptRole(authorRole);
            if (role == null) {
                continue;
            }

            // Estrai il contenuto da content.parts[0]
            JsonNode contentNode = messageNode.path("content");
            JsonNode partsNode = contentNode.path("parts");
            String content = "";
            if (partsNode.isArray() && partsNode.size() > 0) {
                JsonNode firstPart = partsNode.get(0);
                if (firstPart.isTextual()) {
                    content = firstPart.asText();
                } else if (firstPart.isObject()) {
                    content = firstPart.toString();
                }
            }

            if (content.isEmpty()) {
                continue;
            }

            long createTime = messageNode.path("create_time").asLong(0);

            ChatMessage message = ChatMessage.builder()
                    .role(role)
                    .content(content)
                    .build();
            sortedMessages.add(Map.entry(createTime, message));
        }

        // Ordina per timestamp
        sortedMessages.sort(Comparator.comparingLong(Map.Entry::getKey));

        List<ChatMessage> result = new ArrayList<>();
        for (Map.Entry<Long, ChatMessage> entry : sortedMessages) {
            result.add(entry.getValue());
        }
        return result;
    }

    private ChatMessage.Role mapChatGptRole(String role) {
        return switch (role.toLowerCase()) {
            case "user" -> ChatMessage.Role.USER;
            case "assistant" -> ChatMessage.Role.ASSISTANT;
            case "system" -> ChatMessage.Role.SYSTEM;
            case "tool" -> ChatMessage.Role.TOOL;
            default -> null;
        };
    }

    // ===================== Formato Claude =====================

    private List<ConversationContext> parseClaudeFormat(JsonNode root) {
        List<ConversationContext> result = new ArrayList<>();

        String title = getTextOrNull(root, "name");
        if (title == null) {
            title = getTextOrNull(root, "title");
        }

        List<ChatMessage> messages = parseClaudeMessages(root.path("chat_messages"));

        ConversationContext conversation = ConversationContext.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .messages(messages)
                .createdAt(parseInstantOrDefault(root, "created_at"))
                .updatedAt(parseInstantOrNull(root, "updated_at"))
                .tags(new HashSet<>())
                .build();
        result.add(conversation);

        return result;
    }

    private List<ConversationContext> parseClaudeArrayFormat(JsonNode root) {
        List<ConversationContext> result = new ArrayList<>();
        for (JsonNode convNode : root) {
            if (convNode.has("chat_messages")) {
                result.addAll(parseClaudeFormat(convNode));
            }
        }
        return result;
    }

    private List<ChatMessage> parseClaudeMessages(JsonNode messagesNode) {
        List<ChatMessage> messages = new ArrayList<>();
        if (!messagesNode.isArray()) {
            return messages;
        }
        for (JsonNode msgNode : messagesNode) {
            String sender = msgNode.path("sender").asText("");
            ChatMessage.Role role = mapClaudeRole(sender);
            if (role == null) {
                continue;
            }

            String content = getTextOrNull(msgNode, "text");
            if (content == null || content.isEmpty()) {
                continue;
            }

            ChatMessage message = ChatMessage.builder()
                    .role(role)
                    .content(content)
                    .build();
            messages.add(message);
        }
        return messages;
    }

    private ChatMessage.Role mapClaudeRole(String sender) {
        return switch (sender.toLowerCase()) {
            case "human" -> ChatMessage.Role.USER;
            case "assistant" -> ChatMessage.Role.ASSISTANT;
            default -> null;
        };
    }

    // ===================== Utility =====================

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private ChatMessage.Role parseRole(String roleStr) {
        if (roleStr == null || roleStr.isEmpty()) {
            return null;
        }
        try {
            return ChatMessage.Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Instant parseInstantOrDefault(JsonNode node, String field) {
        String value = getTextOrNull(node, field);
        if (value == null) {
            return Instant.now();
        }
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            return Instant.now();
        }
    }

    private Instant parseInstantOrNull(JsonNode node, String field) {
        String value = getTextOrNull(node, field);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Set<String> parseTagsSet(JsonNode tagsNode) {
        Set<String> tags = new HashSet<>();
        if (tagsNode.isArray()) {
            for (JsonNode tag : tagsNode) {
                if (tag.isTextual()) {
                    tags.add(tag.asText());
                }
            }
        }
        return tags;
    }

    private Map<String, Object> parseMetadata(JsonNode metadataNode) {
        if (metadataNode.isMissingNode() || metadataNode.isNull()) {
            return null;
        }
        try {
            return objectMapper.convertValue(metadataNode, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
