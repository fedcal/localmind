package com.localmind.api.auth.controller;

import com.localmind.api.auth.dto.*;
import com.localmind.domain.auth.model.AuthToken;
import com.localmind.domain.auth.port.in.LocalAuthUseCase;
import com.localmind.domain.common.exception.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final LocalAuthUseCase localAuthUseCase;

    public AuthController(LocalAuthUseCase localAuthUseCase) {
        this.localAuthUseCase = localAuthUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        AuthToken token = localAuthUseCase.authenticate(request.getPassword());
        long expiresIn = Duration.between(Instant.now(), token.getExpiresAt()).getSeconds();
        return ResponseEntity.ok(new AuthResponseDto(token.getToken(), expiresIn));
    }

    @PostMapping("/setup")
    public ResponseEntity<AuthResponseDto> setup(@Valid @RequestBody SetupPasswordRequestDto request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthenticationException("Passwords do not match");
        }

        localAuthUseCase.setupPassword(request.getPassword());
        AuthToken token = localAuthUseCase.authenticate(request.getPassword());
        long expiresIn = Duration.between(Instant.now(), token.getExpiresAt()).getSeconds();
        return ResponseEntity.ok(new AuthResponseDto(token.getToken(), expiresIn));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
        localAuthUseCase.changePassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<AuthStatusResponseDto> status(HttpServletRequest request) {
        boolean configured = localAuthUseCase.isAuthConfigured();
        boolean authenticated = false;

        if (configured) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());
                authenticated = localAuthUseCase.validateToken(token);
            }
        }

        return ResponseEntity.ok(new AuthStatusResponseDto(configured, authenticated));
    }
}
