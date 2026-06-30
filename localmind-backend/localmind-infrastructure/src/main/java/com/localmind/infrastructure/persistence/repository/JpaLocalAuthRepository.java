package com.localmind.infrastructure.persistence.repository;

import com.localmind.infrastructure.persistence.entity.LocalAuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaLocalAuthRepository extends JpaRepository<LocalAuthEntity, UUID> {

    Optional<LocalAuthEntity> findByEnabledTrue();
}
