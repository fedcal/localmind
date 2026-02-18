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

/**
 * JPA entity for the mcp_time_entries table.
 * Stores time tracking entries for the time-tracking tool group.
 */
@Entity
@Table(name = "mcp_time_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "task_id", length = 200)
    private String taskId;

    @Column(name = "user_id", length = 200)
    private String userId;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "entry_date", length = 20)
    private String entryDate;

    @Column(name = "timer_started_at")
    private Instant timerStartedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
