# Secure Authentication System

A complete secure user login system with authentication APIs built with Spring Boot 3.2.5.

## Features

- ✅ User Registration and Login
- ✅ JWT-based Authentication
- ✅ Password Encryption (BCrypt)
- ✅ Account Lockout after Failed Attempts
- ✅ Session Timeout Management
- ✅ Password Reset Functionality
- ✅ Input Validation and Sanitization
- ✅ Brute Force Protection
- ✅ HTTPS Support
- ✅ Comprehensive Security Configuration
- ✅ RESTful API Design
- ✅ Responsive Frontend UI
- ✅ Complete Test Coverage

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.2.5
- Spring Security
- Spring Data JPA
- JWT (JSON Web Tokens)
- MySQL/PostgreSQL
- Lombok
- Maven

### Frontend
- HTML5
- CSS3
- JavaScript (Vanilla)
- Responsive Design

## Project Structure

```
src/
├── main/
│   ├── java/com/webapp/auth/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Custom exceptions
│   │   ├── model/           # Entity classes
│   │   ├── repository/      # Data access layer
│   │   ├── security/        # Security components
│   │   ├── service/         # Business logic
│   │   └── util/            # Utility classes
│   └── resources/
│       ├── application.properties
│       └── static/          # Frontend files
└── test/                    # Test classes
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ or PostgreSQL 12+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd Sampleone
```

### 2. Configure Database

Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/auth_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build the project

```bash
mvn clean install
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Authentication APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/logout` | User logout |
| POST | `/api/auth/forgot-password` | Request password reset |
| POST | `/api/auth/reset-password` | Reset password |
| GET | `/api/auth/profile` | Get user profile |
| PUT | `/api/auth/profile` | Update user profile |

## Security Features

### Password Security
- BCrypt hashing with strength 12
- Minimum password requirements enforced
- Password history tracking

### Account Protection
- Maximum 5 failed login attempts
- 15-minute account lockout
- Automatic unlock after timeout

### Session Management
- 30-minute session timeout
- JWT token expiration (24 hours)
- Refresh token support (7 days)

### Input Validation
- Server-side validation
- SQL injection prevention
- XSS protection
- CSRF protection

## Testing

Run all tests:

```bash
mvn test
```

Run specific test class:

```bash
mvn test -Dtest=AuthControllerTest
```

## Configuration

Key configuration properties in `application.properties`:

```properties
# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# Security Configuration
security.max-failed-attempts=5
security.lockout-duration=900000

# Session Configuration
server.servlet.session.timeout=30m
```

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please create an issue in the repository.

