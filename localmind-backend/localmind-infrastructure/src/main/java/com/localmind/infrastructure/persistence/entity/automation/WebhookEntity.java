package com.localmind.infrastructure.persistence.entity.automation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "webhooks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "active", nullable = false)
    private boolean active;
}
