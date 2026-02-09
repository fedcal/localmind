package com.localmind.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String path;

    @Builder.Default
    private boolean recursive = true;

    @Builder.Default
    private boolean watchEnabled = false;

    private Instant lastScanAt;
    private int documentCount;
}
