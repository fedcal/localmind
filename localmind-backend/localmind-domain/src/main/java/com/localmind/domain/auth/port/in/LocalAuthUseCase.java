package com.localmind.domain.auth.port.in;

import com.localmind.domain.auth.model.AuthToken;

public interface LocalAuthUseCase {

    boolean isAuthConfigured();

    AuthToken authenticate(String password);

    void setupPassword(String password);

    void changePassword(String currentPassword, String newPassword);

    boolean validateToken(String token);
}
