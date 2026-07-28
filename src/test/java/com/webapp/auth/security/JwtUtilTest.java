package com.webapp.auth.security;

import com.webapp.auth.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testUsername = "testuser";
    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long jwtExpiration = 86400000; // 24 hours
    private final long refreshExpiration = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", jwtExpiration);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", refreshExpiration);
        jwtUtil.init(); // Initialize the signing key
    }

    @Test
    @DisplayName("Generate token should create valid JWT")
    void testGenerateToken_CreatesValidJWT() {
        // Act
        String token = jwtUtil.generateToken(testUsername);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("Extract username from token should return correct username")
    void testExtractUsername_ReturnsCorrectUsername() {
        // Arrange
        String token = jwtUtil.generateToken(testUsername);

        // Act
        String extractedUsername = jwtUtil.extractUsername(token);

        // Assert
        assertThat(extractedUsername).isEqualTo(testUsername);
    }

    @Test
    @DisplayName("Extract expiration from token should return future date")
    void testExtractExpiration_ReturnsFutureDate() {
        // Arrange
        String token = jwtUtil.generateToken(testUsername);

        // Act
        Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Validate token with correct username should return true")
    void testValidateToken_CorrectUsername_ReturnsTrue() {
        // Arrange
        String token = jwtUtil.generateToken(testUsername);

        // Act
        boolean isValid = jwtUtil.validateToken(token, testUsername);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Validate token with incorrect username should return false")
    void testValidateToken_IncorrectUsername_ReturnsFalse() {
        // Arrange
        String token = jwtUtil.generateToken(testUsername);

        // Act
        boolean isValid = jwtUtil.validateToken(token, "wronguser");

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Is token expired should return false for fresh token")
    void testIsTokenExpired_FreshToken_ReturnsFalse() {
        // Arrange
        String token = jwtUtil.generateToken(testUsername);

        // Act
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Assert
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Generate refresh token should create valid token")
    void testGenerateRefreshToken_CreatesValidToken() {
        // Act
        String refreshToken = jwtUtil.generateRefreshToken(testUsername);

        // Assert
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Refresh token should have longer expiration than access token")
    void testRefreshToken_HasLongerExpiration() {
        // Arrange
        String accessToken = jwtUtil.generateToken(testUsername);
        String refreshToken = jwtUtil.generateRefreshToken(testUsername);

        // Act
        Date accessExpiration = jwtUtil.extractExpiration(accessToken);
        Date refreshExpiration = jwtUtil.extractExpiration(refreshToken);

        // Assert
        assertThat(refreshExpiration).isAfter(accessExpiration);
    }

    @Test
    @DisplayName("Validate token with malformed token should throw exception")
    void testValidateToken_MalformedToken_ThrowsException() {
        // Arrange
        String malformedToken = "this.is.not.a.valid.jwt";

        // Act & Assert
        assertThatThrownBy(() -> jwtUtil.validateToken(malformedToken, testUsername))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("Validate token with invalid signature should throw exception")
    void testValidateToken_InvalidSignature_ThrowsException() {
        // Arrange
        String token = jwtUtil.generateToken(testUsername);
        String tamperedToken = token.substring(0, token.length() - 10) + "tampered123";

        // Act & Assert
        assertThatThrownBy(() -> jwtUtil.validateToken(tamperedToken, testUsername))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Validate expired token should throw ExpiredJwtException")
    void testValidateToken_ExpiredToken_ThrowsException() {
        // Arrange - Create a JWT util with very short expiration
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secretKey", secretKey);
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "jwtExpiration", 1L); // 1 millisecond
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "refreshExpiration", refreshExpiration);
        shortExpirationJwtUtil.init();

        String token = shortExpirationJwtUtil.generateToken(testUsername);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertThatThrownBy(() -> shortExpirationJwtUtil.validateToken(token, testUsername))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Extract username from invalid token should throw exception")
    void testExtractUsername_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("Generate token with null username should throw exception")
    void testGenerateToken_NullUsername_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> jwtUtil.generateToken(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Generate token with empty username should throw exception")
    void testGenerateToken_EmptyUsername_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> jwtUtil.generateToken(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Multiple tokens for same user should be different")
    void testGenerateToken_MultipleCalls_GeneratesDifferentTokens() {
        // Act
        String token1 = jwtUtil.generateToken(testUsername);
        
        // Small delay to ensure different issued-at time
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtUtil.generateToken(testUsername);

        // Assert
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Token should contain correct claims")
    void testGenerateToken_ContainsCorrectClaims() {
        // Act
        String token = jwtUtil.generateToken(testUsername);
        String extractedUsername = jwtUtil.extractUsername(token);
        Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertThat(extractedUsername).isEqualTo(testUsername);
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
        
        // Check that expiration is approximately 24 hours from now
        long expirationTime = expiration.getTime() - System.currentTimeMillis();
        assertThat(expirationTime).isBetween(jwtExpiration - 5000, jwtExpiration + 5000);
    }
}

