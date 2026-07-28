# API Documentation - Secure User Authentication System

## Overview

This document provides comprehensive API documentation for the Secure User Authentication System. The system implements JWT-based stateless authentication with advanced security features including account lockout, rate limiting, and brute force protection.

## Base URL

```
http://localhost:8080/api/auth
```

For production environments, replace with your domain and ensure HTTPS is enabled:
```
https://your-domain.com/api/auth
```

## Authentication Mechanism

The API uses **JWT (JSON Web Token)** Bearer authentication for protected endpoints.

### Token Types

1. **Access Token**: Short-lived token (24 hours) for API authentication
2. **Refresh Token**: Long-lived token (7 days) for obtaining new access tokens

### Using Authentication

Include the access token in the `Authorization` header for protected endpoints:

```
Authorization: Bearer <access_token>
```

---

## API Endpoints

### 1. User Login

Authenticates a user and returns JWT tokens.

**Endpoint:** `POST /api/auth/login`

**Authentication Required:** No

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Request Body Schema:**

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| username | string | Yes | 3-50 characters | Username or email address |
| password | string | Yes | Not blank | User password |

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
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

**Response Schema:**

| Field | Type | Description |
|-------|------|-------------|
| accessToken | string | JWT access token for API authentication |
| refreshToken | string | JWT refresh token for obtaining new access tokens |
| tokenType | string | Token type (always "Bearer") |
| expiresIn | integer | Access token expiration time in seconds |
| user | object | Authenticated user details |
| user.id | integer | User ID |
| user.username | string | Username |
| user.email | string | Email address |
| user.firstName | string | First name |
| user.lastName | string | Last name |

**Error Responses:**

**401 Unauthorized - Invalid Credentials:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password",
  "path": "/api/auth/login"
}
```

**423 Locked - Account Locked:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 423,
  "error": "Locked",
  "message": "Account is locked due to multiple failed login attempts. Please try again after 15 minutes or reset your password.",
  "path": "/api/auth/login"
}
```

**429 Too Many Requests - Rate Limited:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Too many login attempts from this IP address. Please try again in 15 minutes.",
  "path": "/api/auth/login"
}
```

**400 Bad Request - Validation Error:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "username",
      "message": "Username must be between 3 and 50 characters"
    }
  ],
  "path": "/api/auth/login"
}
```

**Example Request (cURL):**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "Password123!"
  }'
```

**Example Request (JavaScript):**
```javascript
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'johndoe',
    password: 'Password123!'
  })
});

const data = await response.json();
if (response.ok) {
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
}
```

---

### 2. User Logout

Invalidates all refresh tokens for the authenticated user.

**Endpoint:** `POST /api/auth/logout`

**Authentication Required:** Yes

**Request Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:** None

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out successfully",
  "timestamp": "2024-01-15T10:30:00.000+00:00"
}
```

**Error Responses:**

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/auth/logout"
}
```

**Example Request (cURL):**
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

**Example Request (JavaScript):**
```javascript
const token = localStorage.getItem('accessToken');
const response = await fetch('http://localhost:8080/api/auth/logout', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

if (response.ok) {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  window.location.href = '/login.html';
}
```

---

### 3. Forgot Password

Initiates the password reset process by sending a reset token to the user's email.

**Endpoint:** `POST /api/auth/forgot-password`

**Authentication Required:** No

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john.doe@example.com"
}
```

**Request Body Schema:**

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| email | string | Yes | Valid email format | User's email address |

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "If an account exists with this email, a password reset link has been sent.",
  "timestamp": "2024-01-15T10:30:00.000+00:00"
}
```

**Note:** For security reasons, the API returns the same success message whether the email exists or not, preventing email enumeration attacks.

**Error Responses:**

**400 Bad Request - Invalid Email:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ],
  "path": "/api/auth/forgot-password"
}
```

**Example Request (cURL):**
```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com"
  }'
```

**Example Request (JavaScript):**
```javascript
const response = await fetch('http://localhost:8080/api/auth/forgot-password', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: 'john.doe@example.com'
  })
});

const data = await response.json();
if (response.ok) {
  alert('Password reset instructions have been sent to your email.');
}
```

---

### 4. Reset Password

Resets the user's password using a valid reset token.

**Endpoint:** `POST /api/auth/reset-password`

**Authentication Required:** No

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "newPassword": "NewPassword123!"
}
```

**Request Body Schema:**

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| token | string | Yes | Not blank | Password reset token from email |
| newPassword | string | Yes | Min 8 characters | New password |

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Password has been reset successfully. You can now login with your new password.",
  "timestamp": "2024-01-15T10:30:00.000+00:00"
}
```

**Error Responses:**

**400 Bad Request - Invalid or Expired Token:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid or expired password reset token",
  "path": "/api/auth/reset-password"
}
```

**400 Bad Request - Weak Password:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "newPassword",
      "message": "Password must be at least 8 characters"
    }
  ],
  "path": "/api/auth/reset-password"
}
```

**Example Request (cURL):**
```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "newPassword": "NewPassword123!"
  }'
```

**Example Request (JavaScript):**
```javascript
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token');

const response = await fetch('http://localhost:8080/api/auth/reset-password', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    token: token,
    newPassword: 'NewPassword123!'
  })
});

const data = await response.json();
if (response.ok) {
  alert('Password reset successful! Redirecting to login...');
  window.location.href = '/login.html';
}
```

---

### 5. Get User Profile

Retrieves the authenticated user's profile information.

**Endpoint:** `GET /api/auth/profile`

**Authentication Required:** Yes

**Request Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:** None

**Success Response (200 OK):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "lastLogin": "2024-01-15T10:30:00.000+00:00",
  "createdAt": "2024-01-01T08:00:00.000+00:00"
}
```

**Response Schema:**

| Field | Type | Description |
|-------|------|-------------|
| id | integer | User ID |
| username | string | Username |
| email | string | Email address |
| firstName | string | First name |
| lastName | string | Last name |
| lastLogin | string (ISO 8601) | Last login timestamp |
| createdAt | string (ISO 8601) | Account creation timestamp |

**Error Responses:**

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/auth/profile"
}
```

**Example Request (cURL):**
```bash
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Request (JavaScript):**
```javascript
const token = localStorage.getItem('accessToken');
const response = await fetch('http://localhost:8080/api/auth/profile', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const profile = await response.json();
if (response.ok) {
  console.log('User Profile:', profile);
}
```

---

### 6. Refresh Access Token

Obtains a new access token using a valid refresh token.

**Endpoint:** `POST /api/auth/refresh`

**Authentication Required:** No (uses refresh token)

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Request Body Schema:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| refreshToken | string | Yes | Valid refresh token |

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
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

**Error Responses:**

**401 Unauthorized - Invalid or Expired Refresh Token:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired refresh token",
  "path": "/api/auth/refresh"
}
```

**Example Request (cURL):**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

**Example Request (JavaScript):**
```javascript
const refreshToken = localStorage.getItem('refreshToken');
const response = await fetch('http://localhost:8080/api/auth/refresh', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    refreshToken: refreshToken
  })
});

const data = await response.json();
if (response.ok) {
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
}
```

---

## Error Response Format

All error responses follow a consistent format:

```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Error Type",
  "message": "Detailed error message",
  "path": "/api/auth/endpoint",
  "errors": [
    {
      "field": "fieldName",
      "message": "Field-specific error message"
    }
  ]
}
```

### Common HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 OK | Request successful |
| 400 Bad Request | Invalid request data or validation error |
| 401 Unauthorized | Authentication required or invalid credentials |
| 403 Forbidden | Authenticated but not authorized |
| 423 Locked | Account locked due to failed login attempts |
| 429 Too Many Requests | Rate limit exceeded |
| 500 Internal Server Error | Server error |

---

## Rate Limiting

The API implements rate limiting to prevent brute force attacks and abuse.

### Login Endpoint Rate Limits

- **Per IP Address**: 10 attempts per 15 minutes
- **Per Account**: 5 failed attempts before account lockout

### Rate Limit Headers

When rate limited, the response includes:

```
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1705318200
```

### Account Lockout

After 5 failed login attempts, the account is locked for 15 minutes. During this period:
- Login attempts return `423 Locked` status
- The lockout automatically expires after 15 minutes
- Users can reset their password to unlock immediately

---

## Security Considerations

### 1. Token Storage

**Frontend (Browser):**
- Store access tokens in `localStorage` or `sessionStorage`
- Never store tokens in cookies without `HttpOnly` and `Secure` flags
- Clear tokens on logout

**Mobile Apps:**
- Use secure storage mechanisms (Keychain on iOS, Keystore on Android)
- Never store tokens in plain text

### 2. Token Transmission

- Always use HTTPS in production
- Include tokens in `Authorization` header, not in URL parameters
- Validate token on every protected endpoint request

### 3. Password Requirements

Enforce strong passwords:
- Minimum 8 characters
- Mix of uppercase, lowercase, numbers, and special characters
- Avoid common passwords

### 4. Input Validation

- All inputs are sanitized to prevent XSS attacks
- SQL injection protection via parameterized queries (JPA)
- Email format validation

### 5. HTTPS Configuration

**Production Checklist:**
- Enable HTTPS with valid SSL/TLS certificate
- Redirect HTTP to HTTPS
- Use HSTS (HTTP Strict Transport Security)
- Disable insecure protocols (TLS 1.0, 1.1)

---

## Best Practices

### 1. Token Refresh Strategy

Implement automatic token refresh before expiration:

```javascript
// Check token expiration and refresh if needed
async function ensureValidToken() {
  const token = localStorage.getItem('accessToken');
  const expiresAt = localStorage.getItem('tokenExpiresAt');
  
  if (Date.now() >= expiresAt - 60000) { // Refresh 1 minute before expiry
    const refreshToken = localStorage.getItem('refreshToken');
    const response = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
    
    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('tokenExpiresAt', Date.now() + data.expiresIn * 1000);
    }
  }
}
```

### 2. Error Handling

Implement centralized error handling:

```javascript
async function apiRequest(url, options) {
  try {
    const response = await fetch(url, options);
    
    if (response.status === 401) {
      // Token expired, try refresh
      await ensureValidToken();
      // Retry request
      return fetch(url, options);
    }
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message);
    }
    
    return response.json();
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}
```

### 3. Session Timeout

Implement client-side session timeout:

```javascript
let sessionTimeout;
const SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

function resetSessionTimeout() {
  clearTimeout(sessionTimeout);
  sessionTimeout = setTimeout(() => {
    localStorage.clear();
    window.location.href = '/login.html?timeout=true';
  }, SESSION_TIMEOUT);
}

// Reset timeout on user activity
document.addEventListener('click', resetSessionTimeout);
document.addEventListener('keypress', resetSessionTimeout);
```

---

## Testing the API

### Using Swagger UI

Access the interactive API documentation:
```
http://localhost:8080/swagger-ui.html
```

### Using Postman

1. Import the API collection
2. Set environment variables:
   - `baseUrl`: `http://localhost:8080`
   - `accessToken`: (obtained from login)
3. Test each endpoint

### Using cURL

See example cURL commands in each endpoint section above.

---

## Support and Contact

For issues, questions, or contributions:
- GitHub Issues: [Project Repository]
- Email: support@example.com
- Documentation: [Full Documentation]

---

**Last Updated:** January 2024  
**API Version:** 1.0.0  
**Spring Boot Version:** 3.2.5

