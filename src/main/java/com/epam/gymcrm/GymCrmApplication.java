package com.epam.gymcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Gym CRM REST API. {@code @SpringBootApplication} scans this package and everything
 * below it for components and turns on Spring Boot's auto-configuration (web server, data source, JPA).
 */
@SpringBootApplication
public class GymCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymCrmApplication.class, args);
    }
}
