# Deployment Guide - Secure User Authentication System

This comprehensive guide covers all aspects of deploying the Secure User Authentication System from development to production environments.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Build Instructions](#build-instructions)
3. [Database Setup](#database-setup)
4. [Configuration](#configuration)
5. [Running the Application](#running-the-application)
6. [Environment-Specific Configurations](#environment-specific-configurations)
7. [HTTPS Setup](#https-setup)
8. [Docker Deployment](#docker-deployment)
9. [Cloud Deployment](#cloud-deployment)
10. [Monitoring and Logging](#monitoring-and-logging)
11. [Security Checklist](#security-checklist)
12. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

| Software | Minimum Version | Recommended Version | Purpose |
|----------|----------------|---------------------|---------|
| Java JDK | 17 | 17 or 21 | Runtime environment |
| Maven | 3.8.0 | 3.9.x | Build tool |
| MySQL | 8.0 | 8.0.x | Primary database |
| PostgreSQL | 13 | 15.x | Alternative database |
| Git | 2.x | Latest | Version control |

### Optional Software

- Docker: 20.x or later (for containerized deployment)
- Docker Compose: 2.x or later (for multi-container setup)
- Nginx: 1.20+ (for reverse proxy)
- Redis: 6.x+ (for distributed rate limiting - future enhancement)

### System Requirements

**Development Environment:**
- CPU: 2+ cores
- RAM: 4 GB minimum, 8 GB recommended
- Disk: 2 GB free space

**Production Environment:**
- CPU: 4+ cores
- RAM: 8 GB minimum, 16 GB recommended
- Disk: 20 GB free space (with room for logs and backups)
- Network: Stable internet connection with HTTPS support

---

## Build Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/secure-auth-system.git
cd secure-auth-system
```

### 2. Build the Project

#### Using Maven

```bash
# Clean and build
mvn clean install

# Skip tests (not recommended for production)
mvn clean install -DskipTests

# Build with specific profile
mvn clean install -Pproduction
```

#### Build Output

The build process creates:
- JAR file: `target/new.com-0.0.1-SNAPSHOT.jar`
- Test reports: `target/surefire-reports/`
- Coverage reports: `target/site/jacoco/`

### 3. Verify Build

```bash
# Check JAR file
ls -lh target/*.jar

# Verify JAR contents
jar tf target/new.com-0.0.1-SNAPSHOT.jar | head -20
```

---

## Database Setup

### MySQL Setup

#### 1. Install MySQL

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
```

**macOS (Homebrew):**
```bash
brew install mysql
brew services start mysql
```

**Windows:**
Download and install from https://dev.mysql.com/downloads/installer/

#### 2. Create Database

```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE auth_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Create user
CREATE USER 'auth_user'@'localhost' IDENTIFIED BY 'your_secure_password';

# Grant privileges
GRANT ALL PRIVILEGES ON auth_system.* TO 'auth_user'@'localhost';
FLUSH PRIVILEGES;

# Exit
EXIT;
```

#### 3. Run Schema Script

```bash
# From project root
mysql -u auth_user -p auth_system < src/main/resources/db/schema.sql
```

#### 4. Load Test Data (Optional - Development Only)

```bash
mysql -u auth_user -p auth_system < src/main/resources/data.sql
```

### PostgreSQL Setup

#### 1. Install PostgreSQL

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**macOS (Homebrew):**
```bash
brew install postgresql
brew services start postgresql
```

#### 2. Create Database

```bash
# Switch to postgres user
sudo -u postgres psql

# Create database
CREATE DATABASE auth_system;

# Create user
CREATE USER auth_user WITH ENCRYPTED PASSWORD 'your_secure_password';

# Grant privileges
GRANT ALL PRIVILEGES ON DATABASE auth_system TO auth_user;

# Exit
\q
```

#### 3. Run Schema Script

```bash
psql -U auth_user -d auth_system -f src/main/resources/db/schema.sql
```

### Database Verification

```sql
-- Check tables
SHOW TABLES; -- MySQL
\dt -- PostgreSQL

-- Verify schema
DESCRIBE users; -- MySQL
\d users -- PostgreSQL

-- Check test data
SELECT username, email, is_active FROM users;
```

---

## Configuration

### Application Properties

Edit `src/main/resources/application.properties`:

#### 1. Database Configuration

**MySQL:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/auth_system?useSSL=true&serverTimezone=UTC
spring.datasource.username=auth_user
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

**PostgreSQL:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_system
spring.datasource.username=auth_user
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=org.postgresql.Driver
```

#### 2. JWT Configuration

**CRITICAL: Change these values in production!**

```properties
# Generate a strong random secret (256-bit minimum)
jwt.secret=your-256-bit-secret-key-change-this-in-production

# Token expiration (in milliseconds)
jwt.expiration=86400000  # 24 hours
jwt.refresh-expiration=604800000  # 7 days
```

**Generate Secure JWT Secret:**

```bash
# Using OpenSSL
openssl rand -base64 64

# Using Python
python3 -c "import secrets; print(secrets.token_urlsafe(64))"

# Using Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

#### 3. Server Configuration

```properties
# Server port
server.port=8080

# Context path (optional)
# server.servlet.context-path=/api

# Session timeout (30 minutes)
server.servlet.session.timeout=30m
```

#### 4. Security Configuration

```properties
# Account lockout
security.max-failed-attempts=5
security.lockout-duration-minutes=15

# Rate limiting
security.rate-limit.max-attempts=10
security.rate-limit.window-minutes=15

# Password reset
security.password-reset.expiration-hours=1
```

#### 5. Logging Configuration

```properties
# Log levels
logging.level.root=INFO
logging.level.com.webapp.auth=DEBUG
logging.level.org.springframework.security=DEBUG

# Log file
logging.file.name=logs/application.log
logging.file.max-size=10MB
logging.file.max-history=30
```

### Environment Variables

For sensitive configuration, use environment variables:

```bash
# Linux/macOS
export DB_PASSWORD="your_secure_password"
export JWT_SECRET="your-256-bit-secret-key"

# Windows (PowerShell)
$env:DB_PASSWORD="your_secure_password"
$env:JWT_SECRET="your-256-bit-secret-key"
```

Update `application.properties`:
```properties
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

---

## Running the Application

### Development Mode

#### Using Maven

```bash
# Run with default profile
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with debug mode
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

#### Using JAR

```bash
# Run the JAR
java -jar target/new.com-0.0.1-SNAPSHOT.jar

# Run with specific profile
java -jar target/new.com-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Run with custom port
java -jar target/new.com-0.0.1-SNAPSHOT.jar --server.port=9090
```

### Production Mode

#### Using systemd (Linux)

Create service file: `/etc/systemd/system/auth-system.service`

```ini
[Unit]
Description=Secure User Authentication System
After=syslog.target network.target

[Service]
User=appuser
Group=appuser
WorkingDirectory=/opt/auth-system
ExecStart=/usr/bin/java -jar /opt/auth-system/new.com-0.0.1-SNAPSHOT.jar --spring.profiles.active=production
SuccessExitStatus=143
StandardOutput=journal
StandardError=journal
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**Start the service:**
```bash
sudo systemctl daemon-reload
sudo systemctl enable auth-system
sudo systemctl start auth-system
sudo systemctl status auth-system
```

**View logs:**
```bash
sudo journalctl -u auth-system -f
```

#### Using nohup (Simple Background Process)

```bash
nohup java -jar target/new.com-0.0.1-SNAPSHOT.jar --spring.profiles.active=production > app.log 2>&1 &
echo $! > app.pid
```

**Stop the application:**
```bash
kill $(cat app.pid)
```

### Verify Application is Running

```bash
# Check health endpoint
curl http://localhost:8080/api/auth/health

# Check Swagger UI
open http://localhost:8080/swagger-ui.html

# Check logs
tail -f logs/application.log
```



---

## Environment-Specific Configurations

### Development Profile

Create `src/main/resources/application-dev.properties`:

```properties
# Development database (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=true

# Debug logging
logging.level.com.webapp.auth=DEBUG
logging.level.org.springframework.security=DEBUG

# CORS - Allow all origins (dev only)
cors.allowed-origins=*
```

### Staging Profile

Create `src/main/resources/application-staging.properties`:

```properties
# Staging database
spring.datasource.url=jdbc:mysql://staging-db.example.com:3306/auth_system
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Moderate logging
logging.level.root=INFO
logging.level.com.webapp.auth=DEBUG

# CORS - Specific origins
cors.allowed-origins=https://staging.example.com

# JWT settings
jwt.expiration=3600000  # 1 hour (shorter for staging)
```

### Production Profile

Create `src/main/resources/application-production.properties`:

```properties
# Production database
spring.datasource.url=jdbc:mysql://prod-db.example.com:3306/auth_system?useSSL=true
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Connection pooling
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# Production logging
logging.level.root=WARN
logging.level.com.webapp.auth=INFO

# CORS - Production domain only
cors.allowed-origins=https://example.com,https://www.example.com

# HTTPS enforcement
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat

# Security headers
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict
```

---

## HTTPS Setup

### 1. Generate SSL Certificate

#### Self-Signed Certificate (Development/Testing)

```bash
# Generate keystore
keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 365 \
  -dname "CN=localhost, OU=Development, O=YourCompany, L=City, ST=State, C=US"

# Enter keystore password when prompted
```

#### Let's Encrypt Certificate (Production)

```bash
# Install Certbot
sudo apt install certbot

# Generate certificate
sudo certbot certonly --standalone -d example.com -d www.example.com

# Convert to PKCS12
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/example.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/example.com/privkey.pem \
  -out keystore.p12 \
  -name tomcat
```

### 2. Configure Application

```properties
# Enable HTTPS
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your_keystore_password
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat

# Redirect HTTP to HTTPS
server.port=8443
```

### 3. HTTP to HTTPS Redirect (Using Nginx)

```nginx
server {
    listen 80;
    server_name example.com www.example.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name example.com www.example.com;

    ssl_certificate /etc/letsencrypt/live/example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/example.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## Docker Deployment

### 1. Create Dockerfile

Create `Dockerfile` in project root:

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/auth/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Create docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: auth-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: auth_system
      MYSQL_USER: auth_user
      MYSQL_PASSWORD: auth_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./src/main/resources/db/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    networks:
      - auth-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build: .
    container_name: auth-app
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/auth_system
      SPRING_DATASOURCE_USERNAME: auth_user
      SPRING_DATASOURCE_PASSWORD: auth_password
      JWT_SECRET: ${JWT_SECRET}
      SPRING_PROFILES_ACTIVE: production
    ports:
      - "8080:8080"
    networks:
      - auth-network
    restart: unless-stopped

volumes:
  mysql_data:

networks:
  auth-network:
    driver: bridge
```

### 3. Build and Run

```bash
# Build image
docker build -t auth-system:latest .

# Run with docker-compose
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop containers
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### 4. Docker Commands

```bash
# List running containers
docker ps

# Execute command in container
docker exec -it auth-app sh

# View application logs
docker logs -f auth-app

# Restart container
docker restart auth-app

# Remove container
docker rm -f auth-app
```

---

## Cloud Deployment

### AWS Deployment

#### Option 1: AWS Elastic Beanstalk

1. Install EB CLI: `pip install awsebcli`
2. Initialize: `eb init -p "Corretto 17" auth-system --region us-east-1`
3. Create environment: `eb create production-env`
4. Deploy: `eb deploy`
5. Open: `eb open`

#### Option 2: AWS EC2

1. Launch EC2 instance (Amazon Linux 2)
2. Install Java: `sudo yum install java-17-amazon-corretto -y`
3. Upload JAR file via SCP
4. Run: `nohup java -jar app.jar > app.log 2>&1 &`

#### Option 3: AWS ECS (Docker)

1. Create ECR repository
2. Build and push Docker image
3. Create ECS task definition and service

### Azure Deployment

#### Azure App Service

1. Install Azure CLI
2. Login: `az login`
3. Create resource group
4. Create App Service plan
5. Create web app with Java 17 runtime
6. Deploy JAR file
7. Configure app settings

### Google Cloud Platform

#### Google App Engine

1. Install gcloud CLI
2. Initialize: `gcloud init`
3. Create app.yaml with Java 17 runtime
4. Deploy: `gcloud app deploy`
5. View logs: `gcloud app logs tail -s default`

---

## Monitoring and Logging

### Application Monitoring

#### Spring Boot Actuator

Enable in application.properties:
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
```

Access endpoints:
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Info: http://localhost:8080/actuator/info

#### Prometheus + Grafana

Use docker-compose to set up monitoring stack with Prometheus and Grafana for metrics visualization.

### Centralized Logging

#### ELK Stack

Configure Logstash appender in logback-spring.xml to send logs to Elasticsearch for centralized logging and analysis with Kibana.

#### CloudWatch (AWS)

Install and configure CloudWatch agent to send application logs and metrics to AWS CloudWatch for monitoring and alerting.

---

## Security Checklist

### Pre-Deployment

- [ ] Change default JWT secret to strong random value (256-bit minimum)
- [ ] Use environment variables for sensitive configuration
- [ ] Enable HTTPS with valid SSL/TLS certificate
- [ ] Configure CORS for specific origins only
- [ ] Set strong database passwords
- [ ] Disable H2 console in production
- [ ] Remove test data from production database
- [ ] Configure firewall rules (allow only necessary ports)
- [ ] Enable database encryption at rest
- [ ] Set up database backups
- [ ] Configure rate limiting
- [ ] Enable security headers (HSTS, CSP, X-Frame-Options)
- [ ] Disable stack traces in error responses
- [ ] Set up intrusion detection system
- [ ] Configure log rotation and retention

### Post-Deployment

- [ ] Verify HTTPS is working
- [ ] Test all API endpoints
- [ ] Verify rate limiting is active
- [ ] Test account lockout mechanism
- [ ] Verify JWT token expiration
- [ ] Check database connections
- [ ] Monitor application logs
- [ ] Set up alerts for errors and security events
- [ ] Perform security scan (OWASP ZAP, Burp Suite)
- [ ] Conduct penetration testing
- [ ] Review and update dependencies regularly
- [ ] Set up automated backups
- [ ] Document incident response procedures

### Regular Maintenance

- [ ] Update dependencies monthly
- [ ] Review security logs weekly
- [ ] Rotate JWT secrets quarterly
- [ ] Update SSL certificates before expiration
- [ ] Perform security audits quarterly
- [ ] Review and update firewall rules
- [ ] Test backup restoration procedures
- [ ] Monitor performance metrics
- [ ] Review and optimize database queries
- [ ] Update documentation

---

## Troubleshooting

### Common Issues

#### 1. Application Won't Start

**Error:** Port 8080 already in use

**Solution:**
```bash
# Find process using port 8080
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows

# Or change port
java -jar app.jar --server.port=9090
```

#### 2. Database Connection Failed

**Error:** Communications link failure

**Solution:**
```bash
# Check database is running
sudo systemctl status mysql  # Linux
brew services list  # macOS

# Test connection
mysql -u auth_user -p -h localhost auth_system

# Check firewall
sudo ufw status  # Linux
```

#### 3. JWT Token Invalid

**Error:** Invalid JWT signature

**Solution:**
- Verify JWT secret matches between environments
- Check token hasn't expired
- Ensure token format is correct: Bearer <token>

#### 4. Account Locked

**Error:** Account is locked

**Solution:**
```sql
-- Unlock account manually
UPDATE users 
SET is_locked = false, 
    failed_login_attempts = 0, 
    account_locked_until = NULL 
WHERE username = 'johndoe';
```

#### 5. Out of Memory

**Error:** java.lang.OutOfMemoryError

**Solution:**
```bash
# Increase heap size
java -Xms512m -Xmx2048m -jar app.jar

# Monitor memory usage
jstat -gc <PID> 1000
```

### Debug Mode

```bash
# Enable debug logging
java -jar app.jar --logging.level.com.webapp.auth=DEBUG

# Enable Spring Security debug
java -jar app.jar --logging.level.org.springframework.security=DEBUG

# Enable SQL logging
java -jar app.jar --logging.level.org.hibernate.SQL=DEBUG
```

### Health Checks

```bash
# Application health
curl http://localhost:8080/api/auth/health

# Database connection
curl http://localhost:8080/actuator/health/db

# Disk space
curl http://localhost:8080/actuator/health/diskSpace
```

---

## Support and Resources

### Documentation
- Spring Boot Documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/
- Spring Security Documentation: https://docs.spring.io/spring-security/reference/
- JWT.io: https://jwt.io/

### Community
- Stack Overflow: [spring-boot] [spring-security] tags
- GitHub Issues: [Project Repository]
- Email: support@example.com

---

**Last Updated:** January 2024  
**Version:** 1.0.0  
**Maintained By:** Development Team

