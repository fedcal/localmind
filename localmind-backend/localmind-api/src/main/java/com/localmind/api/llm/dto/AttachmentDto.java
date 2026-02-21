package com.localmind.api.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {
    private String data;      // base64-encoded content
    private String mimeType;
    private String filename;
}
