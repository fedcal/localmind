package com.localmind.domain.email.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDraft {
    private List<String> to;
    private String subject;
    private String body;
}
