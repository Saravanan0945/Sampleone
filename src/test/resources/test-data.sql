-- Test data for integration tests
-- Password for all users: Password123!
-- BCrypt hash: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIiIkYiOf6

DELETE FROM refresh_tokens;
DELETE FROM password_reset_tokens;
DELETE FROM users;

INSERT INTO users (id, username, email, password_hash, first_name, last_name, is_active, is_locked, failed_login_attempts, created_at, updated_at)
VALUES
    (100, 'integrationuser', 'integration@test.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIiIkYiOf6', 'Integration', 'User', true, false, 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (101, 'lockeduser', 'locked@test.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIiIkYiOf6', 'Locked', 'User', true, true, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (102, 'inactiveuser', 'inactive@test.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIiIkYiOf6', 'Inactive', 'User', false, false, 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Set locked account expiry to future (15 minutes from now)
UPDATE users SET account_locked_until = TIMESTAMPADD(MINUTE, 15, CURRENT_TIMESTAMP()) WHERE id = 101;

