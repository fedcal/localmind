package com.localmind.infrastructure.mcp.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface McpServerJpaRepository extends JpaRepository<McpServerEntity, String> {
    List<McpServerEntity> findByStatus(String status);
}
