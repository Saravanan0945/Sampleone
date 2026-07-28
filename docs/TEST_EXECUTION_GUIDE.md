# Test Execution Quick Reference

## Quick Start

### Run All Tests
```bash
mvn test
```

### Run Tests with Coverage Report
```bash
mvn clean test jacoco:report
```

### Run Specific Test Class
```bash
# Service Tests
mvn test -Dtest=AuthenticationServiceTest

# Controller Tests
mvn test -Dtest=AuthControllerTest

# Security Tests
mvn test -Dtest=SecurityTest

# JWT Tests
mvn test -Dtest=JwtUtilTest

# Integration Tests
mvn test -Dtest=AuthenticationIntegrationTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=AuthenticationServiceTest#testLogin_ValidCredentials_ReturnsLoginResponse
```

### Run Tests in Parallel
```bash
mvn test -T 4
```

### Skip Tests (for build only)
```bash
mvn clean install -DskipTests
```

---

## Test Output

### Successful Test Run
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.webapp.auth.service.AuthenticationServiceTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.webapp.auth.controller.AuthControllerTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.webapp.auth.security.JwtUtilTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.webapp.auth.security.SecurityTest
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.webapp.auth.integration.AuthenticationIntegrationTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 75, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

---

## Test Categories

### Unit Tests (46 tests)
- AuthenticationServiceTest: 15 tests
- AuthControllerTest: 15 tests
- JwtUtilTest: 16 tests

### Integration Tests (10 tests)
- AuthenticationIntegrationTest: 10 tests

### Security Tests (19 tests)
- SecurityTest: 19 tests

---

## Coverage Report

### Generate Coverage Report
```bash
mvn jacoco:report
```

### View Coverage Report
```bash
# Linux/Mac
open target/site/jacoco/index.html

# Windows
start target/site/jacoco/index.html
```

### Coverage Thresholds
- Service Layer: > 80%
- Controller Layer: > 80%
- Security Layer: > 80%
- Overall: > 80%

---

## Test Data

### Test Users
| Username        | Password     | Status   |
|-----------------|--------------|----------|
| integrationuser | Password123! | Active   |
| lockeduser      | Password123! | Locked   |
| inactiveuser    | Password123! | Inactive |

### Test Database
- **Type**: H2 In-Memory Database
- **URL**: jdbc:h2:mem:testdb
- **Username**: sa
- **Password**: (empty)

---

## Debugging Tests

### Run Tests in Debug Mode
```bash
mvn test -Dmaven.surefire.debug
```

### Enable Verbose Logging
```bash
mvn test -X
```

### Run Single Test with Logging
```bash
mvn test -Dtest=AuthenticationServiceTest -Dlogging.level.com.webapp.auth=DEBUG
```

---

## CI/CD Integration

### GitHub Actions
```yaml
- name: Run tests
  run: mvn clean test
  
- name: Generate coverage
  run: mvn jacoco:report
```

### Jenkins
```groovy
stage('Test') {
    steps {
        sh 'mvn clean test'
    }
}
```

---

## Test Maintenance

### Update Test Data
Edit: `src/test/resources/test-data.sql`

### Update Test Configuration
Edit: `src/test/resources/application-test.properties`

### Add New Test
1. Create test class in `src/test/java/com/webapp/auth/`
2. Use `@Test` annotation
3. Follow naming convention: `test<Method>_<Scenario>_<ExpectedResult>`
4. Run: `mvn test -Dtest=YourNewTest`

---

## Common Issues

### Issue: Tests fail with "Connection refused"
**Solution**: Check H2 database configuration in `application-test.properties`

### Issue: JWT tests fail
**Solution**: Verify JWT secret key matches in test configuration

### Issue: Integration tests fail
**Solution**: Ensure `test-data.sql` is loaded correctly with `@Sql` annotation

### Issue: Rate limiting tests fail
**Solution**: Reset rate limiting service in `@BeforeEach` method

---

## Test Statistics

- **Total Tests**: 75
- **Unit Tests**: 46
- **Integration Tests**: 10
- **Security Tests**: 19
- **Expected Coverage**: > 80%
- **Test Execution Time**: ~30-60 seconds

---

## Next Steps

1. ✅ Run all tests: `mvn test`
2. ✅ Check coverage: `mvn jacoco:report`
3. ✅ Review test results
4. ✅ Fix any failing tests
5. ✅ Commit changes

---

**For detailed test documentation, see**: `docs/TEST_SUITE_DOCUMENTATION.md`

