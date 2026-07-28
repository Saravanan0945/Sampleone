package com.webapp.auth.service;

import com.webapp.auth.dto.ApiResponse;
import com.webapp.auth.dto.RegisterRequest;
import com.webapp.auth.exception.UserAlreadyExistsException;
import com.webapp.auth.model.User;
import com.webapp.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;

/**
 * Service for handling user registration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user account
     */
    @Transactional
    public ApiResponse<String> registerUser(RegisterRequest request) {
        log.info("Registration request for username: {}", sanitizeInput(request.getUsername()));

        String username = sanitizeInput(request.getUsername());
        String email = sanitizeInput(request.getEmail());

        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed: Username already exists - {}", username);
            throw new UserAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: Email already exists - {}", email);
            throw new UserAlreadyExistsException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(hashedPassword)
                .firstName(sanitizeInput(request.getFirstName()))
                .lastName(sanitizeInput(request.getLastName()))
                .isActive(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .build();

        userRepository.save(user);

        log.info("User registered successfully: {}", username);

        return ApiResponse.<String>builder()
                .success(true)
                .message("User registered successfully")
                .data(username)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Check if username is available
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(sanitizeInput(username));
    }

    /**
     * Check if email is available
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(sanitizeInput(email));
    }

    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(input.trim());
    }
}

