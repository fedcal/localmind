package com.localmind.domain.llm.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentPart {
    private ContentType type;
    private String content;
    private String mimeType;

    public enum ContentType {
        TEXT, IMAGE, AUDIO
    }
}
