package com.localmind.domain.auth.model;

import java.time.Instant;

public class AuthToken {

    private final String token;
    private final Instant expiresAt;

    public AuthToken(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
