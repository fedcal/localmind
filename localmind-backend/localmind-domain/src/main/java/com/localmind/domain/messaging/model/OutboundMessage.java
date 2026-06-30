package com.localmind.domain.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundMessage {
    private MessagingPlatform platform;
    private String channelId;
    private String text;
    private String replyToMessageId;
    private String botToken;
}
