package com.localmind.domain.auth.service;

import com.localmind.domain.auth.model.AuthToken;
import com.localmind.domain.auth.model.LocalAuth;
import com.localmind.domain.auth.port.out.LocalAuthRepository;
import com.localmind.domain.auth.port.out.PasswordHasherPort;
import com.localmind.domain.common.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LocalAuthServiceTest {

    private LocalAuthRepository localAuthRepository;
    private PasswordHasherPort passwordHasherPort;
    private LocalAuthService service;

    private static final byte[] TEST_SECRET_KEY = "test-secret-key-32-bytes-long!!!".getBytes();

    @BeforeEach
    void setUp() {
        localAuthRepository = mock(LocalAuthRepository.class);
        passwordHasherPort = mock(PasswordHasherPort.class);
        service = new LocalAuthService(localAuthRepository, passwordHasherPort, TEST_SECRET_KEY);
    }

    // --- isAuthConfigured ---

    @Test
    void isAuthConfigured_shouldReturnTrue_whenActiveAuthExists() {
        when(localAuthRepository.findActive()).thenReturn(Optional.of(createLocalAuth()));

        assertThat(service.isAuthConfigured()).isTrue();
        verify(localAuthRepository).findActive();
    }

    @Test
    void isAuthConfigured_shouldReturnFalse_whenNoActiveAuth() {
        when(localAuthRepository.findActive()).thenReturn(Optional.empty());

        assertThat(service.isAuthConfigured()).isFalse();
        verify(localAuthRepository).findActive();
    }

    // --- authenticate ---

    @Test
    void authenticate_shouldReturnToken_whenPasswordIsCorrect() {
        LocalAuth auth = createLocalAuth();
        when(localAuthRepository.findActive()).thenReturn(Optional.of(auth));
        when(passwordHasherPort.matches("correct-password", auth.getPasswordHash())).thenReturn(true);

        AuthToken token = service.authenticate("correct-password");

        assertThat(token).isNotNull();
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getToken()).contains(".");
        assertThat(token.getExpiresAt()).isAfter(Instant.now());
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    void authenticate_shouldThrow_whenPasswordIsIncorrect() {
        LocalAuth auth = createLocalAuth();
        when(localAuthRepository.findActive()).thenReturn(Optional.of(auth));
        when(passwordHasherPort.matches("wrong-password", auth.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate("wrong-password"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void authenticate_shouldThrow_whenAuthNotConfigured() {
        when(localAuthRepository.findActive()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("any-password"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Authentication not configured");
    }

    // --- setupPassword ---

    @Test
    void setupPassword_shouldSaveNewAuth_whenNotConfigured() {
        when(localAuthRepository.findActive()).thenReturn(Optional.empty());
        when(passwordHasherPort.hash("new-password")).thenReturn("$2a$hashed");
        when(localAuthRepository.save(any(LocalAuth.class))).thenAnswer(inv -> inv.getArgument(0));

        service.setupPassword("new-password");

        verify(passwordHasherPort).hash("new-password");
        verify(localAuthRepository).save(any(LocalAuth.class));
    }

    @Test
    void setupPassword_shouldThrow_whenAlreadyConfigured() {
        when(localAuthRepository.findActive()).thenReturn(Optional.of(createLocalAuth()));

        assertThatThrownBy(() -> service.setupPassword("new-password"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Authentication already configured");

        verify(localAuthRepository, never()).save(any());
    }

    // --- changePassword ---

    @Test
    void changePassword_shouldUpdatePassword_whenCurrentPasswordIsCorrect() {
        LocalAuth auth = createLocalAuth();
        when(localAuthRepository.findActive()).thenReturn(Optional.of(auth));
        when(passwordHasherPort.matches("current-password", auth.getPasswordHash())).thenReturn(true);
        when(passwordHasherPort.hash("new-password")).thenReturn("$2a$new-hash");
        when(localAuthRepository.save(any(LocalAuth.class))).thenAnswer(inv -> inv.getArgument(0));

        service.changePassword("current-password", "new-password");

        verify(passwordHasherPort).hash("new-password");
        verify(localAuthRepository).save(any(LocalAuth.class));
    }

    @Test
    void changePassword_shouldThrow_whenCurrentPasswordIsWrong() {
        LocalAuth auth = createLocalAuth();
        when(localAuthRepository.findActive()).thenReturn(Optional.of(auth));
        when(passwordHasherPort.matches("wrong-password", auth.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword("wrong-password", "new-password"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Current password is incorrect");

        verify(localAuthRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrow_whenAuthNotConfigured() {
        when(localAuthRepository.findActive()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changePassword("current", "new"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Authentication not configured");
    }

    // --- validateToken ---

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {
        LocalAuth auth = createLocalAuth();
        when(localAuthRepository.findActive()).thenReturn(Optional.of(auth));
        when(passwordHasherPort.matches("password", auth.getPasswordHash())).thenReturn(true);

        AuthToken authToken = service.authenticate("password");

        assertThat(service.validateToken(authToken.getToken())).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_forExpiredToken() {
        LocalAuth auth = createLocalAuth();
        when(localAuthRepository.findActive()).thenReturn(Optional.of(auth));
        when(passwordHasherPort.matches("password", auth.getPasswordHash())).thenReturn(true);

        AuthToken authToken = service.authenticate("password");

        // Manually expire the token by replacing its expiry in the active tokens map
        service.getActiveTokens().put(authToken.getToken(), Instant.now().minus(1, ChronoUnit.HOURS));

        assertThat(service.validateToken(authToken.getToken())).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_forNullToken() {
        assertThat(service.validateToken(null)).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_forBlankToken() {
        assertThat(service.validateToken("  ")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_forInvalidFormat() {
        assertThat(service.validateToken("no-dot-separator")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_forTamperedSignature() {
        LocalAuth auth = createLocalAuth();
        when(localAuthRepository.findActive()).thenReturn(Optional.of(auth));
        when(passwordHasherPort.matches("password", auth.getPasswordHash())).thenReturn(true);

        AuthToken authToken = service.authenticate("password");
        String tamperedToken = authToken.getToken().substring(0, authToken.getToken().indexOf('.')) + ".tampered-signature";

        assertThat(service.validateToken(tamperedToken)).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_forUnknownToken() {
        // A well-formed token that was never issued
        String unknownToken = UUID.randomUUID() + ".some-signature";
        assertThat(service.validateToken(unknownToken)).isFalse();
    }

    // --- Helper ---

    private LocalAuth createLocalAuth() {
        Instant now = Instant.now();
        return new LocalAuth(
                UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
                "$2a$10$existingHash",
                true,
                now,
                now
        );
    }
}
