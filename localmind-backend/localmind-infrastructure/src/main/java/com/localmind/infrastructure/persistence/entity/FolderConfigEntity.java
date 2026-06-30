package com.localmind.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "folder_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String path;

    @Column(name = "`recursive`")
    @Builder.Default
    private boolean recursive = true;

    @Builder.Default
    private boolean watchEnabled = false;

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    private Instant lastScanAt;
    private int documentCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
