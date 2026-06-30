package com.localmind.domain.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelContext {
    private String id;
    private String channelId;
    private MessagingPlatform platform;
    private String platformUserId;
    private String platformChannelId;
    private String conversationId;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
}
