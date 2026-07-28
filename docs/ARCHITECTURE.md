# System Architecture - Secure User Authentication System

## Table of Contents
1. [Overview](#overview)
2. [Architecture Diagram](#architecture-diagram)
3. [Technology Stack](#technology-stack)
4. [Component Architecture](#component-architecture)
5. [Security Architecture](#security-architecture)
6. [Database Schema](#database-schema)
7. [Data Flow](#data-flow)
8. [Scalability Considerations](#scalability-considerations)

---

## Overview

The Secure User Authentication System is built using a **3-tier architecture** pattern, separating concerns into presentation, business logic, and data access layers. This architecture ensures maintainability, scalability, and security.

### Architecture Principles

- **Separation of Concerns**: Each layer has distinct responsibilities
- **Stateless Authentication**: JWT-based authentication without server-side sessions
- **Security First**: Multiple layers of security controls
- **RESTful Design**: Standard HTTP methods and status codes
- **Scalability**: Horizontal scaling capability

---

## Architecture Diagram

### High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Browser    │  │ Mobile App   │  │  Desktop App │          │
│  │ (HTML/CSS/JS)│  │   (Native)   │  │   (Electron) │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
│         │                  │                  │                   │
│         └──────────────────┴──────────────────┘                   │
│                            │                                      │
│                     HTTPS/REST API                                │
└────────────────────────────┼─────────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────┐
│                    APPLICATION LAYER                              │
│                            │                                      │
│  ┌─────────────────────────▼──────────────────────────┐          │
│  │         Spring Boot Application Server              │          │
│  │                                                      │          │
│  │  ┌────────────────────────────────────────────┐    │          │
│  │  │      PRESENTATION LAYER                     │    │          │
│  │  │  ┌──────────────────────────────────────┐  │    │          │
│  │  │  │   REST Controllers                    │  │    │          │
│  │  │  │  - AuthController                     │  │    │          │
│  │  │  │  - Exception Handlers                 │  │    │          │
│  │  │  └──────────────────────────────────────┘  │    │          │
│  │  └────────────────────────────────────────────┘    │          │
│  │                       ▼                             │          │
│  │  ┌────────────────────────────────────────────┐    │          │
│  │  │      SECURITY LAYER                         │    │          │
│  │  │  ┌──────────────────────────────────────┐  │    │          │
│  │  │  │   Spring Security                     │  │    │          │
│  │  │  │  - JwtAuthenticationFilter            │  │    │          │
│  │  │  │  - CustomUserDetailsService           │  │    │          │
│  │  │  │  - SecurityConfig                     │  │    │          │
│  │  │  └──────────────────────────────────────┘  │    │          │
│  │  └────────────────────────────────────────────┘    │          │
│  │                       ▼                             │          │
│  │  ┌────────────────────────────────────────────┐    │          │
│  │  │      BUSINESS LOGIC LAYER                   │    │          │
│  │  │  ┌──────────────────────────────────────┐  │    │          │
│  │  │  │   Services                            │  │    │          │
│  │  │  │  - AuthenticationService              │  │    │          │
│  │  │  │  - UserService                        │  │    │          │
│  │  │  │  - RegistrationService                │  │    │          │
│  │  │  │  - RateLimitingService                │  │    │          │
│  │  │  └──────────────────────────────────────┘  │    │          │
│  │  └────────────────────────────────────────────┘    │          │
│  │                       ▼                             │          │
│  │  ┌────────────────────────────────────────────┐    │          │
│  │  │      DATA ACCESS LAYER                      │    │          │
│  │  │  ┌──────────────────────────────────────┐  │    │          │
│  │  │  │   Repositories (JPA)                  │  │    │          │
│  │  │  │  - UserRepository                     │  │    │          │
│  │  │  │  - RefreshTokenRepository             │  │    │          │
│  │  │  │  - PasswordResetTokenRepository       │  │    │          │
│  │  │  └──────────────────────────────────────┘  │    │          │
│  │  └────────────────────────────────────────────┘    │          │
│  │                       ▼                             │          │
│  │  ┌────────────────────────────────────────────┐    │          │
│  │  │      UTILITY LAYER                          │    │          │
│  │  │  - JwtUtil (Token Management)               │    │          │
│  │  │  - Input Sanitization                       │    │          │
│  │  └────────────────────────────────────────────┘    │          │
│  └──────────────────────────────────────────────────┘          │
└────────────────────────────┼─────────────────────────────────────┘
                             │
                        JDBC/JPA
                             │
┌────────────────────────────▼─────────────────────────────────────┐
│                      DATABASE LAYER                               │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │   Relational Database (MySQL/PostgreSQL)                  │    │
│  │                                                            │    │
│  │   Tables:                                                  │    │
│  │   - users                                                  │    │
│  │   - refresh_tokens                                         │    │
│  │   - password_reset_tokens                                  │    │
│  └──────────────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Backend Technologies

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Framework** | Spring Boot | 3.2.5 | Application framework |
| **Language** | Java | 17+ | Programming language |
| **Security** | Spring Security | 6.x | Authentication & authorization |
| **JWT** | JJWT | 0.12.5 | Token generation & validation |
| **ORM** | Hibernate/JPA | 6.x | Object-relational mapping |
| **Database** | MySQL/PostgreSQL | 8.0+/13+ | Data persistence |
| **Password Hashing** | BCrypt | - | Password encryption |
| **Build Tool** | Maven | 3.8+ | Dependency management |
| **Validation** | Hibernate Validator | 8.x | Input validation |
| **Logging** | SLF4J + Logback | - | Application logging |
| **API Documentation** | SpringDoc OpenAPI | 2.x | API documentation |

### Frontend Technologies

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Markup** | HTML5 | Structure |
| **Styling** | CSS3 | Presentation |
| **Scripting** | JavaScript (ES6+) | Interactivity |
| **Icons** | Font Awesome | UI icons |
| **HTTP Client** | Fetch API | API communication |

### Development Tools

- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **Version Control**: Git
- **Testing**: JUnit 5, Mockito, MockMvc
- **Code Coverage**: JaCoCo
- **API Testing**: Postman, Swagger UI

---

## Component Architecture

### 1. Presentation Layer (Controllers)

**Responsibility**: Handle HTTP requests and responses

**Components**:
- `AuthController`: Authentication endpoints
- `GlobalExceptionHandler`: Centralized exception handling

**Key Features**:
- Request validation using `@Valid`
- DTO mapping
- HTTP status code management
- CORS configuration
- Swagger/OpenAPI annotations

**Example Flow**:
```
HTTP Request → Controller → Validation → Service Layer → Response
```

---

### 2. Security Layer

**Responsibility**: Authentication and authorization

**Components**:

#### JwtAuthenticationFilter
- Intercepts all HTTP requests
- Extracts JWT from Authorization header
- Validates token signature and expiration
- Sets authentication in SecurityContext

#### CustomUserDetailsService
- Loads user details from database
- Supports username and email login
- Returns Spring Security UserDetails

#### SecurityConfig
- Configures security filter chain
- Defines public and protected endpoints
- Configures CORS and CSRF
- Sets session management to STATELESS

**Security Flow**:
```
Request → JwtAuthenticationFilter → Token Validation → 
UserDetailsService → Authentication → SecurityContext → Controller
```

---

### 3. Business Logic Layer (Services)

**Responsibility**: Core business logic and rules

**Components**:

#### AuthenticationService
- User login with credential validation
- Account lockout management
- Token generation and refresh
- Password reset flow
- Logout (token invalidation)

#### UserService
- User profile management
- Password change
- Profile updates

#### RegistrationService
- New user registration
- Username/email availability checks
- Input sanitization

#### RateLimitingService
- IP-based rate limiting
- Brute force protection
- In-memory cache with TTL

**Service Layer Patterns**:
- Transactional operations (`@Transactional`)
- Exception handling
- Input validation and sanitization
- Logging

---

### 4. Data Access Layer (Repositories)

**Responsibility**: Database operations

**Components**:
- `UserRepository`: User CRUD operations
- `RefreshTokenRepository`: Token management
- `PasswordResetTokenRepository`: Reset token management

**Repository Pattern**:
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

**Benefits**:
- Abstraction over database operations
- Type-safe queries
- Automatic query generation
- Transaction management

---

### 5. Utility Layer

**Components**:

#### JwtUtil
- Token generation (access & refresh)
- Token parsing and validation
- Claims extraction
- Expiration checking

#### Input Sanitization
- XSS prevention using `HtmlUtils.htmlEscape()`
- Applied to all user inputs

---

## Security Architecture

### 1. Authentication Flow

```
┌──────┐                                                    ┌──────────┐
│Client│                                                    │  Server  │
└──┬───┘                                                    └────┬─────┘
   │                                                             │
   │  1. POST /api/auth/login                                   │
   │    { username, password }                                  │
   ├────────────────────────────────────────────────────────────>
   │                                                             │
   │                    2. Validate Credentials                 │
   │                       - Check user exists                  │
   │                       - Check account not locked           │
   │                       - Verify password (BCrypt)           │
   │                       - Check rate limit                   │
   │                                                             │
   │                    3. Generate Tokens                      │
   │                       - Create JWT access token            │
   │                       - Create refresh token               │
   │                       - Save refresh token to DB           │
   │                                                             │
   │  4. Return tokens + user details                           │
   <────────────────────────────────────────────────────────────┤
   │    { accessToken, refreshToken, user }                     │
   │                                                             │
   │  5. Store tokens in localStorage                           │
   │                                                             │
   │  6. GET /api/auth/profile                                  │
   │     Authorization: Bearer <accessToken>                    │
   ├────────────────────────────────────────────────────────────>
   │                                                             │
   │                    7. Validate Token                       │
   │                       - Extract from header                │
   │                       - Verify signature                   │
   │                       - Check expiration                   │
   │                       - Load user details                  │
   │                                                             │
   │  8. Return user profile                                    │
   <────────────────────────────────────────────────────────────┤
   │    { id, username, email, ... }                            │
   │                                                             │
```

### 2. JWT Token Structure

**Access Token** (24-hour expiration):
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "johndoe",
    "iat": 1705318200,
    "exp": 1705404600
  },
  "signature": "HMACSHA256(base64UrlEncode(header) + '.' + base64UrlEncode(payload), secret)"
}
```

**Refresh Token** (7-day expiration):
- Stored in database with user reference
- Used to obtain new access tokens
- Invalidated on logout

### 3. Password Security

**Hashing Algorithm**: BCrypt with strength 12

**Process**:
1. User provides plain text password
2. BCrypt generates salt automatically
3. Password + salt hashed 2^12 times
4. Hash stored in database (60 characters)

**Example**:
```
Plain: Password123!
Hash: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVv.o0dQpe
```

### 4. Account Lockout Mechanism

**Trigger**: 5 failed login attempts

**Lockout Duration**: 15 minutes

**Process**:
```
Login Attempt → Password Validation
                     │
                     ├─ Valid → Reset failed_login_attempts = 0
                     │
                     └─ Invalid → Increment failed_login_attempts
                                      │
                                      ├─ < 5 → Allow retry
                                      │
                                      └─ >= 5 → Lock account
                                                - Set is_locked = true
                                                - Set account_locked_until = now + 15 min
                                                - Return 423 Locked
```

**Unlock Methods**:
1. Automatic after 15 minutes
2. Password reset
3. Manual admin intervention

### 5. Rate Limiting (Brute Force Protection)

**Strategy**: IP-based rate limiting

**Limits**:
- 10 login attempts per 15 minutes per IP
- Configurable via `application.properties`

**Implementation**:
```java
ConcurrentHashMap<String, LoginAttemptInfo>
    - Key: IP Address
    - Value: { attemptCount, firstAttemptTime }
```

**Cleanup**: Automatic removal of expired entries

### 6. Input Sanitization

**XSS Prevention**:
```java
String sanitized = HtmlUtils.htmlEscape(userInput);
```

**Applied to**:
- Username
- Email
- First name
- Last name
- All user-provided text

**SQL Injection Prevention**:
- JPA parameterized queries
- No raw SQL with user input

---

## Database Schema

### Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                          users                               │
├─────────────────────────────────────────────────────────────┤
│ PK  id                    BIGINT AUTO_INCREMENT              │
│     username              VARCHAR(50) UNIQUE NOT NULL        │
│     email                 VARCHAR(100) UNIQUE NOT NULL       │
│     password_hash         VARCHAR(255) NOT NULL              │
│     first_name            VARCHAR(50)                        │
│     last_name             VARCHAR(50)                        │
│     is_active             BOOLEAN DEFAULT true               │
│     is_locked             BOOLEAN DEFAULT false              │
│     failed_login_attempts INT DEFAULT 0                      │
│     last_login_timestamp  TIMESTAMP                          │
│     account_locked_until  TIMESTAMP                          │
│     created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP│
│     updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP│
└─────────────────────────────────────────────────────────────┘
                              │
                              │ 1
                              │
                              │ *
┌─────────────────────────────▼───────────────────────────────┐
│                     refresh_tokens                           │
├─────────────────────────────────────────────────────────────┤
│ PK  id                    BIGINT AUTO_INCREMENT              │
│ FK  user_id               BIGINT NOT NULL                    │
│     token                 VARCHAR(500) UNIQUE NOT NULL       │
│     expiry_date           TIMESTAMP NOT NULL                 │
│     created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP│
└─────────────────────────────────────────────────────────────┘

                              │
                              │ 1
                              │
                              │ *
┌─────────────────────────────▼───────────────────────────────┐
│                password_reset_tokens                         │
├─────────────────────────────────────────────────────────────┤
│ PK  id                    BIGINT AUTO_INCREMENT              │
│ FK  user_id               BIGINT NOT NULL                    │
│     token                 VARCHAR(255) UNIQUE NOT NULL       │
│     expiry_date           TIMESTAMP NOT NULL                 │
│     is_used               BOOLEAN DEFAULT false              │
│     created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP│
└─────────────────────────────────────────────────────────────┘
```

### Table Descriptions

#### users
**Purpose**: Store user account information

**Key Columns**:
- `password_hash`: BCrypt hashed password (never plain text)
- `is_locked`: Account lockout status
- `failed_login_attempts`: Counter for failed logins
- `account_locked_until`: Temporary lockout expiration

**Indexes**:
- PRIMARY KEY on `id`
- UNIQUE INDEX on `username`
- UNIQUE INDEX on `email`
- INDEX on `is_active` (for active user queries)

#### refresh_tokens
**Purpose**: Store JWT refresh tokens

**Key Columns**:
- `token`: JWT refresh token string
- `expiry_date`: Token expiration timestamp
- `user_id`: Foreign key to users table

**Indexes**:
- PRIMARY KEY on `id`
- UNIQUE INDEX on `token`
- INDEX on `user_id`
- INDEX on `expiry_date` (for cleanup queries)

**Cascade**: DELETE on user deletion

#### password_reset_tokens
**Purpose**: Store password reset tokens

**Key Columns**:
- `token`: UUID reset token
- `expiry_date`: Token expiration (1 hour)
- `is_used`: One-time use flag

**Indexes**:
- PRIMARY KEY on `id`
- UNIQUE INDEX on `token`
- INDEX on `user_id`
- COMPOSITE INDEX on `expiry_date, is_used`

**Cascade**: DELETE on user deletion

---

## Data Flow

### 1. Login Flow

```
User Input (username, password)
    │
    ▼
Frontend Validation
    │
    ▼
POST /api/auth/login
    │
    ▼
AuthController.login()
    │
    ▼
AuthenticationService.login()
    │
    ├─► RateLimitingService.isRateLimited(ip)
    │       │
    │       ├─ Yes → Throw TooManyRequestsException (429)
    │       └─ No → Continue
    │
    ├─► UserRepository.findByUsernameOrEmail()
    │       │
    │       ├─ Not Found → Throw UserNotFoundException (404)
    │       └─ Found → Continue
    │
    ├─► Check is_locked or account_locked_until
    │       │
    │       ├─ Locked → Throw AccountLockedException (423)
    │       └─ Not Locked → Continue
    │
    ├─► PasswordEncoder.matches(password, hash)
    │       │
    │       ├─ Invalid → Increment failed_login_attempts
    │       │              │
    │       │              ├─ >= 5 → Lock account
    │       │              └─ Throw InvalidCredentialsException (401)
    │       │
    │       └─ Valid → Reset failed_login_attempts = 0
    │                  Update last_login_timestamp
    │
    ├─► JwtUtil.generateToken(username)
    │       │
    │       └─ Return accessToken
    │
    ├─► JwtUtil.generateRefreshToken(username)
    │       │
    │       └─ Return refreshToken
    │
    ├─► RefreshTokenRepository.save(refreshToken)
    │
    └─► Return LoginResponse
            │
            ▼
        Frontend stores tokens
            │
            ▼
        Redirect to profile page
```

### 2. Protected Endpoint Access Flow

```
User Request with Authorization Header
    │
    ▼
JwtAuthenticationFilter.doFilterInternal()
    │
    ├─► Extract token from "Bearer <token>"
    │       │
    │       └─ No token → Continue (will be rejected by SecurityConfig)
    │
    ├─► JwtUtil.extractUsername(token)
    │       │
    │       └─ Invalid token → Log error, continue
    │
    ├─► CustomUserDetailsService.loadUserByUsername()
    │       │
    │       └─ Return UserDetails
    │
    ├─► JwtUtil.validateToken(token, userDetails)
    │       │
    │       ├─ Invalid → Continue (unauthorized)
    │       └─ Valid → Set authentication in SecurityContext
    │
    ▼
Controller method executes
    │
    ▼
Return response
```

### 3. Password Reset Flow

```
User enters email
    │
    ▼
POST /api/auth/forgot-password
    │
    ▼
AuthenticationService.forgotPassword()
    │
    ├─► UserRepository.findByEmail()
    │       │
    │       └─ Not Found → Return success (prevent enumeration)
    │
    ├─► Generate UUID token
    │
    ├─► PasswordResetTokenRepository.save()
    │       - token
    │       - expiry_date = now + 1 hour
    │       - is_used = false
    │
    ├─► Send email with reset link
    │       (Simulated with log message)
    │
    └─► Return success message
            │
            ▼
        User clicks link in email
            │
            ▼
        GET /reset-password.html?token=<token>
            │
            ▼
        User enters new password
            │
            ▼
        POST /api/auth/reset-password
            │
            ▼
        AuthenticationService.resetPassword()
            │
            ├─► PasswordResetTokenRepository.findByToken()
            │       │
            │       ├─ Not Found → Throw InvalidTokenException
            │       └─ Found → Validate expiry and is_used
            │
            ├─► PasswordEncoder.encode(newPassword)
            │
            ├─► UserRepository.save()
            │       - Update password_hash
            │       - Set is_locked = false
            │       - Set failed_login_attempts = 0
            │
            ├─► Mark token as used (is_used = true)
            │
            ├─► RefreshTokenRepository.deleteByUser()
            │       (Invalidate all sessions)
            │
            └─► Return success message
                    │
                    ▼
                Redirect to login page
```

---

## Scalability Considerations

### 1. Horizontal Scaling

**Stateless Design**:
- No server-side sessions
- JWT tokens contain all necessary information
- Multiple application instances can run in parallel

**Load Balancing**:
```
                    ┌─────────────┐
                    │Load Balancer│
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐       ┌────▼────┐       ┌────▼────┐
   │ App     │       │ App     │       │ App     │
   │Instance1│       │Instance2│       │Instance3│
   └────┬────┘       └────┬────┘       └────┬────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
                    ┌──────▼──────┐
                    │  Database   │
                    └─────────────┘
```

### 2. Database Optimization

**Connection Pooling**:
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

**Indexes**:
- All foreign keys indexed
- Frequently queried columns indexed
- Composite indexes for multi-column queries

**Query Optimization**:
- Use JPA query methods
- Avoid N+1 queries
- Use pagination for large result sets

### 3. Caching Strategy

**Rate Limiting Cache**:
- In-memory ConcurrentHashMap
- TTL-based cleanup
- Can be replaced with Redis for distributed systems

**Future Enhancements**:
- Cache user details (Redis)
- Cache JWT validation results
- Distributed session management

### 4. Monitoring and Logging

**Application Metrics**:
- Login success/failure rates
- Token generation/validation times
- API response times
- Error rates

**Logging Levels**:
- INFO: Successful operations
- WARN: Failed login attempts, rate limiting
- ERROR: System errors, exceptions

**Log Aggregation**:
- ELK Stack (Elasticsearch, Logback, Kibana)
- CloudWatch (AWS)
- Application Insights (Azure)

### 5. Security Enhancements

**Production Checklist**:
- [ ] Enable HTTPS with valid SSL certificate
- [ ] Change default JWT secret to strong random value
- [ ] Configure CORS for specific origins
- [ ] Enable rate limiting on all endpoints
- [ ] Implement request logging and monitoring
- [ ] Set up intrusion detection
- [ ] Regular security audits
- [ ] Dependency vulnerability scanning

---

## Deployment Architecture

### Development Environment
```
Developer Machine
    │
    ├─ Spring Boot (embedded Tomcat)
    ├─ H2 In-Memory Database
    └─ Port 8080
```

### Production Environment
```
                    ┌─────────────┐
                    │   CDN       │
                    │ (Static)    │
                    └─────────────┘
                           │
                    ┌──────▼──────┐
                    │   Nginx     │
                    │(Reverse Proxy)│
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │Load Balancer│
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐       ┌────▼────┐       ┌────▼────┐
   │ Spring  │       │ Spring  │       │ Spring  │
   │ Boot    │       │ Boot    │       │ Boot    │
   │ App 1   │       │ App 2   │       │ App 3   │
   └────┬────┘       └────┬────┘       └────┬────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
                    ┌──────▼──────┐
                    │  MySQL      │
                    │  Primary    │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  MySQL      │
                    │  Replica    │
                    └─────────────┘
```

---

## Conclusion

This architecture provides:
- ✅ **Security**: Multiple layers of protection
- ✅ **Scalability**: Horizontal scaling capability
- ✅ **Maintainability**: Clear separation of concerns
- ✅ **Performance**: Optimized database queries and caching
- ✅ **Reliability**: Comprehensive error handling and logging

The system is production-ready and follows industry best practices for secure authentication systems.

---

**Last Updated:** January 2024  
**Version:** 1.0.0  
**Author:** Development Team

