package com.webapp.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * RefreshToken entity for managing JWT refresh tokens.
 * Enables token rotation and revocation for enhanced security.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_token", columnList = "token"),
    @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_tokens_expiry_date", columnList = "expiryDate")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_token_user"))
    private User user;

    @NotBlank(message = "Token is required")
    @Column(name = "token", unique = true, nullable = false, length = 500)
    private String token;

    @NotNull(message = "Expiry date is required")
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Check if the refresh token has expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Check if the refresh token is still valid.
     */
    public boolean isValid() {
        return !isExpired();
    }
}

