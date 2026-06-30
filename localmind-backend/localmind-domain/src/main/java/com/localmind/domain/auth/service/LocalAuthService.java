package com.localmind.domain.auth.service;

import com.localmind.domain.auth.model.AuthToken;
import com.localmind.domain.auth.model.LocalAuth;
import com.localmind.domain.auth.port.in.LocalAuthUseCase;
import com.localmind.domain.auth.port.out.LocalAuthRepository;
import com.localmind.domain.auth.port.out.PasswordHasherPort;
import com.localmind.domain.common.exception.AuthenticationException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocalAuthService implements LocalAuthUseCase {

    private static final long TOKEN_EXPIRY_HOURS = 24;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final LocalAuthRepository localAuthRepository;
    private final PasswordHasherPort passwordHasherPort;
    private final byte[] secretKey;
    private final Map<String, Instant> activeTokens = new ConcurrentHashMap<>();

    public LocalAuthService(LocalAuthRepository localAuthRepository,
                            PasswordHasherPort passwordHasherPort) {
        this.localAuthRepository = localAuthRepository;
        this.passwordHasherPort = passwordHasherPort;
        this.secretKey = generateSecretKey();
    }

    // Visible for testing
    LocalAuthService(LocalAuthRepository localAuthRepository,
                     PasswordHasherPort passwordHasherPort,
                     byte[] secretKey) {
        this.localAuthRepository = localAuthRepository;
        this.passwordHasherPort = passwordHasherPort;
        this.secretKey = secretKey;
    }

    @Override
    public boolean isAuthConfigured() {
        return localAuthRepository.findActive().isPresent();
    }

    @Override
    public AuthToken authenticate(String password) {
        LocalAuth auth = localAuthRepository.findActive()
                .orElseThrow(() -> new AuthenticationException("Authentication not configured"));

        if (!passwordHasherPort.matches(password, auth.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        return generateToken();
    }

    @Override
    public void setupPassword(String password) {
        Optional<LocalAuth> existing = localAuthRepository.findActive();
        if (existing.isPresent()) {
            throw new AuthenticationException("Authentication already configured");
        }

        String hash = passwordHasherPort.hash(password);
        Instant now = Instant.now();

        LocalAuth auth = new LocalAuth();
        auth.setId(UUID.randomUUID());
        auth.setPasswordHash(hash);
        auth.setEnabled(true);
        auth.setCreatedAt(now);
        auth.setUpdatedAt(now);

        localAuthRepository.save(auth);
    }

    @Override
    public void changePassword(String currentPassword, String newPassword) {
        LocalAuth auth = localAuthRepository.findActive()
                .orElseThrow(() -> new AuthenticationException("Authentication not configured"));

        if (!passwordHasherPort.matches(currentPassword, auth.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        auth.setPasswordHash(passwordHasherPort.hash(newPassword));
        auth.setUpdatedAt(Instant.now());
        localAuthRepository.save(auth);
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        // Check format: must contain a dot separator
        int dotIndex = token.indexOf('.');
        if (dotIndex <= 0 || dotIndex >= token.length() - 1) {
            return false;
        }

        // Verify HMAC signature
        String uuidPart = token.substring(0, dotIndex);
        String signaturePart = token.substring(dotIndex + 1);

        String expectedSignature = computeHmac(uuidPart);
        if (!expectedSignature.equals(signaturePart)) {
            return false;
        }

        // Check expiry from in-memory map
        Instant expiresAt = activeTokens.get(token);
        if (expiresAt == null) {
            return false;
        }

        if (Instant.now().isAfter(expiresAt)) {
            activeTokens.remove(token);
            return false;
        }

        return true;
    }

    private AuthToken generateToken() {
        String uuid = UUID.randomUUID().toString();
        String signature = computeHmac(uuid);
        String token = uuid + "." + signature;
        Instant expiresAt = Instant.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS);

        activeTokens.put(token, expiresAt);

        return new AuthToken(token, expiresAt);
    }

    private String computeHmac(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }

    private static byte[] generateSecretKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return key;
    }

    // Visible for testing
    Map<String, Instant> getActiveTokens() {
        return activeTokens;
    }
}
