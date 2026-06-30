package com.localmind.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptPasswordHasherAdapterTest {

    private BcryptPasswordHasherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BcryptPasswordHasherAdapter();
    }

    @Test
    void hash_shouldReturnNonNullHash() {
        String hash = adapter.hash("my-password");

        assertThat(hash).isNotNull();
        assertThat(hash).isNotBlank();
        assertThat(hash).startsWith("$2a$");
    }

    @Test
    void matches_shouldReturnTrue_forCorrectPassword() {
        String hash = adapter.hash("my-password");

        assertThat(adapter.matches("my-password", hash)).isTrue();
    }

    @Test
    void matches_shouldReturnFalse_forWrongPassword() {
        String hash = adapter.hash("my-password");

        assertThat(adapter.matches("wrong-password", hash)).isFalse();
    }
}
