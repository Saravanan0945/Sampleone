# Authentication API Documentation

## Overview

This document provides comprehensive documentation for the User Authentication REST API. The API provides secure user authentication, authorization, and profile management using JWT (JSON Web Token) based authentication.

**Base URL**: `http://localhost:8080`  
**API Version**: 1.0.0  
**Authentication**: JWT Bearer Token

---

## Table of Contents

1. [Authentication Flow](#authentication-flow)
2. [API Endpoints](#api-endpoints)
3. [Request/Response Examples](#requestresponse-examples)
4. [Error Handling](#error-handling)
5. [Security Features](#security-features)
6. [Swagger UI](#swagger-ui)

---

## Authentication Flow

### Standard Login Flow

```
1. User submits credentials → POST /api/auth/login
2. Server validates credentials
3. Server generates JWT access token (24h) and refresh token (7 days)
4. Client stores tokens securely
5. Client includes access token in Authorization header for protected endpoints
6. When access token expires, use refresh token → POST /api/auth/refresh
```

### Password Reset Flow

```
1. User requests password reset → POST /api/auth/forgot-password
2. Server generates reset token and sends email (simulated)
3. User clicks reset link with token
4. User submits new password → POST /api/auth/reset-password
5. Server validates token and updates password
```

---

## API Endpoints

### 1. User Login

**Endpoint**: `POST /api/auth/login`  
**Authentication**: Not required  
**Description**: Authenticate user with username/email and password

#### Request Body

```json
{
  "usernameOrEmail": "john.doe@example.com",
  "password": "Password123!"
}
```

#### Response (200 OK)

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

#### Error Responses

- **401 Unauthorized**: Invalid credentials
- **423 Locked**: Account locked due to multiple failed attempts
- **429 Too Many Requests**: Rate limit exceeded (10 attempts per 15 minutes)

---

### 2. User Logout

**Endpoint**: `POST /api/auth/logout`  
**Authentication**: Required (JWT Bearer Token)  
**Description**: Logout user and invalidate all refresh tokens

#### Request Headers

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response (200 OK)

```json
{
  "success": true,
  "message": "Logout successful",
  "data": "User logged out successfully",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Error Responses

- **401 Unauthorized**: Invalid or missing JWT token

---

### 3. Forgot Password

**Endpoint**: `POST /api/auth/forgot-password`  
**Authentication**: Not required  
**Description**: Initiate password reset process

#### Request Body

```json
{
  "email": "john.doe@example.com"
}
```

#### Response (200 OK)

```json
{
  "success": true,
  "message": "Password reset email sent successfully",
  "data": "If the email exists, a password reset link has been sent",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Error Responses

- **404 Not Found**: User not found with provided email

---

### 4. Reset Password

**Endpoint**: `POST /api/auth/reset-password`  
**Authentication**: Not required  
**Description**: Reset user password using reset token

#### Request Body

```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "NewPassword123!"
}
```

#### Response (200 OK)

```json
{
  "success": true,
  "message": "Password reset successful",
  "data": "Your password has been reset successfully",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Error Responses

- **400 Bad Request**: Invalid or expired reset token

---

### 5. Get User Profile

**Endpoint**: `GET /api/auth/profile`  
**Authentication**: Required (JWT Bearer Token)  
**Description**: Retrieve authenticated user's profile information

#### Request Headers

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response (200 OK)

```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "isActive": true,
  "isLocked": false,
  "lastLogin": "2024-01-15T10:30:00",
  "createdAt": "2024-01-01T08:00:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### Error Responses

- **401 Unauthorized**: Invalid or missing JWT token
- **404 Not Found**: User not found

---

### 6. Refresh Access Token

**Endpoint**: `POST /api/auth/refresh`  
**Authentication**: Not required (uses refresh token)  
**Description**: Generate new access token using refresh token

#### Request Body

```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### Response (200 OK)

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

#### Error Responses

- **401 Unauthorized**: Invalid or expired refresh token

---

### 7. Health Check

**Endpoint**: `GET /api/auth/health`  
**Authentication**: Not required  
**Description**: Check if authentication service is running

#### Response (200 OK)

```json
{
  "success": true,
  "message": "Authentication service is running",
  "data": "OK",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Request/Response Examples

### cURL Examples

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john.doe@example.com",
    "password": "Password123!"
  }'
```

#### Get Profile (with JWT)

```bash
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Forgot Password

```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com"
  }'
```

#### Reset Password

```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "550e8400-e29b-41d4-a716-446655440000",
    "newPassword": "NewPassword123!"
  }'
```

#### Refresh Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

---

## Error Handling

### Standard Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

### HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 400 | Bad Request - Invalid input data |
| 401 | Unauthorized - Invalid or missing authentication |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 423 | Locked - Account locked |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error |

### Common Error Scenarios

#### Invalid Credentials (401)

```json
{
  "success": false,
  "message": "Invalid username or password",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Account Locked (423)

```json
{
  "success": false,
  "message": "Account is locked due to multiple failed login attempts. Please try again after 15 minutes.",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Rate Limit Exceeded (429)

```json
{
  "success": false,
  "message": "Too many login attempts. Please try again later.",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Validation Error (400)

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "usernameOrEmail": "Username or email is required",
    "password": "Password must be between 8 and 100 characters"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Security Features

### 1. JWT Token Authentication

- **Access Token**: Short-lived (24 hours), used for API authentication
- **Refresh Token**: Long-lived (7 days), used to obtain new access tokens
- **Algorithm**: HMAC-SHA256
- **Token Format**: `Authorization: Bearer <token>`

### 2. Password Security

- **Hashing**: BCrypt with strength 12
- **Minimum Length**: 8 characters
- **Validation**: Enforced on registration and password reset

### 3. Account Lockout Protection

- **Failed Attempts Threshold**: 5 attempts
- **Lockout Duration**: 15 minutes
- **Automatic Unlock**: After lockout duration expires
- **Counter Reset**: On successful login

### 4. Rate Limiting (Brute Force Protection)

- **IP-Based Tracking**: 10 login attempts per 15 minutes
- **Automatic Cleanup**: Expired entries removed automatically
- **Configurable**: Via application.properties

### 5. Input Sanitization

- **XSS Prevention**: All user inputs sanitized using HtmlUtils
- **Validation**: Jakarta Bean Validation annotations
- **SQL Injection**: Protected by JPA/Hibernate parameterized queries

### 6. CORS Configuration

- **Allowed Origins**: Configurable (default: localhost:3000, localhost:8080)
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Credentials**: Enabled for cookie-based authentication
- **Max Age**: 3600 seconds

### 7. Session Management

- **Stateless**: No server-side sessions
- **JWT-Based**: All authentication via tokens
- **Token Rotation**: Refresh tokens rotated on use

---

## Swagger UI

### Accessing Swagger Documentation

The API documentation is available via Swagger UI at:

**URL**: `http://localhost:8080/swagger-ui.html`

### Features

- **Interactive API Testing**: Test all endpoints directly from the browser
- **Request/Response Examples**: View sample requests and responses
- **Authentication**: Test secured endpoints with JWT tokens
- **Schema Documentation**: View all DTOs and models

### Using Swagger UI

1. **Navigate** to `http://localhost:8080/swagger-ui.html`
2. **Expand** an endpoint to view details
3. **Click** "Try it out" to test the endpoint
4. **Fill** in request parameters/body
5. **Execute** to send the request
6. **View** the response

### Authenticating in Swagger

1. **Login** via `/api/auth/login` endpoint
2. **Copy** the `accessToken` from the response
3. **Click** the "Authorize" button (lock icon) at the top
4. **Enter**: `Bearer <your-access-token>`
5. **Click** "Authorize"
6. **Test** secured endpoints

---

## Configuration

### Application Properties

Key configuration properties in `application.properties`:

```properties
# Server Configuration
server.port=8080

# JWT Configuration
jwt.secret=your-secret-key-here-change-in-production-min-256-bits
jwt.access-token-expiration=86400000
jwt.refresh-token-expiration=604800000

# Security Configuration
security.max-failed-attempts=5
security.lockout-duration-minutes=15
security.rate-limit.max-attempts=10
security.rate-limit.window-minutes=15

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/auth_db
spring.datasource.username=root
spring.datasource.password=password

# Swagger Configuration
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/api-docs
```

---

## Testing

### Test Users (Development Only)

The following test users are available in development mode:

| Username | Email | Password | Status |
|----------|-------|----------|--------|
| johndoe | john.doe@example.com | Password123! | Active |
| janedoe | jane.doe@example.com | Password123! | Active |
| admin | admin@example.com | Password123! | Active |
| testuser | test.user@example.com | Password123! | Active |
| inactiveuser | inactive@example.com | Password123! | Inactive |
| lockeduser | locked@example.com | Password123! | Locked |

---

## Best Practices

### Client-Side Implementation

1. **Store Tokens Securely**: Use httpOnly cookies or secure storage
2. **Handle Token Expiry**: Implement automatic token refresh
3. **Clear Tokens on Logout**: Remove all tokens from storage
4. **Validate Input**: Client-side validation before API calls
5. **Handle Errors Gracefully**: Display user-friendly error messages

### Security Recommendations

1. **Use HTTPS**: Always use HTTPS in production
2. **Rotate Secrets**: Change JWT secret regularly
3. **Monitor Failed Attempts**: Track and alert on suspicious activity
4. **Implement CAPTCHA**: Add CAPTCHA after multiple failed attempts
5. **Enable 2FA**: Implement two-factor authentication for sensitive operations

---

## Support

For API support or questions, contact:
- **Email**: support@webapp.com
- **Documentation**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2024-01-15 | Initial release with core authentication features |

---

## License

Apache 2.0 - See LICENSE file for details

