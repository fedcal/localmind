package com.localmind.domain.auth.port.out;

import com.localmind.domain.auth.model.LocalAuth;

import java.util.Optional;

public interface LocalAuthRepository {

    Optional<LocalAuth> findActive();

    LocalAuth save(LocalAuth auth);
}
