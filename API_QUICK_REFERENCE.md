# Authentication API - Quick Reference

## Base URL
```
http://localhost:8080
```

## Endpoints Summary

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/api/auth/login` | No | User login |
| POST | `/api/auth/logout` | Yes | User logout |
| POST | `/api/auth/forgot-password` | No | Request password reset |
| POST | `/api/auth/reset-password` | No | Reset password with token |
| GET | `/api/auth/profile` | Yes | Get user profile |
| POST | `/api/auth/refresh` | No | Refresh access token |
| GET | `/api/auth/health` | No | Health check |

## Quick Start

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"johndoe","password":"Password123!"}'
```

**Response:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "550e8400...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

### 2. Access Protected Endpoint
```bash
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer eyJhbGc..."
```

### 3. Refresh Token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"550e8400..."}'
```

## Test Users (Development)

| Username | Password | Status |
|----------|----------|--------|
| johndoe | Password123! | Active |
| janedoe | Password123! | Active |
| admin | Password123! | Active |

## Swagger UI

Interactive API documentation available at:
```
http://localhost:8080/swagger-ui.html
```

## Security Features

- ✅ JWT token authentication (24h expiry)
- ✅ Refresh tokens (7 days expiry)
- ✅ BCrypt password hashing (strength 12)
- ✅ Account lockout (5 failed attempts, 15 min lockout)
- ✅ Rate limiting (10 attempts per 15 min per IP)
- ✅ Input sanitization (XSS prevention)
- ✅ CORS protection
- ✅ Stateless sessions

## Error Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 400 | Bad Request |
| 401 | Unauthorized |
| 423 | Account Locked |
| 429 | Rate Limit Exceeded |
| 500 | Server Error |

## Configuration

Key settings in `application.properties`:
```properties
server.port=8080
jwt.access-token-expiration=86400000  # 24 hours
jwt.refresh-token-expiration=604800000  # 7 days
security.max-failed-attempts=5
security.lockout-duration-minutes=15
```

## Support

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Full Documentation**: See API_DOCUMENTATION.md

