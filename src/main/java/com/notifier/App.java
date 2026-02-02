package com.notifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The main entry point for the Job-Scraper application.
 * This class bootstraps the Spring Boot context and enables scheduled tasks
 * via the {@link EnableScheduling} annotation.
 */
@SpringBootApplication
@EnableScheduling
public class App {

    /**
     * Starts the Spring Boot application.
     * @param args Command line arguments passed to the application.
     */
    static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}