package com.webapp.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for rate limiting login attempts by IP address to prevent brute force attacks.
 * Uses in-memory cache with automatic cleanup of expired entries.
 */
@Service
@Slf4j
public class RateLimitingService {

    @Value("${security.rate-limit.max-attempts:10}")
    private int maxAttempts;

    @Value("${security.rate-limit.window-minutes:15}")
    private int windowMinutes;

    private final Map<String, LoginAttemptInfo> loginAttempts = new ConcurrentHashMap<>();

    /**
     * Check if the IP address has exceeded the rate limit
     */
    public boolean isRateLimited(String ipAddress) {
        cleanupExpiredEntries();
        
        LoginAttemptInfo attemptInfo = loginAttempts.get(ipAddress);
        if (attemptInfo == null) {
            return false;
        }

        if (attemptInfo.isExpired(windowMinutes)) {
            loginAttempts.remove(ipAddress);
            return false;
        }

        boolean limited = attemptInfo.getAttempts() >= maxAttempts;
        if (limited) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
        }
        return limited;
    }

    /**
     * Record a login attempt for the IP address
     */
    public void recordLoginAttempt(String ipAddress) {
        cleanupExpiredEntries();
        
        loginAttempts.compute(ipAddress, (key, existingInfo) -> {
            if (existingInfo == null || existingInfo.isExpired(windowMinutes)) {
                return new LoginAttemptInfo(1, LocalDateTime.now());
            } else {
                existingInfo.incrementAttempts();
                return existingInfo;
            }
        });

        log.debug("Recorded login attempt for IP: {}. Total attempts: {}", 
                  ipAddress, loginAttempts.get(ipAddress).getAttempts());
    }

    /**
     * Reset login attempts for the IP address (called after successful login)
     */
    public void resetLoginAttempts(String ipAddress) {
        loginAttempts.remove(ipAddress);
        log.debug("Reset login attempts for IP: {}", ipAddress);
    }

    /**
     * Get remaining attempts before rate limit
     */
    public int getRemainingAttempts(String ipAddress) {
        LoginAttemptInfo attemptInfo = loginAttempts.get(ipAddress);
        if (attemptInfo == null || attemptInfo.isExpired(windowMinutes)) {
            return maxAttempts;
        }
        return Math.max(0, maxAttempts - attemptInfo.getAttempts());
    }

    /**
     * Clean up expired entries to prevent memory leaks
     */
    private void cleanupExpiredEntries() {
        loginAttempts.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(windowMinutes)
        );
    }

    /**
     * Inner class to track login attempt information
     */
    private static class LoginAttemptInfo {
        private int attempts;
        private final LocalDateTime firstAttemptTime;

        public LoginAttemptInfo(int attempts, LocalDateTime firstAttemptTime) {
            this.attempts = attempts;
            this.firstAttemptTime = firstAttemptTime;
        }

        public int getAttempts() {
            return attempts;
        }

        public void incrementAttempts() {
            this.attempts++;
        }

        public boolean isExpired(int windowMinutes) {
            return LocalDateTime.now().isAfter(firstAttemptTime.plusMinutes(windowMinutes));
        }
    }
}

