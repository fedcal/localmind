package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.auth.model.LocalAuth;
import com.localmind.infrastructure.persistence.entity.LocalAuthEntity;
import com.localmind.infrastructure.persistence.repository.JpaLocalAuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalAuthRepositoryAdapterTest {

    @Mock
    private JpaLocalAuthRepository jpaRepository;

    @InjectMocks
    private LocalAuthRepositoryAdapter adapter;

    private static final UUID AUTH_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final Instant NOW = Instant.parse("2026-02-14T10:00:00Z");

    @Test
    void findActive_shouldReturnDomainModel_whenActiveExists() {
        LocalAuthEntity entity = LocalAuthEntity.builder()
                .id(AUTH_UUID)
                .passwordHash("$2a$10$hash")
                .enabled(true)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(jpaRepository.findByEnabledTrue()).thenReturn(Optional.of(entity));

        Optional<LocalAuth> result = adapter.findActive();

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(AUTH_UUID);
        assertThat(result.get().getPasswordHash()).isEqualTo("$2a$10$hash");
        assertThat(result.get().isEnabled()).isTrue();
        assertThat(result.get().getCreatedAt()).isEqualTo(NOW);
        assertThat(result.get().getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void findActive_shouldReturnEmpty_whenNoActiveExists() {
        when(jpaRepository.findByEnabledTrue()).thenReturn(Optional.empty());

        Optional<LocalAuth> result = adapter.findActive();

        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldConvertToEntityAndPersist() {
        LocalAuth auth = new LocalAuth(AUTH_UUID, "$2a$10$hash", true, NOW, NOW);

        LocalAuthEntity savedEntity = LocalAuthEntity.builder()
                .id(AUTH_UUID)
                .passwordHash("$2a$10$hash")
                .enabled(true)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(jpaRepository.save(any(LocalAuthEntity.class))).thenReturn(savedEntity);

        LocalAuth result = adapter.save(auth);

        ArgumentCaptor<LocalAuthEntity> captor = ArgumentCaptor.forClass(LocalAuthEntity.class);
        verify(jpaRepository).save(captor.capture());

        LocalAuthEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getId()).isEqualTo(AUTH_UUID);
        assertThat(capturedEntity.getPasswordHash()).isEqualTo("$2a$10$hash");
        assertThat(capturedEntity.isEnabled()).isTrue();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(AUTH_UUID);
        assertThat(result.getPasswordHash()).isEqualTo("$2a$10$hash");
    }
}
