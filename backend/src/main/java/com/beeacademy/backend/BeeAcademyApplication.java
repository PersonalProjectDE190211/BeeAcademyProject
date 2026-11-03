package com.beeacademy.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point của Spring Boot application (Modular Monolith + Clean Architecture).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class BeeAcademyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeeAcademyApplication.class, args);
    }
}
