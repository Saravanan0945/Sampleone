-- ============================================================================
-- User Authentication Schema
-- Database: MySQL/PostgreSQL compatible
-- ============================================================================

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS users;

-- ============================================================================
-- Users Table
-- Stores user account information and authentication details
-- ============================================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    is_locked BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    last_login_timestamp TIMESTAMP NULL,
    account_locked_until TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_username_length CHECK (CHAR_LENGTH(username) >= 3),
    CONSTRAINT chk_email_format CHECK (email LIKE '%@%.%'),
    CONSTRAINT chk_failed_attempts CHECK (failed_login_attempts >= 0)
);

-- ============================================================================
-- Refresh Tokens Table
-- Stores JWT refresh tokens for token rotation and revocation
-- ============================================================================
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key relationship
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_expiry_future CHECK (expiry_date > created_at)
);

-- ============================================================================
-- Password Reset Tokens Table
-- Stores one-time use tokens for password reset functionality
-- ============================================================================
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key relationship
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_reset_expiry_future CHECK (expiry_date > created_at)
);

-- ============================================================================
-- Performance Indexes
-- ============================================================================

-- Users table indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_is_locked ON users(is_locked);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Refresh tokens table indexes
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expiry_date ON refresh_tokens(expiry_date);

-- Password reset tokens table indexes
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_expiry ON password_reset_tokens(expiry_date);
CREATE INDEX idx_password_reset_is_used ON password_reset_tokens(is_used);

-- ============================================================================
-- Comments for documentation
-- ============================================================================

-- Users table column descriptions:
-- id: Unique identifier for each user
-- username: Unique username for login (3-50 characters)
-- email: Unique email address for login and communication
-- password_hash: BCrypt hashed password (never store plain text)
-- first_name: User's first name (optional)
-- last_name: User's last name (optional)
-- is_active: Account activation status (email verification, admin approval)
-- is_locked: Temporary lock status (security measure)
-- failed_login_attempts: Counter for failed login attempts (reset on success)
-- last_login_timestamp: Timestamp of last successful login
-- account_locked_until: Timestamp until which account is locked (NULL if not locked)
-- created_at: Account creation timestamp
-- updated_at: Last modification timestamp (auto-updated)

-- Refresh tokens table column descriptions:
-- id: Unique identifier for each refresh token
-- user_id: Reference to the user who owns this token
-- token: The actual JWT refresh token string
-- expiry_date: When this refresh token expires
-- created_at: When this refresh token was created

-- Password reset tokens table column descriptions:
-- id: Unique identifier for each reset token
-- user_id: Reference to the user requesting password reset
-- token: The actual reset token (UUID or JWT)
-- expiry_date: When this reset token expires (typically 1 hour)
-- is_used: Whether this token has been used (one-time use only)
-- created_at: When this reset token was created


