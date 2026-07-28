package com.webapp.auth.service;

import com.webapp.auth.dto.LoginRequest;
import com.webapp.auth.dto.LoginResponse;
import com.webapp.auth.exception.AccountLockedException;
import com.webapp.auth.exception.InvalidCredentialsException;
import com.webapp.auth.exception.UserNotFoundException;
import com.webapp.auth.model.RefreshToken;
import com.webapp.auth.model.User;
import com.webapp.auth.repository.PasswordResetTokenRepository;
import com.webapp.auth.repository.RefreshTokenRepository;
import com.webapp.auth.repository.UserRepository;
import com.webapp.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RateLimitingService rateLimitingService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$12$hashedPassword")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123!");
    }

    @Test
    @DisplayName("Login with valid credentials should return LoginResponse with tokens")
    void testLogin_ValidCredentials_ReturnsLoginResponse() {
        // Arrange
        String ipAddress = "192.168.1.1";
        when(rateLimitingService.isRateLimited(ipAddress)).thenReturn(false);
        when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", testUser.getPasswordHash()))
                .thenReturn(true);
        when(jwtUtil.generateToken(testUser.getUsername()))
                .thenReturn("access-token-123");
        when(jwtUtil.generateRefreshToken(testUser.getUsername()))
                .thenReturn("refresh-token-456");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        LoginResponse response = authenticationService.login(loginRequest, ipAddress);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");

        // Verify interactions
        verify(userRepository).save(argThat(user -> 
            user.getFailedLoginAttempts() == 0 && 
            user.getLastLoginTimestamp() != null
        ));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(rateLimitingService).recordLoginAttempt(ipAddress);
    }

    @Test
    @DisplayName("Login with invalid username should throw UserNotFoundException")
    void testLogin_InvalidUsername_ThrowsUserNotFoundException() {
        // Arrange
        String ipAddress = "192.168.1.1";
        when(rateLimitingService.isRateLimited(ipAddress)).thenReturn(false);
        when(userRepository.findByUsernameOrEmail("invaliduser", "invaliduser"))
                .thenReturn(Optional.empty());
        loginRequest.setUsername("invaliduser");

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginRequest, ipAddress))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Login with invalid password should throw InvalidCredentialsException and increment failed attempts")
    void testLogin_InvalidPassword_ThrowsInvalidCredentialsException() {
        // Arrange
        String ipAddress = "192.168.1.1";
        when(rateLimitingService.isRateLimited(ipAddress)).thenReturn(false);
        when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", testUser.getPasswordHash()))
                .thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        loginRequest.setPassword("WrongPassword");

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginRequest, ipAddress))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        // Verify failed attempts incremented
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getFailedLoginAttempts()).isEqualTo(1);
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Login with 5 failed attempts should lock account and throw AccountLockedException")
    void testLogin_FiveFailedAttempts_LocksAccount() {
        // Arrange
        String ipAddress = "192.168.1.1";
        testUser.setFailedLoginAttempts(4); // 4 previous failures
        when(rateLimitingService.isRateLimited(ipAddress)).thenReturn(false);
        when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", testUser.getPasswordHash()))
                .thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        loginRequest.setPassword("WrongPassword");

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginRequest, ipAddress))
                .isInstanceOf(InvalidCredentialsException.class);

        // Verify account locked
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(savedUser.isLocked()).isTrue();
        assertThat(savedUser.getAccountLockedUntil()).isNotNull();
    }

    @Test
    @DisplayName("Login with locked account should throw AccountLockedException")
    void testLogin_LockedAccount_ThrowsAccountLockedException() {
        // Arrange
        String ipAddress = "192.168.1.1";
        testUser.setLocked(true);
        testUser.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
        when(rateLimitingService.isRateLimited(ipAddress)).thenReturn(false);
        when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginRequest, ipAddress))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Account is locked");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Login with inactive account should throw exception")
    void testLogin_InactiveAccount_ThrowsException() {
        // Arrange
        String ipAddress = "192.168.1.1";
        testUser.setActive(false);
        when(rateLimitingService.isRateLimited(ipAddress)).thenReturn(false);
        when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginRequest, ipAddress))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Account is not active");
    }

    @Test
    @DisplayName("Logout should delete all refresh tokens for user")
    void testLogout_DeletesRefreshTokens() {
        // Arrange
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));

        // Act
        authenticationService.logout("testuser");

        // Assert
        verify(refreshTokenRepository).deleteByUser(testUser);
    }

    @Test
    @DisplayName("Logout with invalid username should throw UserNotFoundException")
    void testLogout_InvalidUsername_ThrowsUserNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("invaliduser"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.logout("invaliduser"))
                .isInstanceOf(UserNotFoundException.class);

        verify(refreshTokenRepository, never()).deleteByUser(any());
    }

    @Test
    @DisplayName("Forgot password should generate reset token for valid email")
    void testForgotPassword_ValidEmail_GeneratesResetToken() {
        // Arrange
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        authenticationService.forgotPassword("test@example.com");

        // Assert
        verify(passwordResetTokenRepository).deleteByUser(testUser);
        verify(passwordResetTokenRepository).save(argThat(token ->
            token.getUser().equals(testUser) &&
            token.getToken() != null &&
            token.getExpiryDate() != null
        ));
    }

    @Test
    @DisplayName("Forgot password with invalid email should throw UserNotFoundException")
    void testForgotPassword_InvalidEmail_ThrowsUserNotFoundException() {
        // Arrange
        when(userRepository.findByEmail("invalid@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.forgotPassword("invalid@example.com"))
                .isInstanceOf(UserNotFoundException.class);

        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reset password should update password and unlock account")
    void testResetPassword_ValidToken_UpdatesPasswordAndUnlocksAccount() {
        // Arrange
        testUser.setLocked(true);
        testUser.setFailedLoginAttempts(5);
        
        com.webapp.auth.model.PasswordResetToken resetToken = 
            com.webapp.auth.model.PasswordResetToken.builder()
                .token("valid-reset-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();

        when(passwordResetTokenRepository.findByToken("valid-reset-token"))
                .thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("NewPassword123!"))
                .thenReturn("$2a$12$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authenticationService.resetPassword("valid-reset-token", "NewPassword123!");

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        assertThat(savedUser.getPasswordHash()).isEqualTo("$2a$12$newHashedPassword");
        assertThat(savedUser.isLocked()).isFalse();
        assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(savedUser.getAccountLockedUntil()).isNull();
        
        verify(passwordResetTokenRepository).save(argThat(token -> token.isUsed()));
        verify(refreshTokenRepository).deleteByUser(testUser);
    }

    @Test
    @DisplayName("Reset password with expired token should throw exception")
    void testResetPassword_ExpiredToken_ThrowsException() {
        // Arrange
        com.webapp.auth.model.PasswordResetToken resetToken = 
            com.webapp.auth.model.PasswordResetToken.builder()
                .token("expired-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .isUsed(false)
                .build();

        when(passwordResetTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(resetToken));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.resetPassword("expired-token", "NewPassword123!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired or invalid");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refresh access token should generate new token for valid refresh token")
    void testRefreshAccessToken_ValidToken_GeneratesNewAccessToken() {
        // Arrange
        RefreshToken refreshToken = RefreshToken.builder()
                .token("valid-refresh-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken("valid-refresh-token"))
                .thenReturn(Optional.of(refreshToken));
        when(jwtUtil.generateToken(testUser.getUsername()))
                .thenReturn("new-access-token");

        // Act
        LoginResponse response = authenticationService.refreshAccessToken("valid-refresh-token");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Refresh access token with expired token should throw exception")
    void testRefreshAccessToken_ExpiredToken_ThrowsException() {
        // Arrange
        RefreshToken refreshToken = RefreshToken.builder()
                .token("expired-refresh-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();

        when(refreshTokenRepository.findByToken("expired-refresh-token"))
                .thenReturn(Optional.of(refreshToken));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.refreshAccessToken("expired-refresh-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired");

        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Refresh access token with invalid token should throw exception")
    void testRefreshAccessToken_InvalidToken_ThrowsException() {
        // Arrange
        when(refreshTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.refreshAccessToken("invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(jwtUtil, never()).generateToken(anyString());
    }
}

