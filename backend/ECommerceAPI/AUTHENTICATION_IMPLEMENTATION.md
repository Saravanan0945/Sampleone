# Authentication Service Implementation

## Overview

The authentication service has been fully implemented with JWT token generation, BCrypt password hashing, and comprehensive forgot password/reset password functionality.

## Features Implemented

### 1. **User Registration**
- ✅ Validates unique username and email
- ✅ Hashes passwords using BCrypt
- ✅ Creates user with 'Customer' role by default
- ✅ Automatically creates shopping cart for new users
- ✅ Generates JWT token upon successful registration
- ✅ Comprehensive error handling with specific exception messages

### 2. **User Login**
- ✅ Supports login with username OR email
- ✅ Verifies password using BCrypt.Verify
- ✅ Checks if user account is active
- ✅ Updates last login timestamp
- ✅ Generates JWT token with user claims (Id, Username, Email, Role)
- ✅ Token expiration set to 24 hours (configurable)

### 3. **Forgot Password**
- ✅ Validates email exists in database
- ✅ Generates unique reset token (GUID)
- ✅ Saves PasswordResetToken with 1-hour expiration
- ✅ Invalidates any existing unused tokens for the user
- ✅ Returns success (in production, would send email with token)

### 4. **Reset Password**
- ✅ Validates token exists and is not expired
- ✅ Checks if token has already been used
- ✅ Hashes new password using BCrypt
- ✅ Updates user password in database
- ✅ Marks token as used to prevent reuse

## DTOs with Validation Attributes

### RegisterDto
```csharp
- Username: Required, 3-50 characters
- Email: Required, valid email format
- Password: Required, minimum 6 characters
- FirstName: Optional, max 50 characters
- LastName: Optional, max 50 characters
```

### LoginDto
```csharp
- UsernameOrEmail: Required
- Password: Required
```

### ForgotPasswordDto
```csharp
- Email: Required, valid email format
```

### ResetPasswordDto
```csharp
- Token: Required
- NewPassword: Required, minimum 6 characters
```

### AuthResponseDto
```csharp
- Token: JWT token string
- Username: User's username
- Email: User's email
- Role: User's role (Admin/Customer)
```

## Error Handling

### Custom Exceptions
- **AuthenticationException**: Invalid credentials, inactive account (401 Unauthorized)
- **ValidationException**: Username/email exists, invalid/expired token (400 Bad Request)
- **NotFoundException**: User not found (404 Not Found)
- **InvalidOperationException**: Configuration errors (400 Bad Request)

### Error Response Format
```json
{
  "error": "Error message",
  "statusCode": 400
}
```

## API Endpoints

### POST /api/auth/register
**Request:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Success Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "johndoe",
  "email": "john@example.com",
  "role": "Customer"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Username already exists",
  "statusCode": 400
}
```

### POST /api/auth/login
**Request:**
```json
{
  "usernameOrEmail": "johndoe",
  "password": "SecurePass123"
}
```

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "johndoe",
  "email": "john@example.com",
  "role": "Customer"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "Invalid credentials",
  "statusCode": 401
}
```

### POST /api/auth/forgot-password
**Request:**
```json
{
  "email": "john@example.com"
}
```

**Success Response (200 OK):**
```json
{
  "message": "If the email exists, password reset instructions have been sent"
}
```

**Note:** For security, the response doesn't reveal if the email exists or not.

### POST /api/auth/reset-password
**Request:**
```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "newPassword": "NewSecurePass456"
}
```

**Success Response (200 OK):**
```json
{
  "message": "Password reset successfully"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Token has expired",
  "statusCode": 400
}
```

## JWT Token Structure

### Claims Included
- **NameIdentifier**: User ID
- **Name**: Username
- **Email**: User email
- **Role**: User role (Admin/Customer)

### Token Configuration (appsettings.json)
```json
{
  "JwtSettings": {
    "SecretKey": "your-secret-key-min-32-characters-long",
    "Issuer": "ECommerceAPI",
    "Audience": "ECommerceClient",
    "ExpirationHours": "24"
  }
}
```

## Security Features

1. **Password Hashing**: BCrypt with automatic salt generation
2. **JWT Authentication**: Secure token-based authentication
3. **Token Expiration**: Configurable token lifetime (default 24 hours)
4. **Password Reset Token**: 1-hour expiration, single-use tokens
5. **Account Status**: Checks if user account is active
6. **Token Invalidation**: Old reset tokens are invalidated when new ones are generated

## Database Models

### User
- Id, Username (unique), Email (unique), PasswordHash, RoleId, FirstName, LastName, CreatedAt, LastLoginAt, IsActive

### PasswordResetToken
- Id, UserId, Token (unique GUID), ExpiresAt, IsUsed, CreatedAt

### Role
- Id, Name (Admin/Customer), Description

## Testing the Authentication Flow

### 1. Register a New User
```bash
curl -X POST http://localhost:5000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123456",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "Test123456"
  }'
```

### 3. Forgot Password
```bash
curl -X POST http://localhost:5000/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

### 4. Reset Password
```bash
curl -X POST http://localhost:5000/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "token-from-database",
    "newPassword": "NewTest123456"
  }'
```

### 5. Use JWT Token for Protected Endpoints
```bash
curl -X GET http://localhost:5000/api/cart \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Implementation Details

### AuthService Methods

#### RegisterAsync
1. Validates username uniqueness
2. Validates email uniqueness
3. Retrieves Customer role from database
4. Hashes password with BCrypt
5. Creates User entity
6. Saves user to database
7. Creates shopping cart for user
8. Generates JWT token
9. Returns AuthResponseDto

#### LoginAsync
1. Finds user by username or email
2. Checks if user exists
3. Checks if account is active
4. Verifies password with BCrypt
5. Updates last login timestamp
6. Generates JWT token
7. Returns AuthResponseDto

#### ForgotPasswordAsync
1. Finds user by email
2. Checks if user exists and is active
3. Invalidates existing unused tokens
4. Generates unique GUID token
5. Creates PasswordResetToken with 1-hour expiration
6. Saves token to database
7. Returns success (email would be sent in production)

#### ResetPasswordAsync
1. Finds reset token in database
2. Validates token exists
3. Checks if token is already used
4. Checks if token is expired
5. Retrieves associated user
6. Hashes new password with BCrypt
7. Updates user password
8. Marks token as used
9. Saves changes to database

## Next Steps

To complete the authentication implementation:

1. **Email Service**: Implement email sending for password reset tokens
2. **Rate Limiting**: Add rate limiting to prevent brute force attacks
3. **Account Lockout**: Implement account lockout after failed login attempts
4. **Email Verification**: Add email verification for new registrations
5. **Refresh Tokens**: Implement refresh token mechanism for extended sessions
6. **Two-Factor Authentication**: Add 2FA support for enhanced security

## Files Modified/Created

### Created:
- `backend/ECommerceAPI/Exceptions/CustomExceptions.cs` - Custom exception classes

### Modified:
- `backend/ECommerceAPI/DTOs/RegisterDto.cs` - Added validation attributes
- `backend/ECommerceAPI/DTOs/LoginDto.cs` - Added validation attributes and AuthResponseDto
- `backend/ECommerceAPI/DTOs/PasswordDto.cs` - Added validation attributes, updated ResetPasswordDto
- `backend/ECommerceAPI/Services/AuthService.cs` - Complete implementation with error handling
- `backend/ECommerceAPI/Controllers/AuthController.cs` - Updated to use new exception-based error handling
- `backend/ECommerceAPI/Middleware/ErrorHandlingMiddleware.cs` - Enhanced to handle custom exceptions

## Conclusion

The authentication service is now fully implemented with:
- ✅ Secure JWT token generation
- ✅ BCrypt password hashing
- ✅ Username/email login support
- ✅ Comprehensive forgot password functionality
- ✅ Secure password reset with token validation
- ✅ Role-based authorization support
- ✅ Detailed error handling with specific exception messages
- ✅ Input validation with data annotations
- ✅ Automatic cart creation on registration

