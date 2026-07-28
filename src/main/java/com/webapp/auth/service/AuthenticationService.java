package com.webapp.auth.service;

import com.webapp.auth.dto.*;
import com.webapp.auth.exception.*;
import com.webapp.auth.model.PasswordResetToken;
import com.webapp.auth.model.RefreshToken;
import com.webapp.auth.model.User;
import com.webapp.auth.repository.PasswordResetTokenRepository;
import com.webapp.auth.repository.RefreshTokenRepository;
import com.webapp.auth.repository.UserRepository;
import com.webapp.auth.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for handling authentication operations including login, logout,
 * password reset, and token refresh functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RateLimitingService rateLimitingService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

    @Value("${security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Value("${security.password-reset.expiration-hours:1}")
    private int passwordResetExpirationHours;


    /**
     * Authenticate user and generate JWT tokens
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        log.info("Login attempt for user: {}", sanitizeInput(request.getUsernameOrEmail()));

        if (rateLimitingService.isRateLimited(ipAddress)) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
            throw new InvalidCredentialsException("Too many login attempts. Please try again later.");
        }

        String usernameOrEmail = sanitizeInput(request.getUsernameOrEmail());
        String password = request.getPassword();

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    rateLimitingService.recordLoginAttempt(ipAddress);
                    return new UserNotFoundException("Invalid username or password");
                });

        if (!user.getIsActive()) {
            log.warn("Inactive account login attempt: {}", user.getUsername());
            throw new AccountNotActiveException("Account is not active. Please contact support.");
        }

        if (isAccountLocked(user)) {
            log.warn("Locked account login attempt: {}", user.getUsername());
            throw new AccountLockedException(
                String.format("Account is locked until %s due to multiple failed login attempts", 
                    user.getAccountLockedUntil())
            );
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), password)
            );

            handleSuccessfulLogin(user, ipAddress);

            String accessToken = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());
            String refreshToken = createRefreshToken(user);

            log.info("Successful login for user: {}", user.getUsername());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration / 1000)
                    .user(LoginResponse.UserInfo.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .lastLogin(user.getLastLoginTimestamp())
                            .build())
                    .build();

        } catch (BadCredentialsException e) {
            handleFailedLogin(user, ipAddress);
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }


    /**
     * Logout user and invalidate refresh tokens
     */
    @Transactional
    public void logout(String username) {
        log.info("Logout request for user: {}", sanitizeInput(username));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        refreshTokenRepository.deleteByUser(user);
        log.info("Successfully logged out user: {}", username);
    }

    /**
     * Initiate password reset process
     */
    @Transactional
    public ApiResponse<String> forgotPassword(String email) {
        log.info("Password reset request for email: {}", sanitizeInput(email));
        
        String sanitizedEmail = sanitizeInput(email);
        User user = userRepository.findByEmail(sanitizedEmail)
                .orElseThrow(() -> new UserNotFoundException("No account found with this email address"));

        passwordResetTokenRepository.deleteByUser(user);

        String resetToken = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(resetToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(passwordResetExpirationHours))
                .isUsed(false)
                .build();

        passwordResetTokenRepository.save(passwordResetToken);

        String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;
        log.info("Password reset link generated for user {}: {}", user.getUsername(), resetLink);
        log.info("EMAIL SIMULATION: Sending password reset email to {}", user.getEmail());
        log.info("Reset link: {}", resetLink);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Password reset instructions have been sent to your email")
                .data(resetLink)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Reset user password using reset token
     */
    @Transactional
    public ApiResponse<String> resetPassword(String token, String newPassword) {
        log.info("Password reset attempt with token");
        
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (!resetToken.isValid()) {
            log.warn("Invalid or expired reset token used");
            throw new TokenExpiredException("Reset token has expired or already been used");
        }

        User user = resetToken.getUser();

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hashedPassword);
        user.setFailedLoginAttempts(0);
        user.setIsLocked(false);
        user.setAccountLockedUntil(null);
        userRepository.save(user);

        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.deleteByUser(user);

        log.info("Password successfully reset for user: {}", user.getUsername());

        return ApiResponse.<String>builder()
                .success(true)
                .message("Password has been reset successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }


    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public LoginResponse refreshAccessToken(String refreshTokenString) {
        log.info("Token refresh request");
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            log.warn("Expired refresh token used");
            throw new TokenExpiredException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        
        if (!user.getIsActive()) {
            throw new AccountNotActiveException("Account is not active");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("USER")
                .accountExpired(false)
                .accountLocked(user.getIsLocked())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();

        String newAccessToken = jwtUtil.generateToken(userDetails);

        log.info("Access token refreshed for user: {}", user.getUsername());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenString)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .lastLogin(user.getLastLoginTimestamp())
                        .build())
                .build();
    }

    private boolean isAccountLocked(User user) {
        if (!user.getIsLocked()) {
            return false;
        }

        if (user.getAccountLockedUntil() != null && 
            LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
            user.setIsLocked(false);
            user.setAccountLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            return false;
        }

        return true;
    }

    private void handleSuccessfulLogin(User user, String ipAddress) {
        user.setFailedLoginAttempts(0);
        user.setIsLocked(false);
        user.setAccountLockedUntil(null);
        user.setLastLoginTimestamp(LocalDateTime.now());
        userRepository.save(user);

        rateLimitingService.resetLoginAttempts(ipAddress);
    }

    private void handleFailedLogin(User user, String ipAddress) {
        rateLimitingService.recordLoginAttempt(ipAddress);
        
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            user.setIsLocked(true);
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            log.warn("Account locked for user {} after {} failed attempts", 
                     user.getUsername(), attempts);
        }

        userRepository.save(user);
    }

    private String createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        String tokenString = jwtUtil.generateRefreshToken(user.getUsername());
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenString)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenString;
    }

    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(input.trim());
    }
}

