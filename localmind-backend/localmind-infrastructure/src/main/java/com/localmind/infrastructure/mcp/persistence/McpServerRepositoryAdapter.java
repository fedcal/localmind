package com.localmind.infrastructure.mcp.persistence;

import com.localmind.domain.mcp.model.*;
import com.localmind.domain.mcp.port.out.McpServerRegistrationRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class McpServerRepositoryAdapter implements McpServerRegistrationRepository {

    private final McpServerJpaRepository jpaRepository;

    public McpServerRepositoryAdapter(McpServerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public McpServerRegistration save(McpServerRegistration server) {
        McpServerEntity entity = toEntity(server);
        McpServerEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void delete(String serverId) {
        jpaRepository.deleteById(serverId);
    }

    @Override
    public Optional<McpServerRegistration> findById(String serverId) {
        return jpaRepository.findById(serverId).map(this::toDomain);
    }

    @Override
    public List<McpServerRegistration> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private McpServerEntity toEntity(McpServerRegistration domain) {
        McpServerConfig config = domain.getConfig();
        return McpServerEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .type(domain.getType().name())
                .status(domain.getStatus().name())
                .command(config != null ? config.getCommand() : null)
                .args(config != null && config.getArgs() != null ? String.join(",", config.getArgs()) : null)
                .url(config != null ? config.getUrl() : null)
                .timeoutSeconds(config != null ? config.getTimeoutSeconds() : 30)
                .autoReconnect(config != null ? config.isAutoReconnect() : true)
                .createdAt(domain.getCreatedAt())
                .lastConnectedAt(domain.getLastConnectedAt())
                .build();
    }

    private McpServerRegistration toDomain(McpServerEntity entity) {
        McpServerConfig config = McpServerConfig.builder()
                .command(entity.getCommand())
                .args(entity.getArgs() != null ? Arrays.asList(entity.getArgs().split(",")) : null)
                .url(entity.getUrl())
                .timeoutSeconds(entity.getTimeoutSeconds() != null ? entity.getTimeoutSeconds() : 30)
                .autoReconnect(entity.getAutoReconnect() != null ? entity.getAutoReconnect() : true)
                .build();

        return McpServerRegistration.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(McpServerType.valueOf(entity.getType()))
                .status(McpServerStatus.valueOf(entity.getStatus()))
                .config(config)
                .createdAt(entity.getCreatedAt())
                .lastConnectedAt(entity.getLastConnectedAt())
                .build();
    }
}
