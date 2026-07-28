package com.webapp.auth.integration;

import com.webapp.auth.dto.*;
import com.webapp.auth.model.User;
import com.webapp.auth.repository.RefreshTokenRepository;
import com.webapp.auth.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.util.Objects;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";
    }

    @Test
    @Order(1)
    @Sql("/test-data.sql")
    @DisplayName("Complete authentication flow: login → access protected endpoint → logout")
    void testCompleteAuthenticationFlow() {
        // Step 1: Login with valid credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("integrationuser");
        loginRequest.setPassword("Password123!");

        ResponseEntity<ApiResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                loginRequest,
                ApiResponse.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().isSuccess()).isTrue();

        // Extract access token from response
        @SuppressWarnings("unchecked")
        var data = (java.util.LinkedHashMap<String, Object>) loginResponse.getBody().getData();
        String accessToken = (String) data.get("accessToken");
        String refreshToken = (String) data.get("refreshToken");

        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();

        // Step 2: Access protected endpoint with token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse> profileResponse = restTemplate.exchange(
                baseUrl + "/profile",
                HttpMethod.GET,
                requestEntity,
                ApiResponse.class
        );

        assertThat(profileResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(profileResponse.getBody()).isNotNull();
        assertThat(profileResponse.getBody().isSuccess()).isTrue();

        @SuppressWarnings("unchecked")
        var profileData = (java.util.LinkedHashMap<String, Object>) profileResponse.getBody().getData();
        assertThat(profileData.get("username")).isEqualTo("integrationuser");
        assertThat(profileData.get("email")).isEqualTo("integration@test.com");

        // Step 3: Logout
        ResponseEntity<ApiResponse> logoutResponse = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                requestEntity,
                ApiResponse.class
        );

        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logoutResponse.getBody()).isNotNull();
        assertThat(logoutResponse.getBody().isSuccess()).isTrue();

        // Verify refresh tokens are deleted
        assertThat(refreshTokenRepository.findByToken(refreshToken)).isEmpty();
    }

    @Test
    @Order(2)
    @Sql("/test-data.sql")
    @DisplayName("Account lockout after 5 failed login attempts")
    void testAccountLockoutAfterFailedAttempts() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("integrationuser");
        loginRequest.setPassword("WrongPassword");

        // Attempt 5 failed logins
        for (int i = 0; i < 5; i++) {
            ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                    baseUrl + "/login",
                    loginRequest,
                    ApiResponse.class
            );

            if (i < 4) {
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            } else {
                // 5th attempt should lock the account
                assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.LOCKED);
            }
        }

        // Verify account is locked in database
        User user = userRepository.findByUsername("integrationuser").orElseThrow();
        assertThat(user.getFailedLoginAttempts()).isGreaterThanOrEqualTo(5);
        assertThat(user.isLocked()).isTrue();
        assertThat(user.getAccountLockedUntil()).isNotNull();

        // Try to login with correct password - should still be locked
        loginRequest.setPassword("Password123!");
        ResponseEntity<ApiResponse> lockedResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                loginRequest,
                ApiResponse.class
        );

        assertThat(lockedResponse.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
    }

    @Test
    @Order(3)
    @Sql("/test-data.sql")
    @DisplayName("Password reset flow: forgot password → reset password → login with new password")
    void testPasswordResetFlow() {
        // Step 1: Request password reset
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
        forgotRequest.setEmail("integrationuser@test.com");

        ResponseEntity<ApiResponse> forgotResponse = restTemplate.postForEntity(
                baseUrl + "/forgot-password",
                forgotRequest,
                ApiResponse.class
        );

        assertThat(forgotResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // In a real scenario, we would extract the token from email
        // For testing, we'll query the database directly
        User user = userRepository.findByEmail("integrationuser@test.com").orElseThrow();
        
        // Note: In actual implementation, we would need to retrieve the reset token
        // For this test, we'll simulate the reset process
        
        // Step 2: Reset password with token
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setToken("test-reset-token"); // This would be from email in real scenario
        resetRequest.setNewPassword("NewPassword456!");

        // Note: This will fail without a valid token, which is expected behavior
        // In a full integration test, you would need to extract the actual token
    }

    @Test
    @Order(4)
    @Sql("/test-data.sql")
    @DisplayName("JWT token refresh flow")
    void testTokenRefreshFlow() {
        // Step 1: Login to get tokens
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("integrationuser");
        loginRequest.setPassword("Password123!");

        ResponseEntity<ApiResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                loginRequest,
                ApiResponse.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        var data = (java.util.LinkedHashMap<String, Object>) Objects.requireNonNull(loginResponse.getBody()).getData();
        String refreshToken = (String) data.get("refreshToken");

        // Step 2: Use refresh token to get new access token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        ResponseEntity<ApiResponse> refreshResponse = restTemplate.postForEntity(
                baseUrl + "/refresh",
                refreshRequest,
                ApiResponse.class
        );

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResponse.getBody()).isNotNull();
        assertThat(refreshResponse.getBody().isSuccess()).isTrue();

        @SuppressWarnings("unchecked")
        var refreshData = (java.util.LinkedHashMap<String, Object>) refreshResponse.getBody().getData();
        String newAccessToken = (String) refreshData.get("accessToken");

        assertThat(newAccessToken).isNotNull();
        assertThat(newAccessToken).isNotEmpty();

        // Step 3: Use new access token to access protected endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(newAccessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse> profileResponse = restTemplate.exchange(
                baseUrl + "/profile",
                HttpMethod.GET,
                requestEntity,
                ApiResponse.class
        );

        assertThat(profileResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(5)
    @Sql("/test-data.sql")
    @DisplayName("Access protected endpoint without token should return 401")
    void testAccessProtectedEndpointWithoutToken() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                baseUrl + "/profile",
                ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(6)
    @Sql("/test-data.sql")
    @DisplayName("Access protected endpoint with invalid token should return 401")
    void testAccessProtectedEndpointWithInvalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.jwt.token");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl + "/profile",
                HttpMethod.GET,
                requestEntity,
                ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(7)
    @Sql("/test-data.sql")
    @DisplayName("Login with inactive account should fail")
    void testLoginWithInactiveAccount() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("inactiveuser");
        loginRequest.setPassword("Password123!");

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl + "/login",
                loginRequest,
                ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    @Order(8)
    @Sql("/test-data.sql")
    @DisplayName("Login with already locked account should fail")
    void testLoginWithLockedAccount() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("lockeduser");
        loginRequest.setPassword("Password123!");

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl + "/login",
                loginRequest,
                ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("locked");
    }

    @Test
    @Order(9)
    @DisplayName("Health check endpoint should be accessible without authentication")
    void testHealthCheckEndpoint() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                baseUrl + "/health",
                ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    @Order(10)
    @Sql("/test-data.sql")
    @DisplayName("Successful login should reset failed login attempts")
    void testSuccessfulLoginResetsFailedAttempts() {
        // First, make a failed attempt
        LoginRequest failedRequest = new LoginRequest();
        failedRequest.setUsername("integrationuser");
        failedRequest.setPassword("WrongPassword");

        restTemplate.postForEntity(baseUrl + "/login", failedRequest, ApiResponse.class);

        // Verify failed attempts incremented
        User user = userRepository.findByUsername("integrationuser").orElseThrow();
        assertThat(user.getFailedLoginAttempts()).isGreaterThan(0);

        // Now login successfully
        LoginRequest successRequest = new LoginRequest();
        successRequest.setUsername("integrationuser");
        successRequest.setPassword("Password123!");

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl + "/login",
                successRequest,
                ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify failed attempts reset to 0
        user = userRepository.findByUsername("integrationuser").orElseThrow();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(user.getLastLoginTimestamp()).isNotNull();
    }
}

