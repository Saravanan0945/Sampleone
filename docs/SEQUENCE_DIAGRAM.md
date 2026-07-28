# Sequence Diagrams - Secure User Authentication System

This document contains detailed sequence diagrams for all major authentication flows in the system.

## Table of Contents
1. [Login Flow](#1-login-flow)
2. [Logout Flow](#2-logout-flow)
3. [Forgot Password Flow](#3-forgot-password-flow)
4. [Reset Password Flow](#4-reset-password-flow)
5. [Access Protected Resource Flow](#5-access-protected-resource-flow)
6. [Token Refresh Flow](#6-token-refresh-flow)
7. [Account Lockout Flow](#7-account-lockout-flow)
8. [Rate Limiting Flow](#8-rate-limiting-flow)

---

## 1. Login Flow

### Successful Login

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant AuthController
    participant RateLimitingService
    participant AuthenticationService
    participant UserRepository
    participant PasswordEncoder
    participant JwtUtil
    participant RefreshTokenRepository
    participant Database

    User->>Frontend: Enter username & password
    Frontend->>Frontend: Client-side validation
    Frontend->>AuthController: POST /api/auth/login<br/>{username, password}
    
    AuthController->>RateLimitingService: isRateLimited(ipAddress)
    RateLimitingService-->>AuthController: false (not rate limited)
    
    AuthController->>AuthenticationService: login(LoginRequest, ipAddress)
    
    AuthenticationService->>UserRepository: findByUsernameOrEmail(username)
    UserRepository->>Database: SELECT * FROM users WHERE...
    Database-->>UserRepository: User record
    UserRepository-->>AuthenticationService: Optional<User>
    
    AuthenticationService->>AuthenticationService: Check if user exists
    AuthenticationService->>AuthenticationService: Check is_locked status
    AuthenticationService->>AuthenticationService: Check account_locked_until
    
    AuthenticationService->>PasswordEncoder: matches(password, user.passwordHash)
    PasswordEncoder-->>AuthenticationService: true (password valid)
    
    AuthenticationService->>UserRepository: Update user<br/>- failed_login_attempts = 0<br/>- last_login_timestamp = now
    UserRepository->>Database: UPDATE users SET...
    Database-->>UserRepository: Success
    
    AuthenticationService->>JwtUtil: generateToken(username)
    JwtUtil-->>AuthenticationService: accessToken
    
    AuthenticationService->>JwtUtil: generateRefreshToken(username)
    JwtUtil-->>AuthenticationService: refreshToken
    
    AuthenticationService->>RefreshTokenRepository: save(RefreshToken)
    RefreshTokenRepository->>Database: INSERT INTO refresh_tokens...
    Database-->>RefreshTokenRepository: Success
    
    AuthenticationService-->>AuthController: LoginResponse<br/>{accessToken, refreshToken, user}
    AuthController-->>Frontend: 200 OK<br/>LoginResponse
    
    Frontend->>Frontend: Store tokens in localStorage
    Frontend->>User: Redirect to profile page
```

### Failed Login (Invalid Credentials)

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant AuthController
    participant AuthenticationService
    participant UserRepository
    participant PasswordEncoder
    participant Database

    User->>Frontend: Enter username & wrong password
    Frontend->>AuthController: POST /api/auth/login
    
    AuthController->>AuthenticationService: login(LoginRequest, ipAddress)
    
    AuthenticationService->>UserRepository: findByUsernameOrEmail(username)
    UserRepository->>Database: SELECT * FROM users WHERE...
    Database-->>UserRepository: User record
    UserRepository-->>AuthenticationService: Optional<User>
    
    AuthenticationService->>PasswordEncoder: matches(password, user.passwordHash)
    PasswordEncoder-->>AuthenticationService: false (password invalid)
    
    AuthenticationService->>UserRepository: Update user<br/>- failed_login_attempts++
    UserRepository->>Database: UPDATE users SET<br/>failed_login_attempts = failed_login_attempts + 1
    Database-->>UserRepository: Success
    
    AuthenticationService-->>AuthController: throw InvalidCredentialsException
    AuthController-->>Frontend: 401 Unauthorized<br/>{message: "Invalid username or password"}
    
    Frontend->>User: Display error message
```

---

## 2. Logout Flow

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant AuthController
    participant JwtAuthenticationFilter
    participant JwtUtil
    participant AuthenticationService
    participant RefreshTokenRepository
    participant Database

    User->>Frontend: Click Logout button
    
    Frontend->>AuthController: POST /api/auth/logout<br/>Authorization: Bearer <token>
    
    AuthController->>JwtAuthenticationFilter: Filter request
    JwtAuthenticationFilter->>JwtUtil: extractUsername(token)
    JwtUtil-->>JwtAuthenticationFilter: username
    JwtAuthenticationFilter->>JwtUtil: validateToken(token, userDetails)
    JwtUtil-->>JwtAuthenticationFilter: true (valid)
    JwtAuthenticationFilter->>JwtAuthenticationFilter: Set authentication in SecurityContext
    
    AuthController->>AuthController: Get username from SecurityContext
    AuthController->>AuthenticationService: logout(username)
    
    AuthenticationService->>RefreshTokenRepository: deleteByUser(user)
    RefreshTokenRepository->>Database: DELETE FROM refresh_tokens<br/>WHERE user_id = ?
    Database-->>RefreshTokenRepository: Success
    
    AuthenticationService-->>AuthController: Success
    AuthController-->>Frontend: 200 OK<br/>{success: true, message: "Logged out successfully"}
    
    Frontend->>Frontend: Clear tokens from localStorage
    Frontend->>User: Redirect to login page
```

---

## 3. Forgot Password Flow

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant AuthController
    participant AuthenticationService
    participant UserRepository
    participant PasswordResetTokenRepository
    participant EmailService
    participant Database

    User->>Frontend: Enter email address
    Frontend->>Frontend: Validate email format
    Frontend->>AuthController: POST /api/auth/forgot-password<br/>{email}
    
    AuthController->>AuthenticationService: forgotPassword(email)
    
    AuthenticationService->>UserRepository: findByEmail(email)
    UserRepository->>Database: SELECT * FROM users WHERE email = ?
    Database-->>UserRepository: User record (or null)
    UserRepository-->>AuthenticationService: Optional<User>
    
    alt User exists
        AuthenticationService->>PasswordResetTokenRepository: deleteByUser(user)
        PasswordResetTokenRepository->>Database: DELETE FROM password_reset_tokens<br/>WHERE user_id = ?
        
        AuthenticationService->>AuthenticationService: Generate UUID token
        AuthenticationService->>AuthenticationService: Set expiry = now + 1 hour
        
        AuthenticationService->>PasswordResetTokenRepository: save(PasswordResetToken)
        PasswordResetTokenRepository->>Database: INSERT INTO password_reset_tokens...
        Database-->>PasswordResetTokenRepository: Success
        
        AuthenticationService->>EmailService: sendPasswordResetEmail(email, token)
        Note over EmailService: Simulated with log message<br/>In production: send actual email
        EmailService-->>AuthenticationService: Success
    else User not found
        Note over AuthenticationService: Return success anyway<br/>(prevent email enumeration)
    end
    
    AuthenticationService-->>AuthController: Success
    AuthController-->>Frontend: 200 OK<br/>{message: "If an account exists..."}
    
    Frontend->>User: Display success message<br/>"Check your email for reset link"
```

---

## 4. Reset Password Flow

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant AuthController
    participant AuthenticationService
    participant PasswordResetTokenRepository
    participant UserRepository
    participant PasswordEncoder
    participant RefreshTokenRepository
    participant Database

    User->>User: Click reset link in email
    User->>Frontend: GET /reset-password.html?token=<token>
    Frontend->>User: Display reset password form
    
    User->>Frontend: Enter new password
    Frontend->>Frontend: Validate password strength
    Frontend->>AuthController: POST /api/auth/reset-password<br/>{token, newPassword}
    
    AuthController->>AuthenticationService: resetPassword(token, newPassword)
    
    AuthenticationService->>PasswordResetTokenRepository: findByToken(token)
    PasswordResetTokenRepository->>Database: SELECT * FROM password_reset_tokens<br/>WHERE token = ?
    Database-->>PasswordResetTokenRepository: Token record (or null)
    PasswordResetTokenRepository-->>AuthenticationService: Optional<PasswordResetToken>
    
    alt Token not found
        AuthenticationService-->>AuthController: throw InvalidTokenException
        AuthController-->>Frontend: 400 Bad Request<br/>{message: "Invalid or expired token"}
    else Token found
        AuthenticationService->>AuthenticationService: Validate token<br/>- Check expiry_date > now<br/>- Check is_used = false
        
        alt Token valid
            AuthenticationService->>PasswordEncoder: encode(newPassword)
            PasswordEncoder-->>AuthenticationService: hashedPassword
            
            AuthenticationService->>UserRepository: Update user<br/>- password_hash = hashedPassword<br/>- is_locked = false<br/>- failed_login_attempts = 0
            UserRepository->>Database: UPDATE users SET...
            Database-->>UserRepository: Success
            
            AuthenticationService->>PasswordResetTokenRepository: Update token<br/>- is_used = true
            PasswordResetTokenRepository->>Database: UPDATE password_reset_tokens<br/>SET is_used = true
            Database-->>PasswordResetTokenRepository: Success
            
            AuthenticationService->>RefreshTokenRepository: deleteByUser(user)
            RefreshTokenRepository->>Database: DELETE FROM refresh_tokens<br/>WHERE user_id = ?
            Database-->>RefreshTokenRepository: Success
            
            AuthenticationService-->>AuthController: Success
            AuthController-->>Frontend: 200 OK<br/>{message: "Password reset successfully"}
            
            Frontend->>User: Display success message
            Frontend->>User: Redirect to login page
        else Token expired or used
            AuthenticationService-->>AuthController: throw InvalidTokenException
            AuthController-->>Frontend: 400 Bad Request<br/>{message: "Invalid or expired token"}
        end
    end
```

---

## 5. Access Protected Resource Flow

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant JwtAuthenticationFilter
    participant JwtUtil
    participant CustomUserDetailsService
    participant UserRepository
    participant AuthController
    participant Database

    User->>Frontend: Request profile page
    Frontend->>Frontend: Get token from localStorage
    Frontend->>JwtAuthenticationFilter: GET /api/auth/profile<br/>Authorization: Bearer <token>
    
    JwtAuthenticationFilter->>JwtAuthenticationFilter: Extract token from header
    
    alt Token present
        JwtAuthenticationFilter->>JwtUtil: extractUsername(token)
        JwtUtil-->>JwtAuthenticationFilter: username
        
        JwtAuthenticationFilter->>CustomUserDetailsService: loadUserByUsername(username)
        CustomUserDetailsService->>UserRepository: findByUsernameOrEmail(username)
        UserRepository->>Database: SELECT * FROM users WHERE...
        Database-->>UserRepository: User record
        UserRepository-->>CustomUserDetailsService: Optional<User>
        CustomUserDetailsService-->>JwtAuthenticationFilter: UserDetails
        
        JwtAuthenticationFilter->>JwtUtil: validateToken(token, userDetails)
        JwtUtil->>JwtUtil: Check signature
        JwtUtil->>JwtUtil: Check expiration
        JwtUtil-->>JwtAuthenticationFilter: true (valid)
        
        JwtAuthenticationFilter->>JwtAuthenticationFilter: Create Authentication object
        JwtAuthenticationFilter->>JwtAuthenticationFilter: Set in SecurityContext
        
        JwtAuthenticationFilter->>AuthController: Continue to controller
        AuthController->>AuthController: Get username from SecurityContext
        AuthController->>UserRepository: findByUsername(username)
        UserRepository->>Database: SELECT * FROM users WHERE...
        Database-->>UserRepository: User record
        UserRepository-->>AuthController: User
        
        AuthController-->>Frontend: 200 OK<br/>UserProfileResponse
        Frontend->>User: Display profile information
    else Token missing or invalid
        JwtAuthenticationFilter-->>Frontend: 401 Unauthorized
        Frontend->>Frontend: Clear tokens
        Frontend->>User: Redirect to login page
    end
```

---

## 6. Token Refresh Flow

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant AuthController
    participant AuthenticationService
    participant RefreshTokenRepository
    participant JwtUtil
    participant UserRepository
    participant Database

    User->>Frontend: Access token expired
    Frontend->>Frontend: Detect 401 response
    Frontend->>Frontend: Get refresh token from localStorage
    
    Frontend->>AuthController: POST /api/auth/refresh<br/>{refreshToken}
    
    AuthController->>AuthenticationService: refreshAccessToken(refreshToken)
    
    AuthenticationService->>RefreshTokenRepository: findByToken(refreshToken)
    RefreshTokenRepository->>Database: SELECT * FROM refresh_tokens<br/>WHERE token = ?
    Database-->>RefreshTokenRepository: Token record (or null)
    RefreshTokenRepository-->>AuthenticationService: Optional<RefreshToken>
    
    alt Token not found
        AuthenticationService-->>AuthController: throw InvalidTokenException
        AuthController-->>Frontend: 401 Unauthorized<br/>{message: "Invalid refresh token"}
        Frontend->>Frontend: Clear all tokens
        Frontend->>User: Redirect to login page
    else Token found
        AuthenticationService->>AuthenticationService: Check expiry_date > now
        
        alt Token expired
            AuthenticationService->>RefreshTokenRepository: delete(refreshToken)
            RefreshTokenRepository->>Database: DELETE FROM refresh_tokens<br/>WHERE id = ?
            
            AuthenticationService-->>AuthController: throw TokenExpiredException
            AuthController-->>Frontend: 401 Unauthorized<br/>{message: "Refresh token expired"}
            Frontend->>Frontend: Clear all tokens
            Frontend->>User: Redirect to login page
        else Token valid
            AuthenticationService->>UserRepository: findById(token.userId)
            UserRepository->>Database: SELECT * FROM users WHERE id = ?
            Database-->>UserRepository: User record
            UserRepository-->>AuthenticationService: User
            
            AuthenticationService->>AuthenticationService: Check user is_active = true
            
            AuthenticationService->>JwtUtil: generateToken(username)
            JwtUtil-->>AuthenticationService: newAccessToken
            
            AuthenticationService-->>AuthController: LoginResponse<br/>{accessToken: new, refreshToken: same}
            AuthController-->>Frontend: 200 OK<br/>LoginResponse
            
            Frontend->>Frontend: Update access token in localStorage
            Frontend->>Frontend: Retry original request with new token
        end
    end
```

---

## 7. Account Lockout Flow

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant AuthController
    participant AuthenticationService
    participant UserRepository
    participant PasswordEncoder
    participant Database

    Note over User,Database: Attempt 1-4: Failed logins
    
    loop Failed attempts 1-4
        User->>Frontend: Enter wrong password
        Frontend->>AuthController: POST /api/auth/login
        AuthController->>AuthenticationService: login(LoginRequest, ipAddress)
        AuthenticationService->>UserRepository: findByUsernameOrEmail(username)
        UserRepository->>Database: SELECT * FROM users...
        Database-->>UserRepository: User (failed_login_attempts = N)
        
        AuthenticationService->>PasswordEncoder: matches(password, hash)
        PasswordEncoder-->>AuthenticationService: false
        
        AuthenticationService->>UserRepository: Update user<br/>failed_login_attempts = N + 1
        UserRepository->>Database: UPDATE users SET<br/>failed_login_attempts = N + 1
        
        AuthenticationService-->>AuthController: throw InvalidCredentialsException
        AuthController-->>Frontend: 401 Unauthorized
        Frontend->>User: Display error
    end
    
    Note over User,Database: Attempt 5: Account gets locked
    
    User->>Frontend: Enter wrong password (5th attempt)
    Frontend->>AuthController: POST /api/auth/login
    AuthController->>AuthenticationService: login(LoginRequest, ipAddress)
    AuthenticationService->>UserRepository: findByUsernameOrEmail(username)
    UserRepository->>Database: SELECT * FROM users...
    Database-->>UserRepository: User (failed_login_attempts = 4)
    
    AuthenticationService->>PasswordEncoder: matches(password, hash)
    PasswordEncoder-->>AuthenticationService: false
    
    AuthenticationService->>AuthenticationService: failed_login_attempts + 1 >= 5
    AuthenticationService->>UserRepository: Update user<br/>- failed_login_attempts = 5<br/>- is_locked = true<br/>- account_locked_until = now + 15 min
    UserRepository->>Database: UPDATE users SET<br/>is_locked = true,<br/>account_locked_until = now + 15 min
    
    AuthenticationService-->>AuthController: throw AccountLockedException
    AuthController-->>Frontend: 423 Locked<br/>{message: "Account locked for 15 minutes"}
    Frontend->>User: Display lockout message
    
    Note over User,Database: Subsequent attempts during lockout
    
    User->>Frontend: Try to login again
    Frontend->>AuthController: POST /api/auth/login
    AuthController->>AuthenticationService: login(LoginRequest, ipAddress)
    AuthenticationService->>UserRepository: findByUsernameOrEmail(username)
    UserRepository->>Database: SELECT * FROM users...
    Database-->>UserRepository: User (is_locked = true)
    
    AuthenticationService->>AuthenticationService: Check is_locked = true<br/>OR account_locked_until > now
    AuthenticationService-->>AuthController: throw AccountLockedException
    AuthController-->>Frontend: 423 Locked
    Frontend->>User: Display lockout message
    
    Note over User,Database: After 15 minutes
    
    User->>Frontend: Try to login with correct password
    Frontend->>AuthController: POST /api/auth/login
    AuthController->>AuthenticationService: login(LoginRequest, ipAddress)
    AuthenticationService->>UserRepository: findByUsernameOrEmail(username)
    UserRepository->>Database: SELECT * FROM users...
    Database-->>UserRepository: User (account_locked_until < now)
    
    AuthenticationService->>AuthenticationService: Check account_locked_until < now<br/>(lockout expired)
    AuthenticationService->>PasswordEncoder: matches(password, hash)
    PasswordEncoder-->>AuthenticationService: true
    
    AuthenticationService->>UserRepository: Update user<br/>- is_locked = false<br/>- failed_login_attempts = 0<br/>- account_locked_until = null
    UserRepository->>Database: UPDATE users SET...
    
    AuthenticationService-->>AuthController: LoginResponse (success)
    AuthController-->>Frontend: 200 OK
    Frontend->>User: Redirect to profile
```

---

## 8. Rate Limiting Flow

```mermaid
sequenceDiagram
    actor Attacker
    participant Frontend
    participant AuthController
    participant RateLimitingService
    participant AuthenticationService

    Note over Attacker,AuthenticationService: Rapid login attempts from same IP

    loop Attempts 1-10
        Attacker->>Frontend: POST /api/auth/login
        Frontend->>AuthController: POST /api/auth/login<br/>IP: 192.168.1.100
        
        AuthController->>RateLimitingService: isRateLimited("192.168.1.100")
        RateLimitingService->>RateLimitingService: Check attempt count for IP
        RateLimitingService->>RateLimitingService: attemptCount < 10
        RateLimitingService-->>AuthController: false (not rate limited)
        
        AuthController->>RateLimitingService: recordLoginAttempt("192.168.1.100")
        RateLimitingService->>RateLimitingService: Increment attempt count
        
        AuthController->>AuthenticationService: login(LoginRequest, ipAddress)
        AuthenticationService-->>AuthController: Response (success or failure)
        AuthController-->>Frontend: Response
    end
    
    Note over Attacker,AuthenticationService: 11th attempt - Rate limited
    
    Attacker->>Frontend: POST /api/auth/login (11th attempt)
    Frontend->>AuthController: POST /api/auth/login<br/>IP: 192.168.1.100
    
    AuthController->>RateLimitingService: isRateLimited("192.168.1.100")
    RateLimitingService->>RateLimitingService: Check attempt count for IP
    RateLimitingService->>RateLimitingService: attemptCount >= 10
    RateLimitingService-->>AuthController: true (rate limited)
    
    AuthController-->>Frontend: 429 Too Many Requests<br/>{message: "Too many login attempts.<br/>Try again in 15 minutes"}
    Frontend->>Attacker: Display rate limit error
    
    Note over Attacker,AuthenticationService: After 15 minutes
    
    RateLimitingService->>RateLimitingService: Automatic cleanup<br/>(entries older than 15 min removed)
    
    Attacker->>Frontend: POST /api/auth/login
    Frontend->>AuthController: POST /api/auth/login<br/>IP: 192.168.1.100
    
    AuthController->>RateLimitingService: isRateLimited("192.168.1.100")
    RateLimitingService->>RateLimitingService: No entry found (cleaned up)
    RateLimitingService-->>AuthController: false (not rate limited)
    
    AuthController->>AuthenticationService: login(LoginRequest, ipAddress)
    Note over AuthController,AuthenticationService: Login process continues normally
```

---

## Diagram Rendering

These diagrams are written in Mermaid syntax and can be rendered using:

1. **GitHub/GitLab**: Automatically renders Mermaid diagrams in markdown
2. **VS Code**: Install "Markdown Preview Mermaid Support" extension
3. **Online**: https://mermaid.live/
4. **Documentation Sites**: MkDocs, Docusaurus, etc.

## Key Takeaways

### Security Features Illustrated

1. **Multi-layer Validation**: Every request goes through multiple validation steps
2. **Rate Limiting**: IP-based protection against brute force attacks
3. **Account Lockout**: Automatic lockout after 5 failed attempts
4. **Token Management**: Secure generation, validation, and invalidation
5. **Password Security**: BCrypt hashing, never stored in plain text
6. **Stateless Authentication**: JWT tokens contain all necessary information

### Error Handling

All flows include comprehensive error handling:
- Invalid credentials (401)
- Account locked (423)
- Rate limited (429)
- Token expired (401)
- Invalid token (400)
- Server errors (500)

### Performance Considerations

- Database queries optimized with indexes
- In-memory caching for rate limiting
- Stateless design for horizontal scaling
- Efficient token validation

---

**Last Updated:** January 2024  
**Version:** 1.0.0  
**Format:** Mermaid Sequence Diagrams

