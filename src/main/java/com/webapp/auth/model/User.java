package com.webapp.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * User entity representing authenticated users in the system.
 * Contains user credentials, profile information, and security tracking fields.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_username", columnList = "username"),
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_is_active", columnList = "isActive"),
    @Index(name = "idx_users_is_locked", columnList = "isLocked")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login_timestamp")
    private LocalDateTime lastLoginTimestamp;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Check if the account is currently locked.
     * Account is locked if isLocked is true and lockout period hasn't expired.
     */
    @JsonIgnore
    public boolean isAccountLocked() {
        if (!isLocked) {
            return false;
        }
        if (accountLockedUntil == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(accountLockedUntil);
    }

    /**
     * Check if the account is active and not locked.
     */
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !isAccountLocked();
    }

    /**
     * Check if the account is enabled.
     */
    @JsonIgnore
    public boolean isEnabled() {
        return isActive != null && isActive;
    }

    /**
     * Increment failed login attempts counter.
     */
    public void incrementFailedAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
    }

    /**
     * Reset failed login attempts to zero.
     */
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.isLocked = false;
        this.accountLockedUntil = null;
    }

    /**
     * Lock the account until the specified time.
     */
    public void lockAccount(LocalDateTime lockUntil) {
        this.isLocked = true;
        this.accountLockedUntil = lockUntil;
    }

    /**
     * Update last login timestamp to current time.
     */
    public void updateLastLogin() {
        this.lastLoginTimestamp = LocalDateTime.now();
    }

    /**
     * Get full name of the user.
     */
    @JsonIgnore
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }
}

