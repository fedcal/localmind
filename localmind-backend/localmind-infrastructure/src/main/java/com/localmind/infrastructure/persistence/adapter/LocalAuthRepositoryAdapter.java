package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.auth.model.LocalAuth;
import com.localmind.domain.auth.port.out.LocalAuthRepository;
import com.localmind.infrastructure.persistence.entity.LocalAuthEntity;
import com.localmind.infrastructure.persistence.repository.JpaLocalAuthRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LocalAuthRepositoryAdapter implements LocalAuthRepository {

    private final JpaLocalAuthRepository jpaRepository;

    public LocalAuthRepositoryAdapter(JpaLocalAuthRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<LocalAuth> findActive() {
        return jpaRepository.findByEnabledTrue().map(this::toDomain);
    }

    @Override
    public LocalAuth save(LocalAuth auth) {
        LocalAuthEntity entity = toEntity(auth);
        LocalAuthEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private LocalAuthEntity toEntity(LocalAuth auth) {
        return LocalAuthEntity.builder()
                .id(auth.getId())
                .passwordHash(auth.getPasswordHash())
                .enabled(auth.isEnabled())
                .createdAt(auth.getCreatedAt())
                .updatedAt(auth.getUpdatedAt())
                .build();
    }

    private LocalAuth toDomain(LocalAuthEntity entity) {
        return new LocalAuth(
                entity.getId(),
                entity.getPasswordHash(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
