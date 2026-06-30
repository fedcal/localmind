package com.localmind.domain.messaging.model;

import lombok.Getter;

@Getter
public enum MessagingPlatform {
    SLACK("Slack"),
    DISCORD("Discord"),
    TELEGRAM("Telegram");

    private final String displayName;

    MessagingPlatform(String displayName) {
        this.displayName = displayName;
    }
}
