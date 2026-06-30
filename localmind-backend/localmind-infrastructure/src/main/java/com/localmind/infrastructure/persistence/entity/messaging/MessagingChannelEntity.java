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
@Table(name = "messaging_channels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagingChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 30)
    private MessagingPlatform platform;

    @Column(name = "bot_token", length = 500)
    private String botToken;

    @Column(name = "webhook_secret", length = 500)
    private String webhookSecret;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "default_system_prompt", columnDefinition = "TEXT")
    private String defaultSystemPrompt;

    @Column(name = "default_model")
    private String defaultModel;

    @Column(name = "default_provider", length = 30)
    private String defaultProvider;

    @Column(name = "enable_rag", nullable = false)
    private boolean enableRag;

    @Column(name = "enable_tool_calling", nullable = false)
    private boolean enableToolCalling;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
