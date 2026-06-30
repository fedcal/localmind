package com.localmind.domain.document.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadata {
    private String author;
    private Instant createdDate;
    private int pageCount;
    private String language;
}
