package com.localmind.api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.auth.dto.ChangePasswordRequestDto;
import com.localmind.api.auth.dto.LoginRequestDto;
import com.localmind.api.auth.dto.SetupPasswordRequestDto;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.domain.auth.model.AuthToken;
import com.localmind.domain.auth.port.in.LocalAuthUseCase;
import com.localmind.domain.common.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;
    private LocalAuthUseCase localAuthUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        localAuthUseCase = mock(LocalAuthUseCase.class);
        AuthController controller = new AuthController(localAuthUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    // --- POST /login ---

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() throws Exception {
        AuthToken token = new AuthToken("uuid.signature", Instant.now().plus(24, ChronoUnit.HOURS));
        when(localAuthUseCase.authenticate("correct-password")).thenReturn(token);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto("correct-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("uuid.signature"))
                .andExpect(jsonPath("$.expiresIn").isNumber());

        verify(localAuthUseCase).authenticate("correct-password");
    }

    @Test
    void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
        when(localAuthUseCase.authenticate("wrong-password"))
                .thenThrow(new AuthenticationException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto("wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void login_shouldReturn400_whenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // --- POST /setup ---

    @Test
    void setup_shouldReturnToken_whenSetupSucceeds() throws Exception {
        AuthToken token = new AuthToken("uuid.signature", Instant.now().plus(24, ChronoUnit.HOURS));
        doNothing().when(localAuthUseCase).setupPassword("my-password");
        when(localAuthUseCase.authenticate("my-password")).thenReturn(token);

        SetupPasswordRequestDto request = new SetupPasswordRequestDto("my-password", "my-password");

        mockMvc.perform(post("/api/v1/auth/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("uuid.signature"))
                .andExpect(jsonPath("$.expiresIn").isNumber());

        verify(localAuthUseCase).setupPassword("my-password");
        verify(localAuthUseCase).authenticate("my-password");
    }

    @Test
    void setup_shouldReturn401_whenAlreadyConfigured() throws Exception {
        doThrow(new AuthenticationException("Authentication already configured"))
                .when(localAuthUseCase).setupPassword("my-password");

        SetupPasswordRequestDto request = new SetupPasswordRequestDto("my-password", "my-password");

        mockMvc.perform(post("/api/v1/auth/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication already configured"));
    }

    @Test
    void setup_shouldReturn401_whenPasswordsDoNotMatch() throws Exception {
        SetupPasswordRequestDto request = new SetupPasswordRequestDto("password1", "password2");

        mockMvc.perform(post("/api/v1/auth/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Passwords do not match"));
    }

    // --- POST /change-password ---

    @Test
    void changePassword_shouldReturn200_whenSuccessful() throws Exception {
        doNothing().when(localAuthUseCase).changePassword("current", "new-pass");

        ChangePasswordRequestDto request = new ChangePasswordRequestDto("current", "new-pass");

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(localAuthUseCase).changePassword("current", "new-pass");
    }

    @Test
    void changePassword_shouldReturn401_whenCurrentPasswordIsWrong() throws Exception {
        doThrow(new AuthenticationException("Current password is incorrect"))
                .when(localAuthUseCase).changePassword("wrong", "new-pass");

        ChangePasswordRequestDto request = new ChangePasswordRequestDto("wrong", "new-pass");

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    // --- GET /status ---

    @Test
    void status_shouldReturnConfiguredTrue_whenAuthIsConfigured() throws Exception {
        when(localAuthUseCase.isAuthConfigured()).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(true))
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    void status_shouldReturnConfiguredFalse_whenAuthIsNotConfigured() throws Exception {
        when(localAuthUseCase.isAuthConfigured()).thenReturn(false);

        mockMvc.perform(get("/api/v1/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(false))
                .andExpect(jsonPath("$.authenticated").value(false));
    }
}
