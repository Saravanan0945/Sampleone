# Configuration Guide

## Overview

This document provides comprehensive guidance on configuring the Secure User Authentication System for different environments (development, testing, production).

---

## Table of Contents

1. [Environment Profiles](#environment-profiles)
2. [Configuration Files](#configuration-files)
3. [Environment Variables](#environment-variables)
4. [Database Configuration](#database-configuration)
5. [Security Configuration](#security-configuration)
6. [HTTPS/SSL Configuration](#httpsssl-configuration)
7. [Email Configuration](#email-configuration)
8. [Logging Configuration](#logging-configuration)
9. [Production Checklist](#production-checklist)

---

## Environment Profiles

The application supports three profiles:

### 1. **Default Profile** (application.properties)
- Used when no profile is specified
- Suitable for quick testing
- Uses MySQL database
- Moderate security settings

### 2. **Development Profile** (application-dev.properties)
- Activate with: `--spring.profiles.active=dev`
- Uses H2 in-memory database (no external database required)
- Relaxed security settings
- Verbose logging
- Swagger UI enabled
- Auto-creates database schema

### 3. **Production Profile** (application-prod.properties)
- Activate with: `--spring.profiles.active=prod`
- Requires all environment variables to be set
- Strict security settings
- HTTPS enforced
- Minimal logging
- Swagger UI disabled
- Database schema validation only

---

## Configuration Files

### File Structure

```
src/main/resources/
├── application.properties          # Default configuration
├── application-dev.properties      # Development profile
├── application-prod.properties     # Production profile (DO NOT COMMIT)
├── application-test.properties     # Test profile
├── banner.txt                      # Custom startup banner
└── db/
    └── schema.sql                  # Database schema
```

### Priority Order

Spring Boot loads configuration in this order (later overrides earlier):
1. `application.properties`
2. `application-{profile}.properties`
3. Environment variables
4. Command-line arguments

---

## Environment Variables

### Required for Production

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `prod` |
| `DB_URL` | Database JDBC URL | `jdbc:mysql://db.example.com:3306/auth_db` |
| `DB_USERNAME` | Database username | `auth_user` |
| `DB_PASSWORD` | Database password | `SecureP@ssw0rd!` |
| `JWT_SECRET` | JWT signing secret (64+ chars) | `<generated-secret>` |
| `SSL_KEYSTORE` | Path to SSL keystore | `/etc/ssl/keystore.p12` |
| `SSL_PASSWORD` | SSL keystore password | `<keystore-password>` |
| `ADMIN_USERNAME` | Admin username | `admin` |
| `ADMIN_PASSWORD` | Admin password | `<strong-password>` |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `https://app.example.com` |
| `MAIL_HOST` | SMTP server host | `smtp.gmail.com` |
| `MAIL_USERNAME` | Email username | `noreply@example.com` |
| `MAIL_PASSWORD` | Email password | `<email-password>` |
| `FRONTEND_URL` | Frontend application URL | `https://app.example.com` |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Server port | `8443` (prod), `8080` (dev) |
| `JWT_EXPIRATION` | JWT expiration (ms) | `3600000` (1 hour) |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (ms) | `86400000` (24 hours) |
| `LOG_FILE_PATH` | Log file path | `/var/log/auth-system/application.log` |
| `DB_POOL_SIZE` | Database connection pool size | `20` |
| `RATE_LIMIT_MAX_ATTEMPTS` | Max login attempts | `5` |
| `MAX_FAILED_ATTEMPTS` | Max failed login attempts | `5` |
| `LOCKOUT_DURATION` | Account lockout duration (ms) | `1800000` (30 min) |

---

## Database Configuration

### Development (H2 In-Memory)

```properties
# Automatically configured in application-dev.properties
# Access H2 Console: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:auth_db
# Username: sa
# Password: (leave empty)
```

### Production (MySQL)

```bash
# Set environment variables
export DB_URL="jdbc:mysql://localhost:3306/auth_db?useSSL=true&serverTimezone=UTC"
export DB_USERNAME="auth_user"
export DB_PASSWORD="SecurePassword123!"
```

**Create Database:**

```sql
CREATE DATABASE auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'auth_user'@'%' IDENTIFIED BY 'SecurePassword123!';
GRANT ALL PRIVILEGES ON auth_db.* TO 'auth_user'@'%';
FLUSH PRIVILEGES;
```

**Run Schema:**

```bash
mysql -u auth_user -p auth_db < src/main/resources/db/schema.sql
```

### Production (PostgreSQL)

```bash
# Set environment variables
export DB_URL="jdbc:postgresql://localhost:5432/auth_db"
export DB_USERNAME="auth_user"
export DB_PASSWORD="SecurePassword123!"
export DB_DRIVER="org.postgresql.Driver"
export HIBERNATE_DIALECT="org.hibernate.dialect.PostgreSQLDialect"
```

**Create Database:**

```sql
CREATE DATABASE auth_db;
CREATE USER auth_user WITH PASSWORD 'SecurePassword123!';
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;
```

---

## Security Configuration

### Generate JWT Secret

```bash
# Generate a strong 64-character secret
openssl rand -base64 64

# Set as environment variable
export JWT_SECRET="<generated-secret>"
```

### Password Encoding

- **Algorithm**: BCrypt
- **Strength**: 12 rounds (production), 10 rounds (development)
- **Configuration**: `security.password.encoder.strength`

### Account Lockout

```properties
# Lock account after 5 failed attempts
security.max-failed-attempts=5

# Lock for 30 minutes (1800000 ms)
security.lockout-duration=1800000
```

### Rate Limiting

```properties
# Max 5 login attempts per 15 minutes per IP
security.rate-limit.max-attempts=5
security.rate-limit.window-minutes=15
```

---

## HTTPS/SSL Configuration

### Generate Self-Signed Certificate (Development)

```bash
keytool -genkeypair \
  -alias tomcat \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore keystore.p12 \
  -validity 3650 \
  -storepass changeit \
  -dname "CN=localhost, OU=Development, O=MyCompany, L=City, ST=State, C=US"
```

### Production Certificate (Let's Encrypt)

```bash
# Install Certbot
sudo apt-get install certbot

# Generate certificate
sudo certbot certonly --standalone -d yourdomain.com

# Convert to PKCS12
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/yourdomain.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/yourdomain.com/privkey.pem \
  -out keystore.p12 \
  -name tomcat \
  -passout pass:your-password
```

### Configure HTTPS

```bash
# Set environment variables
export SSL_ENABLED=true
export SSL_KEYSTORE=/path/to/keystore.p12
export SSL_PASSWORD=your-keystore-password
export SSL_KEY_ALIAS=tomcat
```

---

## Email Configuration

### Gmail SMTP

```bash
# Enable "Less secure app access" or use App Password
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

### SendGrid

```bash
export MAIL_HOST=smtp.sendgrid.net
export MAIL_PORT=587
export MAIL_USERNAME=apikey
export MAIL_PASSWORD=your-sendgrid-api-key
```

### AWS SES

```bash
export MAIL_HOST=email-smtp.us-east-1.amazonaws.com
export MAIL_PORT=587
export MAIL_USERNAME=your-smtp-username
export MAIL_PASSWORD=your-smtp-password
```

---

## Logging Configuration

### Development

```properties
# Verbose logging
logging.level.root=INFO
logging.level.com.webapp.auth=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Production

```properties
# Minimal logging
logging.level.root=WARN
logging.level.com.webapp.auth=INFO
logging.file.name=/var/log/auth-system/application.log
logging.file.max-size=50MB
logging.file.max-history=90
```

### Log Rotation

```bash
# Create logrotate configuration
sudo nano /etc/logrotate.d/auth-system

# Add configuration:
/var/log/auth-system/*.log {
    daily
    rotate 90
    compress
    delaycompress
    missingok
    notifempty
    create 0640 appuser appuser
}
```

---

## Production Checklist

### Pre-Deployment

- [ ] **JWT Secret**: Generated strong random secret (64+ characters)
- [ ] **Database**: Production database created and configured
- [ ] **SSL Certificate**: Valid SSL certificate installed
- [ ] **Environment Variables**: All required variables set
- [ ] **Admin Credentials**: Changed from defaults
- [ ] **CORS Origins**: Set to production frontend URL only
- [ ] **Email Service**: Configured and tested
- [ ] **Logging**: Log directory created with proper permissions
- [ ] **Firewall**: Configured to allow only necessary ports
- [ ] **Database Backups**: Automated backup system configured

### Security Hardening

- [ ] **HTTPS**: Enforced (HTTP redirects to HTTPS)
- [ ] **Swagger**: Disabled in production
- [ ] **SQL Initialization**: Disabled (`spring.sql.init.mode=never`)
- [ ] **Error Messages**: Minimal information exposed
- [ ] **Rate Limiting**: Enabled and configured
- [ ] **Session Timeout**: Set to 15 minutes
- [ ] **Password Policy**: Strong password requirements enforced
- [ ] **Account Lockout**: Enabled (5 attempts, 30 min lockout)

### Monitoring

- [ ] **Health Checks**: Actuator endpoints configured
- [ ] **Metrics**: Prometheus/Grafana integration (optional)
- [ ] **Alerts**: Set up for critical errors
- [ ] **Log Aggregation**: ELK Stack or CloudWatch (optional)
- [ ] **Uptime Monitoring**: External monitoring service configured

### Testing

- [ ] **Unit Tests**: All tests passing (`mvn test`)
- [ ] **Integration Tests**: All tests passing
- [ ] **Security Tests**: Penetration testing completed
- [ ] **Load Testing**: Performance under load verified
- [ ] **Backup Restore**: Backup and restore process tested

---

## Running the Application

### Development Mode

```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Using JAR
java -jar -Dspring.profiles.active=dev target/new.com-0.0.1-SNAPSHOT.jar
```

### Production Mode

```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export DB_URL="jdbc:mysql://localhost:3306/auth_db"
export DB_USERNAME="auth_user"
export DB_PASSWORD="SecurePassword123!"
export JWT_SECRET="<your-secret>"
export SSL_KEYSTORE="/path/to/keystore.p12"
export SSL_PASSWORD="<keystore-password>"
# ... (set all other required variables)

# Run application
java -jar target/new.com-0.0.1-SNAPSHOT.jar
```

### Using Docker

```bash
# Build image
docker build -t secure-auth-system:1.0.0 .

# Run container
docker run -d \
  -p 8443:8443 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:mysql://db:3306/auth_db" \
  -e DB_USERNAME="auth_user" \
  -e DB_PASSWORD="SecurePassword123!" \
  -e JWT_SECRET="<your-secret>" \
  --name auth-system \
  secure-auth-system:1.0.0
```

---

## Troubleshooting

### Common Issues

**Issue**: Application fails to start with "Cannot load driver class"
- **Solution**: Ensure database driver dependency is in `pom.xml`

**Issue**: JWT token validation fails
- **Solution**: Verify `JWT_SECRET` is the same across all instances

**Issue**: HTTPS not working
- **Solution**: Check SSL certificate path and password

**Issue**: Database connection timeout
- **Solution**: Verify database URL, credentials, and network connectivity

**Issue**: Email not sending
- **Solution**: Check SMTP credentials and firewall rules

---

## Support

For additional help:
- Check logs: `tail -f logs/auth-system.log`
- Review documentation: `docs/` directory
- API documentation: http://localhost:8080/swagger-ui.html (dev only)

---

**Last Updated**: 2024
**Version**: 1.0.0

