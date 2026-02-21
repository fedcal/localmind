package com.localmind.infrastructure.mcp.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "mcp_servers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpServerEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(length = 500)
    private String command;

    @Column(columnDefinition = "TEXT")
    private String args;

    @Column(length = 500)
    private String url;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Column(name = "auto_reconnect")
    private Boolean autoReconnect;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_connected_at")
    private LocalDateTime lastConnectedAt;
}
