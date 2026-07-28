package com.webapp.auth.controller;

import com.webapp.auth.dto.*;
import com.webapp.auth.service.AuthenticationService;
import com.webapp.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 * Handles user login, logout, password reset, token refresh, and profile management.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * User login endpoint.
     * Authenticates user credentials and returns JWT tokens.
     *
     * @param loginRequest Login credentials (username/email and password)
     * @param request HTTP request for IP address extraction
     * @return LoginResponse with access token, refresh token, and user details
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user with username/email and password. Returns JWT access token and refresh token."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Account locked due to multiple failed login attempts",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too many login attempts - rate limit exceeded",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        )
    })
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        String ipAddress = extractIpAddress(request);
        log.info("Login attempt for user: {} from IP: {}", loginRequest.getUsernameOrEmail(), ipAddress);
        
        LoginResponse response = authenticationService.login(loginRequest, ipAddress);
        
        log.info("Login successful for user: {}", loginRequest.getUsernameOrEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * User logout endpoint.
     * Invalidates all refresh tokens for the authenticated user.
     *
     * @return Success message
     */
    @PostMapping("/logout")
    @Operation(
        summary = "User logout",
        description = "Logout authenticated user and invalidate all refresh tokens."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<com.webapp.auth.dto.ApiResponse<String>> logout() {
        
        String username = extractUsernameFromSecurityContext();
        log.info("Logout request for user: {}", username);
        
        authenticationService.logout(username);
        
        log.info("Logout successful for user: {}", username);
        return ResponseEntity.ok(
            com.webapp.auth.dto.ApiResponse.<String>builder()
                .success(true)
                .message("Logout successful")
                .data("User logged out successfully")
                .build()
        );
    }

    /**
     * Forgot password endpoint.
     * Initiates password reset process by sending reset token to user's email.
     *
     * @param request Forgot password request with email
     * @return Success message
     */
    @PostMapping("/forgot-password")
    @Operation(
        summary = "Forgot password",
        description = "Initiate password reset process. Sends password reset token to user's email."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset email sent successfully",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found with provided email",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        )
    })
    public ResponseEntity<com.webapp.auth.dto.ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        log.info("Forgot password request for email: {}", request.getEmail());
        
        com.webapp.auth.dto.ApiResponse<String> response = authenticationService.forgotPassword(request.getEmail());
        
        log.info("Forgot password email sent to: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Reset password endpoint.
     * Resets user password using the reset token.
     *
     * @param request Reset password request with token and new password
     * @return Success message
     */
    @PostMapping("/reset-password")
    @Operation(
        summary = "Reset password",
        description = "Reset user password using the reset token received via email."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset successful",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired reset token",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        )
    })
    public ResponseEntity<com.webapp.auth.dto.ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        log.info("Reset password request with token: {}", request.getToken().substring(0, 10) + "...");
        
        com.webapp.auth.dto.ApiResponse<String> response = authenticationService.resetPassword(
            request.getToken(),
            request.getNewPassword()
        );
        
        log.info("Password reset successful");
        return ResponseEntity.ok(response);
    }

    /**
     * Get user profile endpoint.
     * Returns authenticated user's profile information.
     *
     * @return User profile details
     */
    @GetMapping("/profile")
    @Operation(
        summary = "Get user profile",
        description = "Retrieve authenticated user's profile information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserProfileResponse> getProfile() {
        
        String username = extractUsernameFromSecurityContext();
        log.info("Profile request for user: {}", username);
        
        UserProfileResponse profile = userService.getUserProfile(username);
        
        log.info("Profile retrieved successfully for user: {}", username);
        return ResponseEntity.ok(profile);
    }

    /**
     * Refresh access token endpoint.
     * Generates new access token using refresh token.
     *
     * @param request Refresh token request
     * @return New LoginResponse with fresh access token
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Generate new access token using refresh token."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        )
    })
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        log.info("Token refresh request");
        
        LoginResponse response = authenticationService.refreshAccessToken(request.getRefreshToken());
        
        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for authentication service.
     *
     * @return Success message
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Check if authentication service is running."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(schema = @Schema(implementation = com.webapp.auth.dto.ApiResponse.class))
        )
    })
    public ResponseEntity<com.webapp.auth.dto.ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            com.webapp.auth.dto.ApiResponse.<String>builder()
                .success(true)
                .message("Authentication service is running")
                .data("OK")
                .build()
        );
    }

    /**
     * Extract IP address from HTTP request.
     * Handles X-Forwarded-For header for proxy/load balancer scenarios.
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Handle multiple IPs in X-Forwarded-For (take first one)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress != null ? ipAddress : "unknown";
    }

    /**
     * Extract username from Spring Security context.
     *
     * @return Authenticated username
     */
    private String extractUsernameFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authenticated user found in security context");
            throw new RuntimeException("User not authenticated");
        }
        
        return authentication.getName();
    }
}

