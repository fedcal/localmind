package com.localmind.domain.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Role role;
    private String content;
    private Map<String, Object> metadata;
    private List<ContentPart> parts;

    public enum Role {
        SYSTEM, USER, ASSISTANT, TOOL
    }
}
