package com.localmind.infrastructure.persistence.entity.mcp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mcp_standups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandupNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "yesterday", nullable = false, length = 2000)
    private String yesterday;

    @Column(name = "today", nullable = false, length = 2000)
    private String today;

    @Column(name = "blockers", length = 2000)
    private String blockers;

    @Column(name = "standup_date", nullable = false, length = 10)
    private String standupDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
