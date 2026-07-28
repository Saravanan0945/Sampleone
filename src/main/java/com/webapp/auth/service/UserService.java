package com.webapp.auth.service;

import com.webapp.auth.dto.ChangePasswordRequest;
import com.webapp.auth.dto.UserProfileResponse;
import com.webapp.auth.exception.InvalidCredentialsException;
import com.webapp.auth.exception.UserNotFoundException;
import com.webapp.auth.model.User;
import com.webapp.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user profile operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get user profile by username
     */
    public UserProfileResponse getUserProfile(String username) {
        log.info("Fetching profile for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .lastLogin(user.getLastLoginTimestamp())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        log.info("Password change request for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            log.warn("Invalid current password provided for user: {}", username);
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", username);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserProfileResponse updateProfile(String username, String firstName, String lastName) {
        log.info("Profile update request for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName.trim());
        }
        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName.trim());
        }

        userRepository.save(user);
        log.info("Profile updated successfully for user: {}", username);

        return getUserProfile(username);
    }
}

