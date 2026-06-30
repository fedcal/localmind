package com.localmind.domain.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundMessage {
    private MessagingPlatform platform;
    private String platformUserId;
    private String platformChannelId;
    private String text;
    private String messageId;
}
