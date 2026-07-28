package com.webapp.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot Application Class for Secure Authentication System
 * 
 * This class serves as the entry point for the Spring Boot application.
 * It enables JPA repositories, transaction management, and JPA auditing.
 * 
 * @author Secure Auth Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.webapp.auth.repository")
@EnableTransactionManagement
@EnableJpaAuditing
public class AuthApplication {

    /**
     * Main method to start the Spring Boot application
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}

