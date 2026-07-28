# Frontend Implementation - Secure User Login System

## 📁 Frontend Structure

```
src/main/resources/static/
├── index.html                    # Main entry point (redirects to login/profile)
├── login.html                    # Login page
├── register.html                 # Registration page
├── forgot-password.html          # Forgot password page
├── reset-password.html           # Reset password page
├── profile.html                  # User profile page (protected)
├── css/
│   ├── login.css                 # Styles for login, register, forgot/reset password
│   └── profile.css               # Styles for profile page
└── js/
    ├── auth-check.js             # Authentication utility functions
    ├── login.js                  # Login page functionality
    ├── register.js               # Registration page functionality
    ├── forgot-password.js        # Forgot password functionality
    ├── reset-password.js         # Reset password functionality
    └── profile.js                # Profile page functionality
```

## ✨ Features Implemented

### 🔐 Security Features

1. **JWT Token Management**
   - Access token (24-hour expiry)
   - Refresh token (7-day expiry)
   - Automatic token refresh on expiry
   - Secure token storage (localStorage/sessionStorage)

2. **Session Management**
   - 30-minute inactivity timeout
   - Automatic session expiry handling
   - Activity tracking (mouse, keyboard, scroll, touch)
   - Session status display

3. **Input Validation & Sanitization**
   - Client-side validation for all inputs
   - HTML escaping to prevent XSS attacks
   - Email format validation
   - Password strength validation
   - Real-time field validation

4. **Account Security**
   - Password show/hide toggle
   - Password strength indicator
   - Account lockout protection (5 failed attempts)
   - Rate limiting (10 attempts per 15 minutes)
   - Brute force protection

### 🎨 UI/UX Features

1. **Responsive Design**
   - Mobile-first approach
   - Tablet and desktop optimized
   - Breakpoints: 480px, 768px
   - Touch-friendly controls

2. **Accessibility**
   - ARIA labels for screen readers
   - Keyboard navigation support
   - Focus visible indicators
   - Reduced motion support
   - Semantic HTML

3. **User Feedback**
   - Loading spinners during API calls
   - Success/error messages
   - Field-level error messages
   - Auto-hide messages (5 seconds)
   - Rate limit warnings

4. **Visual Design**
   - Modern gradient backgrounds
   - Card-based layouts
   - Smooth animations
   - Font Awesome icons
   - Consistent color scheme

## 📄 Page Descriptions

### 1. **index.html** - Entry Point
- Automatically redirects to login or profile based on authentication status
- Checks for valid JWT token
- Shows loading message during redirect

### 2. **login.html** - Login Page
**Features:**
- Username/email input field
- Password input with show/hide toggle
- "Remember me" checkbox
- Forgot password link
- Registration link
- Client-side validation
- Error/success messages
- Rate limit warnings

**Validation:**
- Username: 3-50 characters
- Password: minimum 8 characters
- Empty field checks

**API Integration:**
- POST /api/auth/login
- Stores JWT tokens
- Redirects to profile on success
- Handles 401 (invalid credentials), 423 (locked account), 429 (rate limited)

### 3. **register.html** - Registration Page
**Features:**
- Username input
- Email input
- First name input
- Last name input
- Password input with show/hide toggle
- Client-side validation
- Link back to login

**Validation:**
- Username: 3-50 characters, alphanumeric with _ and -
- Email: valid email format
- First/Last name: minimum 2 characters
- Password: 8+ characters with uppercase, lowercase, and number

**API Integration:**
- POST /api/auth/register
- Redirects to login on success

### 4. **forgot-password.html** - Forgot Password
**Features:**
- Email input field
- Client-side validation
- Success message with instructions
- Link back to login

**Validation:**
- Email: valid email format
- Empty field check

**API Integration:**
- POST /api/auth/forgot-password
- Shows success message
- Auto-redirects to login after 5 seconds

### 5. **reset-password.html** - Reset Password
**Features:**
- New password input with show/hide toggle
- Confirm password input with show/hide toggle
- Password strength indicator (Weak/Fair/Good/Strong)
- Real-time strength calculation
- Client-side validation
- Link back to login

**Validation:**
- Password: 8+ characters with uppercase, lowercase, and number
- Passwords must match
- Token validation from URL parameter

**API Integration:**
- POST /api/auth/reset-password
- Extracts token from URL query parameter
- Redirects to login on success

### 6. **profile.html** - User Profile (Protected)
**Features:**
- Navigation bar with logout button
- User avatar and header
- Profile details card (ID, username, email, name, last login, created date)
- Account actions (change password, refresh session)
- Session information (timeout, token expiry, status)
- Loading overlay for API calls

**Protection:**
- Requires valid JWT token
- Auto-redirects to login if not authenticated
- Session timeout tracking
- Token expiry display

**API Integration:**
- GET /api/auth/profile
- POST /api/auth/logout
- POST /api/auth/refresh

## 🔧 JavaScript Utilities

### **auth-check.js** - Core Authentication Library

**Functions:**
- `isAuthenticated()` - Check if user is authenticated
- `getToken()` - Get JWT access token
- `getRefreshToken()` - Get JWT refresh token
- `setTokens()` - Store authentication tokens
- `setUser()` - Store user information
- `getUser()` - Get stored user information
- `clearAuth()` - Clear all authentication data
- `isTokenExpired()` - Check if token is expired
- `parseJwt()` - Parse JWT token payload
- `authenticatedFetch()` - Make authenticated API requests
- `refreshToken()` - Refresh access token
- `redirectToLogin()` - Redirect to login page
- `redirectToProfile()` - Redirect to profile page
- `initSessionTimeout()` - Initialize session timeout tracking
- `protectPage()` - Protect page from unauthenticated access
- `sanitizeHTML()` - Sanitize HTML to prevent XSS
- `escapeHTML()` - Escape HTML special characters

**Configuration:**
- API_BASE_URL: '/api/auth'
- SESSION_TIMEOUT: 30 minutes (1,800,000 ms)

## 🎯 API Endpoints Used

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | /api/auth/login | User login | No |
| POST | /api/auth/register | User registration | No |
| POST | /api/auth/logout | User logout | Yes |
| POST | /api/auth/forgot-password | Request password reset | No |
| POST | /api/auth/reset-password | Reset password with token | No |
| POST | /api/auth/refresh | Refresh access token | No |
| GET | /api/auth/profile | Get user profile | Yes |

## 🚀 Usage Instructions

### Starting the Application

1. **Build and run the Spring Boot application:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

2. **Access the application:**
   - Main page: http://localhost:8080/
   - Login: http://localhost:8080/login.html
   - Register: http://localhost:8080/register.html
   - Profile: http://localhost:8080/profile.html

### Test Users (from data.sql)

| Username | Email | Password | Status |
|----------|-------|----------|--------|
| johndoe | john.doe@example.com | Password123! | Active |
| janedoe | jane.doe@example.com | Password123! | Active |
| admin | admin@example.com | Password123! | Active |
| testuser | test.user@example.com | Password123! | Active |
| inactiveuser | inactive@example.com | Password123! | Inactive |
| lockeduser | locked@example.com | Password123! | Locked |

### User Flows

#### 1. **Login Flow**
1. Navigate to http://localhost:8080/
2. Enter username/email and password
3. Optionally check "Remember me"
4. Click "Login"
5. On success, redirected to profile page
6. On failure, error message displayed

#### 2. **Registration Flow**
1. Click "Sign up" on login page
2. Fill in username, email, first name, last name, password
3. Click "Create Account"
4. On success, redirected to login page
5. Login with new credentials

#### 3. **Forgot Password Flow**
1. Click "Forgot Password?" on login page
2. Enter email address
3. Click "Send Reset Link"
4. Check console logs for reset token (email simulation)
5. Navigate to reset password page with token
6. Enter new password
7. Click "Reset Password"
8. Redirected to login page

#### 4. **Profile Access Flow**
1. Login successfully
2. View profile information
3. Click "Refresh Session" to refresh token
4. Click "Logout" to logout

## 🔒 Security Best Practices Implemented

1. **Token Security**
   - JWT tokens stored in localStorage (remember me) or sessionStorage
   - Tokens sent in Authorization header (Bearer token)
   - Automatic token refresh before expiry
   - Token validation on every protected page

2. **XSS Prevention**
   - All user inputs sanitized using HTML escaping
   - Content-Type headers properly set
   - No inline JavaScript in HTML

3. **CSRF Protection**
   - Stateless JWT authentication (no cookies)
   - CSRF disabled in Spring Security (not needed for JWT)

4. **Session Security**
   - 30-minute inactivity timeout
   - Automatic logout on timeout
   - Activity tracking to reset timeout

5. **Input Validation**
   - Client-side validation for immediate feedback
   - Server-side validation for security
   - Regex patterns for format validation

6. **Rate Limiting**
   - Client-side: 10 attempts before temporary lockout
   - Server-side: IP-based rate limiting (10 attempts per 15 minutes)
   - Account lockout after 5 failed login attempts

## 📱 Responsive Breakpoints

```css
/* Mobile First (default) */
/* 0px - 479px */

/* Small Mobile */
@media (max-width: 480px) {
    /* Smaller padding, font sizes */
}

/* Tablet */
@media (max-width: 768px) {
    /* Adjusted layouts, hidden text on nav */
}

/* Desktop */
@media (min-width: 769px) {
    /* Full layouts, all features visible */
}
```

## 🎨 Color Scheme

```css
--primary-color: #4f46e5;      /* Indigo */
--primary-hover: #4338ca;      /* Darker indigo */
--primary-light: #eef2ff;      /* Light indigo */
--success-color: #10b981;      /* Green */
--error-color: #ef4444;        /* Red */
--warning-color: #f59e0b;      /* Amber */
--text-primary: #1f2937;       /* Dark gray */
--text-secondary: #6b7280;     /* Medium gray */
--border-color: #e5e7eb;       /* Light gray */
--bg-color: #f9fafb;           /* Very light gray */
--white: #ffffff;              /* White */
```

## 🧪 Testing the Frontend

### Manual Testing Checklist

**Login Page:**
- [ ] Empty fields show validation errors
- [ ] Invalid credentials show error message
- [ ] Valid credentials redirect to profile
- [ ] Password show/hide toggle works
- [ ] Remember me checkbox stores token in localStorage
- [ ] Rate limiting shows warning after multiple failures
- [ ] Forgot password link navigates correctly

**Registration Page:**
- [ ] All fields validate correctly
- [ ] Password strength requirements enforced
- [ ] Duplicate username/email shows error
- [ ] Successful registration redirects to login

**Forgot Password Page:**
- [ ] Email validation works
- [ ] Success message shows after submission
- [ ] Auto-redirect to login after 5 seconds

**Reset Password Page:**
- [ ] Password strength indicator updates in real-time
- [ ] Passwords must match
- [ ] Invalid token shows error
- [ ] Successful reset redirects to login

**Profile Page:**
- [ ] Requires authentication (redirects if not logged in)
- [ ] Displays user information correctly
- [ ] Logout button works
- [ ] Refresh session button works
- [ ] Session timeout triggers after 30 minutes of inactivity
- [ ] Token expiry countdown updates

### Browser Compatibility

Tested and compatible with:
- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Edge 90+
- ✅ Mobile browsers (iOS Safari, Chrome Mobile)

## 📝 Notes

1. **Email Simulation**: Password reset emails are simulated with console logs. In production, integrate with an email service (SendGrid, AWS SES, etc.)

2. **HTTPS**: For production, enable HTTPS configuration in application.properties

3. **Token Storage**: Consider using httpOnly cookies for enhanced security in production

4. **Error Handling**: All API errors are caught and displayed to users with appropriate messages

5. **Accessibility**: All forms and buttons are keyboard accessible and screen reader friendly

## 🔄 Future Enhancements

- [ ] Two-factor authentication (2FA)
- [ ] Social login (Google, Facebook, GitHub)
- [ ] Email verification on registration
- [ ] Password change functionality
- [ ] Profile editing
- [ ] User avatar upload
- [ ] Activity log
- [ ] Device management
- [ ] Dark mode toggle
- [ ] Multi-language support

## 📞 Support

For issues or questions:
1. Check the browser console for errors
2. Verify backend is running on port 8080
3. Check network tab for API responses
4. Review application.properties configuration
5. Check server logs for backend errors

