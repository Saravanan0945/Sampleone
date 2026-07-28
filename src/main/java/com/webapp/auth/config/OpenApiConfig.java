package com.webapp.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * Accessible at: http://localhost:8080/swagger-ui.html
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "User Authentication API",
        version = "1.0.0",
        description = "Secure User Authentication and Authorization REST API with JWT token-based authentication. " +
                      "Provides endpoints for user login, logout, password reset, token refresh, and profile management.",
        contact = @Contact(
            name = "API Support",
            email = "support@webapp.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Local Development Server"
        ),
        @Server(
            url = "https://api.webapp.com",
            description = "Production Server"
        )
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    description = "JWT authentication token. Format: Bearer {token}",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Configuration is done via annotations
}

