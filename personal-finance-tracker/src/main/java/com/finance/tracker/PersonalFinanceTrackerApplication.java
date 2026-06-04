package com.finance.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PersonalFinanceTrackerApplication
 *
 * Entry point for the Spring Boot application.
 *
 * @SpringBootApplication enables:
 *   - @Configuration        → registers beans
 *   - @EnableAutoConfiguration → auto-configures MongoDB, Web, etc.
 *   - @ComponentScan        → scans all sub-packages for @Component, @Service, etc.
 *
 * On startup:
 *   1. Spring connects to MongoDB at the URI in application.properties
 *   2. Spring Data MongoDB creates the "transactions" collection automatically
 *   3. REST endpoints become available at http://localhost:8080/api/transactions
 */
@SpringBootApplication
public class PersonalFinanceTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalFinanceTrackerApplication.class, args);
        System.out.println("""
                
                ╔══════════════════════════════════════════════╗
                ║      Personal Finance Tracker Started!        ║
                ║  API Base URL: http://localhost:8080          ║
                ║  Docs URL:     /api/transactions              ║
                ╚══════════════════════════════════════════════╝
                """);
    }
}
