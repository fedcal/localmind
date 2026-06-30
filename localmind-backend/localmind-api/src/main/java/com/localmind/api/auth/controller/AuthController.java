package com.localmind.api.auth.controller;

import com.localmind.api.auth.dto.*;
import com.localmind.domain.auth.model.AuthToken;
import com.localmind.domain.auth.port.in.LocalAuthUseCase;
import com.localmind.domain.common.exception.AuthenticationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Autenticazione locale / Local authentication")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final LocalAuthUseCase localAuthUseCase;

    public AuthController(LocalAuthUseCase localAuthUseCase) {
        this.localAuthUseCase = localAuthUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Login con password / Login with password")
    @ApiResponse(responseCode = "200", description = "Autenticazione riuscita / Authentication successful")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        AuthToken token = localAuthUseCase.authenticate(request.getPassword());
        long expiresIn = Duration.between(Instant.now(), token.getExpiresAt()).getSeconds();
        return ResponseEntity.ok(new AuthResponseDto(token.getToken(), expiresIn));
    }

    @PostMapping("/setup")
    @Operation(summary = "Configura password iniziale / Setup initial password")
    @ApiResponse(responseCode = "200", description = "Password configurata / Password configured")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
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
    @Operation(summary = "Cambia password / Change password")
    @ApiResponse(responseCode = "200", description = "Password cambiata / Password changed")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
        localAuthUseCase.changePassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @Operation(summary = "Stato autenticazione / Authentication status")
    @ApiResponse(responseCode = "200", description = "Stato corrente / Current status")
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
