package com.localmind.infrastructure.persistence.entity.messaging;

import com.localmind.domain.messaging.model.MessagingPlatform;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "channel_contexts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelContextEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "channel_id", nullable = false, columnDefinition = "char(36)")
    private String channelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 30)
    private MessagingPlatform platform;

    @Column(name = "platform_user_id", nullable = false)
    private String platformUserId;

    @Column(name = "platform_channel_id", nullable = false)
    private String platformChannelId;

    @Column(name = "conversation_id", columnDefinition = "char(36)")
    private String conversationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_activity", nullable = false)
    private LocalDateTime lastActivity;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (lastActivity == null) lastActivity = now;
    }

    @PreUpdate
    protected void onUpdate() {
        lastActivity = LocalDateTime.now();
    }
}
