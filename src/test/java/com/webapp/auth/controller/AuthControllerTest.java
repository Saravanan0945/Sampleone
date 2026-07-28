package com.webapp.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webapp.auth.dto.*;
import com.webapp.auth.exception.AccountLockedException;
import com.webapp.auth.exception.InvalidCredentialsException;
import com.webapp.auth.exception.UserNotFoundException;
import com.webapp.auth.service.AuthenticationService;
import com.webapp.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private UserService userService;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123!");

        loginResponse = LoginResponse.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-456")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login with valid credentials should return 200 OK with LoginResponse")
    void testLogin_ValidCredentials_Returns200() throws Exception {
        // Arrange
        when(authenticationService.login(any(LoginRequest.class), anyString()))
                .thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-456"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(authenticationService).login(any(LoginRequest.class), anyString());
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid credentials should return 401 Unauthorized")
    void testLogin_InvalidCredentials_Returns401() throws Exception {
        // Arrange
        when(authenticationService.login(any(LoginRequest.class), anyString()))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("POST /api/auth/login with locked account should return 423 Locked")
    void testLogin_LockedAccount_Returns423() throws Exception {
        // Arrange
        when(authenticationService.login(any(LoginRequest.class), anyString()))
                .thenThrow(new AccountLockedException("Account is locked due to multiple failed login attempts"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Account is locked due to multiple failed login attempts"));
    }

    @Test
    @DisplayName("POST /api/auth/login with missing fields should return 400 Bad Request")
    void testLogin_MissingFields_Returns400() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername(""); // Empty username
        invalidRequest.setPassword(""); // Empty password

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login with non-existent user should return 404 Not Found")
    void testLogin_UserNotFound_Returns404() throws Exception {
        // Arrange
        when(authenticationService.login(any(LoginRequest.class), anyString()))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /api/auth/logout should return 200 OK")
    void testLogout_Returns200() throws Exception {
        // Arrange
        doNothing().when(authenticationService).logout("testuser");

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authenticationService).logout("testuser");
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password with valid email should return 200 OK")
    void testForgotPassword_ValidEmail_Returns200() throws Exception {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        doNothing().when(authenticationService).forgotPassword("test@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset instructions sent to your email"));

        verify(authenticationService).forgotPassword("test@example.com");
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password with invalid email should return 400 Bad Request")
    void testForgotPassword_InvalidEmail_Returns400() throws Exception {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("invalid-email"); // Invalid email format

        // Act & Assert
        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password with valid token should return 200 OK")
    void testResetPassword_ValidToken_Returns200() throws Exception {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-reset-token");
        request.setNewPassword("NewPassword123!");

        doNothing().when(authenticationService).resetPassword("valid-reset-token", "NewPassword123!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        verify(authenticationService).resetPassword("valid-reset-token", "NewPassword123!");
    }

    @Test
    @DisplayName("POST /api/auth/reset-password with short password should return 400 Bad Request")
    void testResetPassword_ShortPassword_Returns400() throws Exception {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-reset-token");
        request.setNewPassword("short"); // Too short

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /api/auth/profile should return 200 OK with user details when authenticated")
    void testGetProfile_Authenticated_Returns200() throws Exception {
        // Arrange
        UserProfileResponse profileResponse = UserProfileResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .lastLogin(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserProfile("testuser")).thenReturn(profileResponse);

        // Act & Assert
        mockMvc.perform(get("/api/auth/profile"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("Test"))
                .andExpect(jsonPath("$.data.lastName").value("User"));

        verify(userService).getUserProfile("testuser");
    }

    @Test
    @DisplayName("GET /api/auth/profile without authentication should return 401 Unauthorized")
    void testGetProfile_NotAuthenticated_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/profile"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserProfile(anyString());
    }

    @Test
    @DisplayName("POST /api/auth/refresh with valid refresh token should return 200 OK with new tokens")
    void testRefreshToken_ValidToken_Returns200() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        LoginResponse newTokenResponse = LoginResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("valid-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .username("testuser")
                .email("test@example.com")
                .build();

        when(authenticationService.refreshAccessToken("valid-refresh-token"))
                .thenReturn(newTokenResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("valid-refresh-token"));

        verify(authenticationService).refreshAccessToken("valid-refresh-token");
    }

    @Test
    @DisplayName("POST /api/auth/refresh with invalid token should return 400 Bad Request")
    void testRefreshToken_InvalidToken_Returns400() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(authenticationService.refreshAccessToken("invalid-token"))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    @DisplayName("GET /api/auth/health should return 200 OK")
    void testHealthCheck_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication service is running"));
    }
}

