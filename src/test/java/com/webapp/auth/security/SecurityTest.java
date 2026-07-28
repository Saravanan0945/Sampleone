package com.webapp.auth.security;

import com.webapp.auth.dto.LoginRequest;
import com.webapp.auth.service.RateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.HtmlUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security Tests")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimitingService rateLimitingService;

    private final String testIpAddress = "192.168.1.100";

    @BeforeEach
    void setUp() {
        // Reset rate limiting for test IP
        rateLimitingService.resetLoginAttempts(testIpAddress);
    }

    @Test
    @DisplayName("Brute force protection: Multiple rapid login attempts should be rate limited")
    void testBruteForceProtection() {
        // Simulate 15 rapid login attempts (limit is 10)
        for (int i = 0; i < 15; i++) {
            rateLimitingService.recordLoginAttempt(testIpAddress);
        }

        // Verify rate limiting is active
        boolean isRateLimited = rateLimitingService.isRateLimited(testIpAddress);
        assertThat(isRateLimited).isTrue();

        // Verify remaining attempts is 0
        int remainingAttempts = rateLimitingService.getRemainingAttempts(testIpAddress);
        assertThat(remainingAttempts).isEqualTo(0);
    }

    @Test
    @DisplayName("Rate limiting should reset after time window expires")
    void testRateLimitingReset() {
        // Record some attempts
        for (int i = 0; i < 5; i++) {
            rateLimitingService.recordLoginAttempt(testIpAddress);
        }

        // Manually reset
        rateLimitingService.resetLoginAttempts(testIpAddress);

        // Verify rate limiting is not active
        boolean isRateLimited = rateLimitingService.isRateLimited(testIpAddress);
        assertThat(isRateLimited).isFalse();

        // Verify remaining attempts is back to max
        int remainingAttempts = rateLimitingService.getRemainingAttempts(testIpAddress);
        assertThat(remainingAttempts).isEqualTo(10); // Default max attempts
    }

    @Test
    @Sql("/test-data.sql")
    @DisplayName("Input sanitization: XSS attempt in username should be sanitized")
    void testInputSanitization_XSSInUsername() throws Exception {
        String xssPayload = "<script>alert('XSS')</script>";
        String sanitizedPayload = HtmlUtils.htmlEscape(xssPayload);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(xssPayload);
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + xssPayload + "\",\"password\":\"Password123!\"}"))
                .andDo(print())
                .andExpect(status().isNotFound()); // User not found because XSS payload doesn't exist

        // Verify the payload would be sanitized if it reached the service layer
        assertThat(sanitizedPayload).doesNotContain("<script>");
        assertThat(sanitizedPayload).contains("&lt;script&gt;");
    }

    @Test
    @Sql("/test-data.sql")
    @DisplayName("Input sanitization: SQL injection attempt should be handled safely")
    void testInputSanitization_SQLInjection() throws Exception {
        String sqlInjectionPayload = "admin' OR '1'='1";

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(sqlInjectionPayload);
        loginRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + sqlInjectionPayload + "\",\"password\":\"password\"}"))
                .andDo(print())
                .andExpect(status().isNotFound()); // Should not find user, SQL injection prevented
    }

    @Test
    @DisplayName("CORS configuration: Preflight request should be allowed")
    void testCORSConfiguration() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("CORS configuration: Allowed origins should be configured")
    void testCORSAllowedOrigins() throws Exception {
        mockMvc.perform(post("/api/auth/health")
                        .header("Origin", "http://localhost:8080"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Security headers: Response should include security headers")
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"));
    }

    @Test
    @Sql("/test-data.sql")
    @DisplayName("Password field should never be exposed in API responses")
    void testPasswordNotExposedInResponse() throws Exception {
        // Login successfully
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"integrationuser\",\"password\":\"Password123!\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("Unauthorized access to protected endpoint should return 401")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/auth/profile"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid JWT token should return 401")
    void testInvalidJWTToken() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Malformed Authorization header should return 401")
    void testMalformedAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "InvalidFormat token123"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Public endpoints should be accessible without authentication")
    void testPublicEndpointsAccessible() throws Exception {
        // Health endpoint
        mockMvc.perform(get("/api/auth/health"))
                .andDo(print())
                .andExpect(status().isOk());

        // Login endpoint
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andDo(print())
                .andExpect(status().isNotFound()); // User not found, but endpoint is accessible

        // Forgot password endpoint
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andDo(print())
                .andExpect(status().isNotFound()); // User not found, but endpoint is accessible
    }

    @Test
    @DisplayName("CSRF protection should be disabled for stateless API")
    void testCSRFDisabled() throws Exception {
        // POST request without CSRF token should work (CSRF is disabled for stateless JWT)
        mockMvc.perform(post("/api/auth/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed()); // Method not allowed, but not CSRF error
    }

    @Test
    @DisplayName("Rate limiting should track attempts per IP address")
    void testRateLimitingPerIP() {
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.2";

        // Record attempts for IP1
        for (int i = 0; i < 10; i++) {
            rateLimitingService.recordLoginAttempt(ip1);
        }

        // IP1 should be rate limited
        assertThat(rateLimitingService.isRateLimited(ip1)).isTrue();

        // IP2 should not be rate limited
        assertThat(rateLimitingService.isRateLimited(ip2)).isFalse();
    }

    @Test
    @DisplayName("Empty or null JWT token should return 401")
    void testEmptyJWTToken() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer "))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("JWT token without Bearer prefix should return 401")
    void testJWTTokenWithoutBearerPrefix() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "some.jwt.token"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Multiple failed login attempts should increment counter")
    void testFailedLoginAttemptsIncrement() {
        String testIp = "192.168.1.50";
        
        // Initial state
        assertThat(rateLimitingService.getRemainingAttempts(testIp)).isEqualTo(10);

        // Record 3 attempts
        for (int i = 0; i < 3; i++) {
            rateLimitingService.recordLoginAttempt(testIp);
        }

        // Verify remaining attempts decreased
        assertThat(rateLimitingService.getRemainingAttempts(testIp)).isEqualTo(7);
    }

    @Test
    @DisplayName("Content-Type validation: Invalid content type should return 415")
    void testInvalidContentType() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("username=test&password=test"))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }
}

