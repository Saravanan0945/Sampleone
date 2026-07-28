-- ============================================================================
-- Sample Test Data for Development and Testing
-- ============================================================================
-- IMPORTANT: This file should NOT be used in production environments
-- Remove or disable this file before deploying to production
-- ============================================================================

-- Sample Users with BCrypt hashed passwords
-- All passwords are hashed with BCrypt strength 12
-- Default password for all test users: "Password123!"

-- Test User 1: Active user with no failed attempts
INSERT INTO users (username, email, password_hash, first_name, last_name, is_active, is_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'johndoe',
    'john.doe@example.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVvMpYKZTe',
    'John',
    'Doe',
    TRUE,
    FALSE,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Test User 2: Active user with some login history
INSERT INTO users (username, email, password_hash, first_name, last_name, is_active, is_locked, failed_login_attempts, last_login_timestamp, created_at, updated_at)
VALUES (
    'janedoe',
    'jane.doe@example.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVvMpYKZTe',
    'Jane',
    'Doe',
    TRUE,
    FALSE,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Test User 3: Admin user
INSERT INTO users (username, email, password_hash, first_name, last_name, is_active, is_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'admin',
    'admin@example.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVvMpYKZTe',
    'Admin',
    'User',
    TRUE,
    FALSE,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Test User 4: Inactive user (for testing account activation)
INSERT INTO users (username, email, password_hash, first_name, last_name, is_active, is_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'inactiveuser',
    'inactive@example.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVvMpYKZTe',
    'Inactive',
    'User',
    FALSE,
    FALSE,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Test User 5: Locked user (for testing account lockout)
INSERT INTO users (username, email, password_hash, first_name, last_name, is_active, is_locked, failed_login_attempts, account_locked_until, created_at, updated_at)
VALUES (
    'lockeduser',
    'locked@example.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVvMpYKZTe',
    'Locked',
    'User',
    TRUE,
    TRUE,
    5,
    TIMESTAMPADD(MINUTE, 15, CURRENT_TIMESTAMP),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Test User 6: User with failed login attempts (but not locked yet)
INSERT INTO users (username, email, password_hash, first_name, last_name, is_active, is_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'testuser',
    'test@example.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVvMpYKZTe',
    'Test',
    'User',
    TRUE,
    FALSE,
    3,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================================
-- Test Credentials Summary
-- ============================================================================
-- Username: johndoe      | Email: john.doe@example.com    | Password: Password123! | Status: Active
-- Username: janedoe      | Email: jane.doe@example.com    | Password: Password123! | Status: Active
-- Username: admin        | Email: admin@example.com       | Password: Password123! | Status: Active
-- Username: inactiveuser | Email: inactive@example.com    | Password: Password123! | Status: Inactive
-- Username: lockeduser   | Email: locked@example.com      | Password: Password123! | Status: Locked
-- Username: testuser     | Email: test@example.com        | Password: Password123! | Status: Active (3 failed attempts)
-- ============================================================================

-- Note: The BCrypt hash above corresponds to the password "Password123!"
-- To generate new BCrypt hashes, use:
-- - Online: https://bcrypt-generator.com/ (set rounds to 12)
-- - Java: BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
--         String hash = encoder.encode("yourPassword");
-- - Command line: htpasswd -bnBC 12 "" yourPassword | tr -d ':\n'

