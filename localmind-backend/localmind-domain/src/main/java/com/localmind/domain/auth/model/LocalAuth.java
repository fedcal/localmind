package com.localmind.domain.auth.model;

import java.time.Instant;
import java.util.UUID;

public class LocalAuth {

    private UUID id;
    private String passwordHash;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public LocalAuth() {
    }

    public LocalAuth(UUID id, String passwordHash, boolean enabled, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
