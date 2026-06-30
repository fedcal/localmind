package com.localmind.domain.agent.model;

import lombok.Getter;

@Getter
public enum AgentType {
    TECH("Tech Agent", "Expert in code, debugging and technical analysis"),
    BUSINESS("Business Agent", "Expert in reports, summaries and business analysis"),
    LEGAL("Legal Agent", "Expert in clauses, legal references and compliance"),
    PERSONAL("Personal Agent", "Friendly assistant for simple explanations");

    private final String displayName;
    private final String description;

    AgentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
